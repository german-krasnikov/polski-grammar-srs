// Generated from kotlin/shared/src/commonMain/kotlin/polski/data/PlRuVocabularyRows.kt by dsl/derive-enru-mappers.mjs: the en-ru copy of that file. Edit the pl-ru file, then run biome generate.
package polski.data

import polski.data.rows.enru.*

/**
 * R1.8b: the en-ru vocabulary cards and the chain of the training from the generated rows (the stem alternations are in EnRuLexiconRows.kt).
 * The ids of the cards and of the chain steps are unique (the contract cannot see inside a list).
 */
private val vocabularyRow = VOCABULARY_ROW_ROWS.single()

internal val generatedEnRuVocabularyItems: List<VocabularyItem> = vocabularyRow.toItems()
internal val generatedEnRuVocabularyInstructions = vocabularyRow.instructions.let { CourseVocabularyInstructions(it.react, it.web, it.native, it.ios) }
internal val generatedEnRuVocabularyUnavailableLabel: String = vocabularyRow.unavailableLabel

internal val generatedEnRuChainPresentation = TRAINING_ROW_ROWS.single().toChain()

internal fun VocabularyRow.toItems(): List<VocabularyItem> {
    require(items.map { it.id }.toSet().size == items.size) { "a vocabulary card id is used twice" }
    return items.map { VocabularyItem(it.id, it.lemma, it.translation, it.form, it.example, it.level, it.frequencyRank) }
}

internal fun TrainingRow.toChain(): ChainPresentation {
    val steps = chainPresentation.steps
    require(steps.map { it.id }.toSet().size == steps.size) { "a chain step id is used twice" }
    return ChainPresentation(
        steps.map { ChainStep(it.id, it.label) },
        chainPresentation.completion.let { ChainCompletion(it.title, it.reactEyebrow, it.reactBody, it.webBody) },
    )
}
