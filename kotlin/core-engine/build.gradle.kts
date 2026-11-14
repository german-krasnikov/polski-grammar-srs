// UniversalCorePlan.md §4.1/§12 UC-05: Morphology/TableMorphology — the lookup-only realization of
// one lexeme under one FeatureBundle (§1.5: tables at runtime, rules only at build time). Never
// imports a concrete language/pair/host; forms.generated.json (per-language, build-time) is the
// only thing that varies.
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
            api(project(":core-model"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
