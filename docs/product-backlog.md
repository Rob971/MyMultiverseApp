# Product UX backlog — nutrition logistics & silent-butler AI

**Status:** S12 shipped · **Updated:** 2026-06-23  
**App:** MyMultiverse (KMP) · **QA YAML baseline:** v52 · **Release:** 1.0.36  
**Current sprint:** S12 complete — E3-9 DNS go-live (ops)

This backlog synthesizes product thesis, UX roadmap, and AI interaction model for household grocery + meal planning. Use it as the scope razor before building anything new.

**Related rules:** `.cursor/rules/ui-ux-compose.mdc`, `i18n-multilingual.mdc`, `qa-testing.mdc`

---

## Progress summary

| Phase | Sprints | Status |
|-------|---------|--------|
| **Phase 0** — Grandma adds milk | S1, S2 | **Done** |
| **Phase 1** — Paper at the store | S3, S4 | **Done** |
| **Phase 2** — Silent butler | S5–S7 | **Done** |
| **Phase 3** — Family in the loop | S8–S10 | **Done** |
| **Phase 4** — Ghost magic | S11–S12 | **Done** |
| **Phase 5** — UX audit clarity | S13 | **Done** |

**Phases remaining:** Phase 5 maintenance + ops (E3-9 DNS). Phases 0–4 are complete.

**Open stories (non-deferred):** E3-9 (App Links DNS live).

### Shipped stories (28 unique IDs + E6-3 ongoing)

| Sprint | Stories |
|--------|---------|
| **S1** | E1-1, E1-2, E1-3, E3-1, E6-3 |
| **S2** | E1-4, E2-1, E2-3, E5-1 |
| **S3** | E2-2, E2-4, E3-5 |
| **S4** | E3-6, E1-5 (+ instrumented: swipe-to-check, wakelock, empty-grocery CTA) |
| **S5** | E4-1, E4-4 (+ sheet dismiss on apply; instrumented: `AiHelperSheet`) |
| **S6** | E4-2, E4-3, E4-8 (+ `AiHelperLaunchContext`, grocery sheet; instrumented: inline triggers) |
| **S7** | E4-5, E4-6, E4-7, E5-3 (+ chip-first sheet, empty-state chips; instrumented) |
| **S8** | E3-2, E1-7 (+ invite share sheet, Sunday plan nudge on Today) |
| **S9** | E3-3, E3-4 (+ member-joined push, foreground FCM, grocery collaboration snackbar) |
| **S10** | E3-7, E5-2, E6-1 (+ silent duplicate merge, suggest→ingredients sheet, font scaling) |
| **S11** | E1-6, E6-2 (+ week banner on Today, tab + AI a11y) |
| **S12** | E4-9 (+ ghost pairing banner on Groceries), E4-10 (+ pantry check on meal→grocery), E4-11 (+ contextual Use up chips from meal/grocery history) |
| **Post-S11 releases** | Onboarding v1.0.33–36, App Links tooling, Firebase QA v48 (see below) |

**Next up:** E3-9 DNS go-live · E4-9 (#52) close when verified on testers

---

## North star

**MyMultiverse wins when a family plans dinner and shops groceries with less effort than paper and texting — together, every week.**

### Three goals (scope filter)

| Goal | Razor question |
|------|----------------|
| **Paper-easy** | Is this faster than a sticky note at the store? |
| **Safe sharing** | Would a less technical user trust that their partner sees the same list? |
| **Weekly habit** | Does this bring them back Sunday plan → Friday shop? |

### AI rule (cross-cutting)

**Silent butler** — contextual, tap-driven, bottom-sheet, terracotta read-only output, never a chat room or floating bubble. Sparkles = AI only; editable lists stay separate.

### Explicitly out of scope (until core loop metrics are green)

- Adventures / Budget surfaces
- Floating AI chat bubble / FAB
- Full-screen AI as the **default** tab path (sheet is default on Plan; full-screen remains for overlay/deep link until E4-7)
- Anonymous guest editing (RLS model requires auth)
- Auto-aisle grouping, recipe scrape, live presence, predictive pantry (Phase 4+)

---

## Already shipped (do not regress)

**Foundation (pre–UX backlog)**

- Bottom tabs: Today / Plan / Groceries (`MainTabShell`, v32)
- Sticky `GroceryInputBar` (phone bottom bar; side panel ≥600dp)
- First-win checklist on Welcome Home (`HomeFirstWinChecklist`)
- Guided empty states (grocery, meal plan, solo household)
- Offline-first nutrition + sync outbox + `NutritionSyncStatusBanner`
- AI read-only terracotta (`AiReadOnlyGroceryList`, `AiGrocerySuggestionsSection`)
- Deep-link invites + Google/Apple SSO on join (`JoinHouseholdScreen`)
- Per-meal and bulk meal → grocery (`nutrition_meal_generate_grocery`)
- Journey design system Waves A–C (dark theme, wide layouts, 48dp targets)
- 8-locale i18n + parity tests

**Phase 0 — S1–S2 (v33)**

- Plain-language copy: “family” not “household”; no Supabase/sync jargon (E1-1)
- Tonight’s dinner card on Today → tap opens Plan tab (E1-2)
- Solo one-tap create family + bottom tabs (`quickCreateHousehold`, E1-3)
- Friendlier sync banner + icons + brief synced pulse (E3-1)
- Post-create auto-focus Groceries tab (E1-4)
- Keep screen on while shopping — `KeepScreenOn` on Groceries (E2-1)
- iOS haptics on grocery check-off (E2-3)
- Hero “Add to grocery list” on `MealPlanDayCard` (E5-1)
- Firebase QA YAML maintained through v33 (E6-3)

**Phase 1 — S3–S4 (v34–v35)**

- Bidirectional swipe on grocery rows: right = check, left = delete (E2-2)
- Shopping mode: unchecked first; hide/show checked toggle (E2-4)
- Viewer read-only banner on Groceries + Plan tab roots (E3-5)
- Skip redundant `NutritionEntryGate` when family already resolved in tabs (E3-6)
- Empty grocery CTA on Today when list is empty (E1-5)
- Instrumented tests: swipe-to-check, wakelock flag, empty-grocery CTA

**Phase 2 — S5–S7 (v36–v38)**

- `AiHelperSheet` — `ModalBottomSheet` ~60% over Plan tab; dismiss on apply (E4-1, S5)
- Shared `NutritionAiAssistantContent`; Plan tab “Plan with AI” opens sheet (not full-screen)
- AI copy rewrite — output-first, no chatbot tone, all 8 locales (E4-4, S5)
- Inline empty meal slot “Suggest 20-min meal”; grocery “Build list from this week’s meals” chip; terracotta AI chips (E4-2, E4-3, E4-8, S6)
- Chip-first sheet, launch context hides mode switcher, full-screen AI demoted to fallback; plan empty-state AI chips (E4-5, E4-6, E4-7, E5-3, S7)
- Instrumented tests: `AiHelperSheet`, inline triggers, chip-first sheet, empty-state chips

**Phase 3 — S8–S9 (v39–v40)**

- Invite share sheet with pre-written SMS/WhatsApp message + deep link (E3-2, S8)
- Sunday empty-week plan nudge on Today — dismissible, week-scoped (E1-7, S8)
- Member-joined push via FCM foreground handler + `HouseholdPushEvents` (E3-3, S9)
- Debounced grocery collaboration snackbar on remote add/check (E3-4, S9)
- Firebase QA YAML v40: invite share, Sunday nudge, member-joined push, collaboration snackbar (E6-3)

**Post–S11 releases (v1.0.33–1.0.36, QA v43–v48)**

- SSO onboarding gate (`AuthScreen`), dedicated household setup, invite deep-link resolver (v1.0.33)
- Dark-mode household members cards + overflow menus; semantic ink on auth/invite surfaces (v1.0.34)
- `JourneySemanticColors` sweep on shared UI components (v1.0.35)
- Email/password auth fallback; HTTPS invite App Links client; instrumented onboarding smoke (v1.0.36)
- Firebase App Links hosting: `firebase.json`, deploy/verify/DNS scripts, GitHub **App Links hosting** workflow (v47)
- Ghost pairing banner on Groceries — terracotta “Often bought together” + one-tap add (E4-9, v48)

**Navigation note:** Bottom tabs use custom `MainTabShell` + `AppRoute` — **not** Voyager (catalog only).

## Epic map

| Epic | Goal | Theme |
|------|------|--------|
| **E1** | Habit + Paper | Onboarding & Today |
| **E2** | Paper | Grocery at the store |
| **E3** | Sharing | Sync & collaboration trust |
| **E4** | Habit + Paper | Silent butler AI |
| **E5** | Habit | Meal ↔ grocery loop |
| **E6** | Paper | Accessibility & polish |
| **E7** | All | Bigger bets (deferred) |

**Priority:** P0 = next sprint · P1 = following 2–3 sprints · P2 = later · P3 = deferred  
**Status:** ✅ shipped · 🔜 next · ⏳ planned

---

## E1 — Onboarding & Today

| ID | Story | P | Status | Primary touchpoints |
|----|-------|---|--------|---------------------|
| E1-1 | Plain-language copy pass (“family” not “household”; no Supabase/sync jargon to end users) | P0 | ✅ S1 | All 8 `composeResources/values*/strings.xml`, locale parity tests |
| E1-2 | “Tonight’s dinner” on Today tab → tap opens Plan (today expanded) | P0 | ✅ S1 | `HomeScreen`, `HomeScreenModel`, `nutritionSummary` |
| E1-3 | Solo path: one-tap create family + show bottom tabs immediately | P0 | ✅ S1 | `HomeScreen` onboarding, `App.kt` `showBottomBar` |
| E1-4 | Post-create: auto-focus Groceries tab or prominent “Add first item” on Today | P0 | ✅ S2 | `App.kt` tab state, `PostCreateFocusTarget` |
| E1-5 | Empty grocery CTA on Today when list is empty | P1 | ✅ S4 | `HomeScreen` This week section |
| E1-6 | Week context banner on Today (“Week of …”) | P1 | ✅ S11 | `WeekContextBanner` on `HomeWelcomeContent` |
| E1-7 | Sunday empty-week nudge on Today | P1 | ✅ S8 | `HomeSundayPlanNudgeCard`, `HomeWeekPlanNudgeStore` |
| E1-8 | Today tab: three hero actions (Plan meals · Grocery list · Family hub); demote duplicate cards | P1 | ✅ S13 | `HomePrimaryActions`, `HomeWelcomeContent` |
| E1-9 | Family hub in account sheet (profile avatar); remove Family section from Today scroll | P1 | ✅ S13 | `HomeAccountSheet`, `AppRoute.HouseholdMembers` |

## E2 — Grocery at the store

| ID | Story | P | Status | Primary touchpoints |
|----|-------|---|--------|---------------------|
| E2-1 | Wakelock setting: “Keep screen on while shopping” (auto on Groceries tab) | P0 | ✅ S2 | `KeepScreenOn` expect/actual; `GroceryShoppingScreen` |
| E2-2 | Swipe-to-check (wide gesture); retain row tap + haptics; reconcile with swipe-to-delete | P0 | ✅ S3 | `GroceryItemRow.kt` |
| E2-3 | Wire iOS haptics on grocery check-off | P0 | ✅ S2 | `JourneyHaptics.ios.kt` |
| E2-4 | Shopping mode: unchecked first; optional hide checked | P1 | ✅ S3 | `GroceryShoppingScreen`, `GroceryListPresentation` |
| E2-5 | Auto-aisle categorization | P3 | Deferred | — |
| E2-6 | Smart paste / recipe ingredient parse | P3 | Deferred | — |

---

## E3 — Sync & collaboration trust

| ID | Story | P | Status | Primary touchpoints |
|----|-------|---|--------|---------------------|
| E3-1 | Friendlier sync copy + status icon (pending → synced) | P0 | ✅ S1 | `NutritionSyncStatusBanner.kt`, string keys |
| E3-2 | Invite share sheet with pre-written SMS/WhatsApp message + deep link | P1 | ✅ S8 | `HouseholdMembersScreen`, `HouseholdInviteSharePayload` |
| E3-3 | Push notifications: invite received / member joined | P1 | ✅ S9 | `InvitePushPayload`, `PlatformPushSetup`, edge |
| E3-4 | Lightweight activity snackbar (“Maria added milk”) | P2 | ✅ S9 | `NutritionScreenModel`, realtime/sync |
| E3-5 | Viewer read-only banner on Groceries + Plan tab roots | P1 | ✅ S3 | `HouseholdViewerReadOnlyNotice` on tab entry |
| E3-6 | Skip redundant `NutritionEntryGate` when household already resolved | P1 | ✅ S4 | `NutritionFlow.kt`, `App.kt` |
| E3-7 | Silent duplicate merge (e.g. two “Milk” → one line or count) vs error snackbar | P2 | ✅ S10 | Domain + `GroceryShoppingScreen` |
| E3-8 | Live presence (“Alex is shopping now”) | P3 | Deferred | — |
| E3-9 | HTTPS App Links hosting on `mymultiverse.app` (Firebase Hosting + custom DNS) | P1 | ⏳ S12 | `web/`, `scripts/deploy-app-links-hosting.sh`, `docs/app-links-custom-dns.md` |

## E4 — Silent butler AI

| ID | Story | P | Status | Primary touchpoints |
|----|-------|---|--------|---------------------|
| E4-1 | `AiHelperSheet` (`ModalBottomSheet` ~60%) reusing `NutritionScreenModel.runAiAssistant()` | P0 | ✅ S5 | `AiHelperSheet.kt`, `NutritionAiAssistantContent.kt` |
| E4-2 | Inline trigger: empty meal slot → “Suggest 20-min meal” (Sparkles, tertiary) | P0 | ✅ S6 | `MealPlanDayCard` |
| E4-3 | Grocery chip: “Build list from this week’s meals” | P0 | ✅ S6 | `GroceryShoppingScreen` |
| E4-4 | Copy rewrite — output-first, no chatbot tone (8 locales) | P0 | ✅ S5 | `nutrition_ai_*` keys |
| E4-5 | Chip-first sheet; text field behind “More options” | P1 | ✅ S7 | `AiHelperSheet` |
| E4-6 | Mode from launch context (hide Advice/Grocery/Meal plan switcher in default flow) | P1 | ✅ S7 | Sheet launch params |
| E4-7 | Demote full-screen `NutritionAiAdviceScreen` to fallback/deep link only | P1 | ✅ S7 | `NutritionFlow`, `WeeklyMealPlanScreen` |
| E4-8 | Terracotta AI chips in read-only zones (fix teal on `AiGrocerySuggestionChips` where AI-origin) | P1 | ✅ S6 | `ui-ux-compose.mdc` |
| E4-9 | Ghost pairing banner (“+ Add salsa and cheese?”) | P2 | ✅ S12 | `GroceryGhostPairingBanner` + `GroceryGhostPairing` |
| E4-10 | Pantry check section: “Check if you have these” on meal→grocery | P2 | ✅ S12 | `PantryCheckSection`, `MealGroceryPartition` |
| E4-11 | Contextual chips from history (“Use up chicken”) | P2 | ✅ S12 | `NutritionContextualChips`, `MealPlanEmptyState`, `NutritionAiAssistantContent` |

---

## E5 — Meal ↔ grocery loop

| ID | Story | P | Status | Primary touchpoints |
|----|-------|---|--------|---------------------|
| E5-1 | Primary CTA per meal: “Add to grocery list” (hero on `MealPlanDayCard`) | P0 | ✅ S2 | `WeeklyMealPlanScreen`, `MealPlanDayCard` |
| E5-2 | Sheet flow: suggest meal → add → optional ingredients in one path | P1 | ✅ S10 | E4 + E5 integration |
| E5-3 | Plan tab empty state: contextual AI chips opening sheet | P1 | ✅ S7 | `MealPlanEmptyState` |
| E5-4 | Predictive replenishment | P3 | Deferred | — |
| E5-5 | Pantry memory for AI suggestions | P3 | Deferred | — |

---

## E6 — Accessibility & polish

| ID | Story | P | Status | Primary touchpoints |
|----|-------|---|--------|---------------------|
| E6-1 | System font scaling on grocery rows + meal plan | P1 | ✅ S10 | `GroceryItemRow`, `MealPlanDayCard` |
| E6-2 | `contentDescription` audit on tab bar + AI entry points | P2 | ✅ S11 | `MainTabShell`, `AiInlineTriggerButton` |
| E6-3 | Firebase QA YAML + manual cases for changed flows | P0 | ✅ ongoing | `firebase-appdistribution-testcases.yaml` (currently **v48**) |
| E6-4 | Napulitano explicit language label + dialect subtitle in picker (not “NAP” code) | P2 | ✅ S13 | `SupportedAppLanguages`, `LanguagePicker`, 8 locales |

## Execution plan (12 weeks)

### Phase 0 — “Grandma adds milk” (weeks 1–2) ✅

**Promise:** “I opened the app and added milk without getting lost.”

| Sprint | Deliverables | Status |
|--------|----------------|--------|
| **S1** | E1-1, E1-2, E1-3, E3-1, E6-3 | ✅ |
| **S2** | E1-4, E2-1, E2-3, E5-1 | ✅ |

**Exit metrics:** onboarding completion ↑ · time-to-first-grocery-item &lt; 2 min median · locale tests green.

### Phase 1 — “Paper at the store” (weeks 3–4) ✅

**Promise:** “At the store, this beats my notes app.”

| Sprint | Deliverables | Status |
|--------|----------------|--------|
| **S3** | E2-2, E2-4, E3-5 | ✅ |
| **S4** | E3-6, E1-5, instrumented tests (swipe, wakelock) | ✅ |

**Exit metrics:** check-offs per shop ↑ · Groceries tab weekly opens ↑.

### Phase 2 — “Silent butler” (weeks 5–7) ✅

**Promise:** “AI suggests dinner in place — it doesn’t talk at me.”

| Sprint | Deliverables | Status |
|--------|----------------|--------|
| **S5** | E4-1, E4-4, sheet dismiss on apply | ✅ |
| **S6** | E4-2, E4-3, E4-8 | ✅ |
| **S7** | E4-5, E4-6, E4-7, E5-3 | ✅ |

**Exit metrics:** AI opens from inline &gt; 80% · keyboard use in AI &lt; 20% · adopt rate ↑.

### Phase 3 — “Family in the loop” (weeks 8–10) — in progress

**Promise:** “My partner and I trust the same list.”

| Sprint | Deliverables | Status |
|--------|----------------|--------|
| **S8** | E3-2, E1-7 | ✅ |
| **S9** | E3-3, E3-4 | ✅ done |
| **S10** | E3-7, E5-2, E6-1 | ✅ **shipped** |
| **S11** | E1-6, E6-2 (polish) | ✅ **shipped** |

**Exit metrics:** invite accept within 48h ↑ · households with 2+ weekly actives ↑.

### Phase 4 — “Ghost magic” (weeks 11–12+, optional)

Only if Phases 0–2 metrics are green.

| Sprint | Deliverables | Status |
|--------|----------------|--------|
| **S12** | E4-9 ghost pairing | ✅ shipped (v48) |
| **S12** | E4-10 pantry check | ✅ shipped (v50) |
| **S12+** | E3-9 DNS go-live · optional spike (E3-8 presence **or** E2-5 aisle) | ⏳ |

### Phase 5 — “UX audit clarity” (S13)

**Promise:** “Today tells me what matters now — without five buttons saying the same thing.”

| Sprint | Deliverables | Status |
|--------|----------------|--------|
| **S13** | E1-8 Today hero CTAs (keep bottom tabs) | ✅ shipped |
| **S13** | E1-9 Family hub via profile account sheet | ✅ shipped |
| **S13** | E6-4 Napulitano picker label + subtitle | ✅ shipped |

**Deprecate on Today (when S13 ships):** “This week” nutrition card duplicate CTAs, first-win checklist on scroll, inline Family household card (Sunday nudge stays dismissible only).

## Dependency graph

```
E1-3 solo onboarding ──► E1-4 Groceries focus ✅
E4-1 AiHelperSheet ✅ ──► E4-2, E4-3, E4-5, E5-2
E2-2 swipe-to-check ✅ ──► instrumented grocery tests ✅
E1-1 copy pass ✅ ──► E4-4 AI copy ✅
E1-8 Today hero CTAs ──► demotes E1-5 empty-grocery CTA on scroll (tabs remain)
E1-9 account sheet family ──► replaces Today Family section
E3-9 App Links DNS ──► HTTPS invite opens Android app (E3-2 share links)
```

---

## Metrics (monthly review)

| Metric | Target signal | Phase |
|--------|---------------|-------|
| Time to first grocery item | &lt; 2 min median | 0 |
| Onboarding → Welcome Home | &gt; 80% | 0 |
| Groceries tab opens / user / week | ≥ 1 | 1 |
| Check-offs per shopping trip | ≥ 3 | 1 |
| Meal slots filled / week / household | ≥ 3 | 2 |
| AI sheet vs full-screen opens | &gt; 80% sheet | 2 |
| Households with 2+ weekly actives | &gt; 40% | 3 |
| Invite sent → joined (48h) | baseline → improve | 3 |

---

## Definition of done (every story)

- [ ] Journey components; semantic colors; 48dp touch targets
- [ ] New strings in all **8** locales; `*LocaleStringsTest` / key registry updated
- [ ] Unit tests for domain/screen model; instrumented for primary tap paths
- [ ] `firebase-appdistribution-testcases.yaml` bumped if user-facing flow changes
- [ ] `./gradlew :composeApp:testDebugUnitTest` (+ instrumented compile if UI)
- [ ] **AI stories:** terracotta read-only · Sparkles only · explicit adopt · no mixed rows

---

## Minimum viable path (6 weeks, constrained capacity)

1. ~~**S1 + S2** — onboarding, tonight’s dinner, wakelock, copy~~ ✅  
2. ~~**S5–S7** — silent butler AI (sheet, inline triggers, chip-first)~~ ✅  
3. ~~**S3** — swipe-to-check + E5-1 meal CTA~~ ✅  
4. ~~**S8** — invite share sheet + Sunday plan nudge~~ ✅ · ~~**S9** — push + activity snackbar~~ ✅ · ~~**S10** — household polish~~ ✅ · ~~**S11** — week banner + a11y~~ ✅  
5. ~~**S13** — Today hero CTAs + family in account sheet + Napulitano label (UX audit)~~ ✅  
6. ~~**S12** — E4-10 pantry check · E4-11 contextual chips~~ ✅ · **E3-9** App Links DNS live (manual DNS)

## GitHub tracking

**Shipped story issues (S1–S11 polish):** #26–#31, #32–#43, #44–#51, #55–#59 — **closed** on GitHub.

**Open / next:** E3-9 · E4-9 (#52) close when verified on testers.

| Kind | Issue |
|------|-------|
| **Meta** | [#25 — Product UX backlog](https://github.com/Rob971/MyMultiverseApp/issues/25) |
| **Epic E1** | [#66 — Onboarding & Today](https://github.com/Rob971/MyMultiverseApp/issues/66) |
| **Epic E2** | [#67 — Grocery at the store](https://github.com/Rob971/MyMultiverseApp/issues/67) |
| **Epic E3** | [#68 — Sync & collaboration trust](https://github.com/Rob971/MyMultiverseApp/issues/68) |
| **Epic E4** | [#69 — Silent butler AI](https://github.com/Rob971/MyMultiverseApp/issues/69) |
| **Epic E5** | [#70 — Meal ↔ grocery loop](https://github.com/Rob971/MyMultiverseApp/issues/70) |
| **Epic E6** | [#71 — Accessibility & polish](https://github.com/Rob971/MyMultiverseApp/issues/71) |
| **Epic E7** | [#72 — Bigger bets (deferred)](https://github.com/Rob971/MyMultiverseApp/issues/72) |

**Labels:** `ux-backlog`, `phase-0` … `phase-5`, `epic-e1` … `epic-e7`

**Search:** [`label:ux-backlog`](https://github.com/Rob971/MyMultiverseApp/issues?q=label%3Aux-backlog)

### Story index (#26–#65)

| ID | Issue | Phase | P | Status |
|----|-------|-------|---|--------|
| E1-1 | [#26](https://github.com/Rob971/MyMultiverseApp/issues/26) | 0 | P0 | ✅ S1 |
| E1-2 | [#27](https://github.com/Rob971/MyMultiverseApp/issues/27) | 0 | P0 | ✅ S1 |
| E1-3 | [#28](https://github.com/Rob971/MyMultiverseApp/issues/28) | 0 | P0 | ✅ S1 |
| E1-4 | [#29](https://github.com/Rob971/MyMultiverseApp/issues/29) | 0 | P0 | ✅ S2 |
| E1-5 | [#30](https://github.com/Rob971/MyMultiverseApp/issues/30) | 1 | P1 | ✅ S4 |
| E1-6 | [#31](https://github.com/Rob971/MyMultiverseApp/issues/31) | 1 | P1 | ✅ S11 |
| E1-7 | [#32](https://github.com/Rob971/MyMultiverseApp/issues/32) | 3 | P1 | ✅ S8 |
| E1-8 | — | 5 | P1 | ✅ S13 |
| E1-9 | — | 5 | P1 | ✅ S13 |
| E2-1 | [#33](https://github.com/Rob971/MyMultiverseApp/issues/33) | 0 | P0 | ✅ S2 |
| E2-2 | [#34](https://github.com/Rob971/MyMultiverseApp/issues/34) | 1 | P0 | ✅ S3 |
| E2-3 | [#35](https://github.com/Rob971/MyMultiverseApp/issues/35) | 0 | P0 | ✅ S2 |
| E2-4 | [#36](https://github.com/Rob971/MyMultiverseApp/issues/36) | 1 | P1 | ✅ S3 |
| E3-1 | [#37](https://github.com/Rob971/MyMultiverseApp/issues/37) | 0 | P0 | ✅ S1 |
| E3-2 | [#38](https://github.com/Rob971/MyMultiverseApp/issues/38) | 3 | P1 | ✅ S8 |
| E3-3 | [#39](https://github.com/Rob971/MyMultiverseApp/issues/39) | 3 | P1 | ✅ S9 |
| E3-4 | [#40](https://github.com/Rob971/MyMultiverseApp/issues/40) | 3 | P2 | ✅ S9 |
| E3-5 | [#41](https://github.com/Rob971/MyMultiverseApp/issues/41) | 1 | P1 | ✅ S3 |
| E3-6 | [#42](https://github.com/Rob971/MyMultiverseApp/issues/42) | 1 | P1 | ✅ S4 |
| E3-7 | [#43](https://github.com/Rob971/MyMultiverseApp/issues/43) | 3 | P2 | ✅ S10 |
| E3-9 | — | 3 | P1 | ⏳ S12 |
| E4-1 | [#44](https://github.com/Rob971/MyMultiverseApp/issues/44) | 2 | P0 | ✅ S5 |
| E4-2 | [#45](https://github.com/Rob971/MyMultiverseApp/issues/45) | 2 | P0 | ✅ S6 |
| E4-3 | [#46](https://github.com/Rob971/MyMultiverseApp/issues/46) | 2 | P0 | ✅ S6 |
| E4-4 | [#47](https://github.com/Rob971/MyMultiverseApp/issues/47) | 2 | P0 | ✅ S5 |
| E4-5 | [#48](https://github.com/Rob971/MyMultiverseApp/issues/48) | 2 | P1 | ✅ S7 |
| E4-6 | [#49](https://github.com/Rob971/MyMultiverseApp/issues/49) | 2 | P1 | ✅ S7 |
| E4-7 | [#50](https://github.com/Rob971/MyMultiverseApp/issues/50) | 2 | P1 | ✅ S7 |
| E4-8 | [#51](https://github.com/Rob971/MyMultiverseApp/issues/51) | 2 | P1 | ✅ S6 |
| E4-9 | [#52](https://github.com/Rob971/MyMultiverseApp/issues/52) | 4 | P2 | ✅ S12 |
| E4-10 | [#53](https://github.com/Rob971/MyMultiverseApp/issues/53) | 4 | P2 | ✅ S12 |
| E4-11 | [#54](https://github.com/Rob971/MyMultiverseApp/issues/54) | 4 | P2 | ✅ S12 |
| E5-1 | [#55](https://github.com/Rob971/MyMultiverseApp/issues/55) | 0 | P0 | ✅ S2 |
| E5-2 | [#56](https://github.com/Rob971/MyMultiverseApp/issues/56) | 3 | P1 | ✅ S10 |
| E5-3 | [#57](https://github.com/Rob971/MyMultiverseApp/issues/57) | 2 | P1 | ✅ S7 |
| E6-1 | [#58](https://github.com/Rob971/MyMultiverseApp/issues/58) | 3 | P1 | ✅ S10 |
| E6-2 | [#59](https://github.com/Rob971/MyMultiverseApp/issues/59) | 3 | P2 | ✅ S11 |
| E6-3 | [#60](https://github.com/Rob971/MyMultiverseApp/issues/60) | 0 | P0 | ✅ ongoing |
| E6-4 | — | 5 | P2 | ✅ S13 |
| E2-5 | [#61](https://github.com/Rob971/MyMultiverseApp/issues/61) | 4 | P3 | deferred |
| E2-6 | [#62](https://github.com/Rob971/MyMultiverseApp/issues/62) | 4 | P3 | deferred |
| E3-8 | [#63](https://github.com/Rob971/MyMultiverseApp/issues/63) | 4 | P3 | deferred |
| E5-4 | [#64](https://github.com/Rob971/MyMultiverseApp/issues/64) | 4 | P3 | deferred |
| E5-5 | [#65](https://github.com/Rob971/MyMultiverseApp/issues/65) | 4 | P3 | deferred |

**Recreate issues:** `./scripts/create-ux-backlog-issues.sh` (idempotent only if issues do not already exist).
