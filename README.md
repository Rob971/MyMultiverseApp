# MyMultiverse (KMP)

Kotlin Multiplatform family logistics app built with **Compose Multiplatform**, **Voyager** navigation, **Koin** DI, and **Supabase** for auth and collaborative sharing.

Android and iOS share one UI and domain layer; platform code is limited to `androidMain` / `iosMain` bindings.

## Features

| Area | Status |
|------|--------|
| Home hub | Greeting, navigation into topics |
| Nutrition | Grocery list, weekly meal plan, local AI assistant |
| Auth | Email sign-up / sign-in; Google & Apple OAuth (deeplink `app.mymultiverse.kmp://auth/callback`); sign out from Home |
| Sharing | Nutrition **spaces** with per-space feature toggles |
| Collaboration | Members by email (direct add or pending invite), roles, groups, group member management |
| Sync | Offline-first grocery & meal plan per space (local cache + outbox + Supabase); hub sync status banner |
| Realtime | Live updates when another member edits shared nutrition data |

Supported UI languages: English, French, Spanish, German, Italian, Arabic (incl. Saudi), Neapolitan.

## Architecture

Clean architecture with three Kotlin packages under `composeApp/src/commonMain/kotlin/app/mymultiverse/kmp/`:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **Domain** | `domain/` | Models, repository interfaces, use cases, pure helpers |
| **Data** | `data/` | Local store, remote API, sync engine, Supabase repositories |
| **Presentation** | `presentation/` | Compose UI, Voyager routes, screen models, theme, DI |

**Dependency rule:** domain never depends on Compose, Android, iOS, or data implementations. Presentation talks to **domain ports** only (`NutritionSessionCoordinator`, `AuthRepository`, etc.); Koin wires implementations.

### Mobile frontend vs API (data layer)

| Package | Role |
|---------|------|
| `presentation/` | Compose UI + screen models (no Supabase / Settings imports) |
| `domain/repository/` | Contracts the UI depends on |
| `data/local/` | Device cache (`NutritionLocalStore`, sync outbox) |
| `data/remote/` | Supabase PostgREST (`NutritionRemoteApi`) |
| `data/sync/` | Offline-first orchestration (`NutritionSyncEngine`, `OfflineFirstNutritionRepository`, Realtime) |
| `data/supabase/` | Auth, sharing spaces, collaboration repositories |

### Offline-first nutrition sync

When a sharing space is active:

1. **Write locally first** — grocery / meal plan changes land in `Settings` immediately.
2. **Push** — `NutritionSyncEngine` upserts to Supabase; failures enqueue to a durable outbox.
3. **Pull** — on space activation, pending pushes flush then remote week data is fetched.
4. **Realtime** — other members’ edits stream in; own `updated_by` echoes are ignored.

Personal nutrition (no space) stays local-only via `NutritionRepositoryImpl`.

### Nutrition sharing flow

```
Login → Home → Nutrition → Sharing spaces → Space hub
                              ├── People & groups
                              ├── Grocery (synced)
                              ├── Meal plan (synced)
                              └── AI advice (local assistant)
```

When you open a space (hub, grocery, or meal plan), `NutritionSessionCoordinator` activates an `OfflineFirstNutritionRepository` for that `space_id`, flushes any pending outbox entries, pulls the current week, and subscribes to Supabase Realtime.

## Prerequisites

- **JDK 17**
- **Android Studio** (latest stable) for Android
- **Xcode** (macOS) for iOS simulator / device builds
- A **Supabase** project with migrations applied (see below)

## Local setup

### 1. Clone and configure Supabase

```bash
cp local.properties.example local.properties
```

Edit `local.properties` and set your publishable/anon key (and optionally override the project URL):

```properties
supabase.url=https://your-project.supabase.co
supabase.anonKey=your_supabase_anon_or_publishable_key_here
```

The Gradle task `generateSupabaseSecrets` embeds both values into `commonMain` at compile time for Android and iOS (never commit `local.properties`). If `supabase.url` is omitted, the default production URL from `composeApp/build.gradle.kts` is used.

Without a key, auth and sharing screens show a “not configured” state; local-only nutrition still works.

**Local secrets (never commit)** — copy from the `*.example` templates; see `.gitignore` for the full list.

| Local file | Template | Purpose |
|------------|----------|---------|
| `local.properties` | `local.properties.example` | Supabase URL + anon key (required for auth/sharing) |
| `composeApp/google-services.json` | `composeApp/google-services.json.example` | Firebase Android config (Crashlytics + App Distribution); required for crash reports on tester APKs |

CI builds use GitHub Secrets `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `GOOGLE_SERVICES_JSON` (full contents of `composeApp/google-services.json` for Firebase Crashlytics on tester APKs) instead of local files.

Production migration deploy (`.github/workflows/supabase-deploy.yml`) additionally requires `SUPABASE_ACCESS_TOKEN`, `SUPABASE_DB_PASSWORD`, and `SUPABASE_PROJECT_REF` (e.g. `ivjdzreazvkrrirecznk`).

### 2. Apply database migrations

SQL files live in `supabase/migrations/`. Apply them to your Supabase project (Dashboard SQL editor, Supabase CLI, or MCP):

1. `20250615120000_sharing_spaces_foundation.sql` — profiles, groups, spaces, RLS
2. `20250615120100_revoke_handle_new_user_execute.sql`
3. `20250615130000_nutrition_sync_and_collaboration.sql` — week data sync, email lookup RPC
4. `20250615140000_nutrition_realtime.sql` — enables Realtime on `nutrition_space_week_data`
5. `20250615150000_space_invites_and_group_archival.sql` — pending invites, accept RPC, archive expired event groups

Or with the Supabase CLI linked to your project:

```bash
./scripts/apply-supabase-migrations.sh
```

Configure **Auth → URL configuration** in Supabase Dashboard:

| Field | Value |
|-------|--------|
| **Site URL** | `https://mymultiverse.app` (plain URL only — no markdown) |
| **Redirect URLs** | `app.mymultiverse.kmp://auth/callback` |

Enable Google/Apple providers when using OAuth. A Site URL like `[https://mymultiverse.app](https://mymultiverse.app)` breaks OAuth `/callback` with HTTP 500 `unexpected_failure`.

Ensure **Realtime** is enabled for the project (Database → Replication). The last migration adds the table to the `supabase_realtime` publication.

### 3. Run the app

**Android**

```bash
./gradlew :composeApp:assembleDebug
```

Open the `composeApp` run configuration in Android Studio, or install the APK from `composeApp/build/outputs/apk/debug/`.

**iOS** (macOS)

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

Open `iosApp/` in Xcode and run on a simulator.

## Testing

```bash
# Unit tests (domain, screen models, i18n parity, Koin graph)
./gradlew :composeApp:testDebugUnitTest

# Instrumented UI tests (Android emulator required)
./gradlew :composeApp:connectedDebugAndroidTest
```

Manual QA checklist for Firebase testers: `firebase-appdistribution-testcases.yaml`.

## CI/CD

GitHub Actions workflow: [`.github/workflows/kmp-ci.yml`](.github/workflows/kmp-ci.yml)

| Trigger | Jobs |
|---------|------|
| **Push** to `feature/**` | **Android CI** → **Release** (Firebase + version bump). ~4 min target. |
| **Push** to `main` / `master` | Android CI + Supabase Migrations + instrumented + iOS (parallel) → Release |
| **Pull request** into `main` / `master` | Same as main push (merge gate) |
| **Manual dispatch** (`kmp-ci.yml`) | `all`, `android-ci`, `android-instrumented-tests`, `supabase-migrations`, `ios-compatibility`, `release` |

**Supabase deploy** ([`supabase-deploy.yml`](.github/workflows/supabase-deploy.yml)): `db push` on `main` when migrations change. Requires `SUPABASE_ACCESS_TOKEN`, `SUPABASE_DB_PASSWORD`, `SUPABASE_PROJECT_REF`. CI validates migrations locally with `supabase db start` (no remote writes).

**Manual `release`:** reuses the APK from the latest successful **Android CI** on that branch (optional `source_run_id`).

Firebase App Distribution runs on every **push**, **pull request** (opened / reopened), manual `all`, and manual `release`.

## App versioning

Canonical version: [`gradle/app-version.properties`](gradle/app-version.properties) (read by `composeApp/build.gradle.kts` and synced to iOS `Info.plist`).

| Event | Bump | Example `versionName` |
|-------|------|------------------------|
| Successful **push** to `feature/**` | Candidate +1, `version.code` +1 | `1.0.5` (RC; third segment) |
| Successful **push** to `main` / `master` (merge) | LTS patch +1, candidate reset | `1.0.1` |

CI commits bumps with `chore(version): … [skip ci]` so only user pushes run the full pipeline.

## Project layout

```
composeApp/
  src/commonMain/     Shared UI, domain, data, resources (8 locales)
  src/androidMain/    Android entry, Koin platform module
  src/iosMain/        iOS entry, Koin platform module
  src/commonTest/     Unit tests
  src/androidInstrumentedTest/  UI tests
supabase/migrations/  Postgres schema + RLS + Realtime
.github/workflows/    CI pipelines
```

## Supabase tables (sharing)

| Table | Purpose |
|-------|---------|
| `profiles` | App user profile (linked to `auth.users`) |
| `contact_groups` | Reusable person groups (long-term or event) |
| `sharing_spaces` | Topic container (e.g. nutrition) with owner |
| `space_members` | People or groups granted access to a space |
| `space_nutrition_features` | Enabled features per space (grocery, meal plan, AI) |
| `nutrition_space_week_data` | Serialized grocery / meal plan / AI grocery per week |

Row Level Security restricts reads and writes to space owners and members.

## Agent / contributor notes

See [`AGENTS.md`](AGENTS.md) and [`.cursor/rules/`](.cursor/rules/) for delivery workflow, i18n requirements (all 8 locales for new strings), and layer boundaries.

## License

Private project — all rights reserved unless otherwise noted by the repository owner.
