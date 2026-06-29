import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(compose.preview)
    implementation(libs.koin.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.koin.compose)
    androidTestImplementation(libs.multiplatform.settings.test)
}
