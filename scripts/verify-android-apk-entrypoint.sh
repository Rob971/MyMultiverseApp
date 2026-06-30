#!/usr/bin/env bash
# Fail if the launcher activity is missing from APK DEX (regression guard after AGP 9 split).
# Usage: ./scripts/verify-android-apk-entrypoint.sh path/to/app.apk

set -euo pipefail

APK_PATH="${1:-}"
MAIN_ACTIVITY_DESCRIPTOR='Lapp/mymultiverse/ammo/MainActivity;'

if [[ -z "${APK_PATH}" || ! -f "${APK_PATH}" ]]; then
  echo "Usage: $0 <apk-path>" >&2
  exit 1
fi

if ! command -v unzip >/dev/null 2>&1 || ! command -v strings >/dev/null 2>&1; then
  echo "ERROR: unzip and strings are required" >&2
  exit 1
fi

tmp_dir="$(mktemp -d)"
trap 'rm -rf "${tmp_dir}"' EXIT

unzip -q "${APK_PATH}" 'classes*.dex' -d "${tmp_dir}"

for dex in "${tmp_dir}"/classes*.dex; do
  if strings "${dex}" | grep -qF "${MAIN_ACTIVITY_DESCRIPTOR}"; then
    echo "OK: ${MAIN_ACTIVITY_DESCRIPTOR} found in $(basename "${dex}")"
    exit 0
  fi
done

echo "FAIL: ${MAIN_ACTIVITY_DESCRIPTOR} not found in any DEX file under ${APK_PATH}" >&2
echo "The manifest launcher will crash with ClassNotFoundException." >&2
exit 1
