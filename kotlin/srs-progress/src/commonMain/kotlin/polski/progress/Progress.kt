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
    /** SB-8: sentence-builder scaffolding rung for this skill — 0=Full (most help), 1=Partial,
     *  2=None (least help). A plain Int, not core-model's `ScaffoldingLevel`: `:srs-progress` has
     *  no dependency on `:core-model`. Independent of [streak], which mixes every `AnswerMode` —
     *  this rung advances only from TileBuilder-mode reviews (`ReviewReducer.recordReview`'s
     *  `tileCorrect` param). Default 0: a never-drilled-via-tiles skill, and every pre-SB-8 stored
     *  document (decoded with this field absent), starts at Full. */
    val tileScaffoldingRung: Int = 0,
    /** Consecutive correct TileBuilder-mode reviews at the CURRENT [tileScaffoldingRung] only —
     *  reset to 0 on a wrong Tile review or whenever the rung itself changes (promote/demote).
     *  Default 0, same compatibility reasoning as [tileScaffoldingRung]. */
    val tileScaffoldingStreak: Int = 0,
)

/** Retains the parsed legacy tree so unknown JSON properties survive review and export. */
data class ProgressDocument(
    val progress: Progress,
    val source: JsonObject = JsonObject(emptyMap()),
)
