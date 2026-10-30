// Generated from kotlin/shared/src/commonMain/kotlin/polski/data/PlRuFormTables.kt by dsl/derive-enru-mappers.mjs: the en-ru copy of that file. Edit the pl-ru file, then run biome generate.
package polski.data

import polski.core.model.FeatureBundle
import polski.core.model.FeatureKey
import polski.core.model.FeatureValue
import polski.data.rows.enru.forms.FORM_LEXEME_ROW_ROWS
import polski.data.rows.enru.forms.FormLexemeRow

/**
 * R1.12: the en-ru form table, as `TableMorphology` takes it: for each lexeme (`noun:wife`) the form under each bundle of features. The rows are derived from the
 * lexicon at build time (dsl/derive-forms.mjs), so this file only lays them out as maps; a bundle or a lexeme written twice is refused, not overwritten.
 */
internal val generatedEnRuFormTable: Map<String, Map<FeatureBundle, String>> = FORM_LEXEME_ROW_ROWS.toFormTable()

internal fun List<FormLexemeRow>.toFormTable(): Map<String, Map<FeatureBundle, String>> = map { lexeme ->
    lexeme.lexeme to lexeme.forms.map { entry ->
        entry.bundle.map { FeatureKey(it.feature) to FeatureValue(it.value) }.toDistinctMap("bundle of ${lexeme.lexeme}") to entry.form
    }.toDistinctMap("forms of ${lexeme.lexeme}")
}.toDistinctMap("lexemes")
