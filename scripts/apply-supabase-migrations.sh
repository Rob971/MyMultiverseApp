#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if ! command -v supabase >/dev/null 2>&1; then
  echo "Install Supabase CLI: https://supabase.com/docs/guides/cli"
  echo "Then link the project: supabase link --project-ref ivjdzreazvkrrirecznk"
  exit 1
fi

echo "Applying migrations from supabase/migrations ..."
supabase db push

echo "Done. Verify RLS and Realtime in the Supabase dashboard."
