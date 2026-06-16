#!/usr/bin/env bash
# One-time fix when schema was applied in Supabase SQL Editor before using this repo's migrations.
# Updates the migration HISTORY table only — does not re-run SQL that is already in the database.
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Current migration status:"
supabase migration list
echo ""

echo "Step 1: Remove orphan dashboard-only history entries ..."
supabase migration repair --status reverted 20260615145754 20260615145817

echo ""
echo "Step 2: Mark repo migrations as applied (schema already in DB from SQL editor) ..."
supabase migration repair --status applied \
  20250615120000 \
  20250615120100 \
  20250615130000 \
  20250615140000

echo ""
echo "Step 3: Push any migration not yet in the database (e.g. space_invites) ..."
if supabase db push; then
  echo "All migrations in sync."
else
  echo ""
  echo "If push failed because objects already exist, mark the last migration applied:"
  echo "  supabase migration repair --status applied 20250615150000"
  exit 1
fi

echo ""
supabase migration list
echo "Done."
