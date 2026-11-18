plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "br.com.pfm.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.pfm.mobile"
        minSdk = 29
        targetSdk = 35
        versionCode = 200
        versionName = "2.0.0"

        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:18080/\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Room Database (Offline-First < 100ms persistence)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // WorkManager (Offline-First Background Sync with Exponential Backoff)
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // CameraX + ML Kit Barcode Scanning (NFC-e SEFAZ QR Code)
    val cameraxVersion = "1.4.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Networking: OkHttp (HTTP/2) + Cronet QUIC (HTTP/3)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.android.gms:play-services-cronet:18.1.0")
    implementation("com.google.net.cronet:cronet-okhttp:0.1.0")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

// Kover coverage. Exclusions are deliberately narrow: only Android-framework-bound
// Compose UI and generated code. NO business logic (domain, data, sync) is excluded.
kover {
    reports {
        filters {
            excludes {
                classes(
                    // Compose screens/previews (need Android runtime / emulator)
                    "br.com.pfm.mobile.ui.ReceiptComposeScreensKt*",
                    "br.com.pfm.mobile.ui.ComposableSingletons*",
                    // Generated code
                    "br.com.pfm.mobile.BuildConfig",
                    "*_Impl*",
                    "*_Factory*",
                    "*_HiltModules*",
                    "*Hilt_*",
                    "dagger.hilt.internal.aggregatedroot.codegen.*",
                    "hilt_aggregated_deps.*"
                )
            }
        }
    }
}
