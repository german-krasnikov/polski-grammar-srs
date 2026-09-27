package polski.training

import kotlin.test.*
import polski.model.*
import polski.data.skills

/** Generated from tests/fixtures/kotlin-parity at React revision df59774e153a5bdf590fe27bd8780c7bd5457a26. */
class TrainingParityTest {
    private class Draws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }
    private fun factory(draws: List<Double>) = ExerciseFactory(Draws(draws), ExerciseIdFactory { "generated" })
    @Test fun metadataAndChainLinks() {
        assertEquals(16, skills.size)
        assertEquals(12, sentenceSeeds.size)
        for (seed in sentenceSeeds) {
            val chain = factory(listOf(0.1)).generateChain(seed)
            assertEquals(5, chain.size)
            for (i in 0 until 4) assertEquals(chain[i].expected, chain[i + 1].source)
        }
    }
    @Test fun allExerciseCases() {
        run { // C-01-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[0]
            assertEquals(Exercise("generated", "case.acc.f", "To jest moja piękna żona.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę moją piękną żonę.", listOf(), "Widzę → Biernik.", listOf("acc"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moja piękna żona", "moją piękną żonę", "Widzę → Biernik."))), actual, "C-01-01")
        }
        run { // C-01-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę moją piękną żonę.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem moją piękną żonę.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-01-02")
        }
        run { // C-01-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem moją piękną żonę.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojej pięknej żony.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("moją piękną żonę", "mojej pięknej żony", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-01-03")
        }
        run { // C-01-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojej pięknej żony.", "Замени «мой / моя» на «их».", "Nie widziałem ich pięknej żony.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "wife", "beautiful", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojej pięknej żony", "ich pięknej żony", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-01-04")
        }
        run { // C-01-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich pięknej żony.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich pięknej żonie.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "wife", "beautiful", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich pięknej żony", "ich pięknej żonie", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-01-05")
        }
        run { // C-02-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój dobry mąż.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mojego dobrego męża.", listOf(), "Widzę → Biernik.", listOf("acc"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój dobry mąż", "mojego dobrego męża", "Widzę → Biernik."))), actual, "C-02-01")
        }
        run { // C-02-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mojego dobrego męża.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mojego dobrego męża.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-02-02")
        }
        run { // C-02-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mojego dobrego męża.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego dobrego męża.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mojego dobrego męża", "mojego dobrego męża", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-02-03")
        }
        run { // C-02-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego dobrego męża.", "Замени «мой / моя» на «их».", "Nie widziałem ich dobrego męża.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "husband", "good", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego męża", "ich dobrego męża", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-02-04")
        }
        run { // C-02-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich dobrego męża.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich dobrym mężu.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "husband", "good", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich dobrego męża", "ich dobrym mężu", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-02-05")
        }
        run { // C-03-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("friendM", "good"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój dobry kolega.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mojego dobrego kolegę.", listOf(), "Widzę → Biernik.", listOf("acc"), "friendM", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój dobry kolega", "mojego dobrego kolegę", "Widzę → Biernik."))), actual, "C-03-01")
        }
        run { // C-03-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("friendM", "good"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mojego dobrego kolegę.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mojego dobrego kolegę.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "friendM", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-03-02")
        }
        run { // C-03-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("friendM", "good"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mojego dobrego kolegę.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego dobrego kolegi.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "friendM", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mojego dobrego kolegę", "mojego dobrego kolegi", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-03-03")
        }
        run { // C-03-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("friendM", "good"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego dobrego kolegi.", "Замени «мой / моя» на «их».", "Nie widziałem ich dobrego kolegi.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "friendM", "good", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego kolegi", "ich dobrego kolegi", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-03-04")
        }
        run { // C-03-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("friendM", "good"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich dobrego kolegi.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich dobrym koledze.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "friendM", "good", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich dobrego kolegi", "ich dobrym koledze", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-03-05")
        }
        run { // C-04-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój mały syn.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mojego małego syna.", listOf(), "Widzę → Biernik.", listOf("acc"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój mały syn", "mojego małego syna", "Widzę → Biernik."))), actual, "C-04-01")
        }
        run { // C-04-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mojego małego syna.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mojego małego syna.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-04-02")
        }
        run { // C-04-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mojego małego syna.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego małego syna.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mojego małego syna", "mojego małego syna", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-04-03")
        }
        run { // C-04-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego małego syna.", "Замени «мой / моя» на «их».", "Nie widziałem ich małego syna.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "son", "small", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego małego syna", "ich małego syna", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-04-04")
        }
        run { // C-04-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich małego syna.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich małym synu.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "son", "small", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich małego syna", "ich małym synu", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-04-05")
        }
        run { // C-05-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój dobry pies.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mojego dobrego psa.", listOf(), "Widzę → Biernik.", listOf("acc"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój dobry pies", "mojego dobrego psa", "Widzę → Biernik."))), actual, "C-05-01")
        }
        run { // C-05-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mojego dobrego psa.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mojego dobrego psa.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-05-02")
        }
        run { // C-05-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mojego dobrego psa.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego dobrego psa.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mojego dobrego psa", "mojego dobrego psa", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-05-03")
        }
        run { // C-05-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego dobrego psa.", "Замени «мой / моя» на «их».", "Nie widziałem ich dobrego psa.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "dog", "good", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego psa", "ich dobrego psa", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-05-04")
        }
        run { // C-05-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich dobrego psa.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich dobrym psie.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "dog", "good", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich dobrego psa", "ich dobrym psie", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-05-05")
        }
        run { // C-06-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój mały kot.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mojego małego kota.", listOf(), "Widzę → Biernik.", listOf("acc"), "cat", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój mały kot", "mojego małego kota", "Widzę → Biernik."))), actual, "C-06-01")
        }
        run { // C-06-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mojego małego kota.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mojego małego kota.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "cat", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-06-02")
        }
        run { // C-06-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mojego małego kota.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego małego kota.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "cat", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mojego małego kota", "mojego małego kota", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-06-03")
        }
        run { // C-06-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego małego kota.", "Замени «мой / моя» на «их».", "Nie widziałem ich małego kota.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "cat", "small", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego małego kota", "ich małego kota", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-06-04")
        }
        run { // C-06-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich małego kota.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich małym kocie.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "cat", "small", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich małego kota", "ich małym kocie", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-06-05")
        }
        run { // C-07-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[0]
            assertEquals(Exercise("generated", "case.acc.f", "To jest moja nowa książka.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę moją nową książkę.", listOf(), "Widzę → Biernik.", listOf("acc"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moja nowa książka", "moją nową książkę", "Widzę → Biernik."))), actual, "C-07-01")
        }
        run { // C-07-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę moją nową książkę.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem moją nową książkę.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-07-02")
        }
        run { // C-07-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem moją nową książkę.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojej nowej książki.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("moją nową książkę", "mojej nowej książki", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-07-03")
        }
        run { // C-07-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojej nowej książki.", "Замени «мой / моя» на «их».", "Nie widziałem ich nowej książki.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "book", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojej nowej książki", "ich nowej książki", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-07-04")
        }
        run { // C-07-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich nowej książki.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich nowej książce.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "book", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich nowej książki", "ich nowej książce", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-07-05")
        }
        run { // C-08-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój nowy samochód.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mój nowy samochód.", listOf(), "Widzę → Biernik.", listOf("acc"), "car", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój nowy samochód", "mój nowy samochód", "Widzę → Biernik."))), actual, "C-08-01")
        }
        run { // C-08-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mój nowy samochód.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mój nowy samochód.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "car", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-08-02")
        }
        run { // C-08-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mój nowy samochód.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego nowego samochodu.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "car", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mój nowy samochód", "mojego nowego samochodu", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-08-03")
        }
        run { // C-08-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego nowego samochodu.", "Замени «мой / моя» на «их».", "Nie widziałem ich nowego samochodu.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "car", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego nowego samochodu", "ich nowego samochodu", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-08-04")
        }
        run { // C-08-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich nowego samochodu.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich nowym samochodzie.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "car", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich nowego samochodu", "ich nowym samochodzie", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-08-05")
        }
        run { // C-09-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój nowy dom.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mój nowy dom.", listOf(), "Widzę → Biernik.", listOf("acc"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój nowy dom", "mój nowy dom", "Widzę → Biernik."))), actual, "C-09-01")
        }
        run { // C-09-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mój nowy dom.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mój nowy dom.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-09-02")
        }
        run { // C-09-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mój nowy dom.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego nowego domu.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mój nowy dom", "mojego nowego domu", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-09-03")
        }
        run { // C-09-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego nowego domu.", "Замени «мой / моя» на «их».", "Nie widziałem ich nowego domu.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "house", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego nowego domu", "ich nowego domu", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-09-04")
        }
        run { // C-09-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich nowego domu.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich nowym domu.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "house", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich nowego domu", "ich nowym domu", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-09-05")
        }
        run { // C-10-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[0]
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój nowy telefon.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę mój nowy telefon.", listOf(), "Widzę → Biernik.", listOf("acc"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój nowy telefon", "mój nowy telefon", "Widzę → Biernik."))), actual, "C-10-01")
        }
        run { // C-10-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę mój nowy telefon.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem mój nowy telefon.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-10-02")
        }
        run { // C-10-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem mój nowy telefon.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego nowego telefonu.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mój nowy telefon", "mojego nowego telefonu", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-10-03")
        }
        run { // C-10-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego nowego telefonu.", "Замени «мой / моя» на «их».", "Nie widziałem ich nowego telefonu.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "phone", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego nowego telefonu", "ich nowego telefonu", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-10-04")
        }
        run { // C-10-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich nowego telefonu.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich nowym telefonie.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "phone", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich nowego telefonu", "ich nowym telefonie", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-10-05")
        }
        run { // C-11-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[0]
            assertEquals(Exercise("generated", "case.acc.n", "To jest moje małe dziecko.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę moje małe dziecko.", listOf(), "Widzę → Biernik.", listOf("acc"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moje małe dziecko", "moje małe dziecko", "Widzę → Biernik."))), actual, "C-11-01")
        }
        run { // C-11-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę moje małe dziecko.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem moje małe dziecko.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-11-02")
        }
        run { // C-11-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem moje małe dziecko.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego małego dziecka.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("moje małe dziecko", "mojego małego dziecka", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-11-03")
        }
        run { // C-11-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego małego dziecka.", "Замени «мой / моя» на «их».", "Nie widziałem ich małego dziecka.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "child", "small", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego małego dziecka", "ich małego dziecka", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-11-04")
        }
        run { // C-11-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich małego dziecka.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich małym dziecku.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "child", "small", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich małego dziecka", "ich małym dziecku", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-11-05")
        }
        run { // C-12-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[0]
            assertEquals(Exercise("generated", "case.acc.n", "To jest moje nowe okno.", "Скажи, что видишь это. Начни с «Widzę…».", "Widzę moje nowe okno.", listOf(), "Widzę → Biernik.", listOf("acc"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moje nowe okno", "moje nowe okno", "Widzę → Biernik."))), actual, "C-12-01")
        }
        run { // C-12-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[1]
            assertEquals(Exercise("generated", "verb.past", "Widzę moje nowe okno.", "Перенеси предложение в прошедшее время. Говори от мужского лица.", "Widziałem moje nowe okno.", listOf(), "Меняется только время глагола.", listOf("past", "acc"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Widziałem", "Меняется только время глагола."))), actual, "C-12-02")
        }
        run { // C-12-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[2]
            assertEquals(Exercise("generated", "case.gen.neg", "Widziałem moje nowe okno.", "Теперь сделай это предложение отрицательным.", "Nie widziałem mojego nowego okna.", listOf(), "Отрицание → Biernik меняется на Dopełniacz.", listOf("gen", "negation"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("moje nowe okno", "mojego nowego okna", "Отрицание → Biernik меняется на Dopełniacz."))), actual, "C-12-03")
        }
        run { // C-12-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[3]
            assertEquals(Exercise("generated", "agreement.my", "Nie widziałem mojego nowego okna.", "Замени «мой / моя» на «их».", "Nie widziałem ich nowego okna.", listOf(), "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu.", listOf("gen", "agreement"), "window", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("mojego nowego okna", "ich nowego okna", "ich не склоняется; прилагательное и существительное остаются в Dopełniaczu."))), actual, "C-12-04")
        }
        run { // C-12-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[4]
            assertEquals(Exercise("generated", "case.loc", "Nie widziałem ich nowego okna.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o ich nowym oknie.", listOf(), "o → Miejscownik. Сохрани владельца «их».", listOf("loc"), "window", "new", PossessiveId.fromId("their"), NumberGram.fromId("sg"), listOf(FormChange("ich nowego okna", "ich nowym oknie", "o → Miejscownik. Сохрани владельца «их»."))), actual, "C-12-05")
        }
        run { // E-case.acc.n-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.n", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "case.acc.n", "To jest moje małe dziecko.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę moje małe dziecko.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "n"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moje małe dziecko", "moje małe dziecko", "Biernik совпадает с Mianownikiem: группа сохраняет базовую форму."))), actual, "E-case.acc.n-preferred")
        }
        run { // E-case.acc.n-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.n", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "case.acc.n", "To jest moje nowe okno.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę moje nowe okno.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "n"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moje nowe okno", "moje nowe okno", "Biernik совпадает с Mianownikiem: группа сохраняет базовую форму."))), actual, "E-case.acc.n-fallback")
        }
        run { // E-case.acc.n-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.n", null)
            assertEquals(Exercise("generated", "case.acc.n", "To jest moje małe dziecko.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę moje małe dziecko.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "n"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moje małe dziecko", "moje małe dziecko", "Biernik совпадает с Mianownikiem: группа сохраняет базовую форму."))), actual, "E-case.acc.n-random")
        }
        run { // E-case.acc.f-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.f", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "case.acc.f", "To jest moja piękna żona.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę moją piękną żonę.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "f"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moja piękna żona", "moją piękną żonę", "Изменяется вся группа слов."))), actual, "E-case.acc.f-preferred")
        }
        run { // E-case.acc.f-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.f", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "case.acc.f", "To jest moja piękna żona.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę moją piękną żonę.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "f"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moja piękna żona", "moją piękną żonę", "Изменяется вся группа слов."))), actual, "E-case.acc.f-fallback")
        }
        run { // E-case.acc.f-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.f", null)
            assertEquals(Exercise("generated", "case.acc.f", "To jest moja piękna żona.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę moją piękną żonę.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "f"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moja piękna żona", "moją piękną żonę", "Изменяется вся группа слов."))), actual, "E-case.acc.f-random")
        }
        run { // E-case.acc.m-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.m", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój mały syn.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę mojego małego syna.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "m-personal"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój mały syn", "mojego małego syna", "Изменяется вся группа слов."))), actual, "E-case.acc.m-preferred")
        }
        run { // E-case.acc.m-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.m", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój mały syn.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę mojego małego syna.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "m-personal"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój mały syn", "mojego małego syna", "Изменяется вся группа слов."))), actual, "E-case.acc.m-fallback")
        }
        run { // E-case.acc.m-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.acc.m", null)
            assertEquals(Exercise("generated", "case.acc.m", "To jest mój mały syn.", "Скажи, что ты видишь этого человека или этот предмет. Начни с «Widzę…».", "Widzę mojego małego syna.", listOf(), "Widzę требует Biernik. Согласуй притяжательное местоимение, прилагательное и существительное.", listOf("acc", "m-personal"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mój mały syn", "mojego małego syna", "Изменяется вся группа слов."))), actual, "E-case.acc.m-random")
        }
        run { // E-case.gen.neg-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.gen.neg", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "case.gen.neg", "Widzę moją piękną żonę.", "Сделай всё предложение отрицательным.", "Nie widzę mojej pięknej żony.", listOf(), "Отрицание widzę переводит прямой объект из Biernika в Dopełniacz.", listOf("gen", "negation"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz"))), actual, "E-case.gen.neg-preferred")
        }
        run { // E-case.gen.neg-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.gen.neg", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "case.gen.neg", "Widzę moje nowe okno.", "Сделай всё предложение отрицательным.", "Nie widzę mojego nowego okna.", listOf(), "Отрицание widzę переводит прямой объект из Biernika в Dopełniacz.", listOf("gen", "negation"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("moje nowe okno", "mojego nowego okna", "Biernik → Dopełniacz"))), actual, "E-case.gen.neg-fallback")
        }
        run { // E-case.gen.neg-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.gen.neg", null)
            assertEquals(Exercise("generated", "case.gen.neg", "Widzę mojego dobrego psa.", "Сделай всё предложение отрицательным.", "Nie widzę mojego dobrego psa.", listOf(), "Отрицание widzę переводит прямой объект из Biernika в Dopełniacz.", listOf("gen", "negation"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Nie", "Добавь отрицание nie."), FormChange("mojego dobrego psa", "mojego dobrego psa", "Biernik → Dopełniacz"))), actual, "E-case.gen.neg-random")
        }
        run { // E-case.inst-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.inst", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "case.inst", "Widzę moją piękną żonę.", "Скажи, что идёшь вместе с этим человеком. Начни с «Idę z…».", "Idę z moją piękną żoną.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("inst"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moją piękną żonę", "moją piękną żoną", "z kim? czym? — Narzędnik"))), actual, "E-case.inst-preferred")
        }
        run { // E-case.inst-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.inst", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "case.inst", "Widzę mojego dobrego męża.", "Скажи, что идёшь вместе с этим человеком. Начни с «Idę z…».", "Idę z moim dobrym mężem.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("inst"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego męża", "moim dobrym mężem", "z kim? czym? — Narzędnik"))), actual, "E-case.inst-fallback")
        }
        run { // E-case.inst-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.inst", null)
            assertEquals(Exercise("generated", "case.inst", "Widzę mojego dobrego męża.", "Скажи, что идёшь вместе с этим человеком. Начни с «Idę z…».", "Idę z moim dobrym mężem.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("inst"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego męża", "moim dobrym mężem", "z kim? czym? — Narzędnik"))), actual, "E-case.inst-random")
        }
        run { // E-case.loc-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.loc", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "case.loc", "Widzę moją piękną żonę.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o mojej pięknej żonie.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("loc"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moją piękną żonę", "mojej pięknej żonie", "o kim? czym? — Miejscownik"))), actual, "E-case.loc-preferred")
        }
        run { // E-case.loc-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.loc", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "case.loc", "Widzę moje nowe okno.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o moim nowym oknie.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("loc"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moje nowe okno", "moim nowym oknie", "o kim? czym? — Miejscownik"))), actual, "E-case.loc-fallback")
        }
        run { // E-case.loc-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.loc", null)
            assertEquals(Exercise("generated", "case.loc", "Widzę mojego dobrego psa.", "Теперь скажи, что говоришь об этом. Начни с «Mówię o…».", "Mówię o moim dobrym psie.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("loc"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego psa", "moim dobrym psie", "o kim? czym? — Miejscownik"))), actual, "E-case.loc-random")
        }
        run { // E-case.dat-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.dat", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "case.dat", "Widzę moją piękną żonę.", "Скажи, что даришь этому человеку подарок. Начни с «Daję prezent…».", "Daję prezent mojej pięknej żonie.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("dat"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moją piękną żonę", "mojej pięknej żonie", "komu? czemu? — Celownik"))), actual, "E-case.dat-preferred")
        }
        run { // E-case.dat-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.dat", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "case.dat", "Widzę mojego dobrego męża.", "Скажи, что даришь этому человеку подарок. Начни с «Daję prezent…».", "Daję prezent mojemu dobremu mężowi.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("dat"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego męża", "mojemu dobremu mężowi", "komu? czemu? — Celownik"))), actual, "E-case.dat-fallback")
        }
        run { // E-case.dat-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("case.dat", null)
            assertEquals(Exercise("generated", "case.dat", "Widzę mojego dobrego męża.", "Скажи, что даришь этому человеку подарок. Начни с «Daję prezent…».", "Daję prezent mojemu dobremu mężowi.", listOf(), "Новая конструкция задаёт падеж всей группы: местоимение + прилагательное + существительное.", listOf("dat"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego męża", "mojemu dobremu mężowi", "komu? czemu? — Celownik"))), actual, "E-case.dat-random")
        }
        run { // E-agreement.my-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("agreement.my", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "agreement.my", "Widzę moją piękną żonę.", "Замени «мой / моя» на «её». Сохрани время и смысл предложения.", "Widzę jej piękną żonę.", listOf(), "mój, twój, nasz, wasz согласуются с предметом обладания. jego, jej, ich не склоняются.", listOf("agreement", "acc"), "wife", "beautiful", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("moją piękną żonę", "jej piękną żonę", "Меняем владельца, сохраняем Biernik."))), actual, "E-agreement.my-preferred")
        }
        run { // E-agreement.my-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("agreement.my", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "agreement.my", "Widzę moje nowe okno.", "Замени «мой / моя» на «её». Сохрани время и смысл предложения.", "Widzę jej nowe okno.", listOf(), "mój, twój, nasz, wasz согласуются с предметом обладания. jego, jej, ich не склоняются.", listOf("agreement", "acc"), "window", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("moje nowe okno", "jej nowe okno", "Меняем владельца, сохраняем Biernik."))), actual, "E-agreement.my-fallback")
        }
        run { // E-agreement.my-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("agreement.my", null)
            assertEquals(Exercise("generated", "agreement.my", "Widzę mojego dobrego psa.", "Замени «мой / моя» на «ваш / ваша». Сохрани время и смысл предложения.", "Widzę waszego dobrego psa.", listOf(), "mój, twój, nasz, wasz согласуются с предметом обладания. jego, jej, ich не склоняются.", listOf("agreement", "acc"), "dog", "good", PossessiveId.fromId("yourPlural"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego psa", "waszego dobrego psa", "Меняем владельца, сохраняем Biernik."))), actual, "E-agreement.my-random")
        }
        run { // E-verb.present-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.present", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "verb.present", "Moja piękna żona szła do domu.", "Перенеси всё предложение в настоящее время.", "Moja piękna żona idzie do domu.", listOf(), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "present", "nom"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("szła", "idzie", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.present-preferred")
        }
        run { // E-verb.present-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.present", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "verb.present", "Mój dobry mąż szedł do domu.", "Перенеси всё предложение в настоящее время.", "Mój dobry mąż idzie do domu.", listOf(), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "present", "nom"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("szedł", "idzie", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.present-fallback")
        }
        run { // E-verb.present-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.present", null)
            assertEquals(Exercise("generated", "verb.present", "Mój dobry mąż szedł do domu.", "Перенеси всё предложение в настоящее время.", "Mój dobry mąż idzie do domu.", listOf(), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "present", "nom"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("szedł", "idzie", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.present-random")
        }
        run { // E-verb.past-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.past", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "verb.past", "Moja piękna żona idzie do domu.", "Перенеси всё предложение в прошедшее время.", "Moja piękna żona szła do domu.", listOf(), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "past", "nom"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("idzie", "szła", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.past-preferred")
        }
        run { // E-verb.past-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.past", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "verb.past", "Mój dobry mąż idzie do domu.", "Перенеси всё предложение в прошедшее время.", "Mój dobry mąż szedł do domu.", listOf(), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "past", "nom"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("idzie", "szedł", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.past-fallback")
        }
        run { // E-verb.past-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.past", null)
            assertEquals(Exercise("generated", "verb.past", "Mój dobry mąż idzie do domu.", "Перенеси всё предложение в прошедшее время.", "Mój dobry mąż szedł do domu.", listOf(), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "past", "nom"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("idzie", "szedł", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.past-random")
        }
        run { // E-verb.future-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.future", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "verb.future", "Moja piękna żona idzie do domu.", "Перенеси всё предложение в будущее время. Сохрани несовершенный вид.", "Moja piękna żona będzie iść do domu.", listOf("Moja piękna żona będzie szła do domu."), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "future", "nom"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("idzie", "będzie iść", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.future-preferred")
        }
        run { // E-verb.future-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.future", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "verb.future", "Mój dobry mąż idzie do domu.", "Перенеси всё предложение в будущее время. Сохрани несовершенный вид.", "Mój dobry mąż będzie iść do domu.", listOf("Mój dobry mąż będzie szedł do domu."), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "future", "nom"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("idzie", "będzie iść", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.future-fallback")
        }
        run { // E-verb.future-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("verb.future", null)
            assertEquals(Exercise("generated", "verb.future", "Mój dobry mąż idzie do domu.", "Перенеси всё предложение в будущее время. Сохрани несовершенный вид.", "Mój dobry mąż będzie iść do domu.", listOf("Mój dobry mąż będzie szedł do domu."), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "future", "nom"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("idzie", "będzie iść", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.future-random")
        }
        run { // E-aspect-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("aspect", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "aspect", "Dzisiaj kupuję nową książkę.", "Скажи, что вчера уже купил эту книгу: завершённый результат. Начни с «Wczoraj…».", "Wczoraj kupiłem nową książkę.", listOf("Wczoraj kupiłam nową książkę."), "Процесс kupować → завершённый результат kupić. Форма kupiłam также правильная.", listOf("aspect", "past"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("kupuję", "kupiłem", "Совершенный вид + прошедшее время."))), actual, "E-aspect-preferred")
        }
        run { // E-aspect-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("aspect", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "aspect", "Dzisiaj kupuję nową książkę.", "Скажи, что вчера уже купил эту книгу: завершённый результат. Начни с «Wczoraj…».", "Wczoraj kupiłem nową książkę.", listOf("Wczoraj kupiłam nową książkę."), "Процесс kupować → завершённый результат kupić. Форма kupiłam также правильная.", listOf("aspect", "past"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("kupuję", "kupiłem", "Совершенный вид + прошедшее время."))), actual, "E-aspect-fallback")
        }
        run { // E-aspect-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("aspect", null)
            assertEquals(Exercise("generated", "aspect", "Dzisiaj kupuję nową książkę.", "Скажи, что вчера уже купил эту книгу: завершённый результат. Начни с «Wczoraj…».", "Wczoraj kupiłem nową książkę.", listOf("Wczoraj kupiłam nową książkę."), "Процесс kupować → завершённый результат kupić. Форма kupiłam также правильная.", listOf("aspect", "past"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("kupuję", "kupiłem", "Совершенный вид + прошедшее время."))), actual, "E-aspect-random")
        }
        run { // E-pronouns-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("pronouns", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "pronouns", "Widzę moją piękną żonę.", "Замени всю группу после «Widzę» одним личным местоимением.", "Widzę ją.", listOf(), "Местоимение заменяет всю группу слов и остаётся в Bierniku.", listOf("pronoun", "acc"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("moją piękną żonę", "ją", "кого? что?"))), actual, "E-pronouns-preferred")
        }
        run { // E-pronouns-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("pronouns", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "pronouns", "Widzę mojego dobrego męża.", "Замени всю группу после «Widzę» одним личным местоимением.", "Widzę go.", listOf(), "Местоимение заменяет всю группу слов и остаётся в Bierniku.", listOf("pronoun", "acc"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego męża", "go", "кого? что?"))), actual, "E-pronouns-fallback")
        }
        run { // E-pronouns-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("pronouns", null)
            assertEquals(Exercise("generated", "pronouns", "Widzę mojego dobrego męża.", "Замени всю группу после «Widzę» одним личным местоимением.", "Widzę go.", listOf(), "Местоимение заменяет всю группу слов и остаётся в Bierniku.", listOf("pronoun", "acc"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("mojego dobrego męża", "go", "кого? что?"))), actual, "E-pronouns-random")
        }
        run { // E-mixed-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("mixed", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz"))), actual, "E-mixed-preferred")
        }
        run { // E-mixed-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("mixed", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "mixed", "Widzę moje nowe okno.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojego nowego okna.", listOf("Nie widziałaś mojego nowego okna."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moje nowe okno", "mojego nowego okna", "Biernik → Dopełniacz"))), actual, "E-mixed-fallback")
        }
        run { // E-mixed-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("mixed", null)
            assertEquals(Exercise("generated", "mixed", "Widzę mojego dobrego psa.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojego dobrego psa.", listOf("Nie widziałaś mojego dobrego psa."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("mojego dobrego psa", "mojego dobrego psa", "Biernik → Dopełniacz"))), actual, "E-mixed-random")
        }
        run { // E-sentence.question-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("sentence.question", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "sentence.question", "Widzisz moją piękną żonę.", "Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Czy…».", "Czy widzisz moją piękną żonę?", listOf(), "Czy превращает утверждение в общий вопрос. Падеж объекта сохраняется.", listOf("question", "acc"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Czy", "Добавь вопросительное czy."))), actual, "E-sentence.question-preferred")
        }
        run { // E-sentence.question-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("sentence.question", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "sentence.question", "Widzisz moje nowe okno.", "Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Czy…».", "Czy widzisz moje nowe okno?", listOf(), "Czy превращает утверждение в общий вопрос. Падеж объекта сохраняется.", listOf("question", "acc"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Czy", "Добавь вопросительное czy."))), actual, "E-sentence.question-fallback")
        }
        run { // E-sentence.question-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("sentence.question", null)
            assertEquals(Exercise("generated", "sentence.question", "Widzisz mojego dobrego psa.", "Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Czy…».", "Czy widzisz mojego dobrego psa?", listOf(), "Czy превращает утверждение в общий вопрос. Падеж объекта сохраняется.", listOf("question", "acc"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("", "Czy", "Добавь вопросительное czy."))), actual, "E-sentence.question-random")
        }
        run { // E-sentence.plural-preferred
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("sentence.plural", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "sentence.plural", "Widzę moją piękną żonę.", "Поставь всю группу после «Widzę» во множественное число.", "Widzę moje piękne żony.", listOf(), "Во множественном числе мужские личные формы отличаются от остальных: moich dobrych kolegów, но moje nowe książki.", listOf("plural", "acc"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("pl"), listOf(FormChange("moją piękną żonę", "moje piękne żony", "Меняются число и согласование всей группы."))), actual, "E-sentence.plural-preferred")
        }
        run { // E-sentence.plural-fallback
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("sentence.plural", SentenceSeed("window", "new"))
            assertEquals(Exercise("generated", "sentence.plural", "Widzę moje nowe okno.", "Поставь всю группу после «Widzę» во множественное число.", "Widzę moje nowe okna.", listOf(), "Во множественном числе мужские личные формы отличаются от остальных: moich dobrych kolegów, но moje nowe książki.", listOf("plural", "acc"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("pl"), listOf(FormChange("moje nowe okno", "moje nowe okna", "Меняются число и согласование всей группы."))), actual, "E-sentence.plural-fallback")
        }
        run { // E-sentence.plural-random
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("sentence.plural", null)
            assertEquals(Exercise("generated", "sentence.plural", "Widzę mojego dobrego psa.", "Поставь всю группу после «Widzę» во множественное число.", "Widzę moje dobre psy.", listOf(), "Во множественном числе мужские личные формы отличаются от остальных: moich dobrych kolegów, но moje nowe książki.", listOf("plural", "acc"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("pl"), listOf(FormChange("mojego dobrego psa", "moje dobre psy", "Меняются число и согласование всей группы."))), actual, "E-sentence.plural-random")
        }
        run { // E-agreement.my-accepted
            val factory = factory(listOf(0.2, 0.4, 0.6))
            val actual = factory.generateForSkill("agreement.my", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "agreement.my", "Widzę moją piękną żonę.", "Замени «мой / моя» на «его». Сохрани время и смысл предложения.", "Widzę jego piękną żonę.", listOf(), "mój, twój, nasz, wasz согласуются с предметом обладания. jego, jej, ich не склоняются.", listOf("agreement", "acc"), "wife", "beautiful", PossessiveId.fromId("his"), NumberGram.fromId("sg"), listOf(FormChange("moją piękną żonę", "jego piękną żonę", "Меняем владельца, сохраняем Biernik."))), actual, "E-agreement.my-accepted")
        }
        run { // E-verb.future-accepted
            val factory = factory(listOf(0.2, 0.4, 0.6))
            val actual = factory.generateForSkill("verb.future", SentenceSeed("husband", "good"))
            assertEquals(Exercise("generated", "verb.future", "Mój dobry mąż idzie do domu.", "Перенеси всё предложение в будущее время. Сохрани несовершенный вид.", "Mój dobry mąż będzie iść do domu.", listOf("Mój dobry mąż będzie szedł do domu."), "Подлежащее остаётся в Mianowniku. В прошедшем времени глагол согласуется с ним по роду.", listOf("verb", "future", "nom"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("idzie", "będzie iść", "Меняется время глагола, остальные слова сохраняются."))), actual, "E-verb.future-accepted")
        }
        run { // E-aspect-accepted
            val factory = factory(listOf(0.2, 0.4, 0.6))
            val actual = factory.generateForSkill("aspect", SentenceSeed("book", "new"))
            assertEquals(Exercise("generated", "aspect", "Dzisiaj kupuję nową książkę.", "Скажи, что вчера уже купил эту книгу: завершённый результат. Начни с «Wczoraj…».", "Wczoraj kupiłem nową książkę.", listOf("Wczoraj kupiłam nową książkę."), "Процесс kupować → завершённый результат kupić. Форма kupiłam также правильная.", listOf("aspect", "past"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("kupuję", "kupiłem", "Совершенный вид + прошедшее время."))), actual, "E-aspect-accepted")
        }
        run { // E-mixed-accepted
            val factory = factory(listOf(0.2, 0.4, 0.6))
            val actual = factory.generateForSkill("mixed", SentenceSeed("wife", "beautiful"))
            assertEquals(Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz"))), actual, "E-mixed-accepted")
        }
    }
    @Test fun allEvaluationCases() {
        run { // A-exact
            assertEquals(Evaluation(true, "nie widziałeś mojej pięknej żony", "Nie widziałeś mojej pięknej żony.", 0), evaluate("Nie widziałeś mojej pięknej żony.", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-exact")
        }
        run { // A-accepted
            assertEquals(Evaluation(true, "nie widziałaś mojej pięknej żony", "Nie widziałeś mojej pięknej żony.", 0), evaluate("Nie widziałaś mojej pięknej żony.", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-accepted")
        }
        run { // A-case
            assertEquals(Evaluation(true, "nie widziałeś mojej pięknej żony", "Nie widziałeś mojej pięknej żony.", 0), evaluate("NIE WIDZIAŁEŚ MOJEJ PIĘKNEJ ŻONY.", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-case")
        }
        run { // A-whitespace
            assertEquals(Evaluation(true, "nie widziałeś mojej pięknej żony", "Nie widziałeś mojej pięknej żony.", 0), evaluate("  Nie   widziałeś   mojej   pięknej   żony.  ", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-whitespace")
        }
        run { // A-punctuation
            assertEquals(Evaluation(true, "nie widziałeś mojej pięknej żony", "Nie widziałeś mojej pięknej żony.", 0), evaluate("Nie widziałeś mojej pięknej żony?!", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-punctuation")
        }
        run { // A-decomposed
            assertEquals(Evaluation(true, "nie widziałeś mojej pięknej żony", "Nie widziałeś mojej pięknej żony.", 0), evaluate("Nie widziałeś mojej pięknej żony.", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-decomposed")
        }
        run { // A-no-diacritics
            assertEquals(Evaluation(false, "nie widziales mojej pieknej zony", "Nie widziałeś mojej pięknej żony.", 4), evaluate("Nie widziales mojej pieknej zony.", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-no-diacritics")
        }
        run { // A-empty
            assertEquals(Evaluation(false, "", "Nie widziałeś mojej pięknej żony.", 32), evaluate("", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-empty")
        }
        run { // A-wrong
            assertEquals(Evaluation(false, "widzę mojego dobrego kolegę", "Nie widziałeś mojej pięknej żony.", 23), evaluate("Widzę mojego dobrego kolegę.", Exercise("generated", "mixed", "Widzę moją piękną żonę.", "Скажи «ты не видел…» в прошедшем времени. Сохрани «мой / моя».", "Nie widziałeś mojej pięknej żony.", listOf("Nie widziałaś mojej pięknej żony."), "TY + прошедшее время + отрицание. Отрицание требует Dopełniacza всей группы слов.", listOf("mixed", "gen", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("Widzę", "Nie widziałeś", "Лицо + время + отрицание"), FormChange("moją piękną żonę", "mojej pięknej żony", "Biernik → Dopełniacz")))), "A-wrong")
        }
    }
    @Test fun queueSelection() {
        val cards = listOf(DueSkillCard("first", 20), DueSkillCard("second", 10), DueSkillCard("third", 10))
        assertEquals("first", nextSkillId(cards, "first"))
        assertEquals("second", nextSkillId(cards))
        assertEquals("second", nextSkillId(cards, "unknown"))
        assertFailsWith<IllegalStateException> { nextSkillId(emptyList()) }
    }
    @Test fun portsAndUtf16Distance() {
        assertFailsWith<IllegalArgumentException> { ExerciseFactory(RandomSource { 1.0 }, ExerciseIdFactory { "id" }).generateForSkill("mixed") }
        assertFailsWith<IllegalArgumentException> { ExerciseFactory(RandomSource { 0.0 }, ExerciseIdFactory { "" }).generateChain() }
        val ex = factory(listOf(0.0)).generateForSkill("mixed").copy(expected = "a", accepted = emptyList())
        assertEquals(2, evaluate("😀", ex).distance)
        assertEquals(1, evaluate("b", ex.copy(expected = "aaaa", accepted = listOf("bb"))).distance)
        assertFailsWith<IllegalStateException> { factory(listOf(0.0)).generateForSkill("unknown") }
        val preferred = factory(listOf(0.0)).generateForSkill("case.gen.neg", SentenceSeed("wife", "small"))
        assertEquals("small", preferred.adjectiveId)
        assertEquals("Nie widzę mojej małej żony.", preferred.expected)
    }
}
