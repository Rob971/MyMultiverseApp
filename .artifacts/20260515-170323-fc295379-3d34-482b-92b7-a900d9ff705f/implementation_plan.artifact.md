# Implementation Plan - i18n Support

This plan outlines the steps to add internationalization (i18n) support for English (en), Spanish (es), Italian (it), French (fr), and German (de) markets.

## Proposed Changes

### Configuration

#### [composeApp/build.gradle.kts](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/build.gradle.kts)
- Add `implementation(compose.components.resources)` to `commonMain` dependencies if not already present.

### Resources

#### [NEW] [strings.xml](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/composeResources/values/strings.xml)
- Default strings (English).

#### [NEW] [strings.xml](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/composeResources/values-en/strings.xml)
- English translations.

#### [NEW] [strings.xml](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/composeResources/values-es/strings.xml)
- Spanish translations.

#### [NEW] [strings.xml](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/composeResources/values-it/strings.xml)
- Italian translations.

#### [NEW] [strings.xml](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/composeResources/values-fr/strings.xml)
- French translations.

#### [NEW] [strings.xml](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/composeResources/values-de/strings.xml)
- German translations.

### Presentation Layer

Update the following files to use `stringResource` instead of hardcoded strings:
- [HomeScreen.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/screens/home/HomeScreen.kt)
- [JourneyEditScreen.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/screens/home/JourneyEditScreen.kt)
- [CalendarScreen.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/screens/calendar/CalendarScreen.kt)
- [InsightsScreen.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/screens/insights/InsightsScreen.kt)
- [DetailScreen.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/screens/detail/DetailScreen.kt)
- [JourneyBanner.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/components/JourneyBanner.kt)
- [JourneyDreamCard.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/components/JourneyDreamCard.kt)
- [TaskEditDialog.kt](file:///Users/roberto971/myprojects/mymultiverse/KmpVoyagerCleanArchitecture/composeApp/src/commonMain/kotlin/com/mymultiverse/kmp/presentation/components/TaskEditDialog.kt)

## Verification Plan

### Automated Tests
- Run `./gradlew :composeApp:assembleDebug` to ensure the project still builds with the new resources.

### Manual Verification
- I will use `render_compose_preview` on one of the screens to verify that strings are being loaded (default language).
- I will check the generated `Res` class to ensure all keys are correctly generated.
