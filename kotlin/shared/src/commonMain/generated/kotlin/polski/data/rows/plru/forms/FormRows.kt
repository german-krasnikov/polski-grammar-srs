// Generated from dsl/forms.data-contract, derived by dsl/derive-forms.mjs from courses-biome/pl-ru/nouns.core-data, courses-biome/pl-ru/adjectives.core-data, courses-biome/pl-ru/verbs.core-data, courses-biome/pl-ru/possessives.core-data, courses-biome/pl-ru/morphology.core-data
// source-sha256=168626c2637cbff3b3138759d1f218e504280e49045dae45e53b7afbd8bd1257
// Edit the source, then run biome generate.

package polski.data.rows.plru.forms

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
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "żona",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "żony",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "żonie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "żonę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "żoną",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "żonie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "żono",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "żony",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "żon",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "żonom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "żony",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "żonami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "żonach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "żony",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:woman",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "kobieta",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "kobiety",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "kobiecie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "kobietę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "kobietą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "kobiecie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "kobieto",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "kobiety",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "kobiet",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "kobietom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "kobiety",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "kobietami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "kobietach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "kobiety",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:book",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "książka",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "książki",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "książce",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "książkę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "książką",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "książce",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "książko",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "książki",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "książek",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "książkom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "książki",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "książkami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "książkach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "książki",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:work",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "praca",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pracy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pracy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "pracę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pracą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pracy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "praco",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "prace",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "prac",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pracom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "prace",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pracami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pracach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "prace",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:husband",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mąż",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "męża",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "mężowi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "męża",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "mężem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "mężu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mężu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mężowie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "mężów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "mężom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "mężów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "mężami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "mężach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mężowie",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:friendM",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "kolega",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "kolegi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "koledze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "kolegę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "kolegą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "koledze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "kolego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "koledzy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "kolegów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "kolegom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "kolegów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "kolegami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "kolegach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "koledzy",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:son",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "syn",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "syna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "synowi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "syna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "synem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "synu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "synu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "synowie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "synów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "synom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "synów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "synami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "synach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "synowie",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:dog",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "pies",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "psa",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "psu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "psa",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "psem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "psie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "psie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "psy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "psów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "psom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "psy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "psami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "psach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "psy",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:cat",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "kot",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "kota",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "kotu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "kota",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "kotem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "kocie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "kocie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "koty",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "kotów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "kotom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "koty",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "kotami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "kotach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "koty",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:car",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "samochód",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "samochodu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "samochodowi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "samochód",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "samochodem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "samochodzie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "samochodzie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "samochody",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "samochodów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "samochodom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "samochody",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "samochodami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "samochodach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "samochody",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:house",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "domu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "domowi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "domem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "domu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "domu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "domy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "domów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "domom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "domy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "domami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "domach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "domy",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart1(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "noun:phone",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "telefon",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "telefonu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "telefonowi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "telefon",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "telefonem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "telefonie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "telefonie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "telefony",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "telefonów",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "telefonom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "telefony",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "telefonami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "telefonach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "telefony",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:child",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dziecko",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dziecka",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dziecku",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dziecko",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dzieckiem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dziecku",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dziecko",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dzieci",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dzieci",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dzieciom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dzieci",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dziećmi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dzieciach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dzieci",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "noun:window",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "okno",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "okna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "oknu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "okno",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "oknem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "oknie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "okno",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "okna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "okien",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "oknom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "okna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "oknami",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "oknach",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "okna",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "adjective:beautiful",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "pięknego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "pięknego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "piękny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "piękną",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "piękną",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękni",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękni",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "pięknym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "piękne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "pięknymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "pięknych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "piękne",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart2(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "adjective:new",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowa",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nową",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nową",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowa",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "nowym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nowe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "nowymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "nowych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nowe",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart3(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "adjective:small",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "mały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mała",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mała",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mali",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mali",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "małym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "małe",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "małymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "małych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "małe",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart4(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "adjective:nice",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładnego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładnego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładny",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładną",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładną",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładna",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładni",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładni",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ładnym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ładne",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ładnymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ładnych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ładne",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart5(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "adjective:good",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobry",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobrego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobry",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobry",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobrego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobry",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobry",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobry",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobry",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobra",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobrej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobrą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobra",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobrzy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobrzy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "dobrym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "dobre",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "dobrymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "dobrych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "dobre",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart6(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "adjective:old",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stary",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "staremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "starego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stary",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stary",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "staremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "starego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stary",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stary",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "staremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "stary",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stary",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stara",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "starej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "starą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stara",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "staremu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "starzy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "starzy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "starym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "stare",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "starymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "starych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "stare",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart7(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "adjective:expensive",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogiego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogiemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogiego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogiego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogiemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogiego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogiego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogiemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "droga",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogiej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogiej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogiej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "droga",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogiego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogiemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drodzy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drodzy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "drogim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "drogie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "drogimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "drogich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "drogie",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart8(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "possessive:my",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "mojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "mojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "mojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "mojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "mojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "mojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "mój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "mojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "mojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "mój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "mój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "moja",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "mojej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "mojej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "moją",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moją",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "mojej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "moja",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "mojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "mojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "moi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "moi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "moim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "moje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "moimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "moich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "moje",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart9(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "possessive:your",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twój",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twoja",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twojej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twojej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twoją",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoją",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twojej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twoja",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twojego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twojemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twoi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twoi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "twoim",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "twoje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "twoimi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "twoich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "twoje",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart10(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "possessive:his",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jego",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart11(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "possessive:her",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "jej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "jej",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart12(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "possessive:our",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "naszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "naszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasza",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "naszą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasza",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "naszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "nasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "naszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "naszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "nasze",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart13(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "possessive:yourPlural",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "waszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "waszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "wasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasza",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "waszą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszej",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasza",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszego",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszemu",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "waszym",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "wasze",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "waszymi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "waszych",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "wasze",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart14(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "possessive:their",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-inanimate"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "nom"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "gen"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "dat"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "acc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "inst"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "loc"),
                ),
                form = "ich",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                    BundleFeatureRow(feature = "Case", value = "voc"),
                ),
                form = "ich",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:have",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "mam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "masz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "ma",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "mamy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "macie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "mają",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "miałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "miałam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "miałom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "miałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "miałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "miałaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "miałoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "miałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "miał",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "miała",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "miało",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "miał",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mieliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "miałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "miałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "miałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mieliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "miałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "miałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "miałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mieli",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "miały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "miały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "miały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę mieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz mieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie mieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy mieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie mieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą mieć",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart15(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "verb:see",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "widzę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "widzisz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "widzi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "widzimy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "widzicie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "widzą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "widziałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "widziałam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "widziałom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "widziałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "widziałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "widziałaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "widziałoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "widziałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "widział",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "widziała",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "widziało",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "widział",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "widzieliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "widziałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "widziałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "widziałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "widzieliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "widziałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "widziałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "widziałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "widzieli",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "widziały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "widziały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "widziały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę widzieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz widzieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie widzieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy widzieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie widzieć",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą widzieć",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:like",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "lubię",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "lubisz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "lubi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "lubimy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "lubicie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "lubią",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "lubiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "lubiłam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "lubiłom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "lubiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "lubiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "lubiłaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "lubiłoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "lubiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "lubił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "lubiła",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "lubiło",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "lubił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "lubiliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "lubiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "lubiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "lubiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "lubiliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "lubiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "lubiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "lubiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "lubili",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "lubiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "lubiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "lubiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę lubić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz lubić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie lubić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy lubić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie lubić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą lubić",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart16(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "verb:buy",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "kupuję",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "kupujesz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "kupuje",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "kupujemy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "kupujecie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "kupują",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupowałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupowałam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupowałom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupowałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupowałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupowałaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupowałoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupowałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupował",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupowała",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupowało",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupował",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupowaliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupowałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupowałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupowałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupowaliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupowałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupowałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupowałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupowali",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupowały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupowały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupowały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę kupować",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz kupować",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie kupować",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy kupować",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie kupować",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą kupować",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:read",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "czytam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "czytasz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "czyta",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "czytamy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "czytacie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "czytają",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "czytałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "czytałam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "czytałom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "czytałem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "czytałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "czytałaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "czytałoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "czytałeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "czytał",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "czytała",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "czytało",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "czytał",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "czytaliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "czytałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "czytałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "czytałyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "czytaliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "czytałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "czytałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "czytałyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "czytali",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "czytały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "czytały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "czytały",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę czytać",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz czytać",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie czytać",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy czytać",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie czytać",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą czytać",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart17(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "verb:talk",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "mówię",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "mówisz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "mówi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "mówimy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "mówicie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "mówią",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mówiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "mówiłam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "mówiłom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "mówiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mówiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "mówiłaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "mówiłoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "mówiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mówił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "mówiła",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "mówiło",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "mówił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mówiliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "mówiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "mówiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "mówiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mówiliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "mówiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "mówiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "mówiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "mówili",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "mówiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "mówiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "mówiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę mówić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz mówić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie mówić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy mówić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie mówić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą mówić",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:go",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "idę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "idziesz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "idzie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "idziemy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "idziecie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "idą",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "szedłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "szłam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "szłom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "szedłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "szedłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "szłaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "szłoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "szedłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "szedł",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "szła",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "szło",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "szedł",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "szliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "szłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "szłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "szłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "szliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "szłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "szłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "szłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "szli",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "szły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "szły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "szły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę iść",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz iść",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie iść",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy iść",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie iść",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą iść",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart18(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "verb:be",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "jestem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "jesteś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "jest",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "jesteśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "jesteście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "są",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "byłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "byłam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "byłom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "byłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "byłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "byłaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "byłoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "byłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "był",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "była",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "było",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "był",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "byliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "byłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "byłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "byłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "byliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "byłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "byłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "byłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "byli",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "były",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "były",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "były",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:buyDone",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupiłam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupiłom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupiłaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupiłoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupiła",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupiło",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupiliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupiliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "kupili",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "kupiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "kupiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "kupiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "kupię",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "kupisz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "kupi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "kupimy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "kupicie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "kupią",
            ),
        ),
    ),
    FormLexemeRow(
        lexeme = "verb:do",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "robię",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "robisz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "robi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "robimy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "robicie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "present"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "robią",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "robiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "robiłam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "robiłom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "robiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "robiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "robiłaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "robiłoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "robiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "robił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "robiła",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "robiło",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "robił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "robiliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "robiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "robiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "robiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "robiliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "robiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "robiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "robiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "robili",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "robiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "robiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "robiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będę robić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będziesz robić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "będzie robić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziemy robić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będziecie robić",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "będą robić",
            ),
        ),
    ),
)

private fun formLexemeRowRowsPart19(): List<FormLexemeRow> = listOf(
    FormLexemeRow(
        lexeme = "verb:doDone",
        forms = listOf(
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "zrobiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "zrobiłam",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "zrobiłom",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "zrobiłem",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "zrobiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "zrobiłaś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "zrobiłoś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "zrobiłeś",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "zrobił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "zrobiła",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "zrobiło",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "zrobił",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "zrobiliśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "zrobiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "zrobiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "zrobiłyśmy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "zrobiliście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "zrobiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "zrobiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "zrobiłyście",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-personal"),
                ),
                form = "zrobili",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "f"),
                ),
                form = "zrobiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "n"),
                ),
                form = "zrobiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "past"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                    BundleFeatureRow(feature = "Gender", value = "m-animate"),
                ),
                form = "zrobiły",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "zrobię",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "zrobisz",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "sg"),
                ),
                form = "zrobi",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "1"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "zrobimy",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "2"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "zrobicie",
            ),
            FormRow(
                bundle = listOf(
                    BundleFeatureRow(feature = "Tense", value = "future"),
                    BundleFeatureRow(feature = "Person", value = "3"),
                    BundleFeatureRow(feature = "Number", value = "pl"),
                ),
                form = "zrobią",
            ),
        ),
    ),
)

val FORM_LEXEME_ROW_ROWS: List<FormLexemeRow> = formLexemeRowRowsPart0() + formLexemeRowRowsPart1() + formLexemeRowRowsPart2() + formLexemeRowRowsPart3() + formLexemeRowRowsPart4() + formLexemeRowRowsPart5() + formLexemeRowRowsPart6() + formLexemeRowRowsPart7() + formLexemeRowRowsPart8() + formLexemeRowRowsPart9() + formLexemeRowRowsPart10() + formLexemeRowRowsPart11() + formLexemeRowRowsPart12() + formLexemeRowRowsPart13() + formLexemeRowRowsPart14() + formLexemeRowRowsPart15() + formLexemeRowRowsPart16() + formLexemeRowRowsPart17() + formLexemeRowRowsPart18() + formLexemeRowRowsPart19()
