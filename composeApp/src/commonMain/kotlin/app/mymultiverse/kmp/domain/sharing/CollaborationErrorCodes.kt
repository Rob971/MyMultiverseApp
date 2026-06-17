package app.mymultiverse.kmp.domain.sharing

object CollaborationErrorCodes {
    const val MEMBER_EMAIL_REQUIRED = "member_email_required"
    const val MEMBER_CANNOT_ADD_SELF = "member_cannot_add_self"
    const val MEMBER_ALREADY_EXISTS = "member_already_exists"
    const val INSUFFICIENT_ROLE = "insufficient_role"
    const val PROFILE_EMAIL_REQUIRED = "profile_email_required"
    const val INVITE_EMAIL_MISMATCH = "invite_email_mismatch"
    const val INVITE_NOT_FOUND = "invite_not_found"
    const val HOUSEHOLD_ALREADY_ACTIVE = "household_already_active"
    const val SUPABASE_NOT_CONFIGURED = "supabase_not_configured"

    fun messageContains(code: String, message: String?): Boolean =
        message?.contains(code, ignoreCase = true) == true
}
