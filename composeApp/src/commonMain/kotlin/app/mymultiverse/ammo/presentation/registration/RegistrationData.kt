package app.mymultiverse.ammo.presentation.registration

/**
 * Lightweight in-memory holder that carries data from the 2-step registration
 * flow into the post-auth household setup screen. Cleared once the household is
 * created so subsequent navigations start fresh.
 */
class RegistrationData {
    /** Household name entered on step 2 of registration, or null if skipped. */
    var pendingHouseholdName: String? = null

    fun clear() {
        pendingHouseholdName = null
    }
}
