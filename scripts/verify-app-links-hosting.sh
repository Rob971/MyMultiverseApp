#!/usr/bin/env bash
# Verify mymultiverse.app App Links / Universal Links hosting.
# Requires: curl

set -euo pipefail

HOST="${APP_LINKS_HOST:-mymultiverse.app}"
BASE="https://${HOST}"

fail() {
  echo "ERROR: $*" >&2
  exit 1
}

curl_well_known() {
  local path="$1"
  curl -sS -L -o "${2}" -w '%{http_code}' "${BASE}${path}"
}

echo "==> assetlinks.json"
ASSET_BODY="$(mktemp)"
ASSET_STATUS="$(curl_well_known "/.well-known/assetlinks.json" "${ASSET_BODY}")"
[[ "${ASSET_STATUS}" == "200" ]] || fail "assetlinks.json returned ${ASSET_STATUS} (apex must serve Firebase Hosting — disconnect Squarespace DNS if you see 301/404)"
grep -q 'delegate_permission/common.handle_all_urls' "${ASSET_BODY}" || fail "assetlinks.json missing relation"
grep -q 'app.mymultiverse.kmp' "${ASSET_BODY}" || fail "assetlinks.json missing package_name"
if grep -q 'REPLACE_WITH_RELEASE_SHA256_FINGERPRINT' "${ASSET_BODY}"; then
  fail "assetlinks.json still contains placeholder fingerprint"
fi
rm -f "${ASSET_BODY}"
echo "OK"

if [[ "${VERIFY_IOS_UNIVERSAL_LINKS:-0}" == "1" ]]; then
  echo "==> apple-app-site-association"
  AASA_BODY="$(mktemp)"
  AASA_STATUS="$(curl_well_known "/.well-known/apple-app-site-association" "${AASA_BODY}")"
  [[ "${AASA_STATUS}" == "200" ]] || fail "apple-app-site-association returned ${AASA_STATUS}"
  grep -q 'applinks' "${AASA_BODY}" || fail "AASA missing applinks"
  grep -q '/invite' "${AASA_BODY}" || fail "AASA missing /invite path"
  if grep -q 'TEAMID' "${AASA_BODY}"; then
    fail "AASA still contains TEAMID placeholder"
  fi
  rm -f "${AASA_BODY}"
  echo "OK"
else
  echo "==> apple-app-site-association (skipped — Android-only; set VERIFY_IOS_UNIVERSAL_LINKS=1 to check iOS)"
fi

echo "==> invite fallback page"
INVITE_BODY="$(mktemp)"
INVITE_STATUS="$(curl -sS -L -o "${INVITE_BODY}" -w '%{http_code}' "${BASE}/invite?token=ci-smoke-test")"
[[ "${INVITE_STATUS}" == "200" ]] || fail "/invite returned ${INVITE_STATUS}"
grep -qi 'Ammò' "${INVITE_BODY}" || fail "/invite missing landing copy"
rm -f "${INVITE_BODY}"
echo "OK"

echo "All App Links hosting checks passed for ${BASE}"
