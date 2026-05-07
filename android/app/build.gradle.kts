plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.assetcoach"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.assetcoach"
        minSdk = 28        // Android 9, Pie — SMS Notification Listener works
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
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
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    debugImplementation(libs.androidx.ui.tooling)

    // Lifecycle / ViewModel for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ─────────────────────────────────────────────────────
    // Phase 2 — Data layer
    // ─────────────────────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // SQLCipher 통합은 Phase 2.5 — 일반 Room 안정화 후 SupportFactory 추가
    // implementation("net.zetetic:android-database-sqlcipher:4.5.4")

    // ─────────────────────────────────────────────────────
    // Phase 3 — On-device LLM (uncomment when implementing)
    // ─────────────────────────────────────────────────────
    // implementation("com.google.mediapipe:tasks-genai:0.10.14")

    // ─────────────────────────────────────────────────────
    // Phase 4 — SMS parsing, work scheduling
    // ─────────────────────────────────────────────────────
    // implementation("androidx.work:work-runtime-ktx:2.9.1")
}
