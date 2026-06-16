#!/usr/bin/env bash
# Formats user-facing app version from gradle/app-version.properties fields.
# RC builds use major.minor from LTS with candidate as the third segment (e.g. 1.0.5).
# Stable builds use the full LTS string (e.g. 1.0.0).
format_app_version() {
  local lts="${1:?lts required}"
  local candidate="${2:-0}"
  if [[ "$candidate" -gt 0 ]]; then
    local major minor _patch
    IFS='.' read -r major minor _patch <<< "$lts"
    major="${major:-0}"
    minor="${minor:-0}"
    printf '%s.%s.%s\n' "$major" "$minor" "$candidate"
  else
    printf '%s\n' "$lts"
  fi
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  format_app_version "${1:?usage: $0 <lts> <candidate>}" "${2:-0}"
fi
