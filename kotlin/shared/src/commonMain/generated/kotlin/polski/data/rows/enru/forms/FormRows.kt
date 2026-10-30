// Generated from dsl/forms.data-contract, derived by dsl/derive-forms.mjs from courses-biome/en-ru/nouns.core-data, courses-biome/en-ru/adjectives.core-data, courses-biome/en-ru/verbs.core-data, courses-biome/en-ru/possessives.core-data, courses-biome/en-ru/personalPronouns.core-data, courses-biome/en-ru/prepositions.core-data
// source-sha256=168626c2637cbff3b3138759d1f218e504280e49045dae45e53b7afbd8bd1257
// Edit the source, then run biome generate.

package polski.data.rows.enru.forms

data class BundleFeatureRow(
    val feature: String,
    val value: String,
)

data class FormRow(
    val bundle: List<BundleFeatureRow>,
    val form: String,
)

data class FormLexemeRow(
    val lexeme: String,
    val forms: List<FormRow>,
)

private fun formLexemeRowRowsPart0(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "noun:wife",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "wife"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "wives"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:woman",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "woman"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "women"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:book",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "book"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "books"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:work",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "job"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "jobs"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:husband",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "husband"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "husbands"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:friend",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "friend"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "friends"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:son",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "son"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "sons"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:dog",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "dog"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "dogs"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:cat",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "cat"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "cats"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:car",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "car"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "cars"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:house",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "house"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "houses"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:phone",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "phone"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "phones"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:child",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "child"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "children"),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:window",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "sg")), form = "window"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Number", value = "pl")), form = "windows"),
        ),
    ),
    FormLexemeRow(lexeme = "adjective:beautiful", forms = listOf(FormRow(bundle = emptyList(), form = "beautiful"))),
    FormLexemeRow(lexeme = "adjective:new", forms = listOf(FormRow(bundle = emptyList(), form = "new"))),
    FormLexemeRow(lexeme = "adjective:small", forms = listOf(FormRow(bundle = emptyList(), form = "small"))),
    FormLexemeRow(lexeme = "adjective:nice", forms = listOf(FormRow(bundle = emptyList(), form = "nice"))),
    FormLexemeRow(lexeme = "adjective:good", forms = listOf(FormRow(bundle = emptyList(), form = "good"))),
    FormLexemeRow(lexeme = "adjective:old", forms = listOf(FormRow(bundle = emptyList(), form = "old"))),
    FormLexemeRow(lexeme = "adjective:expensive", forms = listOf(FormRow(bundle = emptyList(), form = "expensive"))),
    FormLexemeRow(lexeme = "possessive:my", forms = listOf(FormRow(bundle = emptyList(), form = "my"))),
    FormLexemeRow(lexeme = "possessive:your", forms = listOf(FormRow(bundle = emptyList(), form = "your"))),
    FormLexemeRow(lexeme = "possessive:his", forms = listOf(FormRow(bundle = emptyList(), form = "his"))),
    FormLexemeRow(lexeme = "possessive:her", forms = listOf(FormRow(bundle = emptyList(), form = "her"))),
    FormLexemeRow(lexeme = "possessive:its", forms = listOf(FormRow(bundle = emptyList(), form = "its"))),
    FormLexemeRow(lexeme = "possessive:our", forms = listOf(FormRow(bundle = emptyList(), form = "our"))),
    FormLexemeRow(lexeme = "possessive:their", forms = listOf(FormRow(bundle = emptyList(), form = "their"))),
    FormLexemeRow(lexeme = "pronoun:I", forms = listOf(FormRow(bundle = emptyList(), form = "I"))),
    FormLexemeRow(lexeme = "pronoun:you", forms = listOf(FormRow(bundle = emptyList(), form = "you"))),
    FormLexemeRow(lexeme = "pronoun:he", forms = listOf(FormRow(bundle = emptyList(), form = "he"))),
    FormLexemeRow(lexeme = "pronoun:she", forms = listOf(FormRow(bundle = emptyList(), form = "she"))),
    FormLexemeRow(lexeme = "pronoun:it", forms = listOf(FormRow(bundle = emptyList(), form = "it"))),
    FormLexemeRow(lexeme = "pronoun:we", forms = listOf(FormRow(bundle = emptyList(), form = "we"))),
    FormLexemeRow(lexeme = "pronoun:they", forms = listOf(FormRow(bundle = emptyList(), form = "they"))),
    FormLexemeRow(lexeme = "neg:not", forms = listOf(FormRow(bundle = emptyList(), form = "not"))),
    FormLexemeRow(
        lexeme = "prep:role",
        forms = listOf(
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Case", value = "Subj")), form = ""),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Case", value = "Obj")), form = ""),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Case", value = "In")), form = "in"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Case", value = "With")), form = "with"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Case", value = "To")), form = "to"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Case", value = "About")), form = "about"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Case", value = "Of")), form = "of"),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:walk",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walks",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walk",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "walked"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "walk"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "walking"),
            FormRow(bundle = emptyList(), form = "walk"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walks",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "walked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "walked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will walk",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:see",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "sees",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "see",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "saw"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "see"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "seeing"),
            FormRow(bundle = emptyList(), form = "see"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "saw",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "saw",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "sees",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "saw",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "saw",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "saw",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "see",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "saw",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will see",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:talk",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talks",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talk",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "talked"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "talk"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "talking"),
            FormRow(bundle = emptyList(), form = "talk"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talks",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "talked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talk",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "talked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will talk",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart1(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "verb:go",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "goes",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "go",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "went"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "go"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "going"),
            FormRow(bundle = emptyList(), form = "go"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "went",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "went",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "goes",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "went",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "went",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "went",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "go",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "went",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will go",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:like",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "likes",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "like",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "liked"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "like"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "liking"),
            FormRow(bundle = emptyList(), form = "like"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "liked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "liked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "likes",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "liked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "liked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "liked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "like",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "liked",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will like",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:read",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "reads",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "read"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "read"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "reading"),
            FormRow(bundle = emptyList(), form = "read"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "reads",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "read",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will read",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:have",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "has",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "have",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "had"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "have"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "having"),
            FormRow(bundle = emptyList(), form = "have"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "had",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "had",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "has",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "had",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "had",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "had",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "have",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "had",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will have",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart2(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "verb:do",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "does",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "did"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "do"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "doing"),
            FormRow(bundle = emptyList(), form = "do"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "did",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "did",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "does",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "did",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "did",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "did",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "did",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will do",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "aux:do",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "does",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "do",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "did"),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:be",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "am",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "is",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "was"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Fut")), form = "be"),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Aspect", value = "Continuous")), form = "being"),
            FormRow(bundle = emptyList(), form = "be"),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "am",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "was",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will be",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "were",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will be",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "is",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "was",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will be",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "were",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will be",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "were",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will be",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "were",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will be",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "aux:be",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "am",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "is",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Pres"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "are",
            ),
            FormRow(bundle = listOf(BundleFeatureRow(feature = "Tense", value = "Past")), form = "was"),
        ),
    ),
    FormLexemeRow(
        lexeme = "aux:will",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Fut"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Fut"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Fut"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "will",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Fut"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Fut"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "Fut"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "will",
            ),
        ),
    ),
)

val FORM_LEXEME_ROW_ROWS: List<FormLexemeRow> = formLexemeRowRowsPart0() + formLexemeRowRowsPart1() + formLexemeRowRowsPart2()
