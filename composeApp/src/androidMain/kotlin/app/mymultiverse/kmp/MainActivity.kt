package app.mymultiverse.kmp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import app.mymultiverse.kmp.data.observability.FirebaseBuildFlags
import app.mymultiverse.kmp.data.platform.AndroidNotificationChannels
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
        val launchRedirectUrl = intent?.data?.toString()
            ?.takeIf(AuthRedirectEvents::isAuthRedirect)
        setContent {
            KoinApplication(application = {
                androidContext(this@MainActivity)
                modules(appModule)
            }) {
                App()
                if (launchRedirectUrl != null) {
                    LaunchedEffect(launchRedirectUrl) {
                        AuthRedirectEvents.emit(launchRedirectUrl)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deliverAuthRedirect(intent)
    }

    private fun deliverAuthRedirect(intent: Intent?) {
        intent?.data?.toString()?.let(AuthRedirectEvents::emit)
    }
}
