package polski.data

import polski.data.rows.enru.*
import polski.model.PossessiveId

/**
 * R1.14a: the en-ru lexicon from the generated rows. English has no case or gender, so the pl-shaped nouns, adjectives and verbs stay empty in
 * EnRuTypedPack; what is read here is the noun labels, the verbs and pronouns of the matrix, the raw pronoun forms (the case ids of English are
 * Subj and Obj, not GramCase) and the possessives. A row has its id once (KEY), so nothing is overwritten.
 */
internal val generatedEnRuNounLabels: Map<String, Pair<String, String>> = EN_NOUN_ROW_ROWS.toNounLabels()
internal val generatedEnVerbs: List<EnVerb> = EN_VERB_ROW_ROWS.toEnVerbs()
internal val generatedEnPronouns: List<EnPronoun> = EN_PRONOUN_ROW_ROWS.toEnPronouns()
internal val generatedEnRuPersonalPronounRawForms: Map<String, Map<String, String>> = EN_PRONOUN_ROW_ROWS.toRawForms()
internal val generatedEnRuPossessives: List<Possessive> = POSSESSIVE_ROW_ROWS.toPossessives()

internal fun List<EnNounRow>.toNounLabels() = associate { it.id to (it.lemma to it.meaning) }
internal fun List<EnVerbRow>.toEnVerbs() = map { EnVerb(it.id, it.lemma, it.meaning) }
internal fun List<EnPronounRow>.toEnPronouns() = map { EnPronoun(it.id, it.forms.Subj) }
internal fun List<EnPronounRow>.toRawForms() = associate { it.id to mapOf("Subj" to it.forms.Subj, "Obj" to it.forms.Obj) }
internal fun List<PossessiveRow>.toPossessives() = map { Possessive(PossessiveId.fromId(it.id), it.label) }
