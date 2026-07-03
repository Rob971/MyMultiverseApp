# P2 staging QA checklist

Run on **staging** (Supabase + Firebase tester APK) with **two phones / two accounts** before wide P2 distribution.

**Automated CI:** `scripts/verify-supabase-household.sh` with `SUPABASE_TEST_EMAIL` / `SUPABASE_TEST_PASSWORD` covers household bootstrap, nutrition persistence, GDPR export RPC, and device token registration.

**Manual cases:** [`firebase-appdistribution-testcases.yaml`](../firebase-appdistribution-testcases.yaml) (currently **v76**; ids below).

---

## Prerequisites

| Item | Done |
|------|------|
| PR1 merged: edge functions deployed; `SUPABASE_SERVICE_ROLE_KEY` set | ☑ (Supabase Deploy green on `main`) |
| PR2 merged: outbox `pg_net` trigger + delivery config | ☑ |
| `RESEND_API_KEY` set (for invite email) | ☐ |
| `FCM_SERVICE_ACCOUNT_JSON` set (for Android invite push) | ☐ |
| `APNS_KEY_ID` / `APNS_TEAM_ID` / `APNS_PRIVATE_KEY` set (for iOS invite push) | ☐ |
| Xcode Push Notifications capability enabled for `iosApp` | ☐ |
| Tester APK with `google-services.json` (FCM + Crashlytics) | ☐ |
| Two test accounts (owner + invitee) | ☐ |

---

## P2 manual cases

### `home-export-personal-data` — Export personal data (GDPR)

| Step | Pass |
|------|------|
| Sign in → Home → **Export my data** | ☐ |
| Share sheet opens with JSON on iOS and Android | ☐ |
| JSON includes profile + household affiliation | ☐ |

**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `home-delete-account` — Delete account (GDPR)

Use a **disposable** test account (not the shared CI smoke user).

| Step | Pass |
|------|------|
| Sole member or non-owner: Delete account → confirm → returns to login | ☐ |
| Owner with other members: error to transfer or dissolve first | ☐ |
| `delete-account` edge function completes; user cannot sign in again | ☐ |

**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `household-add-dependant` — Add dependant (no login)

| Step | Pass |
|------|------|
| Owner/editor → Members → Add dependant → display name | ☐ |
| Dependant badge on list; no email/login | ☐ |
| Remove dependant works | ☐ |
| Counts toward 20-member cap (note if at limit) | ☐ |

**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `household-member-avatar` — Member profile photo (1.1.6+)

Requires migration `20250703000000` and `member-avatars` bucket (`supabase seed buckets --linked`).

| Step | Pass |
|------|------|
| Each member row shows circular avatar (initials when no photo) | ☐ |
| Tap own avatar → gallery → photo appears on row | ☐ |
| Owner/editor: tap dependant avatar → set photo | ☐ |
| Second device / refresh: other members see updated avatar | ☐ |

**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `household-invite-notification` — Invite email / push

| Step | Pass |
|------|------|
| Phone A invites Phone B by email | ☐ |
| B receives invite email (Resend configured) | ☐ |
| Email contains **Accept invitation** button and `app.mymultiverse.ammo://invite?token=…` link | ☐ |
| Outbox row gets `processed_at` without manual function invoke | ☐ |
| B sees pending invite on **Home onboarding** (existing flow) | ☐ |
| Tap email link opens app with invite token (after client deep-link branch merges) | ☐ N/A |
| Push notification includes `invite_token` (optional until FCM/APNs wired) | ☐ N/A |

**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

## Home onboarding v14 (PR #12)

See [`qa-signoff-v14-home-onboarding.md`](qa-signoff-v14-home-onboarding.md) for rename, admin role, and onboarding layout cases.

| Case id | Pass |
|---------|------|
| `home-onboarding-create-household` | ☐ |
| `home-rename-household` | ☐ |
| `household-admin-role` | ☐ |
| `household-onboarding-create-with-invite` | ☐ |

---

## Regression spot-checks (recommended)

| Case id | Pass |
|---------|------|
| `household-invite-two-phones` | ☐ |
| `nutrition-viewer-read-only` | ☐ |
| `household-transfer-ownership` (unit-tested; manual optional) | ☐ |

---

## Sign-off

| Role | Name | Date |
|------|------|------|
| QA / tester | | |
| Engineering | | |

**Notes:**

---

## CI secrets (repository settings)

| Secret | Purpose |
|--------|---------|
| `SUPABASE_TEST_EMAIL` | Dedicated smoke-test user (not production personal account) |
| `SUPABASE_TEST_PASSWORD` | Password for smoke-test user |

When both are set, **Supabase Migrations** job in KMP CI runs the authenticated round-trip in `verify-supabase-household.sh`.
