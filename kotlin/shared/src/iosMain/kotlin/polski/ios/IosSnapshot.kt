package polski.ios

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.presentationBySkillId
import polski.data.skillById
import polski.data.styleContentBySkillId
import polski.grammar.caseRows
import polski.grammar.nounPhrase
import polski.model.GramCase
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.ContrastPair
import polski.presentation.EndingPart
import polski.presentation.Lifehack
import polski.presentation.StaticPackLifehackProvider
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.StylePhase
import polski.presentation.StyleRegistry
import polski.presentation.blocksToJson
import polski.presentation.contrastPairJson
import polski.presentation.exerciseJson
import polski.presentation.lifehackObjectJson
import polski.presentation.putEffects
import polski.presentation.putProgressFields
import polski.presentation.putSharedSnapshotHeader

internal fun snapshot(state: AppUiState): String = buildJsonObject {
    putSharedSnapshotHeader(state, styleBlocksSnapshot(state), ::lifehackJson)
    put("exercise", exerciseJson(state))
    // Emphasis contract §5 / web fix 804f6c6: before reveal, the row matching this exercise's own
    // target case is the answer, so its form stays masked; every other row is unrelated reference
    // material and shows normally.
    val revealed = state.phase == CardPhase.Revealed
    put("referenceRows", JsonArray(if (state.showReference && !state.introPending) state.exercise?.let { exercise -> caseRows.map { row -> buildJsonObject {
        put("title", "${row.pl} · ${row.ru}")
        val from = nounPhrase(exercise.nounId, GramCase.NOM, exercise.number, exercise.adjectiveId, exercise.possessive)
        val isTarget = row.id.id in exercise.tags
        if (isTarget && !revealed) {
            put("text", referenceMaskPlaceholder)
            put("pair", pairSnapshot(ContrastPair(from, referenceMaskPlaceholder,
                listOf(EndingPart(from, isEnding = false)), listOf(EndingPart(referenceMaskPlaceholder, isEnding = false)))))
        } else {
            val form = nounPhrase(exercise.nounId, row.id, exercise.number, exercise.adjectiveId, exercise.possessive)
            put("text", form)
            put("pair", pairSnapshot(ContrastPair.generated(from, form)))
        }
    } } } ?: emptyList() else emptyList()))
    put("matrix", matrixSnapshot(state))
    putProgressFields(state)
    putEffects(state)
}.toString()

/** UC-10/S2: the resolved (post-fallback) style's blocks, by the same [blocksToJson] shape other
 *  snapshot sections already use. Both phases are always composed here — not just the current
 *  [AppUiState.phase] — because [FlashCardView] keeps the question visible after reveal (D1) and
 *  needs `front` blocks throughout, while `back` blocks only render once revealed; composing both
 *  is cheap (pure, no FSRS/progress read) and avoids a phase-shaped hole in the wire contract.
 *  [nativeContrastFallback] (S1) reads regardless of [AppUiState.styleId] — the settings hint needs
 *  to know whether the *current skill* lacks native-contrast content even while another style is selected. */
private fun styleBlocksSnapshot(state: AppUiState): JsonElement {
    val exercise = state.exercise ?: return JsonNull
    val registry = StyleRegistry.recipes
    val recipe = registry[state.styleId] ?: return JsonNull
    val content = styleContentBySkillId(exercise.primarySkill)
    val effective = registry[StyleComposer.resolveEffectiveStyle(recipe, content, registry)] ?: recipe
    val skill = skillById(exercise.primarySkill)
    val focus = presentationBySkillId(exercise.primarySkill)
    val nativeContrastRecipe = registry[StyleId.NativeContrast]
    val nativeContrastFallback = nativeContrastRecipe != null &&
        StyleComposer.resolveEffectiveStyle(nativeContrastRecipe, content, registry) != StyleId.NativeContrast
    return buildJsonObject {
        put("effectiveStyleId", effective.id.value)
        put("nativeContrastFallback", nativeContrastFallback)
        put("front", blocksToJson(StyleComposer.compose(effective, StylePhase.Front, exercise, skill, focus, content)))
        put("back", blocksToJson(StyleComposer.compose(effective, StylePhase.Back, exercise, skill, focus, content)))
        put("lifehacks", lifehacksSnapshot(exercise.primarySkill))
    }
}

/** EN-21 (`EnRuPackPlan.md` §4.3): not a [StyleComposer] block — §4.3's whole point is that this
 *  list is the same regardless of [effective] above, so it sits beside `front`/`back` rather than
 *  inside either. Empty for a skill with no authored tip (`StaticPackLifehackProvider.forSkill`),
 *  and [FlashCardView]'s renderer must skip the section entirely on empty, never draw an empty
 *  frame — the same rule `LifehackWeb.kt`'s `renderLifehackBlock` already follows. */
private fun lifehacksSnapshot(skillId: String): JsonElement = lifehacksJsonFor(StaticPackLifehackProvider.forSkill(skillId))

/** Shared by the per-skill back-face block above and [putSharedSnapshotHeader]'s pack-wide
 *  `lifehackGroups` listing — one wire shape for a [Lifehack], so the two never drift apart.
 *  iOS's own key for the status field is "statusLabel" (see [lifehackObjectJson]). */
private fun lifehackJson(hack: Lifehack): JsonObject = lifehackObjectJson(hack, "statusLabel")

private fun lifehacksJsonFor(hacks: List<Lifehack>): JsonArray = JsonArray(hacks.map(::lifehackJson))

/** Emphasis contract §5: what the target row's "Стало" shows before reveal — never the answer. */
private const val referenceMaskPlaceholder = "?"

private fun pairSnapshot(pair: ContrastPair): JsonObject = contrastPairJson(pair)
