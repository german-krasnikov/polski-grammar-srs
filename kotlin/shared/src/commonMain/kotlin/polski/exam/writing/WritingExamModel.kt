package polski.exam.writing

data class WritingTask(
    val id: String, val instruction: String, val promptText: String, val wordLimit: Int?,
    val setId: String? = null, val genre: String = "letter", val promptImageUrl: String? = null,
    val promptImageAlt: String? = null,
)
data class WritingExam(val tasks: List<WritingTask>, val maximumQuarter: Int? = null) {
    init {
        require(tasks.map { it.id }.distinct().size == tasks.size)
        require(maximumQuarter == null || maximumQuarter > 0)
    }
    fun tasksForSet(setId: String?): List<WritingTask> = if (setId == null) tasks.filter { it.setId == null } else tasks.filter { it.setId == setId }
    val sets: List<String> get() = tasks.mapNotNull { it.setId }.distinct()
}
