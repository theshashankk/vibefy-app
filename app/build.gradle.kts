plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.vibefy.musicwtf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vibefy.musicwtf"
        minSdk = 26          // Android 8 — covers 95%+ of active devices
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Dynamic domain base URL — change here for future domain migrations
        buildConfigField("String", "BASE_URL", "\"https://music-wtf.vercel.app\"")
    }

    // Custom Branded APK Filename: MusicWTF-v1.0.0-release.apk
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as? com.android.build.gradle.api.ApkVariantOutput
            if (output != null) {
                val appName = "MusicWTF"
                val version = variant.versionName
                val buildType = variant.buildType.name
                output.outputFileName = "${appName}-v${version}-${buildType}.apk"
            }
        }
    }

    signingConfigs {
        create("release") {
            val ksFile = System.getenv("KEYSTORE_FILE") ?: "release-upload-keystore.jks"
            val envPass = System.getenv("KEYSTORE_PASSWORD")
            val envAlias = System.getenv("KEY_ALIAS")
            val envKeyPass = System.getenv("KEY_PASSWORD")

            if (file(ksFile).exists() && !envPass.isNullOrBlank()) {
                storeFile = file(ksFile)
                storePassword = envPass
                keyAlias = envAlias ?: "musicwtf"
                keyPassword = envKeyPass ?: envPass
            } else {
                // Fallback to debug key if no custom release environment credentials exist
                storeFile = signingConfigs.getByName("debug").storeFile
                storePassword = signingConfigs.getByName("debug").storePassword
                keyAlias = signingConfigs.getByName("debug").keyAlias
                keyPassword = signingConfigs.getByName("debug").keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.kotlinx.serialization)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Coil
    implementation(libs.coil.compose)

    // WebView
    implementation(libs.androidx.webkit)

    // Media3
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)
    implementation(libs.media3.datasource.okhttp)

    // WorkManager & Hilt Integration
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Splash screen API
    implementation(libs.androidx.splashscreen)

    // Glance widget
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // System UI controller
    implementation(libs.accompanist.systemuicontroller)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
