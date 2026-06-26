#!/usr/bin/env python3
"""Prune older Firebase App Distribution releases for the Android debug app.

Uses the App Distribution REST API (list + batchDelete). Authenticates via
FIREBASE_SERVICE_ACCOUNT_JSON / FIREBASE_SERVICE_ACCOUNT_JSON_BASE64 (same as CI)
or Application Default Credentials (gcloud auth application-default login).

Examples:
  # Preview deletions (default: keep 10 newest)
  python3 scripts/cleanup-firebase-appdistribution-releases.py --dry-run

  # Keep only the 5 newest releases
  python3 scripts/cleanup-firebase-appdistribution-releases.py --keep 5

  # Delete releases older than 90 days, always keep at least 5
  python3 scripts/cleanup-firebase-appdistribution-releases.py --older-than-days 90 --keep 5
"""

from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime, timedelta, timezone
from pathlib import Path

PROJECT_NUMBER = "37917280954"
FIREBASE_APP_ID = "1:37917280954:android:f25a8ccc787de9f3f91083"
API_BASE = "https://firebaseappdistribution.googleapis.com/v1"
SCOPE = "https://www.googleapis.com/auth/cloud-platform"


def access_token() -> str:
    if os.environ.get("FIREBASE_SERVICE_ACCOUNT_JSON") or os.environ.get(
        "FIREBASE_SERVICE_ACCOUNT_JSON_BASE64"
    ):
        script = Path(__file__).resolve().parent / "firebase-credentials.py"
        result = subprocess.run(
            [sys.executable, str(script), "token"],
            check=True,
            capture_output=True,
            text=True,
        )
        return result.stdout.strip()

    try:
        from google.auth.transport.requests import Request
        import google.auth
    except ImportError as exc:
        raise SystemExit(
            "Install google-auth and requests, or set FIREBASE_SERVICE_ACCOUNT_JSON.\n"
            "  pip install google-auth requests"
        ) from exc

    creds, _ = google.auth.default(scopes=[SCOPE])
    creds.refresh(Request())
    return creds.token


def api_request(
    method: str,
    path: str,
    token: str,
    *,
    query: dict[str, str] | None = None,
    body: dict | None = None,
) -> dict:
    url = f"{API_BASE}{path}"
    if query:
        url = f"{url}?{urllib.parse.urlencode(query)}"

    data = None
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/json",
    }
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"

    request = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            raw = response.read().decode("utf-8")
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise SystemExit(f"HTTP {exc.code} for {method} {path}: {detail}") from exc


def list_releases(token: str) -> list[dict]:
    parent = f"/projects/{PROJECT_NUMBER}/apps/{urllib.parse.quote(FIREBASE_APP_ID, safe='')}/releases"
    releases: list[dict] = []
    page_token = ""

    while True:
        query: dict[str, str] = {"pageSize": "100", "orderBy": "createTime desc"}
        if page_token:
            query["pageToken"] = page_token
        payload = api_request("GET", parent, token, query=query)
        releases.extend(payload.get("releases", []))
        page_token = payload.get("nextPageToken", "")
        if not page_token:
            break

    return releases


def parse_create_time(value: str) -> datetime:
    normalized = value.replace("Z", "+00:00")
    return datetime.fromisoformat(normalized).astimezone(timezone.utc)


def release_label(release: dict) -> str:
    display = release.get("displayVersion") or "?"
    build = release.get("buildVersion") or "?"
    created = release.get("createTime", "")[:19]
    release_id = release.get("name", "").rsplit("/", 1)[-1]
    return f"{created} v{display} ({build}) id={release_id}"


def select_releases_to_delete(
    releases: list[dict],
    *,
    keep: int,
    older_than_days: int | None,
) -> list[dict]:
    if not releases:
        return []

    sorted_releases = sorted(
        releases,
        key=lambda item: parse_create_time(item["createTime"]),
        reverse=True,
    )
    protected = sorted_releases[:keep]
    candidates = sorted_releases[keep:]

    if older_than_days is not None:
        cutoff = datetime.now(timezone.utc) - timedelta(days=older_than_days)
        candidates = [
            release
            for release in candidates
            if parse_create_time(release["createTime"]) < cutoff
        ]

    protected_names = {release["name"] for release in protected}
    return [release for release in candidates if release["name"] not in protected_names]


def batch_delete(token: str, names: list[str]) -> None:
    parent = (
        f"/projects/{PROJECT_NUMBER}/apps/"
        f"{urllib.parse.quote(FIREBASE_APP_ID, safe='')}/releases:batchDelete"
    )
    for index in range(0, len(names), 100):
        chunk = names[index : index + 100]
        api_request("POST", parent, token, body={"names": chunk})


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Delete older Firebase App Distribution releases."
    )
    parser.add_argument(
        "--keep",
        type=int,
        default=10,
        help="Minimum number of newest releases to keep (default: 10).",
    )
    parser.add_argument(
        "--older-than-days",
        type=int,
        default=None,
        help="Only delete releases older than this many days (optional).",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="List releases that would be deleted without deleting.",
    )
    args = parser.parse_args()

    if args.keep < 1:
        raise SystemExit("--keep must be at least 1.")

    token = access_token()
    releases = list_releases(token)
    to_delete = select_releases_to_delete(
        releases,
        keep=args.keep,
        older_than_days=args.older_than_days,
    )

    print(f"App: {FIREBASE_APP_ID}")
    print(f"Total releases: {len(releases)}")
    print(f"Keeping newest: {min(args.keep, len(releases))}")
    if args.older_than_days is not None:
        print(f"Also requiring age > {args.older_than_days} days for deletion")
    print(f"Selected for deletion: {len(to_delete)}")

    if not to_delete:
        print("Nothing to delete.")
        return

    for release in to_delete:
        print(f"  - {release_label(release)}")

    if args.dry_run:
        print("Dry run only; no releases deleted.")
        return

    batch_delete(token, [release["name"] for release in to_delete])
    print(f"Deleted {len(to_delete)} release(s).")


if __name__ == "__main__":
    main()
