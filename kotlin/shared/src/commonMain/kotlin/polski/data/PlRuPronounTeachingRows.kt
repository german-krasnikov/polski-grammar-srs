package polski.data

import polski.data.rows.plru.*
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.presentation.ContrastPair
import polski.presentation.EndingPart

/**
 * R1.10b: the pl-ru pronoun and possessive teaching and the Russian support table, from the generated rows.
 * A context is read by its case id (its own `id` is the same text and is not used); an absent prefix is "" and absent special values are none.
 */
private val teachingRow = REFERENCE_ROW_ROWS.single()

internal val generatedPlRuPronounTeaching: PronounTeaching = teachingRow.pronounTeaching.toTeaching()
internal val generatedPlRuRussianSupport: RussianSupport = teachingRow.russianSupport.toSupport()

internal fun PronounTeachingRow.toTeaching() = PronounTeaching(
    personal.title, personal.intro.react, personal.intro.compact, personal.footer.react, personal.footer.web, personal.footer.native,
    personal.pronounIds, contexts.map { it.toContext() }, possessive.title, possessive.demo.toDemo(),
)

internal fun PronounContextRow.toContext() = PronounContext(
    GramCase.fromId(caseId), PronounCue(cue.full, cue.compact, cue.ios), caseName, valuePrefix.orEmpty(), specialValues.orEmpty().associate { it.pronounId to it.value },
)

internal fun PossessiveDemoRow.toDemo() = PossessiveDemo(
    nounId, adjectiveId, NumberGram.fromId(number),
    cases.map { PossessiveDemoCase(GramCase.fromId(it.id), it.caption.web, it.caption.desktop, it.caption.ios) },
    invariableOwnerIds.map(PossessiveId::fromId).toSet(), rule.invariable, rule.variable,
)

internal fun RussianSupportSectionRow.toSupport() = RussianSupport(title.full, title.compact, columns, rows.map { it.toRow() })

internal fun SupportRow.toRow() = RussianSupportRow(
    id, cue, react.toTable(), web.toTable(), desktop.toTable(), mobileLine,
    comparisons.map { ContrastPair(it.from, it.to, it.beforeParts.map(EndingPartRow::toPart), it.afterParts.map(EndingPartRow::toPart)) },
)

internal fun ConstructionCheckRow.toTable() = RussianSupportTable(construction, check)

internal fun EndingPartRow.toPart() = EndingPart(text, isEnding, isChanged)
