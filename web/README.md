# Website moved

Public site and Firebase Hosting for **mymultiverse.app** live in a dedicated repo:

**https://github.com/Rob971/mymultiverse-website**

That repo owns company pages, `/privacy`, `/terms`, `/invite`, `/.well-known/*` for App Links, and hosted **Ammò brand assets** at `/brand/ammo-round-logo-*.png` (used by invite emails and the app marketing pages).

This app repo keeps the in-app copy under `composeApp/src/commonMain/composeResources/drawable/ammo_round_logo.webp` plus platform launcher icons. When updating the logo, sync website `public/brand/` and regenerate app assets together.

APK fingerprint helper: `scripts/print-android-apk-fingerprint.sh`.
