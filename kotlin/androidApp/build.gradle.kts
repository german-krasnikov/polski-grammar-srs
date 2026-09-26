plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
}

android {
    namespace = "dev.polski.grammarmatrix"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.polski.grammarmatrix"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // FC2-04: Robolectric needs the real merged manifest/resources (not its bare defaults) to
    // resolve `androidx.activity.ComponentActivity` for `createComposeRule()` — otherwise it falls
    // back to a synthetic "org.robolectric.default" package that has no activities at all.
    testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(compose.material3)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
    // FC2-04: the one gap in v1's otherwise-already-correct 90° face swap — a real Compose UI test
    // moving MainTestClock to angle≈45f/135f, run under Robolectric (no device/emulator needed).
    testImplementation("androidx.compose.ui:ui-test-junit4:1.12.1")
    testImplementation("androidx.compose.ui:ui-test-manifest:1.12.1")
}
