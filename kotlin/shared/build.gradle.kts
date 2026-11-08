import java.security.MessageDigest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

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

// Shared by the test-source tasks below: a JS/Wasm-safe way to embed a file's bytes as a Kotlin string constant,
// so commonTest never does file I/O at runtime (browser targets have none).
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
            // Rows printed by `biome generate --project dsl/biome.project` from courses-biome/<pair>/*.core-data (standard layout, committed).
            kotlin.srcDir("src/commonMain/generated/kotlin")
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            // UniversalCorePlan.md §4.1 UC-01: model/Grammar.kt's Polish enums are a thin adapter
            // over the universal core's open FeatureKey/FeatureValue catalog.
            api(project(":core-model"))
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
            // Rows printed by `biome generate --project dsl/biome.project` from dsl/grammar-oracle/*.core-data (same layout as commonMain's generated rows, line 200, but test-only data has no business being in commonMain).
            kotlin.srcDir("src/commonTest/generated/kotlin")
        }
    }
}
