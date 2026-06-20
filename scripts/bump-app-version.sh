#!/usr/bin/env bash
# Bumps app version in gradle/app-version.properties and syncs iOS Info.plist.
# Usage: ./scripts/bump-app-version.sh patch|minor|none
set -euo pipefail

MODE="${1:?usage: $0 patch|minor|none}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=format-app-version.sh
source "$ROOT_DIR/scripts/format-app-version.sh"
PROPS_FILE="$ROOT_DIR/gradle/app-version.properties"
IOS_PLIST="$ROOT_DIR/iosApp/iosApp/Info.plist"

if [[ ! -f "$PROPS_FILE" ]]; then
  echo "Missing $PROPS_FILE"
  exit 1
fi

read_prop() {
  grep "^$1=" "$PROPS_FILE" | cut -d= -f2- || true
}

version_name="$(read_prop version.name)"
if [[ -z "$version_name" ]]; then
  version_name="$(read_prop version.lts)"
fi
version_prerelease="$(read_prop version.prerelease)"
version_code="$(read_prop version.code)"

bump_patch() {
  local version="$1"
  local major minor patch
  IFS='.' read -r major minor patch <<< "$version"
  major="${major:-0}"
  minor="${minor:-0}"
  patch="${patch:-0}"
  echo "${major}.${minor}.$((patch + 1))"
}

bump_minor() {
  local version="$1"
  local major minor patch
  IFS='.' read -r major minor patch <<< "$version"
  major="${major:-0}"
  minor="${minor:-0}"
  echo "${major}.$((minor + 1)).0"
}

case "$MODE" in
  patch)
    version_name="$(bump_patch "$version_name")"
    version_prerelease=""
    version_code=$((version_code + 1))
    ;;
  minor)
    version_name="$(bump_minor "$version_name")"
    version_prerelease=""
    version_code=$((version_code + 1))
    ;;
  none)
    version_code=$((version_code + 1))
    ;;
  *)
    echo "Unknown mode: $MODE (expected patch, minor, or none)"
    exit 1
    ;;
esac

display_name="$(format_app_version "$version_name" "$version_prerelease")"

{
  echo "# Canonical app version (bumped by the Release workflow via workflow_dispatch)."
  echo "# Optional pre-release suffix (e.g. beta.1); omit the key for stable releases."
  echo "version.name=${version_name}"
  echo "version.code=${version_code}"
  if [[ -n "$version_prerelease" ]]; then
    echo "version.prerelease=${version_prerelease}"
  fi
} > "$PROPS_FILE"

if [[ -f "$IOS_PLIST" ]]; then
  sed -i.bak "/<key>CFBundleShortVersionString<\\/key>/{n;s|<string>.*</string>|<string>${display_name}</string>|;}" "$IOS_PLIST"
  sed -i.bak "/<key>CFBundleVersion<\\/key>/{n;s|<string>.*</string>|<string>${version_code}</string>|;}" "$IOS_PLIST"
  rm -f "$IOS_PLIST.bak"
fi

echo "display_name=${display_name}"
echo "version_name=${version_name}"
echo "version_code=${version_code}"
echo "tag_name=v${display_name}"
