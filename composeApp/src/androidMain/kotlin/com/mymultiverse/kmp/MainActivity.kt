package com.mymultiverse.kmp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.mymultiverse.kmp.presentation.App
import com.mymultiverse.kmp.presentation.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoinApplication(application = {
                androidContext(this@MainActivity)
                modules(appModule)
            }) {
                App()
            }
        }
    }
}
