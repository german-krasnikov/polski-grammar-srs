package polski.data

import polski.data.rows.plru.*
import polski.model.Gender
import polski.model.NumberGram
import polski.model.Person
import polski.model.Tense

/**
 * R1.10a: the pl-ru case and verb teaching and the auxiliary of the future, from the generated rows. A subject whose gender the learner
 * selects has no fixed gender, and every other subject has one: the contract cannot say that, this does.
 */
private val referenceRow = REFERENCE_ROW_ROWS.single()

internal val generatedPlRuCaseTeaching: CaseTeaching = referenceRow.caseTeaching.let { CaseTeaching(it.caseNote.react, it.caseNote.compact, it.comparisonReadingHint) }
internal val generatedPlRuVerbTeaching: VerbTeaching = referenceRow.verbTeaching.toTeaching()
internal val generatedPlRuFutureAuxiliary: FutureAuxiliary = MORPHOLOGY_ROW_ROWS.single().futureAuxiliary.let { FutureAuxiliary(it.verbId, it.forms.toPersons()) }

internal fun VerbTeachingRow.toTeaching() = VerbTeaching(
    subjects.map { it.toSubject() },
    genderControlLabel.toLabel(),
    genderOptions.map { VerbGenderOption(it.id, it.label.toLabel()) },
    mapOf(Tense.PRESENT to tenseLabels.present.toLabel(), Tense.PAST to tenseLabels.past.toLabel(), Tense.FUTURE to tenseLabels.future.toLabel()),
    futureExplanation.react,
    futureExplanation.compact,
)

internal fun VerbSubjectRow.toSubject(): VerbSubject {
    val selected = genderMode == VerbSubjectRowGenderMode.SELECTED
    require(selected == (fixedGender == null)) { "$id: ${genderMode.tag}, but the fixed gender is ${if (fixedGender == null) "absent" else "written"}" }
    return VerbSubject(id, Person.fromId(person), NumberGram.fromId(number), selected, fixedGender?.let(Gender::fromId), label.toLabel())
}

internal fun FullCompactRow.toLabel() = VerbLabel(full, compact)
