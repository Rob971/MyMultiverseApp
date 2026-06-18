package app.mymultiverse.kmp.push

import android.content.Context

internal object PushTokenRefreshStore {
    private const val PREFS_NAME = "push_token_refresh"
    private const val KEY_TOKEN = "pending_fcm_token"

    fun save(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun consume(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        prefs.edit().remove(KEY_TOKEN).apply()
        return token
    }
}
