#!/usr/bin/env bash
set -euo pipefail

# Define color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'


# Helper functions
info()    { echo -e "${BOLD}${CYAN}[INFO]${RESET} $1"; }
success() { echo -e "${BOLD}${GREEN}[OK]${RESET}   $1"; }
warn()    { echo -e "${BOLD}${YELLOW}[WARN]${RESET} $1"; }
error()   { echo -e "${BOLD}${RED}[ERROR]${RESET} $1"; }


cat <<'EOF'


    ____ ___ ____ ____ ____ _ _  _ ____    ____ ___ ____ ____ _  _ 
    [__   |  |__| |___ |___ | |\ | | __    [__   |  |__| |    |_/  
    ___]  |  |  | |    |    | | \| |__]    ___]  |  |  | |___ | \_ 
                                                                  

     Local Kubernetes Deployment 🚀

     Brage Dorin <dorin.brage@gmail.com>
     Github: https://github.com/nirodg/hr-staffing-process


EOF

# Configuration
REGISTRY="localhost:5000"
INGRESS_HOST="localhost"
NAMESPACE="${NAMESPACE:-default}"
TIMEOUT="180"


# Kubernetes resource files
RESOURCES=(
  "deployment/db.yaml"
  "deployment/adminer.yaml"
  "deployment/keycloak.yaml"
  "deployment/kafka-stack.yaml"
  "deployment/grafana-stack.yaml"
  "deployment/backend.yaml"
  "deployment/frontend.yaml"
  # Uncomment when ready
  # "deployment/ingress.yaml"
)

# Which workloads to wait for
STATEFULSETS=(db)
DEPLOYMENTS=(adminer keycloak zookeeper kafka kafka-ui loki grafana backend-app frontend-app)


# Cleanup
printf "\n" && info "🧹 Cleaning previous deployment... \n"
for res in "${RESOURCES[@]}"; do
  kubectl delete -f "$res" --namespace "$NAMESPACE" --ignore-not-found
done

# Build source code to images
printf "\n" && info "🔨 Building images... \n"
docker build -t "${REGISTRY}/staffing-backend:latest" ./backend
docker build -t "${REGISTRY}/staffing-frontend:latest" ./frontend

# Deploy
printf "\n" && info "🚀 Applying manifests... \n"
for f in "${RESOURCES[@]}"; do
  kubectl apply -f "$f" -n "$NAMESPACE"
done

printf "\n" && info "⏳ Waiting for rollouts (timeout=${TIMEOUT})... \n"
for s in "${STATEFULSETS[@]}"; do
  info "→ statefulset/$s"
  kubectl rollout status statefulset/"$s" -n "$NAMESPACE" --timeout="$TIMEOUT"s
done
for d in "${DEPLOYMENTS[@]}"; do
  info "→ deployment/$d"
  kubectl rollout status deployment/"$d" -n "$NAMESPACE" --timeout="$TIMEOUT"s
done

success "👉 Ready to use!"