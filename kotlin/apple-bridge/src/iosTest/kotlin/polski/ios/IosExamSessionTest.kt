package polski.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import polski.exam.grammar.GrammarExamScorer

class IosExamSessionTest {
    private fun IosExamSession.view() = Json.parseToJsonElement(currentSnapshot()).jsonObject

    @Test
    fun firstWrongStandaloneChoicesExposeImmediatePracticeCommit() {
        val session = IosExamSession()
        session.selectExam("b1-2017")
        session.selectModule("grammar")
        val grammarId = session.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content
        session.answerInModule("grammar", grammarId, "dobry teatr", 1_000L)
        val grammar = session.view().getValue("grammar").jsonObject
        assertTrue(grammar.containsKey("practice"), "first wrong grammar choice must expose practice feedback")
        assertEquals(true, grammar.getValue("committed").jsonPrimitive.content.toBoolean())

        session.selectModule("reading")
        val question = session.view().getValue("part").jsonObject.getValue("questions").jsonArray.first().jsonObject
        val readingId = question.getValue("id").jsonPrimitive.content
        session.answerInModule("reading", readingId, "kioski internetowe są popularne w całym kraju", 2_000L)
        val reading = session.view().getValue("part").jsonObject
        assertTrue(reading.containsKey("practice"), "first wrong reading choice must expose practice feedback")
        assertEquals(true, reading.getValue("questions").jsonArray.first().jsonObject.getValue("committed").jsonPrimitive.content.toBoolean())
    }

    @Test
    fun firstCommitAndGenerationGuardKeepGrammarAnswerStable() {
        val session = IosExamSession()
        session.selectExam("b1-2017")
        session.selectModule("grammar")
        val first = session.view().getValue("grammar").jsonObject
        val id = first.getValue("itemId").jsonPrimitive.content
        session.answerInModule("grammar", id, "dobry teatr", 1_000L)
        val committed = session.view().getValue("grammar").jsonObject
        assertEquals("dobry teatr", committed.getValue("committedRaw").jsonPrimitive.content)
        session.answerInModule("grammar", id, "dobrego teatru", 1_001L)
        assertEquals("dobry teatr", session.view().getValue("grammar").jsonObject.getValue("answer").jsonPrimitive.content)
        val practice = committed.getValue("practice").jsonObject
        val generation = practice.getValue("generation").jsonPrimitive.long
        session.advancePracticeFocusInModule("grammar", committed.getValue("testId").jsonPrimitive.content,
            id, generation - 1, 3_000L)
        assertEquals(id, session.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content)
        session.advancePracticeFocusInModule("grammar", committed.getValue("testId").jsonPrimitive.content,
            id, generation, 3_001L)
        assertEquals(1, session.view().getValue("grammar").jsonObject.getValue("currentIndex").jsonPrimitive.content.toInt())
    }

    @Test
    fun groupedFocusStaysInPassageAndPreferenceSwitchPreservesDrafts() {
        val session = IosExamSession()
        session.selectExam("b1-2017")
        session.selectModule("reading")
        val part = session.view().getValue("part").jsonObject
        val id = part.getValue("questions").jsonArray.first().jsonObject.getValue("id").jsonPrimitive.content
        val group = part.getValue("id").jsonPrimitive.content
        session.answerInModule("reading", id, "kioski internetowe są popularne w całym kraju", 1_000L)
        val practice = session.view().getValue("part").jsonObject.getValue("practice").jsonObject
        assertEquals(true, practice.getValue("visible").jsonObject.getValue("mayAutoAdvance").jsonPrimitive.content.toBoolean())
        session.advancePracticeFocusInModule("reading", group, id, practice.getValue("generation").jsonPrimitive.long, 3_000L)
        val focused = session.view().getValue("part").jsonObject
        assertEquals(group, focused.getValue("id").jsonPrimitive.content)
        assertEquals(JsonNull, focused.getValue("practice").jsonObject.getValue("visible"))
        assertTrue(focused.getValue("practice").jsonObject.getValue("focusedQuestionId").jsonPrimitive.content != id)
        session.setImmediateFeedbackEnabled(false)
        val off = session.view().getValue("part").jsonObject
        assertEquals(false, off.getValue("questions").jsonArray.first().jsonObject.getValue("committed").jsonPrimitive.content.toBoolean())
        assertEquals("kioski internetowe są popularne w całym kraju", off.getValue("questions").jsonArray.first().jsonObject.getValue("answer").jsonPrimitive.content)
        session.answerInModule("reading", id, "kioski internetowe oferują dostęp do Internetu", 3_001L)
        session.setImmediateFeedbackEnabled(true)
        val on = session.view().getValue("part").jsonObject
        assertEquals("kioski internetowe oferują dostęp do Internetu", on.getValue("questions").jsonArray.first().jsonObject.getValue("answer").jsonPrimitive.content)
        assertEquals(false, on.getValue("questions").jsonArray.first().jsonObject.getValue("committed").jsonPrimitive.content.toBoolean())
    }

    @Test
    fun frozenGroupedReviewUsesSubmittedAnswersIncludingBlank() {
        val session = IosExamSession()
        session.selectExam("b1-2019")
        session.selectModule("listening")
        val question = session.view().getValue("part").jsonObject.getValue("questions").jsonArray.first().jsonObject
        val id = question.getValue("id").jsonPrimitive.content
        session.answerInModule("listening", id, "a) w kinie.", 1_000L)
        session.submitInModule("listening", 2_000L)
        val review = session.view().getValue("part").jsonObject.getValue("review").jsonArray
        assertTrue(review.size > 1)
        assertEquals("a) w kinie.", review.first().jsonObject.getValue("chosenAnswer").jsonPrimitive.content)
        assertTrue(review.first().jsonObject.getValue("officialExamples").jsonArray.isNotEmpty())
        assertEquals(JsonNull, review[1].jsonObject.getValue("chosenAnswer"))
        session.setImmediateFeedbackEnabled(false)
        assertEquals(review, session.view().getValue("part").jsonObject.getValue("review").jsonArray)
    }

    @Test
    fun textCheckAndClosedSetCommitOnlyWhenComplete() {
        val session = IosExamSession()
        session.selectExam("c1-2024-04")
        session.selectModule("grammar")
        val firstTextIndex = (0 until 20).first { index ->
            while (session.view().getValue("grammar").jsonObject.getValue("currentIndex").jsonPrimitive.content.toInt() < index) session.next()
            session.view().getValue("grammar").jsonObject.getValue("questionType").jsonPrimitive.content == "fill_gap"
        }
        val text = session.view().getValue("grammar").jsonObject
        assertEquals(firstTextIndex, text.getValue("currentIndex").jsonPrimitive.content.toInt())
        val id = text.getValue("itemId").jsonPrimitive.content
        session.answerInModule("grammar", id, "próba", 1_000L)
        assertEquals(false, session.view().getValue("grammar").jsonObject.getValue("committed").jsonPrimitive.content.toBoolean())
        session.checkPracticeAnswerInModule("grammar", id, 1_001L)
        assertEquals(true, session.view().getValue("grammar").jsonObject.getValue("committed").jsonPrimitive.content.toBoolean())
        session.answerInModule("grammar", id, "zmiana", 1_002L)
        assertEquals("próba", session.view().getValue("grammar").jsonObject.getValue("answer").jsonPrimitive.content)

        val listening = IosExamSession()
        listening.selectExam("b2-2017")
        listening.selectModule("listening")
        repeat(3) { listening.next() }
        val question = listening.view().getValue("part").jsonObject.getValue("questions").jsonArray.first().jsonObject
        assertEquals("unordered_set", question.getValue("answerMode").jsonPrimitive.content)
        val questionId = question.getValue("id").jsonPrimitive.content
        listening.answerInModule("listening", questionId, "1", 2_000L)
        assertEquals(false, listening.view().getValue("part").jsonObject.getValue("questions").jsonArray.first().jsonObject
            .getValue("committed").jsonPrimitive.content.toBoolean())
        listening.answerInModule("listening", questionId, "1|3", 2_001L)
        assertEquals(true, listening.view().getValue("part").jsonObject.getValue("questions").jsonArray.first().jsonObject
            .getValue("committed").jsonPrimitive.content.toBoolean())
    }

    @Test
    fun timedPartRemainsEditableWithoutEarlyKey() {
        val session = IosExamSession()
        session.selectExam("b1-2019")
        session.startTimed(1_000L)
        val part = session.view().getValue("part").jsonObject
        val id = part.getValue("questions").jsonArray.first().jsonObject.getValue("id").jsonPrimitive.content
        session.answerInModule("listening", id, "a) w kinie.", 1_001L)
        session.answerInModule("listening", id, "b) w bibliotece.", 1_002L)
        val current = session.view().getValue("part").jsonObject
        assertEquals("b) w bibliotece.", current.getValue("questions").jsonArray.first().jsonObject.getValue("answer").jsonPrimitive.content)
        assertEquals(false, current.getValue("practice").jsonObject.getValue("enabled").jsonPrimitive.content.toBoolean())
        assertEquals(JsonNull, current.getValue("practice").jsonObject.getValue("visible"))
        assertFalse(current.containsKey("review"))
    }

    @Test
    fun publishedPartsExposeSourceCardsAndKeepIndependentDrafts() {
        val session = IosExamSession()
        session.selectExam("b1-2019")
        assertEquals(listOf(true, true, true, true), session.view().getValue("modules").jsonArray.map {
            it.jsonObject.getValue("availableOnIpad").jsonPrimitive.content.toBoolean()
        })
        session.selectModule("reading")
        val reading = session.view().getValue("part").jsonObject
        val questionId = reading.getValue("questions").jsonArray.first().jsonObject.getValue("id").jsonPrimitive.content
        assertTrue(reading.getValue("text").jsonPrimitive.content.isNotBlank())
        assertFalse(reading.containsKey("correctAnswer"))
        session.answer(questionId, "próba")
        session.selectModule("listening")
        val listening = session.view().getValue("part").jsonObject
        assertTrue(listening.getValue("audioAssetId").jsonPrimitive.content.isNotBlank())
        assertTrue(listening.getValue("segmentEndMs").jsonPrimitive.long > listening.getValue("segmentStartMs").jsonPrimitive.long)
        session.selectModule("writing")
        val writing = session.view().getValue("part").jsonObject
        assertTrue(writing.getValue("sets").jsonArray.isNotEmpty())
        val set = writing.getValue("sets").jsonArray.first().jsonPrimitive.content
        session.selectWritingSet(set)
        val task = session.view().getValue("part").jsonObject
        val taskId = task.getValue("taskId").jsonPrimitive.content
        session.answer(taskId, "Żółć. Mój tekst.")
        assertEquals("Żółć. Mój tekst.", session.view().getValue("part").jsonObject.getValue("draft").jsonPrimitive.content)
        session.submit()
        assertEquals(JsonNull, session.view().getValue("part").jsonObject.getValue("finalPercent"))
        session.selectModule("reading")
        val restored = session.view().getValue("part").jsonObject
        assertEquals("próba", restored.getValue("questions").jsonArray.first().jsonObject.getValue("answer").jsonPrimitive.content)
    }

    @Test
    fun timedDeadlineFreezesCurrentPartBeforeLateAnswer() {
        val session = IosExamSession()
        session.selectExam("b1-2017")
        session.startTimed(1_000L)
        val started = session.view().getValue("sitting").jsonObject
        assertEquals("listening", started.getValue("module").jsonPrimitive.content)
        val deadline = started.getValue("deadlineEpochMs").jsonPrimitive.long
        val questionId = session.view().getValue("part").jsonObject.getValue("questions").jsonArray.first()
            .jsonObject.getValue("id").jsonPrimitive.content
        session.answerInModule("listening", questionId, "late", deadline)
        val advanced = session.view().getValue("sitting").jsonObject
        assertEquals("reading", advanced.getValue("module").jsonPrimitive.content)
        assertEquals("", session.view().getValue("part").jsonObject.getValue("questions").jsonArray.first()
            .jsonObject.getValue("answer").jsonPrimitive.content)
        assertTrue(advanced.getValue("frozenModules").jsonArray.map { it.jsonPrimitive.content }.contains("listening"))
    }

    @Test
    fun queuedActionsKeepTheirRenderedModuleAfterDeadlineTransition() {
        val session = IosExamSession()
        session.selectExam("b1-2017")
        session.startTimed(1_000L)
        val listeningId = session.view().getValue("part").jsonObject.getValue("questions").jsonArray.first()
            .jsonObject.getValue("id").jsonPrimitive.content
        val deadline = session.view().getValue("sitting").jsonObject.getValue("deadlineEpochMs").jsonPrimitive.long
        session.tick(deadline)
        val before = session.view()
        assertEquals("reading", before.getValue("sitting").jsonObject.getValue("module").jsonPrimitive.content)
        session.answerInModule("listening", listeningId, "stale", deadline + 1)
        session.submitInModule("listening", deadline + 2)
        session.nextInModule("listening", deadline + 3)
        val after = session.view()
        assertEquals("reading", after.getValue("sitting").jsonObject.getValue("module").jsonPrimitive.content)
        assertEquals(before.getValue("sitting").jsonObject.getValue("frozenModules"),
            after.getValue("sitting").jsonObject.getValue("frozenModules"))
        assertEquals(before.getValue("part").jsonObject.getValue("position"), after.getValue("part").jsonObject.getValue("position"))
        assertEquals(before.getValue("part").jsonObject.getValue("questions"), after.getValue("part").jsonObject.getValue("questions"))
    }

    @Test
    fun writingSetBoundaryAcceptsNullAndRejectsUnknownWithoutErasingDraft() {
        val session = IosExamSession()
        session.selectExam("b1-2019")
        session.selectModule("writing")
        val firstSet = session.view().getValue("part").jsonObject.getValue("sets").jsonArray.first().jsonPrimitive.content
        session.selectWritingSetAt(firstSet, 100L)
        val taskId = session.view().getValue("part").jsonObject.getValue("taskId").jsonPrimitive.content
        session.answerInModule("writing", taskId, "Żółć draft", 101L)
        session.selectWritingSetAt("not-a-set", 102L)
        assertEquals("invalid_set", session.view().getValue("errorCode").jsonPrimitive.content)
        assertEquals("Żółć draft", session.view().getValue("part").jsonObject.getValue("draft").jsonPrimitive.content)
        session.selectWritingSetAt(null, 103L)
        assertEquals(JsonNull, session.view().getValue("errorCode"))
        session.selectWritingSetAt(firstSet, 104L)
        assertEquals("Żółć draft", session.view().getValue("part").jsonObject.getValue("draft").jsonPrimitive.content)
    }

    @Test
    fun timedWritingSetBoundaryUsesSameNullableSourceSetAndKeepsDraft() {
        val session = IosExamSession()
        session.selectExam("b1-2019")
        session.startTimed(1_000L)
        var now = 1_001L
        while (session.view().getValue("sitting").jsonObject.getValue("module").jsonPrimitive.content != "writing") {
            val module = session.view().getValue("sitting").jsonObject.getValue("module").jsonPrimitive.content
            session.submitInModule(module, now++)
        }
        val firstSet = session.view().getValue("part").jsonObject.getValue("sets").jsonArray.first().jsonPrimitive.content
        session.selectWritingSetAt(firstSet, now++)
        val taskId = session.view().getValue("part").jsonObject.getValue("taskId").jsonPrimitive.content
        session.answerInModule("writing", taskId, "Żółć draft", now++)
        session.selectWritingSetAt("invalid", now++)
        assertEquals("invalid_set", session.view().getValue("errorCode").jsonPrimitive.content)
        assertEquals("Żółć draft", session.view().getValue("part").jsonObject.getValue("draft").jsonPrimitive.content)
        session.selectWritingSetAt(null, now++)
        assertEquals(JsonNull, session.view().getValue("errorCode"))
        session.selectWritingSetAt(firstSet, now++)
        assertEquals("Żółć draft", session.view().getValue("part").jsonObject.getValue("draft").jsonPrimitive.content)
    }

    @Test
    fun unknownNonGrammarItemReportsErrorAndKeepsAnswer() {
        val session = IosExamSession()
        session.selectExam("b1-2019")
        for (module in listOf("reading", "listening", "writing")) {
            session.selectModule(module)
            if (module == "writing") {
                val set = session.view().getValue("part").jsonObject.getValue("sets").jsonArray.first().jsonPrimitive.content
                session.selectWritingSet(set)
            }
            val before = session.view().getValue("part")
            session.answerInModule(module, "unknown-source-id", "bad", 10L)
            assertEquals("unknown_item", session.view().getValue("errorCode").jsonPrimitive.content, module)
            assertEquals(before, session.view().getValue("part"), module)
        }
    }

    @Test
    fun everyPublishedPaperHasFourNativePartSnapshotsAndTimedCover() {
        for (entry in polski.exam.ExamCatalog.entries) {
            val session = IosExamSession()
            session.selectExam(entry.routeId)
            for (module in entry.modules) {
                session.selectModule(module.module.name.lowercase())
                val snapshot = session.view()
                assertEquals(JsonNull, snapshot.getValue("errorCode"), entry.routeId)
                if (module.module == polski.exam.ExamModule.Grammar) {
                    assertTrue(snapshot.getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content.isNotBlank())
                } else if (module.module == polski.exam.ExamModule.Writing) {
                    assertTrue(snapshot.getValue("part").jsonObject.getValue("sets").jsonArray.isNotEmpty())
                } else {
                    assertTrue(snapshot.getValue("part").jsonObject.getValue("id").jsonPrimitive.content.isNotBlank())
                }
            }
            session.startTimed(5_000L)
            assertEquals(entry.modules.first().module.name.lowercase(), session.view().getValue("sitting").jsonObject
                .getValue("module").jsonPrimitive.content)
        }
    }

    @Test
    fun writingGuidanceReachesStandaloneAndTimedNativeSnapshots() {
        for ((route, category, languagePoints, total) in listOf(
            listOf("b1-2017", "bogactwo językowe", "10", "30"),
            listOf("b2-2017", "bogactwo językowe", "16", "40"),
            listOf("c1-2022-11", "Środki językowe", "18", "40"),
        )) {
            val session = IosExamSession()
            session.selectExam(route)
            session.selectModule("writing")
            session.selectWritingSet("I")
            val standalone = session.view().getValue("part").jsonObject.getValue("instruction").jsonPrimitive.content
            assertTrue(standalone.contains(category), route)
            assertTrue(standalone.contains("0 – $languagePoints p."), route)
            assertTrue(standalone.contains("$total p."), route)
            session.startTimed(1_000L)
            var now = 1_001L
            while (session.view().getValue("sitting").jsonObject.getValue("module").jsonPrimitive.content != "writing") {
                val module = session.view().getValue("sitting").jsonObject.getValue("module").jsonPrimitive.content
                session.submitInModule(module, now++)
            }
            session.selectWritingSetAt("I", now)
            val timed = session.view().getValue("part").jsonObject.getValue("instruction").jsonPrimitive.content
            assertEquals(standalone, timed, route)
        }
    }

    @Test
    fun compactOpenSetAnswerKeepsManualPendingRatherThanBecomingMalformed() {
        val session = IosExamSession()
        session.setImmediateFeedbackEnabled(false) // Legacy direct answer bypasses current-task practice controls.
        session.selectExam("b2-2019")
        session.selectModule("listening")
        val questionId = "certyfikat-polski-B2-2019-listening-5-1"
        session.answer(questionId, "żółć próba")
        session.submit()
        val question = polski.exam.officialListeningData.getValue("b2-2019").tasks
            .flatMap { it.questions }.single { it.id == questionId }
        val expected = polski.exam.listening.ListeningExamScorer.grade(question, "żółć próba")
        val actual = session.view().getValue("part").jsonObject.getValue("score").jsonObject
        assertTrue(expected.pendingQuarter > 0)
        assertEquals(expected.pendingQuarter.toString(), actual.getValue("manualPendingQuarter").jsonPrimitive.content)
        assertEquals(JsonNull, actual.getValue("finalPercent"))
    }

    @Test
    fun submittedReviewProjectsFourSourceRubricCriteriaInOrderWithoutAwardingThem() {
        val item = polski.exam.grammar.GrammarItem(
            itemId = "source-style-rubric", questionType = polski.exam.QuestionType.FillGap,
            context = "Polecenie", gap = null, options = emptyList(), correctAnswer = "wzór",
            points = 1, rubricComponents = listOf(
                polski.exam.grammar.GrammarRubricComponent("c3", 3, 1, "Kryterium trzecie"),
                polski.exam.grammar.GrammarRubricComponent("c1", 1, 1, "Kryterium pierwsze"),
                polski.exam.grammar.GrammarRubricComponent("c4", 4, 1, "Kryterium czwarte"),
                polski.exam.grammar.GrammarRubricComponent("c2", 2, 1, "Kryterium drugie"),
            ),
        )
        val source = polski.exam.grammar.GrammarTest("source-style", "Polecenie", listOf(item))
        val row = grammarReviewItemJson(source, item, mapOf(item.itemId to "moja odpowiedź"))
        val rubric = row.getValue("rubricComponents").jsonArray.map { it.jsonObject }
        assertEquals(listOf("c1", "c2", "c3", "c4"), rubric.map { it.getValue("componentId").jsonPrimitive.content })
        assertEquals(listOf("Kryterium pierwsze", "Kryterium drugie", "Kryterium trzecie", "Kryterium czwarte"),
            rubric.map { it.getValue("criterionText").jsonPrimitive.content })
        assertEquals(listOf("0,25", "0,25", "0,25", "0,25"), rubric.map { it.getValue("creditPoints").jsonPrimitive.content })
        assertEquals("0", row.getValue("confirmedQuarter").jsonPrimitive.content)
        assertFalse(row.containsKey("rubricAwardedQuarter"), "review criteria are not automatic awards")
    }

    @Test
    fun questionTargetIsVisibleButOfficialAnswerIsHiddenUntilSubmit() {
        val session = IosExamSession()
        session.selectExam("b1-2017")
        session.selectModule("grammar")
        val targetId = "B1-2017-g4-1"
        while (session.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content != targetId) {
            session.next()
        }
        val before = session.view().getValue("grammar").jsonObject
        assertEquals("z gazety", before.getValue("questionTarget").jsonPrimitive.content)
        assertEquals("", before.getValue("answer").jsonPrimitive.content)
        assertFalse(before.containsKey("review"))
        assertFalse(before.containsKey("officialExamples"))
        session.submit()
        val after = session.view().getValue("grammar").jsonObject
        val review = after.getValue("review").jsonArray.single {
            it.jsonObject.getValue("itemId").jsonPrimitive.content == targetId
        }.jsonObject
        assertEquals("skąd", review.getValue("officialExamples").jsonArray.first().jsonPrimitive.content)
    }

    @Test
    fun catalogQuestionAnswerAndGradeUseOfficialSourceAndSharedEvaluator() {
        val session = IosExamSession()
        val catalog = session.view()
        assertEquals("catalog", catalog.getValue("screen").jsonPrimitive.content)
        assertEquals(16, catalog.getValue("catalog").jsonArray.size)
        assertEquals("B1 Certyfikat 2017", catalog.getValue("catalog").jsonArray.first().jsonObject
            .getValue("displayTitle").jsonPrimitive.content)
        val published = catalog.getValue("catalog").jsonArray.map { it.jsonObject }
        assertEquals(listOf("c1-2022-03", "c1-2022-11", "c1-2023-04", "c1-2023-11", "c1-2024-04"),
            published.takeLast(5).map { it.getValue("routeId").jsonPrimitive.content })
        assertEquals("C1 Certyfikat 2022-11", published[12].getValue("displayTitle").jsonPrimitive.content)
        assertEquals("C1 Certyfikat 2024-04", published.last().getValue("displayTitle").jsonPrimitive.content)
        session.selectExam("b1-2017")
        val modules = session.view().getValue("modules").jsonArray
        assertEquals(listOf("listening", "reading", "grammar", "writing"), modules.map { it.jsonObject.getValue("module").jsonPrimitive.content })
        assertEquals(listOf(true, true, true, true), modules.map { it.jsonObject.getValue("availableOnIpad").jsonPrimitive.content.toBoolean() })
        session.selectModule("grammar")
        val first = session.view().getValue("grammar").jsonObject
        assertEquals("B1-2017-g1-1", first.getValue("itemId").jsonPrimitive.content)
        assertEquals("dumni są z ___", first.getValue("context").jsonPrimitive.content)
        assertEquals("choose_form", first.getValue("questionType").jsonPrimitive.content)
        assertEquals(listOf("dobry teatr", "dobrego teatru", "dobrym teatrem"), first.getValue("options").jsonArray.map { it.jsonPrimitive.content })
        assertFalse(first.containsKey("officialExamples"), "the key is withheld before submit")
        session.answer("B1-2017-g1-1", "dobrego teatru")
        session.next()
        assertEquals("B1-2017-g1-2", session.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content)
        session.previous()
        assertEquals("dobrego teatru", session.view().getValue("grammar").jsonObject.getValue("answer").jsonPrimitive.content)
        session.submit()
        val graded = session.view()
        assertEquals("grammarResult", graded.getValue("screen").jsonPrimitive.content)
        val result = graded.getValue("grammar").jsonObject
        val firstItem = polski.exam.officialGrammarData.getValue("b1-2017").first().items.first()
        val expected = GrammarExamScorer.grade(firstItem, "dobrego teatru")
        assertEquals(expected.confirmedQuarter.toString(), result.getValue("confirmedQuarter").jsonPrimitive.content)
        assertEquals(expected.pendingQuarter.toString(), result.getValue("manualPendingQuarter").jsonPrimitive.content)
        assertTrue(result.getValue("final").jsonPrimitive.content.toBoolean())
        assertEquals(expected.status.name, result.getValue("review").jsonArray[0].jsonObject.getValue("status").jsonPrimitive.content)
        session.answer("B1-2017-g1-1", "wrong")
        assertEquals("dobrego teatru", session.view().getValue("grammar").jsonObject.getValue("answer").jsonPrimitive.content)
    }

    @Test
    fun c1AprilPairedInflectionsUseWholeNativeAttempt() {
        val firstId = "certyfikat-polski-C1-2024-04-g1-q8"
        val secondId = "certyfikat-polski-C1-2024-04-g1-q9"
        for ((firstAnswer, secondAnswer, expectedStatus) in listOf(
            Triple("kilkorgiem", "przyjaciół", "ConfirmedFull"),
            Triple("kilku", "przyjaciół", "PendingManual"),
        )) {
            val session = IosExamSession()
            session.selectExam("c1-2024-04")
            session.selectModule("grammar")
            repeat(7) { session.next() }
            assertEquals(firstId, session.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content)
            session.answer(firstId, firstAnswer)
            session.next()
            assertEquals(secondId, session.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content)
            session.answer(secondId, secondAnswer)
            while (!session.view().getValue("grammar").jsonObject.getValue("isLast").jsonPrimitive.content.toBoolean()) {
                session.next()
            }
            session.submit()
            val review = session.view().getValue("grammar").jsonObject.getValue("review").jsonArray
                .map { it.jsonObject }.associateBy { it.getValue("itemId").jsonPrimitive.content }
            assertEquals(expectedStatus, review.getValue(firstId).getValue("status").jsonPrimitive.content)
            assertEquals(expectedStatus, review.getValue(secondId).getValue("status").jsonPrimitive.content)
        }
    }

    @Test
    fun c1NovemberPublishedRubricAppearsOnlyAfterNativeSubmit() {
        val targetId = "certyfikat-polski-C1-2022-11-g2-q1"
        val session = IosExamSession()
        session.selectExam("c1-2022-11")
        session.selectModule("grammar")
        repeat(10) { session.next() }
        val before = session.view().getValue("grammar").jsonObject
        assertEquals(targetId, before.getValue("itemId").jsonPrimitive.content)
        assertFalse(before.containsKey("rubricComponents"))
        session.answer(targetId, "Agencje turystyczne odnotowują spadek zainteresowania klientów.")
        while (!session.view().getValue("grammar").jsonObject.getValue("isLast").jsonPrimitive.content.toBoolean()) {
            session.next()
        }
        session.submit()
        val review = session.view().getValue("grammar").jsonObject.getValue("review").jsonArray
            .map { it.jsonObject }.single { it.getValue("itemId").jsonPrimitive.content == targetId }
        val rubric = review.getValue("rubricComponents").jsonArray.map { it.jsonObject }
        assertEquals((1..4).map { "$targetId-c$it" }, rubric.map { it.getValue("componentId").jsonPrimitive.content })
        assertEquals(listOf("odnotowują", "spadek", "zainteresowania", "klientów"),
            rubric.map { it.getValue("criterionText").jsonPrimitive.content })
        assertEquals(listOf("0,25", "0,25", "0,25", "0,25"),
            rubric.map { it.getValue("creditPoints").jsonPrimitive.content })
    }

    @Test
    fun unknownOpenAnswerIsPendingAndSnapshotReadsKeepDraft() {
        val session = IosExamSession()
        session.selectExam("b1-2019")
        session.selectModule("grammar")
        val open = polski.exam.officialGrammarData.getValue("b1-2019").flatMap { it.items }
            .first { it.gradingMode == polski.exam.ExamGradingMode.OpenManual }
        while (session.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content != open.itemId) session.next()
        session.answer(open.itemId, "własna poprawna odpowiedź")
        assertEquals("grammar", session.view().getValue("screen").jsonPrimitive.content)
        assertEquals("własna poprawna odpowiedź", session.view().getValue("grammar").jsonObject.getValue("answer").jsonPrimitive.content)
        assertEquals("własna poprawna odpowiedź", session.view().getValue("grammar").jsonObject.getValue("answer").jsonPrimitive.content)
        session.submit()
        val result = session.view().getValue("grammar").jsonObject
        val expected = GrammarExamScorer.grade(open, "własna poprawna odpowiedź")
        assertFalse(result.getValue("final").jsonPrimitive.content.toBoolean())
        assertEquals(JsonNull, result.getValue("percentage"))
        assertEquals(expected.pendingQuarter.toString(), result.getValue("manualPendingQuarter").jsonPrimitive.content)
        val review = result.getValue("review").jsonArray.single { it.jsonObject.getValue("itemId").jsonPrimitive.content == open.itemId }.jsonObject
        assertEquals(expected.status.name, review.getValue("status").jsonPrimitive.content)
    }

    @Test
    fun closeAndNewExamReleaseOldDraftWithoutMutatingOtherPaper() {
        val session = IosExamSession()
        session.selectExam("b1-2017")
        session.selectModule("grammar")
        session.answer("B1-2017-g1-1", "dobrego teatru")
        session.selectExam("b2-2020")
        session.selectModule("grammar")
        assertEquals("", session.view().getValue("grammar").jsonObject.getValue("answer").jsonPrimitive.content)
        session.close()
        assertEquals("catalog", session.view().getValue("screen").jsonPrimitive.content)
        assertEquals(JsonNull, session.view().getValue("selectedExamId"))
    }

    @Test
    fun conditionalWordBankVariantUsesWholeAttemptAndKeepsSelectedTokenId() {
        val first = IosExamSession()
        first.setImmediateFeedbackEnabled(false) // Legacy whole-attempt fixture answers a later item directly.
        first.selectExam("b2-2020")
        first.selectModule("grammar")
        val atSix = "certyfikat-polski-B2-2020-grammar-2-6"
        val atEight = "certyfikat-polski-B2-2020-grammar-2-8"
        val wToken = polski.exam.data.b2y2020.GRAMMAR_BANK_TOKEN_ROW_ROWS.single {
            it.testRef == "certyfikat-polski-B2-2020-grammar-2" && it.text == "w"
        }
        while (first.view().getValue("grammar").jsonObject.getValue("itemId").jsonPrimitive.content != atSix) first.next()
        first.answerBankToken(atSix, wToken.tokenId)
        assertEquals(wToken.tokenId, first.view().getValue("grammar").jsonObject.getValue("selectedBankTokenId").jsonPrimitive.content)
        first.answer(atEight, "w")
        first.submit()
        fun status(session: IosExamSession) = session.view().getValue("grammar").jsonObject.getValue("review")
            .jsonArray.single { it.jsonObject.getValue("itemId").jsonPrimitive.content == atEight }
            .jsonObject.getValue("status").jsonPrimitive.content
        val sourceTest = polski.exam.officialGrammarData.getValue("b2-2020")
            .single { it.items.any { item -> item.itemId == atEight } }
        val item = sourceTest.items.single { it.itemId == atEight }
        assertEquals(GrammarExamScorer.gradeInAttempt(sourceTest, item, mapOf(atSix to "w", atEight to "w")).status.name,
            status(first))

        val second = IosExamSession()
        second.setImmediateFeedbackEnabled(false)
        second.selectExam("b2-2020")
        second.selectModule("grammar")
        second.answer(atEight, "w")
        second.submit()
        assertEquals(GrammarExamScorer.gradeInAttempt(sourceTest, item, mapOf(atEight to "w")).status.name,
            status(second))
    }

    @Test
    fun authoredAnswersProduceTheSameReviewAsSharedScorer() {
        fun reviewed(routeId: String, itemId: String, answer: String): kotlinx.serialization.json.JsonObject {
            val session = IosExamSession()
            session.setImmediateFeedbackEnabled(false) // Source scorer fixture is intentionally noninteractive.
            session.selectExam(routeId)
            session.selectModule("grammar")
            session.answer(itemId, answer)
            session.submit()
            return session.view().getValue("grammar").jsonObject.getValue("review").jsonArray
                .single { it.jsonObject.getValue("itemId").jsonPrimitive.content == itemId }.jsonObject
        }
        val attempts = listOf(
            Triple("b1-2017", "B1-2017-g4-2", "z kim"),
            Triple("b2-2019", "certyfikat-polski-B2-2019-grammar-6-9", "będą stosować"),
            Triple("b1-2017", "B1-2017-g1-1", "zła odpowiedź"),
            Triple("b1-2017", "B1-2017-g1-1", ""),
        )
        for ((routeId, itemId, answer) in attempts) {
            val item = polski.exam.officialGrammarData.getValue(routeId).flatMap { it.items }
                .single { it.itemId == itemId }
            val expected = GrammarExamScorer.grade(item, answer)
            val actual = reviewed(routeId, itemId, answer)
            assertEquals(expected.status.name, actual.getValue("status").jsonPrimitive.content, "$routeId/$itemId")
            assertEquals(expected.confirmedQuarter.toString(), actual.getValue("confirmedQuarter").jsonPrimitive.content)
            assertEquals(expected.pendingQuarter.toString(), actual.getValue("pendingQuarter").jsonPrimitive.content)
            assertEquals(item.maximumQuarter.toString(), actual.getValue("maximumQuarter").jsonPrimitive.content)
        }
    }
}
