#!/usr/bin/env bash
# Formats user-facing app version from gradle/app-version.properties fields.
# Keep in sync with AppVersionFormatter.kt.
format_app_version() {
  local name="${1:?name required}"
  local prerelease="${2:-}"
  if [[ -n "$prerelease" ]]; then
    printf '%s-%s\n' "$name" "$prerelease"
  else
    printf '%s\n' "$name"
  fi
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  format_app_version "${1:?usage: $0 <name> [prerelease]}" "${2:-}"
fi
