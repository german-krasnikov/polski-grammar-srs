plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

val courseFile = layout.projectDirectory.file("../../courses/pl-ru/course.json")
val frequencyFile = layout.projectDirectory.file("../../courses/pl-ru/frequency-top1000.json")
val generatedCourseDirectory = layout.buildDirectory.dir("generated/course/kotlin")
val generateCoursePackSource by tasks.registering {
    inputs.file(courseFile)
    inputs.file(frequencyFile)
    outputs.dir(generatedCourseDirectory)
    doLast {
        fun literalChunks(source: String) = source.chunked(8000).map { chunk ->
            chunk.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("$", "\\$")
                .replace("\n", "\\n")
        }
        val chunks = literalChunks(courseFile.asFile.readText())
        val frequencyChunks = literalChunks(frequencyFile.asFile.readText())
        val target = generatedCourseDirectory.get().file("polski/data/GeneratedCourseJson.kt").asFile
        target.parentFile.mkdirs()
        target.writeText("package polski.data\n\ninternal val generatedCourseJson = buildString {\n" +
            chunks.joinToString("\n") { "    append(\"$it\")" } + "\n}\n" +
            "internal val generatedFrequencyJson = buildString {\n" +
            frequencyChunks.joinToString("\n") { "    append(\"$it\")" } + "\n}\n")
    }
}
tasks.matching { it.name.startsWith("compileKotlin") || it.name == "compileAndroidMain" }
    .configureEach { dependsOn(generateCoursePackSource) }

kotlin {
    jvmToolchain(21)
    jvm("desktop")
    val iosDevice = iosArm64()
    val iosSimulator = iosSimulatorArm64()
    val macDevice = macosArm64()
    android {
        namespace = "dev.polski.grammarmatrix.shared"
        compileSdk = 37
        minSdk = 24
    }
    js {
        browser()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    applyDefaultHierarchyTemplate()

    listOf(iosDevice, iosSimulator, macDevice).forEach { target ->
        target.binaries.framework {
            baseName = "PolskiShared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedCourseDirectory)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
    }
}
