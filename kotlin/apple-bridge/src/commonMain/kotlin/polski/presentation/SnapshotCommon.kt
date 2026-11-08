package polski.presentation

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.courseChainPresentation
import polski.data.nounLemma
import polski.data.presentationBySkillId
import polski.data.skillById
import polski.data.skills
import polski.srs.Rating
import polski.training.sentenceSeeds

/**
 * W3.2 (architect finding #5): [AppUiState]-to-JSON glue that iOS's `snapshot()` (`IosSnapshot.kt`)
 * and macOS's `snapshot()` (`MacSnapshot.kt`) used to each re-type byte for byte. None of this is
 * platform-specific — the wire shape here is identical on both hosts — so it lives once in
 * apple-bridge commonMain, and each host's own `snapshot()` just calls into it. Platform-specific pieces
 * (`styleBlocksSnapshot`'s field names, `matrixSnapshot`, the per-hack status key) stay in each
 * host file, because those really do differ by host.
 */

/** Shared `{id, title}` choice-list entry — every host's dropdown-style arrays (nouns, adjectives,
 *  owners, verbs, seeds, ...) use this same shape. */
internal fun choice(id: String, title: String): JsonObject = buildJsonObject { put("id", id); put("title", title) }

/** Every snapshot field that never differs by host, from `loadStatus` through `chainAnswers`.
 *  [styleBlocks] and [hackToJson] are the two places a host's own wire shape does differ
 *  (`styleBlocksSnapshot`'s field names; "statusLabel" on iOS vs "status" on macOS), so the caller
 *  supplies them rather than this function guessing one shape for both. */
internal fun JsonObjectBuilder.putSharedSnapshotHeader(
    state: AppUiState,
    styleBlocks: JsonElement,
    hackToJson: (Lifehack) -> JsonObject,
) {
    put("loadStatus", state.loadStatus.name)
    put("tab", state.tab.name)
    put("mode", state.mode.name)
    put("phase", state.phase.name)
    put("answerMode", state.answerMode.name)
    // Swift still speaks the pre-UC-10 2-value wire vocabulary (see [toLegacyWireValue]).
    put("explanationMethod", state.styleId.toLegacyWireValue())
    put("styleId", state.styleId.value)
    put("styleBlocks", styleBlocks)
    put("draft", state.draft)
    put("introPending", state.introPending)
    put("dueCount", state.dueCount)
    put("todayCount", state.todayCount)
    put("chainIndex", state.chainIndex)
    put("chainCount", state.chain.size)
    put("chainDisplayCount", state.chainDisplayCount)
    put("chainStepLabels", JsonArray(courseChainPresentation.steps.map { JsonPrimitive(it.label) }))
    put("chainStepSummary", courseChainPresentation.summary)
    put("chainCompletionTitle", courseChainPresentation.completion.title)
    put("seedIndex", state.seedIndex)
    put("showReference", state.showReference)
    put("showSkillPicker", state.showSkillPicker)
    put("revision", state.revision)
    put("savedRevision", state.savedRevision)
    put("error", state.error?.let(::JsonPrimitive) ?: JsonNull)
    put("nextDue", state.nextDue?.toEpochMilliseconds()?.let(::JsonPrimitive) ?: JsonNull)
    put("now", state.now?.toEpochMilliseconds()?.let(::JsonPrimitive) ?: JsonNull)
    put("seeds", JsonArray(sentenceSeeds.mapIndexed { index, seed -> choice(index.toString(), nounLemma(seed.nounId)) }))
    put("skills", JsonArray(skills.map { skill -> buildJsonObject {
        put("id", skill.id); put("title", skill.title); put("group", skill.group)
        put("level", skill.level); put("formula", skill.formula); put("theory", skill.theory)
        put("hasLifehack", StaticPackLifehackProvider.hasLifehacks(skill.id))
    } }))
    // EnRuPackPlan.md §4.3's pack-wide "Лайфхаки" listing (not the per-skill card block inside
    // styleBlocks) — every skill/topic the active pack's own lifehacks.json actually covers, in
    // curriculum order, so a settings/reference screen can draw one section without recomputing
    // the grouping itself.
    put("lifehackGroups", JsonArray(StaticPackLifehackProvider.listAll().map { group -> buildJsonObject {
        put("skillId", group.skillId?.let(::JsonPrimitive) ?: JsonNull)
        put("topic", group.topic?.let(::JsonPrimitive) ?: JsonNull)
        put("title", group.title)
        put("lifehacks", JsonArray(group.lifehacks.map(hackToJson)))
    } }))
    put("chainAnswers", JsonArray(if (state.phase == CardPhase.ChainComplete) state.chain.map { JsonPrimitive(it.expected) } else emptyList()))
    put("uiResetProgress", UiCopy.ResetProgress)
    put("uiProgressTab", UiCopy.ProgressTab)
    put("uiSettingsTab", UiCopy.SettingsTab)
    put("uiLifehacks", UiCopy.Lifehacks)
    put("uiAgain", UiCopy.Again)
    put("uiGood", UiCopy.Good)
    put("uiShowAnswer", UiCopy.ShowAnswer)
    put("uiCopy", JsonObject(UiCopy.all().mapValues { (_, value) -> JsonPrimitive(value) }))
}

/** UC-10/exercise wire shape: identical on every host because the question/reveal fields a
 *  flashcard needs never differ by platform (only [styleBlocksSnapshot]'s own block shape does). */
internal fun exerciseJson(state: AppUiState): JsonElement = state.exercise?.let { exercise ->
    buildJsonObject {
        val presentation = presentationBySkillId(exercise.primarySkill)
        val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
        put("id", exercise.id)
        put("skillId", exercise.primarySkill)
        put("skillTitle", skillById(exercise.primarySkill).title)
        put("skillLevel", skillById(exercise.primarySkill).level)
        // EnRuPackPlan.md §4.2: cheap enough to compute even on the front (unrevealed) face — a
        // front-side badge can hint "this skill has a lifehack" before the back's own full list
        // (styleBlocksSnapshot's "lifehacks") ever renders.
        put("hasLifehack", StaticPackLifehackProvider.hasLifehacks(exercise.primarySkill))
        put("source", exercise.source)
        put("sourceParts", JsonArray(sentenceHighlightParts(exercise.source, exercise.changes, ChangeSide.Before).map { part -> buildJsonObject {
            put("text", part.text); put("changed", part.isChanged)
        } }))
        if (state.phase == CardPhase.Question && state.introPending) {
            put("methodIntroduce", method.introduce)
        } else {
            put("prompt", exercise.prompt)
            put("methodLead", method.promptLead)
            if (state.phase == CardPhase.Question) put("methodRetrieve", method.retrieve)
            if (state.phase == CardPhase.Revealed) {
                put("methodFeedback", method.feedback)
                put("methodReview", method.review)
                put("expected", exercise.expected)
                put("expectedParts", JsonArray(sentenceHighlightParts(exercise.expected, exercise.changes, ChangeSide.After).map { part -> buildJsonObject {
                    put("text", part.text); put("changed", part.isChanged)
                } }))
                put("accepted", JsonArray(exercise.accepted.map(::JsonPrimitive)))
                put("explanation", exercise.explanation)
                put("formula", skillById(exercise.primarySkill).formula)
                put("changes", JsonArray(exercise.changes.map { change -> buildJsonObject {
                    put("from", change.from); put("to", change.to); put("reason", change.reason)
                    put("toParts", JsonArray(endingHighlightParts(change.from, change.to).map { part -> buildJsonObject {
                        put("text", part.text); put("changed", part.isChanged)
                    } }))
                } }))
                put("frozenAnswer", state.frozenAnswer?.let(::JsonPrimitive) ?: JsonNull)
                put("correct", state.evaluation?.correct?.let(::JsonPrimitive) ?: JsonNull)
                put("intervals", buildJsonObject {
                    Rating.entries.forEach { rating ->
                        put(rating.name, state.intervals?.get(rating)?.toEpochMilliseconds()?.let(::JsonPrimitive) ?: JsonNull)
                    }
                })
            }
        }
    }
} ?: JsonNull

/** "progress"/"totalReviews": identical on every host, read straight off [AppUiState.progress]. */
internal fun JsonObjectBuilder.putProgressFields(state: AppUiState) {
    val progress = state.progress
    put("progress", JsonArray(progress?.cards?.mapNotNull { card ->
        val skill = skills.firstOrNull { it.id == card.skillId } ?: return@mapNotNull null
        val stats = progress.stats[card.skillId]
        buildJsonObject {
            put("id", skill.id); put("title", skill.title); put("group", skill.group)
            put("reviews", stats?.reviews ?: 0); put("correct", stats?.correct ?: 0)
            put("streak", stats?.streak ?: 0); put("mistakes", stats?.mistakes ?: 0)
            put("due", card.card.due.toEpochMilliseconds())
        }
    } ?: emptyList()))
    put("totalReviews", state.progress?.totalReviews ?: 0)
}

/** "effects": identical on every host — [AppUiState.pendingEffects] is already host-neutral. */
internal fun JsonObjectBuilder.putEffects(state: AppUiState) {
    put("effects", JsonArray(state.pendingEffects.map { effect -> buildJsonObject {
        put("id", effect.id)
        when (effect) {
            is UiEffect.FocusReveal -> { put("kind", "focus"); put("exerciseId", effect.exerciseId) }
            is UiEffect.DownloadJson -> { put("kind", "export"); put("filename", effect.filename); put("json", effect.json) }
            is UiEffect.DownloadText -> { put("kind", "exportText"); put("filename", effect.filename); put("text", effect.text) }
            is UiEffect.ConfirmReset -> { put("kind", "reset"); put("prompt", effect.prompt) }
        }
    } }))
}

/** Shared per-[Lifehack] wire shape. Each host names the status field differently (iOS
 *  "statusLabel", macOS "status" — both pre-existing public contracts), so the caller passes
 *  [statusKey] rather than this layer silently merging the two into one name. */
internal fun lifehackObjectJson(hack: Lifehack, statusKey: String): JsonObject = buildJsonObject {
    put("id", hack.id)
    put("text", hack.text)
    put("citation", hack.source.citation)
    put("url", hack.source.url?.let(::JsonPrimitive) ?: JsonNull)
    put(statusKey, when (hack.status) { LifehackStatus.Editorial -> "editorial"; LifehackStatus.Community -> "community" })
}
