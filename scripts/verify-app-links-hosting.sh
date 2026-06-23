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

echo "==> assetlinks.json"
ASSET_BODY="$(mktemp)"
ASSET_STATUS="$(curl -sS -o "${ASSET_BODY}" -w '%{http_code}' "${BASE}/.well-known/assetlinks.json")"
[[ "${ASSET_STATUS}" == "200" ]] || fail "assetlinks.json returned ${ASSET_STATUS}"
grep -q 'delegate_permission/common.handle_all_urls' "${ASSET_BODY}" || fail "assetlinks.json missing relation"
grep -q 'app.mymultiverse.kmp' "${ASSET_BODY}" || fail "assetlinks.json missing package_name"
if grep -q 'REPLACE_WITH_RELEASE_SHA256_FINGERPRINT' "${ASSET_BODY}"; then
  fail "assetlinks.json still contains placeholder fingerprint"
fi
rm -f "${ASSET_BODY}"
echo "OK"

echo "==> apple-app-site-association"
AASA_BODY="$(mktemp)"
AASA_STATUS="$(curl -sS -o "${AASA_BODY}" -w '%{http_code}' "${BASE}/.well-known/apple-app-site-association")"
[[ "${AASA_STATUS}" == "200" ]] || fail "apple-app-site-association returned ${AASA_STATUS}"
grep -q 'applinks' "${AASA_BODY}" || fail "AASA missing applinks"
grep -q '/invite' "${AASA_BODY}" || fail "AASA missing /invite path"
if grep -q 'TEAMID' "${AASA_BODY}"; then
  fail "AASA still contains TEAMID placeholder"
fi
rm -f "${AASA_BODY}"
echo "OK"

echo "==> invite fallback page"
INVITE_BODY="$(mktemp)"
INVITE_STATUS="$(curl -sS -o "${INVITE_BODY}" -w '%{http_code}' "${BASE}/invite?token=ci-smoke-test")"
[[ "${INVITE_STATUS}" == "200" ]] || fail "/invite returned ${INVITE_STATUS}"
grep -qi 'MyMultiverse' "${INVITE_BODY}" || fail "/invite missing landing copy"
rm -f "${INVITE_BODY}"
echo "OK"

echo "All App Links hosting checks passed for ${BASE}"
