#!/usr/bin/env bash
# Shared remote psql connection for hosted Supabase Postgres.
# GitHub Actions runners often lack IPv6 routes to db.<ref>.supabase.co; prefer IPv4 via hostaddr.

supabase_remote_psql_conn() {
  if [[ -z "${SUPABASE_PROJECT_REF:-}" || -z "${SUPABASE_DB_PASSWORD:-}" ]]; then
    echo "ERROR: SUPABASE_PROJECT_REF and SUPABASE_DB_PASSWORD must be set" >&2
    return 1
  fi

  export PGPASSWORD="${SUPABASE_DB_PASSWORD}"
  local host="db.${SUPABASE_PROJECT_REF}.supabase.co"
  local conn="host=${host} port=5432 dbname=postgres user=postgres sslmode=require"

  if command -v getent >/dev/null 2>&1; then
    local ipv4
    ipv4="$(getent ahostsv4 "${host}" 2>/dev/null | awk '{print $1; exit}')"
    if [[ -n "${ipv4}" ]]; then
      conn="${conn} hostaddr=${ipv4}"
    fi
  fi

  printf '%s' "${conn}"
}

supabase_remote_psql() {
  if ! command -v psql >/dev/null 2>&1; then
    echo "ERROR: psql is required (install postgresql-client)" >&2
    return 1
  fi
  psql "$(supabase_remote_psql_conn)" "$@"
}
