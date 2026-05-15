package com.example.kmp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.example.kmp.presentation.App
import com.example.kmp.presentation.di.appModule
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
