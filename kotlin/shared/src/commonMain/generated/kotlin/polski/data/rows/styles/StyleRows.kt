// Generated from dsl/style.data-contract, courses-biome/styles/styles.core-data
// source-sha256=505b30624de46bde4a34563fc324335777481348c16523cfc2ae9a9e838a13b6
// Edit the source, then run biome generate.

package polski.data.rows.styles

enum class BlockKindRow(val tag: String) {
    FORMULA("formula"),
    TABLE("table"),
    RULE("rule"),
    SCENE("scene"),
    NATIVEPARALLEL("nativeParallel"),
    EXAMPLES("examples"),
    WHYONDEMAND("whyOnDemand"),
    CHANGES("changes"),
    CONTRAST("contrast"),
}

data class LocalizedTextRow(
    val ru: String,
    val en: String,
)

data class StyleRecipeRow(
    val id: String,
    val label: LocalizedTextRow,
    val description: LocalizedTextRow,
    val front: List<BlockKindRow>,
    val back: List<BlockKindRow>,
    val requires: List<BlockKindRow>? = null,
    val fallback: String? = null,
)

val STYLE_RECIPE_ROW_ROWS: List<StyleRecipeRow> = listOf(
    StyleRecipeRow(
        id = "minimal-theory",
        label = LocalizedTextRow(ru = "Минимум теории", en = "Minimal theory"),
        description = LocalizedTextRow(
            ru = "Только примеры; «Почему так?» — по запросу.",
            en = "Examples only; \"Why?\" is available on demand.",
        ),
        front = listOf(BlockKindRow.EXAMPLES),
        back = listOf(BlockKindRow.WHYONDEMAND, BlockKindRow.CHANGES),
    ),
    StyleRecipeRow(
        id = "native-contrast",
        label = LocalizedTextRow(ru = "Через сравнение с родным", en = "Native contrast"),
        description = LocalizedTextRow(
            ru = "По-родному так → по-изучаемому так: где совпадает, где отличается.",
            en = "L1 parallel: where it matches the target language, where it differs.",
        ),
        front = listOf(BlockKindRow.NATIVEPARALLEL),
        back = listOf(BlockKindRow.CHANGES, BlockKindRow.CONTRAST),
        requires = listOf(BlockKindRow.NATIVEPARALLEL),
        fallback = "rule-first",
    ),
    StyleRecipeRow(
        id = "rule-first",
        label = LocalizedTextRow(ru = "Через правило", en = "Rule first"),
        description = LocalizedTextRow(
            ru = "Сначала схема и таблица окончаний, затем пример.",
            en = "The scheme and the ending table come first, the example follows.",
        ),
        front = listOf(BlockKindRow.TABLE, BlockKindRow.FORMULA),
        back = listOf(BlockKindRow.RULE, BlockKindRow.CHANGES, BlockKindRow.CONTRAST),
    ),
    StyleRecipeRow(
        id = "situation-first",
        label = LocalizedTextRow(ru = "Через ситуацию", en = "Situation first"),
        description = LocalizedTextRow(
            ru = "Короткая сцена сразу, правило — после ответа.",
            en = "A short scene up front, the rule follows the answer.",
        ),
        front = listOf(BlockKindRow.SCENE),
        back = listOf(BlockKindRow.CHANGES, BlockKindRow.RULE),
    ),
)
