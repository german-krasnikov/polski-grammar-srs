package polski.exam

import polski.exam.setpolicy.Policy
import polski.exam.setpolicy.data.SET_POLICY_OUTCOME_ROW_ROWS
import polski.grammar.normalize

/** One source fact; all accepted wordings retain this identity and credit. */
data class ExamAcceptedFact(
    val id: String,
    val displayText: String,
    val creditQuarter: Int,
    val aliases: List<String> = emptyList(),
)

enum class ExamSetOverselectionMode(val tag: String) {
    LegacyWrong("legacy_wrong"),
    ManualReview("manual_review");

    companion object {
        fun fromTag(tag: String): ExamSetOverselectionMode = entries.single { it.tag == tag }
    }
}

/** An accepted pool and the number of facts requested by this question. */
data class ExamSetAnswerSpec(
    val requiredSelections: Int,
    val maximumQuarter: Int,
    val facts: List<ExamAcceptedFact>,
    val options: List<String> = emptyList(),
    val gradingMode: ExamGradingMode = ExamGradingMode.Exact,
    val overselectionMode: ExamSetOverselectionMode? = null,
)

/** Build a source set from explicit facts, or from exact printed choices with no aliases. */
fun sourceSetAnswerSpec(
    requiredSelections: Int,
    maximumQuarter: Int,
    facts: List<ExamAcceptedFact>,
    options: List<String>,
    gradingMode: ExamGradingMode,
    overselectionMode: ExamSetOverselectionMode?,
    correctAnswer: String,
    creditPerCorrectQuarter: Int?,
): ExamSetAnswerSpec {
    val resolvedFacts = if (facts.isNotEmpty()) facts else {
        require(gradingMode == ExamGradingMode.Exact && options.isNotEmpty() && creditPerCorrectQuarter != null) {
            "A source set without fact rows requires exact choices and per-choice credit"
        }
        correctAnswer.split("|").map { token ->
            ExamAcceptedFact(normalize(token), token, creditPerCorrectQuarter)
        }
    }
    return ExamSetAnswerSpec(requiredSelections, maximumQuarter, resolvedFacts, options, gradingMode, overselectionMode)
}

internal data class SetPolicyFacts(
    val answerNonblank: Boolean,
    val selectionCount: Int,
    val requiredSelections: Int,
    val distinctKnownFactCount: Int,
    val knownCreditQuarter: Int,
    val maximumQuarter: Int,
    val unknownNonblankCount: Int,
    val hasMalformedSelection: Boolean,
    val hasPerFactCredit: Boolean,
    val openManual: Boolean,
    val sourceConservative: Boolean,
    val pendingBudgetQuarter: Int,
)

/** Resolve a source fact set without assigning a native grading verdict. */
fun evaluateFactSetAnswer(spec: ExamSetAnswerSpec, answer: String): ExamAnswerEvaluation {
    require(spec.maximumQuarter > 0 && spec.requiredSelections > 0 && spec.facts.size >= spec.requiredSelections)
    require(spec.facts.map { it.id }.all { it.isNotBlank() } && spec.facts.map { it.id }.distinct().size == spec.facts.size)
    val weights = spec.facts.map { it.creditQuarter }.distinct()
    require(weights.size == 1 && weights.single() > 0 && spec.requiredSelections.toLong() * weights.single() == spec.maximumQuarter.toLong()) {
        "Source fact credit must equal the question maximum"
    }
    val byText = mutableMapOf<String, String>()
    for (fact in spec.facts) {
        for (text in listOf(fact.displayText) + fact.aliases) {
            val key = normalize(text)
            require(key.isNotBlank()) { "Source fact wording must not be blank" }
            val old = byText[key]
            require(old == null || old == fact.id) { "Ambiguous source fact wording: $key" }
            byText[key] = fact.id
        }
    }
    val choices = spec.options.map(::normalize)
    require(choices.all { it.isNotBlank() } && choices.distinct().size == choices.size) { "Invalid source choices" }
    require(choices.isEmpty() || choices.containsAll(spec.facts.map { normalize(it.displayText) })) {
        "Source choices must contain accepted facts"
    }
    val rawSelections = answer.split("|")
    val selected = rawSelections.map(::normalize).filter { it.isNotBlank() }
    val knownIds = selected.mapNotNull(byText::get).toSet()
    val knownCredit = knownIds.size.toLong() * weights.single()
    require(knownCredit <= Int.MAX_VALUE) { "Source fact credit overflows quarter units" }
    val unknown = selected.count { it !in byText }
    val malformed = (rawSelections.size > spec.requiredSelections && rawSelections.any { normalize(it).isBlank() }) ||
        rawSelections.any { it.isNotBlank() && normalize(it).isBlank() } ||
        selected.any { it !in byText && (choices.isEmpty() && spec.gradingMode == ExamGradingMode.Exact || choices.isNotEmpty() && it !in choices) }
    return evaluateSetPolicy(
        SetPolicyFacts(
            answerNonblank = answer.isNotBlank(),
            selectionCount = selected.size,
            requiredSelections = spec.requiredSelections,
            distinctKnownFactCount = knownIds.size,
            knownCreditQuarter = knownCredit.toInt(),
            maximumQuarter = spec.maximumQuarter,
            unknownNonblankCount = if (malformed) 0 else unknown,
            hasMalformedSelection = malformed,
            hasPerFactCredit = true,
            openManual = spec.gradingMode == ExamGradingMode.OpenManual,
            sourceConservative = spec.overselectionMode == ExamSetOverselectionMode.ManualReview,
            pendingBudgetQuarter = spec.maximumQuarter - knownCredit.coerceAtMost(spec.maximumQuarter.toLong()).toInt(),
        ),
    )
}

internal fun evaluateSetPolicy(facts: SetPolicyFacts): ExamAnswerEvaluation {
    val disposition = Policy.disposition(
        Policy.DispositionInput(
            answerNonblank = facts.answerNonblank,
            selectionCount = facts.selectionCount.toDouble(),
            requiredSelections = facts.requiredSelections.toDouble(),
            distinctKnownFactCount = facts.distinctKnownFactCount.toDouble(),
            knownCreditQuarter = facts.knownCreditQuarter.toDouble(),
            maximumQuarter = facts.maximumQuarter.toDouble(),
            unknownNonblankCount = facts.unknownNonblankCount.toDouble(),
            hasMalformedSelection = facts.hasMalformedSelection,
            hasPerFactCredit = facts.hasPerFactCredit,
            openManual = facts.openManual,
            usesSourceConservativeOverselection = facts.sourceConservative,
        ),
    )
    val outcome = SET_POLICY_OUTCOME_ROW_ROWS.single { it.disposition == disposition }
    fun quarter(source: String): Int = when (source) {
        "maximum" -> facts.maximumQuarter
        "known" -> facts.knownCreditQuarter
        "remaining" -> facts.pendingBudgetQuarter
        "zero" -> 0
        else -> error("Unknown set credit source: $source")
    }
    return ExamAnswerEvaluation(
        ExamAnswerStatus.entries.single { it.tag == outcome.status.tag },
        quarter(outcome.confirmedSource.tag),
        quarter(outcome.pendingSource.tag),
    )
}
