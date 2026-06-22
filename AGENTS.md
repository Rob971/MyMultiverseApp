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
