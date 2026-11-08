package polski.ui

private val b1_2019Writing = WritingExam(listOf(
    WritingTask(
        "w1",
        "Zadanie 1. Proszę napisać list (80-100 słów).",
        "Właśnie wróciłeś/wróciłaś z wycieczki do Krakowa. Napisz list do przyjaciela/przyjaciółki, w którym:\n- opiszesz, co widziałeś/widziałaś w Krakowie\n- napisz, co Ci się podobało najbardziej\n- zaproś przyjaciela/przyjaciółkę na wspólną wycieczkę",
        100,
    ),
    WritingTask(
        "w2",
        "Zadanie 2. Proszę napisać tekst (120-150 słów).",
        "Napisz artykuł do gazetki szkolnej na temat: 'Czy warto uczyć się języków obcych?'\n- przedstaw argumenty za i przeciw\n- napisz, jakie języki obce znasz i dlaczego je wybrałeś/wybrałaś\n- podaj swoją opinię",
        150,
    ),
))

private val b2_2019Writing = WritingExam(listOf(
    WritingTask(
        "b2w1",
        "Zadanie 1. Proszę napisać tekst oficjalny (200-250 słów).",
        "Napisz list formalny do redakcji gazety, w którym wyrażasz swoją opinię na temat planowanej budowy centrum handlowego w centrum miasta. Podaj argumenty za i przeciw, a następnie przedstaw swoją propozycję rozwiązania problemu.",
        250,
    ),
))

private val b1SampleWriting = WritingExam(listOf(
    WritingTask(
        "sw1",
        "Zadanie. Proszę napisać krótki tekst (80-100 słów).",
        "Niedawno zmieniłeś/zmieniłaś mieszkanie. Napisz e-mail do kolegi/koleżanki:\n- opisz nowe mieszkanie\n- napisz, co jest w okolicy\n- zaproś kolegę/koleżankę w odwiedziny",
        100,
    ),
))

internal val examWritingData: Map<String, WritingExam> = mapOf(
    "b1-sample" to b1SampleWriting,
    "b1-2019" to b1_2019Writing,
    "b2-2019" to b2_2019Writing,
)
