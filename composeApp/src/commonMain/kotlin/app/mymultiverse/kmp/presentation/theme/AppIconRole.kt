package app.mymultiverse.kmp.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Semantic icon roles — each maps to a vector in [AppIcons] and a theme-aware tint via [resolveTint].
 * Use [app.mymultiverse.kmp.presentation.components.JourneyIcon] in UI instead of raw [androidx.compose.material3.Icon].
 */
enum class AppIconRole {
    NavHome,
    NavMealPlan,
    NavGrocery,
    ChromeBack,
    ChromeClose,
    ChromeOverflow,
    ChromeChevronRight,
    ChromeChevronLeft,
    ChromeExpand,
    Language,
    Account,
    ActionAdd,
    ActionEdit,
    ActionDelete,
    ActionConfirm,
    GroceryChecked,
    GroceryUnchecked,
    AiAccent,
    SyncIdle,
    SyncPending,
    SyncOffline,
    SyncSuccess,
    Hint,
    KeepScreenOn,
    PantryHave,
    FeatureAccent,
    Muted,
    Primary,
    Destructive,
    DragHandle,
    MealSlot,
    ComingSoonExplore,
    ComingSoonBudget,
    Household,
    InviteMember,
    OnAccent,
}

fun AppIconRole.imageVector(): ImageVector = when (this) {
    AppIconRole.NavHome -> AppIcons.Home
    AppIconRole.NavMealPlan -> AppIcons.MealPlan
    AppIconRole.NavGrocery -> AppIcons.GroceryList
    AppIconRole.ChromeBack -> AppIcons.ArrowBack
    AppIconRole.ChromeClose -> AppIcons.Close
    AppIconRole.ChromeOverflow -> AppIcons.MoreVert
    AppIconRole.ChromeChevronRight -> AppIcons.ChevronRight
    AppIconRole.ChromeChevronLeft -> AppIcons.ChevronLeft
    AppIconRole.ChromeExpand -> AppIcons.KeyboardArrowDown
    AppIconRole.Language -> AppIcons.Language
    AppIconRole.Account -> AppIcons.Person
    AppIconRole.ActionAdd -> AppIcons.Add
    AppIconRole.ActionEdit -> AppIcons.Edit
    AppIconRole.ActionDelete -> AppIcons.Delete
    AppIconRole.ActionConfirm -> AppIcons.Check
    AppIconRole.GroceryChecked -> AppIcons.CheckCircle
    AppIconRole.GroceryUnchecked -> AppIcons.RadioButtonUnchecked
    AppIconRole.AiAccent -> AppIcons.Sparkles
    AppIconRole.SyncIdle -> AppIcons.SyncPending
    AppIconRole.SyncPending -> AppIcons.SyncPending
    AppIconRole.SyncOffline -> AppIcons.SyncOffline
    AppIconRole.SyncSuccess -> AppIcons.CheckCircle
    AppIconRole.Hint -> AppIcons.Lightbulb
    AppIconRole.KeepScreenOn -> AppIcons.KeepScreenOn
    AppIconRole.PantryHave -> AppIcons.PantryHave
    AppIconRole.MealSlot -> AppIcons.Restaurant
    AppIconRole.ComingSoonExplore -> AppIcons.Explore
    AppIconRole.ComingSoonBudget -> AppIcons.AccountBalance
    AppIconRole.Household -> AppIcons.Household
    AppIconRole.InviteMember -> AppIcons.PersonAdd
    AppIconRole.DragHandle -> AppIcons.DragHandle
    AppIconRole.FeatureAccent,
    AppIconRole.Muted,
    AppIconRole.Primary,
    AppIconRole.Destructive,
    AppIconRole.OnAccent,
    -> error("Role $this requires an explicit imageVector")
}

@Composable
fun AppIconRole.resolveTint(accentColor: Color? = null): Color = when (this) {
    AppIconRole.NavHome,
    AppIconRole.NavMealPlan,
    AppIconRole.NavGrocery,
    -> JourneySemanticColors.inkMuted()

    AppIconRole.ChromeBack,
    AppIconRole.Language,
    AppIconRole.Primary,
    AppIconRole.ActionAdd,
    AppIconRole.ActionEdit,
    AppIconRole.SyncIdle,
    AppIconRole.SyncPending,
    AppIconRole.KeepScreenOn,
    AppIconRole.Hint,
    -> JourneySemanticColors.brandTeal()

    AppIconRole.ChromeClose,
    AppIconRole.ChromeOverflow,
    AppIconRole.Muted,
    AppIconRole.ChromeExpand,
    AppIconRole.GroceryUnchecked,
    AppIconRole.ComingSoonExplore,
    AppIconRole.ComingSoonBudget,
    AppIconRole.Household,
    AppIconRole.SyncOffline,
    -> JourneySemanticColors.inkMuted()

    AppIconRole.DragHandle,
    -> JourneySemanticColors.inkSecondary()

    AppIconRole.InviteMember -> JourneySemanticColors.brandTeal()

    AppIconRole.ChromeChevronRight,
    AppIconRole.ChromeChevronLeft,
    -> JourneySemanticColors.inkSecondary()

    AppIconRole.Account -> JourneySemanticColors.brandTeal()

    AppIconRole.ActionDelete,
    AppIconRole.Destructive,
    -> JourneySemanticColors.brandTerracotta()

    AppIconRole.ActionConfirm,
    AppIconRole.GroceryChecked,
    AppIconRole.SyncSuccess,
    AppIconRole.PantryHave,
    -> JourneySemanticColors.successAccent()

    AppIconRole.AiAccent -> JourneySemanticColors.brandTerracotta()

    AppIconRole.FeatureAccent,
    AppIconRole.MealSlot,
    -> accentColor ?: JourneySemanticColors.brandTeal()

    AppIconRole.OnAccent -> JourneySemanticColors.onAccentButton()
}
