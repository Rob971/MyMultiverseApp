#!/usr/bin/env bash
# Bumps app version in gradle/app-version.properties and syncs iOS Info.plist.
# Usage: ./scripts/bump-app-version.sh candidate|lts
set -euo pipefail

MODE="${1:?usage: $0 candidate|lts}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PROPS_FILE="$ROOT_DIR/gradle/app-version.properties"
IOS_PLIST="$ROOT_DIR/iosApp/iosApp/Info.plist"

if [[ ! -f "$PROPS_FILE" ]]; then
  echo "Missing $PROPS_FILE"
  exit 1
fi

version_lts="$(grep '^version.lts=' "$PROPS_FILE" | cut -d= -f2-)"
version_candidate="$(grep '^version.candidate=' "$PROPS_FILE" | cut -d= -f2-)"
version_code="$(grep '^version.code=' "$PROPS_FILE" | cut -d= -f2-)"

bump_patch() {
  local version="$1"
  local major minor patch
  IFS='.' read -r major minor patch <<< "$version"
  major="${major:-0}"
  minor="${minor:-0}"
  patch="${patch:-0}"
  patch=$((patch + 1))
  echo "${major}.${minor}.${patch}"
}

case "$MODE" in
  candidate)
    version_candidate=$((version_candidate + 1))
    version_code=$((version_code + 1))
    ;;
  lts)
    version_lts="$(bump_patch "$version_lts")"
    version_candidate=0
    version_code=$((version_code + 1))
    ;;
  *)
    echo "Unknown mode: $MODE (expected candidate or lts)"
    exit 1
    ;;
esac

if [[ "$version_candidate" -gt 0 ]]; then
  display_name="${version_lts}-rc.${version_candidate}"
else
  display_name="${version_lts}"
fi

cat > "$PROPS_FILE" <<EOF
# Canonical app version (updated by CI on successful pipeline runs).
# LTS: stable release on main. Candidate: rc build counter on feature branches.
version.lts=${version_lts}
version.candidate=${version_candidate}
version.code=${version_code}
EOF

if [[ -f "$IOS_PLIST" ]]; then
  sed -i.bak "/<key>CFBundleShortVersionString<\\/key>/{n;s|<string>.*</string>|<string>${display_name}</string>|;}" "$IOS_PLIST"
  sed -i.bak "/<key>CFBundleVersion<\\/key>/{n;s|<string>.*</string>|<string>${version_code}</string>|;}" "$IOS_PLIST"
  rm -f "$IOS_PLIST.bak"
fi

echo "display_name=${display_name}"
echo "version_code=${version_code}"
echo "version_lts=${version_lts}"
echo "version_candidate=${version_candidate}"
