# Agent guidelines (MyMultiverse KMP)

> ## MANDATORY FOR ALL AGENTS
>
> **Every Cursor agent session MUST follow `.cursor/rules/agents-mandatory.mdc` and all rules it references.**  
> Do not implement, review, test, commit, or release without the task-start and task-end checklists in that file.  
> Rules are binding — not suggestions.

Cursor rules in `.cursor/rules/` define how to plan, implement, test, review, commit, and ship. Rules with `alwaysApply: true` are injected into every session; scoped rules must be **read** before editing matching paths (see `agents-mandatory.mdc`).

## Product context

**MyMultiverse** — household nutrition logistics (shared grocery + weekly meal plan + local AI assistant). Single-household collaboration with roles (owner/admin/editor/viewer). Auth via Supabase; offline-first nutrition sync. Visual language: **Napolitan Heart** (terracotta + teal + parchment). Design system Waves A–C are **shipped** (Journey components, dark theme, wide layouts).

## Rule index

### Always apply (every session)

| Rule | Topics |
|------|--------|
| **`agents-mandatory.mdc`** | **Compliance gate — read first** |
| `delivery-workflow.mdc` | Plan → implement → test → review → CI/release |
| `review-commit.mdc` | Self-review, PR/commit policy, merge blockers |
| `architecture-clean.mdc` | Domain / data / presentation, DI, screen models |
| `kmp-core.mdc` | Stack versions, AppNavigator, Koin, Compose, coroutines |
| `i18n-multilingual.mdc` | 8 locales, string keys, parity tests, RTL |
| `official-docs-urls.mdc` | Indexed docs + repo skills |
| `ui-ux-compose.mdc` | Journey design system, dark/wide UX, testTags |
| `qa-testing.mdc` | Unit, instrumented, Firebase YAML |

### Read before editing matching paths

| Rule | When to read |
|------|----------------|
| `backend-data.mdc` | `data/**` changes |
| `supabase-backend.mdc` | `supabase/**`, `data/supabase/**` |
| `ci-cd.mdc` | `.github/**`, Firebase YAML, release |
| `ios-platform.mdc` | `iosMain/**`, `iosApp/**` |

## Tech stack (current)

| Layer | Technology |
|-------|------------|
| UI | Compose Multiplatform 1.8.0, Material 3, custom `AppNavigator` |
| DI | Koin 4.0.2 |
| Async | kotlinx.coroutines 1.10.2 |
| Backend | Supabase Kotlin 3.5.0 + Postgres RLS + Deno edge functions |
| Network | Ktor 3.4.0 |
| Persistence | `NutritionLocalStore`, multiplatform-settings, sync outbox |
| Android | AGP 9.2.1, minSdk 24, Firebase Crashlytics/Messaging |
| iOS | Compose MP + iosApp; CI compile disabled (local/manual) |
| i18n | 8 locales via `composeResources` |
| QA | commonTest + androidInstrumentedTest + Firebase YAML |

**Not used:** Voyager navigation (catalog only), SQLDelight.

## Repo skills (read when relevant)

- `.agents/skills/supabase/SKILL.md` — any Supabase work
- `.agents/skills/supabase-postgres-best-practices/SKILL.md` — SQL/RLS performance

## Quick commands

```bash
# Unit tests (required before merge)
./gradlew :composeApp:testDebugUnitTest

# Instrumented compile (when UI changed)
./gradlew :composeApp:compileDebugAndroidTestKotlinAndroid

# iOS compile (local)
./gradlew :composeApp:compileKotlinIosSimulatorArm64

# Supabase smoke (staging credentials)
./scripts/verify-supabase-household.sh
```

## CI & release

| Action | Command / trigger |
|--------|-------------------|
| PR checks | `gh pr checks N --watch --interval 15` |
| Merge | Squash to `main`, delete branch |
| Release | `gh workflow run "KMP CI" --ref main -f job=release -f version_bump=patch` |

PR/push runs Android CI + instrumented + Supabase migration validation. **No auto-release on merge.**

## Definition of done (summary)

1. `agents-mandatory.mdc` task-start + task-end checklists satisfied  
2. Plan + minimal scope (`delivery-workflow.mdc`)  
3. Clean architecture + Journey UI + 8-locale i18n  
4. Unit tests green; instrumented compile if UI touched (`qa-testing.mdc`)  
5. `firebase-appdistribution-testcases.yaml` updated if flows changed  
6. `review-commit.mdc` self-review passed  
7. Commit/PR only when user asks; release manual after merge  

## Cursor Cloud specific instructions

Environment is Linux x86_64 (no macOS, so iOS targets cannot be built/run here). The startup update script ensures `local.properties` has `sdk.dir` and pre-resolves Gradle dependencies.

- **Toolchain (provided by the snapshot, not the update script):** Android SDK at `~/android-sdk` (cmdline-tools, `platform-tools`, `platforms;android-35`, `build-tools;35.0.0`, `emulator`, `system-images;android-35;google_apis;x86_64`). `ANDROID_HOME`/`ANDROID_SDK_ROOT`/`PATH` are exported from `~/.bashrc`. JDK 17 for the build is auto-provisioned by Gradle's foojay toolchain resolver (system default JDK is 21, which only runs Gradle itself).
- **Supabase config comes from the `SUPABASE_ANON_KEY` secret.** The update script writes `supabase.url` + `supabase.anonKey` into `local.properties` from the injected `SUPABASE_ANON_KEY` env var (project `https://ivjdzreazvkrrirecznk.supabase.co`); the build's `generateSupabaseSecrets` task embeds them at compile time. If that secret is absent, the key is empty and the app launches to the login screen showing auth "not available on this build" (`AuthState.ConfigurationMissing`). The UI auth gate (`presentation/App.kt`) means the household/nutrition screens are **not reachable** without a valid key — there is no UI path to local-only nutrition.
- **Authenticated flows need a confirmed account.** With the anon key set, the auth round-trip works (verified: `POST /auth/v1/token?grant_type=password` returns proper backend responses). But the project has email confirmation enabled (`mailer_autoconfirm: false`) and Google OAuth, so you cannot self-register a usable account from the emulator. To exercise sign-in → create household → grocery/meal-plan end to end, supply credentials for an already-confirmed account (e.g. `SUPABASE_TEST_EMAIL` / `SUPABASE_TEST_PASSWORD`, as referenced in `README.md`).
- **Standard build/test commands** are in `README.md` and the Quick commands table above (`:composeApp:assembleDebug`, `:composeApp:testDebugUnitTest`, `:composeApp:compileDebugAndroidTestKotlinAndroid`). All work here without a device. Lint (`:composeApp:lintDebug`) runs but currently has one pre-existing error (`MissingPermission` in `androidMain/.../push/HouseholdPushNotificationHandler.kt`); lint is not a CI gate (see `ci-cd.mdc`).
- **Emulator caveat (important):** there is **no KVM** on the VM, so the emulator only runs in pure software rendering (`emulator -avd <name> -no-accel -gpu swiftshader_indirect -no-window`). It is extremely slow: cold boot ~7–8 min, app first-frame render can take 1–3 min, and `system_server`/SystemUI frequently throw "isn't responding" ANRs under CPU load. The app does launch and render its real UI (verified), but **interactive UI testing via taps is unreliable** — prefer the unit/instrumented test suites for verifying logic. If you must interact, run `adb shell settings put global hide_error_dialogs 1`, give long idle periods between actions, and drive via `adb` rather than expecting fast GUI responsiveness.
