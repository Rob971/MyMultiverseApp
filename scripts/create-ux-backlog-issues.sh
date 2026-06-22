#!/usr/bin/env bash
# Creates GitHub issues from docs/product-backlog.md
# Run from repo root: ./scripts/create-ux-backlog-issues.sh
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DOC="docs/product-backlog.md"

create_issue() {
  local title="$1"
  local labels="$2"
  local body="$3"
  gh issue create --title "$title" --label "$labels" --body "$body"
}

DOC_LINK="See [\`$DOC\`](https://github.com/Rob971/MyMultiverseApp/blob/main/$DOC) on \`main\` (or current branch)."

echo "Creating meta issue..."
META_URL=$(create_issue \
  "[Meta] Product UX backlog — nutrition logistics & silent-butler AI" \
  "documentation,ux-backlog" \
 "$(cat <<EOF
## Summary

Master tracker for the product UX backlog: paper-easy grocery, safe sharing, weekly habit, and silent-butler AI.

**Doc:** \`$DOC\`

## North star

Families plan dinner and shop groceries with less effort than paper and texting — together, every week.

## Phases

- **Phase 0** — Grandma adds milk (weeks 1–2)
- **Phase 1** — Paper at the store (weeks 3–4)
- **Phase 2** — Silent butler AI (weeks 5–7)
- **Phase 3** — Family in the loop (weeks 8–10)
- **Phase 4+** — Ghost magic / bigger bets (optional)

Child issues use labels \`epic-e*\` and \`phase-*\`. Search: \`label:ux-backlog\`.

$DOC_LINK
EOF
)")
echo "Meta: $META_URL"

declare -a ISSUE_URLS=()

issue() {
  local id="$1"
  local title="$2"
  local epic="$3"
  local phase="$4"
  local priority="$5"
  local effort="$6"
  local touchpoints="$7"
  local acceptance="$8"
  local url
  url=$(create_issue \
    "[$id] $title" \
    "enhancement,ux-backlog,$epic,$phase" \
 "$(cat <<EOF
**Epic:** $epic · **Phase:** $phase · **Priority:** $priority · **Effort:** $effort

## Story

$title

## Touchpoints

$touchpoints

## Acceptance criteria

$acceptance

## Definition of done

- [ ] Journey components; 48dp targets; semantic colors
- [ ] 8-locale strings if copy changes; locale parity tests
- [ ] Unit / instrumented tests as appropriate
- [ ] Firebase QA YAML bumped if user-facing flow changes
- [ ] \`:composeApp:testDebugUnitTest\` green

$DOC_LINK
EOF
)")
  ISSUE_URLS+=("$id|$url")
  echo "$id -> $url"
}

echo ""
echo "Creating story issues..."

# E1
issue "E1-1" "Plain-language copy pass (family not household; no backend jargon)" "epic-e1" "phase-0" "P0" "S" \
  "All 8 \`composeResources/values*/strings.xml\`; locale parity tests" \
  "- [ ] User-facing strings avoid household/Supabase/sync jargon where plain words work
- [ ] Keys updated in all 8 locales
- [ ] Locale parity tests pass"

issue "E1-2" "Tonight's dinner on Today tab" "epic-e1" "phase-0" "P0" "S" \
  "\`HomeScreen\`, \`HomeScreenModel\`, \`nutritionSummary\`" \
  "- [ ] Today shows tonight's meal when planned
- [ ] Tap opens Plan tab with today expanded
- [ ] Stable testTag on Today"

issue "E1-3" "Solo onboarding: one-tap create family + show bottom tabs" "epic-e1" "phase-0" "P0" "M" \
  "\`HomeScreen\` onboarding, \`App.kt\` \`showBottomBar\`" \
  "- [ ] User can create household in one tap with sensible default name
- [ ] Bottom tabs visible immediately after create (not blocked on invite)
- [ ] Onboarding loading/error still use Journey patterns"

issue "E1-4" "Post-create: focus Groceries tab or Add first item on Today" "epic-e1" "phase-0" "P0" "S" \
  "\`App.kt\` tab state, \`HomeScreen\`" \
  "- [ ] After first household create, user lands on Groceries or sees prominent add-first-item CTA
- [ ] No extra navigation steps to add first item"

issue "E1-5" "Empty grocery CTA on Today when list empty" "epic-e1" "phase-1" "P1" "S" \
  "\`HomeScreen\` This week section" \
  "- [ ] Today shows grocery empty CTA when list has zero items
- [ ] CTA opens Groceries tab or input"

issue "E1-6" "Week context banner on Today" "epic-e1" "phase-1" "P1" "S" \
  "\`HomeScreen\`, \`WeekContextBanner\` pattern" \
  "- [ ] Today shows current week range label
- [ ] Matches nutrition week key"

issue "E1-7" "Sunday empty-week nudge on Today" "epic-e1" "phase-3" "P1" "M" \
  "\`HomeScreenModel\`, \`WeekCalendar\`" \
  "- [ ] When new week has no meal plan, Today shows gentle plan-week nudge
- [ ] Dismissible; not blocking"

# E2
issue "E2-1" "Wakelock: Keep screen on while shopping" "epic-e2" "phase-0" "P0" "S" \
  "Platform \`expect/actual\`; Groceries screen or settings" \
  "- [ ] Setting or Groceries option keeps screen awake while list open
- [ ] Android implemented; iOS best-effort documented
- [ ] Does not drain battery when user leaves Groceries"

issue "E2-2" "Swipe-to-check on grocery rows" "epic-e2" "phase-1" "P0" "M" \
  "\`GroceryItemRow.kt\`" \
  "- [ ] Wide swipe gesture checks/unchecks item
- [ ] Haptic on check (Android + iOS)
- [ ] Delete still available without conflicting gesture (edit or alternate swipe)"

issue "E2-3" "Wire iOS haptics on grocery check-off" "epic-e2" "phase-0" "P0" "S" \
  "\`JourneyHaptics.ios.kt\`" \
  "- [ ] iOS performs light haptic on grocery toggle
- [ ] Parity with Android behavior"

issue "E2-4" "Shopping mode: unchecked first; hide checked optional" "epic-e2" "phase-1" "P1" "M" \
  "\`GroceryShoppingScreen\`, \`GroceryListPresentation\`" \
  "- [ ] Unchecked items appear first in shopping mode
- [ ] Optional toggle to hide checked items
- [ ] Wide layout still works"

# E3
issue "E3-1" "Friendlier sync copy + status icon" "epic-e3" "phase-0" "P0" "S" \
  "\`NutritionSyncStatusBanner.kt\`, string keys" \
  "- [ ] Plain-language sync messages (no technical jargon)
- [ ] Subtle pending/synced visual indicator
- [ ] 8 locales"

issue "E3-2" "Invite share sheet with pre-written message" "epic-e3" "phase-3" "P1" "M" \
  "\`HouseholdMembersScreen\`" \
  "- [ ] After invite sent, user can share via system sheet
- [ ] Message includes deep link and friendly copy
- [ ] 8 locales for share template"

issue "E3-3" "Push: invite received / member joined" "epic-e3" "phase-3" "P1" "L" \
  "\`InvitePushPayload\`, \`PlatformPushSetup\`, edge" \
  "- [ ] Push notifies invitee of pending invite
- [ ] Push notifies household when member joins
- [ ] Deep link opens correct flow"

issue "E3-4" "Activity snackbar (partner added item)" "epic-e3" "phase-3" "P2" "M" \
  "\`NutritionScreenModel\`, sync/realtime" \
  "- [ ] Debounced snackbar when remote member adds/checks item
- [ ] Non-intrusive; no chat UI"

issue "E3-5" "Viewer read-only banner on Groceries + Plan tabs" "epic-e3" "phase-1" "P1" "S" \
  "\`HouseholdViewerReadOnlyNotice\` on tab roots" \
  "- [ ] Viewer sees banner on Groceries and Plan without attempting edit
- [ ] Write actions remain disabled"

issue "E3-6" "Skip redundant NutritionEntryGate when household resolved" "epic-e3" "phase-1" "P1" "S" \
  "\`NutritionFlow.kt\`, \`App.kt\`" \
  "- [ ] Plan/Groceries tabs open without extra loading gate when \`resolvedHousehold\` exists
- [ ] Gate still shown when household truly missing"

issue "E3-7" "Silent duplicate grocery merge" "epic-e3" "phase-3" "P2" "M" \
  "Domain + \`GroceryShoppingScreen\`" \
  "- [ ] Adding duplicate label merges or increments instead of error-only path
- [ ] Unit tests for merge rules"

# E4
issue "E4-1" "AiHelperSheet bottom sheet (~60% height)" "epic-e4" "phase-2" "P0" "L" \
  "New composable; \`NutritionScreenModel.runAiAssistant()\`; pattern \`HomeAccountSheet\`" \
  "- [ ] Sheet opens over Plan/Grocery; list visible behind
- [ ] Dismisses on apply or swipe down
- [ ] Viewer read-only respected
- [ ] testTags for sheet and primary actions"

issue "E4-2" "Inline empty meal slot: Suggest 20-min meal" "epic-e4" "phase-2" "P0" "M" \
  "\`MealPlanDayCard\`" \
  "- [ ] Empty lunch/dinner slot shows tertiary Sparkles action
- [ ] Opens \`AiHelperSheet\` with meal context for that day
- [ ] 8 locales"

issue "E4-3" "Grocery chip: Build list from this week's meals" "epic-e4" "phase-2" "P0" "M" \
  "\`GroceryShoppingScreen\`" \
  "- [ ] Chip visible when grocery empty or at top of list
- [ ] Opens sheet in grocery mode from meal plan data
- [ ] Explicit adopt to editable list"

issue "E4-4" "AI copy rewrite — output-first, no chatbot tone" "epic-e4" "phase-2" "P0" "M" \
  "\`nutrition_ai_*\` keys, all locales" \
  "- [ ] Remove conversational greetings and marketing fluff
- [ ] Loading/result strings are brief and functional
- [ ] Locale parity tests pass"

issue "E4-5" "Chip-first AI sheet; text field behind More options" "epic-e4" "phase-2" "P1" "M" \
  "\`AiHelperSheet\`" \
  "- [ ] Primary flow uses chips only
- [ ] Free-text criteria collapsed by default
- [ ] Multi-chip selection supported"

issue "E4-6" "AI mode from launch context (hide mode switcher)" "epic-e4" "phase-2" "P1" "M" \
  "Sheet launch params" \
  "- [ ] Entry point sets mode (meal/grocery/advice)
- [ ] Default UX hides Advice/Grocery/Meal plan tabs"

issue "E4-7" "Demote full-screen NutritionAiAdviceScreen to fallback" "epic-e4" "phase-2" "P1" "S" \
  "\`NutritionFlow\`, \`WeeklyMealPlanScreen\`" \
  "- [ ] Primary paths use sheet only
- [ ] Full screen retained for deep links/tests if needed"

issue "E4-8" "Terracotta AI chips in read-only zones" "epic-e4" "phase-2" "P1" "S" \
  "\`AiGrocerySuggestionChips\`, design system" \
  "- [ ] AI-origin chips use terracotta not teal in read-only sections
- [ ] Aligns with \`ui-ux-compose.mdc\`"

issue "E4-9" "Ghost pairing banner (e.g. taco fixings)" "epic-e4" "phase-4" "P2" "M" \
  "\`JourneyBanner\` + domain helper" \
  "- [ ] Related items trigger dismissible add-more banner
- [ ] One tap adds suggested items"

issue "E4-10" "Pantry check section on meal to grocery" "epic-e4" "phase-4" "P2" "M" \
  "Meal→grocery flow" \
  "- [ ] Staples grouped under Check if you have these
- [ ] Not mixed into primary shopping list by default"

issue "E4-11" "Contextual AI chips from history (Use up chicken)" "epic-e4" "phase-4" "P2" "L" \
  "Domain + sheet" \
  "- [ ] Chips derived from recent meals or list items
- [ ] No keyboard required"

# E5
issue "E5-1" "Hero CTA per meal: Add to grocery list" "epic-e5" "phase-0" "P0" "S" \
  "\`MealPlanDayCard\`, \`WeeklyMealPlanScreen\`" \
  "- [ ] Prominent primary action per planned meal
- [ ] Uses existing generate-grocery path
- [ ] Snackbar/undo where applicable"

issue "E5-2" "Sheet flow: meal suggest → add → ingredients" "epic-e5" "phase-3" "P1" "M" \
  "E4 + E5 integration" \
  "- [ ] End-to-end from inline suggest to grocery ingredients in sheet
- [ ] Sheet dismisses after apply"

issue "E5-3" "Plan empty state: contextual AI chips open sheet" "epic-e5" "phase-2" "P1" "M" \
  "\`MealPlanEmptyState\`" \
  "- [ ] Chips like Under 30 min, Kid-approved on empty plan
- [ ] Open sheet not full AI screen"

# E6
issue "E6-1" "System font scaling on grocery + meal plan" "epic-e6" "phase-3" "P1" "M" \
  "\`GroceryItemRow\`, \`MealPlanDayCard\`" \
  "- [ ] Layout readable at largest system font
- [ ] No clipped text on Welcome cards (dark mode)"

issue "E6-2" "contentDescription audit: tabs + AI entry" "epic-e6" "phase-3" "P2" "S" \
  "\`MainTabShell\`, AI triggers" \
  "- [ ] Tab bar icons have meaningful descriptions
- [ ] AI entry points accessible"

issue "E6-3" "Firebase QA YAML for Phase 0–2 flows" "epic-e6" "phase-0" "P0" "S" \
  "\`firebase-appdistribution-testcases.yaml\`" \
  "- [ ] Bump YAML version
- [ ] Manual cases for tonight's dinner, wakelock, AI sheet when shipped"

# E7 deferred
issue "E2-5" "[Deferred] Auto-aisle grocery categorization" "epic-e7" "phase-4" "P3" "L" \
  "Future grocery domain" \
  "- [ ] Spike only until Phase 0–2 metrics green"

issue "E2-6" "[Deferred] Smart paste / recipe ingredient parse" "epic-e7" "phase-4" "P3" "L" \
  "Future AI + grocery" \
  "- [ ] Spike only"

issue "E3-8" "[Deferred] Live shopping presence" "epic-e7" "phase-4" "P3" "L" \
  "Realtime presence" \
  "- [ ] Spike only"

issue "E5-4" "[Deferred] Predictive replenishment" "epic-e7" "phase-4" "P3" "L" \
  "History + notifications" \
  "- [ ] Requires 3–4 weeks household history"

issue "E5-5" "[Deferred] Pantry memory for AI meals" "epic-e7" "phase-4" "P3" "L" \
  "AI + persistence" \
  "- [ ] Spike only"

echo ""
echo "Done. Meta issue: $META_URL"
echo "Story issues created: ${#ISSUE_URLS[@]}"
echo "Create epic trackers separately (see docs/product-backlog.md GitHub tracking section)."
