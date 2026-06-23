#!/usr/bin/env bash
# Generate web/.well-known/* for mymultiverse.app App Links / Universal Links.
#
# Required env:
#   ANDROID_SHA256_FINGERPRINT — colon-free uppercase SHA-256 from print-android-apk-fingerprint.sh
#   IOS_TEAM_ID — Apple Team ID (same as APNS_TEAM_ID)
#
# Optional:
#   ANDROID_PACKAGE_NAME (default app.mymultiverse.kmp)
#   IOS_BUNDLE_ID (default app.mymultiverse.kmp)

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="${ROOT_DIR}/web/.well-known"

ANDROID_SHA256="${ANDROID_SHA256_FINGERPRINT:-${ANDROID_RELEASE_SHA256:-}}"
IOS_TEAM_ID="${IOS_TEAM_ID:-${APNS_TEAM_ID:-}}"
ANDROID_PACKAGE="${ANDROID_PACKAGE_NAME:-app.mymultiverse.kmp}"
IOS_BUNDLE="${IOS_BUNDLE_ID:-app.mymultiverse.kmp}"

if [[ -z "${ANDROID_SHA256}" ]]; then
  echo "ERROR: set ANDROID_SHA256_FINGERPRINT (run scripts/print-android-apk-fingerprint.sh on your APK)" >&2
  exit 1
fi
if [[ -z "${IOS_TEAM_ID}" ]]; then
  echo "ERROR: set IOS_TEAM_ID or APNS_TEAM_ID" >&2
  exit 1
fi

ANDROID_SHA256="${ANDROID_SHA256//:/}"
ANDROID_SHA256="$(echo "${ANDROID_SHA256}" | tr '[:lower:]' '[:upper:]')"

mkdir -p "${OUT_DIR}"

cat > "${OUT_DIR}/assetlinks.json" <<EOF
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "${ANDROID_PACKAGE}",
      "sha256_cert_fingerprints": [
        "${ANDROID_SHA256}"
      ]
    }
  }
]
EOF

cat > "${OUT_DIR}/apple-app-site-association" <<EOF
{
  "applinks": {
    "apps": [],
    "details": [
      {
        "appID": "${IOS_TEAM_ID}.${IOS_BUNDLE}",
        "paths": ["/invite", "/invite/*"]
      }
    ]
  }
}
EOF

echo "Wrote ${OUT_DIR}/assetlinks.json"
echo "Wrote ${OUT_DIR}/apple-app-site-association"
echo "Deploy web/ to https://mymultiverse.app (see scripts/verify-app-links-hosting.sh)"
