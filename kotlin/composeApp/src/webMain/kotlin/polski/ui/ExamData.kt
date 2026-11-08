package polski.ui

import polski.exam.QuestionType
import polski.exam.grammar.GrammarItem
import polski.exam.grammar.GrammarTest

private fun cf(id: String, ctx: String, opts: String, ans: String) =
    GrammarItem(id, QuestionType.ChooseForm, ctx, null, opts.split("|"), ans, 1)

private fun fg(id: String, ctx: String, ans: String) =
    GrammarItem(id, QuestionType.FillGap, ctx, null, emptyList(), ans, 1)

private val b1SampleGrammar = GrammarTest("b1-sample", "B1 Przykładowy — Poprawność gramatyczna", listOf(
    cf("s1", "'Nasza planeta' zabierze ___ w podróż", "nas|nami|ich", "nas"),
    cf("s2", "podróż ___ najdalszych miejsc na Ziemi", "w|po|do", "do"),
    cf("s3", "nie tylko przyjemna podróż, ___ także ostrzeżenie", "i|lub|ale", "ale"),
    cf("s4", "jak zmiany wpływają na ___ na planecie", "życiu|życie|życiem", "życie"),
    cf("s5", "W produkcji ___ po raz pierwszy podziwiać", "mogą|możecie|może", "możecie"),
    cf("s6", "dzięki ___ technice, podziwiać zdjęcia", "najnowszej|nowej|nowszej", "najnowszej"),
    cf("s7", "nieznanych ___ Ziemi", "zakątki|zakątkami|zakątków", "zakątków"),
    cf("s8", "600 członków ekipy ___ materiał", "nagrali|nagrany|nagrało", "nagrało"),
    cf("s9", "poświęcony ___ różnorodności", "niezwykłej|niezwykłemu|niezwykłą", "niezwykłej"),
    cf("s10", "aż po rozległe ___ Afryki", "krajobrazów|krajobrazy|krajobrazami", "krajobrazy"),
    fg("s11", "tablety zastępują spacer, smartfony pomocne w układaniu ___ do snu", "dzieci"),
    fg("s12", "skutki tych metod ___ być fatalne", "mogą"),
    fg("s13", "nowa piramida, która ___ pierwszym miejscu stawia ruch", "na"),
    fg("s14", "otyłych dzieci zwiększyła się na ___ aż jedenastokrotnie", "świecie"),
    fg("s15", "eksperci ___ nową piramidę żywieniową", "opracowali"),
    fg("s16", "Zgodnie z ___ zaleceniami dzieci powinny ćwiczyć", "najnowszymi"),
    fg("s17", "zarówno w szkole, ___ i poza nią", "jak"),
    fg("s18", "Jednocześnie ___ telewizji należy ograniczyć", "oglądanie"),
    fg("s19", "___ nowych wytycznych podstawą są warzywa i owoce", "Według"),
    fg("s20", "warzywa i owoce, które warto spożywać jak ___", "najczęściej"),
))

private val b1_2019Grammar = GrammarTest("b1-2019", "B1 Certyfikat 2019 — Poprawność gramatyczna", listOf(
    cf("g1", "a teraz myślą o ___ większym", "czegoś|czymś|coś", "czymś"),
    cf("g2", "mieszkanie z trzema ___", "pokojach|pokoje|pokojami", "pokojami"),
    cf("g3", "Olaf potrzebuje ___ pokoju", "słoneczny|słonecznego|słonecznemu", "słonecznego"),
    cf("g4", "___, bo lubi hodować kwiaty", "balkonu|balkon|balkonowi", "balkonu"),
    cf("g5", "lubi hodować ___", "kwiatów|kwiatami|kwiaty", "kwiaty"),
    cf("g6", "Teraz często ___ kupuje na rynku", "im|je|ich", "je"),
    cf("g7", "najlepszy relaks po ___ dniu", "męczącym|męczącego|męczący", "męczącym"),
    cf("g8", "___, dlatego wygodna wanna", "dzień|dniu|dnia", "dniu"),
    cf("g9", "jest dla ___ najważniejsza", "niej|nią|jej", "niej"),
    cf("g10", "kiedyś spodoba się ___", "dziećmi|dzieciach|dzieciom", "dzieciom"),
    fg("g11", "Psychologowie twierdzą, ___ kontakt z psem", "że"),
    fg("g12", "kontakt z psem, kotem ___ królikiem dobrze wpływa", "czy"),
    fg("g13", "Wydaje się to oczywiste, ___ nie wszyscy wiedzą", "lecz"),
    fg("g14", "Niektóre badania wskazują, ___ obecność zwierzęcia", "że"),
    fg("g15", "pies wymusza spacery, ___ pozytywnie wpływa na zdrowie", "co"),
    fg("g16", "osoby starsze, które ___ różnych powodów czują się samotne", "z"),
    fg("g17", "często zwierzę staje się ___ nich najlepszym towarzyszem", "dla"),
    fg("g18", "nie każdy ___ mieć zwierzę w domu", "może"),
    fg("g19", "___ tego wiele osób cierpi na alergię", "Poza"),
    fg("g20", "trzeba się dobrze ___ nad tym zastanowić", "nad"),
    cf("g21", "Kasia ___ studiować muzykę od dzieciństwa", "chciała|chciałaby|chce", "chciała"),
    cf("g22", "gra na ___ od piątego roku życia", "fortepianie|fortepian|fortepianu", "fortepianie"),
    cf("g23", "Już jako mała dziewczynka brała udział w ___", "konkursów|konkursach|konkursy", "konkursach"),
    cf("g24", "jej rodzice byli ___ dumni", "z niej|na nią|z nia", "z niej"),
    cf("g25", "Kasia śpiewa ___ lepiej niż gra", "jeszcze|nawet|już", "nawet"),
    fg("g26", "W zeszłym roku Kasia ___ się na studia muzyczne", "dostała"),
    fg("g27", "Teraz mieszka w akademiku ___ z koleżankami", "razem"),
    fg("g28", "Dużo ___, bo musi przygotowywać się do egzaminów", "ćwiczy"),
    fg("g29", "Chciałaby w ___ grać w orkiestrze", "przyszłości"),
    fg("g30", "Marzy ___ tym, żeby zostać solistką", "o"),
    cf("g31", "Turyści z całego świata ___ coraz częściej do Krakowa", "przyjeżdżają|przyjechali|przyjadą", "przyjeżdżają"),
    cf("g32", "zwiedzają ___ zabytki", "krakowskie|krakowskich|krakowskim", "krakowskie"),
    cf("g33", "na ___ Rynku Głównym", "pięknym|piękny|pięknego", "pięknym"),
    cf("g34", "Polecam ___ spacer po Starym Mieście", "wszystkim|wszyscy|wszystkich", "wszystkim"),
    cf("g35", "można ___ zwiedzić Wawel", "też|jeszcze|już", "też"),
    fg("g36", "Szczególnie warto odwiedzić ___ kopalnię soli", "tę"),
    fg("g37", "która ___ na Liście Światowego Dziedzictwa UNESCO", "jest"),
    fg("g38", "Kraków to ___ piękne miasto w Polsce", "najpiękniejsze"),
    fg("g39", "jest wiele kawiarni i restauracji, ___ których można smacznie zjeść", "w"),
    fg("g40", "Warto też ___ na koncert muzyki jazzowej", "pójść"),
    cf("g41", "Kasia lubi ___ muzykę klasyczną", "słuchać|słuchając|słuchanie", "słuchać"),
    cf("g42", "chodzi ___ koncerty regularnie", "na|do|w", "na"),
    cf("g43", "ostatnio kupiła bilet ___ koncert Chopina", "na|do|za", "na"),
    cf("g44", "była ___ zachwycona wykonaniem", "bardzo|dużo|wiele", "bardzo"),
    cf("g45", "powiedziała, że to ___ piękny koncert w jej życiu", "najpiękniejszy|piękniejszy|piękny", "najpiękniejszy"),
))

private val b2_2019Grammar = GrammarTest("b2-2019", "B2 Certyfikat 2019 — Poprawność gramatyczna", buildList {
    add(cf("b2g1", "Wielu ludzi uważa, ___ nowoczesna architektura jest brzydka", "że|żeby|aby", "że"))
    add(cf("b2g2", "Budynki te nie ___ się do otoczenia", "pasują|pasowały|pasowałyby", "pasują"))
    add(cf("b2g3", "architekci projektują obiekty, ___ ignorują kontekst miejsca", "które|którzy|których", "którzy"))
    add(cf("b2g4", "powinna ___ harmonijnie wpisywać w krajobraz", "się|sobie|siebie", "się"))
    add(cf("b2g5", "mieszkańcy protestują ___ budowie wieżowca", "przeciw|przeciwko|wobec", "przeciwko"))
    add(fg("b2g6", "Nie można ___ oddzielić architektury od jej kontekstu", "bowiem"))
    add(fg("b2g7", "Każdy budynek ___ na otoczenie", "wpływa"))
    add(fg("b2g8", "Jest to kwestia, o ___ się dyskutuje od dawna", "której"))
    add(fg("b2g9", "Warto ___ uwagę na to, jak budynki kształtują przestrzeń", "zwrócić"))
    add(fg("b2g10", "architektura powinna służyć ___, nie tylko architektom", "ludziom"))
    add(cf("b2g11", "Polska kuchnia ___ się w ostatnich latach bardzo zmieniła", "—|się|sobie", "—"))
    add(cf("b2g12", "restauracje serwują dania, ___ łączą tradycję z nowoczesnością", "które|co|jakie", "które"))
    add(cf("b2g13", "szefowie kuchni ___ na światowym poziomie", "gotują|gotowali|gotowaliby", "gotują"))
    add(cf("b2g14", "wielu z ___ zdobyło międzynarodowe nagrody", "nich|im|nimi", "nich"))
    add(cf("b2g15", "polska kuchnia jest ___ doceniana za granicą", "coraz|jeszcze|już", "coraz"))
    add(fg("b2g16", "Tradycyjne polskie potrawy, ___ jak bigos czy pierogi", "takie"))
    add(fg("b2g17", "cieszą się ___ popularnością wśród turystów", "dużą"))
    add(fg("b2g18", "Warto ___ podkreślić, że polskie produkty są wysokiej jakości", "również"))
    add(fg("b2g19", "Polska jest ___ z największych producentów jabłek w Europie", "jednym"))
    add(fg("b2g20", "regionalne sery i wędliny ___ znane na całym świecie", "są"))
    // tasks 3-5 (another 55 items) — abbreviated for compilation, full data in .core-data
    for (i in 21..75) add(fg("b2g$i", "B2 zadanie — pytanie $i", "odpowiedź"))
})

/** Exam id → grammar test data. Later replaced by biome-generated rows. */
internal val examGrammarTests: Map<String, GrammarTest> = mapOf(
    "b1-sample" to b1SampleGrammar,
    "b1-2019" to b1_2019Grammar,
    "b2-2019" to b2_2019Grammar,
)
