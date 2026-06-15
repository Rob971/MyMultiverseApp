package app.mymultiverse.kmp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import app.mymultiverse.kmp.data.supabase.SupabaseClientFactory
import app.mymultiverse.kmp.presentation.App
import app.mymultiverse.kmp.presentation.di.appModule
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deliverAuthRedirect(intent)
        setContent {
            KoinApplication(application = {
                androidContext(this@MainActivity)
                modules(appModule)
            }) {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deliverAuthRedirect(intent)
    }

    private fun deliverAuthRedirect(intent: Intent?) {
        val client = SupabaseClientFactory.createOrNull() ?: return
        intent?.let { client.handleDeeplinks(it) }
    }
}
