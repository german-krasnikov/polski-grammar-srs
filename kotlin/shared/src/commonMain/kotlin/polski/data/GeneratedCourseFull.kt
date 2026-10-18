// Generated from courses-biome/pl-ru/; do not edit.
// reference SHA-256: 3306d03cefeff36a9fc193c675be3624f5305ac741e6315067b395b217433dff
package polski.data

import polski.model.GramCase
import polski.model.Gender
import polski.model.NumberGram
import polski.model.Person
import polski.model.Tense
import polski.presentation.StemAlternation

// --- chainPresentation ---
internal val generatedPlRuChainPresentation = ChainPresentation(
    listOf(ChainStep("acc", "Вижу"), ChainStep("past", "Прошлое"), ChainStep("neg", "Отрицание"), ChainStep("owner", "Владелец"), ChainStep("loc", "Говорю о")), ChainCompletion("Цепочка завершена", "5 преобразований", "Ты изменил время, отрицание, владельца и падеж, сохранив одну мысль. Оценки сохранены в расписании повторений.", "Пять преобразований завершены. Оценки сохранены в расписании повторений."))

// --- stemAlternations ---
internal val generatedPlRuStemAlternations: List<StemAlternation> =
    listOf(StemAlternation('ó', 'o'))

// --- reference simple strings ---
internal val generatedPlRuMatrixIntroduction = "Один небольшой словарь. Видно, что меняется при каждой операции."
internal val generatedPlRuWebCaseCompositionHeader = "Местоимение + прилагательное + существительное"
internal val generatedPlRuMaleAccIntro = "Для единственного числа сначала определи тип существительного."
internal val generatedPlRuComparisonNounIds: List<String> = listOf("husband", "friendM", "dog", "house", "wife", "book", "child")
internal val generatedPlRuContextHelp = CourseContextHelp("Красным показана старая часть, золотым — новая. Местоимение и прилагательное согласуются с существительным.", "Местоимение и прилагательное согласуются с существительным. jego, jej, ich не изменяются.")
internal val generatedPlRuAspectNoPresent = CourseAspectNoPresent("Нет настоящего времени", "Настоящее: нет настоящего времени")
internal val generatedPlRuVocabularyInstructions = CourseVocabularyInstructions("Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера видны в списке, но пока не добавляются в тренировки. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.", "Частотный ранг не равен уровню CEFR. Слова без проверенного перевода и примера пока не добавляются в тренировки. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.", "Частотный ранг не равен уровню CEFR. Без проверенного перевода и примера слово пока недоступно. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.", "Частотный ранг не равен уровню CEFR. Непроверенные слова пока недоступны. Исходные 32 карточки прошли языковую проверку; метки A1/A2 — локальные группы, не официальная сертификация CEFR.")
internal val generatedPlRuVocabularyUnavailableLabel = "Перевод и пример ещё не проверены"

// --- reference rows ---
internal val generatedPlRuReferenceChainRows: List<ReferenceChainRow> = listOf(ReferenceChainRow("База", "To jest moja piękna żona.", "Widzę moją piękną żonę.", "Biernik всей группы"), ReferenceChainRow("Прошедшее", "Widzę moją piękną żonę.", "Widziałem moją piękną żonę.", "widzę → widziałem"), ReferenceChainRow("Отрицание", "Widziałem moją piękną żonę.", "Nie widziałem mojej pięknej żony.", "вся группа: Biernik → Dopełniacz"), ReferenceChainRow("Их вместо моей", "Nie widziałem mojej pięknej żony.", "Nie widziałem ich pięknej żony.", "mojej → ich; ich не склоняется"), ReferenceChainRow("Говорю о…", "Nie widziałem ich pięknej żony.", "Mówię o ich pięknej żonie.", "o → Miejscownik"))
internal val generatedPlRuReferenceTenseRows: List<ReferenceTenseRow> = listOf(ReferenceTenseRow("Сейчас", "Moja piękna żona idzie do domu.", "Moja piękna żona idzie do domu."), ReferenceTenseRow("Прошедшее", "Moja piękna żona idzie do domu.", "Moja piękna żona szła do domu."), ReferenceTenseRow("Будущее · процесс", "Moja piękna żona szła do domu.", "Moja piękna żona będzie szła do domu."), ReferenceTenseRow("Отрицание", "Moja piękna żona idzie do domu.", "Moja piękna żona nie idzie do domu."), ReferenceTenseRow("Вопрос", "Moja piękna żona idzie do domu.", "Czy moja piękna żona idzie do domu?"))
internal val generatedPlRuReferenceAspectRows: List<ReferenceAspectRow> = listOf(ReferenceAspectRow("robić · процесс", "robić", "robię", "robiłem / robiłam", "będę robić"), ReferenceAspectRow("zrobić · результат", "zrobić", null, "zrobiłem / zrobiłam", "zrobię"), ReferenceAspectRow("kupować · процесс", "kupować", "kupuję", "kupowałem / kupowałam", "będę kupować"), ReferenceAspectRow("kupić · результат", "kupić", null, "kupiłem / kupiłam", "kupię"))
internal val generatedPlRuMaleAccRows: List<MaleAccRow> = listOf(MaleAccRow("person", "Человек", "mąż / kolega", listOf(MaleAccExample("mój dobry mąż", "mojego dobrego męża", "Widzę mojego dobrego męża."), MaleAccExample("mój dobry kolega", "mojego dobrego kolegę", "Widzę mojego dobrego kolegę.")), "На согласную: как Gen. На -a: -ę у существительного, но -ego у местоимения и прилагательного."), MaleAccRow("animal", "Животное", "pies / kot", listOf(MaleAccExample("mój dobry pies", "mojego dobrego psa", "Widzę mojego dobrego psa.")), "Винительный как родительный: pies → psa."), MaleAccRow("object", "Предмет", "dom / samochód", listOf(MaleAccExample("mój nowy samochód", "mój nowy samochód", "Widzę mój nowy samochód.")), "Винительный как именительный: группа сохраняет базовую форму."))
internal val generatedPlRuReferenceSystemCards: List<ReferenceSystemCard> = listOf(ReferenceSystemCard("noun", "Существительное", "Род × число × падеж", "żona → żonę → żony", listOf("żona", "żonę", "żony")), ReferenceSystemCard("agreement", "Прилагательное и владелец", "Копируют род, число и падеж", "moja piękna → moją piękną", listOf("moja piękna", "moją piękną")), ReferenceSystemCard("verb", "Глагол", "Лицо × число × время × вид", "widzę → widziałem → będę widzieć", listOf("widzę", "widziałem", "będę widzieć")), ReferenceSystemCard("modifiers", "Модификаторы", "Отрицание · вопрос · владелец", "Widzę… → Nie widzę… → Czy widzę…?", listOf("Widzę…", "Nie widzę…", "Czy widzę…?")))
internal val generatedPlRuReferencePipeline: ReferencePipeline = ReferencePipeline("Сначала конструкция, затем формы", listOf(ReferencePipelineStep("intent", "01 · Смысл", "Что хочу сказать?", "Вижу / не вижу / говорю о…"), ReferencePipelineStep("case", "02 · Операция", "Какой падеж нужен?", "widzę → Biernik"), ReferencePipelineStep("agreement", "03 · Согласование", "Меняю всю группу", "moją + piękną + żonę")), "widzę → Biernik → moją + piękną + żonę")
