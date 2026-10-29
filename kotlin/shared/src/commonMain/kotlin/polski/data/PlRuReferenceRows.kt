package polski.data

import polski.data.rows.plru.*
import polski.model.Gender
import polski.model.GramCase

/**
 * R1.9: the pl-ru reference content from the generated rows. The case ids are read by GramCase.fromId and the skill of a case row is a
 * skill of the pack (the contract checks it); the gender names are keyed by the ids of the model. The teaching texts follow in R1.10.
 */
private val referenceRow = REFERENCE_ROW_ROWS.single()

internal val generatedPlRuMatrixIntroduction: String = referenceRow.matrixIntroduction
internal val generatedPlRuWebCaseCompositionHeader: String = referenceRow.webCaseCompositionHeader
internal val generatedPlRuMaleAccIntro: String = referenceRow.maleAccIntro
internal val generatedPlRuComparisonNounIds: List<String> = referenceRow.comparisonNounIds
internal val generatedPlRuContextHelp = referenceRow.contextHelp.let { CourseContextHelp(it.react, it.compact) }
internal val generatedPlRuAspectNoPresent = referenceRow.aspectNoPresent.let { CourseAspectNoPresent(it.compact, it.ios) }
internal val generatedPlRuReferenceGenderNames: Map<String, String> = referenceRow.genderNames.toNames()
internal val generatedPlRuCaseReferenceRows: List<CaseReferenceRow> = referenceRow.caseRows.map { it.toCase() }
internal val generatedPlRuReferenceChainRows: List<ReferenceChainRow> = referenceRow.chainRows.map { ReferenceChainRow(it.label, it.from, it.to, it.change) }
internal val generatedPlRuReferenceTenseRows: List<ReferenceTenseRow> = referenceRow.tenseRows.map { ReferenceTenseRow(it.label, it.from, it.to) }
internal val generatedPlRuReferenceAspectRows: List<ReferenceAspectRow> = referenceRow.aspectRows.map { it.toAspect() }
internal val generatedPlRuMaleAccRows: List<MaleAccRow> = referenceRow.maleAccRows.map { it.toMaleAcc() }
internal val generatedPlRuReferenceSystemCards: List<ReferenceSystemCard> =
    referenceRow.systemCards.map { ReferenceSystemCard(it.id, it.title, it.explanation, it.example, it.steps) }
internal val generatedPlRuReferencePipeline: ReferencePipeline = referenceRow.pipeline.let { pipeline ->
    ReferencePipeline(pipeline.title, pipeline.steps.map { ReferencePipelineStep(it.id, it.label, it.question, it.example) }, pipeline.compactExample)
}

internal fun GenderNamesRow.toNames() = mapOf(
    Gender.M_PERSONAL.id to m_personal, Gender.M_ANIMATE.id to m_animate, Gender.M_INANIMATE.id to m_inanimate, Gender.F.id to f, Gender.N.id to n,
)

internal fun RefCaseRow.toCase() = CaseReferenceRow(GramCase.fromId(id), pl, ru, question, trigger, skill)

internal fun RefMaleAccRow.toMaleAcc() = MaleAccRow(id, label, title, examples.map { MaleAccExample(it.from, it.to, it.sentence) }, rule)

internal fun RefAspectRow.toAspect() = ReferenceAspectRow(label, from, present, past, future)
