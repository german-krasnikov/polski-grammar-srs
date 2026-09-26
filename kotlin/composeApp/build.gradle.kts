import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.kmp.library)
}

val macObserverSource = layout.projectDirectory.file("src/desktopMain/macos/MacDisplayObserver.swift")
val macObserverBuild = layout.buildDirectory.dir("macDisplayObserver")
val macObserverResources = layout.buildDirectory.dir("macDisplayResources")
val macObserverArm64 = tasks.register<Exec>("compileMacDisplayObserverArm64") {
    onlyIf { System.getProperty("os.name").startsWith("Mac") }
    inputs.file(macObserverSource)
    val output = macObserverBuild.map { it.file("MacDisplayObserver-arm64") }
    outputs.file(output)
    doFirst { output.get().asFile.parentFile.mkdirs() }
    commandLine("xcrun", "swiftc", "-target", "arm64-apple-macosx12.0",
        macObserverSource.asFile.absolutePath, "-o", output.get().asFile.absolutePath)
}
val macObserverX64 = tasks.register<Exec>("compileMacDisplayObserverX64") {
    onlyIf { System.getProperty("os.name").startsWith("Mac") }
    inputs.file(macObserverSource)
    val output = macObserverBuild.map { it.file("MacDisplayObserver-x64") }
    outputs.file(output)
    doFirst { output.get().asFile.parentFile.mkdirs() }
    commandLine("xcrun", "swiftc", "-target", "x86_64-apple-macosx12.0",
        macObserverSource.asFile.absolutePath, "-o", output.get().asFile.absolutePath)
}
val compileMacDisplayObserver = tasks.register<Exec>("compileMacDisplayObserver") {
    onlyIf { System.getProperty("os.name").startsWith("Mac") }
    dependsOn(macObserverArm64, macObserverX64)
    val output = macObserverResources.map { it.file("macos/MacDisplayObserver") }
    outputs.file(output)
    doFirst { output.get().asFile.parentFile.mkdirs() }
    commandLine("xcrun", "lipo", "-create",
        macObserverBuild.get().file("MacDisplayObserver-arm64").asFile.absolutePath,
        macObserverBuild.get().file("MacDisplayObserver-x64").asFile.absolutePath,
        "-output", output.get().asFile.absolutePath)
}

tasks.matching { it.name == "prepareAppResources" }.configureEach { dependsOn(compileMacDisplayObserver) }
tasks.withType<JavaExec>().matching { it.name == "run" }.configureEach {
    dependsOn(compileMacDisplayObserver)
    systemProperty("polski.mac.observer.path",
        macObserverResources.get().file("macos/MacDisplayObserver").asFile.absolutePath)
}
tasks.matching { it.name == "createDistributable" || it.name == "createReleaseDistributable" }.configureEach {
    dependsOn(compileMacDisplayObserver)
    doLast {
        val variant = if (name == "createReleaseDistributable") "main-release" else "main"
        val helper = layout.buildDirectory.file(
            "compose/binaries/$variant/app/Polski Grammar Matrix.app/Contents/app/resources/MacDisplayObserver",
        ).get().asFile
        check(helper.isFile && helper.setExecutable(true, false) && helper.canExecute()) {
            "Packaged Mac display observer is missing or not executable: $helper"
        }
    }
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")
    android {
        namespace = "dev.polski.grammarmatrix.compose"
        compileSdk = 37
        minSdk = 24
        androidResources { enable = true }
    }
    js {
        browser()
        binaries.executable()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        webTest.dependencies {
            implementation(kotlin("test"))
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
            }
        }
        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.compose.ui:ui-test:1.12.1")
            }
        }
        androidMain.dependencies {
            implementation(compose.material3)
            // FC-16: legacy RiveAnimationView/state-machine-input API — see AndroidRiveOverlay.kt.
            implementation("app.rive:rive-android:11.12.1")
        }
    }
}

compose.desktop {
    application {
        from(kotlin.targets["desktop"])
        mainClass = "polski.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "Polski Grammar Matrix"
            packageVersion = "1.0.0"
            appResourcesRootDir.set(macObserverResources)
            macOS {
                bundleID = "dev.polski.grammarmatrix"
                iconFile.set(rootProject.file("../assets/branding/LearningIcon.icns"))
            }
        }
    }
}
