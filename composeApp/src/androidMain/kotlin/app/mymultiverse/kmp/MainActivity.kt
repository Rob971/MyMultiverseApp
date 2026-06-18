package app.mymultiverse.kmp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import app.mymultiverse.kmp.data.observability.FirebaseBuildFlags
import app.mymultiverse.kmp.data.platform.AndroidNotificationChannels
import app.mymultiverse.kmp.data.invite.InviteRedirectEvents
import app.mymultiverse.kmp.data.invite.InviteRedirectUrls
import app.mymultiverse.kmp.data.invite.extractInvitePushRedirectUrl
import app.mymultiverse.kmp.data.supabase.AuthRedirectEvents
import app.mymultiverse.kmp.presentation.App
import app.mymultiverse.kmp.presentation.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseBuildFlags.PUSH_ENABLED) {
            AndroidNotificationChannels.ensureCreated(this)
        }
        val launchAuthRedirectUrl = intent?.data?.toString()
            ?.takeIf(AuthRedirectEvents::isAuthRedirect)
        val launchInviteRedirectUrl = intent?.data?.toString()
            ?.takeIf(InviteRedirectUrls::isInviteRedirect)
            ?: intent?.extractInvitePushRedirectUrl()
        setContent {
            KoinApplication(application = {
                androidContext(this@MainActivity)
                modules(appModule)
            }) {
                App()
                if (launchAuthRedirectUrl != null) {
                    LaunchedEffect(launchAuthRedirectUrl) {
                        AuthRedirectEvents.emit(launchAuthRedirectUrl)
                    }
                }
                if (launchInviteRedirectUrl != null) {
                    LaunchedEffect(launchInviteRedirectUrl) {
                        InviteRedirectEvents.emit(launchInviteRedirectUrl)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deliverAuthRedirect(intent)
        deliverInviteRedirect(intent)
    }

    private fun deliverAuthRedirect(intent: Intent?) {
        intent?.data?.toString()?.let(AuthRedirectEvents::emit)
    }

    private fun deliverInviteRedirect(intent: Intent?) {
        intent?.data?.toString()
            ?.takeIf(InviteRedirectUrls::isInviteRedirect)
            ?.let(InviteRedirectEvents::emit)
        intent?.extractInvitePushRedirectUrl()?.let(InviteRedirectEvents::emit)
    }
}
