# Audio Cloze Mode: консолидированный дизайн

Дата: 2026-11-15. Статус: **реализован**.

Результат параллельной работы 4 архитекторов, консолидирован координатором.

## 0. Обзор и ключевые решения

### Что это

Новый тренировочный режим: пользователь слышит аудио-фрагмент экзамена и одновременно заполняет пропуск в предложении (cloze). Цель — закрепление языка через комбинацию аудио + gap-fill.

### Разрешённые противоречия между архитекторами

| Вопрос | Архитектор 1 (Data Model) | Архитектор 2 (Engine) | Решение |
|---|---|---|---|
| Нужен ли новый `.data-contract`? | Да, `audio-cloze.data-contract` с `AudioClozeItemRow` | Нет, данные уже есть в `ListeningQuestion.ChooseForm` | **Нет** для Phase 1. Существующие `ChooseForm` вопросы уже содержат предложение с `___`, правильный ответ и дистракторы. Новый контракт нужен только для Phase 2 (transcript-based). |
| Нужна ли морфология? | `AudioClozeGenerator` с `MorphologyParadigm` | Нет, авторские дистракторы лучше | **Нет**. Авторские экзаменационные дистракторы качественнее, и это делает генератор язык-агностичным. |
| `AudioClozeExercise` оборачивает `ClozeExercise`? | Композиция `AudioClozeExercise(cloze, audio)` | Плоский `AudioClozeExercise` со всеми полями | **Плоский**. Избегает лишней вложенности; `ClozeExercise` и `AudioClozeExercise` живут рядом, но не связаны типом. |

### Согласованные решения (все 4 архитектора)

1. **Отдельный `AudioClozeExercise`**, не расширение `ClozeExercise` — разные контракты, нет nullable audio полей
2. **Отдельный `AudioClozeSessionState`** с `replayCount` и собственным reducer
3. **Новый `AppTab.AudioCloze`**, не переиспользование `Cloze`
4. **Audio playback — host-owned** (shared код не трогает медиаплеер)
5. **Нет эффекта на FSRS** — это listening drill, не оценка навыка
6. **`AudioSegment(audioUrl, startMs, endMs)`** — единственный новый shared тип в `commonMain`
7. **`WebAudioSegmentPlayer`** извлекается из `ExamListeningController` (DRY)
8. **Нет абстрактного интерфейса плеера** пока нет 3 хостов
9. **Auto-play при загрузке карточки** — ценность режима в «услышал → заполнил»
10. **Универсальность** — ни одна строка не зависит от польского; любой `ListeningExam` работает

### Фазы

- **Phase 1 (реализовано):** Transcript-based cloze — split transcript на предложения, gap из контентного слова, дистракторы из того же текста. 50 tasks с transcript (B2+C1) → сотни упражнений.
- **Phase 2 (реализовано):** Выбор экзамена — пользователь выбирает B1/B2/C1 на экране-пикере перед стартом сессии. `DefaultAudioClozePool.drawFrom(examRouteId)`.
- **Phase 3 (зарезервировано):** `AudioClozeSource.Custom` — будущие диалоги, тексты, пользовательские аудио-материалы.

---

## 1. Data Model

### Новые DSL контракты

**Phase 1: новый контракт не нужен.** Все данные уже есть:
- Предложение с `___` → `ListeningQuestion.questionText`
- Правильный ответ → `ListeningQuestion.correctAnswer`
- Дистракторы → `ListeningQuestion.options`
- Аудио-сегмент → `ListeningTask.startMs/endMs` + `ListeningExam.audioUrl`

**Phase 2 (отложено):** при необходимости — `dsl/audio-cloze.data-contract` с `AudioClozeItemRow(taskRef, gapWord, gapLexemeKey)` для авторских пропусков в transcript-based режиме.

### Shared data class — `AudioSegment`

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/audio/AudioSegment.kt  (NEW)
package polski.audio

data class AudioSegment(
    val audioUrl: String,
    val startMs: Long,
    val endMs: Long,   // 0 means "play to end of track"
)

fun polski.exam.listening.ListeningTask.toAudioSegment(audioUrl: String): AudioSegment =
    AudioSegment(audioUrl, startMs, endMs)
```

Один факт, одно место. `ListeningTask` уже несёт `startMs/endMs/audioUrl`; extension function конвертирует.

---

## 2. Engine & Session Logic

### Public contract

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/AudioClozeExercise.kt  (NEW)
package polski.cloze

data class AudioClozeExercise(
    val id: String,
    val displaySentence: String,   // sentence with "___" gap
    val correctForm: String,
    val options: List<String>,     // exactly 3, shuffled, includes correctForm
    val audioUrl: String,
    val startMs: Long,
    val endMs: Long,
    val source: AudioClozeSource,
    val gapLexemeKey: String,      // "listening:<questionId>" for analytics
) {
    init {
        require(correctForm in options)
        require(options.size == 3)
        require(options.distinct().size == 3)
    }
}

sealed interface AudioClozeSource {
    data class ListeningQuestion(val examRouteId: String, val taskId: String, val questionId: String) : AudioClozeSource
    data class ListeningTranscript(val examRouteId: String, val taskId: String) : AudioClozeSource  // Phase 2
}
```

### Generator

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/AudioClozeGenerator.kt  (NEW)
package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.QuestionType
import polski.exam.listening.ListeningExam

class AudioClozeGenerator(private val random: RandomSource) {

    fun fromListeningQuestion(
        examRouteId: String,
        task: polski.exam.listening.ListeningTask,
        question: polski.exam.listening.ListeningQuestion,
        audioUrl: String,
    ): AudioClozeExercise? {
        if (question.type != QuestionType.ChooseForm) return null
        if (!question.questionText.contains("___")) return null
        val correct = question.correctAnswer
        val distractors = question.options.filter { it != correct }
        if (distractors.size < 2) return null
        val three = listOf(correct) + distractors.take(2)
        return AudioClozeExercise(
            id = "listening:${question.id}",
            displaySentence = question.questionText,
            correctForm = correct,
            options = shuffled(three),
            audioUrl = audioUrl,
            startMs = task.startMs,
            endMs = task.endMs,
            source = AudioClozeSource.ListeningQuestion(examRouteId, task.id, question.id),
            gapLexemeKey = "listening:${question.id}",
        )
    }

    fun fromExam(examRouteId: String, exam: ListeningExam): List<AudioClozeExercise> =
        exam.tasks.flatMap { task ->
            task.questions.mapNotNull { q ->
                fromListeningQuestion(examRouteId, task, q, exam.audioUrl)
            }
        }

    private fun shuffled(items: List<String>): List<String> {
        val pool = items.toMutableList()
        for (i in pool.lastIndex downTo 1) {
            val j = (random.nextDouble() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp
        }
        return pool
    }
}
```

Нет зависимости от `MorphologyParadigm` — полностью язык-агностичен.

### Pool

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/AudioClozePool.kt  (NEW)
package polski.cloze

import polski.core.engine.RandomSource
import polski.exam.listening.ListeningExam

fun interface AudioClozePool {
    fun draw(limit: Int, random: RandomSource): List<AudioClozeExercise>
}

class DefaultAudioClozePool(
    listeningExams: Map<String, ListeningExam>,
    generator: AudioClozeGenerator,
) : AudioClozePool {
    private val candidates: List<AudioClozeExercise> by lazy {
        listeningExams.flatMap { (routeId, exam) -> generator.fromExam(routeId, exam) }
    }

    override fun draw(limit: Int, random: RandomSource): List<AudioClozeExercise> {
        if (candidates.isEmpty()) return emptyList()
        val pool = candidates.toMutableList()
        for (i in pool.lastIndex downTo 1) {
            val j = (random.nextDouble() * (i + 1)).toInt().coerceIn(0, i)
            val tmp = pool[i]; pool[i] = pool[j]; pool[j] = tmp
        }
        return pool.take(limit)
    }
}
```

### Session (pure reducer)

```kotlin
// kotlin/shared/src/commonMain/kotlin/polski/cloze/AudioClozeSession.kt  (NEW)
package polski.cloze

data class AudioClozeSessionState(
    val exercises: List<AudioClozeExercise>,
    val currentIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val replayCount: Map<String, Int> = emptyMap(),
)

sealed interface AudioClozeSessionAction {
    data class Choose(val id: String, val form: String) : AudioClozeSessionAction
    data class RecordReplay(val id: String) : AudioClozeSessionAction
    data object Next : AudioClozeSessionAction
    data object Previous : AudioClozeSessionAction
    data object Restart : AudioClozeSessionAction
}

fun reduceAudioClozeSession(
    state: AudioClozeSessionState,
    action: AudioClozeSessionAction,
): AudioClozeSessionState = when (action) {
    is AudioClozeSessionAction.Choose ->
        if (state.answers.containsKey(action.id)) state
        else state.copy(answers = state.answers + (action.id to action.form))
    is AudioClozeSessionAction.RecordReplay ->
        state.copy(replayCount = state.replayCount + (action.id to (state.replayCount[action.id] ?: 0) + 1))
    AudioClozeSessionAction.Next ->
        if (state.currentIndex >= state.exercises.lastIndex) state
        else state.copy(currentIndex = state.currentIndex + 1)
    AudioClozeSessionAction.Previous ->
        if (state.currentIndex <= 0) state
        else state.copy(currentIndex = state.currentIndex - 1)
    AudioClozeSessionAction.Restart -> state.copy(currentIndex = 0, answers = emptyMap(), replayCount = emptyMap())
}
```

### Integration с TrainingStore

```kotlin
// AppUiState.kt  (EDIT)
val audioCloze: AudioClozeSessionState? = null,

// AppTab  (EDIT)
enum class AppTab { Training, Vocabulary, Matrix, Progress, Cloze, AudioCloze }

// AppAction  (EDIT)
data class StartAudioCloze(val limit: Int = 10) : AppAction
data class AudioClozeAct(val action: AudioClozeSessionAction) : AppAction
```

`TrainingStore` получает `audioClozePool: AudioClozePool by lazy { ... }` из `officialListeningData`.

### Универсальность

| Что | Откуда | Зависит от языка? |
|---|---|---|
| Предложение с gap | `ListeningQuestion.questionText` | Нет |
| Правильный ответ | `ListeningQuestion.correctAnswer` | Нет |
| Дистракторы | `ListeningQuestion.options` | Нет |
| Аудио-сегмент | `ListeningTask.startMs/endMs` | Нет |
| Аудио URL | `ListeningExam.audioUrl` | Нет |

Английский или русский `ListeningExam` → те же `AudioClozeExercise` без изменений кода.

---

## 3. Audio Player DRY & Cross-Platform

### Web: извлечение `WebAudioSegmentPlayer`

Новый файл `kotlin/composeApp/src/webMain/kotlin/polski/ui/WebAudioSegmentPlayer.kt`.

Извлекает из `ExamListeningController` всю логику `HTMLAudioElement`:
- `audioElement`, `playButton`, `timeLabel`, `progressFill`, `mediaError`
- `currentSegmentStart/End`, `playRequested/RequestId/InFlight`, `replaySeekPending`, `playbackError`
- Методы: `setup(segment)`, `seekTo(segment)`, `buildWidget()`, `dispose()`, `cancelPlayback()`

```kotlin
internal class WebAudioSegmentPlayer {
    fun setup(segment: AudioSegment)       // создаёт/переиспользует HTMLAudioElement
    fun seekTo(segment: AudioSegment)      // seek к startMs, сброс play state
    fun buildWidget(): HTMLElement          // возвращает DOM-виджет плеера
    fun dispose()                          // pause + release element
    fun cancelPlayback()                   // cancel intent без release (tab hidden)
}
```

**URL switching**: `setup()` проверяет `audioUrl != currentUrl`. Новый URL → `dispose()` + новый element с `preload="auto"`. Тот же URL → только `seekTo`.

### Рефакторинг `ExamListeningController`

```kotlin
// ExamListeningWeb.kt  (EDIT)
-  private var audioElement: HTMLAudioElement? = null
-  private var playButton: HTMLButtonElement? = null
-  // ... ~15 полей удаляются
+  private val player = WebAudioSegmentPlayer()

// вызовы:
-  setupAudio()           → player.setup(segment)
-  seekToTask(task)       → player.seekTo(task.toAudioSegment(exam.audioUrl))
-  buildCustomPlayer(task) → player.buildWidget()
-  disposeAudio()         → player.dispose()
-  cancelPlayback()       → player.cancelPlayback()
```

`ExamListeningController` уменьшается на ~100 строк. Существующие Playwright тесты экзамена должны пройти без изменений.

### iOS: AVPlayer в SwiftUI host

Snapshot добавляет `audioUrl/startMs/endMs` в JSON. Swift читает и строит `AVPlayer` с `addBoundaryTimeObserver` на `endMs`. Нет нового Kotlin класса.

### Android: ExoPlayer (отложено)

`ClippingMediaSource(startUs = startMs * 1000, endUs = endMs * 1000)`. Отдельный класс `AndroidAudioSegmentPlayer` — без forced абстракции до 3 хостов.

### Lifecycle

| Событие | Web | iOS | Android |
|---|---|---|---|
| Экран открыт | `player.setup(segment)` | AVPlayer создан | ExoPlayer создан |
| Смена упражнения | `player.seekTo(new)` | seek AVPlayer | seek ExoPlayer |
| Смена URL | dispose + новый element | новый AVPlayerItem | новый MediaItem |
| Tab hidden | `player.cancelPlayback()` | auto-pause | lifecycle |
| Экран закрыт | `player.dispose()` | release AVPlayer | release ExoPlayer |

---

## 4. UI & Host Integration

### Entry point

Tab `AudioCloze` виден только когда `audioClozePool.draw(1, random).isNotEmpty()`. Перепроверяется при смене языкового пакета.

### Web UI

Новый `AudioClozeWebController` в `AudioClozeWeb.kt`:

```
<div class="audio-cloze-player">      ← sticky audio widget
  <button>▶ / ⏸</button>
  <div class="progress-bar"><div class="fill" /></div>
  <span class="time">0:00 / 0:04</span>
  <button>↺ Replay</button>
</div>
<div class="exam-progress">1 / 10</div>
<div class="exam-context">             ← предложение с gap
  Moja siostra ___ do sklepu.
</div>
<div class="exam-options">             ← 3 кнопки
  A. poszła   B. pójdzie   C. idzie
</div>
<div class="exam-feedback">...</div>   ← correct/wrong
<div class="exam-nav">← → Restart</div>
```

Auto-play при загрузке карточки. Auto-advance 1.5с после верного ответа. При ошибке — кнопка «Переслушать».

### iOS

`IosSnapshot.kt` → `audioClozeSnapshot()` + `audioClozeAvailable` flag.
`IosSession.kt` → 6 bridge-методов: `startAudioCloze`, `chooseAudioCloze`, `audioClozeNext/Prev/Restart/Replay`.

SwiftUI: `AudioClozeView` → `AudioSegmentPlayerView` (AVPlayer) + `ClozeCardView`.

### Android

Новый `AndroidAudioClozeScreen.kt`:
- `AudioSegmentPlayerRow` — ExoPlayer + play/pause/progress/replay
- Sentence + options + feedback + nav

### UiCopy (DSL)

7 новых ключей в `dsl/ui-copy.declare`:
```
audioClozeTab, audioClozeTitle, audioClozePlay, audioClozePause,
audioClozeReplay, audioClozeNoContent, audioClozeReplayPrompt
```

### Navigation flow

| Триггер | Действие | Аудио |
|---|---|---|
| Вход в таб | `StartAudioCloze(10)` | idle |
| Карточка загружена | auto-seek + auto-play | playing |
| Тап на опцию | `Choose(id, form)` | продолжает |
| Верный ответ + 1.5с | `Next` | pause → seek next |
| Replay тап | `RecordReplay(id)` + re-seek | plays from startMs |
| Все отвечены | results screen inline | idle |
| Restart | `Restart` | first card auto-plays |
| Выход из таба | host: pause/release | silent |

---

## 5. Verification

### Unit tests (commonTest) — 10 cases

| Test | Expected |
|---|---|
| Generator skips non-ChooseForm | null |
| Generator skips missing `___` | null |
| Generator skips < 2 distractors | null |
| Generator happy path | correct AudioClozeExercise; correctForm in options; size == 3 |
| Pool.draw returns subset | limit < candidates → limit exercises |
| Pool.draw returns all when limit > size | all candidates |
| Reducer Choose idempotent | second Choose → state unchanged |
| Reducer RecordReplay increments | 2 replays → count == 2 |
| Reducer Restart clears all | answers empty, replays empty, index 0 |
| AudioClozeExercise init rejects wrong options | throws |

### Integration test (commonTest)

`DefaultAudioClozePool(officialListeningData, generator).draw(10, random).isNotEmpty()` — pipeline end-to-end.

### Web player tests (Playwright) — 10 cases

V1–V4: `WebAudioSegmentPlayer` (seek identity, URL reinit, boundary clamp, dispose)
V5: player widget above gap sentence (DOM order)
V6: `toAudioSegment()` (commonTest)
V7: iOS snapshot JSON
V8–V10: URL switching, visibility cancel, accessibility labels

---

## 6. Consolidated Implementation Plan

### Phase A: Shared domain (commonMain) — 1 задача

1. `AudioSegment.kt` + `toAudioSegment()` extension
2. `AudioClozeExercise.kt` + `AudioClozeSource`
3. `AudioClozeGenerator.kt`
4. `AudioClozePool.kt`
5. `AudioClozeSession.kt` (state + reducer)
6. `AppUiState` += `audioCloze`, `AppTab` += `AudioCloze`, `AppAction` += 2 actions
7. `TrainingStore` += `audioClozePool` + dispatch handlers
8. 10 unit tests (commonTest)

### Phase B: Web audio DRY — 1 задача

1. Извлечь `WebAudioSegmentPlayer.kt` из `ExamListeningController`
2. Рефакторинг `ExamListeningController` → delegate to `player`
3. Проверить Playwright exam audio тесты

### Phase C: Web UI — 1 задача

1. `dsl/ui-copy.declare` += 7 keys → `biome generate`
2. `WebRouteController.kt` += `AudioCloze` route
3. `AudioClozeWeb.kt` (NEW) — controller + entry function
4. `TrainingDomRenderer.kt` — wiring
5. Playwright smoke test

### Phase D: iOS — 1 задача

1. `IosSnapshot.kt` += `audioClozeSnapshot` + `audioClozeAvailable`
2. `IosSession.kt` += 6 bridge methods
3. SwiftUI: `AudioSegmentPlayerView` + `AudioClozeView`
4. XCUITest

### Phase E: Android — 1 задача

1. `AndroidAudioClozeScreen.kt` + `AudioSegmentPlayerRow`
2. `AndroidContent.kt` += `AppTab.AudioCloze` branch
3. UI test

### Порядок: A → B → C → D (параллельно с E после A)

---

## 7. ADR записи

Записать в `AI/decisions.md`:
- Audio Cloze не влияет на FSRS прогресс (listening drill, не оценка)
- Audio playback — host-owned, shared код не знает о медиаплеере
- Auto-play при загрузке карточки; replay — явное действие хоста
- Phase 1: только `ChooseForm` из listening; Phase 2: transcript-based (отложено)
- Новый `audio-cloze.data-contract` не нужен для Phase 1
