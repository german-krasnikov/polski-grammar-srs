plugins {
    id("polski.kmp-common")
}

kotlin {
    sourceSets {
        commonMain {
            // Logic/data artifacts printed by `biome generate --project dsl/biome.project` (committed, not hand-edited).
            kotlin.srcDir("src/commonMain/generated/kotlin")
        }
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
        }
        commonTest {
            kotlin.srcDir("src/commonTest/generated/kotlin")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
