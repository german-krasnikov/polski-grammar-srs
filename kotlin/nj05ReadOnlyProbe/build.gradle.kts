import org.gradle.api.tasks.Sync
import java.security.MessageDigest

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Separate test module: :shared main is a dependency, but :shared commonTest is never
// compiled into this runner. This avoids the JS --tests filter's observed 393-test run.
val sourceManifest = layout.projectDirectory.file("read-only-sources.list")
val generatedSourceSeal = layout.projectDirectory.file("generated-sources.sha256")
val sourcePrefix = "kotlin/shared/src/commonTest/kotlin/polski/textwire/"
val expectedTests = mapOf(
    "LegacyRepresentabilityProbeTest" to 1,
    "RetainedNumberPrototypeTest" to 4,
    "RetainedNumberIndependentMutationTest" to 1,
)
data class SelectedSource(
    val path: String,
    val testClass: String?,
    val methodCount: Int,
    val sha256: String,
)

fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes)
    .joinToString("") { "%02x".format(it.toInt() and 0xff) }

fun generatedHashes(seal: File): Map<String, String> {
    val rows = seal.readLines()
    require(rows.size == 2) { "NJ05 generated-source seal row count changed" }
    val hashes = rows.map { line ->
        val match = Regex("([0-9a-f]{64})  (LegacyAdmissionLedger|LegacyRepresentabilityFixture)\\.kt")
            .matchEntire(line) ?: error("Invalid NJ05 generated-source seal row")
        "${match.groupValues[2]}.kt" to match.groupValues[1]
    }.toMap()
    require(hashes.size == 2 && hashes.keys == setOf(
        "LegacyAdmissionLedger.kt", "LegacyRepresentabilityFixture.kt",
    )) { "NJ05 generated-source seal path set changed" }
    return hashes
}

fun selectedSources(
    manifest: File,
    allowedPrefix: String,
    requiredTests: Map<String, Int>,
): List<SelectedSource> {
    val rows = manifest.readLines()
        .filter { it.isNotBlank() && !it.startsWith('#') }
        .map { line ->
            val columns = line.split(' ')
            when {
                columns.size == 3 && columns[0] == "support" ->
                    SelectedSource(columns[1], null, 0, columns[2])
                columns.size == 5 && columns[0] == "test" ->
                    SelectedSource(columns[1], columns[2], columns[3].toInt(), columns[4])
                else -> error("Invalid NJ05 selection row: $line")
            }
        }
    require(rows.size == 5 && rows.map { it.path }.toSet().size == rows.size)
    require(rows.filter { it.testClass != null }
        .associate { it.testClass!! to it.methodCount } == requiredTests) {
        "NJ05 read-only test class/method selection changed"
    }
    require(rows.count { it.testClass == null } == 2)
    require(rows.all { Regex("[0-9a-f]{64}").matches(it.sha256) }) {
        "NJ05 source hash missing or malformed"
    }
    require(rows.all { it.path.startsWith(allowedPrefix) && it.path.endsWith(".kt") &&
        '/' !in it.path.removePrefix(allowedPrefix) }) {
        "NJ05 source escaped the exact textwire directory"
    }
    return rows
}

val sources = selectedSources(sourceManifest.asFile, sourcePrefix, expectedTests)
// Gradle's root is product/kotlin; manifest paths are relative to the product root.
val productDirectory = rootProject.projectDir.parentFile
val sourceFiles = sources.map { productDirectory.resolve(it.path) }
val allowedDirectory = productDirectory.resolve(sourcePrefix).canonicalFile
val forbiddenCalls = Regex(
    """\b(?:encodeLegacyV1|importJson|exportJson|saveRaw|migrateLegacy|backupJson)\s*\(|\.\s*(?:encodeToString|stringify|serialize|save|put|remove)\s*\(""",
)
val testAnnotation = Regex("(?m)^\\s*@Test\\s*$")
val topLevelClass = Regex("(?m)^class\\s+([A-Za-z][A-Za-z0-9]*)\\b")
val generatedFixtureFiles = mapOf(
    "LegacyAdmissionLedger.kt" to project(":shared").layout.buildDirectory.file(
        "generated/legacyAdmissionTest/kotlin/polski/textwire/LegacyAdmissionLedger.kt",
    ),
    "LegacyRepresentabilityFixture.kt" to project(":shared").layout.buildDirectory.file(
        "generated/representabilityTest/kotlin/polski/textwire/LegacyRepresentabilityFixture.kt",
    ),
)

val prepareNj05ReadOnlyTestSources by tasks.registering(Sync::class) {
    inputs.file(sourceManifest)
    inputs.file(generatedSourceSeal)
    inputs.files(sourceFiles)
    dependsOn(":shared:generateLegacyAdmissionTestSource")
    dependsOn(":shared:generateRepresentabilityTestSource")
    from(sourceFiles) { into("polski/textwire") }
    generatedFixtureFiles.values.forEach { provider ->
        from(provider) { into("polski/textwire") }
    }
    into(layout.buildDirectory.dir("generated/nj05ReadOnlyProbeTest/kotlin"))
    doFirst {
        val expectedGenerated = generatedHashes(generatedSourceSeal.asFile)
        generatedFixtureFiles.forEach { (name, provider) ->
            val file = provider.get().asFile
            require(file.isFile && sha256(file.readBytes()) == expectedGenerated.getValue(name)) {
                "Reviewed NJ05 generated test source changed before copy: $name"
            }
        }
        sources.zip(sourceFiles).forEach { (source, file) ->
            require(file.isFile && file.canonicalFile.parentFile == allowedDirectory) {
                "Missing or redirected NJ05 source: ${source.path}"
            }
            val bytes = file.readBytes()
            require(sha256(bytes) == source.sha256) {
                "Reviewed NJ05 test source changed: ${source.path}"
            }
            val text = bytes.toString(Charsets.UTF_8)
            require(!forbiddenCalls.containsMatchIn(text)) { "Old writer/import/save call in ${source.path}" }
            require(testAnnotation.findAll(text).count() == source.methodCount) {
                "Unexpected @Test count in ${source.path}"
            }
            if (source.testClass != null) {
                require(topLevelClass.findAll(text).map { it.groupValues[1] }.toList() ==
                    listOf(source.testClass)) {
                    "NJ05 test class selection changed in ${source.path}"
                }
            }
        }
    }
}

val verifyNj05ReadOnlySelection by tasks.registering {
    group = "verification"
    description = "Fail if isolated JS/Wasm NJ05 sources include any unreviewed test class."
    inputs.file(generatedSourceSeal)
    dependsOn(prepareNj05ReadOnlyTestSources)
    doLast {
        val output = layout.buildDirectory.dir("generated/nj05ReadOnlyProbeTest/kotlin").get().asFile
        val selected = output.walkTopDown().filter { it.isFile }
            .map { it.relativeTo(output).path.replace(File.separatorChar, '/') }.toSet()
        val expected = (sourceFiles.map { it.name } + listOf(
            "LegacyAdmissionLedger.kt", "LegacyRepresentabilityFixture.kt",
        )).map { "polski/textwire/$it" }.toSet()
        require(selected == expected && selected.size == 7) {
            "NJ05 runner source selection drift: $selected"
        }
        sources.zip(sourceFiles).forEach { (source, file) ->
            val copied = output.resolve("polski/textwire/${file.name}").readBytes()
            require(sha256(copied) == source.sha256) {
                "Copied NJ05 test source changed: ${source.path}"
            }
        }
        generatedHashes(generatedSourceSeal.asFile).forEach { (name, expectedHash) ->
            val bytes = output.resolve("polski/textwire/$name").readBytes()
            require(sha256(bytes) == expectedHash) { "Reviewed NJ05 generated test source changed: $name" }
        }
        println("NJ05 source selection: 3 read-only classes / 6 tests / 2 helpers / 2 sealed fixtures")
    }
}

kotlin {
    js { browser() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonTest {
            kotlin.srcDir(prepareNj05ReadOnlyTestSources.map { it.outputs.files.singleFile })
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":shared"))
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}

tasks.matching { it.name == "jsBrowserTest" || it.name == "wasmJsBrowserTest" }
    .configureEach { dependsOn(verifyNj05ReadOnlySelection) }
