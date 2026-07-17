import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

val googleServicesFile = layout.projectDirectory.file("google-services.json").asFile
val firebaseCrashlyticsEnabled = googleServicesFile.exists()
if (firebaseCrashlyticsEnabled) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)
}

val appVersionProperties = Properties().apply {
    val versionFile = rootProject.file("gradle/app-version.properties")
    check(versionFile.exists()) { "Missing gradle/app-version.properties" }
    versionFile.inputStream().use { load(it) }
}
val appVersionNameBase = appVersionProperties.getProperty("version.name")
    ?: appVersionProperties.getProperty("version.lts")
val appVersionPrerelease = appVersionProperties.getProperty("version.prerelease", "").trim()
val appVersionCode = appVersionProperties.getProperty("version.code", "1").toInt()
val appVersionName = if (appVersionPrerelease.isEmpty()) {
    appVersionNameBase
} else {
    "$appVersionNameBase-$appVersionPrerelease"
}

// Track-encoded version code for Play Store upload uniqueness.
//
// In CI: GITHUB_RUN_NUMBER is the monotonically-increasing counter.
//
// Release tracks (beta, production) use a dense formula so Play Store versionCodes are ordered:
//   beta versionCode < production versionCode (within the same run: beta = N*3+1, prod = N*3+2)
//
// Debug/alpha builds use a separate range starting at 1_000_000 + run_number so that:
//   - A tester who has beta installed (≤ ~120_000 for years) can always install a debug alpha
//     (≥ 1_000_000) without an Android versionCode downgrade block.
//   - Debug builds do NOT go to Play Store; the separation only affects Firebase App Distribution.
//   - To revert to beta/production after installing a debug build, the user must uninstall first
//     (standard developer workflow; debug and release are effectively separate app slots).
//
// Locally (no GITHUB_RUN_NUMBER): falls back to the raw appVersionCode from properties so
// local debug builds and IDE runs are unaffected.
val ciRunNumber: Long? = System.getenv("GITHUB_RUN_NUMBER")?.trim()?.toLongOrNull()
val releaseTrack: String = System.getenv("RELEASE_TRACK")?.trim()?.lowercase() ?: ""
val trackOffset: Long = when (releaseTrack) {
    "beta"       -> 1L
    "production" -> 2L
    else         -> 0L
}
val versionCodeFinal: Int = if (ciRunNumber != null) {
    when (releaseTrack) {
        "beta", "production" ->
            (100_000L + ciRunNumber * 3L + trackOffset).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        else ->
            // Debug/alpha: range 1_000_000+ guarantees alpha > any concurrent beta build.
            (1_000_000L + ciRunNumber).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }
} else {
    appVersionCode
}

fun loadReleaseSigningProperties(): Properties? {
    val fromFile = Properties()
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { fromFile.load(it) }
        return fromFile
    }

    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        val local = Properties().apply { localPropsFile.inputStream().use { load(it) } }
        val storeFile = local.getProperty("ammo.upload.storeFile")?.trim().orEmpty()
        if (storeFile.isNotEmpty()) {
            fromFile.setProperty("storeFile", storeFile)
            fromFile.setProperty("storePassword", local.getProperty("ammo.upload.storePassword", ""))
            fromFile.setProperty("keyAlias", local.getProperty("ammo.upload.keyAlias", ""))
            fromFile.setProperty("keyPassword", local.getProperty("ammo.upload.keyPassword", ""))
            return fromFile
        }
    }

    val envStoreFile = System.getenv("AMMO_UPLOAD_STORE_FILE")?.trim().orEmpty()
    if (envStoreFile.isNotEmpty()) {
        fromFile.setProperty("storeFile", envStoreFile)
        fromFile.setProperty("storePassword", System.getenv("AMMO_UPLOAD_STORE_PASSWORD").orEmpty())
        fromFile.setProperty("keyAlias", System.getenv("AMMO_UPLOAD_KEY_ALIAS").orEmpty())
        fromFile.setProperty("keyPassword", System.getenv("AMMO_UPLOAD_KEY_PASSWORD").orEmpty())
        return fromFile
    }

    return null
}

val releaseSigningProperties = loadReleaseSigningProperties()

android {
    namespace = "app.mymultiverse.ammo"
    compileSdk = 36
    defaultConfig {
        applicationId = "app.mymultiverse.ammo"
        minSdk = 24
        targetSdk = 35
        versionCode = versionCodeFinal
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
    signingConfigs {
        if (releaseSigningProperties != null) {
            create("release") {
                val props = releaseSigningProperties
                val storeFilePath = props.getProperty("storeFile")
                    ?: error("Release signing is configured but storeFile is missing")
                storeFile = rootProject.file(storeFilePath)
                storePassword = props.getProperty("storePassword")
                    ?: error("Release signing is configured but storePassword is missing")
                keyAlias = props.getProperty("keyAlias")
                    ?: error("Release signing is configured but keyAlias is missing")
                keyPassword = props.getProperty("keyPassword")
                    ?: error("Release signing is configured but keyPassword is missing")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (releaseSigningProperties != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            if (firebaseCrashlyticsEnabled) {
                java.srcDir("src/firebase/kotlin")
            }
        }
        // AGP's default instrumented-test source set for a plain `com.android.application`
        // module is `src/androidTest`, not `src/androidInstrumentedTest` (that alias only
        // applies to Kotlin Multiplatform android targets). Repo convention keeps tests under
        // `androidInstrumentedTest` (see qa-testing.mdc), so wire it in explicitly — otherwise
        // this entire suite is silently excluded from compilation and never runs.
        getByName("androidTest") {
            java.srcDir("src/androidInstrumentedTest/kotlin")
        }
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.activity.compose)
    implementation(compose.preview)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    if (firebaseCrashlyticsEnabled) {
        implementation(libs.firebase.crashlytics)
        implementation(libs.firebase.messaging)
        implementation(libs.kotlinx.coroutines.play.services)
    }
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.koin.compose)
    // composeApp declares these as `implementation` in commonMain, so they are not exposed
    // transitively to androidApp's compile classpath — required by the androidInstrumentedTest
    // suite, which composes UI directly (Column/Button/Text, etc.) and shares grocery
    // ghost-pairing dismiss storage via the base multiplatform-settings `Settings` type.
    androidTestImplementation(compose.foundation)
    androidTestImplementation(compose.material3)
    androidTestImplementation(libs.multiplatform.settings)
    androidTestImplementation(libs.multiplatform.settings.test)
}
