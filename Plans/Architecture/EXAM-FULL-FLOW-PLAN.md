# Полный flow экзамена Certyfikat Polski: план

Дата: 2026-11-08. Дополняет `Plans/Architecture/EXAM-MODULES-PLAN.md` (исходный план модулей)
данными реальной структуры B1/B2 (25/45/45/45 мин, 30+30+30+30 пунктов) и моделью
последовательного экзамена из 4 частей. Readonly-аудит existing кода, без реализации.

## Аудит текущего состояния (важно для решения о последовательности работы)

Проверено по факту (не по старому плану):

- `kotlin/shared/src/commonMain/kotlin/polski/exam/` содержит **только** домен Grammar:
  `ExamModel.kt` (ExamId, ExamModule, QuestionType, WritingGenre, AudioSegment) и
  `grammar/{GrammarExamModel,GrammarExamSession,GrammarExamScorer,GrammarExamCardModel}.kt`.
  Паттерн: `*Model.kt` — чистые data-классы; `*Session.kt` — State+sealed Action+reducer;
  `*Scorer.kt` — чистая функция score (нормализация через `polski.grammar.normalize`);
  `*CardModel.kt` — презентационная проекция state→card.
- **Reading, Listening, Writing домена в shared НЕТ.** Вся их логика (состояние ответов,
  скоринг, пагинация) живёт прямо в `kotlin/composeApp/src/webMain/kotlin/polski/ui/
  Exam{Reading,Listening,Writing}Web.kt` как приватные `data class` + мутабельные поля
  контроллера. Это не портируется на Android/iOS/Desktop и будет не нужно, если
  ветки `arch-exam-listening`/`arch-exam-reading` в этой же сессии уже выносят эту
  логику в shared — **координировать с ними, не дублировать**.
- DSL (`dsl/exam.data-contract`) уже покрывает multiple_choice/true_false_not_given/
  fill_gap/match/match_heading/choose_form/transform/word_formation, но реальные
  `.core-data` файлы для B1-2019 используют только `choose_form` и `fill_gap` —
  grammar-5/6 (zadać pytanie / sparafrazować, типа `transform`), grammar-2/8
  (word bank) закодированы как `fill_gap`, теряя тип вопроса. Reading-3/4/5
  (gap_fill_fragment, matching headings, word bank) не имеют заполненных `text`/
  `options` — это пробел в данных, не в контракте; не в скоупе этого плана
  (relevant agents: `fix-exam-data`, `import-all-exams`).
- `points: int` везде. Słuchanie Zad I (7×0.5) и Zad V (5×0.5) реально весят
  половину пункта за вопрос — текущий тип не может выразить это без изменения
  контракта (DSL не поддерживает `double`/`float`, проверено по всем
  `dsl/*.data-contract`).

## 1. Расширение DSL `dsl/exam.data-contract`

Минимальные, обратно совместимые изменения — ни один существующий `.core-data`
файл не ломается (новые поля/значения enum добавляются как OPTIONAL или новые
варианты enum).

```
# --- questionType: добавить варианты, не менять существующие ---
GrammarItemRow.questionType: enum choose_form|fill_gap|transform|word_formation|word_bank
ReadingQuestionRow.questionType: enum multiple_choice|true_false_not_given|match_heading|matching|fill_gap|short_answer|word_bank|gap_fill_fragment
ListeningQuestionRow.questionType: enum multiple_choice|true_false|fill_gap|match|matching|word_bank

# --- гибкий скоринг: несколько допустимых ответов через "|" ---
# correctAnswer уже string — конвенция: "Gdzie byłeś?|Dokąd poszedłeś?" = любой из вариантов
# принимается как верный. Меняется только scorer (см. раздел 3), не контракт.

# --- вес пункта для частей с 0.5 pt/вопрос (Słuchanie I, V) ---
# Новое OPTIONAL поле на уровне task/passage/test row, не item row —
# применяется к всем вопросам этого task. Default 10 = ×1.0 (без изменений
# для существующих данных, где поле не указано).
ListeningTaskRow.pointsPerItemX10: int OPTIONAL   # 5 = ×0.5, 10 = ×1.0 (default)
ReadingPassageRow.pointsPerItemX10: int OPTIONAL
GrammarTestRow.pointsPerItemX10: int OPTIONAL

# --- таймер и бюджет баллов на часть экзамена (новый RECORD) ---
RECORD ExamModuleRow KEY (examRef, module) COUNT 1..
  examRef: ref ExamRow
  module: enum listening|reading|grammar|writing
  timerMinutes: int
  totalPoints: int
END
```

Конвенции без изменения схемы (документируются в контракте комментарием, не
требуют новых полей):

- **word_bank**: `options` = весь банк слов/фраз для task (повторяется в каждой
  question row этого task), `correctAnswer` = верное слово для этого пропуска.
  "Использованные" слова вычисляются на уровне card model (см. §2), не хранятся.
- **matching**: та же механика, что word_bank, но `options` — абстрактные метки
  (A–E, имена людей, заголовки) вместо слов. Единый `matching` тип закрывает
  Słuchanie Zad IV (person↔statement) и Zad V (text↔illustration), Czytanie
  Zad IV (heading↔text). Листенинговый `match` оставлен для обратной
  совместимости существующих данных, не удаляется.
- **gap_fill_fragment** (Czytanie Zad III, A–I): `text` содержит пронумерованные
  маркеры `{{1}}`, `{{2}}`, …; одна `ReadingQuestionRow` на каждый номер,
  `options` = полный банк фрагментов `"A. ...|B. ...|C. ..."`, `correctAnswer`
  = буква (`"C"`).

Почему не новые RECORD-типы на каждый случай: word_bank/matching/gap_fill_fragment
различаются только тем, *что* лежит в уже существующих string-полях `text`/
`options`/`correctAnswer`, а не структурой данных — отдельные записи добавили бы
дублирующиеся JOIN-таблицы без выигрыша в типобезопасности (DSL всё равно хранит
options как pipe-separated string). Единственное реальное структурное отличие —
вес пункта и таймер/бюджет модуля, им и добавлены новые поля/record.

## 2. Домен: промоция Reading/Listening/Writing в shared + генерализация скоринга

**Зависит от /координируется с `arch-exam-listening`/`arch-exam-reading`** —
если эта работа уже идёт параллельно, не дублировать, а свести интерфейс к этому
контракту (ниже — требуемая минимальная форма state, которую должен предоставить
каждый модуль, чтобы `ExamSession` (§3) мог их компоновать).

NEW, по образцу `grammar/` (файл-на-файл):

```
kotlin/shared/src/commonMain/kotlin/polski/exam/
  reading/
    ReadingExamModel.kt      # ReadingPassage, ReadingQuestion (ports existing
                              # webMain private data classes, now shared)
    ReadingExamSession.kt    # ReadingExamState(passages, currentIndex, answers,
                              # submitted, result) + Action + reducer
    ReadingExamScorer.kt     # score(question, answer): Boolean + scoreAttempt(...)
    ReadingExamCardModel.kt  # card: passage + its questions + per-question
                              # usedOptions (derived, see §1 word_bank/matching)
  listening/
    ListeningExamModel.kt    # ListeningTask, ListeningQuestion (+ reuse
                              # ExamModel.AudioSegment)
    ListeningExamSession.kt  # ListeningExamState(tasks, currentIndex, answers,
                              # submitted, result, audio: AudioPlayerState)
    ListeningExamScorer.kt
    ListeningExamCardModel.kt
  writing/
    WritingExamModel.kt      # WritingTask (ports existing)
    WritingExamSession.kt    # WritingExamState(tasks, currentIndex, drafts,
                              # submitted) — no scorer: human-graded (see §4 open Q)
    WritingExamCardModel.kt  # word count vs wordLimit, progress
```

**Required minimal shape** (the actual contract `ExamSession` composes against):
each module's `*ExamState` exposes `currentIndex: Int`, `answers: Map<String, String>`
(or `drafts` for writing), `submitted: Boolean`; each module's reducer is a pure
`(State, Action) -> State`. This mirrors `GrammarExamState`/`reduceGrammarExam`
exactly — no new abstraction needed, just the same shape repeated three times.

Scorer generalization (small, shared change — touches all four scorers the same
way): `correctAnswer` may be `"a|b"` (pipe-separated accepted alternatives).
`fun score(correct: String, answer: String) = normalize(answer) in
 correct.split("|").map(::normalize)`. Backward compatible: single-value
`correctAnswer` behaves exactly as today (membership in a 1-element set = equality).

## 3. `ExamSession`: весь экзамен, 4 части последовательно

NEW `kotlin/shared/src/commonMain/kotlin/polski/exam/session/`:

```kotlin
// ExamSessionModel.kt
data class TimerState(val totalSeconds: Int, val remainingSeconds: Int, val running: Boolean)

data class ExamSessionState(
    val examId: ExamId,
    val moduleConfigs: Map<ExamModule, ExamModuleConfig>,  // from ExamModuleRow
    val partOrder: List<ExamModule> = listOf(Listening, Reading, Grammar, Writing),
    val currentPartIndex: Int = 0,
    val timers: Map<ExamModule, TimerState> = emptyMap(),
    val listening: ListeningExamState,
    val reading: ReadingExamState,
    val grammar: GrammarExamState,
    val writing: WritingExamState,
    val completedParts: Set<ExamModule> = emptySet(),
    val finished: Boolean = false,
)

data class ExamModuleConfig(val timerMinutes: Int, val totalPoints: Int)

sealed interface ExamSessionAction {
    data object Start : ExamSessionAction          // enters partOrder[0], starts its timer
    data object Tick : ExamSessionAction            // -1s on current part's timer; auto-advances at 0
    data object FinishPartEarly : ExamSessionAction // user clicks "Zakończ część" before timer ends
    data class Listening(val action: ListeningExamAction) : ExamSessionAction
    data class Reading(val action: ReadingExamAction) : ExamSessionAction
    data class Grammar(val action: GrammarExamAction) : ExamSessionAction
    data class Writing(val action: WritingExamAction) : ExamSessionAction
}

// ExamSessionSession.kt: reduceExamSession(state, action): ExamSessionState
// - Tick/FinishPartEarly both route through advancePart(state), which marks
//   currentPart submitted (delegating to that module's own Submit action so its
//   own scorer runs), adds it to completedParts, and either moves
//   currentPartIndex++ & starts the next timer, or sets finished=true after Writing.
// - Listening/Reading/Grammar/Writing actions delegate to the matching child
//   reducer untouched — ExamSession never reimplements per-item logic.
```

```kotlin
// ExamSessionScorer.kt
data class ExamTotalResult(
    val listening: Int, val reading: Int, val grammar: Int,
    val autoGradedTotal: Int,       // listening+reading+grammar, max = sum of their totalPoints (90 for B1/B2)
    val writingSubmitted: Boolean,  // true once writing part finished — NOT scored (see open Q below)
    val maxAutoPoints: Int,
    val passThresholdAuto: Int,     // ceil(0.5 * (maxAutoPoints + writingMaxPoints)) minus writing's share — see open Q
)

fun scoreExamSession(state: ExamSessionState): ExamTotalResult
```

```kotlin
// ExamSessionCardModel.kt — presentation projection, like grammarExamCardModel
data class ExamSessionCard(
    val partLabel: String,          // "Część II: Czytanie"
    val partProgress: String,       // "Część 2 / 4"
    val remainingSeconds: Int,
    val currentPart: ExamModule,
)
fun examSessionCardModel(state: ExamSessionState): ExamSessionCard
```

Таймер: `ExamSession` не владеет реальным временем. Хост (web/Android/iOS/Desktop)
шлёт `ExamSessionAction.Tick` раз в секунду (setInterval / coroutine delay(1000) /
Compose `LaunchedEffect`) — reducer только декрементирует `remainingSeconds` и решает
про авто-переход. Это тот же паттерн, что `AnswerNormalizer`'s `expect fun`:
эффекты на платформе, чистая логика в common. Audio playback — аналогично: не
дизайню здесь `AudioPlayerController` expect/actual (платформенный адаптер,
решается на уровне kmp-web/kmp-android/kmp-ios/kmp-desktop skills при реализации),
но `ListeningExamState.audio: AudioPlayerState(isPlaying, positionMs, durationMs)`
— чистая презентационная модель play/pause/replay, которую хост просто отражает в
`<audio>`/`MediaPlayer`/`AVPlayer`.

## 4. Открытые вопросы (не решены мной — нужно решение пользователя/координатора)

1. **Pisanie не имеет авто-скоринга.** 30 из 120 пунктов — человеческая оценка.
   `ExamTotalResult` в этом плане считает только 90 (Listening+Reading+Grammar)
   автоматически и отмечает Writing как "submitted, not graded". Варианты для
   финального UI: (a) показать "90/90 авто + Pisanie оценивает экзаменатор",
   порог явно не вычисляется как единое число; (b) добавить self-assessment
   слайдер 0–30 для Pisanie (неточно, но даёт единое число "x/120"); (c) исключить
   Pisanie совсем из прогресса сдачи. Нужно явное решение — не предполагаю.
2. **Политика повтора аудио.** Задание просит play/pause/replay без ограничений;
   реальный экзамен обычно даёт запись дважды. Если нужен "экзаменационный режим"
   (лимит прослушиваний) — это отдельный флаг state, не добавлен, т.к. запрос не
   просил ограничения.
3. **Пересечение с активными ветками.** `arch-exam-listening`, `arch-exam-reading`,
   `arch-exam-grammar-test`, `build-exam-web-ui`, `build-full-exam-ui` в этой же
   сессии видимо работают над смежным кодом (web controllers только что
   модифицированы/добавлены). §2 этого плана (промоция Reading/Listening/Writing в
   shared) должен быть сверен с ними, чтобы не дублировать и не конфликтовать —
   главный агент должен решить порядок/владение, прежде чем это передавать на
   реализацию.

## 5. Верификация (на этапе реализации, не выполнена здесь)

- `GrammarExamScorerTest`/`GrammarExamSessionTest` (существуют,
  `kotlin/shared/src/commonTest/kotlin/polski/exam/grammar/`) — образец для
  новых `ReadingExamScorerTest`, `ListeningExamScorerTest`, `ExamSessionTest`.
- `ExamSessionTest` acceptance cases: (a) Tick doesn't move part until
  remainingSeconds hits 0; (b) hitting 0 auto-submits current part via its own
  Submit action (dirty/partial answers scored, not discarded); (c) FinishPartEarly
  equals natural timeout for scoring purposes; (d) partOrder is fixed
  Listening→Reading→Grammar→Writing; (e) `scoreExamSession` on an all-correct
  B1-2019 run returns autoGradedTotal == maxAutoPoints (90); (f) pipe-separated
  `correctAnswer` alternatives all score as correct in each of the four scorers.
- Browser parity: Playwright walk of the full 4-part flow once UI exists
  (`e2e-acceptance` memory note — finish with a real-scenario walk, not just
  per-task review).
