import java.util.Properties
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val googleServicesFile = layout.projectDirectory.file("google-services.json").asFile
if (googleServicesFile.exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

val defaultSupabaseUrl = "https://ivjdzreazvkrrirecznk.supabase.co"

val appVersionProperties = Properties().apply {
    val versionFile = rootProject.file("gradle/app-version.properties")
    check(versionFile.exists()) { "Missing gradle/app-version.properties" }
    versionFile.inputStream().use { load(it) }
}
val appVersionLts = appVersionProperties.getProperty("version.lts")
val appVersionCandidate = appVersionProperties.getProperty("version.candidate", "0").toInt()
val appVersionCode = appVersionProperties.getProperty("version.code", "1").toInt()
val appVersionName = if (appVersionCandidate > 0) {
    val ltsParts = appVersionLts.split(".")
    val major = ltsParts.getOrElse(0) { "0" }
    val minor = ltsParts.getOrElse(1) { "0" }
    "$major.$minor.$appVersionCandidate"
} else {
    appVersionLts
}

val generateAppBuildInfo = tasks.register("generateAppBuildInfo") {
    val versionFile = rootProject.layout.projectDirectory.file("gradle/app-version.properties")
    val outputDir = layout.buildDirectory.dir("generated/appinfo/kotlin/app/mymultiverse/kmp/domain")

    inputs.file(versionFile).withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(outputDir)

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("AppBuildInfo.kt").writeText(
            """
            package app.mymultiverse.kmp.domain

            internal object AppBuildInfo {
                const val VERSION_NAME: String = ${appVersionName.quoteForKotlin()}
                const val VERSION_CODE: Int = $appVersionCode
                const val IS_RELEASE_CANDIDATE: Boolean = ${appVersionCandidate > 0}
            }
            """.trimIndent(),
        )
    }
}

val generateSupabaseSecrets = tasks.register("generateSupabaseSecrets") {
    val localPropsFile = rootProject.layout.projectDirectory.file("local.properties")
    val outputDir = layout.buildDirectory.dir("generated/supabase/kotlin/app/mymultiverse/kmp/data/supabase")

    inputs.files(localPropsFile).optional().withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(outputDir)

    doLast {
        val properties = Properties().apply {
            val file = localPropsFile.asFile
            if (file.exists()) {
                file.inputStream().use { load(it) }
            }
        }
        val supabaseUrl = properties.getProperty("supabase.url", defaultSupabaseUrl)
            .trim()
            .ifBlank { defaultSupabaseUrl }
        val supabaseAnonKey = properties.getProperty("supabase.anonKey", "").trim()

        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("SupabaseSecrets.kt").writeText(
            """
            package app.mymultiverse.kmp.data.supabase

            internal object SupabaseSecrets {
                const val URL: String = ${supabaseUrl.quoteForKotlin()}
                const val ANON_KEY: String = ${supabaseAnonKey.quoteForKotlin()}
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
            implementation(libs.firebase.crashlytics)
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
            implementation(libs.supabase.realtime)
            implementation(libs.kermit)
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
        kotlin.srcDir(layout.buildDirectory.dir("generated/appinfo/kotlin"))
    }
}

tasks.matching { it.name.contains("compile", ignoreCase = true) && it.name.contains("Kotlin") }
    .configureEach {
        dependsOn(generateSupabaseSecrets, generateAppBuildInfo)
    }

android {
    namespace = "app.mymultiverse.kmp"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.mymultiverse.kmp"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions {
        animationsDisabled = true
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
