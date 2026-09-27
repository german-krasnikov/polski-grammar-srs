package polski.ui.screens

import polski.presentation.StyleId
import polski.presentation.StyleRecipe

/**
 * UC-10/S1: `courses/styles` JSON content (`recipe.label`/`description`) is authored in a
 * parallel worktree and not merged yet, so every [StyleRecipe] in [polski.presentation.StyleRegistry]
 * still carries empty `label`/`description` maps. These are the Android-owned fallback strings
 * shown until that content lands — [styleLabel]/[styleDescription] prefer the recipe's own "ru"
 * entry the moment one is present, so this map stops being read for that style without any UI change here.
 */
private val fallbackCopy: Map<StyleId, Pair<String, String>> = mapOf(
    StyleId.RuleFirst to ("Схемы и логика" to "Формула, таблица окончаний и правило."),
    StyleId.SituationFirst to ("Живые ситуации" to "Сцена и разбор без сухой теории."),
    StyleId.NativeContrast to ("Через сравнение с родным" to "Русская фраза рядом с польской — где сходится, где расходится."),
    StyleId.MinimalTheory to ("Минимум теории" to "Примеры сразу, объяснение — по запросу."),
)

internal fun styleLabel(recipe: StyleRecipe): String = recipe.label["ru"] ?: fallbackCopy.getValue(recipe.id).first
internal fun styleDescription(recipe: StyleRecipe): String = recipe.description["ru"] ?: fallbackCopy.getValue(recipe.id).second
