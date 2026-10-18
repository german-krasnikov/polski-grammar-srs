import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Separate test module: :shared main is a dependency, but :shared commonTest is never
// compiled into this runner. This avoids the JS --tests filter's observed 393-test run.
val sourceManifest = layout.projectDirectory.file("read-only-sources.list")
val sourcePrefix = "kotlin/shared/src/commonTest/kotlin/polski/textwire/"
val expectedTests = mapOf(
    "LegacyRepresentabilityProbeTest" to 1,
    "RetainedNumberPrototypeTest" to 4,
    "RetainedNumberIndependentMutationTest" to 1,
)
data class SelectedSource(val path: String, val testClass: String?, val methodCount: Int)

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
                columns.size == 2 && columns[0] == "support" -> SelectedSource(columns[1], null, 0)
                columns.size == 4 && columns[0] == "test" ->
                    SelectedSource(columns[1], columns[2], columns[3].toInt())
                else -> error("Invalid NJ05 selection row: $line")
            }
        }
    require(rows.size == 5 && rows.map { it.path }.toSet().size == rows.size)
    require(rows.filter { it.testClass != null }
        .associate { it.testClass!! to it.methodCount } == requiredTests) {
        "NJ05 read-only test class/method selection changed"
    }
    require(rows.count { it.testClass == null } == 2)
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

val prepareNj05ReadOnlyTestSources by tasks.registering(Sync::class) {
    inputs.file(sourceManifest)
    inputs.files(sourceFiles)
    dependsOn(":shared:generateLegacyAdmissionTestSource")
    dependsOn(":shared:generateRepresentabilityTestSource")
    from(sourceFiles) { into("polski/textwire") }
    from(project(":shared").layout.buildDirectory.file(
        "generated/legacyAdmissionTest/kotlin/polski/textwire/LegacyAdmissionLedger.kt",
    )) { into("polski/textwire") }
    from(project(":shared").layout.buildDirectory.file(
        "generated/representabilityTest/kotlin/polski/textwire/LegacyRepresentabilityFixture.kt",
    )) { into("polski/textwire") }
    into(layout.buildDirectory.dir("generated/nj05ReadOnlyProbeTest/kotlin"))
    doFirst {
        sources.zip(sourceFiles).forEach { (source, file) ->
            require(file.isFile && file.canonicalFile.parentFile == allowedDirectory) {
                "Missing or redirected NJ05 source: ${source.path}"
            }
            val text = file.readText()
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
