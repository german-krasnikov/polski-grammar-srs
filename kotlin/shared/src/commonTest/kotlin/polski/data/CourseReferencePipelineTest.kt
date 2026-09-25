package polski.data

import kotlin.test.Test
import kotlin.test.assertEquals

class CourseReferencePipelineTest {
    @Test
    fun authoredPipelinePreservesOrderedReactStepsAndCompactNativeCopy() {
        assertEquals("Сначала конструкция, затем формы", referencePipeline.title)
        assertEquals(
            listOf(
                ReferencePipelineStep("intent", "01 · Смысл", "Что хочу сказать?", "Вижу / не вижу / говорю о…"),
                ReferencePipelineStep("case", "02 · Операция", "Какой падеж нужен?", "widzę → Biernik"),
                ReferencePipelineStep("agreement", "03 · Согласование", "Меняю всю группу", "moją + piękną + żonę"),
            ),
            referencePipeline.steps,
        )
        assertEquals("Что хочу сказать? → Какой падеж нужен? → Меняю всю группу", referencePipeline.compactSummary)
        assertEquals("widzę → Biernik → moją + piękną + żonę", referencePipeline.compactExample)
    }
}
