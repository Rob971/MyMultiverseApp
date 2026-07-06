# IP evidence archives

Run from the app repo root:

```bash
chmod +x scripts/create-ip-evidence-archive.sh
./scripts/create-ip-evidence-archive.sh
```

This creates `docs/ip-evidence/archive-YYYY-MM-DD/` with:

- **logos/** — copies of brand PNGs and in-app logo
- **MANIFEST.md** — git HEAD, release tags, logo commit history
- **ip-evidence-YYYY-MM-DD.tar.gz** — portable bundle for offline backup

Archives under `archive-*/` are gitignored (local evidence only). Copy the `.tar.gz` to cloud backup or share with your lawyer when needed.
