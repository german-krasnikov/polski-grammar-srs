package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CourseChainPresentationTest {
    @Test fun trainingStepsKeepTheirOwnOrderAndLabels() {
        val presentation = courseChainPresentation
        assertEquals(
            listOf(
                ChainStep("acc", "Вижу"),
                ChainStep("past", "Прошлое"),
                ChainStep("neg", "Отрицание"),
                ChainStep("owner", "Владелец"),
                ChainStep("loc", "Говорю о"),
            ),
            presentation.steps,
        )
        assertEquals("Вижу → Прошлое → Отрицание → Владелец → Говорю о", presentation.summary)
        assertNotEquals(referenceChainRows.map { it.label }, presentation.steps.map { it.label })
    }

    @Test fun hostCompletionVariantsRemainDistinct() {
        assertEquals(
            ChainCompletion(
                "Цепочка завершена",
                "5 преобразований",
                "Ты изменил время, отрицание, владельца и падеж, сохранив одну мысль. Оценки сохранены в расписании повторений.",
                "Пять преобразований завершены. Оценки сохранены в расписании повторений.",
            ),
            courseChainPresentation.completion,
        )
    }
}
