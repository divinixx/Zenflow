plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutLibraries)
}

android {
    namespace = "com.divinixx.zenflow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.divinixx.zenflow"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
//        dataBinding = true
        compose = true
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // AppCompat for Material themes
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")

    // Compose BOM and UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.foundation)
    ksp(libs.hilt.compiler)

    // ViewBinding and ViewModel
    val lifecycle_version = "2.9.2"

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    // Lifecycle utilities for Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")

    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")

    // ViewModel integration with Navigation3
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0-alpha02")

    // DataStore
    implementation(libs.androidx.datastore.preferences.core)

    // Glance for Widgets
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Additional Libraries
    implementation(libs.calendar)
    implementation(libs.compose.charts)
    implementation(libs.purchases)
    implementation(libs.purchases.ui)
    implementation(libs.reorderable)
    implementation(libs.materialKolor)
    implementation(libs.colorpicker.compose)
    implementation(libs.aboutLibraries)
    implementation(libs.aboutlibraries.compose.m3)
    implementation(libs.composeIcons.fontAwesome)
    implementation(libs.material.icons.core)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // WebSocket and JSON - Using Ktor for better Kotlin coroutines support
    implementation("io.ktor:ktor-client-core:3.2.3")
    implementation("io.ktor:ktor-client-cio:3.2.3")
    implementation("io.ktor:ktor-client-websockets:3.2.3")
    implementation("io.ktor:ktor-client-logging:3.2.3")
    implementation("com.google.code.gson:gson:2.13.1")

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}