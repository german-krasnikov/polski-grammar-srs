plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

val coursesDirectory = layout.projectDirectory.dir("../../courses")
val frequencyFile = layout.projectDirectory.file("../../courses/pl-ru/frequency-top1000.json")
// UC-10/StylesBlueprint.md §2/§4: every JSON file under here is a presentation-style recipe,
// inlined below the same way course.json is. Listing files by directory scan (not a fixed
// filename list) means a new recipe file needs no Gradle/Kotlin edit to reach StyleRegistry —
// only the JSON file.
val stylesDirectory = layout.projectDirectory.dir("../../courses/styles")
val formsFixtureFile = layout.projectDirectory.file("../../courses/pl-ru/forms.generated.json")
// UniversalCorePlan.md §3.1/§12 UC-06: `lang/<code>/curriculum.json` — scanned the same way as
// `packDirs` below, so a second target language's curriculum needs no Gradle/Kotlin edit.
val langDirectory = layout.projectDirectory.dir("../../courses/lang")
val generatedCourseDirectory = layout.buildDirectory.dir("generated/course/kotlin")
val generatedFormsFixtureDirectory = layout.buildDirectory.dir("generated/formsFixture/kotlin")

// Shared by generateCoursePackSource and generateFormsFixtureSource below: a JS/Wasm-safe way to
// embed a JSON file's bytes as a Kotlin string constant, so commonTest/commonMain never do file
// I/O at runtime (browser targets have none) — the same technique for both, just different inputs.
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
fun literalBuildString(chunks: List<String>): String =
    "buildString {\n" + chunks.joinToString("\n") { "    append(\"$it\")" } + "\n}"

val generateCoursePackSource by tasks.registering {
    // UniversalCorePlan.md §4.2/§12 UC-02: pack ids are discovered by scanning `courses/*` for a
    // `course.json`, not read from one hardcoded path — adding `courses/<id>/course.json` needs no
    // Gradle/Kotlin edit to become an embedded CoursePackSource. `schema`/`styles` aren't packs.
    inputs.dir(coursesDirectory)
    inputs.file(frequencyFile)
    inputs.dir(stylesDirectory)
    inputs.dir(langDirectory)
    outputs.dir(generatedCourseDirectory)
    doLast {
        // UC-02: a pack directory is any immediate child of `courses/` (other than the shared
        // `schema`/`styles` directories) that has its own `course.json` — pl-ru is the only one
        // today, read as schema v1 like before; a second pack directory needs no edit here.
        val packDirs = (coursesDirectory.asFile.listFiles { file -> file.isDirectory } ?: emptyArray())
            .filter { dir -> dir.name != "schema" && dir.name != "styles" && dir.resolve("course.json").isFile }
            .sortedBy { it.name }
        val packSourceLiterals = packDirs.joinToString(",\n") { dir ->
            val chunks = literalChunks(dir.resolve("course.json").readText())
            "    EmbeddedCoursePackSource(\"${dir.name}\", ${literalBuildString(chunks)})"
        }

        val frequencyChunks = literalChunks(frequencyFile.asFile.readText())
        val styleFiles = (stylesDirectory.asFile.listFiles { file -> file.extension == "json" } ?: emptyArray())
            .sortedBy { it.name }
        val stylesJson = "[" + styleFiles.joinToString(",") { it.readText() } + "]"
        val styleChunks = literalChunks(stylesJson)

        // UC-06: any `lang/<code>/curriculum.json` becomes an entry keyed by <code> — pl is the
        // only one today, but a new target language's curriculum needs no edit here.
        val langDirs = (langDirectory.asFile.listFiles { file -> file.isDirectory } ?: emptyArray())
            .filter { dir -> dir.resolve("curriculum.json").isFile }
            .sortedBy { it.name }
        val curriculumEntryLiterals = langDirs.joinToString(",\n") { dir ->
            val chunks = literalChunks(dir.resolve("curriculum.json").readText())
            "    \"${dir.name}\" to ${literalBuildString(chunks)}"
        }

        val target = generatedCourseDirectory.get().file("polski/data/GeneratedCourseJson.kt").asFile
        target.parentFile.mkdirs()
        target.writeText(
            "package polski.data\n\n" +
                "import polski.pack.EmbeddedCoursePackSource\n" +
                "import polski.pack.PackManifest\n\n" +
                "internal val embeddedCoursePackSources: List<EmbeddedCoursePackSource> = listOf(\n" +
                packSourceLiterals + "\n)\n" +
                "internal val coursePackManifest: PackManifest = PackManifest(embeddedCoursePackSources.map { it.id })\n" +
                "internal val generatedFrequencyJson = " + literalBuildString(frequencyChunks) + "\n" +
                "internal val generatedStylesJson = " + literalBuildString(styleChunks) + "\n" +
                "internal val generatedCurriculumJsonByLang: Map<String, String> = mapOf(\n" +
                curriculumEntryLiterals + "\n)\n",
        )
    }
}

// UniversalCorePlan.md §5.3/§12 UC-05: forms.generated.json (scripts/build-pack.mjs) is test-only
// data today — GrammarEngine stays the live path — so it's embedded into commonTest, not
// commonMain, the same way generateCoursePackSource embeds course.json for commonMain.
val generateFormsFixtureSource by tasks.registering {
    inputs.file(formsFixtureFile)
    outputs.dir(generatedFormsFixtureDirectory)
    doLast {
        val chunks = literalChunks(formsFixtureFile.asFile.readText())
        val target = generatedFormsFixtureDirectory.get().file("polski/grammar/GeneratedFormsFixtureJson.kt").asFile
        target.parentFile.mkdirs()
        target.writeText(
            "package polski.grammar\n\n" +
                "internal val generatedFormsFixtureJson = " + literalBuildString(chunks) + "\n",
        )
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
            // UniversalCorePlan.md §4.1 UC-01: model/Grammar.kt's Polish enums are a thin adapter
            // over the universal core's open FeatureKey/FeatureValue catalog.
            api(project(":core-model"))
            // UniversalCorePlan.md §4.1 UC-02: CoursePackSource is the seam generateCoursePackSource
            // (above) generates against — pl-ru is read as v1 through it, same as before.
            api(project(":pack-format"))
        }
        commonTest {
            kotlin.srcDir(generateFormsFixtureSource.map { it.outputs.files.singleFile })
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            // UniversalCorePlan.md §12 UC-05: TableMorphology, compared against GrammarEngine —
            // test-only, since GrammarEngine remains the live path until UC-07/08.
            implementation(project(":core-engine"))
        }
    }
}
