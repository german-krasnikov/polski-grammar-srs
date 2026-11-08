package polski.exam

/** A unique exam sitting: provider + level + year. */
data class ExamId(val provider: String, val targetLanguage: String, val level: String, val year: Int?)

/** The four modules of a language proficiency exam. */
enum class ExamModule { Listening, Reading, Grammar, Writing }

/** Question types shared across modules — each module uses a subset. */
enum class QuestionType(val tag: String) {
    MultipleChoice("multiple_choice"),
    TrueFalse("true_false"),
    TrueFalseNotGiven("true_false_not_given"),
    FillGap("fill_gap"),
    Match("match"),
    MatchHeading("match_heading"),
    ShortAnswer("short_answer"),
    ChooseForm("choose_form"),
    Transform("transform"),
    WordFormation("word_formation"),
    WordBank("word_bank"),
    QuestionFormation("question_formation"),
    GapFillFragment("gap_fill_fragment"),
    Matching("matching");
    companion object {
        fun fromTag(tag: String): QuestionType = entries.first { it.tag == tag }
    }
}

/** Writing genre — extensible via the DSL enum, kept here for domain layer. */
enum class WritingGenre { Letter, Description, Argument, Essay, Speech, Characterization }

/** An audio segment within a single exam audio file (milliseconds). */
data class AudioSegment(val url: String, val startMs: Int, val endMs: Int)
