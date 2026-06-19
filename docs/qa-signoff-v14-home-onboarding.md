# Home onboarding QA sign-off (Firebase v14)

Run on **staging / production Supabase** with the latest **Firebase App Distribution** tester build from `main`.

**Checklist source:** [`firebase-appdistribution-testcases.yaml`](../firebase-appdistribution-testcases.yaml) (version 14).

**Automated coverage:** instrumented tests in `HomeHouseholdUxInstrumentedTest` and `HouseholdMembersAdminInstrumentedTest`; unit tests in `HomeScreenModelTest`, `HouseholdMembersScreenModelTest`, `HouseholdNameRulesTest`, `HouseholdPermissionRulesTest`.

---

## Prerequisites

| Item | Done |
|------|------|
| PR #12 merged; migrations `20250623100000` + `20250623100001` applied | ☑ |
| Supabase Deploy green on `main` | ☑ |
| Two test accounts (owner + invitee) for two-phone flows | ☐ |
| Tester APK from latest `main` Firebase release | ☐ |

---

## v14 manual cases

### `home-onboarding-create-household` — Topics hidden until household exists

| Step | Pass |
|------|------|
| Sign in with no household | ☐ |
| Home shows onboarding (not full topic grid) | ☐ |
| Create with unique name → Welcome Home with Nutrition enabled | ☐ |
| Adventures / Budget show “Coming soon” | ☐ |

**Automated:** `HomeHouseholdUxInstrumentedTest` (onboarding layout, create button gating).  
**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `home-rename-household` — Rename from Welcome (owner or admin)

| Step | Pass |
|------|------|
| Owner or admin taps household name chip → rename dialog | ☐ |
| Taken name shows unavailable; unique name saves | ☐ |
| Updated name visible on Home and Members subtitle | ☐ |

**Automated:** `HomeHouseholdUxInstrumentedTest.welcome_owner_seesRenameChipAndEditAction`; `HomeScreenModelTest` rename flows.  
**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `household-admin-role` — Owner promotes editor to household admin

| Step | Pass |
|------|------|
| Owner → Members → Change role → Household admin → confirm promotion | ☐ |
| Promoted user can invite editor/viewer; cannot promote admin | ☐ |
| Admin cannot transfer ownership or dissolve | ☐ |

**Automated:** `HouseholdMembersAdminInstrumentedTest`; `HouseholdMembersScreenModelTest.owner_canPromoteMemberToAdmin`.  
**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `home-pending-invite` / `household-gate-pending-invite-two-phone` — Invite on Home onboarding

| Step | Pass |
|------|------|
| Invitee sees pending invite on Home onboarding (above create) | ☐ |
| Accept → Welcome Home + joined snackbar | ☐ |
| Two-phone: both devices show same household name | ☐ |

**Automated:** `HomeScreenModelTest` invite accept paths (unit).  
**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

### `household-onboarding-create-with-invite` — Layout when invite + create both visible

| Step | Pass |
|------|------|
| Pending invite card above create section | ☐ |
| Create still available (not blocked) | ☐ |

**Automated:** `HomeHouseholdUxInstrumentedTest` (partial — no pending invite card fixture yet).  
**Device / build:** _______________ **Tester:** _______________ **Date:** _______________

---

## Regression spot-checks (recommended)

| Case id | Pass |
|---------|------|
| `auth-sign-in` (contextual subtitles) | ☐ |
| `household-invite-email-mismatch` | ☐ |
| `nutrition-viewer-read-only` | ☐ |
| `household-invite-blocked-already-member` | ☐ |

---

## Sign-off

| Role | Name | Date |
|------|------|------|
| QA / tester | | |
| Engineering | | |

**Notes:**

---

## External (outside engineering)

| Item | Owner |
|------|--------|
| Legal review of privacy / deletion copy | Legal / product |
| Optional CI secrets `SUPABASE_TEST_EMAIL` / `SUPABASE_TEST_PASSWORD` | DevOps |
