# Household collaboration P2 — implementation spec

**Merged:** PR #8 (`feature/household-collaboration-p2`) · **Closeout:** PR #9 (`feature/p2-closeout`)  
**Depends on:** v1 shipped on `main` (PR #7)

This document locks scope for P2 tracks **A–D**. Adventures/Budget modules remain **out of P2** (separate product track).

**Production closeout plan:** [`household-collaboration-p2-closeout.md`](household-collaboration-p2-closeout.md)

---

## Track A — Push / email invite notifications

| Item | Decision |
|------|----------|
| **Trigger** | After successful `invite_household_member` RPC, enqueue row in `household_notification_outbox` |
| **Delivery** | `AFTER INSERT` trigger on `household_notification_outbox` invokes Edge Function `notify-household-invite` via `pg_net` |
| **Email** | Transactional email to invitee address via `RESEND_API_KEY` (or log-only when unset) |
| **Push** | FCM/APNs to tokens in `user_device_tokens` when invitee already has an account |
| **Deep link** | `app.mymultiverse.kmp://auth/callback` → gate shows pending invite (existing flow) |
| **v1 unchanged** | Invitee still sees invite on gate without notification |

**Client:** `PushNotificationRegistrar` (expect/actual) registers token after auth; `register_device_token` RPC upserts token.

---

## Track B — GDPR account deletion + export UX

| Item | Decision |
|------|----------|
| **Export** | Existing `export_my_personal_data` RPC; **share/save** JSON via platform sheet (`PersonalDataExporter`) |
| **Deletion prep** | `prepare_account_deletion()` RPC: leave/dissolve household, revoke tokens, anonymize `profiles` |
| **Auth removal** | Edge Function `delete-account` (service role) deletes `auth.users` after prep |
| **Owner with members** | Block with `owner_must_transfer_or_dissolve` (same as leave) |
| **Sole owner alone** | `dissolve_household()` then delete |
| **Legal copy** | Confirm dialog on Home; external privacy policy update remains legal review |

---

## Track C — Child / household dependants

| Item | Decision |
|------|----------|
| **Shared email login** | **Deferred** — still one auth account per email for sign-in |
| **Child profiles** | **Household dependants**: display name only, no login, no email |
| **Who manages** | Owner / editor can add and remove dependants |
| **Member limit** | Active dependants count toward max **20** household people |
| **UI** | Members screen: “Add dependant” dialog; list shows dependant badge |
| **Model** | `HouseholdMemberKind.Dependant`; `household_dependants` table |

---

## Track D — Polish

| Item | Decision |
|------|----------|
| **Instrumented tests** | Viewer read-only grocery (no input bar, banner visible); transfer ownership covered by unit tests (flaky instrumented test removed) |
| **Firebase QA** | New cases: `home-export-personal-data`, `home-delete-account`, `household-add-dependant`, `household-invite-notification` (manual push/email) |

---

## Implementation order

1. **D** — instrumented tests (no backend)
2. **B** — migration + edge function + Home UI + export share
3. **A** — outbox + edge function + device tokens + registrar
4. **C** — dependants table/RPCs + members UI

---

## Definition of done (P2 A–D)

- [x] Migrations applied; edge functions deploy + secrets documented ([`supabase-deploy.yml`](../.github/workflows/supabase-deploy.yml), [README](../README.md))
- [x] All new strings in 8 locales; parity tests green
- [x] Unit tests for new screen-model / repository paths (export, delete, dependants)
- [x] Instrumented tests for D (viewer read-only)
- [x] `firebase-appdistribution-testcases.yaml` updated
- [x] `./gradlew :composeApp:testDebugUnitTest` green
- [ ] Staging manual QA sign-off ([`p2-staging-qa-checklist.md`](p2-staging-qa-checklist.md))
- [ ] Legal review of external privacy/deletion copy
