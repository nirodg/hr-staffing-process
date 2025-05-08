#!/bin/sh

CONFIG_PATH="/usr/src/app/src/assets/config.json"

echo "Injecting runtime config..."

cat <<EOF > $CONFIG_PATH
{
  "backendAppToken": "${BACKEND_APP_TOKEN:-dev-token}",
  "keycloakUrl" : "${KEYCLOAK_URL:-replace-me}",
  "apiBaseUrl" : "${API_BASE_URL:-replace-me}"
}
EOF

echo "Starting angular app..."
ng serve --host 0.0.0.0