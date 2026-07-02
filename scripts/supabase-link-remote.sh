#!/usr/bin/env bash
# Link the local supabase/ tree to the hosted project for db push / db query.
# Prefer SUPABASE_ACCESS_TOKEN (password-less CLI login); fall back to DB password.
set -euo pipefail

project_ref="${SUPABASE_PROJECT_REF:-}"
if [[ -z "${project_ref}" ]]; then
  echo "ERROR: SUPABASE_PROJECT_REF must be set" >&2
  exit 1
fi

if [[ -n "${SUPABASE_ACCESS_TOKEN:-}" ]]; then
  echo "==> Linking project ${project_ref} via access token"
  unset SUPABASE_DB_PASSWORD
  if supabase link --project-ref "${project_ref}" --skip-pooler; then
    exit 0
  fi
  echo "WARN: direct link failed; retrying without --skip-pooler"
  supabase link --project-ref "${project_ref}"
elif [[ -n "${SUPABASE_DB_PASSWORD:-}" ]]; then
  echo "==> Linking project ${project_ref} via database password"
  supabase link --project-ref "${project_ref}" --password "${SUPABASE_DB_PASSWORD}"
else
  echo "ERROR: set SUPABASE_ACCESS_TOKEN or SUPABASE_DB_PASSWORD" >&2
  exit 1
fi
