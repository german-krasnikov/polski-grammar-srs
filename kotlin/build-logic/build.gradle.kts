// UniversalCorePlan.md §4.3: taргеты для каждого нового :core-* модуля выносятся сюда, чтобы не
// копировать 7-таргетный boilerplate (см. kotlin/shared/build.gradle.kts) в каждый новый модуль.
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencies {
    // Versions mirror the root build (kotlin/gradle/libs.versions.toml) — kept in sync manually
    // since this included build does not share the root's version catalog.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    implementation("com.android.tools.build:gradle:9.1.1")
}
