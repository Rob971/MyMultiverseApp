package app.mymultiverse.ammo

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import app.mymultiverse.ammo.di.androidFirebasePlatformModule
import app.mymultiverse.ammo.data.observability.FirebaseBuildFlags
import app.mymultiverse.ammo.data.platform.AndroidNotificationChannels
import app.mymultiverse.ammo.data.invite.InviteRedirectEvents
import app.mymultiverse.ammo.data.invite.InviteRedirectUrls
import app.mymultiverse.ammo.data.invite.extractInvitePushRedirectUrl
import app.mymultiverse.ammo.data.supabase.AuthRedirectEvents
import app.mymultiverse.ammo.push.InvitePushIntentDelivery
import app.mymultiverse.ammo.presentation.App
import app.mymultiverse.ammo.presentation.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (FirebaseBuildFlags.PUSH_ENABLED) {
            AndroidNotificationChannels.ensureCreated(this)
            InvitePushIntentDelivery.deliverIntentPayload(intent)
        }
        val launchAuthRedirectUrl = intent?.data?.toString()
            ?.takeIf(AuthRedirectEvents::isAuthRedirect)
        val launchInviteRedirectUrl = intent?.data?.toString()
            ?.takeIf(InviteRedirectUrls::isInviteRedirect)
            ?: intent?.extractInvitePushRedirectUrl()
        setContent {
            KoinApplication(application = {
                androidContext(this@MainActivity)
                modules(
                    buildList {
                        add(appModule)
                        if (FirebaseBuildFlags.PUSH_ENABLED) {
                            add(androidFirebasePlatformModule())
                        }
                    },
                )
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
        InvitePushIntentDelivery.deliverIntentPayload(intent)
    }
}
