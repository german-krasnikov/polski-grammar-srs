plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

val courseFile = layout.projectDirectory.file("../../courses/pl-ru/course.json")
val frequencyFile = layout.projectDirectory.file("../../courses/pl-ru/frequency-top1000.json")
// UC-10/StylesBlueprint.md §2/§4: every JSON file under here is a presentation-style recipe,
// inlined below the same way course.json is. Listing files by directory scan (not a fixed
// filename list) means a new recipe file needs no Gradle/Kotlin edit to reach StyleRegistry —
// only the JSON file.
val stylesDirectory = layout.projectDirectory.dir("../../courses/styles")
val generatedCourseDirectory = layout.buildDirectory.dir("generated/course/kotlin")
val generateCoursePackSource by tasks.registering {
    inputs.file(courseFile)
    inputs.file(frequencyFile)
    inputs.dir(stylesDirectory)
    outputs.dir(generatedCourseDirectory)
    doLast {
        fun literalChunks(source: String): List<String> {
            val rawChunks = mutableListOf<String>()
            var start = 0
            while (start < source.length) {
                var end = minOf(start + 8000, source.length)
                // Never split a UTF-16 surrogate pair across two chunks.
                if (end < source.length && Character.isHighSurrogate(source[end - 1]) && Character.isLowSurrogate(source[end])) {
                    end -= 1
                }
                rawChunks += source.substring(start, end)
                start = end
            }
            return rawChunks.map { chunk ->
                chunk.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("$", "\\$")
                    .replace("\n", "\\n")
            }
        }
        val chunks = literalChunks(courseFile.asFile.readText())
        val frequencyChunks = literalChunks(frequencyFile.asFile.readText())
        val styleFiles = (stylesDirectory.asFile.listFiles { file -> file.extension == "json" } ?: emptyArray())
            .sortedBy { it.name }
        val stylesJson = "[" + styleFiles.joinToString(",") { it.readText() } + "]"
        val styleChunks = literalChunks(stylesJson)
        val target = generatedCourseDirectory.get().file("polski/data/GeneratedCourseJson.kt").asFile
        target.parentFile.mkdirs()
        target.writeText("package polski.data\n\ninternal val generatedCourseJson = buildString {\n" +
            chunks.joinToString("\n") { "    append(\"$it\")" } + "\n}\n" +
            "internal val generatedFrequencyJson = buildString {\n" +
            frequencyChunks.joinToString("\n") { "    append(\"$it\")" } + "\n}\n" +
            "internal val generatedStylesJson = buildString {\n" +
            styleChunks.joinToString("\n") { "    append(\"$it\")" } + "\n}\n")
    }
}
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
            // Deriving the srcDir from the task's own outputs (rather than the raw
            // directory provider) lets Gradle auto-wire every consumer — including
            // compile*MainKotlinMetadata — as a task dependency, not just tasks whose
            // name happens to match a "compileKotlin*" prefix.
            kotlin.srcDir(generateCoursePackSource.map { it.outputs.files.singleFile })
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
