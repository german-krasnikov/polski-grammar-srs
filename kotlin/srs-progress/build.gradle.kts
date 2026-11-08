plugins {
    id("polski.kmp-common")
}

kotlin {
    sourceSets {
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
