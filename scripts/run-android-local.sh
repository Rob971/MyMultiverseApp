#!/usr/bin/env bash
# Run on your Mac (Cursor terminal or iTerm), not the agent sandbox.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# Default Android SDK location (Android Studio). Override with ANDROID_HOME.
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

if [[ ! -x "$ANDROID_HOME/platform-tools/adb" ]]; then
  echo "adb not found. Install Android Studio or set ANDROID_HOME to your SDK root."
  exit 1
fi

echo "Devices:"
adb devices
echo
echo "Building debug APK..."
./gradlew :androidApp:assembleDebug

echo
echo "Installing on connected device/emulator..."
./gradlew :androidApp:installDebug

echo "Done. Launch the app (app.mymultiverse.ammo) from the launcher."
