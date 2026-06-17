package app.mymultiverse.kmp.i18n

object AuthStringKeys {
    val all: Set<String> = setOf(
        "auth_title",
        "auth_subtitle",
        "auth_email_label",
        "auth_password_label",
        "auth_sign_in_button",
        "auth_sign_up_button",
        "auth_switch_to_sign_up",
        "auth_switch_to_sign_in",
        "auth_continue_google",
        "auth_continue_apple",
        "auth_provider_coming_soon",
        "auth_error_generic",
        "auth_error_config_missing",
        "auth_error_invalid_credentials",
        "auth_error_invalid_email",
        "auth_error_weak_password",
        "auth_error_user_already_exists",
        "auth_error_email_unconfirmed",
        "auth_error_signup_disabled",
        "auth_success_email_confirmation",
        "auth_loading",
        "auth_sign_out",
        "auth_pending_invites_title",
        "auth_pending_invites_subtitle",
        "auth_pending_invites_accept",
        "auth_pending_invites_decline",
        "auth_pending_invites_error_generic",
        "auth_pending_invites_email_mismatch",
        "auth_pending_invites_switch_title",
        "auth_pending_invites_switch_message",
        "auth_pending_invites_switch_confirm",
        "auth_pending_invites_switch_cancel",
        "auth_household_joined_success",
    )

    val localeDirectories: List<String> = NutritionStringKeys.localeDirectories
}
