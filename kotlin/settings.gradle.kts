pluginManagement {
    // UniversalCorePlan.md §4.3: convention plugin for the 7 KMP targets shared by :core-* modules.
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "polski-grammar-kotlin"
include(":shared", ":composeApp", ":androidApp", ":core-model", ":pack-format", ":core-engine")
