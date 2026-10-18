import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin { jvmToolchain(21) }

dependencies {
    testImplementation(libs.playwright)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    exclude("**/SettingsRouteSmokeTest.class")
    exclude("**/DriverProbeTest.class")
    exclude("**/DistributionIdentityTest.class")
    maxParallelForks = 1
}

fun registerBrowserTest(taskName: String, branch: String, distributionTask: String) = tasks.register<Test>(taskName) {
    description = "Runs the Kotlin web settings route in Chromium, Firefox and WebKit ($branch)."
    group = "verification"
    dependsOn(":composeApp:$distributionTask")
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    include("**/SettingsRouteSmokeTest.class")
    maxParallelForks = 1
    outputs.upToDateWhen { false }
    systemProperty("polski.browser.branch", branch)
    systemProperty("polski.browser.worktree", rootProject.projectDir.parentFile.absolutePath)
    systemProperty("polski.browser.dist", rootProject.file("composeApp/build/dist/$branch/productionExecutable").absolutePath)
    providers.gradleProperty("polski.browser.denyEngine").orNull?.let {
        systemProperty("polski.browser.denyEngine", it)
    }
    reports.junitXml.outputLocation.set(layout.buildDirectory.dir("test-results/$taskName"))
    reports.html.outputLocation.set(layout.buildDirectory.dir("reports/tests/$taskName"))
}

registerBrowserTest("browserJsTest", "js", "jsBrowserDistribution")
registerBrowserTest("browserWasmTest", "wasmJs", "wasmJsBrowserDistribution")

tasks.register<Test>("driverProbeTest") {
    group = "verification"
    description = "Launches each Gradle-pinned browser without loading application data."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    include("**/DriverProbeTest.class")
    maxParallelForks = 1
    outputs.upToDateWhen { false }
    providers.gradleProperty("polski.browser.denyEngine").orNull?.let {
        systemProperty("polski.browser.denyEngine", it)
    }
}

tasks.register<Test>("distributionIdentityTest") {
    group = "verification"
    description = "Verifies distinct fresh JS and Wasm branch artifacts without launching the app."
    dependsOn(":composeApp:jsBrowserDistribution", ":composeApp:wasmJsBrowserDistribution")
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    include("**/DistributionIdentityTest.class")
    maxParallelForks = 1
    outputs.upToDateWhen { false }
    systemProperty("polski.browser.worktree", rootProject.projectDir.parentFile.absolutePath)
}

tasks.register<JavaExec>("playwright") {
    group = "verification"
    description = "Runs the Gradle-pinned Playwright Java CLI, for example --args=install."
    classpath = configurations.testRuntimeClasspath.get()
    mainClass.set("com.microsoft.playwright.CLI")
}
