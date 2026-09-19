#!/usr/bin/env bash
# CI: prove the F2 favorite-dish invariants on the local Supabase stack (after `supabase start`).
# Requires psql + jq. Covers owner-only RLS, the 10-item cap, concurrency-safety of the cap
# (two psql sessions — A holds the 10th uncommitted, B blocks then fails), atomic replace,
# and GDPR export. This is the DB-level proof; the app-side behavior is unit-tested separately.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for tool in supabase jq psql; do
  if ! command -v "$tool" >/dev/null 2>&1; then
    echo "ERROR: $tool required" >&2
    exit 1
  fi
done

DB_URL="$(supabase status -o json 2>/dev/null | jq -r '.DB_URL // empty')"
if [[ -z "$DB_URL" ]]; then
  echo "ERROR: local Supabase stack is not running — run supabase start first" >&2
  exit 1
fi

USER_A="11111111-1111-1111-1111-111111111111"
USER_B="22222222-2222-2222-2222-222222222222"

# Run SQL as a given authenticated user by setting request.jwt.claim.sub (what auth.uid() reads).
# The claim is session-level (is_local = false): each -c runs in its own transaction, so a
# transaction-local claim would be gone before the SQL below runs and auth.uid() would be null.
as_user() {
  local uid="$1"; shift
  psql "$DB_URL" -v ON_ERROR_STOP=1 -qAt \
    -c "set role authenticated;" \
    -c "select set_config('request.jwt.claim.sub', '$uid', false);" \
    -c "$1"
}

echo "==> 1. owner-only RLS: A inserts, B cannot see it"
as_user "$USER_A" "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Pasta');"
B_VISIBLE="$(as_user "$USER_B" "select count(*) from public.user_favorite_dishes;")"
[[ "$B_VISIBLE" == "0" ]] || { echo "ERROR: B saw A's favorite ($B_VISIBLE)" >&2; exit 1; }
echo "OK: cross-user select denied"

echo "==> 1b. normalised_label is computed by the database"
if as_user "$USER_A" "insert into public.user_favorite_dishes (user_id, label, normalised_label) values ('$USER_A','Pesto','forged');" 2>/dev/null; then
  echo "ERROR: a client-chosen normalised_label was accepted" >&2
  exit 1
fi
if as_user "$USER_A" "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','  PASTA ');" 2>/dev/null; then
  echo "ERROR: a look-alike of 'Pasta' was saved as a new favorite" >&2
  exit 1
fi
[[ "$(as_user "$USER_A" "select normalised_label from public.user_favorite_dishes where label = 'Pasta';")" == "pasta" ]] \
  || { echo "ERROR: normalised_label was not computed as lower(trim(label))" >&2; exit 1; }
echo "OK: normalised_label is database-computed; forged keys and look-alikes are rejected"

echo "==> 2. cap of 10: 11th insert is rejected"
for i in $(seq 2 10); do
  as_user "$USER_A" "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Dish $i');"
done
if as_user "$USER_A" "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Eleventh');" 2>/dev/null; then
  echo "ERROR: 11th favorite was accepted" >&2
  exit 1
fi
echo "OK: 11th insert rejected by trigger"

echo "==> 3. concurrency: A holds uncommitted, B blocks then fails; still exactly 10 rows"
as_user "$USER_A" "delete from public.user_favorite_dishes where normalised_label='dish 10';"  # back to 9
# Session A: insert the 10th and hold the transaction open.
( as_user "$USER_A" "begin; insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Dish 10'); select pg_sleep(3); commit;" ) &
A_PID=$!
sleep 1
if as_user "$USER_A" "select public.add_favorite('Concurrent');" 2>/dev/null; then
  # B (same user here) must not be able to add an 11th while the lock is held.
  echo "ERROR: concurrent add succeeded while cap lock held" >&2
  exit 1
fi
wait "$A_PID"
ROWS="$(as_user "$USER_A" "select count(*) from public.user_favorite_dishes;")"
[[ "$ROWS" == "10" ]] || { echo "ERROR: expected 10 rows, got $ROWS" >&2; exit 1; }
echo "OK: cap held under concurrency (still 10 rows)"

echo "==> 4. atomic replace"
as_user "$USER_A" "select public.replace_favorite('dish 1','Replaced Dish');"
[[ "$(as_user "$USER_A" "select count(*) from public.user_favorite_dishes where normalised_label='dish 1';")" == "0" ]] || { echo "ERROR: replace left old row" >&2; exit 1; }
[[ "$(as_user "$USER_A" "select count(*) from public.user_favorite_dishes where normalised_label='replaced dish';")" == "1" ]] || { echo "ERROR: replace didn't add new row" >&2; exit 1; }
echo "OK: replace atomic"

echo "==> 5. GDPR export includes favorites"
EXPORT="$(as_user "$USER_A" "select public.export_my_personal_data()->>'favorite_dishes';")"
echo "$EXPORT" | grep -q 'replaced dish' || { echo "ERROR: export missing favorite_dishes" >&2; exit 1; }
echo "OK: export_my_personal_data contains favorite_dishes"

echo "All favorite-dish DB checks passed."