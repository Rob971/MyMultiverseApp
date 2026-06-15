#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if ! command -v supabase >/dev/null 2>&1; then
  echo "Supabase CLI not found."
  echo "Install: brew install supabase/tap/supabase"
  echo "Docs: https://supabase.com/docs/guides/cli"
  exit 1
fi

if ! supabase projects list >/dev/null 2>&1; then
  echo "Supabase CLI is not authenticated."
  echo ""
  echo "Create a token at https://supabase.com/dashboard/account/tokens then run:"
  echo "  supabase login --token sbp_xxxxxxxx"
  echo ""
  echo "Then link the project (once):"
  echo "  supabase link --project-ref ivjdzreazvkrrirecznk"
  exit 1
fi

if [[ ! -f supabase/.temp/project-ref ]] && [[ ! -f supabase/config.toml ]]; then
  echo "Project not linked yet. Run once from the repo root:"
  echo "  supabase link --project-ref ivjdzreazvkrrirecznk"
  exit 1
fi

echo "Applying migrations from supabase/migrations ..."
if ! supabase db push; then
  echo ""
  echo "If you see 'Remote migration versions not found in local migrations directory',"
  echo "the dashboard SQL history does not match this repo. Inspect:"
  echo "  supabase migration list"
  echo ""
  echo "Typical fix when schema was applied manually in the SQL editor:"
  echo "  1. Revert orphan remote-only versions (use IDs from migration list):"
  echo "     supabase migration repair --status reverted <remote_id> ..."
  echo "  2. Mark repo migrations already present in the DB as applied:"
  echo "     supabase migration repair --status applied 20250615120000 ..."
  echo "  3. Re-run: ./scripts/apply-supabase-migrations.sh"
  exit 1
fi

echo "Done. Verify RLS and Realtime in the Supabase dashboard."
