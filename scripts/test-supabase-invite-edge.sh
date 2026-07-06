#!/usr/bin/env bash
# Unit-test pure helpers for notify-household-invite (Deno).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FUNCTION_DIR="${ROOT_DIR}/supabase/functions/notify-household-invite"

if [[ ! -d "${FUNCTION_DIR}" ]]; then
  echo "ERROR: ${FUNCTION_DIR} not found" >&2
  exit 1
fi

if ! command -v deno >/dev/null 2>&1; then
  echo "ERROR: deno is required. Install from https://deno.land" >&2
  exit 1
fi

cd "${FUNCTION_DIR}"
deno test invite-content_test.ts invite-token_test.ts notification-i18n_test.ts grocery-item-added-token_test.ts grocery-list-nudge-token_test.ts meal-plan-nudge-token_test.ts
