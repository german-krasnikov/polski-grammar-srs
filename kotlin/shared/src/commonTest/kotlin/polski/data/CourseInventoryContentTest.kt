package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

class CourseInventoryContentTest {
    @Test
    fun remainingGuidanceKeepsHostCopy() {
        assertEquals("Один небольшой словарь. Видно, что меняется при каждой операции.", courseMatrixIntroduction)
        assertEquals("Местоимение + прилагательное + существительное", courseWebCaseCompositionHeader)
        assertEquals("Красным показана старая часть, золотым — новая. Местоимение и прилагательное согласуются с существительным.", courseContextHelp.react)
        assertEquals("Местоимение и прилагательное согласуются с существительным. jego, jej, ich не изменяются.", courseContextHelp.compact)
        assertEquals("Для единственного числа сначала определи тип существительного.", courseMaleAccIntro)
        assertEquals("Нет настоящего времени", courseAspectNoPresent.compact)
        assertEquals("Настоящее: нет настоящего времени", courseAspectNoPresent.ios)
        val reviewNotice = " Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR."
        assertEquals("Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера пока не добавляются в тренировки." + reviewNotice, courseVocabularyInstructions.web)
        assertEquals("Частотный ранг не равен уровню CEFR. Без проверенного перевода и примера слово пока недоступно." + reviewNotice, courseVocabularyInstructions.native)
        assertEquals("Частотный ранг не равен уровню CEFR. Непроверенные слова пока недоступны." + reviewNotice, courseVocabularyInstructions.ios)
        assertEquals("Перевод и пример ещё не проверены", courseVocabularyUnavailableLabel)
    }
}
