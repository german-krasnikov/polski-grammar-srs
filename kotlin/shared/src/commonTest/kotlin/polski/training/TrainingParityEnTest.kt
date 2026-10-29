package polski.training

import kotlin.test.*
import polski.core.engine.ExerciseIdFactory
import polski.core.engine.RandomSource
import polski.data.selectCoursePack
import polski.data.skills
import polski.model.*

/**
 * R1.1: characterization of the English (en-ru) exercise engine. The expected values record what the shipped engine
 * produced before R1.13-R1.17 move it off JSON (tests/fixtures/nj03-exercise-oracle/requirements-en.core-data); they
 * are not a linguistic oracle. A difference after those slices is a RED to investigate, never an edit of the oracle.
 */
class TrainingParityEnTest {
    private class Draws(private val values: List<Double>) : RandomSource {
        private var index = 0
        override fun nextDouble(): Double = values[(index++).coerceAtMost(values.lastIndex)]
    }
    private fun factory(draws: List<Double>) = PlExerciseEngine(Draws(draws), ExerciseIdFactory { "generated" })
    @BeforeTest fun selectEnglish() = selectCoursePack("en-ru")
    @AfterTest fun restorePolish() = selectCoursePack("pl-ru")
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
        // --- GENERATED START: do not edit, run scripts/nj03/generate-exercise-oracle-test.py ---
        run { // C-01-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my beautiful wife.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my beautiful wife.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my beautiful wife", "my beautiful wife", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-01-01")
        }
        run { // C-01-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my beautiful wife.", "Перенеси предложение в прошедшее время.", "I saw my beautiful wife.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-01-02")
        }
        run { // C-01-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my beautiful wife.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my beautiful wife.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "wife", "beautiful", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-01-03")
        }
        run { // C-01-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my beautiful wife.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her beautiful wife.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "wife", "beautiful", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my beautiful wife", "her beautiful wife", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-01-04")
        }
        run { // C-01-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("wife", "beautiful"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her beautiful wife.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her beautiful wife.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "wife", "beautiful", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her beautiful wife", "in her beautiful wife", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-01-05")
        }
        run { // C-02-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my good husband.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my good husband.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my good husband", "my good husband", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-02-01")
        }
        run { // C-02-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my good husband.", "Перенеси предложение в прошедшее время.", "I saw my good husband.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-02-02")
        }
        run { // C-02-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my good husband.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my good husband.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "husband", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-02-03")
        }
        run { // C-02-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my good husband.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her good husband.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "husband", "good", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my good husband", "her good husband", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-02-04")
        }
        run { // C-02-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("husband", "good"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her good husband.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her good husband.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "husband", "good", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her good husband", "in her good husband", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-02-05")
        }
        run { // C-03-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("friend", "good"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my good friend.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my good friend.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "friend", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my good friend", "my good friend", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-03-01")
        }
        run { // C-03-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("friend", "good"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my good friend.", "Перенеси предложение в прошедшее время.", "I saw my good friend.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "friend", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-03-02")
        }
        run { // C-03-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("friend", "good"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my good friend.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my good friend.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "friend", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-03-03")
        }
        run { // C-03-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("friend", "good"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my good friend.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her good friend.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "friend", "good", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my good friend", "her good friend", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-03-04")
        }
        run { // C-03-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("friend", "good"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her good friend.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her good friend.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "friend", "good", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her good friend", "in her good friend", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-03-05")
        }
        run { // C-04-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my small son.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my small son.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my small son", "my small son", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-04-01")
        }
        run { // C-04-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my small son.", "Перенеси предложение в прошедшее время.", "I saw my small son.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-04-02")
        }
        run { // C-04-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my small son.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my small son.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "son", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-04-03")
        }
        run { // C-04-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my small son.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her small son.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "son", "small", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my small son", "her small son", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-04-04")
        }
        run { // C-04-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("son", "small"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her small son.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her small son.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "son", "small", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her small son", "in her small son", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-04-05")
        }
        run { // C-05-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my good dog.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my good dog.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my good dog", "my good dog", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-05-01")
        }
        run { // C-05-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my good dog.", "Перенеси предложение в прошедшее время.", "I saw my good dog.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-05-02")
        }
        run { // C-05-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my good dog.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my good dog.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-05-03")
        }
        run { // C-05-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my good dog.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her good dog.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "dog", "good", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my good dog", "her good dog", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-05-04")
        }
        run { // C-05-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("dog", "good"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her good dog.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her good dog.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "dog", "good", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her good dog", "in her good dog", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-05-05")
        }
        run { // C-06-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my small cat.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my small cat.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "cat", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my small cat", "my small cat", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-06-01")
        }
        run { // C-06-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my small cat.", "Перенеси предложение в прошедшее время.", "I saw my small cat.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "cat", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-06-02")
        }
        run { // C-06-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my small cat.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my small cat.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "cat", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-06-03")
        }
        run { // C-06-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my small cat.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her small cat.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "cat", "small", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my small cat", "her small cat", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-06-04")
        }
        run { // C-06-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("cat", "small"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her small cat.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her small cat.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "cat", "small", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her small cat", "in her small cat", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-06-05")
        }
        run { // C-07-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my new book.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my new book.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new book", "my new book", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-07-01")
        }
        run { // C-07-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my new book.", "Перенеси предложение в прошедшее время.", "I saw my new book.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-07-02")
        }
        run { // C-07-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my new book.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my new book.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-07-03")
        }
        run { // C-07-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my new book.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her new book.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "book", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my new book", "her new book", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-07-04")
        }
        run { // C-07-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("book", "new"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her new book.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her new book.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "book", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her new book", "in her new book", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-07-05")
        }
        run { // C-08-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my new car.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my new car.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "car", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new car", "my new car", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-08-01")
        }
        run { // C-08-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my new car.", "Перенеси предложение в прошедшее время.", "I saw my new car.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "car", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-08-02")
        }
        run { // C-08-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my new car.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my new car.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "car", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-08-03")
        }
        run { // C-08-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my new car.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her new car.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "car", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my new car", "her new car", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-08-04")
        }
        run { // C-08-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("car", "new"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her new car.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her new car.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "car", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her new car", "in her new car", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-08-05")
        }
        run { // C-09-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my new house.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my new house.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new house", "my new house", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-09-01")
        }
        run { // C-09-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my new house.", "Перенеси предложение в прошедшее время.", "I saw my new house.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-09-02")
        }
        run { // C-09-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my new house.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my new house.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-09-03")
        }
        run { // C-09-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my new house.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her new house.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "house", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my new house", "her new house", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-09-04")
        }
        run { // C-09-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("house", "new"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her new house.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her new house.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "house", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her new house", "in her new house", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-09-05")
        }
        run { // C-10-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my new phone.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my new phone.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new phone", "my new phone", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-10-01")
        }
        run { // C-10-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my new phone.", "Перенеси предложение в прошедшее время.", "I saw my new phone.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-10-02")
        }
        run { // C-10-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my new phone.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my new phone.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-10-03")
        }
        run { // C-10-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my new phone.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her new phone.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "phone", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my new phone", "her new phone", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-10-04")
        }
        run { // C-10-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("phone", "new"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her new phone.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her new phone.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "phone", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her new phone", "in her new phone", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-10-05")
        }
        run { // C-11-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my small child.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my small child.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my small child", "my small child", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-11-01")
        }
        run { // C-11-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my small child.", "Перенеси предложение в прошедшее время.", "I saw my small child.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-11-02")
        }
        run { // C-11-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my small child.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my small child.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "child", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-11-03")
        }
        run { // C-11-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my small child.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her small child.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "child", "small", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my small child", "her small child", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-11-04")
        }
        run { // C-11-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("child", "small"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her small child.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her small child.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "child", "small", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her small child", "in her small child", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-11-05")
        }
        run { // C-12-01
            val factory = factory(listOf(0.11))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[0]
            assertEquals(Exercise("generated", "en:role.object", "Here is my new window.", "Скажи, что видишь этот предмет. Начни с «I see…».", "I see my new window.", listOf(), "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней.", listOf("obj"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new window", "my new window", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "C-12-01")
        }
        run { // C-12-02
            val factory = factory(listOf(0.22))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[1]
            assertEquals(Exercise("generated", "en:verb.pastSimple", "I see my new window.", "Перенеси предложение в прошедшее время.", "I saw my new window.", listOf(), "Меняется время глагола: настоящее → прошедшее, дополнение не меняется.", listOf("past"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее, дополнение не меняется."))), actual, "C-12-02")
        }
        run { // C-12-03
            val factory = factory(listOf(0.33))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[2]
            assertEquals(Exercise("generated", "en:polarity.past", "I saw my new window.", "Сделай предложение отрицательным, сохранив прошедшее время.", "I did not see my new window.", listOf(), "Добавлен did not перед базовой формой глагола.", listOf("neg", "past"), "window", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("saw", "did not see", "Добавлен did not перед базовой формой глагола."))), actual, "C-12-03")
        }
        run { // C-12-04
            val factory = factory(listOf(0.44))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[3]
            assertEquals(Exercise("generated", "en:possessive.my", "I did not see my new window.", "Замени владельца на «her». Сохрани отрицание и прошедшее время.", "I did not see her new window.", listOf(), "Меняется владелец; остальная фраза, включая отрицание, не меняется.", listOf("agreement"), "window", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("my new window", "her new window", "Меняется владелец; остальная фраза, включая отрицание, не меняется."))), actual, "C-12-04")
        }
        run { // C-12-05
            val factory = factory(listOf(0.55))
            val actual = factory.generateChain(SentenceSeed("window", "new"))[4]
            assertEquals(Exercise("generated", "en:role.location", "I did not see her new window.", "Скажи, что находишься внутри этого. Начни с «I stayed in…».", "I stayed in her new window.", listOf(), "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется.", listOf("role", "in"), "window", "new", PossessiveId.fromId("her"), NumberGram.fromId("sg"), listOf(FormChange("her new window", "in her new window", "Добавлен предлог in по роли «место»; владелец из предыдущего шага сохраняется."))), actual, "C-12-05")
        }
        run { // E-en:role.object-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:role.object", null)
            assertEquals(Exercise("generated", "en:role.object", "Here is my good dog.", "Скажи, что видишь названное. Начни с «I see…».", "I see my good dog.", listOf(), "I see требует дополнения после глагола; падежа нет — форма слова не меняется, меняется только его место в предложении.", listOf("obj"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my good dog", "my good dog", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "E-en:role.object-A")
        }
        run { // E-en:role.object-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:role.object", null)
            assertEquals(Exercise("generated", "en:role.object", "Here is my new phone.", "Скажи, что видишь названное. Начни с «I see…».", "I see my new phone.", listOf(), "I see требует дополнения после глагола; падежа нет — форма слова не меняется, меняется только его место в предложении.", listOf("obj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new phone", "my new phone", "Роль меняется (подлежащее → дополнение), форма слова остаётся прежней."))), actual, "E-en:role.object-B")
        }
        run { // E-en:verb.presentSimple-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:verb.presentSimple", null)
            assertEquals(Exercise("generated", "en:verb.presentSimple", "My good dog walked.", "Перенеси предложение в настоящее простое время.", "My good dog walks.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "present", "subj"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walked", "walks", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.presentSimple-A")
        }
        run { // E-en:verb.presentSimple-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:verb.presentSimple", null)
            assertEquals(Exercise("generated", "en:verb.presentSimple", "My new phone walked.", "Перенеси предложение в настоящее простое время.", "My new phone walks.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "present", "subj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walked", "walks", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.presentSimple-B")
        }
        run { // E-en:possessive.my-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:possessive.my", null)
            assertEquals(Exercise("generated", "en:possessive.my", "I see my good dog.", "Замени «мой» на «наш / наша». Сохрани время и смысл предложения.", "I see our good dog.", listOf(), "my/your/his/her/its/our/their не согласуются ни с родом, ни с числом предмета — это закрытый список неизменяемых слов.", listOf("agreement", "obj"), "dog", "good", PossessiveId.fromId("our"), NumberGram.fromId("sg"), listOf(FormChange("my good dog", "our good dog", "Меняем владельца, остальная фраза не меняется."))), actual, "E-en:possessive.my-A")
        }
        run { // E-en:possessive.my-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:possessive.my", null)
            assertEquals(Exercise("generated", "en:possessive.my", "I see my new phone.", "Замени «мой» на «твой / твоя». Сохрани время и смысл предложения.", "I see your new phone.", listOf(), "my/your/his/her/its/our/their не согласуются ни с родом, ни с числом предмета — это закрытый список неизменяемых слов.", listOf("agreement", "obj"), "phone", "new", PossessiveId.fromId("your"), NumberGram.fromId("sg"), listOf(FormChange("my new phone", "your new phone", "Меняем владельца, остальная фраза не меняется."))), actual, "E-en:possessive.my-B")
        }
        run { // E-en:mood.question-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:mood.question", null)
            assertEquals(Exercise("generated", "en:mood.question", "You walk.", "Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Do…».", "Do you walk?", listOf(), "Do выносится перед подлежащим; смысловой глагол остаётся в базовой форме.", listOf("mood", "question"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("You walk", "Do you walk", "Do-support: вспомогательный глагол выносится вперёд, смысловой глагол теряет -s."))), actual, "E-en:mood.question-A")
        }
        run { // E-en:mood.question-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:mood.question", null)
            assertEquals(Exercise("generated", "en:mood.question", "You walk.", "Сделай вопрос, на который можно ответить «да» или «нет». Начни с «Do…».", "Do you walk?", listOf(), "Do выносится перед подлежащим; смысловой глагол остаётся в базовой форме.", listOf("mood", "question"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("You walk", "Do you walk", "Do-support: вспомогательный глагол выносится вперёд, смысловой глагол теряет -s."))), actual, "E-en:mood.question-B")
        }
        run { // E-en:role.location-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:role.location", null)
            assertEquals(Exercise("generated", "en:role.location", "I see my good dog.", "Скажи, что находишься внутри этого. Начни с «I live in…».", "I live in my good dog.", listOf(), "Роль вводится отдельным предлогом; форма существительного не меняется — меняется только предлог перед ним.", listOf("role", "in"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my good dog", "in my good dog", "Добавлен предлог in по роли «место»."))), actual, "E-en:role.location-A")
        }
        run { // E-en:role.location-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:role.location", null)
            assertEquals(Exercise("generated", "en:role.location", "I see my new phone.", "Скажи, что находишься внутри этого. Начни с «I live in…».", "I live in my new phone.", listOf(), "Роль вводится отдельным предлогом; форма существительного не меняется — меняется только предлог перед ним.", listOf("role", "in"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new phone", "in my new phone", "Добавлен предлог in по роли «место»."))), actual, "E-en:role.location-B")
        }
        run { // E-en:role.instrument-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:role.instrument", null)
            assertEquals(Exercise("generated", "en:role.instrument", "I see my good dog.", "Скажи, что делаешь это вместе с этим/с помощью этого. Начни с «I go with…».", "I go with my good dog.", listOf(), "Роль вводится отдельным предлогом; форма существительного не меняется — меняется только предлог перед ним.", listOf("role", "with"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my good dog", "with my good dog", "Добавлен предлог with по роли «инструмент/спутник»."))), actual, "E-en:role.instrument-A")
        }
        run { // E-en:role.instrument-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:role.instrument", null)
            assertEquals(Exercise("generated", "en:role.instrument", "I see my new phone.", "Скажи, что делаешь это вместе с этим/с помощью этого. Начни с «I go with…».", "I go with my new phone.", listOf(), "Роль вводится отдельным предлогом; форма существительного не меняется — меняется только предлог перед ним.", listOf("role", "with"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new phone", "with my new phone", "Добавлен предлог with по роли «инструмент/спутник»."))), actual, "E-en:role.instrument-B")
        }
        run { // E-en:polarity.present-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:polarity.present", null)
            assertEquals(Exercise("generated", "en:polarity.present", "I walk.", "Сделай предложение отрицательным.", "I do not walk.", listOf("I don't walk."), "Do/does/did несёт отрицание; смысловой глагол остаётся в базовой форме.", listOf("polarity", "neg"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walk", "do not walk", "Добавлен do/does/did + not, смысловой глагол — в базовой форме."))), actual, "E-en:polarity.present-A")
        }
        run { // E-en:polarity.present-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:polarity.present", null)
            assertEquals(Exercise("generated", "en:polarity.present", "I walk.", "Сделай предложение отрицательным.", "I do not walk.", listOf("I don't walk."), "Do/does/did несёт отрицание; смысловой глагол остаётся в базовой форме.", listOf("polarity", "neg"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walk", "do not walk", "Добавлен do/does/did + not, смысловой глагол — в базовой форме."))), actual, "E-en:polarity.present-B")
        }
        run { // E-en:verb.pastSimple-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:verb.pastSimple", null)
            assertEquals(Exercise("generated", "en:verb.pastSimple", "My good dog walks.", "Перенеси предложение в прошедшее простое время.", "My good dog walked.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "past", "subj"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walks", "walked", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.pastSimple-A")
        }
        run { // E-en:verb.pastSimple-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:verb.pastSimple", null)
            assertEquals(Exercise("generated", "en:verb.pastSimple", "My new phone walks.", "Перенеси предложение в прошедшее простое время.", "My new phone walked.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "past", "subj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walks", "walked", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.pastSimple-B")
        }
        run { // E-en:role.recipient-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:role.recipient", null)
            assertEquals(Exercise("generated", "en:role.recipient", "I see my good friend.", "Скажи, кому это адресовано. Начни с «I give a present to…».", "I give a present to my good friend.", listOf(), "Роль вводится отдельным предлогом; форма существительного не меняется — меняется только предлог перед ним.", listOf("role", "to"), "friend", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my good friend", "to my good friend", "Добавлен предлог to по роли «получатель»."))), actual, "E-en:role.recipient-A")
        }
        run { // E-en:role.recipient-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:role.recipient", null)
            assertEquals(Exercise("generated", "en:role.recipient", "I see my small cat.", "Скажи, кому это адресовано. Начни с «I give a present to…».", "I give a present to my small cat.", listOf(), "Роль вводится отдельным предлогом; форма существительного не меняется — меняется только предлог перед ним.", listOf("role", "to"), "cat", "small", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my small cat", "to my small cat", "Добавлен предлог to по роли «получатель»."))), actual, "E-en:role.recipient-B")
        }
        run { // E-en:number.plural-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:number.plural", null)
            assertEquals(Exercise("generated", "en:number.plural", "I see my good dog.", "Скажи, что таких предметов несколько.", "I see my good dogs.", listOf(), "Существительное получает -s/-es или неправильную форму множественного числа; притяжательное и прилагательное не меняются.", listOf("plural", "obj"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("pl"), listOf(FormChange("my good dog", "my good dogs", "Существительное — во множественном числе, остальные слова — без изменений."))), actual, "E-en:number.plural-A")
        }
        run { // E-en:number.plural-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:number.plural", null)
            assertEquals(Exercise("generated", "en:number.plural", "I see my new phone.", "Скажи, что таких предметов несколько.", "I see my new phones.", listOf(), "Существительное получает -s/-es или неправильную форму множественного числа; притяжательное и прилагательное не меняются.", listOf("plural", "obj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("pl"), listOf(FormChange("my new phone", "my new phones", "Существительное — во множественном числе, остальные слова — без изменений."))), actual, "E-en:number.plural-B")
        }
        run { // E-en:polarity.past-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:polarity.past", null)
            assertEquals(Exercise("generated", "en:polarity.past", "I walked.", "Сделай предложение в прошедшем времени отрицательным.", "I did not walk.", listOf("I didn't walk."), "Do/does/did несёт отрицание; смысловой глагол остаётся в базовой форме.", listOf("polarity", "neg", "past"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walked", "did not walk", "Добавлен do/does/did + not, смысловой глагол — в базовой форме."))), actual, "E-en:polarity.past-A")
        }
        run { // E-en:polarity.past-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:polarity.past", null)
            assertEquals(Exercise("generated", "en:polarity.past", "I walked.", "Сделай предложение в прошедшем времени отрицательным.", "I did not walk.", listOf("I didn't walk."), "Do/does/did несёт отрицание; смысловой глагол остаётся в базовой форме.", listOf("polarity", "neg", "past"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walked", "did not walk", "Добавлен do/does/did + not, смысловой глагол — в базовой форме."))), actual, "E-en:polarity.past-B")
        }
        run { // E-en:verb.futureSimple-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:verb.futureSimple", null)
            assertEquals(Exercise("generated", "en:verb.futureSimple", "My good dog walks.", "Перенеси предложение в будущее простое время.", "My good dog will walk.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "future", "subj"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walks", "will walk", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.futureSimple-A")
        }
        run { // E-en:verb.futureSimple-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:verb.futureSimple", null)
            assertEquals(Exercise("generated", "en:verb.futureSimple", "My new phone walks.", "Перенеси предложение в будущее простое время.", "My new phone will walk.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "future", "subj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walks", "will walk", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.futureSimple-B")
        }
        run { // E-en:pronouns-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:pronouns", null)
            assertEquals(Exercise("generated", "en:pronouns", "I see my new book.", "Замени группу дополнения одним личным местоимением.", "I see it.", listOf(), "It заменяет целую группу слов, обозначающую предмет.", listOf("pronoun", "obj"), "book", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new book", "it", "Вся группа слов заменена местоимением it."))), actual, "E-en:pronouns-A")
        }
        run { // E-en:pronouns-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:pronouns", null)
            assertEquals(Exercise("generated", "en:pronouns", "I see my new phone.", "Замени группу дополнения одним личным местоимением.", "I see it.", listOf(), "It заменяет целую группу слов, обозначающую предмет.", listOf("pronoun", "obj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("my new phone", "it", "Вся группа слов заменена местоимением it."))), actual, "E-en:pronouns-B")
        }
        run { // E-en:verb.presentContinuous-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:verb.presentContinuous", null)
            assertEquals(Exercise("generated", "en:verb.presentContinuous", "My good dog walks.", "Скажи, что это происходит именно сейчас.", "My good dog is walking.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "continuous", "subj"), "dog", "good", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walks", "is walking", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.presentContinuous-A")
        }
        run { // E-en:verb.presentContinuous-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:verb.presentContinuous", null)
            assertEquals(Exercise("generated", "en:verb.presentContinuous", "My new phone walks.", "Скажи, что это происходит именно сейчас.", "My new phone is walking.", listOf(), "Подлежащее не меняется; меняется только форма глагола по времени и (для he/she/it) по -s.", listOf("verb", "continuous", "subj"), "phone", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("walks", "is walking", "Меняется форма глагола, остальные слова сохраняются."))), actual, "E-en:verb.presentContinuous-B")
        }
        run { // E-en:tense.contrast-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:tense.contrast", null)
            assertEquals(Exercise("generated", "en:tense.contrast", "I am reading a new book.", "Скажи, что ты уже прочитал эту книгу — результат сейчас есть, а не процесс. Начни с «I have…».", "I have read a new book.", listOf("I've read a new book."), "I read a new book (Simple — привычка/факт вообще) → I am reading a new book (Continuous — процесс, идёт сейчас) → I have read a new book (Perfect — результат уже есть, важен для настоящего момента). Одно и то же событие можно описать тремя разными «временами»: разница не в моменте времени, а в том, что говорящий хочет подчеркнуть.", listOf("tense", "contrast"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("am reading", "have read", "Continuous (процесс) → Perfect (результат, значимый сейчас): подчёркиваем не «что происходит», а «что уже сделано»."))), actual, "E-en:tense.contrast-A")
        }
        run { // E-en:tense.contrast-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:tense.contrast", null)
            assertEquals(Exercise("generated", "en:tense.contrast", "I am reading a new book.", "Скажи, что ты уже прочитал эту книгу — результат сейчас есть, а не процесс. Начни с «I have…».", "I have read a new book.", listOf("I've read a new book."), "I read a new book (Simple — привычка/факт вообще) → I am reading a new book (Continuous — процесс, идёт сейчас) → I have read a new book (Perfect — результат уже есть, важен для настоящего момента). Одно и то же событие можно описать тремя разными «временами»: разница не в моменте времени, а в том, что говорящий хочет подчеркнуть.", listOf("tense", "contrast"), "house", "new", PossessiveId.fromId("my"), NumberGram.fromId("sg"), listOf(FormChange("am reading", "have read", "Continuous (процесс) → Perfect (результат, значимый сейчас): подчёркиваем не «что происходит», а «что уже сделано»."))), actual, "E-en:tense.contrast-B")
        }
        run { // E-en:mixed-A
            val factory = factory(listOf(0.34, 0.72, 0.18))
            val actual = factory.generateForSkill("en:mixed", null)
            assertEquals(Exercise("generated", "en:mixed", "I see my good dog.", "Замени «мой» на «наш / наша». Сохрани прошедшее время предложения.", "I saw our good dog.", listOf(), "Здесь два изменения сразу: время глагола и притяжательное местоимение; каждое меняется по своему собственному правилу, независимо от другого.", listOf("mixed", "past", "agreement"), "dog", "good", PossessiveId.fromId("our"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее."), FormChange("my good dog", "our good dog", "Меняется владелец; остальная форма существительного и прилагательного не меняется."))), actual, "E-en:mixed-A")
        }
        run { // E-en:mixed-B
            val factory = factory(listOf(0.81, 0.07, 0.55))
            val actual = factory.generateForSkill("en:mixed", null)
            assertEquals(Exercise("generated", "en:mixed", "I see my new phone.", "Замени «мой» на «твой / твоя». Сохрани прошедшее время предложения.", "I saw your new phone.", listOf(), "Здесь два изменения сразу: время глагола и притяжательное местоимение; каждое меняется по своему собственному правилу, независимо от другого.", listOf("mixed", "past", "agreement"), "phone", "new", PossessiveId.fromId("your"), NumberGram.fromId("sg"), listOf(FormChange("see", "saw", "Меняется время глагола: настоящее → прошедшее."), FormChange("my new phone", "your new phone", "Меняется владелец; остальная форма существительного и прилагательного не меняется."))), actual, "E-en:mixed-B")
        }
        // --- GENERATED END ---
    }
}
