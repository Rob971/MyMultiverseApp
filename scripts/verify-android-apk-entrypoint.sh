#!/usr/bin/env bash
# Fail if critical runtime classes are missing from APK DEX (regression guard after AGP 9 split).
# Usage: ./scripts/verify-android-apk-entrypoint.sh path/to/app.apk

set -euo pipefail

APK_PATH="${1:-}"
MAIN_ACTIVITY_DESCRIPTOR='Lapp/mymultiverse/ammo/MainActivity;'
# kotlinx-datetime 0.6.x inner class; absent when 0.7.x wins dependency resolution.
KOTLINX_CLOCK_SYSTEM_DESCRIPTOR=$'Lkotlinx/datetime/Clock$System;'

if [[ -z "${APK_PATH}" || ! -f "${APK_PATH}" ]]; then
  echo "Usage: $0 <apk-path>" >&2
  exit 1
fi

if ! command -v unzip >/dev/null 2>&1 || ! command -v grep >/dev/null 2>&1; then
  echo "ERROR: unzip and grep are required" >&2
  exit 1
fi

tmp_dir="$(mktemp -d)"
trap 'rm -rf "${tmp_dir}"' EXIT

unzip -q "${APK_PATH}" 'classes*.dex' -d "${tmp_dir}"

find_descriptor() {
  local descriptor="$1"
  local dex
  for dex in "${tmp_dir}"/classes*.dex; do
    if grep -aqF -- "${descriptor}" "${dex}"; then
      echo "OK: ${descriptor} found in $(basename "${dex}")"
      return 0
    fi
  done
  return 1
}

if ! find_descriptor "${MAIN_ACTIVITY_DESCRIPTOR}"; then
  echo "FAIL: ${MAIN_ACTIVITY_DESCRIPTOR} not found in any DEX file under ${APK_PATH}" >&2
  echo "The manifest launcher will crash with ClassNotFoundException." >&2
  exit 1
fi

if ! find_descriptor "${KOTLINX_CLOCK_SYSTEM_DESCRIPTOR}"; then
  echo "FAIL: ${KOTLINX_CLOCK_SYSTEM_DESCRIPTOR} not found in any DEX file under ${APK_PATH}" >&2
  echo "Nutrition DI will crash with NoClassDefFoundError (kotlinx-datetime version skew)." >&2
  exit 1
fi

exit 0
