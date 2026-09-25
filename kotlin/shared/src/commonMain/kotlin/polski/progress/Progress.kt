package polski.progress

import kotlinx.serialization.json.JsonObject
import polski.srs.StoredCard

/** V1 progress values. Lists and maps are copied when a document is created or updated. */
data class Progress(
    val version: Int = 1,
    val cards: List<StoredCard>,
    val stats: Map<String, SkillStats>,
    val reviewsToday: Int = 0,
    val lastDay: String,
    val totalReviews: Int = 0,
)

data class SkillStats(
    val reviews: Int = 0,
    val correct: Int = 0,
    val streak: Int = 0,
    val mistakes: Int = 0,
)

/** Retains the parsed legacy tree so unknown JSON properties survive review and export. */
data class ProgressDocument(
    val progress: Progress,
    val source: JsonObject = JsonObject(emptyMap()),
)
