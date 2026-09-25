package polski.training

import polski.data.nounById
import polski.data.courseSentenceSeeds
import polski.data.exerciseCopy
import polski.data.personalPronouns
import polski.data.renderCoursePattern
import polski.data.skillById
import polski.grammar.capitalize
import polski.grammar.caseSentence
import polski.grammar.nounPhrase
import polski.grammar.verbForm
import polski.model.*

/** Values must be in [0, 1); implementations may use any repeatable or platform source. */
fun interface RandomSource { fun nextDouble(): Double }

/** IDs are independent of content randomness and must be nonempty. */
fun interface ExerciseIdFactory { fun newId(): String }

val sentenceSeeds: List<SentenceSeed> by lazy { courseSentenceSeeds }

/** Pure content factory except for the caller-owned random and ID ports. */
class ExerciseFactory(private val random: RandomSource, private val ids: ExerciseIdFactory) {
    private fun sentence(key: String, vararg values: Pair<String, String>): String = renderCoursePattern(key, mapOf(*values))
    private fun <T> pick(items: List<T>): T {
        val draw = random.nextDouble()
        require(draw >= 0.0 && draw < 1.0) { "RandomSource must return [0, 1)" }
        return items[(draw * items.size).toInt()]
    }

    private fun phrase(seed: SentenceSeed, gramCase: GramCase, owner: PossessiveId = PossessiveId.MY, number: NumberGram = NumberGram.SG): String =
        nounPhrase(seed.nounId, gramCase, number, seed.adjectiveId, owner)

    private fun card(
        skill: String, seed: SentenceSeed, source: String, prompt: String, expected: String,
        explanation: String, changes: List<FormChange>, accepted: List<String> = emptyList(),
        nounId: String = seed.nounId, adjectiveId: String = seed.adjectiveId,
        possessive: PossessiveId = PossessiveId.MY, number: NumberGram = NumberGram.SG,
        tags: List<String> = listOf(skill),
    ): Exercise {
        val id = ids.newId()
        require(id.isNotEmpty()) { "Exercise ID must be nonempty" }
        return Exercise(id, skill, source, prompt, expected, accepted, explanation, tags, nounId, adjectiveId, possessive, number, changes)
    }

    fun generateForSkill(skillId: String, preferredSeed: SentenceSeed? = null): Exercise {
        skillById(skillId)
        val candidates = when (skillId) {
            "case.acc.f" -> sentenceSeeds.filter { nounById(it.nounId).gender == Gender.F }
            "case.acc.m" -> sentenceSeeds.filter { nounById(it.nounId).gender.id.startsWith("m-") }
            "case.acc.n" -> sentenceSeeds.filter { nounById(it.nounId).gender == Gender.N }
            "case.inst", "case.dat", "pronouns", "verb.present", "verb.past", "verb.future" ->
                sentenceSeeds.filter { it.nounId in setOf("wife", "husband", "friendM", "son", "child") }
            else -> sentenceSeeds
        }
        val seed = preferredSeed?.takeIf { preferred -> candidates.any { it.nounId == preferred.nounId } } ?: pick(candidates)
        val noun = nounById(seed.nounId)
        val nom = phrase(seed, GramCase.NOM)
        val acc = phrase(seed, GramCase.ACC)
        val gen = phrase(seed, GramCase.GEN)
        fun change(from: String, to: String, reason: String) = listOf(FormChange(from, to, reason))
        if (skillId in setOf("case.acc.f", "case.acc.m", "case.acc.n")) {
            val reason = when {
                noun.lemma.endsWith('a') && noun.gender == Gender.M_PERSONAL -> exerciseCopy("accReasonPersonalA")
                noun.gender == Gender.M_INANIMATE || noun.gender == Gender.N -> exerciseCopy("accReasonUnchanged")
                else -> exerciseCopy("accReasonChanged")
            }
            return card(skillId, seed, caseSentence(seed, GramCase.NOM), exerciseCopy("accPrompt"), sentence("seenAcc", "acc" to acc), exerciseCopy("accExplanation"), change(nom, acc, reason), tags = listOf("acc", noun.gender.id))
        }
        if (skillId == "case.gen.neg") return card(skillId, seed, sentence("seenAcc", "acc" to acc), exerciseCopy("genNegPrompt"), sentence("seenGenNeg", "gen" to gen), exerciseCopy("genNegExplanation"), change(acc, gen, exerciseCopy("genNegReason")), tags = listOf("gen", "negation"))
        val drill = when (skillId) {
            "case.inst" -> Triple(GramCase.INST, exerciseCopy("instStart"), exerciseCopy("instPrompt"))
            "case.loc" -> Triple(GramCase.LOC, exerciseCopy("locStart"), exerciseCopy("locPrompt"))
            "case.dat" -> Triple(GramCase.DAT, exerciseCopy("datStart"), exerciseCopy("datPrompt"))
            else -> null
        }
        if (drill != null) {
            val target = phrase(seed, drill.first)
            val reason = when (drill.first) {
                GramCase.INST -> exerciseCopy("instReason")
                GramCase.LOC -> exerciseCopy("locReason")
                else -> exerciseCopy("datReason")
            }
            return card(skillId, seed, sentence("seenAcc", "acc" to acc), drill.third, sentence("caseDrill", "start" to drill.second, "target" to target), exerciseCopy("caseDrillExplanation"), change(acc, target, reason), tags = listOf(drill.first.id))
        }
        if (skillId == "agreement.my") {
            val owner = pick(listOf(PossessiveId.YOUR, PossessiveId.HIS, PossessiveId.HER, PossessiveId.OUR, PossessiveId.YOUR_PLURAL, PossessiveId.THEIR))
            val label = mapOf(PossessiveId.YOUR to exerciseCopy("ownerYour"), PossessiveId.HIS to exerciseCopy("ownerHis"),
                PossessiveId.HER to exerciseCopy("ownerHer"), PossessiveId.OUR to exerciseCopy("ownerOur"),
                PossessiveId.YOUR_PLURAL to exerciseCopy("ownerYourPlural"), PossessiveId.THEIR to exerciseCopy("ownerTheir")).getValue(owner)
            val target = phrase(seed, GramCase.ACC, owner)
            return card(skillId, seed, sentence("seenAcc", "acc" to acc), exerciseCopy("ownerPromptPrefix") + label + exerciseCopy("ownerPromptSuffix"),
                sentence("seenAcc", "acc" to target), exerciseCopy("ownerExplanation"), change(acc, target, exerciseCopy("ownerReason")),
                possessive = owner, tags = listOf("agreement", "acc"))
        }
        if (skillId.startsWith("verb.")) {
            val tense = Tense.fromId(skillId.substringAfter('.'))
            val fromTense = if (tense == Tense.PRESENT) Tense.PAST else Tense.PRESENT
            val from = verbForm("go", fromTense, Person.THIRD, NumberGram.SG, noun.gender)
            val to = verbForm("go", tense, Person.THIRD, NumberGram.SG, noun.gender)
            val prompt = when (tense) {
                Tense.PAST -> exerciseCopy("verbPastPrompt")
                Tense.FUTURE -> exerciseCopy("verbFuturePrompt")
                Tense.PRESENT -> exerciseCopy("verbPresentPrompt")
            }
            val accepted = if (tense == Tense.FUTURE) listOf(sentence("verbFutureAccepted", "nom" to capitalize(nom), "past" to verbForm("go", Tense.PAST, Person.THIRD, NumberGram.SG, noun.gender))) else emptyList()
            return card(skillId, seed, sentence("verbSentence", "nom" to capitalize(nom), "verb" to from), prompt, sentence("verbSentence", "nom" to capitalize(nom), "verb" to to),
                exerciseCopy("verbExplanation"), change(from, to, exerciseCopy("verbReason")), accepted = accepted,
                tags = listOf("verb", tense.id, "nom"))
        }
        if (skillId == "aspect") return card(skillId, seed, exerciseCopy("aspectSource"), exerciseCopy("aspectPrompt"),
            exerciseCopy("aspectExpected"), exerciseCopy("aspectExplanation"),
            change(exerciseCopy("aspectFrom"), exerciseCopy("aspectTo"), exerciseCopy("aspectReason")),
            accepted = listOf(exerciseCopy("aspectAccepted")), nounId = "book", adjectiveId = "new", tags = listOf("aspect", "past"))
        if (skillId == "pronouns") {
            val key = when (noun.gender) { Gender.F -> "ona"; Gender.N -> "ono"; else -> "on" }
            val pronoun = personalPronouns.getValue(key).getValue(GramCase.ACC)
            return card(skillId, seed, sentence("seenAcc", "acc" to acc), exerciseCopy("pronounPrompt"), sentence("seenAcc", "acc" to pronoun),
                exerciseCopy("pronounExplanation"), change(acc, pronoun, exerciseCopy("pronounReason")), tags = listOf("pronoun", "acc"))
        }
        if (skillId == "sentence.question") return card(skillId, seed, sentence("questionSource", "acc" to acc), exerciseCopy("questionPrompt"),
            sentence("questionExpected", "acc" to acc), exerciseCopy("questionExplanation"),
            change(exerciseCopy("questionChangeFrom"), exerciseCopy("questionChangeTo"), exerciseCopy("questionReason")), tags = listOf("question", "acc"))
        if (skillId == "sentence.plural") {
            val target = phrase(seed, GramCase.ACC, number = NumberGram.PL)
            return card(skillId, seed, sentence("seenAcc", "acc" to acc), exerciseCopy("pluralPrompt"), sentence("seenAcc", "acc" to target),
                exerciseCopy("pluralExplanation"), change(acc, target, exerciseCopy("pluralReason")),
                number = NumberGram.PL, tags = listOf("plural", "acc"))
        }
        if (skillId == "mixed") return card(skillId, seed, sentence("seenAcc", "acc" to acc), exerciseCopy("mixedPrompt"),
            sentence("mixedMale", "gen" to gen), exerciseCopy("mixedExplanation"),
            listOf(FormChange(exerciseCopy("mixedChangeFrom"), exerciseCopy("mixedChangeTo"), exerciseCopy("mixedVerbReason")),
                FormChange(acc, gen, exerciseCopy("mixedNounReason"))),
            accepted = listOf(sentence("mixedFemale", "gen" to gen)), tags = listOf("mixed", "gen", "past"))
        error("Unknown skill $skillId")
    }

    fun generateChain(seed: SentenceSeed = sentenceSeeds.first()): List<Exercise> {
        val nom = phrase(seed, GramCase.NOM)
        val acc = phrase(seed, GramCase.ACC)
        val gen = phrase(seed, GramCase.GEN)
        val theirGen = phrase(seed, GramCase.GEN, PossessiveId.THEIR)
        val theirLoc = phrase(seed, GramCase.LOC, PossessiveId.THEIR)
        fun step(skill: String, source: String, prompt: String, expected: String, from: String, to: String, reason: String, owner: PossessiveId = PossessiveId.MY, tags: List<String>) =
            card(skill, seed, source, prompt, expected, reason, listOf(FormChange(from, to, reason)), possessive = owner, tags = tags)
        val firstSkill = when (nounById(seed.nounId).gender) { Gender.F -> "case.acc.f"; Gender.N -> "case.acc.n"; else -> "case.acc.m" }
        return listOf(
            step(firstSkill, caseSentence(seed, GramCase.NOM), exerciseCopy("chainAccPrompt"), sentence("seenAcc", "acc" to acc), nom, acc,
                exerciseCopy("chainAccReason"), tags = listOf("acc")),
            step("verb.past", sentence("seenAcc", "acc" to acc), exerciseCopy("chainPastPrompt"), sentence("chainPast", "acc" to acc), exerciseCopy("chainPastChangeFrom"), exerciseCopy("chainPastChangeTo"),
                exerciseCopy("chainPastReason"), tags = listOf("past", "acc")),
            step("case.gen.neg", sentence("chainPast", "acc" to acc), exerciseCopy("chainNegPrompt"), sentence("chainNeg", "gen" to gen), acc, gen,
                exerciseCopy("chainNegReason"), tags = listOf("gen", "negation")),
            step("agreement.my", sentence("chainNeg", "gen" to gen), exerciseCopy("chainOwnerPrompt"), sentence("chainOwner", "theirGen" to theirGen),
                gen, theirGen, exerciseCopy("chainOwnerReason"), PossessiveId.THEIR, listOf("gen", "agreement")),
            step("case.loc", sentence("chainOwner", "theirGen" to theirGen), exerciseCopy("chainLocPrompt"), sentence("chainLoc", "theirLoc" to theirLoc),
                theirGen, theirLoc, exerciseCopy("chainLocReason"), PossessiveId.THEIR, listOf("loc")),
        )
    }
}
