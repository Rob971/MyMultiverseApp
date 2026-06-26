# Website moved

Public site and Firebase Hosting for **mymultiverse.app** live in a dedicated repo:

**https://github.com/Rob971/mymultiverse-website**

That repo owns company pages, `/privacy`, `/terms`, `/invite`, `/.well-known/*` for App Links, and hosted **Ammò brand assets** at `/brand/ammo-round-logo-*.png` (used by invite emails and marketing pages).

## Logo sync (app ↔ website)

| Location | Purpose |
|----------|---------|
| `composeApp/.../drawable/ammo_round_logo.webp` | Rich in-app badge (~100 KB) |
| `iosApp/.../app-icon-1024.png` | iOS launcher generated from rich badge (~1.8 MB) |
| `composeApp/src/androidMain/res/` | Android launcher + notification icons |
| **`web/brand/`** (this repo) | Copy into website `public/brand/` before deploy |
| `web/site-updates/` | Reference copies of HTML/CSS/script changes for the website repo |

Invite emails reference `https://mymultiverse.app/brand/ammo-round-logo-256.png` — deploy the website after updating `public/brand/` or invite logos will not load.

### Deploy website brand update

1. Copy `web/brand/*` → `mymultiverse-website/public/brand/`
2. Apply HTML/CSS changes from `web/site-updates/` (or merge branch `cursor/ammo-brand-logo-078a` if pushed manually)
3. Run website `scripts/deploy.sh` or the **Deploy hosting** GitHub workflow

APK fingerprint helper: `scripts/print-android-apk-fingerprint.sh`.
