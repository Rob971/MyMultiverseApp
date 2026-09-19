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
# The claim is session-level (SET, not SET LOCAL): each -c runs in its own transaction, so a
# transaction-local claim would be gone before the SQL below runs and auth.uid() would be null.
# SET prints nothing, so a captured result is only the statement's own output.
as_user() {
  local uid="$1"; shift
  psql "$DB_URL" -v ON_ERROR_STOP=1 -qAt \
    -c "set role authenticated;" \
    -c "set request.jwt.claim.sub = '$uid';" \
    -c "$1"
}

# A statement that must fail, with the expected error. Any other error (missing grant, missing
# user, typo) fails the script instead of passing as "rejected".
expect_error() {
  local pattern="$1" uid="$2" sql="$3" out
  if out="$(as_user "$uid" "$sql" 2>&1)"; then
    echo "ERROR: expected '$pattern' but it succeeded: $sql" >&2
    exit 1
  fi
  grep -q -- "$pattern" <<<"$out" || { echo "ERROR: expected '$pattern', got: $out" >&2; exit 1; }
}

echo "==> 0. test users exist (user_id references auth.users)"
psql "$DB_URL" -v ON_ERROR_STOP=1 -qAt -c "insert into auth.users (id, email) values
  ('$USER_A', 'favorites-a@example.test'), ('$USER_B', 'favorites-b@example.test')
  on conflict (id) do nothing;"

echo "==> 1. owner-only RLS: A inserts, B cannot see it"
as_user "$USER_A" "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Pasta');"
B_VISIBLE="$(as_user "$USER_B" "select count(*) from public.user_favorite_dishes;")"
[[ "$B_VISIBLE" == "0" ]] || { echo "ERROR: B saw A's favorite ($B_VISIBLE)" >&2; exit 1; }
echo "OK: cross-user select denied"

echo "==> 1b. normalised_label is computed by the database"
expect_error 'cannot insert a non-DEFAULT value into column "normalised_label"' "$USER_A" \
  "insert into public.user_favorite_dishes (user_id, label, normalised_label) values ('$USER_A','Pesto','forged');"
expect_error 'duplicate key value violates unique constraint' "$USER_A" \
  "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','  PASTA ');"
[[ "$(as_user "$USER_A" "select normalised_label from public.user_favorite_dishes where label = 'Pasta';")" == "pasta" ]] \
  || { echo "ERROR: normalised_label was not computed as lower(trim(label))" >&2; exit 1; }
echo "OK: normalised_label is database-computed; forged keys and look-alikes are rejected"

echo "==> 2. cap of 10: 11th insert is rejected"
for i in $(seq 2 10); do
  as_user "$USER_A" "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Dish $i');"
done
expect_error 'favorite_cap_exceeded' "$USER_A" \
  "insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Eleventh');"
echo "OK: 11th insert rejected by trigger"

echo "==> 3. concurrency: A holds uncommitted, B blocks then fails; still exactly 10 rows"
as_user "$USER_A" "delete from public.user_favorite_dishes where normalised_label='dish 10';"  # back to 9
# Session A: insert the 10th and hold the transaction open.
( as_user "$USER_A" "begin; insert into public.user_favorite_dishes (user_id, label) values ('$USER_A','Dish 10'); select pg_sleep(3); commit;" ) &
A_PID=$!
sleep 1
# B (same user here) blocks on the cap lock until A commits the 10th, then must hit the cap.
expect_error 'favorite_cap_exceeded' "$USER_A" "select public.add_favorite('Concurrent');"
wait "$A_PID"
ROWS="$(as_user "$USER_A" "select count(*) from public.user_favorite_dishes;")"
[[ "$ROWS" == "10" ]] || { echo "ERROR: expected 10 rows, got $ROWS" >&2; exit 1; }
echo "OK: cap held under concurrency (still 10 rows)"

echo "==> 4. atomic replace"
as_user "$USER_A" "select public.replace_favorite('pasta','Replaced Dish');"
[[ "$(as_user "$USER_A" "select count(*) from public.user_favorite_dishes where normalised_label='pasta';")" == "0" ]] || { echo "ERROR: replace left old row" >&2; exit 1; }
[[ "$(as_user "$USER_A" "select count(*) from public.user_favorite_dishes where normalised_label='replaced dish';")" == "1" ]] || { echo "ERROR: replace didn't add new row" >&2; exit 1; }
echo "OK: replace atomic"

echo "==> 5. GDPR export includes favorites"
EXPORT="$(as_user "$USER_A" "select public.export_my_personal_data()->>'favorite_dishes';")"
# Labels are exported as the user typed them.
jq -e 'index("Replaced Dish") != null' <<<"$EXPORT" >/dev/null || { echo "ERROR: export missing favorite_dishes: $EXPORT" >&2; exit 1; }
echo "OK: export_my_personal_data contains favorite_dishes"

echo "All favorite-dish DB checks passed."