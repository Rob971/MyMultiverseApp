package app.mymultiverse.ammo.presentation.screens.auth

internal fun LoginError.affectsEmailField(): Boolean = when (this) {
    LoginError.InvalidEmail,
    LoginError.UserAlreadyExists,
    LoginError.EmailNotConfirmed,
    -> true
    else -> false
}

internal fun LoginError.affectsPasswordField(): Boolean = when (this) {
    LoginError.WeakPassword,
    LoginError.InvalidCredentials,
    -> true
    else -> false
}

internal fun LoginError.isScreenLevelOnly(): Boolean = when (this) {
    LoginError.Generic,
    LoginError.ConfigMissing,
    LoginError.ProviderComingSoon,
    LoginError.SignUpDisabled,
    -> true
    else -> false
}
