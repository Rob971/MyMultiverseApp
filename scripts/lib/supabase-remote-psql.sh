#!/usr/bin/env bash
# Run SQL against hosted Supabase Postgres.
# Prefer `supabase db query --linked` (Management API) in CI; fall back to psql with IPv4.

_supabase_can_query_linked() {
  command -v supabase >/dev/null 2>&1 &&
    {
      [[ -n "${SUPABASE_ACCESS_TOKEN:-}" ]] ||
        [[ -f ".supabase/project-ref" ]] ||
        [[ -f "supabase/.temp/project-ref" ]]
    }
}

escape_sql_literal() {
  printf '%s' "$1" | sed "s/'/''/g"
}

_supabase_remote_psql_conn() {
  if [[ -z "${SUPABASE_PROJECT_REF:-}" || -z "${SUPABASE_DB_PASSWORD:-}" ]]; then
    echo "ERROR: SUPABASE_PROJECT_REF and SUPABASE_DB_PASSWORD must be set" >&2
    return 1
  fi

  export PGPASSWORD="${SUPABASE_DB_PASSWORD}"
  local host="db.${SUPABASE_PROJECT_REF}.supabase.co"
  local conn="host=${host} port=5432 dbname=postgres user=postgres sslmode=require"

  if command -v dig >/dev/null 2>&1; then
    local ipv4
    ipv4="$(dig +short A "${host}" | grep -E '^[0-9.]+$' | head -1)"
    if [[ -n "${ipv4}" ]]; then
      conn="${conn} hostaddr=${ipv4}"
    fi
  elif command -v getent >/dev/null 2>&1; then
    local ipv4
    ipv4="$(getent ahostsv4 "${host}" 2>/dev/null | awk '{print $1; exit}')"
    if [[ -n "${ipv4}" ]]; then
      conn="${conn} hostaddr=${ipv4}"
    fi
  fi

  printf '%s' "${conn}"
}

supabase_remote_query() {
  local sql="$1"
  if _supabase_can_query_linked; then
    printf '%s\n' "${sql}" | supabase db query --linked
    return
  fi

  if ! command -v psql >/dev/null 2>&1; then
    echo "ERROR: supabase CLI (linked) or psql is required" >&2
    return 1
  fi
  psql "$(_supabase_remote_psql_conn)" -v ON_ERROR_STOP=1 -c "${sql}"
}

supabase_remote_query_scalar() {
  local sql="$1"
  if _supabase_can_query_linked; then
    local json
    json="$(supabase db query --linked --output json "${sql}")"
    PYTHON_JSON="${json}" python3 -c 'import json, os; row=json.loads(os.environ["PYTHON_JSON"])[0]; print(next(iter(row.values())))'
    return
  fi

  if ! command -v psql >/dev/null 2>&1; then
    echo "ERROR: supabase CLI (linked) or psql is required" >&2
    return 1
  fi
  psql "$(_supabase_remote_psql_conn)" -v ON_ERROR_STOP=1 -tA -c "${sql}"
}

supabase_remote_psql() {
  if _supabase_can_query_linked; then
    echo "ERROR: heredoc psql is unavailable when using supabase db query; use supabase_remote_query instead" >&2
    return 1
  fi
  if ! command -v psql >/dev/null 2>&1; then
    echo "ERROR: psql is required (install postgresql-client)" >&2
    return 1
  fi
  psql "$(_supabase_remote_psql_conn)" "$@"
}
