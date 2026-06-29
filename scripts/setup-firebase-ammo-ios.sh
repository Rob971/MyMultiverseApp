#!/usr/bin/env bash
# Fetch GoogleService-Info.plist for app.mymultiverse.ammo (Firebase iOS app).
#
# Prerequisites:
#   npx -y firebase-tools@latest login
#
# Writes iosApp/iosApp/GoogleService-Info.plist (gitignored). Commit the .example template only.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

PROJECT_ID="${FIREBASE_PROJECT_ID:-mymultiverseapp}"
BUNDLE_ID="app.mymultiverse.ammo"
DISPLAY_NAME="${FIREBASE_IOS_DISPLAY_NAME:-Ammò iOS}"
TOOLS_VERSION="${FIREBASE_TOOLS_VERSION:-14.9.0}"
CLI=(npx -y "firebase-tools@${TOOLS_VERSION}")
OUT="iosApp/iosApp/GoogleService-Info.plist"

echo "Using Firebase project: ${PROJECT_ID}"

app_id="$("${CLI[@]}" apps:list IOS --project "$PROJECT_ID" --json 2>/dev/null \
  | python3 -c "
import json, sys
data = json.load(sys.stdin)
for app in data.get('result', []):
    if app.get('bundleId') == '${BUNDLE_ID}':
        print(app['appId'])
        break
" || true)"

if [[ -z "$app_id" ]]; then
  echo "Creating iOS app ${BUNDLE_ID} …"
  create_out="$("${CLI[@]}" apps:create IOS "$DISPLAY_NAME" \
    --bundle-id "$BUNDLE_ID" \
    --project "$PROJECT_ID" 2>&1)"
  echo "$create_out"
  app_id="$(echo "$create_out" | sed -n 's/.*App ID: //p' | head -1)"
else
  echo "iOS app already registered for ${BUNDLE_ID} (${app_id})"
fi

if [[ -z "${app_id:-}" ]]; then
  echo "Could not determine Firebase iOS App ID."
  exit 1
fi

echo "Fetching GoogleService-Info.plist for App ID: ${app_id}"
"${CLI[@]}" apps:sdkconfig IOS "$app_id" --project "$PROJECT_ID" > "$OUT"

echo "Wrote ${OUT}"
echo "Firebase iOS App ID: ${app_id}"
