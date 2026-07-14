# Ammò (MyMultiverse)

Kotlin Multiplatform **family logistics** app (`app.mymultiverse.ammo`): shared grocery lists, weekly meal plans, and a local AI nutrition assistant. Built with **Compose Multiplatform**, custom **AppNavigator** routing, **Koin** DI, and **Supabase** (Auth, Postgres, Realtime).

Android and iOS share one UI and domain layer; platform code is limited to `androidMain` / `iosMain` bindings.

**Audience for this doc:** engineers, QA testers, release owners. For household product rules in depth, see [`docs/household-collaboration.md`](docs/household-collaboration.md).

---

## Product overview (functional)

MyMultiverse helps a **household** (family or roommates) coordinate **weekly meals and groceries** together. The shipped product is the **Nutrition** module: shared grocery lists, meal plans, and a local AI nutrition assistant.

| Concept | What it means for users |
|---------|-------------------------|
| **Account** | One person, one email (Supabase Auth). Sign up / sign in with email, Google, or Apple. |
| **Household** | One shared household per user at a time. Nutrition data and membership are scoped to that household. |
| **Home onboarding** | When signed in without a household, **Home** shows invites + create-household (no separate gate screen). Topic cards appear only after a household is active. |
| **Invite** | Owner sends invite by email; invitee must **accept** on their device. No silent add. |
| **Roles** | **Owner** — transfer/dissolve, promote admin, full control. **Household admin** — invite (editor/viewer), manage members; cannot transfer/dissolve or promote admin. **Editor** — edit shared nutrition. **Viewer** — read-only everywhere. |
| **Nutrition** | Shared grocery list, weekly meal plan, and AI adviser per calendar week. AI output is **read-only**; user lists are editable (unless viewer). |
| **Sync** | Grocery and meal plan are **offline-first**: edits save locally, then push to Supabase; other members see changes via pull + Realtime. |
| **GDPR** | Export personal data from Home (share sheet on Android/iOS); delete account from Home (`delete-account` edge function). Leave household revokes shared access. Legal review of external privacy copy remains outside engineering. |

Supported UI languages: English, French, Spanish, German, Italian, Arabic (incl. Saudi), Neapolitan.

---

## User journeys & screens

### High-level flow

```
                    ┌─────────────┐
                    │   Login     │  email / Google / Apple OAuth
                    └──────┬──────┘
                           │ authenticated
                           ▼
              ┌────────────────────────┐
              │  Home                  │
              │  • onboarding (no      │
              │    household): invites │
              │    + create household  │
              │  • welcome: greeting,  │
              │    This week + CTA,   │
              │    household chip     │
              └─────┬──────────┬───────┘
                    │          │
         Nutrition  │          │  Household members
                    ▼          ▼
         ┌──────────────┐   ┌──────────────────┐
         │ Nutrition hub│   │ Members screen   │
         │ grocery      │   │ invite / leave / │
         │ meal plan    │   │ transfer / admin │
         │ AI adviser   │   └──────────────────┘
         └──────────────┘
```

### Screen map (UI)

| Screen | Package | Purpose | Key `testTag`s |
|--------|---------|---------|----------------|
| **Login** | `presentation/screens/auth/` | Sign up, sign in, OAuth; contextual sign-in vs sign-up subtitle | — |
| **Home** | `presentation/screens/home/` | Onboarding (no household) or welcome hub; rename chip; topic cards | `home_nutrition_card`, `home_household_card`, `home_onboarding_create_name`, `home_household_name_edit` |
| **Household members** | `presentation/screens/household/` | List members, pending invites, add by email, admin role, leave / transfer / dissolve; **profile photos** per member (`MemberAvatar`) | `household_members_add_person`, `household_members_row_*` |
| **Nutrition hub** | `presentation/screens/nutrition/` | Week context, sync banner, cards for grocery / meal plan / AI | `nutrition_hub_grocery`, `nutrition_hub_meal_plan`, `nutrition_hub_ai` |
| **Grocery** | `presentation/screens/nutrition/` | Editable list + read-only AI suggestions | — |
| **Weekly meal plan** | `presentation/screens/nutrition/` | Lunch/dinner per day; per-meal AI grocery | — |
| **AI adviser** | `presentation/screens/nutrition/` | Modes: Advice, Grocery, Meal plan (local assistant) | — |

Navigation routes live in `presentation/navigation/AppRoute.kt`. Root composition: `presentation/App.kt` (auth → Home with onboarding/welcome phases → main stack).

### Household rules (QA cheat sheet)

| Rule | Expected behaviour |
|------|-------------------|
| One household per user | Cannot be in two households; invite to someone already in another household fails |
| Max 20 members | Invite/accept errors at cap (see automated tests) |
| Invite email = auth email | Invitee must sign in with the **exact** invited address (`lower(trim)`); mismatch shows dedicated error |
| Viewer | Can open Nutrition and **see** all data; **no** add/edit/delete/clear/AI write |
| Owner with other members | Must **transfer ownership** before leaving; cannot plain-leave as sole owner |
| Sole owner, no members | **Delete household** (hard delete), not leave |
| Pending invite + create | Both visible on **Home onboarding**; create is **not** blocked (invites shown above) |
| Unique household name | Names are globally unique (Unicode-aware); create/rename blocked when taken |
| Rename household | Owner or household admin can rename from Welcome banner chip |
| Admin role | Owner promotes editor → household admin; admin manages members but cannot transfer/dissolve |
| Switch household | Accepting invite while already affiliated prompts leave-then-accept |

Full spec: [`docs/household-collaboration.md`](docs/household-collaboration.md).

---

## Nutrition module (functional)

| Area | Behaviour |
|------|-----------|
| **Grocery** | Add, check off, edit, delete items; duplicate detection; clear checked + undo; week-scoped |
| **Meal plan** | Seven days, lunch/dinner; today highlighted; expand/collapse days |
| **AI adviser** | On-device assistant: advice text, full-week or today grocery, meal-plan preview + apply |
| **AI vs editable** | AI grocery/meal suggestions appear in a **read-only** section; never mixed into editable CRUD |
| **Sync status** | Hub banner: idle, syncing, pending outbox, offline |
| **Personal mode** | Without Supabase config, nutrition works **local-only** (no household) |

When a household is active, opening Nutrition activates sync for that `household_id` via `NutritionSessionCoordinator` (flush outbox → pull week → subscribe to Realtime).

---

## Architecture (technical)

Clean architecture under `composeApp/src/commonMain/kotlin/app/mymultiverse/ammo/`:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **Domain** | `domain/` | Models, repository interfaces, use cases, pure helpers (`WeekCalendar`, `NutritionAiPlanner`) |
| **Data** | `data/` | Local store, remote API, sync engine, Supabase repositories |
| **Presentation** | `presentation/` | Compose UI, navigation, `*ScreenModel`, theme, DI |

**Dependency rule:** domain never depends on Compose, Android, iOS, or data implementations. Presentation talks to **domain ports** only (`NutritionSessionCoordinator`, `AuthRepository`, `HouseholdRepository`, etc.); Koin wires implementations in `presentation/di/AppModule.kt`.

### Data layer map

| Package | Role |
|---------|------|
| `data/local/` | Device cache (`NutritionLocalStore`, sync outbox in `Settings`) |
| `data/remote/` | Supabase PostgREST (`NutritionRemoteApi`) |
| `data/sync/` | Offline-first orchestration (`NutritionSyncEngine`, `OfflineFirstNutritionRepository`, Realtime) |
| `data/supabase/` | Auth, household, invites, profiles |

### Offline-first nutrition sync

When a household is active:

1. **Write locally first** — grocery / meal plan changes land on device immediately.
2. **Push** — `NutritionSyncEngine` upserts to `nutrition_household_week_data`; failures enqueue to a durable outbox.
3. **Pull** — on household activation, pending pushes flush then remote week data is fetched.
4. **Realtime** — other members’ edits stream in; own `updated_by` echoes are ignored.

Personal nutrition without a configured backend stays local-only via `NutritionRepositoryImpl`.

---

## Backend (Supabase)

The mobile app talks to **Supabase Auth** (sessions, OAuth) and **PostgREST** (tables + RPCs). Row Level Security (RLS) enforces household membership and viewer read-only rules server-side.

| Concern | Implementation |
|---------|----------------|
| **Auth** | Email/password, Google, Apple; redirect `app.mymultiverse.ammo://auth/callback` |
| **Profiles** | `profiles` row per `auth.users`; bootstrap via `ensure_current_profile()` |
| **Household lifecycle** | RPCs: `create_household`, `rename_household`, `check_household_name_available`, `leave_household`, `dissolve_household`, `transfer_household_ownership` |
| **Invites** | `invite_household_member`, `accept_household_invite`, `list_my_pending_household_invites` |
| **Membership query** | `household_membership_status`, `resolve_user_household_row` |
| **Nutrition data** | `nutrition_household_week_data` — one row per `(household_id, week_key, data_kind)` |
| **GDPR** | `export_my_personal_data`, `prepare_account_deletion`, Edge Function `delete-account` |
| **Invite notifications** | `household_notification_outbox` + Edge Function `notify-household-invite` (email via Resend) |
| **Realtime** | `nutrition_household_week_data` in `supabase_realtime` publication |
| **Storage** | `member-avatars` bucket (public read) for family member profile photos; RLS on `storage.objects` in migration `20250703000000` |

Migrations: `supabase/migrations/` (applied in filename order). **Storage buckets** are declared in `supabase/config.toml` (`[storage.buckets.*]`) and synced via `supabase seed buckets --yes` (local/CI) or `supabase seed buckets --linked --yes` (deploy) — do not `INSERT` into `storage.buckets` in SQL migrations (schema varies across CLI versions). Edge functions: `supabase/functions/`. Deploy on `main` via [`.github/workflows/supabase-deploy.yml`](.github/workflows/supabase-deploy.yml) (`db push` + `seed buckets` + `functions deploy`).

Apply locally or to a linked project:

```bash
./scripts/apply-supabase-migrations.sh
```

Verify household RPCs after deploy:

```bash
./scripts/verify-supabase-household.sh
```

Verify P2 edge functions after deploy:

```bash
./scripts/verify-supabase-edge-functions.sh
```

Verify outbox auto-dispatch is wired (remote project + `psql`):

```bash
./scripts/verify-household-notification-delivery.sh
```

After migrations deploy, CI runs `scripts/configure-household-notification-delivery.sh` to store the project URL and anon key in `private.household_notification_delivery_config` (used by the `pg_net` trigger).

### Edge functions (P2)

| Function | Trigger | Purpose |
|----------|---------|---------|
| `notify-household-invite` | **Automatic:** `AFTER INSERT` trigger on `household_notification_outbox` (`pg_net` → edge function) | Process outbox rows; send branded invite email with `app.mymultiverse.ammo://invite?token=…` when `RESEND_API_KEY` is set |
| `delete-account` | App Home → delete account | `prepare_account_deletion` RPC then `auth.admin.deleteUser` (service role) |

**GitHub repository secrets** (for [`supabase-deploy.yml`](.github/workflows/supabase-deploy.yml)):

| Secret | Used by | Required |
|--------|---------|----------|
| `SUPABASE_ACCESS_TOKEN` | Migrations + functions deploy | Yes |
| `SUPABASE_DB_PASSWORD` | `db push` / link | Yes |
| `SUPABASE_PROJECT_REF` | Link + deploy | Yes |
| `SUPABASE_URL` | Smoke probe | Functions job |
| `SUPABASE_ANON_KEY` | Smoke probe; auto-injected in function runtime | Functions job |
| `SUPABASE_SERVICE_ROLE_KEY` | Edge function secrets (`delete-account`, outbox admin) | Yes for functions job |
| `RESEND_API_KEY` | Invite emails via Resend | Optional (log-only when unset) |
| `INVITE_FROM_EMAIL` | Resend `from` address | Optional (defaults to `invites@mymultiverse.app`) |
| `FCM_SERVICE_ACCOUNT_JSON` | FCM HTTP v1 push from `notify-household-invite` (Firebase service account JSON) | Optional (Android push skipped when unset) |
| `APNS_KEY_ID` / `APNS_TEAM_ID` / `APNS_PRIVATE_KEY` | APNs HTTP/2 push for iOS device tokens (`.p8` key contents) | Optional (iOS push skipped when unset) |
| `APNS_BUNDLE_ID` | APNs topic (defaults to `app.mymultiverse.ammo`) | Optional |
| `APNS_USE_SANDBOX` | `true` for debug/simulator tokens, `false` for TestFlight/App Store | Optional (defaults to `true`) |
| `INVITE_OPEN_LINK_BASE` | HTTPS base for invite email CTAs (defaults to `https://mymultiverse.app/invite`) | Optional |

**Android push (client):** `app.mymultiverse.ammo` is registered in Firebase (`1:37917280954:android:a0c28d6a257baf50f91083`). Local builds use `androidApp/google-services.json`; CI uses the `GOOGLE_SERVICES_JSON` GitHub secret (ammo-only JSON).

Re-fetch config anytime:

```bash
./scripts/setup-firebase-ammo-android.sh   # requires: firebase login
```

When present, the app registers a real FCM token on Home refresh and requests `POST_NOTIFICATIONS` on API 33+.

**iOS Firebase (Crashlytics / messaging prep):** copy `iosApp/iosApp/GoogleService-Info.plist.example` → `GoogleService-Info.plist`, or run:

```bash
./scripts/setup-firebase-ammo-ios.sh
```

Crashlytics on iOS still uses `NoOpCrashReporter` until Firebase iOS SDK is linked in Xcode.

Set function secrets manually (one-off or when not using CI):

```bash
supabase link --project-ref "$SUPABASE_PROJECT_REF"
supabase secrets set \
  SUPABASE_SERVICE_ROLE_KEY="$SUPABASE_SERVICE_ROLE_KEY" \
  RESEND_API_KEY="$RESEND_API_KEY" \
  INVITE_FROM_EMAIL="invites@mymultiverse.app" \
  FCM_SERVICE_ACCOUNT_JSON="$FCM_SERVICE_ACCOUNT_JSON" \
  APNS_KEY_ID="$APNS_KEY_ID" \
  APNS_TEAM_ID="$APNS_TEAM_ID" \
  APNS_PRIVATE_KEY="$APNS_PRIVATE_KEY" \
  APNS_BUNDLE_ID="app.mymultiverse.ammo" \
  APNS_USE_SANDBOX="true" \
  INVITE_OPEN_LINK_BASE="https://mymultiverse.app/invite"
supabase functions deploy notify-household-invite --project-ref "$SUPABASE_PROJECT_REF"
supabase functions deploy delete-account --project-ref "$SUPABASE_PROJECT_REF"
```

### Supabase Auth dashboard

| Field | Value |
|-------|--------|
| **Custom domain** | `ammo.mymultiverse.app` (CNAME → `ivjdzreazvkrrirecznk.supabase.co`; apex `mymultiverse.app` stays on Firebase Hosting) |
| **Site URL** | `https://mymultiverse.app` (plain URL only — no markdown) |
| **Redirect URLs** | `app.mymultiverse.ammo://auth`, `app.mymultiverse.ammo://auth/callback` |

Apply via Management API (uses `supabase login` token or `SUPABASE_ACCESS_TOKEN`):

```bash
./scripts/configure-supabase-auth-redirects.sh
```

Enable Google/Apple providers for OAuth. Add provider redirect URI `https://ammo.mymultiverse.app/auth/v1/callback` (Google Cloud + Apple Sign in with Apple). Ensure **Realtime** is enabled (Database → Replication).

### App Links / Universal Links (`mymultiverse.app`)

**Website repo:** [Rob971/mymultiverse-website](https://github.com/Rob971/mymultiverse-website) — company pages, `/privacy`, `/terms`, `/invite`, and `/.well-known/*` on Firebase Hosting.

**DNS:** `mymultiverse.app` must point to **Firebase Hosting** (project `mymultiverseapp`), not Squarespace. Step-by-step: [`docs/app-links-custom-dns.md`](docs/app-links-custom-dns.md). Quick check: `./scripts/check-app-links-dns.sh`.

1. Firebase Console → [Hosting](https://console.firebase.google.com/project/mymultiverseapp/hosting) → **Add custom domain** `mymultiverse.app` → update DNS at [Squarespace Domains](https://domains.squarespace.com).

1. Build or download the APK you distribute (Firebase release uses **debug** signing).
2. `chmod +x scripts/print-android-apk-fingerprint.sh scripts/check-app-links-dns.sh`
3. `./scripts/check-app-links-dns.sh` — confirms apex is off Squarespace before you verify hosting.
4. **Deploy site:** [mymultiverse-website](https://github.com/Rob971/mymultiverse-website) → Actions → **Deploy hosting** (set secrets `FIREBASE_SERVICE_ACCOUNT_JSON`, `ANDROID_SHA256_FINGERPRINT`). Paste the service account JSON as **one minified line** (see website repo README). Or locally in that repo: `./scripts/deploy.sh`.
5. Fingerprint for hosting (must match **app.mymultiverse.ammo** debug/release APK): `ANDROID_SHA256_FINGERPRINT="$(./scripts/print-android-apk-fingerprint.sh androidApp/build/outputs/apk/debug/androidApp-debug.apk)"`
6. Verify in website repo: `./scripts/verify-hosting.sh` — HTTP 200 for `assetlinks.json` with `"package_name": "app.mymultiverse.ammo"`, AASA, `/invite`, homepage, and `/privacy/`.

Local readiness check (source, Firebase, Supabase domain, auth redirects, live assetlinks):

```bash
./scripts/verify-ammo-release-readiness.sh
```

Release CI prints `ANDROID_SHA256_FINGERPRINT` for copying into the website repo secret when the signing cert changes.

---

## Data model (Postgres)

Current table and RPC names (post-migrations `20250618170000`, `20250620000000`).

### Enums

| Type | Values |
|------|--------|
| `app_topic` | `nutrition`, `adventures`, `budget` |
| `nutrition_feature` | `grocery`, `meal_plan`, `ai_advice` (enabled modules per household) |
| `nutrition_data_kind` | `grocery`, `ai_grocery`, `meal_plan` (week payload rows) |
| `household_member_role` | `owner`, `admin`, `editor`, `viewer` |
| `group_lifecycle` | `persistent`, `event` |

### Core tables

| Table | Purpose |
|-------|---------|
| `profiles` | App user profile (`id` = `auth.users.id`, `display_name`, `email`, …) |
| `households` | Shared household container (`name`, `owner_id`, `topic`; nutrition households use `topic = nutrition`) |
| `household_members` | Membership: `user_id` **or** `group_id`, `role`, `left_at` (soft leave) |
| `household_invites` | Pending invites by normalized email; 14-day `expires_at`; `accepted_at` / `declined_at` |
| `household_modules` | Enabled nutrition features per household (`grocery`, `meal_plan`, `ai_advice`) |
| `nutrition_household_week_data` | Serialized week payloads: `payload` text, `updated_at`, `updated_by` |
| `contact_groups` | Reusable person groups (persistent or event-scoped) |
| `group_members` | Users in a contact group |

**Constraints (product):**

- At most **one active** `household_members` row per `user_id` (`left_at IS NULL`).
- At most **20** active members per household (enforced in invite/accept RPCs).
- **Viewers:** RLS on `nutrition_household_week_data` allows `SELECT` only; writes require owner/editor (`household_member_can_write_nutrition`).
- **Dissolve:** deleting a `households` row cascades to members, invites, modules, and week data.

### Key RPCs (callable by authenticated clients)

| RPC | Purpose |
|-----|---------|
| `ensure_current_profile()` | Ensure `profiles` row exists for session user |
| `household_membership_status()` | Gate: affiliated or not, role, household name |
| `create_household(p_name)` | Create household + owner membership + default modules; rejects duplicate names |
| `rename_household(p_household_id, p_name)` | Owner or admin renames household |
| `check_household_name_available(p_name, p_exclude_household_id)` | Availability probe for create/rename UI |
| `ensure_household()` | Idempotent household ensure (used by client bootstrap) |
| `invite_household_member(p_email, p_role)` | Owner sends invite; guards for cap, duplicate, other household |
| `list_my_pending_household_invites()` | Invites matching session email only |
| `accept_household_invite(p_invite_id)` | Join household; email match required |
| `leave_household()` | Member leaves (not sole owner with others) |
| `dissolve_household()` | Owner hard-deletes household when alone |
| `transfer_household_ownership(p_new_owner_user_id)` | Sole owner picks new owner |
| `export_my_personal_data()` | GDPR JSON export |
| `find_profile_id_by_email(p_email)` | Lookup for collaboration flows |

### Entity relationship (simplified)

```
auth.users ──1:1── profiles
profiles ──owns──► households ◄── household_members ──► profiles (or contact_groups)
households ──1:N── household_invites
households ──1:N── household_modules (nutrition features)
households ──1:N── nutrition_household_week_data (per week_key + data_kind)
contact_groups ──1:N── group_members ──► profiles
```

---

## QA & manual testing

**Primary checklist:** [`firebase-appdistribution-testcases.yaml`](firebase-appdistribution-testcases.yaml) (versioned; included in Firebase release notes).

**P2 staging sign-off:** [`docs/p2-staging-qa-checklist.md`](docs/p2-staging-qa-checklist.md) — export, delete account, dependant, invite notification (two devices).

**Home onboarding sign-off (v14):** [`docs/qa-signoff-v14-home-onboarding.md`](docs/qa-signoff-v14-home-onboarding.md) — onboarding, rename, admin role; maps Firebase cases to automated coverage.

### Recommended test setup

| Need | Detail |
|------|--------|
| **Two accounts / two phones** | Household invite, shared grocery sync, transfer ownership, email mismatch |
| **Supabase configured** | `local.properties` or CI build with `SUPABASE_URL` + `SUPABASE_ANON_KEY` |
| **Roles** | Test owner, editor, and viewer on separate accounts |
| **Viewer pass** | `nutrition-viewer-read-only` — no write controls on grocery, meal plan, AI |
| **Crashlytics** | Tester APK built with `google-services.json` |

### Test categories in YAML

- **Auth** — email sign-in/up (contextual subtitles), OAuth redirect, sign out
- **Home onboarding** — create household, pending invite layout, accept on Home, unique name validation
- **Home welcome** — rename household (owner/admin), topic cards after household active
- **Members / admin** — promote to household admin, admin member management limits
- **Members** — invite from Home, transfer ownership + leave, max members note, **member profile photos** (`household-member-avatar`)
- **Nutrition** — hub navigation, grocery CRUD, meal plan, AI modes, AI read-only sections
- **Sync** — status banner, two-phone shared grocery, Supabase persistence smoke
- **GDPR** — export personal data from Home
- **i18n** — language picker smoke

### Automated tests

```bash
# Unit tests (domain, screen models, i18n parity, Koin graph)
./gradlew :composeApp:testDebugUnitTest

# Instrumented UI tests (Android emulator required)
./gradlew :androidApp:connectedDebugAndroidTest
```

CI runs unit tests on every push; instrumented + iOS on PR / `main`.

---

## Prerequisites

- **JDK 17**
- **Android Studio** (latest stable) for Android
- **Xcode** (macOS) for iOS simulator / device builds
- A **Supabase** project with migrations applied (see below)

---

## Local setup

### 1. Clone and configure Supabase

```bash
cp local.properties.example local.properties
```

Edit `local.properties`:

```properties
supabase.url=https://ammo.mymultiverse.app
supabase.anonKey=your_supabase_anon_or_publishable_key_here
```

Gradle task `generateSupabaseSecrets` embeds values into `commonMain` at compile time (never commit `local.properties`). Without keys, auth and sharing show “not configured”; local-only nutrition still works.

**Local secrets (never commit):**

| Local file | Template | Purpose |
|------------|----------|---------|
| `local.properties` | `local.properties.example` | Supabase URL + anon key |
| `androidApp/google-services.json` | `androidApp/google-services.json.example` | Firebase (Crashlytics + App Distribution) |

CI uses GitHub Secrets `SUPABASE_URL` (`https://ammo.mymultiverse.app`), `SUPABASE_ANON_KEY`, and `GOOGLE_SERVICES_JSON`. Optional `SUPABASE_TEST_EMAIL` + `SUPABASE_TEST_PASSWORD` enable authenticated Supabase smoke tests in CI (`verify-supabase-household.sh`).

Production migration deploy requires `SUPABASE_ACCESS_TOKEN`, `SUPABASE_DB_PASSWORD`, `SUPABASE_PROJECT_REF`.

### 2. Apply database migrations

All SQL files in `supabase/migrations/` (apply in timestamp order). Storage buckets are defined in `supabase/config.toml` and seeded after push. Prefer:

```bash
./scripts/apply-supabase-migrations.sh
```

The script runs `supabase db push` then `supabase seed buckets --linked --yes`.

Or Supabase Dashboard SQL editor for ad-hoc applies. Remote field QA needs migrations through at least `20250618170000` (`households` rename) and `20250618180000` (grants).

### 3. Run the app

**Android**

```bash
./gradlew :androidApp:assembleDebug
```

**iOS** (macOS)

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Open `iosApp/` in Xcode and run on a simulator.

---

## CI/CD

GitHub Actions: [`.github/workflows/kmp-ci.yml`](.github/workflows/kmp-ci.yml)

| Trigger | Jobs |
|---------|------|
| **PR** to `main` | Android CI + Supabase Migrations + instrumented (merge gate; no Firebase Release) |
| **Push** to `main` | Same as PR (tests only; no automatic Firebase or version bump) |
| **Manual dispatch** | `all`, `android-ci`, `android-instrumented-tests`, `supabase-migrations`, `release`, `play-store-bundle` (iOS disabled) |

`chore(version): … [skip ci]` pushes skip heavy jobs via the CI gate. Feature branches validate through a PR only (one run per push).

**Supabase deploy** ([`supabase-deploy.yml`](.github/workflows/supabase-deploy.yml)): `db push`, `supabase seed buckets --linked --yes`, and P2 edge functions deploy on `main` when `supabase/migrations/**`, `supabase/config.toml`, or `supabase/functions/**` change; also `workflow_dispatch`.

Firebase App Distribution runs only via **manual dispatch** (`release` or `all`).

**Google Play (signed AAB):** manual dispatch → `play-store-bundle` builds `androidApp-release.aab` and uploads it as a workflow artifact. Requires GitHub secrets `AMMO_UPLOAD_KEYSTORE_BASE64` (base64 `.jks`), `AMMO_UPLOAD_STORE_PASSWORD`, `AMMO_UPLOAD_KEY_ALIAS`, `AMMO_UPLOAD_KEY_PASSWORD`. Local builds: copy [`keystore.properties.example`](keystore.properties.example) → `keystore.properties`, place `ammo-upload.jks` at repo root, then `./gradlew :androidApp:bundleRelease`. After switching from debug to release signing, update `ANDROID_SHA256_FINGERPRINT` in [mymultiverse-website](https://github.com/Rob971/mymultiverse-website) using `./scripts/print-android-keystore-fingerprint.sh`.

---

## App versioning

Canonical version: [`gradle/app-version.properties`](gradle/app-version.properties). Full bump policy: [`.cursor/rules/version-management.mdc`](.cursor/rules/version-management.mdc).

| Field | Purpose |
|-------|---------|
| `version.name` | SemVer user-facing version (e.g. `1.4.0`) — `MAJOR.MINOR.PATCH` |
| `version.code` | Monotonic build number (Android `versionCode`, iOS `CFBundleVersion`) |
| `version.prerelease` | Optional suffix (e.g. `beta.1` → `1.4.0-beta.1`; CI-stamped on beta track, not committed) |

**SemVer bumps (named release only):**

| Bump | Use when |
|------|----------|
| `patch` | Bug fixes, hotfixes, security — no new user-facing features |
| `minor` | New backward-compatible features |
| `major` | Breaking data model, auth, or incompatible migrations |
| `none` | Rebuild only — increments `version.code`, not `version.name` |

**Release (`workflow_dispatch` → `release`):**

1. Choose **version bump**: `major`, `minor`, `patch`, or `none`.
2. CI bumps version, builds debug APK, distributes to Firebase, tags `vX.Y.Z`, commits `chore(version): release … [skip ci]`.

Merges to `main` do **not** change `version.name` automatically. Alpha builds (`distribute-alpha` on `main` push) reuse the current `version.name`.

---

## Project layout

```
androidApp/           Android application (MainActivity, manifest, signing, UI tests)
composeApp/           KMP shared library + iOS framework
  src/commonMain/     Shared UI, domain, data, resources (8 locales)
  src/androidMain/    Android platform actuals (push, FCM, deeplinks)
  src/iosMain/        iOS entry, Koin platform module
  src/commonTest/     Unit tests
androidApp/src/androidInstrumentedTest/  Compose UI instrumented tests
docs/                 Product specs (household-collaboration.md, household-collaboration-p2.md, p2-staging-qa-checklist.md, qa-signoff-v14-home-onboarding.md)
supabase/migrations/  Postgres schema + RLS + Realtime + RPCs
.github/workflows/    CI pipelines
firebase-appdistribution-testcases.yaml  Manual QA checklist
```

---

## Roadmap (post-P2)

**Shipped on `main` (P2, PR #8 + [`feature/p2-closeout`](docs/household-collaboration-p2-closeout.md) / PR #9):** push/email invite notifications, household dependants (display-only), GDPR account deletion + export share, outbox automation, edge function deploy pipeline.

**Shipped on `main` (PR #12):** unified Home onboarding (gate merged into Home), globally unique household names, rename from Welcome, household admin role, login subtitle polish.

**Still open:**

| Track | Items |
|-------|--------|
| **Ops / QA** | Optional `SUPABASE_TEST_EMAIL` / `SUPABASE_TEST_PASSWORD` for CI auth round-trip; staging sign-off ([`docs/p2-staging-qa-checklist.md`](docs/p2-staging-qa-checklist.md), [`docs/qa-signoff-v14-home-onboarding.md`](docs/qa-signoff-v14-home-onboarding.md)) |
| **Product** | Shared-email child login accounts (explicitly deferred); post-traction modules beyond nutrition (deferred) |
| **Legal** | External privacy policy wording review |

---

## Agent / contributor notes

See [`AGENTS.md`](AGENTS.md) and [`.cursor/rules/`](.cursor/rules/) for delivery workflow, i18n (all 8 locales for new strings), and layer boundaries.

---

## License & intellectual property

**Proprietary — all rights reserved.** Copyright © 2024–2026 Roberto Cornano (MyMultiverse).

- [`COPYRIGHT`](COPYRIGHT) — legal notice for this repository
- [`docs/IP.md`](docs/IP.md) — asset inventory, ownership, registration checklist
- [`docs/BRAND.md`](docs/BRAND.md) — logo paths and public-site sync
- [`docs/legal/website-legal-deploy.md`](docs/legal/website-legal-deploy.md) — privacy/terms drafts for website repo

Unauthorized copying, distribution, or commercial use without written permission is prohibited.
