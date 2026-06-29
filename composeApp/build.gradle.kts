import java.util.Properties
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

val googleServicesFile = rootProject.layout.projectDirectory.file("androidApp/google-services.json").asFile
val firebaseCrashlyticsEnabled = googleServicesFile.exists()

val generateFirebaseBuildFlags = tasks.register("generateFirebaseBuildFlags") {
    val outputDir = layout.buildDirectory.dir("generated/firebase/kotlin/app/mymultiverse/ammo/data/observability")

    inputs.property("crashlyticsEnabled", firebaseCrashlyticsEnabled)
    outputs.dir(outputDir)

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("FirebaseBuildFlags.kt").writeText(
            """
            package app.mymultiverse.ammo.data.observability

            internal object FirebaseBuildFlags {
                const val CRASHLYTICS_ENABLED: Boolean = $firebaseCrashlyticsEnabled
                const val PUSH_ENABLED: Boolean = $firebaseCrashlyticsEnabled
            }
            """.trimIndent(),
        )
    }
}

val defaultSupabaseUrl = "https://ammo.mymultiverse.app"

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

val generateAppBuildInfo = tasks.register("generateAppBuildInfo") {
    val versionFile = rootProject.layout.projectDirectory.file("gradle/app-version.properties")
    val outputDir = layout.buildDirectory.dir("generated/appinfo/kotlin/app/mymultiverse/ammo/domain")

    inputs.file(versionFile).withPathSensitivity(PathSensitivity.NONE)
    outputs.dir(outputDir)

    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("AppBuildInfo.kt").writeText(
            """
            package app.mymultiverse.ammo.domain

            internal object AppBuildInfo {
                const val VERSION_NAME: String = ${appVersionName.quoteForKotlin()}
                const val VERSION_CODE: Int = $appVersionCode
                const val IS_PRERELEASE: Boolean = ${appVersionPrerelease.isNotEmpty()}
            }
            """.trimIndent(),
        )
    }
}

val generateSupabaseSecrets = tasks.register("generateSupabaseSecrets") {
    val localPropsFile = rootProject.layout.projectDirectory.file("local.properties")
    val outputDir = layout.buildDirectory.dir("generated/supabase/kotlin/app/mymultiverse/ammo/data/supabase")

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
            package app.mymultiverse.ammo.data.supabase

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
        force("androidx.tracing:tracing:1.1.0")
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
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
            implementation(libs.firebase.messaging)
            implementation(libs.kotlinx.coroutines.play.services)
        }
        androidMain {
            kotlin.srcDir(layout.buildDirectory.dir("generated/firebase/kotlin"))
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
            implementation(libs.supabase.functions)
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.koin.test)
        }
    }

    sourceSets.named("commonMain") {
        kotlin.srcDir(layout.buildDirectory.dir("generated/supabase/kotlin"))
        kotlin.srcDir(layout.buildDirectory.dir("generated/appinfo/kotlin"))
    }
}

tasks.matching { it.name.contains("compile", ignoreCase = true) && it.name.contains("Kotlin") }
    .configureEach {
        dependsOn(generateSupabaseSecrets, generateAppBuildInfo, generateFirebaseBuildFlags)
    }

android {
    namespace = "app.mymultiverse.ammo.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
