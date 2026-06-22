#!/usr/bin/env bash
# Install pre-built debug + androidTest APKs and run the full instrumented suite via adb.
# Used by KMP CI after Android CI uploads both artifacts (no Gradle compile on this runner).
set -euo pipefail

APK_DIR="${1:-apks}"
TEST_RUNNER="${TEST_RUNNER:-androidx.test.runner.AndroidJUnitRunner}"
TEST_PACKAGE="${TEST_PACKAGE:-app.mymultiverse.kmp.test}"

APP_APK="$(find "$APK_DIR" -type f -name '*.apk' ! -name '*androidTest*' | head -n 1)"
TEST_APK="$(find "$APK_DIR" -type f -name '*androidTest*.apk' | head -n 1)"

if [[ -z "$APP_APK" || -z "$TEST_APK" ]]; then
  echo "::error::Missing APK(s) under ${APK_DIR}. Found app='${APP_APK:-}' test='${TEST_APK:-}'"
  exit 1
fi

echo "Installing app APK: $APP_APK"
echo "Installing test APK: $TEST_APK"

adb wait-for-device
adb install -r "$APP_APK"
adb install -r "$TEST_APK"
adb shell am instrument -w -r "${TEST_PACKAGE}/${TEST_RUNNER}"
