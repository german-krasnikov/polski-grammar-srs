package polski.data

import polski.data.rows.plru.*
import polski.presentation.StemAlternation

/**
 * R1.8b: the pl-ru vocabulary cards, the chain of the training and the stem alternations from the generated rows.
 * The ids of the cards and of the chain steps are unique (the contract cannot see inside a list); a stem alternation is two single letters.
 */
private val vocabularyRow = VOCABULARY_ROW_ROWS.single()

internal val generatedPlRuVocabularyItems: List<VocabularyItem> = vocabularyRow.toItems()
internal val generatedPlRuVocabularyInstructions = vocabularyRow.instructions.let { CourseVocabularyInstructions(it.react, it.web, it.native, it.ios) }
internal val generatedPlRuVocabularyUnavailableLabel: String = vocabularyRow.unavailableLabel

internal val generatedPlRuChainPresentation = TRAINING_ROW_ROWS.single().toChain()

internal val generatedPlRuStemAlternations: List<StemAlternation> = STEM_ALTERNATION_ROW_ROWS.map { it.toStemAlternation() }

internal fun VocabularyRow.toItems(): List<VocabularyItem> {
    require(items.map { it.id }.toSet().size == items.size) { "a vocabulary card id is used twice" }
    return items.map { VocabularyItem(it.id, it.lemma, it.translation, it.form, it.example, it.level, it.frequencyRank) }
}

internal fun StemAlternationRow.toStemAlternation() = StemAlternation(a.single(), b.single())

internal fun TrainingRow.toChain(): ChainPresentation {
    val steps = chainPresentation.steps
    require(steps.map { it.id }.toSet().size == steps.size) { "a chain step id is used twice" }
    return ChainPresentation(
        steps.map { ChainStep(it.id, it.label) },
        chainPresentation.completion.let { ChainCompletion(it.title, it.reactEyebrow, it.reactBody, it.webBody) },
    )
}
