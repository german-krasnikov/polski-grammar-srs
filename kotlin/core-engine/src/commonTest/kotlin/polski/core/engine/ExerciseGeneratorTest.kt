package polski.core.engine

import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.core.model.ScaffoldingLevel
import polski.core.model.SkillSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * EnRuPackPlan.md §5 gap A / task EN-02: a synthetic (not pl, not en) construction whose lexical
 * slot is a pack-level constant that is present or absent depending on the *resolved*
 * [polski.core.model.FeatureBundle] of each phrase — never from the drilled seed. This is the
 * capability [ConstantSlot] adds; `pl`'s own recipes pass none and are unaffected (see the golden
 * `TrainingParityTest`/`GrammarParityTest`).
 */
class ExerciseGeneratorTest {
    private val morphology: Morphology = TableMorphology(
        mapOf(
            "noun:thing" to mapOf(bundle("Number" to "sg") to "thing"),
            "noun:other" to mapOf(bundle("Number" to "sg") to "other"),
            "particle:not" to mapOf(emptyMap<FeatureKey, FeatureValue>() to "not"),
        ),
    )

    private fun bundle(vararg pairs: Pair<String, String>) = pairs.associate { (k, v) -> FeatureKey(k) to FeatureValue(v) }

    private val template = ConstructionTemplate(
        slots = listOf(
            SlotTemplate("particle", category = "particle", optional = true),
            SlotTemplate("noun", category = "noun", requiredFeatures = listOf("Number")),
        ),
    )

    private val realizer = ConstructionRealizer(mapOf("test.slot-presence" to template), morphology, LexemeFeatures { emptyMap() })
    private val testSkill = SkillSpec(id = "test.skill", construction = "test.slot-presence")

    private fun generator(
        constantSlots: List<ConstantSlot>,
        recipeOverride: SkillRecipe? = null,
        pattern: PackPattern = PackPattern { _, _ -> error("not used") },
        casePrefix: CasePrefix = CasePrefix { _, _ -> error("not used") },
        seeds: List<Map<String, String>> = listOf(mapOf("noun" to "thing", "adjective" to "unused")),
        random: RandomSource = RandomSource { 0.0 },
    ): ExerciseGenerator {
        val recipe = SkillRecipe(
            skillId = "test.skill",
            source = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Pos")))),
            expected = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Neg")))),
        )
        return ExerciseGenerator(
            realizer = realizer, morphology = morphology, lexemeFeatures = LexemeFeatures { emptyMap() },
            skills = mapOf(testSkill.id to testSkill), recipes = mapOf(recipe.skillId to (recipeOverride ?: recipe)),
            seeds = seeds,
            random = random, ids = ExerciseIdFactory { "generated" },
            copy = PackCopy { it }, pattern = pattern,
            casePrefix = casePrefix, pronouns = PronounForms { _, _ -> error("not used") },
            textCase = TextCase { it }, defaultOwnerLexeme = "unused-owner", verbLexeme = "unused-verb",
            constantSlots = constantSlots,
        )
    }

    /** Draws a fixed queue of values in order; errors loudly (not silently wraps) once exhausted —
     *  a test that over-draws has a wrong assumption about how many random decisions happen. */
    private class QueueRandomSource(draws: List<Double>) : RandomSource {
        private val queue = draws.toMutableList()
        override fun nextDouble(): Double = queue.removeFirstOrNull() ?: error("QueueRandomSource exhausted")
    }

    @Test fun constantSlotIsPresentOnlyInPhrasesWhoseResolvedBundleMatchesItsCondition() {
        val generator = generator(listOf(ConstantSlot(slot = "particle", lexeme = "not", whenFeature = "Polarity", whenValues = listOf("Neg"))))
        val exercise = generator.generateForSkill("test.skill")
        assertEquals("thing", exercise.source, "Polarity=Pos must not draw the conditional constant slot")
        assertEquals("not thing", exercise.expected, "Polarity=Neg must draw the conditional constant slot")
    }

    @Test fun constantSlotWithNoConditionIsPresentInEveryPhrase() {
        val generator = generator(listOf(ConstantSlot(slot = "particle", lexeme = "not")))
        val exercise = generator.generateForSkill("test.skill")
        assertEquals("not thing", exercise.source)
        assertEquals("not thing", exercise.expected)
    }

    @Test fun noConstantSlotsLeavesTheOptionalSlotAbsentAsBefore() {
        val generator = generator(emptyList())
        val exercise = generator.generateForSkill("test.skill")
        assertEquals("thing", exercise.source)
        assertEquals("thing", exercise.expected)
    }

    // M4: two consecutive dynamic-seed draws for the same skill must never repeat the same
    // (noun, adjective) pair when ≥2 lexical candidates exist, even when the RandomSource would
    // otherwise pick the same index both times.
    @Test fun generateForSkillNeverRepeatsTheSameSeedTwiceInARowWhenMultipleCandidatesExist() {
        val seeds = listOf(mapOf("noun" to "thing", "adjective" to "a"), mapOf("noun" to "other", "adjective" to "b"))
        val generator = generator(emptyList(), seeds = seeds, random = QueueRandomSource(listOf(0.0, 0.0)))
        val first = generator.generateForSkill("test.skill")
        val second = generator.generateForSkill("test.skill")
        kotlin.test.assertNotEquals(seedKey(first.slots), seedKey(second.slots))
    }

    // M4: a skill whose lexical filter leaves exactly one candidate must keep generating every
    // call (no throw, no stall) — the anti-repeat filter falls back to the unfiltered pool.
    @Test fun generateForSkillWithExactlyOneCandidateNeverStalls() {
        val generator = generator(emptyList(), random = QueueRandomSource(listOf(0.0, 0.0, 0.0)))
        val exercises = (1..3).map { generator.generateForSkill("test.skill") }
        assertTrue(exercises.all { seedKey(it.slots) == "thing:unused" })
    }

    // M4: an explicit preferredSeed is never second-guessed by the anti-repeat filter, even if it
    // equals the immediately preceding seed.
    @Test fun preferredSeedIsReturnedEvenWhenItMatchesTheLastServedSeed() {
        val seeds = listOf(mapOf("noun" to "thing", "adjective" to "a"), mapOf("noun" to "other", "adjective" to "b"))
        val generator = generator(emptyList(), seeds = seeds, random = QueueRandomSource(listOf(0.0, 0.0)))
        val first = generator.generateForSkill("test.skill", preferredSeed = mapOf("noun" to "thing", "adjective" to "a"))
        val second = generator.generateForSkill("test.skill", preferredSeed = mapOf("noun" to "thing", "adjective" to "a"))
        assertEquals("thing:a", seedKey(first.slots))
        assertEquals("thing:a", seedKey(second.slots))
    }

    // M5: a weakSeedCount marking one candidate's seed as the only one with errors — a draw below
    // 0.5 (the explore/exploit decision) must return that candidate.
    @Test fun weakSeedCountBelowHalfPicksTheOnlyWeakCandidate() {
        val seeds = listOf(mapOf("noun" to "thing", "adjective" to "a"), mapOf("noun" to "other", "adjective" to "b"))
        // Draw 1 (explore/exploit): 0.0 < 0.5 -> explore. Draw 2 (pick over the weak subset, size 1): 0.0.
        val generator = generator(emptyList(), seeds = seeds, random = QueueRandomSource(listOf(0.0, 0.0)))
        val weakSeedCount: (String) -> Int = { key -> if (key == "other:b") 3 else 0 }
        val exercise = generator.generateForSkill("test.skill", weakSeedCount = weakSeedCount)
        assertEquals("other:b", seedKey(exercise.slots))
    }

    // M5: a draw at/above 0.5 falls back to uniform selection over the whole (M4-filtered) pool,
    // ignoring weakSeedCount entirely.
    @Test fun weakSeedCountAtOrAboveHalfFallsBackToUniformSelection() {
        val seeds = listOf(mapOf("noun" to "thing", "adjective" to "a"), mapOf("noun" to "other", "adjective" to "b"))
        // Draw 1 (explore/exploit): 0.5 -> exploit uniform. Draw 2 (uniform pick over 2 candidates): 0.0 -> index 0.
        val generator = generator(emptyList(), seeds = seeds, random = QueueRandomSource(listOf(0.5, 0.0)))
        val weakSeedCount: (String) -> Int = { key -> if (key == "other:b") 3 else 0 }
        val exercise = generator.generateForSkill("test.skill", weakSeedCount = weakSeedCount)
        assertEquals("thing:a", seedKey(exercise.slots))
    }

    // M5 regression guard: omitting weakSeedCount (default null) draws exactly once per call and
    // reproduces the pre-M5 uniform behavior — same assertion shape as the M4 tests above.
    @Test fun noWeakSeedCountReproducesUniformBehaviorWithOneDrawPerCall() {
        val generator = generator(emptyList(), random = QueueRandomSource(listOf(0.0)))
        val exercise = generator.generateForSkill("test.skill")
        assertEquals("thing:unused", seedKey(exercise.slots))
    }

    @Test fun comparisonPracticeCyclesThroughDistinctAuthoredSentences() {
        fun exercise(key: String) = StaticExercise(key, "prompt", "$key expected", "explanation", emptyList(), key, "$key expected", "reason", "thing", "unused", emptyList())
        val pool = listOf("A", "B", "C", "D", "E", "F").map(::exercise)
        val generator = generator(emptyList(), SkillRecipe("test.skill", staticPool = pool))
        val firstCycle = (1..6).map { generator.generateForSkill("test.skill").source }
        assertEquals(pool.map { it.sourceKey }.toSet(), firstCycle.toSet())
        val seventh = generator.generateForSkill("test.skill").source
        kotlin.test.assertNotEquals(firstCycle.last(), seventh)
    }

    // SB-5 blueprint §6 AC5.2/AC5.3: tileTargetSlots opts a constructed recipe into CoreExercise.tilePhrase; absent stays empty/null.
    @Test fun recipeWithTileTargetSlotsProducesATilePhraseForItsSingleEmbeddedPhrase() {
        val recipe = SkillRecipe(
            skillId = "test.skill",
            source = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Pos")))),
            expected = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Neg")))),
            tileTargetSlots = listOf("noun"),
        )
        val exercise = generator(emptyList(), recipe).generateForSkill("test.skill")
        assertEquals(listOf("noun"), exercise.tileTargetSlots)
        val tilePhrase = exercise.tilePhrase
        kotlin.test.assertNotNull(tilePhrase)
        assertEquals("test.slot-presence", tilePhrase.construction)
        assertEquals(mapOf("Number" to "sg", "Polarity" to "Neg"), tilePhrase.bundle)
        assertEquals(exercise.expected, tilePhrase.text)
        assertEquals(setOf("noun"), tilePhrase.slotSpans.keys)
    }

    @Test fun recipeWithoutTileTargetSlotsLeavesTileFieldsAtTheirDefaults() {
        val exercise = generator(emptyList()).generateForSkill("test.skill")
        assertEquals(emptyList(), exercise.tileTargetSlots)
        assertNull(exercise.tilePhrase)
    }

    // Reviewer-flagged gap: real pl-ru recipes (e.g. case.acc.f) use expected = Pattern(key, {one named value is the sole embedded Phrase}) —
    // only Direct was covered before. singlePhraseSpec's Pattern branch, and TileGenerator finding that phrase inside the rendered text, now both have a test.
    @Test fun patternShapedExpectedIsolatesItsSoleEmbeddedPhraseAndTileGeneratorFindsItInTheRenderedText() {
        val patternPhrase = PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Neg"))
        val recipe = SkillRecipe(
            skillId = "test.skill",
            source = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Pos")))),
            expected = TextSpec.Pattern("seen", mapOf("acc" to TextValue.Phrase(patternPhrase))),
            tileTargetSlots = listOf("noun"),
        )
        val pattern = PackPattern { key, values -> "<$key: " + values.entries.joinToString(", ") { (k, v) -> "$k=$v" } + ">" }
        val exercise = generator(emptyList(), recipe, pattern = pattern).generateForSkill("test.skill")

        assertEquals("<seen: acc=thing>", exercise.expected)
        val tilePhrase = exercise.tilePhrase
        assertNotNull(tilePhrase)
        assertEquals("test.slot-presence", tilePhrase.construction)
        assertEquals(mapOf("Number" to "sg", "Polarity" to "Neg"), tilePhrase.bundle)
        assertEquals("thing", tilePhrase.text)
        assertTrue(exercise.expected.indexOf(tilePhrase.text) >= 0, "TileGenerator.fromAnswerPhrase locates the phrase by substring; it must be present")

        val tileGenerator = TileGenerator(realizer, morphology as MorphologyParadigm, mapOf(testSkill.id to testSkill), RandomSource { 0.0 })
        assertNotNull(tileGenerator.fromExercise(exercise, ScaffoldingLevel.None), "TileGenerator must build real tiles for a Pattern-shaped expected, not null")
    }

    @Test fun concatShapedExpectedIsolatesItsSoleEmbeddedPhraseAmongItsParts() {
        val concatPhrase = PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Neg"))
        val recipe = SkillRecipe(
            skillId = "test.skill",
            source = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Pos")))),
            expected = TextSpec.Concat(listOf(TextValue.Copy("open:"), TextValue.Phrase(concatPhrase))),
            tileTargetSlots = listOf("noun"),
        )
        val exercise = generator(emptyList(), recipe).generateForSkill("test.skill")

        assertEquals("open:thing", exercise.expected)
        val tilePhrase = exercise.tilePhrase
        assertNotNull(tilePhrase)
        assertEquals("test.slot-presence", tilePhrase.construction)
        assertEquals("thing", tilePhrase.text)
        assertTrue(exercise.expected.indexOf(tilePhrase.text) >= 0)
    }

    @Test fun prefixedExpectedWithANonVocativePrefixCaseIsolatesItsOwnPhrase() {
        val prefixedPhrase = PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Neg"))
        val recipe = SkillRecipe(
            skillId = "test.skill",
            source = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Pos")))),
            expected = TextSpec.Prefixed(prefixedPhrase, prefixCase = "nom", punct = "."),
            tileTargetSlots = listOf("noun"),
        )
        val casePrefix = CasePrefix { caseId, _ -> "[$caseId]" }
        val exercise = generator(emptyList(), recipe, casePrefix = casePrefix).generateForSkill("test.skill")

        assertEquals("[nom] thing.", exercise.expected)
        val tilePhrase = exercise.tilePhrase
        assertNotNull(tilePhrase)
        assertEquals("test.slot-presence", tilePhrase.construction)
        assertEquals("thing", tilePhrase.text)
        assertTrue(exercise.expected.indexOf(tilePhrase.text) >= 0)
    }

    // P1 blueprint §P1.3 (AC6): poolOf is a pure function of (skillId, seedKey) — same inputs,
    // same pool, every time, no RandomSource involved.
    @Test fun poolOfIsDeterministicAndStableAcrossRepeatedCalls() {
        val seed = mapOf("noun" to "thing", "adjective" to "a")
        val first = poolOf("test.skill", seed)
        repeat(10) { assertEquals(first, poolOf("test.skill", seed)) }
    }

    @Test fun poolOfPartitionsBySeedContentNotByExerciseId() {
        // A different skillId or a different seed can land in a different pool; the same
        // (skillId, seed) pair never does, regardless of how many freshly-minted exercise IDs
        // were issued in between — the partition key is content, not `ExerciseIdFactory.newId()`.
        val seedA = mapOf("noun" to "thing", "adjective" to "a")
        val seedB = mapOf("noun" to "other", "adjective" to "a")
        assertEquals(poolOf("test.skill", seedA), poolOf("test.skill", seedA))
        // Not every (skillId, seed) need differ, but the function must not depend on call order/count.
        repeat(5) { poolOf("test.skill", seedB) }
        assertEquals(poolOf("test.skill", seedA), poolOf("test.skill", seedA))
    }

    // M4: two consecutive dynamic-seed draws for the same skill must never repeat the same
    @Test fun generateForSkillNeverServesAHeldOutProbeSeed() {
        // Brute-force over a large adjective range (noun fixed to a morphology-known id) to find
        // a concrete Training seed and a concrete non-Training (probe) seed for this test's skill
        // — grounded in poolOf's real partition, not an assumption about which id lands where.
        val candidates = (0 until 500).map { i -> mapOf("noun" to "thing", "adjective" to "a$i") }
        val trainingSeed = candidates.first { poolOf("test.skill", it) == ContentPool.Training }
        val probeSeed = candidates.first { poolOf("test.skill", it) != ContentPool.Training }
        val seeds = listOf(trainingSeed, probeSeed)
        val generator = generator(emptyList(), seeds = seeds, random = RandomSource { kotlin.random.Random.nextDouble() })
        repeat(50) {
            val exercise = generator.generateForSkill("test.skill", excludeProbePool = true)
            assertEquals(seedKey(trainingSeed), seedKey(exercise.slots), "a probe seed must never surface as an ordinary exercise")
        }
    }

    // Regression guard: excludeProbePool defaults to false, reproducing the exact pre-P1 uniform
    // pool (byte-parity with TrainingParityTest's pinned React-oracle fixture) — a probe seed CAN
    // be drawn unless a host explicitly opts in.
    @Test fun generateForSkillWithoutExcludeProbePoolCanStillDrawAProbeSeed() {
        val candidates = (0 until 500).map { i -> mapOf("noun" to "thing", "adjective" to "a$i") }
        val probeSeed = candidates.first { poolOf("test.skill", it) != ContentPool.Training }
        val generator = generator(emptyList(), seeds = listOf(probeSeed), random = QueueRandomSource(listOf(0.0)))
        val exercise = generator.generateForSkill("test.skill")
        assertEquals(seedKey(probeSeed), seedKey(exercise.slots))
    }

    @Test fun generateForSkillFallsBackToTheFullPoolWhenEveryCandidateIsAProbeSeed() {
        // Fail-soft: a skill whose entire candidate pool happens to hash into a probe bucket must
        // still generate every time, not stall — same shape as M4's own anti-repeat fallback.
        val candidates = (0 until 500).map { i -> mapOf("noun" to "thing", "adjective" to "a$i") }
        val onlyProbeSeed = candidates.first { poolOf("test.skill", it) != ContentPool.Training }
        val generator = generator(emptyList(), seeds = listOf(onlyProbeSeed), random = QueueRandomSource(listOf(0.0, 0.0)))
        val exercise = generator.generateForSkill("test.skill", excludeProbePool = true)
        assertEquals(seedKey(onlyProbeSeed), seedKey(exercise.slots))
    }

    @Test fun generateProbeExerciseOnlyDrawsFromItsRequestedWindow() {
        val candidates = (0 until 500).map { i -> mapOf("noun" to "thing", "adjective" to "a$i") }
        val byPool = ContentPool.entries.associateWith { pool -> candidates.filter { poolOf("test.skill", it) == pool } }
        val seeds = (byPool[ContentPool.Training].orEmpty().take(2) +
            byPool[ContentPool.ProbeD7].orEmpty().take(2) + byPool[ContentPool.ProbeD30].orEmpty().take(2))
        val generator = generator(emptyList(), seeds = seeds, random = RandomSource { kotlin.random.Random.nextDouble() })
        repeat(30) {
            val exercise = generator.generateProbeExercise("test.skill", ContentPool.ProbeD7)
            assertEquals(ContentPool.ProbeD7, poolOf("test.skill", exercise.slots))
        }
        repeat(30) {
            val exercise = generator.generateProbeExercise("test.skill", ContentPool.ProbeD30)
            assertEquals(ContentPool.ProbeD30, poolOf("test.skill", exercise.slots))
        }
    }

    @Test fun generateProbeExerciseRejectsTheTrainingWindow() {
        val generator = generator(emptyList())
        assertFailsWith<IllegalArgumentException> { generator.generateProbeExercise("test.skill", ContentPool.Training) }
    }

    // prefixCase == "voc" capitalizes the phrase (TextCase), breaking verbatim substring matching against expected — singlePhraseSpec must refuse it, fail-soft.
    @Test fun prefixedExpectedWithAVocativePrefixCaseIsIneligibleForTiles() {
        val prefixedPhrase = PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Neg"))
        val recipe = SkillRecipe(
            skillId = "test.skill",
            source = TextSpec.Direct(TextValue.Phrase(PhraseSpec("test.slot-presence", mapOf("Number" to "sg", "Polarity" to "Pos")))),
            expected = TextSpec.Prefixed(prefixedPhrase, prefixCase = "voc", punct = "!"),
            tileTargetSlots = listOf("noun"),
        )
        val exercise = generator(emptyList(), recipe).generateForSkill("test.skill")
        assertEquals(listOf("noun"), exercise.tileTargetSlots)
        assertNull(exercise.tilePhrase)
    }
}
