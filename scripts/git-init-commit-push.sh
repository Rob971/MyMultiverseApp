#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

# Recover from a failed `git init` (e.g. missing hooks directory).
if [[ -d .git ]] && [[ ! -d .git/hooks ]]; then
  echo "Removing incomplete .git directory."
  rm -rf .git
fi

if [[ ! -d .git ]]; then
  git init -b main
fi

git add -A
if git diff --staged --quiet; then
  echo "Nothing to commit."
else
  git commit -m "Initial commit: KMP Compose Voyager Koin clean architecture scaffold

Add Compose Multiplatform composeApp module targeting Android and iOS with
Voyager navigation, Koin DI, and domain/data/presentation layers. Include Gradle
version catalog, iOS Xcode wrapper, Cursor rules, and docs for Cursor official
library indexing."
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "Install GitHub CLI (gh) or add remote manually, then: git push -u origin main"
  exit 0
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "Run: gh auth login -h github.com"
  exit 1
fi

REPO_NAME="${1:-ammo}"
VISIBILITY="${2:-public}"

if git remote get-url origin >/dev/null 2>&1; then
  echo "Remote origin already set; pushing."
  git push -u origin main
else
  gh repo create "$REPO_NAME" --"$VISIBILITY" --source=. --remote=origin --push
fi

echo "Done."
