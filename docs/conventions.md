# Ammò codebase conventions

Reference material about *this* codebase: what exists, what to reuse, what proves it works.

Operating rules for agents live in exactly one place on this machine —
`~/Documents/Cline/Rules`, installed into this repo's `AGENTS.md` together with the repo's facts
block. This file holds no rules. It replaces the 14 `.cursor/rules/*.mdc` files removed on
2026-09-23 (recover the originals with `git log -- .cursor/rules`).

## Product and modules

Household nutrition logistics: a shared grocery list, a weekly meal plan and a local AI assistant,
for one household with roles (owner / admin / editor / viewer). Home is the nutrition hub;
Adventures and Budget are coming-soon placeholders. Visual language: **Napolitan Heart** (terracotta,
teal, parchment). Design-system waves A–C are shipped.

| Module | Contents |
|---|---|
| `:composeApp` | KMP shared library: `commonMain`, `androidMain`, `androidFirebase` (compiled when `google-services.json` exists), `iosMain`, `commonTest` |
| `:androidApp` | Android application: `MainActivity`, manifest, signing, instrumented tests |
| `iosApp/` | Xcode project embedding the Compose framework |
| `supabase/` | migrations, `config.toml`, Deno edge functions |

Base package `app.mymultiverse.ammo`. `AppBuildInfo`, `FirebaseBuildFlags` and `SupabaseSecrets` are
**generated** by Gradle tasks in `composeApp/build.gradle.kts` (package
`app.mymultiverse.ammo.data.observability` for the flags) — add fields in the generator, never by hand.

## Architecture

| Layer | Package | Holds |
|---|---|---|
| Domain | `domain/` | Models, repository interfaces, ports (`NutritionSessionCoordinator`), pure logic (`NutritionAiPlanner`, `WeekCalendar`, `GroceryListPresentation`) |
| Data | `data/` | Repository impls, Supabase client, local store, sync engine, DTOs, codecs |
| Presentation | `presentation/` | Compose UI, `AppNavigator`, `*ScreenModel`, theme, DI |

- Formatting, sorting and partitioning live in domain helpers, not duplicated in composables.
- Screen models collect repository `Flow`s and expose `StateFlow` UI state; they map errors to sealed
  UI state or snackbar events, and never call `stringResource`.
- Repository (`domain/repository/` → `data/repository/`, `data/sync/`) versus service
  (`domain/service/` → `data/service/`, e.g. `NutritionAiAssistantService`, a stateless capability).
- Personal/offline nutrition: `NutritionLocalStore` + codecs. Household nutrition:
  `OfflineFirstNutritionRepository` + `NutritionSyncEngine` (outbox) + Supabase Realtime.
  Week keys scope grocery, meal plan and AI grocery per household week. Server wins on pull; the
  outbox retries on push failure.
- When Supabase is unconfigured, `Unconfigured*` repositories keep the app usable behind a
  configuration gate.
- Navigation: `AppRoute` (`Home`, `HouseholdMembers`, `Nutrition(section, aiMode)`) with `AppNavigator`
  (`navigateTo`, `replaceCurrent`, `navigateBack`, `canGoBack`). `App.kt` is the auth gate plus an
  `AnimatedContent` over routes. Screen models are plain Koin classes, not Voyager `ScreenModel`.
- DI: `presentation/di/AppModule.kt` plus `PlatformModule.kt` per platform, and the Android Firebase
  split (`androidFirebasePlatformModule` / `androidNoFirebasePlatformModule`, chosen by
  `FirebaseBuildFlags.PUSH_ENABLED` in `MainActivity`). Test fakes live in
  `commonTest/.../presentation/di/`.
- `expect`/`actual` only where no multiplatform library exists: haptics, auth deeplinks, language
  manager, personal export, push tokens, avatar image picker.
- Logging through Kermit, with sensitive fields redacted.

New feature shape: domain model and API (plus migration/RLS if backed by Supabase) → data
implementation and sync payloads → screen model and UI → unit tests plus an instrumented tap path →
Firebase QA case if user-visible.

## Design system (Journey)

Required primitives for new UI:

| Component | Use |
|---|---|
| `JourneyPrimaryButton` / `Secondary` / `Tertiary` | Action hierarchy: one primary per section, outlined alternate, text for cancel |
| `JourneyDestructiveOutlinedButton` / `DestructiveTextButton` | Irreversible actions, confirmed in a dialog |
| `JourneyTextField` | All forms: 56dp min, `isError`, `supportingText` |
| `JourneyIconButton` | Icon actions at 48×48dp |
| `JourneyLoadingContent` / `JourneyErrorContent` / `JourneyEmptyState` | Async and empty states |
| `JourneySnackbarHost` | Snackbars, above the grocery bottom bar |
| `JourneyBanner` | Inline notices |

Surfaces and chrome: `FamilyLogisticsCardSurface`, `FamilyLogisticCard`,
`FamilyLogisticsSectionHeader`, `NutritionFeatureHeader`, `NutritionScaffold`, `GroceryInputBar`,
`GroceryItemRow`, `MealPlanDayCard`, `MealPlanEmptyState`, `NapolitanBackground` inside `AppTheme`,
`HouseholdViewerReadOnlyNotice` when `canWriteHouseholdData` is false, `MemberAvatar` (48dp,
tap-to-upload when permitted), `MainTabIconArt` (transparent WebP glyphs; 32dp tab, 48dp hero).

AI output is read-only and visually separate: `AiReadOnlyGroceryList`, `AiGrocerySuggestionsSection`,
terracotta `AiReadOnlyAccent` — never teal, never interleaved with editable rows.

Colour and type: `SharedJourneyColors` with `sharedJourneyLightScheme()` / `sharedJourneyDarkScheme()`;
theme-aware `JourneySemanticColors.inkDeep()`, `inkMuted()`, `cardSurface()`, `elevatedSurface()`;
static brand `TerracottaOrange`, `MediterraneanTeal`, `InkSecondary` for subtitles (not
`InkDeep.copy(alpha = …)`); icons from `AppIcons` only. Typography roles are defined in `AppTheme.kt`
with body line height ≈1.5×.

Layout: `ScreenLayout` tokens (`horizontalPadding` 24dp, `sectionSpacing` 16dp, `expandedMinWidth`
600dp, `expandedSidePanelWidth` 340dp); `Modifier.screenContentArea(padding)` and
`screenListPadding()`; `imePadding()` on scrollable content and input bars; `BoxWithConstraints` for
wide layouts — grocery uses a bottom bar on phones and a side panel at ≥600dp. Touch targets are at
least 48×48dp (`FamilyLogisticsDesign.minTouchTarget`), grocery rows `heightIn(min = 56.dp)`.

States: loading via `JourneyLoadingContent` or an in-button spinner (`MediterraneanTeal`, never both);
errors via `JourneyErrorContent` with retry or a snackbar; empty via `JourneyEmptyState` with a
`testTag`; success via snackbar with undo where it applies. Pull-to-refresh on grocery and meal plan
goes through `NutritionScreenModel.refresh()`. Haptics: `rememberJourneyHapticFeedback()` (Android
light click on grocery toggle; iOS is a no-op until wired).

Accessibility and testability: `contentDescription` on meaningful icons, `null` only for decorative
duplicates; `Sparkles` means AI and nothing else; per-feature `*TestTags` objects, and instrumented
tests address tags rather than display text.

Do not regress (waves A–C): Journey form controls everywhere, Journey loading/error on the home and
nutrition gates, inline validation on login and join, snackbars above the grocery input,
pull-to-refresh, dark theme following the system, wide grocery and meal-plan layouts, green locale
parity tests.

## Internationalisation

Eight locale directories under `composeApp/src/commonMain/composeResources/`: `values` (English
default), `-fr`, `-es`, `-de`, `-it`, `-ar`, `-ar-rSA`, `-nap` (Neapolitan).

- Strings live only in `values*/strings.xml`; composables read them with `stringResource`.
- Resolve strings in composable scope and pass the `String` into `LaunchedEffect` or callbacks.
- Register feature keys in `i18n/*StringKeys.kt` (`NutritionStringKeys`, `HomeStringKeys`,
  `HouseholdStringKeys`, …). These files contain more than one `listOf(...)`, so a scripted edit must
  anchor on a line unique to the target block.
- A new accessor needs its explicit import: `import ammo.composeapp.generated.resources.<key>`.
- Placeholders identical across locales (`%1$d`, `%2$s`, same order); default `values` never blank.
- RTL (`values-ar*`): logical placement, no LTR-only assumptions. `LanguageManager` /
  `SupportedAppLanguages` switch the language live — no cached English.
- Reuse existing helpers (`nutritionDayLabel()`, shared meal-slot keys, `action_back`) and keep the
  same terminology in the app and in `firebase-appdistribution-testcases.yaml`.

## Testing

| Level | Where | Scope |
|---|---|---|
| Unit | `composeApp/src/commonTest` | Domain, screen models, repositories, codecs, locale parity, navigation contracts |
| Instrumented | `androidApp/src/androidInstrumentedTest` | Compose smoke on critical paths, via `testTag` |
| Manual | `firebase-appdistribution-testcases.yaml` | Tester checklist embedded in Firebase release notes (keep its `version` key in step with changes) |

What each change is expected to bring:

| Change | Test work |
|---|---|
| New domain method or pure function | Unit test: happy path plus an edge or error case |
| New repository method | Unit test through a `Fake*`; assert the emitted `Flow` value or return |
| New screen model transition | `runTest` + `TestDispatcher`, `advanceUntilIdle()`, assert the `StateFlow` |
| New codec or mapper | Round-trip domain ↔ DTO |
| New composable screen | `*TestTags` constants plus an instrumented smoke test of the primary tap path |
| New `items()` block | Stable unique keys, type-prefixed (`"invite-"`, `"member-"`) when several blocks share a column |
| Changed signature or behaviour | Update every test that calls it; add a case for any new path |
| Changed interface | Update **all** fakes, including instrumented ones, in the same commit |
| Changed navigation | `AppNavigatorTest`, `AppNavigationTest`, `HouseholdNavigationContractTest` |
| Removed feature | Delete its tests, its `testTag`s and its QA YAML entries |

Named suites worth knowing: `*LocaleStringsTest` (Auth, Home, Nutrition, Household, Sharing),
`AppIconsTest`, `OfflineFirstNutritionRepositoryTest`, invite parser and OTP tests, Deno `*_test.ts`
helpers. Instrumented tests use `createAndroidComposeRule` + `AppTheme` + `InstrumentedKoinHost` /
`InstrumentedNutritionFakes`, `onNodeWithTag` over text, `performScrollTo` for off-screen nodes, and
`InstrumentedComposeTest.waitFor` instead of `Thread.sleep`.

Supabase checks when touching invites or migrations:

```bash
./scripts/test-supabase-invite-edge.sh      # Deno edge helpers
./scripts/ci-test-invite-preview-rpc.sh     # RPC smoke
./scripts/verify-supabase-household.sh      # staging round-trip, needs credentials
```

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `.github/workflows/kmp-ci.yml` | PR, branch push, tag push, dispatch | Android CI, instrumented UI tests, Supabase migration validation, the distribution jobs |
| `.github/workflows/supabase-deploy.yml` | `main` pushes touching `supabase/**`, dispatch | `db push`, bucket seed, edge deploy, smoke probes |

Website hosting lives in a separate repo, `Rob971/mymultiverse-website`.

| Branch | Purpose | Distribution |
|---|---|---|
| `main` | Integration, always green | Alpha → Firebase App Distribution, automatically on push |
| `release/X.Y` | Beta stabilisation, cut from `main` | `distribute-beta` → Play track `internal` |
| `feature/*` | All feature work | PR → squash → `main` |
| `hotfix/X.Y.Z` | Emergency fix from a production tag | Production via tag |

Jobs: `release` (bump + Firebase + `[skip ci]` commit + tag), `distribute-alpha`, `distribute-beta`,
`distribute-production` (staged or full Play rollout), `play-store-bundle` (signed AAB artifact only).
Android CI builds the debug and androidTest APKs once; the instrumented job downloads them and drives
`adb`. Path filters skip instrumented work when the diff misses `composeApp/**`, `androidApp/**`,
Gradle or the workflow. The distribute jobs use a read-only Gradle cache.

Version codes are computed in `androidApp/build.gradle.kts` from `GITHUB_RUN_NUMBER`: alpha and debug
get `1_000_000 + run`, Play tracks get `100_000 + 3 × run + offset` (beta 1, production 2). Local
builds without that variable fall back to `version.code`.

Secrets (never committed): `SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_ACCESS_TOKEN`,
`SUPABASE_DB_PASSWORD`, `SUPABASE_PROJECT_REF`, `SUPABASE_SERVICE_ROLE_KEY`, `GOOGLE_SERVICES_JSON`,
`FIREBASE_SERVICE_ACCOUNT_JSON`, `FIREBASE_TESTERS`/`FIREBASE_GROUPS` (named release),
`FIREBASE_ALPHA_TESTERS`/`FIREBASE_ALPHA_GROUPS` (alpha), the four `AMMO_UPLOAD_*` signing secrets,
and `AMMO_PLAY_SERVICE_ACCOUNT_JSON` for Play uploads.

Supabase in CI: PR validation runs `supabase start`, applies migrations, seeds buckets and smokes the
invite preview RPC with no remote writes; the CLI is pinned (2.109.0 in KMP CI) and the local CLI can
differ, so a local pass does not prove a CI pass. Deploys happen only in `supabase-deploy.yml`.

## Versioning

`gradle/app-version.properties` is the source of truth: `version.name` is SemVer for users,
`version.code` a monotonic store integer, and `version.prerelease` is stamped by CI in memory only.

| Bump | When |
|---|---|
| PATCH | Bug fix, security or performance fix, copy or i18n correction |
| MINOR | New user-visible capability, additive schema with migration |
| MAJOR | Breaking data model, auth/session contract change, removed feature, migration older clients cannot survive |

Refactors, dependency bumps alone and QA-YAML-only changes ride alpha without a bump. Alpha iterates
on `main` with no `version.name` change; a named `release` dispatch is the only routine way to advance
it. Beta is a feature freeze with PATCH-level fixes only. Production takes hotfixes only, with
`distribution/whatsnew/whatsnew-en-US` updated before the tag.

Display: `AppVersionFormatter` renders `1.4.0`, `1.4.0-alpha.N` or `1.4.0-beta.N` through the single
`home_app_version` key ("Version %1$s"); the suffix in `VERSION_NAME` is what distinguishes tracks, so
no separate channel label or `IS_PRERELEASE` branch belongs in the UI. Alpha < beta < rc < stable, and
a beta build is never called an RC.

## Data layer and Supabase

- `supabase/migrations/` holds ordered SQL; `supabase/config.toml` defines the local stack and the
  storage buckets; `supabase/functions/` holds the Deno edge functions (`notify-household-invite`,
  `invite-open`, `delete-account`), whose pure helpers are unit-tested in `*_test.ts`.
- Household model: `households`, `household_members`, invites and roles; nutrition rows are
  week-scoped with realtime channels per household. (The old `sharing_spaces` / `space_members` /
  `nutrition_space_week_data` names were renamed — always grep `alter table … rename to` before
  trusting a table name in an old doc.)
- Member avatars: `member-avatars` bucket, public read, paths `profiles/{user_id}` and
  `dependants/{dependant_id}`, RLS in `20250703000000_member_avatars.sql`, helper
  `can_upload_dependant_avatar()`; uploads go through
  `SupabaseHouseholdCollaborationRepository.updateMemberAvatar()`.
- Kotlin client: build-time secrets from `local.properties` into generated `SupabaseSecrets.kt`; OAuth
  deeplink `app.mymultiverse.ammo://auth/callback`; Supabase errors map to domain failures, never raw
  exception text in the UI. An invite is accepted only when the email matches the auth email.
- Errors: fail fast at the service boundary, never swallow a write failure; surface through
  `NutritionAiState.Error`, snackbars or inline field errors.
- Observability: `data/observability/` Crashlytics breadcrumbs on Android, with no credentials, tokens
  or emails in them.

Security to re-check on every migration or RLS change: RLS enabled and scoped to `auth.uid()` plus
household role; viewers cannot write nutrition rows; the service role key stays in edge functions and
CI; no PII in migration comments or logs.

## iOS

Shared UI lives in `commonMain`; `composeApp/src/iosMain/…` holds adapters only: auth deeplink
(`data/supabase/AuthDeeplinkHandler.ios.kt`), language (`domain/manager/IOSLanguageManager.kt`),
haptics (`presentation/platform/JourneyHaptics.ios.kt`, a no-op today), APNs push token and personal
export under `data/platform/`. Use `platform.UIKit.*` only where native UI is unavoidable, and
`@ObjCName` on APIs exposed to Swift. There is no iOS instrumented suite: compile
`:composeApp:compileKotlinIosSimulatorArm64` and smoke the app manually.

A local simulator run (verified 2026-09-18, Xcode 27) needs `JAVA_HOME` exported first, then
`xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination
'id=<sim UDID>' -derivedDataPath <dir> IPHONEOS_DEPLOYMENT_TARGET=15.0
ENABLE_USER_SCRIPT_SANDBOXING=NO build` — Xcode's Gradle step does not set `JAVA_HOME`, and the
project's iOS 14.1 target is rejected by Xcode 27.

## Cursor Cloud environment

Linux x86_64, so iOS targets cannot build there. The Android SDK sits at `~/android-sdk` with
`ANDROID_HOME` exported from `~/.bashrc`; Gradle provisions JDK 25 through the foojay resolver.
`local.properties` is written from the `SUPABASE_ANON_KEY` secret; without it the app launches into
`AuthState.ConfigurationMissing` and the nutrition screens are unreachable. Email confirmation and
Google OAuth are on, so a usable account has to be created in the Supabase dashboard — prefer
`scripts/verify-supabase-household.sh` over the emulator for authenticated end-to-end checks. The VM
has no KVM: the emulator runs in software rendering, boots in 7–8 minutes and ANRs under load, so use
it only when a test genuinely needs a device. `:composeApp:lintDebug` runs but is not a CI gate and
currently reports one pre-existing `MissingPermission` error.

## External documentation

Kotlin/KMP, Compose Multiplatform, Material 3, Koin, kotlinx.coroutines, Supabase (Kotlin client, DB,
RLS, Edge), Ktor client, Gradle KMP, Firebase Crashlytics. Confirm any API against
`gradle/libs.versions.toml` before suggesting a pattern, and read release notes for breaking changes
before a bump — then run the unit suite and the instrumented compile.
