# Agent guidelines (MyMultiverse KMP)

Cursor rules in `.cursor/rules/` define how to plan, implement, test, and ship changes. **Always apply** rules run on every task; others activate when matching files are open.

## Rule index

| Rule | Scope | Topics |
|------|--------|--------|
| `delivery-workflow.mdc` | Always | Plan → implement → test → review → CI; definition of done |
| `architecture-clean.mdc` | Always | Domain / data / presentation, DI, layer boundaries |
| `kmp-core.mdc` | Always | KMP, Compose, resources, coroutines |
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

Instrumented UI tests: GitHub Actions → **KMP CI** → `workflow_dispatch` → `android-instrumented-tests`.
