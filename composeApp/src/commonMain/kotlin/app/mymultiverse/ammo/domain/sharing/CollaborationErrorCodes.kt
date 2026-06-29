package app.mymultiverse.ammo.domain.sharing

object CollaborationErrorCodes {
    const val MEMBER_EMAIL_REQUIRED = "member_email_required"
    const val MEMBER_CANNOT_ADD_SELF = "member_cannot_add_self"
    const val MEMBER_ALREADY_EXISTS = "member_already_exists"
    const val INSUFFICIENT_ROLE = "insufficient_role"
    const val PROFILE_EMAIL_REQUIRED = "profile_email_required"
    const val INVITE_EMAIL_MISMATCH = "invite_email_mismatch"
    const val INVITE_TOKEN_REQUIRED = "invite_token_required"
    const val INVITE_NOT_FOUND = "invite_not_found"
    const val INVITE_DECLINED = "invite_declined"
    const val INVITE_ALREADY_ACCEPTED = "invite_already_accepted"
    const val INVITE_EXPIRED = "invite_expired"
    const val HOUSEHOLD_ALREADY_ACTIVE = "household_already_active"
    const val INVITEE_HOUSEHOLD_ALREADY_ACTIVE = "invitee_household_already_active"
    const val HOUSEHOLD_MEMBER_LIMIT_REACHED = "household_member_limit_reached"
    const val OWNER_MUST_TRANSFER_OR_DISSOLVE = "owner_must_transfer_or_dissolve"
    const val HOUSEHOLD_NOT_FOUND = "household_not_found"
    const val INVALID_TRANSFER_TARGET = "invalid_transfer_target"
    const val TRANSFER_TARGET_NOT_MEMBER = "transfer_target_not_member"
    const val SUPABASE_NOT_CONFIGURED = "supabase_not_configured"
    const val HOUSEHOLD_NAME_TAKEN = "household_name_taken"
    const val HOUSEHOLD_NAME_REQUIRED = "household_name_required"
    const val CANNOT_ASSIGN_OWNER_ROLE = "cannot_assign_owner_role"
    const val MEMBER_NOT_FOUND = "member_not_found"

    fun messageContains(code: String, message: String?): Boolean =
        message?.contains(code, ignoreCase = true) == true
}
