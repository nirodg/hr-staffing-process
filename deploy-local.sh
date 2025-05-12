#!/usr/bin/env bash
set -euo pipefail

# ────────────────  styling  ────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'
info()    { echo -e "${BOLD}${CYAN}[INFO]${RESET}  $*"; }
success() { echo -e "${BOLD}${GREEN}[OK]${RESET}    $*"; }
warn()    { echo -e "${BOLD}${YELLOW}[WARN]${RESET}  $*"; }
error()   { echo -e "${BOLD}${RED}[ERROR]${RESET} $*"; }

# ────────────────  banner ────────────────
cat <<'EOF'

    ____ ___ ____ ____ ____ _ _  _ ____    ____ ___ ____ ____ _  _
    [__   |  |__| |___ |___ | |\ | | __    [__   |  |__| |    |_/
    ___]  |  |  | |    |    | | \| |__]    ___]  |  |  | |___ | \_

            Local Kubernetes Deployment 🚀

EOF

# ────────────────  defaults ────────────────
REGISTRY="localhost:5000"
NAMESPACE="default"
TIMEOUT="180"
BUILD_ALL=false
BUILD_ONE=""
ASK_SELECT=false
DELETE_ALL=false
DELETE_ONE=""
ASK_DELETE=false
SHOW_ROUTES=false

# ────────────────  contexts / tags ────────────────
DIRS=(backend frontend)
declare -A TAGS=(
  [backend]="${REGISTRY}/staffing-backend:latest"
  [frontend]="${REGISTRY}/staffing-frontend:latest"
)
declare -A RESMAP=(
  [backend]=deployment/backend.yaml
  [frontend]=deployment/frontend.yaml
)

# stacks
RESOURCES=(
  deployment/db.yaml
  deployment/adminer.yaml
  deployment/keycloak.yaml
  deployment/kafka-stack.yaml
  deployment/grafana-stack.yaml
  "${RESMAP[backend]}"
  "${RESMAP[frontend]}"
)
STATEFULSETS=(db)
DEPLOYMENTS=(adminer keycloak zookeeper kafka kafka-ui loki grafana backend-app frontend-app)

# ────────────────  usage ────────────────
usage(){
cat <<USAGE
Usage: $0 [options]

  -n,  --namespace <ns>     Target namespace (default: default)
  -build-all                Build all images and exit
  -build [context]          Build one image (context optional -> menu)
  -down [context]           Delete resources: no arg = whole stack, arg = one workspace
  -routes                   Show service routes/ports and exit
  -h,  --help               Show this help
USAGE
}

# ────────────────  arg-parse ────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    -n|--namespace)  NAMESPACE="$2"; shift 2 ;;
    -build-all)      BUILD_ALL=true; shift ;;
    -build)
      if [[ -n "${2-}" && "$2" != -* ]]; then BUILD_ONE="$2"; shift 2
      else ASK_SELECT=true; shift; fi ;;
    -down)
      if [[ -n "${2-}" && "$2" != -* ]]; then DELETE_ONE="$2"; shift 2
      else DELETE_ALL=true; shift; fi ;;
    -routes)         SHOW_ROUTES=true; shift ;;
    -h|--help)       usage; exit 0 ;;
    *)               error "Unknown option $1"; usage; exit 1 ;;
  esac
done

# ────────────────  helpers ────────────────
build_image(){ local d="$1" t="$2"; info "🔨 Building $d → $t"; docker build -t "$t" "./$d"; }

ensure_namespace(){
  if ! kubectl get ns "$NAMESPACE" >/dev/null 2>&1; then
    info "🆕 Creating namespace $NAMESPACE"; kubectl create ns "$NAMESPACE"
  fi
}

delete_resources(){
  local yaml="$1"
  info "🗑  Deleting $(basename "$yaml")"; kubectl delete -f "$yaml" -n "$NAMESPACE" --ignore-not-found
}

cleanup_all(){
  info "🧹 Deleting full stack from $NAMESPACE"
  for r in "${RESOURCES[@]}"; do delete_resources "$r"; done
  success "Stack removed."
}

delete_workspace(){
  local ctx="$1"
  [[ -v RESMAP[$ctx] ]] || { error "Unknown workspace $ctx"; exit 1; }
  delete_resources "${RESMAP[$ctx]}"
  success "$ctx workspace removed."
}

show_routes(){
  info "📡 Services in $NAMESPACE"; kubectl get svc -n "$NAMESPACE"
  if kubectl get ingress -n "$NAMESPACE" >/dev/null 2>&1; then
    printf "\n"; info "🌐 Ingresses"; kubectl get ingress -n "$NAMESPACE"
  fi
}

# ────────────────  interactive build/delete ────────────────
if $ASK_SELECT; then
  echo "Select project to build:"; for i in "${!DIRS[@]}"; do printf "  %d) %s\n" $((i+1)) "${DIRS[$i]}"; done
  read -rp "Enter number: " idx
  [[ "$idx" =~ ^[0-9]+$ && idx-1 < ${#DIRS[@]} ]] || { error "Invalid selection"; exit 1; }
  BUILD_ONE="${DIRS[$((idx-1))]}"
fi

if $ASK_DELETE; then
  echo "Select workspace to delete:"; for i in "${!DIRS[@]}"; do printf "  %d) %s\n" $((i+1)) "${DIRS[$i]}"; done
  read -rp "Enter number: " idx
  [[ "$idx" =~ ^[0-9]+$ && idx-1 < ${#DIRS[@]} ]] || { error "Invalid selection"; exit 1; }
  DELETE_ONE="${DIRS[$((idx-1))]}"
fi

# ────────────────  short-circuit modes ────────────────
if [[ -n "$DELETE_ONE" ]]; then ensure_namespace; delete_workspace "$DELETE_ONE"; exit 0; fi
$DELETE_ALL && { ensure_namespace; cleanup_all; exit 0; }
$SHOW_ROUTES && { ensure_namespace; show_routes; exit 0; }

if [[ -n "$BUILD_ONE" ]]; then
  build_image "$BUILD_ONE" "${TAGS[$BUILD_ONE]}"; success "Build completed."; exit 0
fi
if $BUILD_ALL; then for d in "${DIRS[@]}"; do build_image "$d" "${TAGS[$d]}"; done; success "All images built."; exit 0; fi


# ────────────────────────  port-forward  ───────────────────────
portForward(){
  PORTS=(
  "svc/frontend-app 4200:4200"
  "svc/keycloak      8080:8080"
  "svc/backend-app   8050:8080"
  "svc/grafana       8090:3000"
  "svc/kafka-ui      8095:8080"
)

  info "🔌 Starting port-forwarding (Ctrl+C to stop)…"
  for spec in "${PORTS[@]}"; do
    set -- $spec                       # split into 3 words
    kubectl port-forward -n "$NAMESPACE" "$1" "$2" >/dev/null 2>&1 &
    pids+=" $!"
    printf "  %s  →  http://localhost:%s\n" "$1" "${2%%:*}"
  done

  trap 'kill $pids 2>/dev/null' EXIT
  wait
}

# ────────────────  deploy workflow ────────────────
ensure_namespace
cleanup_all          # clear any previous stack

printf "\n"; info "🔨 Building images…"
for d in "${DIRS[@]}"; do build_image "$d" "${TAGS[$d]}"; done

printf "\n"; info "🚀 Applying manifests…"
for r in "${RESOURCES[@]}"; do kubectl apply -f "$r" -n "$NAMESPACE"; done

printf "\n"; info "⏳ Waiting for rollouts (${TIMEOUT}s)…"
for s in "${STATEFULSETS[@]}"; do info "→ statefulset/$s"; kubectl rollout status statefulset/"$s" -n "$NAMESPACE" --timeout="${TIMEOUT}s"; done
for d in "${DEPLOYMENTS[@]}";  do info "→ deployment/$d"; kubectl rollout status deployment/"$d"  -n "$NAMESPACE" --timeout="${TIMEOUT}s";  done

show_routes
success "Cluster ready! 🎉"
portForward
