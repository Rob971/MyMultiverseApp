# App Links custom DNS — `mymultiverse.app` → Firebase Hosting

Use this when moving **off Squarespace website hosting** so `https://mymultiverse.app` serves Firebase Hosting (`mymultiverseapp`) for App Links and the `/invite` fallback page.

## Current state (check with `./scripts/check-app-links-dns.sh`)

| Check | Typical value today |
|-------|---------------------|
| Apex A | `198.49.23.144` (Squarespace) |
| `www` | `ext-sq.squarespace.com` / Squarespace A records |
| Nameservers | `ns-cloud-b*.googledomains.com` (Squarespace Domains) |

The Android app and Supabase invite emails expect the **apex**: `https://mymultiverse.app/invite?token=…`

## 1. Add the domain in Firebase (source of truth for records)

1. Open [Firebase Hosting — mymultiverseapp](https://console.firebase.google.com/project/mymultiverseapp/hosting).
2. **Add custom domain** → enter `mymultiverse.app`.
3. Copy the **TXT** verification record and the **A** (and any **AAAA**) records shown in the wizard.  
   Firebase docs often show apex A → `199.36.158.100`, but **always use the values from your console** — they can change.
4. Optionally add `www.mymultiverse.app` in the same wizard if you want `www` to work (recommended so the old 301 to www does not 404).

Leave the wizard open until DNS propagates and status becomes **Connected**.

## 2. Update DNS at Squarespace Domains

DNS is managed at [Squarespace Domains](https://domains.squarespace.com) (formerly Google Domains) — nameservers stay `ns-cloud-b*.googledomains.com`.

1. Open **DNS** for `mymultiverse.app`.
2. **Remove** (or disable) records that point to Squarespace hosting:
   - Apex **A** → `198.49.23.144` (and any duplicate Squarespace A/AAAA)
   - **CNAME** `www` → `ext-sq.squarespace.com`
   - Any other Squarespace **A** / **CNAME** on `@` or `www` that conflict with Firebase
3. **Add** records from the Firebase wizard:
   - **TXT** on `@` (apex) — domain verification
   - **A** on `@` → Firebase IP(s)
   - If Firebase lists **www**: add the **A** or **CNAME** for `www` as shown
4. Save and wait for propagation (minutes to 48h; usually &lt; 1h).

Do **not** change nameservers unless you are moving DNS to another provider entirely.

## 3. Deploy `web/` to Firebase Hosting

After DNS propagates (or with **Skip verify** while DNS is still on Squarespace):

```bash
gh workflow run "App Links hosting" --ref main -f skip_verify=true
```

Re-run with `skip_verify=false` once DNS resolves:

```bash
gh workflow run "App Links hosting" --ref main -f skip_verify=false
```

Or locally (service account JSON required):

```bash
chmod +x scripts/print-android-apk-fingerprint.sh scripts/deploy-app-links-hosting.sh scripts/check-app-links-dns.sh
./scripts/check-app-links-dns.sh
ANDROID_SHA256_FINGERPRINT="$(./scripts/print-android-apk-fingerprint.sh composeApp/build/outputs/apk/debug/composeApp-debug.apk)" \
  GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json \
  ./scripts/deploy-app-links-hosting.sh
```

Optional — iOS Universal Links: set `IOS_TEAM_ID=YOUR_APPLE_TEAM_ID` on the deploy command (generates `apple-app-site-association`).

## 4. Verify

```bash
./scripts/check-app-links-dns.sh      # DNS points at Firebase, not Squarespace
./scripts/verify-app-links-hosting.sh # HTTP 200 for assetlinks, AASA, /invite
```

On device: tap an invite link `https://mymultiverse.app/invite?token=…` — Android should open the app when App Links are verified.

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Firebase stuck on **Needs setup** | Remove conflicting Squarespace A/CNAME on `@` and `www`; wait for TXT verification |
| `verify-app-links-hosting.sh` 301 to www then 404 | Add `www.mymultiverse.app` in Firebase Hosting or redirect www → apex in Firebase |
| Workflow fails on credentials | Add repo secret `FIREBASE_SERVICE_ACCOUNT_JSON` |
| iOS Universal Links needed later | Re-deploy with `IOS_TEAM_ID=…` locally or add optional workflow input |
| SSL pending | DNS must point only to Firebase; duplicate A records to old host block certificate issuance |

## Related files

- `firebase.json` — Hosting config for `web/`
- `scripts/deploy-app-links-hosting.sh` — generate well-known + deploy
- `scripts/verify-app-links-hosting.sh` — post-deploy HTTP checks
- `README.md` — App Links section
