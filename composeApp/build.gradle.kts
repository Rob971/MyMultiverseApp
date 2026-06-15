import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val supabaseAnonKey = localProperties.getProperty("supabase.anonKey", "")

val generateSupabaseSecrets = tasks.register("generateSupabaseSecrets") {
    val outputDir = layout.buildDirectory.dir("generated/supabase/kotlin/app/mymultiverse/kmp/data/supabase")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("SupabaseSecrets.kt").writeText(
            """
            package app.mymultiverse.kmp.data.supabase

            internal object SupabaseSecrets {
                const val ANON_KEY: String = ${supabaseAnonKey.trim().quoteForKotlin()}
            }
            """.trimIndent(),
        )
    }
}

fun String.quoteForKotlin(): String = buildString {
    append('"')
    for (ch in this@quoteForKotlin) {
        when (ch) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(ch)
        }
    }
    append('"')
}

configurations.configureEach {
    resolutionStrategy {
        force("androidx.browser:browser:1.8.0")
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    }
}

kotlin {
    androidTarget {
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.androidx.appcompat)
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation("org.jetbrains.compose.ui:ui-backhandler:1.8.0")

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.coroutines)

            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.koin.test)
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.androidx.appcompat)
                implementation(kotlin("test"))
                implementation(libs.androidx.test.ext.junit)
                implementation(libs.androidx.test.runner)
                implementation(libs.androidx.compose.ui.test.junit4)
                implementation(libs.androidx.compose.ui.test.manifest)
                implementation(libs.koin.compose)
            }
        }
    }

    sourceSets.named("commonMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/supabase/kotlin"))
    }
}

tasks.matching { it.name.contains("compile", ignoreCase = true) && it.name.contains("Kotlin") }
    .configureEach {
        dependsOn(generateSupabaseSecrets)
    }

android {
    namespace = "app.mymultiverse.kmp"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.mymultiverse.kmp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Merges ComponentActivity into the debug app manifest for Compose UI tests.
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
