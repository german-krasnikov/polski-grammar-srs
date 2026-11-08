plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())
    val iosDevice = iosArm64()
    val iosSimulator = iosSimulatorArm64()
    val macDevice = macosArm64()
    applyDefaultHierarchyTemplate()

    listOf(iosDevice, iosSimulator, macDevice).forEach { target ->
        target.binaries.framework {
            baseName = "PolskiShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
