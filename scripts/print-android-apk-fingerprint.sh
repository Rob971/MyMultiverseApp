#!/usr/bin/env bash
# Print SHA-256 certificate fingerprint for Android App Links (assetlinks.json).
# Usage: ./scripts/print-android-apk-fingerprint.sh path/to/app.apk

set -euo pipefail

APK_PATH="${1:-}"
if [[ -z "${APK_PATH}" || ! -f "${APK_PATH}" ]]; then
  echo "Usage: $0 <apk-path>" >&2
  exit 1
fi

find_apksigner() {
  if command -v apksigner >/dev/null 2>&1; then
    command -v apksigner
    return 0
  fi

  local candidates=()
  [[ -n "${ANDROID_HOME:-}" ]] && candidates+=("${ANDROID_HOME}")
  [[ -n "${ANDROID_SDK_ROOT:-}" ]] && candidates+=("${ANDROID_SDK_ROOT}")
  candidates+=("${HOME}/Android/Sdk" "${HOME}/Library/Android/sdk")

  local sdk_dir
  if [[ -f local.properties ]]; then
    sdk_dir="$(grep -E '^sdk\.dir=' local.properties | head -1 | cut -d= -f2- | sed 's/\\:/:/g' || true)"
    [[ -n "${sdk_dir}" ]] && candidates+=("${sdk_dir}")
  fi

  local sdk version
  for sdk in "${candidates[@]}"; do
    [[ -d "${sdk}/build-tools" ]] || continue
    version="$(ls -1 "${sdk}/build-tools" 2>/dev/null | sort -V | tail -1)"
    if [[ -n "${version}" && -x "${sdk}/build-tools/${version}/apksigner" ]]; then
      echo "${sdk}/build-tools/${version}/apksigner"
      return 0
    fi
  done
  return 1
}

fingerprint_from_apksigner() {
  local apksigner_bin="$1"
  "${apksigner_bin}" verify --print-certs "${APK_PATH}" 2>/dev/null \
    | awk '/certificate SHA-256 digest:/{gsub(/:/,"",$NF); print toupper($NF); exit}'
}

fingerprint_from_legacy_cert() {
  local tmp_dir
  tmp_dir="$(mktemp -d)"
  trap 'rm -rf "${tmp_dir}"' RETURN
  local cert_file=""
  cert_file="$(unzip -Z1 "${APK_PATH}" 'META-INF/*.RSA' 2>/dev/null | head -1 || true)"
  if [[ -z "${cert_file}" ]]; then
    cert_file="$(unzip -Z1 "${APK_PATH}" 'META-INF/*.DSA' 2>/dev/null | head -1 || true)"
  fi
  if [[ -z "${cert_file}" ]]; then
    cert_file="$(unzip -Z1 "${APK_PATH}" 'META-INF/*.EC' 2>/dev/null | head -1 || true)"
  fi
  if [[ -z "${cert_file}" ]]; then
    return 1
  fi
  unzip -p "${APK_PATH}" "${cert_file}" > "${tmp_dir}/cert.bin"
  keytool -printcert -file "${tmp_dir}/cert.bin" | awk '/SHA256:/{gsub(/:/,"",$2); print toupper($2); exit}'
}

FINGERPRINT=""
if APKSIGNER_BIN="$(find_apksigner)"; then
  FINGERPRINT="$(fingerprint_from_apksigner "${APKSIGNER_BIN}" || true)"
fi

if [[ -z "${FINGERPRINT}" ]] && command -v unzip >/dev/null 2>&1 && command -v keytool >/dev/null 2>&1; then
  FINGERPRINT="$(fingerprint_from_legacy_cert || true)"
fi

if [[ -z "${FINGERPRINT}" ]]; then
  echo "ERROR: could not read APK signing certificate (install Android build-tools apksigner or use a v1-signed APK)" >&2
  exit 1
fi

echo "${FINGERPRINT}"
