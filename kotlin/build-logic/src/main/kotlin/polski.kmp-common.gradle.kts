import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

// UniversalCorePlan.md §4.3: the same 7 KMP targets as :shared (kotlin/shared/build.gradle.kts),
// factored out so a new :core-* module is just `plugins { id("polski.kmp-common") }` +
// dependencies, with no copy of this target boilerplate. Android namespace/SDK levels are the
// one thing every consumer would otherwise duplicate identically — defaulted here from the
// module's own Gradle project name, so a new module needs zero configuration to compile.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    android {
        namespace = "dev.polski.grammarmatrix.${project.name.replace("-", "")}"
        compileSdk = 37
        minSdk = 24
    }
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }
    applyDefaultHierarchyTemplate()
}
