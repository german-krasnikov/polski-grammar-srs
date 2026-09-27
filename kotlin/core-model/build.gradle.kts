// UniversalCorePlan.md §4.1 UC-01: FeatureKey/FeatureValue/FeatureBundle/Construction/SkillSpec —
// the universal core's open feature catalog. Never imports a concrete language, pair or host.
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
