# Agent guidelines (MyMultiverse KMP)

Cursor rules in `.cursor/rules/` define how to plan, implement, test, review, commit, and ship. **Always-apply** rules run on every task; others activate when matching files are open.

## Product context

**MyMultiverse** — household nutrition logistics (shared grocery + weekly meal plan + local AI assistant). Single-household collaboration with roles (owner/admin/editor/viewer). Auth via Supabase; offline-first nutrition sync. Visual language: **Napolitan Heart** (terracotta + teal + parchment). Design system Waves A–C are **shipped** (Journey components, dark theme, wide layouts).

## Rule index

| Rule | Scope | Topics |
|------|--------|--------|
| `delivery-workflow.mdc` | Always | Plan → implement → test → review → CI/release |
| `review-commit.mdc` | Always | Self-review, PR/commit gates, merge & release policy |
| `architecture-clean.mdc` | Always | Domain / data / presentation, DI, screen models |
| `kmp-core.mdc` | Always | Stack versions, AppNavigator, Koin, Compose, coroutines |
| `i18n-multilingual.mdc` | Always | 8 locales, string keys, parity tests, RTL |
| `official-docs-urls.mdc` | Always | Indexed docs + repo skills for fast-moving APIs |
| `ui-ux-compose.mdc` | `presentation/**` | Journey design system, dark/wide UX, testTags |
| `qa-testing.mdc` | Tests + Firebase YAML | Unit, instrumented, manual QA v31 |
| `ci-cd.mdc` | `.github/`, Firebase YAML | Actions, manual release, secrets |
| `backend-data.mdc` | `data/**` | Repositories, sync, codecs, services |
| `supabase-backend.mdc` | `supabase/**`, `data/supabase/**` | Migrations, RLS, edge functions |
| `ios-platform.mdc` | `iosMain/**` | UIKit, interop, iOS compile |

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

1. Plan + minimal scope  
2. Clean architecture + Journey UI + 8-locale i18n  
3. Unit tests green; instrumented compile if UI touched  
4. `firebase-appdistribution-testcases.yaml` updated if flows changed  
5. `review-commit.mdc` self-review passed  
6. Commit/PR only when user asks; release manual after merge  
