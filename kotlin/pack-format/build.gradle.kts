// UniversalCorePlan.md §4.1 UC-02: CoursePackSource is the seam between how a pack's raw JSON
// reaches Kotlin (embedded at build time today; a future DownloadedPackSource plugs in beside it,
// §4.2) and how :shared parses it. Never imports a concrete language/pair/host.
plugins {
    id("polski.kmp-common")
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
