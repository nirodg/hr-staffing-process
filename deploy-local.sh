#!/bin/bash

# Configuration
REGISTRY="localhost:5000"
NAMESPACE="staffing-local"
INGRESS_HOST="staffing.local"  # Changed to DNS-style name
TEMP_HOSTS_ENTRY="127.0.0.1 staffing.local"  # For local DNS resolution

# Clean previous deployment
echo "🧹 Cleaning previous deployment..."
kubectl delete namespace $NAMESPACE > /dev/null 2>&1
docker-compose down -v > /dev/null 2>&1

# Add temporary hosts entry (sudo may be required)
echo "📡 Adding temporary hosts entry..."
if ! grep -q "$INGRESS_HOST" /etc/hosts; then
  echo $TEMP_HOSTS_ENTRY | sudo tee -a /etc/hosts > /dev/null
fi

# Start local registry and build images
echo "🚢 Starting local registry and building images..."
docker-compose up -d registry db keycloak
sleep 5  # Wait for registry

# Build and push images
echo "🔨 Building and pushing images..."
docker build -t $REGISTRY/staffing-backend:latest ./backend && docker push $REGISTRY/staffing-backend:latest
docker build -t $REGISTRY/staffing-frontend:latest ./frontend && docker push $REGISTRY/staffing-frontend:latest

# Create Kubernetes resources
echo "🚀 Deploying to Kubernetes..."
kubectl create namespace $NAMESPACE

# Deploy backend with initContainer for DB migration
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: $NAMESPACE
spec:
  replicas: 1
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      initContainers:
      - name: db-migrate
        image: $REGISTRY/staffing-backend:latest
        command: ["sh", "-c", "while ! nc -z db 3306; do sleep 2; done; ./mvnw liquibase:update"]
        envFrom:
        - configMapRef:
            name: backend-config
      containers:
      - name: backend
        image: $REGISTRY/staffing-backend:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: backend-config
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: backend-config
  namespace: $NAMESPACE
data:
  SPRING_DATASOURCE_URL: "jdbc:mysql://db:3306/staffing_process"
  SPRING_DATASOURCE_USERNAME: "root"
  SPRING_DATASOURCE_PASSWORD: "pass"
  SPRING_LIQUIBASE_ENABLED: "true"
  SPRING_PROFILES_ACTIVE: "local"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: $NAMESPACE
spec:
  replicas: 1
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: frontend
        image: $REGISTRY/staffing-frontend:latest
        ports:
        - containerPort: 80
        readinessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: db
  namespace: $NAMESPACE
spec:
  selector:
    app: db
  ports:
    - protocol: TCP
      port: 3306
      targetPort: 3306
---
apiVersion: v1
kind: Service
metadata:
  name: backend
  namespace: $NAMESPACE
spec:
  selector:
    app: backend
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: frontend
  namespace: $NAMESPACE
spec:
  selector:
    app: frontend
  ports:
    - protocol: TCP
      port: 80
      targetPort: 80
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: main-ingress
  namespace: $NAMESPACE
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/ssl-redirect: "false"
spec:
  ingressClassName: nginx
  rules:
  - host: $INGRESS_HOST
    http:
      paths:
      - path: /backend(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: backend
            port:
              number: 8080
      - path: /ui(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: frontend
            port:
              number: 80
      - path: /keycloak(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: keycloak
            port:
              number: 8080
EOF

# Wait for services with timeout
echo "⏳ Waiting for services to start (max 3 minutes)..."
timeout 180s bash -c "until kubectl get pods -n $NAMESPACE | grep -E 'backend-.*1/1|frontend-.*1/1' >/dev/null; do sleep 5; echo 'Still waiting...'; done" || \
  (echo "❌ Timed out waiting for pods"; kubectl get pods -n $NAMESPACE; exit 1)

# Port-forward for local access
echo "🔌 Setting up port forwarding (terminate with Ctrl+C)..."
echo -e "\nAccess URLs:"
echo -e "👉 Frontend: http://$INGRESS_HOST/ui"
echo -e "👉 Backend API: http://$INGRESS_HOST/backend"
echo -e "👉 Keycloak: http://$INGRESS_HOST/keycloak"
echo -e "\nYou may need to add to your /etc/hosts:\n127.0.0.1 $INGRESS_HOST\n"

kubectl port-forward --namespace $NAMESPACE service/backend 8080:8080 &
kubectl port-forward --namespace $NAMESPACE service/frontend 80:80 &
kubectl port-forward --namespace $NAMESPACE service/keycloak 8081:8080 &

wait