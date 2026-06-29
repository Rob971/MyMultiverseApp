#!/usr/bin/env bash
# Print SHA-256 certificate fingerprint for Android App Links (assetlinks.json).
# Usage: ./scripts/print-android-keystore-fingerprint.sh <keystore> <alias> [storepass]

set -euo pipefail

KEYSTORE_PATH="${1:-}"
KEY_ALIAS="${2:-}"
STORE_PASSWORD="${3:-${AMMO_UPLOAD_STORE_PASSWORD:-}}"

if [[ -z "${KEYSTORE_PATH}" || -z "${KEY_ALIAS}" ]]; then
  echo "Usage: $0 <keystore-path> <key-alias> [store-password]" >&2
  exit 1
fi

if [[ ! -f "${KEYSTORE_PATH}" ]]; then
  echo "ERROR: keystore not found: ${KEYSTORE_PATH}" >&2
  exit 1
fi

if [[ -z "${STORE_PASSWORD}" ]]; then
  echo "ERROR: store password required (arg 3 or AMMO_UPLOAD_STORE_PASSWORD)" >&2
  exit 1
fi

FINGERPRINT="$(
  keytool -list -v -keystore "${KEYSTORE_PATH}" -alias "${KEY_ALIAS}" -storepass "${STORE_PASSWORD}" 2>/dev/null \
    | awk '/SHA256:/{gsub(/:/,"",$2); print toupper($2); exit}'
)"

if [[ -z "${FINGERPRINT}" ]]; then
  echo "ERROR: could not read SHA-256 fingerprint from keystore" >&2
  exit 1
fi

echo "${FINGERPRINT}"
