package app.mymultiverse.ammo.data.invite

object InvitePushPayload {
    const val TYPE_HOUSEHOLD_INVITE = "household_invite"
    const val TYPE_MEMBER_JOINED = "household_member_joined"
    const val TYPE_GROCERY_LIST_NUDGE = "grocery_list_nudge"
    const val TYPE_MEAL_PLAN_NUDGE = "meal_plan_nudge"
    const val TYPE_GROCERY_ITEM_ADDED = "grocery_item_added"
    const val TYPE_MEAL_PLAN_ITEM_ADDED = "meal_plan_item_added"
    const val KEY_TYPE = "type"
    const val KEY_INVITE_TOKEN = "invite_token"
    const val KEY_HOUSEHOLD_ID = "household_id"
    const val KEY_MEMBER_NAME = "member_name"
    const val KEY_NUDGER_NAME = "nudger_name"
    const val KEY_ACTOR_NAME = "actor_name"
    const val KEY_WEEK_KEY = "week_key"
    const val KEY_ITEM_LABEL = "item_label"
    const val KEY_ADDED_COUNT = "added_count"
    const val KEY_DAY_INDEX = "day_index"
    const val KEY_MEAL_SLOT = "meal_slot"

    fun inviteTokenFromData(data: Map<String, String>): String? {
        if (data[KEY_TYPE] != TYPE_HOUSEHOLD_INVITE) return null
        return data[KEY_INVITE_TOKEN]?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun memberJoinedHouseholdIdFromData(data: Map<String, String>): String? {
        if (data[KEY_TYPE] != TYPE_MEMBER_JOINED) return null
        return data[KEY_HOUSEHOLD_ID]?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun groceryListNudgeFromData(data: Map<String, String>): GroceryListNudgePush? {
        if (data[KEY_TYPE] != TYPE_GROCERY_LIST_NUDGE) return null
        val householdId = data[KEY_HOUSEHOLD_ID]?.trim().orEmpty()
        if (householdId.isEmpty()) return null
        return GroceryListNudgePush(
            householdId = householdId,
            weekKey = data[KEY_WEEK_KEY]?.trim().orEmpty(),
            nudgerName = data[KEY_NUDGER_NAME]?.trim().orEmpty(),
        )
    }

    fun groceryItemAddedFromData(data: Map<String, String>): GroceryItemAddedPush? {
        if (data[KEY_TYPE] != TYPE_GROCERY_ITEM_ADDED) return null
        val householdId = data[KEY_HOUSEHOLD_ID]?.trim().orEmpty()
        if (householdId.isEmpty()) return null
        return GroceryItemAddedPush(
            householdId = householdId,
            weekKey = data[KEY_WEEK_KEY]?.trim().orEmpty(),
            actorName = data[KEY_ACTOR_NAME]?.trim().orEmpty(),
            itemLabel = data[KEY_ITEM_LABEL]?.trim().orEmpty(),
            addedCount = data[KEY_ADDED_COUNT]?.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        )
    }

    fun mealPlanItemAddedFromData(data: Map<String, String>): MealPlanItemAddedPush? {
        if (data[KEY_TYPE] != TYPE_MEAL_PLAN_ITEM_ADDED) return null
        val householdId = data[KEY_HOUSEHOLD_ID]?.trim().orEmpty()
        if (householdId.isEmpty()) return null
        return MealPlanItemAddedPush(
            householdId = householdId,
            weekKey = data[KEY_WEEK_KEY]?.trim().orEmpty(),
            actorName = data[KEY_ACTOR_NAME]?.trim().orEmpty(),
            itemLabel = data[KEY_ITEM_LABEL]?.trim().orEmpty(),
            addedCount = data[KEY_ADDED_COUNT]?.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            dayIndex = data[KEY_DAY_INDEX]?.toIntOrNull() ?: 0,
            mealSlot = data[KEY_MEAL_SLOT]?.trim().orEmpty(),
        )
    }

    fun inviteRedirectUrlFromData(data: Map<String, String>): String? {
        val token = inviteTokenFromData(data) ?: return null
        return InviteRedirectUrls.build(token)
    }

    fun deliverFromPushData(data: Map<String, String>) {
        inviteRedirectUrlFromData(data)?.let(InviteRedirectEvents::emit)
        memberJoinedHouseholdIdFromData(data)?.let(HouseholdPushEvents::emitMemberJoined)
        groceryListNudgeFromData(data)?.let(HouseholdPushEvents::emitGroceryListNudge)
        groceryItemAddedFromData(data)?.let(HouseholdPushEvents::emitGroceryItemAdded)
        mealPlanItemAddedFromData(data)?.let(HouseholdPushEvents::emitMealPlanItemAdded)
    }
}
