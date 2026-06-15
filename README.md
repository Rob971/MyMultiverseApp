# MyMultiverse (KMP)

Kotlin Multiplatform family logistics app built with **Compose Multiplatform**, **Voyager** navigation, **Koin** DI, and **Supabase** for auth and collaborative sharing.

Android and iOS share one UI and domain layer; platform code is limited to `androidMain` / `iosMain` bindings.

## Features

| Area | Status |
|------|--------|
| Home hub | Greeting, navigation into topics |
| Nutrition | Grocery list, weekly meal plan, local AI assistant |
| Auth | Email sign-up / sign-in via Supabase (Google & Apple stubbed) |
| Sharing | Nutrition **spaces** with per-space feature toggles |
| Collaboration | Invite people by email, create/add contact groups |
| Sync | Grocery & meal plan stored per `space_id` + week in Supabase |
| Realtime | Live updates when another member edits shared nutrition data |

Supported UI languages: English, French, Spanish, German, Italian, Arabic (incl. Saudi), Neapolitan.

## Architecture

Clean architecture with three Kotlin packages under `composeApp/src/commonMain/kotlin/app/mymultiverse/kmp/`:

| Layer | Package | Responsibility |
|-------|---------|----------------|
| **Domain** | `domain/` | Models, repository interfaces, use cases, pure helpers |
| **Data** | `data/` | Supabase repos, local settings persistence, codecs |
| **Presentation** | `presentation/` | Compose UI, Voyager routes, screen models, theme, DI |

**Dependency rule:** domain never depends on Compose, Android, iOS, or data implementations. Presentation talks to domain contracts; Koin wires implementations.

### Nutrition sharing flow

```
Login → Home → Nutrition → Sharing spaces → Space hub
                              ├── People & groups
                              ├── Grocery (synced)
                              ├── Meal plan (synced)
                              └── AI advice (local assistant)
```

When you open a space hub, the app activates a `SyncingNutritionRepository` for that `space_id`, pulls the current week from PostgREST, and subscribes to Supabase Realtime on `nutrition_space_week_data`. Local edits upsert to Supabase; remote edits from other members update the UI without a manual refresh.

## Prerequisites

- **JDK 17**
- **Android Studio** (latest stable) for Android
- **Xcode** (macOS) for iOS simulator / device builds
- A **Supabase** project with migrations applied (see below)

## Local setup

### 1. Clone and configure Supabase anon key

```bash
cp local.properties.example local.properties
```

Edit `local.properties` and set your publishable/anon key:

```properties
supabase.anonKey=your_supabase_anon_or_publishable_key_here
```

The project URL is committed in `SupabaseConfig.kt`. The Gradle task `generateSupabaseSecrets` embeds the anon key at compile time (never commit `local.properties`).

Without a key, auth and sharing screens show a “not configured” state; local-only nutrition still works.

### 2. Apply database migrations

SQL files live in `supabase/migrations/`. Apply them to your Supabase project (Dashboard SQL editor, Supabase CLI, or MCP):

1. `20250615120000_sharing_spaces_foundation.sql` — profiles, groups, spaces, RLS
2. `20250615120100_revoke_handle_new_user_execute.sql`
3. `20250615130000_nutrition_sync_and_collaboration.sql` — week data sync, email lookup RPC
4. `20250615140000_nutrition_realtime.sql` — enables Realtime on `nutrition_space_week_data`

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
| **Push** to `main`, `master`, or `feature/**` | Unit tests, instrumented tests, debug APK, optional Firebase distribute |
| **Pull request** (any branch) | Unit tests, instrumented tests, debug APK, iOS compile signal |
| **Manual dispatch** | Choose `all`, `android`, `android-instrumented-tests`, `ios`, `firebase`, etc. |

Firebase App Distribution runs on **push** and manual `firebase` targets only—not on pull requests.

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
