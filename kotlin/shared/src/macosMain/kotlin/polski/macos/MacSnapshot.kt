package polski.macos

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.adjectives
import polski.data.nounById
import polski.data.nouns
import polski.data.personalPronouns
import polski.data.possessives
import polski.data.skillById
import polski.data.skills
import polski.data.verbs
import polski.data.referenceChainRows
import polski.data.courseChainPresentation
import polski.data.referenceSystemCards
import polski.data.referencePipeline
import polski.data.referenceRussianSupport
import polski.data.referenceCaseTeaching
import polski.data.referenceVerbTeaching
import polski.data.referencePronounTeaching
import polski.data.comparisonNounIds
import polski.data.referenceTenseRows
import polski.data.referenceAspectRows
import polski.data.maleAccRows
import polski.data.courseMatrixIntroduction
import polski.data.courseAspectNoPresent
import polski.grammar.caseRows
import polski.grammar.caseSentence
import polski.grammar.nounPhrase
import polski.grammar.possessiveForm
import polski.grammar.verbForm
import polski.model.Aspect
import polski.model.Gender
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.Person
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Tense
import polski.presentation.AppUiState
import polski.presentation.CardPhase
import polski.presentation.chainDisplayCount
import polski.presentation.UiEffect
import polski.presentation.endingHighlightParts
import polski.presentation.changeHighlightParts
import polski.presentation.sentenceHighlightParts
import polski.presentation.ChangeSide
import polski.presentation.ContrastPair
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.StylePhase
import polski.presentation.StyleRegistry
import polski.presentation.blocksToJson
import polski.presentation.toLegacyWireValue
import polski.data.styleContentBySkillId
import polski.data.presentationBySkillId
import polski.srs.Rating
import polski.training.sentenceSeeds

internal fun snapshot(state: AppUiState): String = buildJsonObject {
    put("schemaVersion", 1)
    put("loadStatus", state.loadStatus.name)
    put("tab", state.tab.name)
    put("mode", state.mode.name)
    put("phase", state.phase.name)
    put("answerMode", state.answerMode.name)
    // Swift still speaks the pre-UC-10 2-value wire vocabulary (see [toLegacyWireValue]).
    put("explanationMethod", state.styleId.toLegacyWireValue())
    put("styleId", state.styleId.value)
    put("styleBlocks", styleBlocksSnapshot(state))
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
    put("seeds", JsonArray(sentenceSeeds.mapIndexed { index, seed -> choice(index.toString(), nounById(seed.nounId).lemma) }))
    put("skills", JsonArray(skills.map { skill -> buildJsonObject {
        put("id", skill.id); put("title", skill.title); put("group", skill.group)
        put("level", skill.level); put("formula", skill.formula); put("theory", skill.theory)
    } }))
    put("chainAnswers", JsonArray(if (state.phase == CardPhase.ChainComplete) state.chain.map { JsonPrimitive(it.expected) } else emptyList()))
    put("exercise", state.exercise?.let { exercise -> buildJsonObject {
        val presentation = presentationBySkillId(exercise.primarySkill)
        val method = if (state.styleId == StyleId.SituationFirst) presentation.situations else presentation.logic
        put("id", exercise.id)
        put("skillId", exercise.primarySkill)
        put("skillTitle", skillById(exercise.primarySkill).title)
        put("skillLevel", skillById(exercise.primarySkill).level)
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
    } } ?: JsonNull)
    put("referenceRows", JsonArray(if (state.showReference && !state.introPending) state.exercise?.let { exercise -> caseRows.map { row -> buildJsonObject {
        put("title", "${row.pl} · ${row.ru}")
        val form = nounPhrase(exercise.nounId, row.id, exercise.number, exercise.adjectiveId, exercise.possessive)
        put("text", form)
        put("pair", pairSnapshot(ContrastPair.generated(
            nounPhrase(exercise.nounId, GramCase.NOM, exercise.number, exercise.adjectiveId, exercise.possessive), form)))
    } } } ?: emptyList() else emptyList()))
    put("matrix", matrixSnapshot(state))
    put("progress", JsonArray(state.progress?.cards?.mapNotNull { card ->
        val skill = skills.firstOrNull { it.id == card.skillId } ?: return@mapNotNull null
        val stats = state.progress.stats[card.skillId]
        buildJsonObject {
            put("id", skill.id); put("title", skill.title); put("group", skill.group)
            put("reviews", stats?.reviews ?: 0); put("correct", stats?.correct ?: 0)
            put("streak", stats?.streak ?: 0); put("mistakes", stats?.mistakes ?: 0)
            put("due", card.card.due.toEpochMilliseconds())
        }
    } ?: emptyList()))
    put("totalReviews", state.progress?.totalReviews ?: 0)
    put("effects", JsonArray(state.pendingEffects.map { effect -> buildJsonObject {
        put("id", effect.id)
        when (effect) {
            is UiEffect.FocusReveal -> { put("kind", "focus"); put("exerciseId", effect.exerciseId) }
            is UiEffect.DownloadJson -> { put("kind", "export"); put("filename", effect.filename); put("json", effect.json) }
            is UiEffect.ConfirmReset -> { put("kind", "reset"); put("prompt", effect.prompt) }
        }
    } }))
}.toString()

private fun choice(id: String, title: String): JsonObject = buildJsonObject { put("id", id); put("title", title) }

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
    }
}

private fun pairSnapshot(pair: ContrastPair): JsonObject = buildJsonObject {
    put("from", pair.from)
    put("to", pair.to)
    put("beforeParts", JsonArray(pair.parts(ChangeSide.Before).map { part -> buildJsonObject {
        put("text", part.text); put("changed", part.isChanged)
    } }))
    put("afterParts", JsonArray(pair.parts(ChangeSide.After).map { part -> buildJsonObject {
        put("text", part.text); put("changed", part.isChanged)
    } }))
}

private fun matrixSnapshot(state: AppUiState): JsonElement {
    val selection = state.matrixSelection
    val number = NumberGram.fromId(selection.numberId)
    val owner = PossessiveId.fromId(selection.ownerId)
    val seed = SentenceSeed(selection.nounId, selection.adjectiveId)
    return buildJsonObject {
        put("section", selection.section.name)
        put("matrixIntroduction", courseMatrixIntroduction)
        put("aspectNoPresent", courseAspectNoPresent.ios)
        put("nounId", selection.nounId)
        put("adjectiveId", selection.adjectiveId)
        put("ownerId", selection.ownerId)
        put("numberId", selection.numberId)
        put("verbId", selection.verbId)
        put("feminineGroup", selection.feminineGroup)
        put("nouns", JsonArray(nouns.map { choice(it.id, "${it.lemma} — ${it.meaning}") }))
        put("adjectives", JsonArray(adjectives.map { choice(it.id, "${it.lemma} — ${it.meaning}") }))
        put("owners", JsonArray(possessives.map { choice(it.id.id, it.label) }))
        put("numbers", JsonArray(listOf(choice("sg", "Единственное"), choice("pl", "Множественное"))))
        put("verbs", JsonArray(verbs.filter { it.aspect == Aspect.IMPERFECTIVE }.map { choice(it.id, "${it.lemma} — ${it.meaning}") }))
        put("pipelineTitle", referencePipeline.title)
        put("pipelineSummary", referencePipeline.compactSummary)
        put("pipelineExample", referencePipeline.compactExample)
        put("supportTitle", referenceRussianSupport.compactTitle)
        put("supportLines", JsonArray(referenceRussianSupport.rows.map { JsonPrimitive(it.mobileLine) }))
        put("supportRows", JsonArray(referenceRussianSupport.rows.map { row -> buildJsonObject {
            put("line", row.mobileLine)
            put("comparisons", JsonArray(row.comparisons.map(::pairSnapshot)))
        } }))
        put("caseNote", referenceCaseTeaching.compactNote)
        put("systemCards", JsonArray(referenceSystemCards.map { card -> buildJsonObject {
            put("id", card.id); put("title", card.title)
            put("explanation", card.explanation); put("example", card.example)
            put("steps", JsonArray(card.steps.zipWithNext { from, to -> pairSnapshot(ContrastPair.generated(from, to)) }))
        } }))
        put("chainRows", JsonArray(referenceChainRows.map { row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to); put("change", row.change)
            put("beforeParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.Before).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
            put("afterParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.After).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
        } }))
        put("tenseRows", JsonArray(referenceTenseRows.map { row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to)
            put("beforeParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.Before).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
            put("afterParts", JsonArray(changeHighlightParts(row.from, row.to, ChangeSide.After).map { part -> buildJsonObject {
                put("text", part.text); put("changed", part.isChanged)
            } }))
        } }))
        put("aspectRows", JsonArray(referenceAspectRows.map { row -> buildJsonObject {
            put("label", row.label); put("from", row.from)
            put("entries", JsonArray(listOf(
                "Настоящее" to row.present, "Прошедшее" to row.past, "Будущее" to row.future,
            ).map { (label, form) -> buildJsonObject {
                put("label", label); put("available", form != null)
                if (form != null) {
                    put("from", row.from); put("to", form)
                    put("beforeParts", JsonArray(changeHighlightParts(row.from, form, ChangeSide.Before).map { part -> buildJsonObject {
                        put("text", part.text); put("changed", part.isChanged)
                    } }))
                    put("afterParts", JsonArray(changeHighlightParts(row.from, form, ChangeSide.After).map { part -> buildJsonObject {
                        put("text", part.text); put("changed", part.isChanged)
                    } }))
                }
            } }))
        } }))
        put("maleAccRows", JsonArray(maleAccRows.map { row -> buildJsonObject {
            put("label", row.label); put("title", row.title); put("rule", row.rule)
            put("examples", JsonArray(row.examples.map { example -> buildJsonObject {
                put("label", "Форма в Bierniku")
                put("from", example.from); put("to", example.to); put("change", example.sentence)
                put("beforeParts", JsonArray(changeHighlightParts(example.from, example.to, ChangeSide.Before).map { part -> buildJsonObject {
                    put("text", part.text); put("changed", part.isChanged)
                } }))
                put("afterParts", JsonArray(changeHighlightParts(example.from, example.to, ChangeSide.After).map { part -> buildJsonObject {
                    put("text", part.text); put("changed", part.isChanged)
                } }))
            } }))
        } }))
        put("cases", JsonArray(caseRows.map { row -> buildJsonObject {
            put("title", "${row.pl} · ${row.ru}")
            put("question", row.question); put("trigger", row.trigger)
            val phrase = nounPhrase(selection.nounId, row.id, number, selection.adjectiveId, owner)
            val sentence = caseSentence(seed, row.id, owner, number)
            put("phrase", phrase); put("sentence", sentence)
            put("phrasePair", pairSnapshot(ContrastPair.generated(
                nounPhrase(selection.nounId, GramCase.NOM, number, selection.adjectiveId, owner), phrase)))
            put("sentencePair", pairSnapshot(ContrastPair.generated(caseSentence(seed, GramCase.NOM, owner, number), sentence)))
        } }))
        put("comparisonCases", JsonArray(caseRows.map { choice(it.id.name, it.pl) }))
        put("comparison", JsonArray(comparisonNounIds.map { id ->
            val noun = nounById(id)
            buildJsonObject {
                put("title", noun.lemma)
                GramCase.entries.forEach { gramCase ->
                    val form = noun.forms.getValue(number).getValue(gramCase)
                    put(gramCase.name, form)
                    put("${gramCase.name}pair", pairSnapshot(ContrastPair.generated(
                        noun.forms.getValue(number).getValue(GramCase.NOM), form)))
                }
            }
        }))
        put("verbGenderControlLabel", referenceVerbTeaching.genderControlLabel.compact)
        put("verbGenderOptions", JsonArray(referenceVerbTeaching.genderOptions.map { choice(it.id, it.label.compact) }))
        put("verbFutureExplanation", referenceVerbTeaching.compactFutureExplanation)
        put("verbTenseLabels", buildJsonObject {
            Tense.entries.forEach { tense -> put(tense.id, referenceVerbTeaching.tenseLabels.getValue(tense).compact) }
        })
        put("verbsRows", JsonArray(referenceVerbTeaching.subjects.map { subject -> buildJsonObject {
            put("title", subject.label.compact)
            listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE).forEach { tense ->
                val form = verbForm(selection.verbId, tense, subject.person, subject.number, subject.gender(selection.feminineGroup))
                put(tense.id, form)
                put("${tense.id}Pair", pairSnapshot(ContrastPair.generated(verbs.first { it.id == selection.verbId }.lemma, form)))
            }
        } }))
        put("pronounIntro", referencePronounTeaching.compactIntro)
        put("pronounFooter", referencePronounTeaching.nativeFooter)
        put("pronounContexts", JsonArray(referencePronounTeaching.contexts.map { context -> buildJsonObject {
            put("id", context.id.id)
            put("iosCue", context.cue.ios)
        } }))
        put("possessiveTitle", referencePronounTeaching.possessiveTitle)
        put("possessiveCases", JsonArray(referencePronounTeaching.demo.cases.map { row -> buildJsonObject {
            put("id", row.id.id)
            put("iosCaption", row.ios)
        } }))
        put("pronouns", JsonArray(referencePronounTeaching.pronounIds.map { id ->
            val forms = personalPronouns.getValue(id)
            buildJsonObject {
            put("title", id)
            referencePronounTeaching.contexts.forEach { context ->
                val form = if (context.id == GramCase.LOC) forms.getValue(GramCase.LOC) else context.value(id, forms)
                put(context.id.id, form)
                put("${context.id.id}pair", pairSnapshot(ContrastPair.generated(id, form)))
            }
        } }))
        put("possessives", JsonArray(possessives.map { possessive -> buildJsonObject {
            put("title", possessive.label)
            referencePronounTeaching.demo.cases.forEach { row ->
                val phrase = referencePronounTeaching.demo.phrase(possessive.id, row.id)
                put(row.id.id, phrase)
                put("${row.id.id}pair", pairSnapshot(ContrastPair.generated(
                    referencePronounTeaching.demo.phrase(possessive.id, GramCase.NOM), phrase)))
            }
        } }))
    }
}
