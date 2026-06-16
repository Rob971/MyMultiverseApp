# Agent guidelines (MyMultiverse KMP)

Cursor rules in `.cursor/rules/` define how to plan, implement, test, and ship changes. **Always apply** rules run on every task; others activate when matching files are open.

## Rule index

| Rule | Scope | Topics |
|------|--------|--------|
| `delivery-workflow.mdc` | Always | Plan → implement → test → review → CI; definition of done |
| `architecture-clean.mdc` | Always | Domain / data / presentation, DI, layer boundaries |
| `kmp-core.mdc` | Always | KMP, Compose, resources, coroutines |
| `i18n-multilingual.mdc` | Always | 8 locales, string keys, parity tests, RTL, no hardcoded UI copy |
| `official-docs-urls.mdc` | Always | Indexed docs for fast-moving APIs |
| `ui-ux-compose.mdc` | `presentation/**` | Layout, design system, UX states, i18n, testTags |
| `qa-testing.mdc` | Tests + Firebase YAML | Unit, instrumented, manual QA checklist |
| `ci-cd.mdc` | `.github/`, Firebase YAML | Actions, secrets, App Distribution |
| `backend-data.mdc` | `data/**` | Repositories, services, persistence |
| `ios-platform.mdc` | `iosMain/**` | UIKit, Swift interop |

## Quick commands

```bash
./gradlew :composeApp:testDebugUnitTest
./gradlew :composeApp:compileDebugAndroidTestKotlinAndroid
```

Instrumented UI tests: run on **push** (including `feature/**`), **pull request** (opened / reopened), or GitHub Actions → **KMP CI** → `workflow_dispatch` → pick `all`, `android-unit-tests`, or `android-instrumented-tests`.
