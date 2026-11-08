package polski.ui

import polski.exam.QuestionType

private val b1_2019Listening = ListeningExam(
    tasks = listOf(
        ListeningTask("b1-l1",
            "Zadanie 1. Proszę uważnie słuchać nagrania. Uwaga! Nagranie zostanie odtworzone tylko jeden raz. To zdanie może wypowiedzieć kto / gdzie?",
            listOf(
                ListeningQ("b1-lq1-1", QuestionType.MultipleChoice, "1. Ta wypowiedź jest typowa", listOf("a) w kinie", "b) w bibliotece", "c) w księgarni"), "b) w bibliotece"),
                ListeningQ("b1-lq1-2", QuestionType.MultipleChoice, "2. To zdanie mógł powiedzieć", listOf("a) lekarz", "b) nauczyciel", "c) fryzjer"), "c) fryzjer"),
                ListeningQ("b1-lq1-3", QuestionType.MultipleChoice, "3. Ten tekst usłyszymy", listOf("a) w biurze", "b) na poczcie", "c) w sklepie"), "c) w sklepie"),
                ListeningQ("b1-lq1-4", QuestionType.MultipleChoice, "4. To zdanie mógł powiedzieć", listOf("a) fryzjer", "b) kelner", "c) mechanik"), "a) fryzjer"),
                ListeningQ("b1-lq1-5", QuestionType.MultipleChoice, "5. Kto to powiedział?", listOf("a) mama", "b) koleżanka", "c) szefowa"), "b) koleżanka"),
                ListeningQ("b1-lq1-6", QuestionType.MultipleChoice, "6. Ten tekst usłyszymy", listOf("a) w kwiaciarni", "b) w aptece", "c) w piekarni"), "a) w kwiaciarni"),
                ListeningQ("b1-lq1-7", QuestionType.MultipleChoice, "7. To zdanie mógł powiedzieć", listOf("a) pracownik banku", "b) sprzedawca", "c) listonosz"), "a) pracownik banku"),
                ListeningQ("b1-lq1-8", QuestionType.MultipleChoice, "8. Ta wypowiedź to fragment", listOf("a) diagnozy lekarza", "b) prognozy pogody", "c) rozmowy w sklepie"), "b) prognozy pogody"),
                ListeningQ("b1-lq1-9", QuestionType.MultipleChoice, "9. To zdanie usłyszymy na", listOf("a) imieninach", "b) urodzinach", "c) ślubie"), "c) ślubie"),
                ListeningQ("b1-lq1-10", QuestionType.MultipleChoice, "10. Kto to powiedział?", listOf("a) trener", "b) kibic", "c) sędzia"), "b) kibic"),
                ListeningQ("b1-lq1-11", QuestionType.MultipleChoice, "11. Ta wypowiedź jest typowa", listOf("a) w muzeum", "b) w kinie", "c) w teatrze"), "a) w muzeum"),
                ListeningQ("b1-lq1-12", QuestionType.MultipleChoice, "12. To zdanie mógł powiedzieć", listOf("a) taksówkarz", "b) pasażer", "c) policjant"), "a) taksówkarz"),
                ListeningQ("b1-lq1-13", QuestionType.MultipleChoice, "13. To pytanie usłyszymy", listOf("a) w aptece", "b) w sklepie odzieżowym", "c) na poczcie"), "b) w sklepie odzieżowym"),
                ListeningQ("b1-lq1-14", QuestionType.MultipleChoice, "14. To zdanie usłyszymy", listOf("a) w radio", "b) w telewizji", "c) na komendzie policji"), "a) w radio"),
            ),
            startMs = 85000, endMs = 305000
        ),
        ListeningTask("b1-l2",
            "Zadanie 2. Dziennikarz rozmawia na temat akcji 'Torby bumerangi'. TAK/NIE. Uwaga! Nagranie zostanie odtworzone dwa razy.",
            listOf(
                ListeningQ("b1-lq2-1", QuestionType.TrueFalse, "1. Do tej pory niezbyt dużo sklepów bierze udział w akcji.", emptyList(), "NIE"),
                ListeningQ("b1-lq2-2", QuestionType.TrueFalse, "2. Akcja 'Torby bumerangi' ma pomóc dbać o środowisko.", emptyList(), "TAK"),
                ListeningQ("b1-lq2-3", QuestionType.TrueFalse, "3. Żeby wziąć udział w akcji, trzeba kupić specjalny koszyk.", emptyList(), "NIE"),
                ListeningQ("b1-lq2-4", QuestionType.TrueFalse, "4. Pomysł na akcję powstał w Australii.", emptyList(), "TAK"),
                ListeningQ("b1-lq2-5", QuestionType.TrueFalse, "5. Tylko sklepy ekologiczne mogą uczestniczyć w akcji.", emptyList(), "NIE"),
                ListeningQ("b1-lq2-6", QuestionType.TrueFalse, "6. Organizatorzy bali się, że klienci nie będą oddawać toreb.", emptyList(), "TAK"),
                ListeningQ("b1-lq2-7", QuestionType.TrueFalse, "7. Sklepy same zgłaszają się do udziału w akcji.", emptyList(), "TAK"),
                ListeningQ("b1-lq2-8", QuestionType.TrueFalse, "8. Klienci niechętnie oddają torby.", emptyList(), "NIE"),
            ),
            startMs = 310000, endMs = 544000
        ),
        ListeningTask("b1-l3",
            "Zadanie 3. Proszę wysłuchać informacji na temat nowego systemu informowania o zagrożeniach i zaznaczyć właściwe odpowiedzi. Uwaga! Nagranie zostanie odtworzone dwa razy.",
            listOf(
                ListeningQ("b1-lq3-1", QuestionType.MultipleChoice, "1. Wiadomości alarmowe", listOf("a) przesyła policja i straż pożarna", "b) powstają w Centrum Bezpieczeństwa", "c) przygotowują operatorzy komórkowi"), "b) powstają w Centrum Bezpieczeństwa"),
                ListeningQ("b1-lq3-2", QuestionType.MultipleChoice, "2. Żeby otrzymać SMSa alarmowego, trzeba", listOf("a) zapisać się do bazy systemu", "b) być w miejscu zagrożenia", "c) mieć specjalną aplikację"), "b) być w miejscu zagrożenia"),
                ListeningQ("b1-lq3-3", QuestionType.MultipleChoice, "3. System ostrzega przed", listOf("a) przestępstwami", "b) niebezpiecznymi zjawiskami pogodowymi", "c) wypadkami drogowymi"), "b) niebezpiecznymi zjawiskami pogodowymi"),
                ListeningQ("b1-lq3-4", QuestionType.MultipleChoice, "4. Po otrzymaniu wiadomości alarmowej należy", listOf("a) zadzwonić na numer alarmowy", "b) uciekać z miejsca zagrożenia", "c) postępować zgodnie z instrukcją"), "c) postępować zgodnie z instrukcją"),
                ListeningQ("b1-lq3-5", QuestionType.MultipleChoice, "5. Uwagi na temat systemu można zgłaszać", listOf("a) telefonicznie", "b) mailowo", "c) na stronie internetowej"), "b) mailowo"),
            ),
            startMs = 547000, endMs = 787000
        ),
        ListeningTask("b1-l4",
            "Zadanie 4. Co jest ważne w rozmowach biznesowych? Proszę dopasować osoby (1-4) do ich rad (A-E). Uwaga! Nagranie zostanie odtworzone dwa razy.",
            listOf(
                ListeningQ("b1-lq4-A", QuestionType.MultipleChoice, "A. mówi, że bardzo ważne jest poznanie partnera przed spotkaniem.", listOf("1", "2", "3", "4"), "2"),
                ListeningQ("b1-lq4-B", QuestionType.MultipleChoice, "B. radzi, żeby być uprzejmym i uśmiechniętym.", listOf("1", "2", "3", "4"), "3"),
                ListeningQ("b1-lq4-C", QuestionType.MultipleChoice, "C. mówi, że dobrze jest pozwolić mówić partnerowi.", listOf("1", "2", "3", "4"), "4"),
                ListeningQ("b1-lq4-D", QuestionType.MultipleChoice, "D. radzi, by wcześniej określić cel spotkania i nasze oczekiwania.", listOf("1", "2", "3", "4"), "1"),
                ListeningQ("b1-lq4-E", QuestionType.MultipleChoice, "E. mówi, że milczenie może być ważniejsze niż słowa.", listOf("1", "2", "3", "4"), "4"),
            ),
            startMs = 790000, endMs = 1061000
        ),
        ListeningTask("b1-l5",
            "Zadanie 5. Sposoby organizacji przeprowadzki. Proszę dopasować numer tekstu do ilustracji. Uwaga! Nagranie zostanie odtworzone dwa razy.",
            listOf(
                ListeningQ("b1-lq5-B", QuestionType.FillGap, "B", emptyList(), "?"),
                ListeningQ("b1-lq5-C", QuestionType.FillGap, "C", emptyList(), "?"),
                ListeningQ("b1-lq5-D", QuestionType.FillGap, "D", emptyList(), "?"),
                ListeningQ("b1-lq5-E", QuestionType.FillGap, "E", emptyList(), "?"),
                ListeningQ("b1-lq5-F", QuestionType.FillGap, "F", emptyList(), "?"),
                ListeningQ("b1-lq5-G", QuestionType.FillGap, "G", emptyList(), "?"),
                ListeningQ("b1-lq5-H", QuestionType.FillGap, "H", emptyList(), "?"),
                ListeningQ("b1-lq5-I", QuestionType.FillGap, "I", emptyList(), "?"),
                ListeningQ("b1-lq5-J", QuestionType.FillGap, "J", emptyList(), "?"),
                ListeningQ("b1-lq5-K", QuestionType.FillGap, "K", emptyList(), "?"),
            ),
            startMs = 1066000, endMs = 1472000
        ),
    ),
    audioUrl = "exam-audio/B1-2019.mp3",
)

private val b2_2019Listening = ListeningExam(
    tasks = listOf(
        ListeningTask("b2-l1",
            "Zadanie 1. Proszę uważnie słuchać nagrania i wykonywać zadanie. Uwaga! Nagranie zostanie odtworzone tylko jeden raz.",
            emptyList(),
            startMs = 62900, endMs = 424900
        ),
        ListeningTask("b2-l2",
            "Zadanie 2. Proszę uważnie słuchać nagrania. Uwaga! Nagranie zostanie odtworzone dwa razy.",
            emptyList(),
            startMs = 427900, endMs = 789900
        ),
        ListeningTask("b2-l3",
            "Zadanie 3. Proszę uważnie słuchać nagrania. Uwaga! Nagranie zostanie odtworzone dwa razy.",
            emptyList(),
            startMs = 792900, endMs = 1144900
        ),
        ListeningTask("b2-l4",
            "Zadanie 4. Proszę uważnie słuchać nagrania. Uwaga! Nagranie zostanie odtworzone dwa razy.",
            emptyList(),
            startMs = 1144900, endMs = 1577200
        ),
        ListeningTask("b2-l5",
            "Zadanie 5. Proszę uważnie słuchać nagrania. Uwaga! Nagranie zostanie odtworzone dwa razy.",
            emptyList(),
            startMs = 1580200, endMs = 2003200
        ),
    ),
    audioUrl = "exam-audio/B2-2019.mp3",
)

internal val examListeningData: Map<String, ListeningExam> = mapOf(
    "b1-2019" to b1_2019Listening,
    "b2-2019" to b2_2019Listening,
)
