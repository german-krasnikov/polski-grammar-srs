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
import polski.exam.grammar.GrammarExamScorer

class IosExamSessionTest {
    private fun IosExamSession.view() = Json.parseToJsonElement(currentSnapshot()).jsonObject

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
        assertEquals(6, catalog.getValue("catalog").jsonArray.size)
        assertEquals("B1 Certyfikat 2017", catalog.getValue("catalog").jsonArray.first().jsonObject
            .getValue("displayTitle").jsonPrimitive.content)
        session.selectExam("b1-2017")
        val modules = session.view().getValue("modules").jsonArray
        assertEquals(listOf("listening", "reading", "grammar", "writing"), modules.map { it.jsonObject.getValue("module").jsonPrimitive.content })
        assertEquals(listOf(false, false, true, false), modules.map { it.jsonObject.getValue("availableOnIpad").jsonPrimitive.content.toBoolean() })
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
