package polski.ui

import polski.exam.QuestionType

private val b1_2019Reading = ReadingExam(
    passages = listOf(
        ReadingPassage("r1", "Proszę przeczytać fragmenty tekstów i zaznaczyć właściwą odpowiedź.", ""),
        ReadingPassage("r2", "Proszę przeczytać tekst i zaznaczyć: TAK lub NIE.",
            "Według przepisów pasażer ma prawo do zwrotu pieniędzy, jeżeli rejs został odwołany na krócej niż 14 dni przed wylotem lub jest opóźniony o ponad 3 godziny. Linia lotnicza musi zapewnić pasażerom jedzenie i napoje, a w razie potrzeby również nocleg w hotelu. Pasażer może także zrezygnować z lotu i poprosić o zwrot pełnej ceny biletu."),
        ReadingPassage("r3", "Proszę przeczytać tekst i dopasować usunięte fragmenty (A-I).",
            "Słynny na cały świat Jarmark Dominikański w Gdańsku ma bardzo długą tradycję. Pierwszy Jarmark odbył się w 1260 roku i od tego czasu co roku przyciąga tysiące kupców i turystów. Na Jarmarku można kupić rękodzieło, biżuterię, ubrania, a także spróbować regionalnych potraw."),
        ReadingPassage("r4", "Proszę połączyć tytuły z kolumny I z fragmentami tekstów z kolumny II.", ""),
        ReadingPassage("r5", "Proszę uzupełnić tekst wyrazami z ramki.",
            "Akcja 'Majewskich posadzili' jest częścią kampanii, którą prowadzi fundacja 'Zmiana'. Celem akcji jest zachęcenie mieszkańców do sadzenia drzew w swoich ogrodach i na osiedlach. Organizatorzy chcą, aby każda polska rodzina posadzilla przynajmniej jedno drzewo."),
    ),
    questions = listOf(
        ReadingQuestion("rq1-1", "r1", QuestionType.MultipleChoice, "Pytanie 1.1", listOf("A", "B", "C"), "B"),
        ReadingQuestion("rq1-2", "r1", QuestionType.MultipleChoice, "Pytanie 1.2", listOf("A", "B", "C"), "A"),
        ReadingQuestion("rq1-3", "r1", QuestionType.MultipleChoice, "Pytanie 1.3", listOf("A", "B", "C"), "B"),
        ReadingQuestion("rq1-4", "r1", QuestionType.MultipleChoice, "Pytanie 1.4", listOf("A", "B", "C"), "C"),
        ReadingQuestion("rq2-1", "r2", QuestionType.TrueFalse, "Pasażer ma prawo do zwrotu pieniędzy za odwołany lot.", listOf("TAK", "NIE"), "TAK"),
        ReadingQuestion("rq2-2", "r2", QuestionType.TrueFalse, "Linia lotnicza musi zapewnić jedzenie i nocleg.", listOf("TAK", "NIE"), "TAK"),
        ReadingQuestion("rq2-3", "r2", QuestionType.TrueFalse, "Pasażer nie może zrezygnować z lotu.", listOf("TAK", "NIE"), "NIE"),
        ReadingQuestion("rq2-4", "r2", QuestionType.TrueFalse, "Zwrot przysługuje tylko przy odwołaniu lotu na 7 dni przed.", listOf("TAK", "NIE"), "NIE"),
        ReadingQuestion("rq2-5", "r2", QuestionType.TrueFalse, "Opóźnienie ponad 3 godziny uprawnia do odszkodowania.", listOf("TAK", "NIE"), "TAK"),
        ReadingQuestion("rq3-1", "r3", QuestionType.MultipleChoice, "Kiedy odbył się pierwszy Jarmark?", listOf("A. 1160", "B. 1260", "C. 1360"), "B. 1260"),
        ReadingQuestion("rq3-2", "r3", QuestionType.MultipleChoice, "Co można kupić na Jarmarku?", listOf("A. tylko jedzenie", "B. rękodzieło i biżuterię", "C. elektronikę"), "B. rękodzieło i biżuterię"),
        ReadingQuestion("rq5-1", "r5", QuestionType.FillGap, "Celem akcji jest zachęcenie ___ do sadzenia drzew.", listOf("mieszkańców", "turystów", "dzieci"), "mieszkańców"),
        ReadingQuestion("rq5-2", "r5", QuestionType.FillGap, "Organizatorzy chcą, aby każda ___ posadzilla drzewo.", listOf("szkoła", "rodzina", "firma"), "rodzina"),
    ),
)

/** Exam id → reading data. Later replaced by biome-generated rows. */
internal val examReadingData: Map<String, ReadingExam> = mapOf(
    "b1-2019" to b1_2019Reading,
)
