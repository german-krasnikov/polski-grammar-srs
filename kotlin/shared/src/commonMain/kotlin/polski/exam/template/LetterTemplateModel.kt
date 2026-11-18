package polski.exam.template

/** Stateless query model over a pack's letter templates and task links. No IO, no coroutines. */
class LetterTemplateModel(
    private val templates: List<LetterTemplate>,
    private val links: List<LetterTaskLink>,
) {
    /** Templates explicitly linked to this writing task. Empty if none authored. */
    fun forTask(taskId: String): List<LetterTemplate> {
        val ids = links.filter { it.taskRef == taskId }.map { it.templateRef }
        return templates.filter { it.id in ids }
    }

    fun all(): List<LetterTemplate> = templates
}
