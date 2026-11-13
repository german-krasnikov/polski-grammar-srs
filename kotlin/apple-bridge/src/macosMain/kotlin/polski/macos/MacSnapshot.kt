package polski.macos

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
import polski.presentation.ContrastPair
import polski.presentation.Lifehack
import polski.presentation.StaticPackLifehackProvider
import polski.presentation.StyleComposer
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
    put("schemaVersion", 1)
    putSharedSnapshotHeader(state, styleBlocksSnapshot(state), ::lifehackJson)
    put("exercise", exerciseJson(state))
    put("referenceRows", JsonArray(if (state.showReference && state.caseReferenceAvailable && !state.introPending) state.exercise?.let { exercise -> caseRows.map { row -> buildJsonObject {
        put("title", "${row.pl} · ${row.ru}")
        val form = nounPhrase(exercise.nounId, row.id, exercise.number, exercise.adjectiveId, exercise.possessive)
        put("text", form)
        put("pair", pairSnapshot(ContrastPair.generated(
            nounPhrase(exercise.nounId, GramCase.NOM, exercise.number, exercise.adjectiveId, exercise.possessive), form)))
    } } } ?: emptyList() else emptyList()))
    put("matrix", matrixSnapshot(state))
    putProgressFields(state)
    putEffects(state)
}.toString()

/** UC-10 S2: the resolved (post-fallback) style's blocks, by the same [polski.presentation.blocksToJson]
 *  shape other snapshot sections already use. Both phases are always included — not just
 *  `state.phase`'s — because the macOS card (`MacFlashCardView`'s D1 expand-reveal) keeps the front
 *  face mounted after reveal too, so it needs its own [StylePhase.Front] blocks even while
 *  `state.phase == Revealed`. */
private fun styleBlocksSnapshot(state: AppUiState): JsonElement {
    val exercise = state.exercise ?: return JsonNull
    val registry = StyleRegistry.recipes
    val recipe = registry[state.styleId] ?: return JsonNull
    val content = styleContentBySkillId(exercise.primarySkill)
    val effective = registry[StyleComposer.resolveEffectiveStyle(recipe, content, registry)] ?: recipe
    val skill = skillById(exercise.primarySkill)
    val focus = presentationBySkillId(exercise.primarySkill)
    return buildJsonObject {
        put("effectiveStyleId", effective.id.value)
        // native-contrast's only requirement (StyleComposer's isSatisfiedBy) — surfaced directly so
        // Settings can hint "no content for this skill" regardless of which style is selected now.
        put("nativeContrastAvailable", content.nativeParallel.isNotEmpty())
        put("frontBlocks", blocksToJson(StyleComposer.compose(effective, StylePhase.Front, exercise, skill, focus, content)))
        put("backBlocks", blocksToJson(StyleComposer.compose(effective, StylePhase.Back, exercise, skill, focus, content)))
        // EN-21 (EnRuPackPlan.md §4.2/§4.3): deliberately its own field, not a block kind inside
        // frontBlocks/backBlocks — a lifehack renders the same way for every style, so Swift shows
        // it once, outside MacStyleBlockView's per-kind switch (see MacFlashCardView's backFace).
        put("lifehacks", JsonArray(StaticPackLifehackProvider.forSkill(exercise.primarySkill).map(::lifehackJson)))
    }
}

/** Shared by the per-skill back-face block above and [putSharedSnapshotHeader]'s pack-wide
 *  `lifehackGroups` listing — one wire shape for a [Lifehack], so the two never drift apart.
 *  macOS's own key for the status field is "status" (see [lifehackObjectJson]). */
private fun lifehackJson(hack: Lifehack): JsonObject = lifehackObjectJson(hack, "status")

private fun pairSnapshot(pair: ContrastPair): JsonObject = contrastPairJson(pair)
