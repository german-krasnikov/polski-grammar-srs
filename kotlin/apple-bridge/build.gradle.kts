plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Native host admission tests reuse the same sealed input and digest implementation as :shared.
// Copy only these test sources into this module; production source sets never see them.
val syncLegacyAdmissionHostTestSources by tasks.registering(Sync::class) {
    dependsOn(":shared:generateLegacyAdmissionTestSource", ":shared:generateRepresentabilityTestSource")
    from(project(":shared").file("src/commonTest/kotlin/polski/textwire/LegacyAdmissionDigest.kt")) {
        into("polski/textwire")
    }
    from(project(":shared").layout.buildDirectory.dir("generated/legacyAdmissionTest/kotlin"))
    from(project(":shared").layout.buildDirectory.dir("generated/representabilityTest/kotlin"))
    into(layout.buildDirectory.dir("generated/legacyAdmissionHostTest/kotlin"))
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
            // Swift hosts call shared's SwipeDecision and other existing public types directly.
            // Explicit direct export preserves the old shared framework surface.
            export(project(":shared"))
            export(project(":srs-progress"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":shared"))
            api(project(":srs-progress"))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        commonTest {
            kotlin.srcDir(syncLegacyAdmissionHostTestSources.map { it.outputs.files.singleFile })
        }
    }
}
