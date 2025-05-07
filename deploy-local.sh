#!/bin/bash

# Configuration
REGISTRY="localhost:5000"  # Changed for Windows Docker Desktop
NAMESPACE="staffing-local"
INGRESS_HOST="localhost"

# Clean previous deployment
echo "🧹 Cleaning previous deployment..."
kubectl delete namespace "$NAMESPACE" 2>/dev/null
docker-compose down -v 2>/dev/null

# Start local registry (standalone, not in compose)
echo "🚢 Starting local registry..."
docker run -d -p 5000:5000 --restart=always --name registry registry:2

# Wait for registry
echo "🔄 Waiting for registry..."
while ! curl -s http://localhost:5000/v2/_catalog >/dev/null; do
  sleep 2
done

# Start other services
echo "🚢 Starting dependencies..."
docker-compose up -d db keycloak
sleep 5

# Build and push images
echo "🔨 Building and pushing images..."
docker build -t "${REGISTRY}/staffing-backend:latest" ./backend
docker push "${REGISTRY}/staffing-backend:latest"

# Upload manually to k8s
# docker save -o backend.tar ${REGISTRY}/staffing-backend:latest
# nerdctl --namespace $NAMESPACE load < backend.tar


docker build -t "${REGISTRY}/staffing-frontend:latest" ./frontend
docker push "${REGISTRY}/staffing-frontend:latest"

# Upload manually to k8s
# docker save -o frontend.tar ${REGISTRY}/frontend-backend:latest
# nerdctl --namespace $NAMESPACE load < frontend.tar


# Create Kubernetes resources
echo "🚀 Deploying to Kubernetes..."
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Namespace
metadata:
  name: $NAMESPACE
---
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
      containers:
      - name: backend
        image: ${REGISTRY}/staffing-backend:latest
        imagePullPolicy: Always  # Critical for local registry
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://db:3306/staffing_process"
        - name: SPRING_DATASOURCE_USERNAME
          value: "root"
        - name: SPRING_DATASOURCE_PASSWORD
          value: "pass"
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
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
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  ingressClassName: nginx
  rules:
  - host: $INGRESS_HOST
    http:
      paths:
      - path: /backend
        pathType: Prefix
        backend:
          service:
            name: backend
            port:
              number: 8080
      - path: /ui
        pathType: Prefix
        backend:
          service:
            name: frontend
            port:
              number: 80
      - path: /keycloak
        pathType: Prefix
        backend:
          service:
            name: keycloak
            port:
              number: 8080
EOF

# Wait for services (simplified check)
echo "⏳ Waiting for services to start..."
timeout=180
start_time=$(date +%s)

while true; do
  # Simplified check that works across kubectl versions
  ready_pods=$(kubectl get pods -n "$NAMESPACE" -o jsonpath='{.items[*].status.conditions[?(@.type=="Ready")].status}' | grep -c "True")
  
  if [ "$ready_pods" -ge 2 ]; then
    break
  fi
  
  if [ $(($(date +%s) - start_time)) -gt "$timeout" ]; then
    echo "❌ Timed out waiting for pods"
    kubectl get pods -n "$NAMESPACE"
    exit 1
  fi
  
  sleep 5
  echo "Still waiting for pods to be ready..."
done

# Port forwarding
echo "🔌 Setting up port forwarding..."
echo -e "\nAccess URLs:"
echo -e "👉 Frontend: http://localhost/ui"
echo -e "👉 Backend API: http://localhost/backend"
echo -e "👉 Keycloak: http://localhost/keycloak"
echo -e "\nPress Ctrl+C to stop port forwarding..."

kubectl port-forward --namespace "$NAMESPACE" service/backend 8080:8080 &
backend_pid=$!
kubectl port-forward --namespace "$NAMESPACE" service/frontend 80:80 &
frontend_pid=$!
kubectl port-forward --namespace "$NAMESPACE" service/keycloak 8081:8080 &
keycloak_pid=$!

trap 'kill $backend_pid $frontend_pid $keycloak_pid' EXIT
wait