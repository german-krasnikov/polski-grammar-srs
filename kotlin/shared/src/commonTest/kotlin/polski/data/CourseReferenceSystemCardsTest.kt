package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

class CourseReferenceSystemCardsTest {
    @Test
    fun authoredSystemCardsKeepTheFourExistingExamplesInOrder() {
        assertEquals(
            listOf(
                ReferenceSystemCard("noun", "Существительное", "Род × число × падеж", "żona → żonę → żony",
                    listOf("żona", "żonę", "żony")),
                ReferenceSystemCard("agreement", "Прилагательное и владелец", "Копируют род, число и падеж", "moja piękna → moją piękną",
                    listOf("moja piękna", "moją piękną")),
                ReferenceSystemCard("verb", "Глагол", "Лицо × число × время × вид", "widzę → widziałem → będę widzieć",
                    listOf("widzę", "widziałem", "będę widzieć")),
                ReferenceSystemCard("modifiers", "Модификаторы", "Отрицание · вопрос · владелец", "Widzę… → Nie widzę… → Czy widzę…?",
                    listOf("Widzę…", "Nie widzę…", "Czy widzę…?")),
            ),
            referenceSystemCards,
        )
    }
}
