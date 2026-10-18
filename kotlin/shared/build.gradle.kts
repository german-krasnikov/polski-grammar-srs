import java.security.MessageDigest

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
// EN-04/ADR-20 UC-12: the v2 layer files `CoursePackLoader` reconstructs a v1 `CoursePack` from —
// `lang/<code>/lexicon.json` (scanned below alongside curriculum.json) and `pairs/<pairId>/pair.json`.
val pairsDirectory = layout.projectDirectory.dir("../../courses/pairs")
val generatedCourseDirectory = layout.buildDirectory.dir("generated/course/kotlin")
val generatedFormsFixtureDirectory = layout.buildDirectory.dir("generated/formsFixture/kotlin")
val legacyAdmissionLedgers = listOf(
    layout.projectDirectory.file("../../tests/fixtures/textwire/legacy-boundary.cases"),
    layout.projectDirectory.file("../../tests/fixtures/textwire/numeric-admission.cases"),
)
val legacyAdmissionSeal = layout.projectDirectory.file("../../tests/fixtures/textwire/legacy-admission.sha256")
val generatedLegacyAdmissionDirectory = layout.buildDirectory.dir("generated/legacyAdmissionTest/kotlin")

// Test-only values come from the independently reviewed text ledgers. No old input or hash
// is maintained a second time in Kotlin, and this source is never attached to commonMain.
val generateLegacyAdmissionTestSource by tasks.registering {
    legacyAdmissionLedgers.forEach { inputs.file(it) }
    inputs.file(legacyAdmissionSeal)
    outputs.dir(generatedLegacyAdmissionDirectory)
    doLast {
        val sealLine = Regex("([0-9a-f]{64})  (tests/fixtures/textwire/[a-z-]+\\.cases)")
        val sealRows = legacyAdmissionSeal.asFile.readLines()
        val sealed = sealRows.associate { line ->
            val match = sealLine.matchEntire(line) ?: error("Invalid legacy admission seal line")
            match.groupValues[2] to match.groupValues[1]
        }
        val paths = legacyAdmissionLedgers.map { "tests/fixtures/textwire/${it.asFile.name}" }
        require(sealRows.size == 2 && sealed.size == 2 && sealed.keys == paths.toSet()) {
            "Legacy admission seal path set changed or duplicated"
        }
        legacyAdmissionLedgers.zip(paths).forEach { (ledger, path) ->
            val actual = MessageDigest.getInstance("SHA-256")
                .digest(ledger.asFile.readBytes())
                .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
            require(actual == sealed.getValue(path)) { "Legacy admission ledger changed without reviewed seal: $path" }
        }
        val entry = Regex("(?i)\\bsize ([0-9,]+); SHA-256 ([0-9a-f]{64})(?:;|\\.)")
        val rows = legacyAdmissionLedgers.flatMap { ledger ->
            ledger.asFile.readLines().mapNotNull { line ->
                val match = entry.find(line) ?: return@mapNotNull null
                val label = line.substringBefore(':').substringBefore(" = ").trim()
                require(label.isNotBlank() && label.all { it.code in 32..126 }) { "Invalid legacy admission label: $label" }
                val size = match.groupValues[1].replace(",", "").toInt()
                val digest = match.groupValues[2].lowercase()
                Triple(label, size, digest)
            }
        }
        require(rows.size == 16 && rows.map { it.first }.toSet().size == rows.size) {
            "Expected 16 distinct legacy admission hash recipes, got ${rows.size}"
        }
        val target = generatedLegacyAdmissionDirectory.get().file("polski/textwire/LegacyAdmissionLedger.kt").asFile
        target.parentFile.mkdirs()
        target.writeText(
            "package polski.textwire\n\n" +
                "internal val legacyAdmissionLedger: Map<String, Pair<Int, String>> = mapOf(\n" +
                rows.joinToString(",\n") { (label, size, digest) ->
                    "    \"$label\" to ($size to \"$digest\")"
                } + "\n)\n",
        )
    }
}

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

val representabilityInput = layout.projectDirectory.file("../../tests/fixtures/textwire/inputs/legacy-representability-progress.input")
val representabilityCases = layout.projectDirectory.file("../../tests/fixtures/textwire/legacy-representability.cases")
val representabilitySeal = layout.projectDirectory.file("../../tests/fixtures/textwire/legacy-representability.sha256")
val generatedRepresentabilityTestDirectory = layout.buildDirectory.dir("generated/representabilityTest/kotlin")

// Embed only frozen, input-only old-user bytes and independent expectations in commonTest.
val generateRepresentabilityTestSource by tasks.registering {
    inputs.files(representabilityInput, representabilityCases, representabilitySeal)
    outputs.dir(generatedRepresentabilityTestDirectory)
    doLast {
        val expectedPaths = listOf(
            "tests/fixtures/textwire/inputs/legacy-representability-progress.input",
            "tests/fixtures/textwire/legacy-representability.cases",
        )
        val actualFiles = listOf(representabilityInput.asFile, representabilityCases.asFile)
        val rows = representabilitySeal.asFile.readLines()
        require(rows.size == expectedPaths.size) { "Representability seal must list exactly two files" }
        rows.zip(expectedPaths.zip(actualFiles)).forEach { (line, pair) ->
            val (path, file) = pair
            val match = Regex("([0-9a-f]{64})  (tests/fixtures/textwire/[a-z/-]+\\.(?:input|cases))")
                .matchEntire(line) ?: error("Invalid representability seal line")
            require(match.groupValues[2] == path) { "Representability seal path/order changed" }
            val actual = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
                .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
            require(actual == match.groupValues[1]) { "Representability fixture changed without reviewed seal: $path" }
        }
        val output = generatedRepresentabilityTestDirectory.get()
            .file("polski/textwire/LegacyRepresentabilityFixture.kt").asFile
        output.parentFile.mkdirs()
        output.writeText(
            "package polski.textwire\n\n" +
                "internal val legacyRepresentabilityInput = " +
                literalBuildString(literalChunks(representabilityInput.asFile.readText())) + "\n" +
                "internal val legacyRepresentabilityCases = " +
                literalBuildString(literalChunks(representabilityCases.asFile.readText())) + "\n" +
                "internal const val legacyRepresentabilityInputSha256 = \"${rows[0].substringBefore("  ")}\"\n",
        )
    }
}

val acPositiveCorpus = layout.projectDirectory.file("../../tests/fixtures/textwire/positive.cases")
val acNegativeCorpus = layout.projectDirectory.file("../../tests/fixtures/textwire/negative.cases")
val acCorpusSeal = layout.projectDirectory.file("../../tests/fixtures/textwire/corpus.sha256")
val generatedAcCorpusTestDirectory = layout.buildDirectory.dir("generated/acCorpusTest/kotlin")

// Test-only literal bytes are copied from the independent, sealed A/C corpus. No candidate
// encoder supplies an expectation, and the generated source never enters an app source set.
val generateAcCorpusTestSource by tasks.registering {
    inputs.files(acPositiveCorpus, acNegativeCorpus, acCorpusSeal)
    outputs.dir(generatedAcCorpusTestDirectory)
    doLast {
        val pinned = mapOf(
            "tests/fixtures/textwire/positive.cases" to
                "3efd4826a17727b98c81cf31563ece48f9fdf9db33312291b669e4a9e3144075",
            "tests/fixtures/textwire/negative.cases" to
                "4beaf078c24a55a1ff3c2dfcbad0e0e419367f4ed8ddcdd13d3dc4031c8ef646",
        )
        val sealRows = acCorpusSeal.asFile.readLines()
        require(sealRows.size == 4 && sealRows.take(2) == pinned.map { (path, hash) -> "$hash  $path" }) {
            "Frozen A/C corpus seal changed"
        }
        for ((path, file) in listOf(
            "tests/fixtures/textwire/positive.cases" to acPositiveCorpus.asFile,
            "tests/fixtures/textwire/negative.cases" to acNegativeCorpus.asFile,
        )) {
            val actual = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
                .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
            require(actual == pinned.getValue(path)) { "Frozen A/C corpus bytes changed: $path" }
        }
        val target = generatedAcCorpusTestDirectory.get()
            .file("polski/textwire/FrozenAcCorpusSource.kt").asFile
        target.parentFile.mkdirs()
        target.writeText(
            "package polski.textwire\n\n" +
                "internal val frozenAcPositiveCorpus = " +
                literalBuildString(literalChunks(acPositiveCorpus.asFile.readText())) + "\n" +
                "internal val frozenAcNegativeCorpus = " +
                literalBuildString(literalChunks(acNegativeCorpus.asFile.readText())) + "\n",
        )
    }
}

val generateCoursePackSource by tasks.registering {
    // UniversalCorePlan.md §4.2/§12 UC-02: pack ids are discovered by scanning `courses/*` for a
    // `course.json`, not read from one hardcoded path — adding `courses/<id>/course.json` needs no
    // Gradle/Kotlin edit to become an embedded CoursePackSource. `schema`/`styles` aren't packs.
    inputs.dir(coursesDirectory)
    inputs.file(frequencyFile)
    inputs.dir(stylesDirectory)
    inputs.dir(langDirectory)
    inputs.dir(pairsDirectory)
    outputs.dir(generatedCourseDirectory)
    doLast {
        // NJ03 B8: course.json removed as authored source; pl-ru now loads via v2 layers
        // (pair.json + lexicon.json) exactly like en-ru. embeddedCoursePackSources is empty.
        val packSourceLiterals = ""

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

        // NJ03: lexicon.json and pair.json no longer embedded — all packs use typed objects.
        val lexiconEntryLiterals = ""
        val pairEntryLiterals = ""

        // EN-21/ADR-15: `pairs/<pairId>/lifehacks.json` — pair-scoped, outside core, scanned the
        // same way as pair.json above so a pack without lifehacks (or a future 3rd pack) needs no
        // Gradle/Kotlin edit; [polski.presentation.StaticPackLifehackProvider] reads this map.
        val lifehackDirs = (pairsDirectory.asFile.listFiles { file -> file.isDirectory } ?: emptyArray())
            .filter { dir -> dir.resolve("lifehacks.json").isFile }
            .sortedBy { it.name }
        val lifehackEntryLiterals = lifehackDirs.joinToString(",\n") { dir ->
            val chunks = literalChunks(dir.resolve("lifehacks.json").readText())
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
                curriculumEntryLiterals + "\n)\n" +
                "internal val generatedLexiconJsonByLang: Map<String, String> = mapOf(\n" +
                lexiconEntryLiterals + "\n)\n" +
                "internal val generatedPairJsonByPairId: Map<String, String> = mapOf(\n" +
                pairEntryLiterals + "\n)\n" +
                "internal val generatedLifehacksJsonByPairId: Map<String, String> = mapOf(\n" +
                lifehackEntryLiterals + "\n)\n",
        )
    }
}

// UniversalCorePlan.md §5.3/§12 UC-08: forms.generated.json backs the live pl-ru engine
// (polski.core.PlEngine.kt), embedded into commonMain the same way generateCoursePackSource
// embeds course.json. realization.json/exercise-recipes.json are scanned per `lang/<code>/`
// the same way generateCoursePackSource scans curriculum.json (EN-05) — a second target
// language's files need no Gradle/Kotlin edit to reach polski.core.
val generateFormsFixtureSource by tasks.registering {
    inputs.file(formsFixtureFile)
    inputs.dir(langDirectory)
    outputs.dir(generatedFormsFixtureDirectory)
    doLast {
        val chunks = literalChunks(formsFixtureFile.asFile.readText())
        val target = generatedFormsFixtureDirectory.get().file("polski/grammar/GeneratedFormsFixtureJson.kt").asFile
        target.parentFile.mkdirs()
        target.writeText(
            "package polski.grammar\n\n" +
                "internal val generatedFormsFixtureJson = " + literalBuildString(chunks) + "\n",
        )

        val langDirs = (langDirectory.asFile.listFiles { file -> file.isDirectory } ?: emptyArray())
            .sortedBy { it.name }
        val realizationEntryLiterals = langDirs
            .filter { dir -> dir.resolve("realization.json").isFile }
            .joinToString(",\n") { dir ->
                val entryChunks = literalChunks(dir.resolve("realization.json").readText())
                "    \"${dir.name}\" to ${literalBuildString(entryChunks)}"
            }
        val recipesEntryLiterals = langDirs
            .filter { dir -> dir.resolve("exercise-recipes.json").isFile }
            .joinToString(",\n") { dir ->
                val entryChunks = literalChunks(dir.resolve("exercise-recipes.json").readText())
                "    \"${dir.name}\" to ${literalBuildString(entryChunks)}"
            }
        // EN-24 (UC-09 part 2/2 minimum): a per-lang `lang/<code>/forms.generated.json`, additive
        // to pl-ru's own [formsFixtureFile] above (still read directly by name for "pl", untouched)
        // — a second target language's materialized forms need no Gradle/Kotlin edit beyond
        // dropping the file in `lang/<code>/`, same scanning pattern as realization.json above.
        val formsGeneratedEntryLiterals = langDirs
            .filter { dir -> dir.resolve("forms.generated.json").isFile }
            .joinToString(",\n") { dir ->
                val entryChunks = literalChunks(dir.resolve("forms.generated.json").readText())
                "    \"${dir.name}\" to ${literalBuildString(entryChunks)}"
            }
        val recipesTarget = generatedFormsFixtureDirectory.get().file("polski/core/GeneratedRecipesFixtureJson.kt").asFile
        recipesTarget.parentFile.mkdirs()
        recipesTarget.writeText(
            "package polski.core\n\n" +
                "internal val generatedRealizationJsonByLang: Map<String, String> = mapOf(\n" +
                realizationEntryLiterals + "\n)\n" +
                "internal val generatedExerciseRecipesJsonByLang: Map<String, String> = mapOf(\n" +
                recipesEntryLiterals + "\n)\n" +
                "internal val generatedFormsGeneratedJsonByLang: Map<String, String> = mapOf(\n" +
                formsGeneratedEntryLiterals + "\n)\n",
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
            kotlin.srcDir(generateFormsFixtureSource.map { it.outputs.files.singleFile })
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
            // UniversalCorePlan.md §12 UC-08: ExerciseGenerator/TableMorphology are the live pl-ru
            // engine now (polski.core.PlEngine.kt) — api so hosts (:composeApp/:androidApp) can
            // build ports (RandomSource/ExerciseIdFactory) against :core-engine's own types.
            api(project(":core-engine"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        }
        commonTest {
            kotlin.srcDir(generateLegacyAdmissionTestSource.map { it.outputs.files.singleFile })
            kotlin.srcDir(generateRepresentabilityTestSource.map { it.outputs.files.singleFile })
            kotlin.srcDir(generateAcCorpusTestSource.map { it.outputs.files.singleFile })
        }
    }
}
