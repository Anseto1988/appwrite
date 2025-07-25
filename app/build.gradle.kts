plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.snacktrack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.snacktrack"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
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

// Konfiguration für Abhängigkeitsauflösung
configurations.all {
    resolutionStrategy {
        force("com.squareup.okhttp3:okhttp:4.12.0")
        force("com.squareup.okhttp3:okhttp-bom:4.12.0")
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Material Icons Core and Extended
    implementation("androidx.compose.material:material-icons-core:1.6.2")
    implementation("androidx.compose.material:material-icons-extended:1.6.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // OkHttp BOM - explizit hinzugefügt um Konflikte zu vermeiden
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    
    // Appwrite SDK
    implementation("io.appwrite:sdk-for-android:4.0.1") {
        exclude(group = "com.squareup.okhttp3", module = "okhttp-bom")
    }
    
    // OkHttp-Abhängigkeiten explizit hinzufügen
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // ZXing für Barcode-Scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // ML Kit für Barcode-Scanning (ersetzt durch ZXing)
    // implementation("com.google.mlkit:barcode-scanning:17.2.0")
    val cameraxVersion = "1.3.1" // CameraX-Abhängigkeiten bleiben vorerst bestehen
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")
    
    
    // WorkManager für Hintergrundaufgaben
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Diagramme und Visualisierungen - Alternative zu MPAndroidChart
    implementation("co.yml:ycharts:2.1.0")
    
    // Coil für Bildladung
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Datastore für Einstellungen
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Enhanced Testing Dependencies
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("com.google.truth:truth:1.1.5")
    
    // MockK for better Kotlin support
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-android:1.13.8")
    
    // Test runner for instrumented tests
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.rules:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    
    // Compose testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.2")
}