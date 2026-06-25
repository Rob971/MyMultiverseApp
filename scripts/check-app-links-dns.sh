#!/usr/bin/env bash
# Check whether mymultiverse.app DNS is ready for Firebase Hosting (custom domain migration).
# Does not call Firebase APIs — compares public DNS to known Squarespace vs Firebase targets.
#
# Usage: ./scripts/check-app-links-dns.sh
# Optional: FIREBASE_HOSTING_A=199.36.158.100 (override expected apex A record)

set -euo pipefail

HOST="${APP_LINKS_HOST:-mymultiverse.app}"
WWW_HOST="www.${HOST}"
FIREBASE_A="${FIREBASE_HOSTING_A:-199.36.158.100}"
SQUARESPACE_A="198.49.23.144"

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

warn() {
  echo "WARN: $*" >&2
}

echo "==> Nameservers for ${HOST}"
if command -v dig >/dev/null 2>&1; then
  dig +short NS "${HOST}" | sed 's/^/  /' || true
else
  warn "dig not installed; skipping NS lookup"
fi

echo "==> Apex A record (${HOST})"
APEX_A=""
if command -v dig >/dev/null 2>&1; then
  APEX_A="$(dig +short A "${HOST}" | head -1 || true)"
fi
if [[ -z "${APEX_A}" ]]; then
  warn "No apex A record found (may be OK if using AAAA only — confirm in Firebase console)"
else
  echo "  ${APEX_A}"
  if [[ "${APEX_A}" == "${SQUARESPACE_A}" ]]; then
    fail "Apex still points to Squarespace (${SQUARESPACE_A}). Update DNS per docs/app-links-custom-dns.md"
  fi
  if [[ "${APEX_A}" != "${FIREBASE_A}" ]]; then
    warn "Apex A is ${APEX_A}, expected Firebase ${FIREBASE_A} (confirm against Firebase Hosting wizard)"
  else
    echo "OK — apex A matches Firebase Hosting IP"
  fi
fi

echo "==> www CNAME/A (${WWW_HOST})"
if command -v dig >/dev/null 2>&1; then
  WWW_CNAME="$(dig +short CNAME "${WWW_HOST}" | head -1 || true)"
  WWW_A="$(dig +short A "${WWW_HOST}" | head -1 || true)"
  if [[ -n "${WWW_CNAME}" ]]; then
    echo "  CNAME → ${WWW_CNAME}"
    if [[ "${WWW_CNAME}" == *"squarespace"* ]]; then
      warn "www still CNAMEs to Squarespace — add www in Firebase Hosting or remove this CNAME"
    fi
  elif [[ -n "${WWW_A}" ]]; then
    echo "  A → ${WWW_A}"
    if [[ "${WWW_A}" == "${SQUARESPACE_A}" ]]; then
      warn "www still points to Squarespace"
    fi
  else
    echo "  (no www record)"
  fi
fi

echo "==> HTTPS probe (apex)"
if command -v curl >/dev/null 2>&1; then
  STATUS="$(curl -sS -o /dev/null -w '%{http_code}' -L "https://${HOST}/.well-known/assetlinks.json" || echo "000")"
  SERVER="$(curl -sSI "https://${HOST}/" 2>/dev/null | awk -F': ' 'tolower($1)=="server"{print $2; exit}' | tr -d '\r')"
  echo "  assetlinks.json HTTP ${STATUS}"
  [[ -n "${SERVER}" ]] && echo "  Server: ${SERVER}"
  if [[ "${SERVER}" == *"Squarespace"* ]]; then
    fail "HTTPS still served by Squarespace — complete DNS migration first"
  fi
  if [[ "${STATUS}" == "200" ]]; then
    echo "OK — assetlinks.json reachable on apex"
  else
    warn "assetlinks.json not 200 yet (deploy mymultiverse-website after DNS + run verify-hosting.sh)"
  fi
else
  warn "curl not installed; skipping HTTPS probe"
fi

echo "DNS check finished for ${HOST}"
