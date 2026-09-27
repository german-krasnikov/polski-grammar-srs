package polski.ui

import polski.data.styleContentBySkillId
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.StyleRegistry

/**
 * UC-10 §6: web-only Russian copy for the 4 [StyleId] recipes. A recipe's own label/description
 * are content data (UC-12 moves them to per-style JSON files under `courses` `styles`); while that
 * content isn't merged yet they default to empty, so this file supplies a Russian fallback — never
 * hardcoded ahead of recipe data, only behind it. This file is UI copy, not the `Block`/`StyleRecipe`/
 * `StyleComposer` types themselves, so it is outside ST-06's no-`ru`-strings grep.
 */
private val fallbackLabel: Map<StyleId, String> = mapOf(
    StyleId.RuleFirst to "Схемы и логика",
    StyleId.SituationFirst to "Живые ситуации",
    StyleId.NativeContrast to "Через сравнение с родным",
    StyleId.MinimalTheory to "Минимум теории",
)

private val fallbackDescription: Map<StyleId, String> = mapOf(
    StyleId.RuleFirst to "Формула и таблица окончаний, затем разбор изменений и правило.",
    StyleId.SituationFirst to "Короткая сцена и намерение, затем разбор изменений.",
    StyleId.NativeContrast to "По-родному так → по-изучаемому так, где сходится и где отличается.",
    StyleId.MinimalTheory to "Только примеры и разбор; правило — по запросу «Почему так?».",
)

internal fun styleLabel(id: StyleId): String =
    StyleRegistry.recipes[id]?.label?.get("ru")?.takeIf { it.isNotBlank() } ?: fallbackLabel.getValue(id)

internal fun styleDescription(id: StyleId): String =
    StyleRegistry.recipes[id]?.description?.get("ru")?.takeIf { it.isNotBlank() } ?: fallbackDescription.getValue(id)

/**
 * Null once the current skill has real `nativeParallel` content (or there is no current skill to
 * check); otherwise names the style [StyleComposer.resolveEffectiveStyle] falls back to today —
 * true for every skill until CONTENT's `styleContent` authoring merges (UC-10 §3).
 */
internal fun nativeContrastFallbackHint(currentSkillId: String?): String? {
    val skillId = currentSkillId ?: return null
    val recipe = StyleRegistry.recipes.getValue(StyleId.NativeContrast)
    val resolved = StyleComposer.resolveEffectiveStyle(recipe, styleContentBySkillId(skillId), StyleRegistry.recipes)
    if (resolved == StyleId.NativeContrast) return null
    return "«${styleLabel(StyleId.NativeContrast)}» пока недоступен для текущего навыка — показывается «${styleLabel(resolved)}»."
}
