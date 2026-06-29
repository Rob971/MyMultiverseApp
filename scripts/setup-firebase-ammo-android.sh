#!/usr/bin/env bash
# Register app.mymultiverse.ammo in Firebase project mymultiverseapp and write google-services.json.
#
# Prerequisites:
#   npx -y firebase-tools@latest login
#
# After this script succeeds:
#   1. Confirm FIREBASE_APP_ID in .github/workflows/kmp-ci.yml
#   2. Update GitHub secret GOOGLE_SERVICES_JSON (minified androidApp/google-services.json)

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

PROJECT_ID="${FIREBASE_PROJECT_ID:-mymultiverseapp}"
PACKAGE_NAME="app.mymultiverse.ammo"
DISPLAY_NAME="${FIREBASE_ANDROID_DISPLAY_NAME:-Ammò Android}"
TOOLS_VERSION="${FIREBASE_TOOLS_VERSION:-14.9.0}"
CLI=(npx -y "firebase-tools@${TOOLS_VERSION}")

echo "Using Firebase project: ${PROJECT_ID}"

app_id="$("${CLI[@]}" apps:list ANDROID --project "$PROJECT_ID" --json 2>/dev/null \
  | python3 -c "
import json, sys
data = json.load(sys.stdin)
for app in data.get('result', []):
    if app.get('packageName') == '${PACKAGE_NAME}':
        print(app['appId'])
        break
" || true)"

if [[ -z "$app_id" ]]; then
  echo "Creating Android app ${PACKAGE_NAME} …"
  create_out="$("${CLI[@]}" apps:create ANDROID "$DISPLAY_NAME" \
    --package-name "$PACKAGE_NAME" \
    --project "$PROJECT_ID" 2>&1)"
  echo "$create_out"
  app_id="$(echo "$create_out" | sed -n 's/.*App ID: //p' | head -1)"
else
  echo "Android app already registered for ${PACKAGE_NAME} (${app_id})"
fi

if [[ -z "${app_id:-}" ]]; then
  echo "Could not determine Firebase App ID. Run: firebase apps:list ANDROID --project ${PROJECT_ID} --json"
  exit 1
fi

echo "Fetching google-services.json for App ID: ${app_id}"
"${CLI[@]}" apps:sdkconfig ANDROID "$app_id" --project "$PROJECT_ID" \
  | python3 -c "
import json, sys
data = json.load(sys.stdin)
data['client'] = [
    c for c in data.get('client', [])
    if c.get('client_info', {}).get('android_client_info', {}).get('package_name') == '${PACKAGE_NAME}'
]
json.dump(data, sys.stdout, indent=2)
print()
" > androidApp/google-services.json

echo ""
echo "Wrote androidApp/google-services.json"
echo ""
echo "Next steps:"
echo "  FIREBASE_APP_ID=${app_id}"
echo "  → Confirm in .github/workflows/kmp-ci.yml"
echo "  → Update GitHub secret GOOGLE_SERVICES_JSON from androidApp/google-services.json"
