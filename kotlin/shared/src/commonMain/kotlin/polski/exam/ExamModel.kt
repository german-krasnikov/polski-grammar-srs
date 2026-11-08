package polski.exam

/** A unique exam sitting: provider + level + year. */
data class ExamId(val provider: String, val targetLanguage: String, val level: String, val year: Int?)

/** The four modules of a language proficiency exam. */
enum class ExamModule { Listening, Reading, Grammar, Writing }

/** Question types shared across modules — each module uses a subset. */
enum class QuestionType {
    MultipleChoice,
    TrueFalse,
    TrueFalseNotGiven,
    FillGap,
    Match,
    MatchHeading,
    ShortAnswer,
    ChooseForm,
    Transform,
    WordFormation,
}

/** Writing genre — extensible via the DSL enum, kept here for domain layer. */
enum class WritingGenre { Letter, Description, Argument, Essay }

/** An audio segment within a single exam audio file (milliseconds). */
data class AudioSegment(val url: String, val startMs: Int, val endMs: Int)
