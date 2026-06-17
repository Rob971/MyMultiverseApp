#!/usr/bin/env bash
# CI: prove supabase/migrations apply cleanly on a fresh local Postgres (no remote db push).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: Docker is required for supabase db start" >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: Docker daemon is not available" >&2
  exit 1
fi

echo "==> Validating migration filenames"
migration_count=0
previous=""
for file in supabase/migrations/*.sql; do
  [[ -f "$file" ]] || continue
  migration_count=$((migration_count + 1))
  base="$(basename "$file" .sql)"
  if [[ ! "$base" =~ ^[0-9]{14}_[a-z0-9_]+$ ]]; then
    echo "ERROR: migration filename must match YYYYMMDDHHMMSS_name.sql: $file" >&2
    exit 1
  fi
  if [[ -n "$previous" && "$base" < "$previous" ]]; then
    echo "ERROR: migrations are not lexicographically ordered: $previous then $base" >&2
    exit 1
  fi
  previous="$base"
done
if [[ "$migration_count" -eq 0 ]]; then
  echo "ERROR: no SQL files in supabase/migrations" >&2
  exit 1
fi
echo "OK: ${migration_count} migration files"

echo "==> Applying migrations locally (supabase db start)"
supabase db start

echo "All Supabase migration checks passed."
