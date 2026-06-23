#!/usr/bin/env bash
# Print SHA-256 certificate fingerprint for Android App Links (assetlinks.json).
# Usage: ./scripts/print-android-apk-fingerprint.sh path/to/app.apk

set -euo pipefail

APK_PATH="${1:-}"
if [[ -z "${APK_PATH}" || ! -f "${APK_PATH}" ]]; then
  echo "Usage: $0 <apk-path>" >&2
  exit 1
fi

if command -v apksigner >/dev/null 2>&1; then
  apksigner verify --print-certs "${APK_PATH}" 2>/dev/null | awk '/Signer #1 certificate SHA-256 digest:/{print $6; exit}'
  exit 0
fi

if ! command -v unzip >/dev/null 2>&1 || ! command -v keytool >/dev/null 2>&1; then
  echo "ERROR: need apksigner or unzip+keytool" >&2
  exit 1
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT
unzip -p "${APK_PATH}" META-INF/CERT.RSA > "${TMP_DIR}/cert.rsa" 2>/dev/null \
  || unzip -p "${APK_PATH}" META-INF/CERT.DSA > "${TMP_DIR}/cert.rsa" 2>/dev/null \
  || unzip -p "${APK_PATH}" META-INF/CERT.EC > "${TMP_DIR}/cert.rsa"

keytool -printcert -file "${TMP_DIR}/cert.rsa" | awk '/SHA256:/{gsub(/:/,"",$2); print toupper($2); exit}'
