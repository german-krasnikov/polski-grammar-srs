package polski.ui

import polski.exam.QuestionType

private val b1_2019Listening = ListeningExam(
    tasks = listOf(
        ListeningTask("l1", "Zadanie 1. Proszę wysłuchać tekstów i zdecydować, czy zdania są prawdziwe (P) czy fałszywe (F).", listOf(
            ListeningQ("lq1-1", QuestionType.TrueFalse, "Pani Kowalska pracuje w szkole.", emptyList(), "FAŁSZ"),
            ListeningQ("lq1-2", QuestionType.TrueFalse, "Pan Nowak zaprasza na obiad.", emptyList(), "PRAWDA"),
            ListeningQ("lq1-3", QuestionType.TrueFalse, "Sklep jest otwarty w niedzielę.", emptyList(), "FAŁSZ"),
            ListeningQ("lq1-4", QuestionType.TrueFalse, "Pociąg do Krakowa odjeżdża o 10:15.", emptyList(), "PRAWDA"),
            ListeningQ("lq1-5", QuestionType.TrueFalse, "Pogoda jutro będzie słoneczna.", emptyList(), "FAŁSZ"),
        )),
        ListeningTask("l2", "Zadanie 2. Proszę wysłuchać dialogów i wybrać właściwą odpowiedź.", listOf(
            ListeningQ("lq2-1", QuestionType.MultipleChoice, "Gdzie odbywa się rozmowa?", listOf("A. w restauracji", "B. w szpitalu", "C. w sklepie"), "A. w restauracji"),
            ListeningQ("lq2-2", QuestionType.MultipleChoice, "Co zamawia klient?", listOf("A. kawę", "B. herbatę", "C. sok"), "B. herbatę"),
            ListeningQ("lq2-3", QuestionType.MultipleChoice, "O której godzinie jest spotkanie?", listOf("A. o 14:00", "B. o 15:30", "C. o 16:00"), "B. o 15:30"),
            ListeningQ("lq2-4", QuestionType.MultipleChoice, "Ile kosztuje bilet?", listOf("A. 20 zł", "B. 35 zł", "C. 50 zł"), "C. 50 zł"),
        )),
        ListeningTask("l3", "Zadanie 3. Proszę wysłuchać tekstu i odpowiedzieć na pytania.", listOf(
            ListeningQ("lq3-1", QuestionType.MultipleChoice, "O czym jest audycja?", listOf("A. o sporcie", "B. o podróżach", "C. o zdrowiu"), "C. o zdrowiu"),
            ListeningQ("lq3-2", QuestionType.MultipleChoice, "Ile godzin dziennie powinniśmy ćwiczyć?", listOf("A. 30 minut", "B. 1 godzinę", "C. 2 godziny"), "A. 30 minut"),
            ListeningQ("lq3-3", QuestionType.TrueFalse, "Ekspert zaleca dietę wegetariańską.", emptyList(), "FAŁSZ"),
        )),
        ListeningTask("l4", "Zadanie 4. Proszę wysłuchać i uzupełnić luki.", listOf(
            ListeningQ("lq4-1", QuestionType.FillGap, "Numer telefonu to: 22 ___", emptyList(), "456-78-90"),
            ListeningQ("lq4-2", QuestionType.FillGap, "Spotkanie jest w ___ o godzinie 18.", emptyList(), "piątek"),
            ListeningQ("lq4-3", QuestionType.FillGap, "Proszę przynieść ___ i długopis.", emptyList(), "zeszyt"),
        )),
    ),
    audioUrl = "exam-audio/B1-2019.mp3",
)

private val b2_2019Listening = ListeningExam(
    tasks = listOf(
        ListeningTask("b2l1", "Zadanie 1. Proszę wysłuchać pięciu krótkich tekstów i zdecydować: PRAWDA czy FAŁSZ.", listOf(
            ListeningQ("b2lq1-1", QuestionType.TrueFalse, "Wykład jest o historii architektury.", emptyList(), "PRAWDA"),
            ListeningQ("b2lq1-2", QuestionType.TrueFalse, "Prelegent pracuje na uniwersytecie.", emptyList(), "PRAWDA"),
            ListeningQ("b2lq1-3", QuestionType.TrueFalse, "Wystawa jest czynna do końca miesiąca.", emptyList(), "FAŁSZ"),
            ListeningQ("b2lq1-4", QuestionType.TrueFalse, "Koncert odbędzie się w filharmonii.", emptyList(), "PRAWDA"),
            ListeningQ("b2lq1-5", QuestionType.TrueFalse, "Bilety na spektakl są już wyprzedane.", emptyList(), "FAŁSZ"),
        )),
        ListeningTask("b2l2", "Zadanie 2. Proszę wysłuchać rozmowy i wybrać właściwą odpowiedź.", listOf(
            ListeningQ("b2lq2-1", QuestionType.MultipleChoice, "Jaki jest temat rozmowy?", listOf("A. praca", "B. podróże", "C. edukacja"), "C. edukacja"),
            ListeningQ("b2lq2-2", QuestionType.MultipleChoice, "Co mówca uważa za najważniejsze?", listOf("A. doświadczenie", "B. wykształcenie", "C. znajomości"), "A. doświadczenie"),
            ListeningQ("b2lq2-3", QuestionType.MultipleChoice, "Ile lat trwają studia?", listOf("A. 3 lata", "B. 4 lata", "C. 5 lat"), "C. 5 lat"),
        )),
        ListeningTask("b2l3", "Zadanie 3. Proszę wysłuchać audycji radiowej i odpowiedzieć na pytania.", listOf(
            ListeningQ("b2lq3-1", QuestionType.MultipleChoice, "O jakiej branży mowa?", listOf("A. IT", "B. medycyna", "C. gastronomia"), "C. gastronomia"),
            ListeningQ("b2lq3-2", QuestionType.TrueFalse, "Polska kuchnia zyskuje na popularności za granicą.", emptyList(), "PRAWDA"),
            ListeningQ("b2lq3-3", QuestionType.TrueFalse, "Wszystkie polskie restauracje mają gwiazdki Michelin.", emptyList(), "FAŁSZ"),
            ListeningQ("b2lq3-4", QuestionType.MultipleChoice, "Co jest kluczowe dla sukcesu?", listOf("A. lokalizacja", "B. jakość produktów", "C. reklama"), "B. jakość produktów"),
        )),
    ),
    audioUrl = "exam-audio/B2-2019.mp3",
)

internal val examListeningData: Map<String, ListeningExam> = mapOf(
    "b1-2019" to b1_2019Listening,
    "b2-2019" to b2_2019Listening,
)
