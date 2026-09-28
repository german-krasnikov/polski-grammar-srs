package polski.ios

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import polski.data.adjectives
import polski.data.enPersonalPronouns
import polski.data.enVerbForm
import polski.data.enVerbs
import polski.data.nounById
import polski.data.nounLemma
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
import polski.presentation.EndingPart
import polski.data.presentationBySkillId
import polski.data.styleContentBySkillId
import polski.core.engine.MatrixColumn
import polski.core.engine.MatrixTableEngine
import polski.presentation.LifehackStatus
import polski.presentation.StaticPackLifehackProvider
import polski.presentation.StyleComposer
import polski.presentation.StyleId
import polski.presentation.StylePhase
import polski.presentation.StyleRegistry
import polski.presentation.blocksToJson
import polski.presentation.toLegacyWireValue
import polski.presentation.toViewModel
import polski.srs.Rating
import polski.training.sentenceSeeds

internal fun snapshot(state: AppUiState): String = buildJsonObject {
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
    put("seeds", JsonArray(sentenceSeeds.mapIndexed { index, seed -> choice(index.toString(), nounLemma(seed.nounId)) }))
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
private fun lifehacksSnapshot(skillId: String): JsonElement =
    JsonArray(StaticPackLifehackProvider.forSkill(skillId).map { hack -> buildJsonObject {
        put("id", hack.id)
        put("text", hack.text)
        put("citation", hack.source.citation)
        put("url", hack.source.url?.let(::JsonPrimitive) ?: JsonNull)
        put("statusLabel", when (hack.status) { LifehackStatus.Editorial -> "editorial"; LifehackStatus.Community -> "community" })
    } })

/** Emphasis contract §5: what the target row's "Стало" shows before reveal — never the answer. */
private const val referenceMaskPlaceholder = "?"

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
        val chainTable = MatrixTableEngine.build(
            rowAxis = referenceChainRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn("Целое предложение", { it.to }, contrastFrom = { it.from }),
                MatrixColumn("Что изменилось", { it.change }),
            ),
        ).toViewModel()
        put("chainRows", JsonArray(referenceChainRows.mapIndexed { i, row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to); put("change", row.change)
            val pairJson = pairSnapshot(requireNotNull(chainTable.rows[i].cells[0].contrast))
            put("beforeParts", pairJson.getValue("beforeParts")); put("afterParts", pairJson.getValue("afterParts"))
        } }))
        val tenseTable = MatrixTableEngine.build(
            rowAxis = referenceTenseRows,
            rowHeaderLabel = "Операция",
            rowHeader = { it.label },
            columns = listOf(MatrixColumn("Предложение", { it.to }, contrastFrom = { it.from })),
        ).toViewModel()
        put("tenseRows", JsonArray(referenceTenseRows.mapIndexed { i, row -> buildJsonObject {
            put("label", row.label); put("from", row.from); put("to", row.to)
            val pairJson = pairSnapshot(requireNotNull(tenseTable.rows[i].cells[0].contrast))
            put("beforeParts", pairJson.getValue("beforeParts")); put("afterParts", pairJson.getValue("afterParts"))
        } }))
        val aspectTable = MatrixTableEngine.build(
            rowAxis = referenceAspectRows,
            rowHeaderLabel = "Смысл",
            rowHeader = { it.label },
            columns = listOf(
                MatrixColumn("Настоящее", { it.present ?: "" }, contrastFrom = { row -> if (row.present == null) null else row.from }),
                MatrixColumn("Прошедшее", { it.past }, contrastFrom = { it.from }),
                MatrixColumn("Будущее", { it.future }, contrastFrom = { it.from }),
            ),
        ).toViewModel()
        put("aspectRows", JsonArray(referenceAspectRows.mapIndexed { i, row -> buildJsonObject {
            put("label", row.label); put("from", row.from)
            put("entries", JsonArray(listOf("Настоящее", "Прошедшее", "Будущее").mapIndexed { colIndex, label ->
                val cell = aspectTable.rows[i].cells[colIndex]
                buildJsonObject {
                    put("label", label); put("available", cell.contrast != null)
                    val contrast = cell.contrast
                    if (contrast != null) {
                        val pairJson = pairSnapshot(contrast)
                        put("from", contrast.from); put("to", cell.value)
                        put("beforeParts", pairJson.getValue("beforeParts")); put("afterParts", pairJson.getValue("afterParts"))
                    }
                }
            }))
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
        // UniversalCorePlan.md §5.3.3/§12 UC-09 (host wiring, part 2/2, iOS): every table below is
        // built through the same :core-engine MatrixTableEngine + MatrixTableViewModel that
        // MatrixTableViewModelTest already proves byte-identical to the pack's own grammar
        // functions — this snapshot no longer re-derives cell values by hand, it reads them off
        // the exported view model. The wire JSON shape Swift already binds to (named per-section
        // fields, not a generic grid) is unchanged on purpose: that shape is covered by
        // IosMatrixSnapshotTest and the SwiftUI screens/screenshots, and switching it is a
        // separate, host-visible change with its own risk (ContrastHighlightPlan.md scopes only
        // the *engine* switch here, not a host UI rewrite).
        val casesTable = MatrixTableEngine.build(
            rowAxis = caseRows,
            rowHeaderLabel = "Падеж · русская опора",
            rowHeader = { "${it.pl} · ${it.ru}" },
            columns = listOf(
                MatrixColumn("Вопрос / конструкция", { "${it.question} · ${it.trigger}" }),
                MatrixColumn(
                    "Целая группа слов",
                    { nounPhrase(selection.nounId, it.id, number, selection.adjectiveId, owner) },
                    contrastFrom = { nounPhrase(selection.nounId, GramCase.NOM, number, selection.adjectiveId, owner) },
                ),
                MatrixColumn(
                    "Целое предложение",
                    { caseSentence(seed, it.id, owner, number) },
                    contrastFrom = { caseSentence(seed, GramCase.NOM, owner, number) },
                ),
            ),
        ).toViewModel()
        put("cases", JsonArray(caseRows.mapIndexed { i, row -> buildJsonObject {
            put("title", "${row.pl} · ${row.ru}")
            put("question", row.question); put("trigger", row.trigger)
            val phraseCell = casesTable.rows[i].cells[1]
            val sentenceCell = casesTable.rows[i].cells[2]
            put("phrase", phraseCell.value); put("sentence", sentenceCell.value)
            put("phrasePair", pairSnapshot(requireNotNull(phraseCell.contrast)))
            put("sentencePair", pairSnapshot(requireNotNull(sentenceCell.contrast)))
        } }))
        put("comparisonCases", JsonArray(caseRows.map { choice(it.id.name, it.pl) }))
        val comparisonTable = MatrixTableEngine.build(
            rowAxis = GramCase.entries,
            rowHeaderLabel = "Падеж",
            rowHeader = { it.name },
            columns = comparisonNounIds.map { id ->
                MatrixColumn(
                    nounById(id).lemma,
                    { gramCase -> nounById(id).forms.getValue(number).getValue(gramCase) },
                    contrastFrom = { nounById(id).forms.getValue(number).getValue(GramCase.NOM) },
                )
            },
        ).toViewModel()
        put("comparison", JsonArray(comparisonNounIds.mapIndexed { colIndex, id ->
            val noun = nounById(id)
            buildJsonObject {
                put("title", noun.lemma)
                GramCase.entries.forEachIndexed { rowIndex, gramCase ->
                    val cell = comparisonTable.rows[rowIndex].cells[colIndex]
                    put(gramCase.name, cell.value)
                    put("${gramCase.name}pair", pairSnapshot(requireNotNull(cell.contrast)))
                }
            }
        }))
        put("verbGenderControlLabel", referenceVerbTeaching.genderControlLabel.compact)
        put("verbGenderOptions", JsonArray(referenceVerbTeaching.genderOptions.map { choice(it.id, it.label.compact) }))
        put("verbFutureExplanation", referenceVerbTeaching.compactFutureExplanation)
        put("verbTenseLabels", buildJsonObject {
            Tense.entries.forEach { tense -> put(tense.id, referenceVerbTeaching.tenseLabels.getValue(tense).compact) }
        })
        val verbTenses = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE)
        // EnRuAcceptance-2026-09-28.md §7 item 1/3: [verbs] is honestly empty for a pack whose
        // verbs aren't aspect-marked (en-ru) — [referenceVerbTeaching.subjects] is empty too
        // (ADR-36), so [verbsRows] below is empty regardless; this fallback just avoids crashing
        // before reaching that, matching MatrixTables.kt#verbsTable's own fix for this same gap.
        val verbLemma = verbs.firstOrNull { it.id == selection.verbId }?.lemma ?: ""
        val verbsTable = MatrixTableEngine.build(
            rowAxis = referenceVerbTeaching.subjects,
            rowHeaderLabel = "Кто",
            rowHeader = { it.label.compact },
            columns = verbTenses.map { tense ->
                MatrixColumn(
                    tense.id,
                    { subject -> verbForm(selection.verbId, tense, subject.person, subject.number, subject.gender(selection.feminineGroup)) },
                    contrastFrom = { verbLemma },
                )
            },
        ).toViewModel()
        put("verbsRows", JsonArray(referenceVerbTeaching.subjects.mapIndexed { i, subject -> buildJsonObject {
            put("title", subject.label.compact)
            verbTenses.forEachIndexed { colIndex, tense ->
                val cell = verbsTable.rows[i].cells[colIndex]
                put(tense.id, cell.value)
                put("${tense.id}Pair", pairSnapshot(requireNotNull(cell.contrast)))
            }
        } }))
        // EN-24 (UC-09 part 2/2, ios lane, EnRuPackPlan.md §5 gap H / §6): mirrors the web host's
        // `renderEnglishVerbMatrix` (ADR-28) — the one live English matrix (Present/Past/Future ×
        // person, fixed example verb "see") plus a do-support table, both read through
        // `enVerbForm` (`:shared` commonMain, shared with the web host — not re-derived here) and
        // the same MatrixTableEngine/MatrixTableViewModel every pl table above already uses.
        val englishExampleVerbId = "see"
        val englishExampleLemma = enVerbs.first { it.id == englishExampleVerbId }.lemma
        put("englishExampleLemma", englishExampleLemma)
        val englishTenses = listOf(Tense.PRESENT, Tense.PAST, Tense.FUTURE)
        val englishVerbsTable = MatrixTableEngine.build(
            rowAxis = enPersonalPronouns,
            rowHeaderLabel = "Кто",
            rowHeader = { it.subject },
            columns = englishTenses.map { tense ->
                MatrixColumn(tense.id, { pronoun -> enVerbForm(englishExampleVerbId, tense, pronoun.id) }, contrastFrom = { englishExampleLemma })
            },
        ).toViewModel()
        put("englishVerbsRows", JsonArray(enPersonalPronouns.mapIndexed { i, pronoun -> buildJsonObject {
            put("title", pronoun.subject)
            englishTenses.forEachIndexed { colIndex, tense ->
                val cell = englishVerbsTable.rows[i].cells[colIndex]
                put(tense.id, cell.value)
                put("${tense.id}Pair", pairSnapshot(requireNotNull(cell.contrast)))
            }
        } }))
        // "do"/"does" (Present) and invariant "did" (Past); the future column has no do-support at
        // all (plan §5 gap H — future is built with "will" alone) so it carries no pair, unlike
        // every other matrix cell above.
        val englishDoSupportTable = MatrixTableEngine.build(
            rowAxis = enPersonalPronouns,
            rowHeaderLabel = "Кто",
            rowHeader = { it.subject },
            columns = listOf(Tense.PRESENT, Tense.PAST).map { tense ->
                MatrixColumn(tense.id, { pronoun -> enVerbForm("do", tense, pronoun.id) }, contrastFrom = { "do" })
            },
        ).toViewModel()
        put("englishDoSupportRows", JsonArray(enPersonalPronouns.mapIndexed { i, pronoun -> buildJsonObject {
            put("title", pronoun.subject)
            listOf(Tense.PRESENT, Tense.PAST).forEachIndexed { colIndex, tense ->
                val cell = englishDoSupportTable.rows[i].cells[colIndex]
                put(tense.id, cell.value)
                put("${tense.id}Pair", pairSnapshot(requireNotNull(cell.contrast)))
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
        // The LOC context's own column keeps the raw locative form instead of `context.value`'s
        // preposition prefix — the same override Android's `androidLine` applies (CourseData.kt)
        // — so this host-specific choice lives in the column definition, not a second copy of the
        // table shape; every other context column reads through the pack's own `context.value`.
        val pronounsTable = MatrixTableEngine.build(
            rowAxis = referencePronounTeaching.pronounIds,
            rowHeaderLabel = "Кто",
            rowHeader = { it },
            columns = referencePronounTeaching.contexts.map { context ->
                MatrixColumn(
                    context.id.id,
                    { id -> if (context.id == GramCase.LOC) personalPronouns.getValue(id).getValue(GramCase.LOC) else context.value(id, personalPronouns.getValue(id)) },
                    contrastFrom = { id -> id },
                )
            },
        ).toViewModel()
        put("pronouns", JsonArray(referencePronounTeaching.pronounIds.mapIndexed { i, id -> buildJsonObject {
            put("title", id)
            referencePronounTeaching.contexts.forEachIndexed { colIndex, context ->
                val cell = pronounsTable.rows[i].cells[colIndex]
                put(context.id.id, cell.value)
                put("${context.id.id}pair", pairSnapshot(requireNotNull(cell.contrast)))
            }
        } }))
        val possessivesTable = MatrixTableEngine.build(
            rowAxis = possessives,
            rowHeaderLabel = "Кому принадлежит",
            rowHeader = { it.label },
            columns = referencePronounTeaching.demo.cases.map { case ->
                MatrixColumn(
                    case.id.id,
                    { p -> referencePronounTeaching.demo.phrase(p.id, case.id) },
                    contrastFrom = { p -> referencePronounTeaching.demo.phrase(p.id, GramCase.NOM) },
                )
            },
        ).toViewModel()
        put("possessives", JsonArray(possessives.mapIndexed { i, possessive -> buildJsonObject {
            put("title", possessive.label)
            referencePronounTeaching.demo.cases.forEachIndexed { colIndex, row ->
                val cell = possessivesTable.rows[i].cells[colIndex]
                put(row.id.id, cell.value)
                put("${row.id.id}pair", pairSnapshot(requireNotNull(cell.contrast)))
            }
        } }))
    }
}
