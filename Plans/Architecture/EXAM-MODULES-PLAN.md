# Экзаменационные модули: план

Дата: 2026-11-08. Синтез 4 архитектурных ревью: аудирование, чтение, грамматика+письмо, data pipeline.

## Цель

Универсальный модуль подготовки к языковым экзаменам:
- Certyfikat Polski (B1, B2, C1, B1 Dostosowany)
- IELTS (Academic, General Training) — в будущем
- Расширяемый для DELF, TestDaF и других

## Архитектура

### Новый Gradle модуль `:exam-data`

```
kotlin/exam-data/
  src/commonMain/kotlin/polski/exam/
    ExamModel.kt          — domain types (ExamId, Level, TaskType)
    listening/
      ListeningModel.kt   — domain: AudioSegment, ListeningTask, ListeningQuestion
      ListeningSession.kt — state + reducer
      ListeningCardModel.kt — pure presentation
    reading/
      ReadingModel.kt     — domain: Passage, ReadingQuestion
      ReadingSession.kt
      ReadingCardModel.kt
    grammar/
      GrammarModel.kt     — domain: GrammarItem, GrammarTest
      GrammarSession.kt
      GrammarScorer.kt    — uses polski.grammar.normalize
    writing/
      WritingModel.kt     — domain: WritingTask, genre
      WritingSession.kt
      WritingEvaluator.kt — reserved interface
```

Зависимости: `:exam-data` → `:core-model` (для normalize). Не зависит от `:shared`.

### DSL контракты

`dsl/exam.data-contract` — единый контракт для всех модулей:

```
DSL 1
RECORD ExamRow KEY examId COUNT 1..
  examId: string
  provider: string          # "certyfikat-polski", "ielts", "delf"
  targetLanguage: string    # "pl", "en", "fr", "de"
  level: string             # "B1", "B2", "C1", "academic", "general"
  year: int OPTIONAL
  sourceUrl: string OPTIONAL
  license: string OPTIONAL
END

# Listening
RECORD ListeningTaskRow KEY taskId COUNT 1..
  taskId: string
  examRef: ref ExamRow
  taskNumber: int
  audioUrl: string
  segmentStart: int OPTIONAL   # seconds
  segmentEnd: int OPTIONAL
  instruction: string
END

RECORD ListeningQuestionRow KEY questionId COUNT 1..
  questionId: string
  taskRef: ref ListeningTaskRow
  questionType: enum multiple_choice|true_false|fill_gap|match
  questionText: string
  options: string OPTIONAL     # pipe-separated for MC
  correctAnswer: string
  points: int
END

# Reading
RECORD ReadingPassageRow KEY passageId COUNT 1..
  passageId: string
  examRef: ref ExamRow
  taskNumber: int
  title: string OPTIONAL
  source: string OPTIONAL
  instruction: string
  text: string
END

RECORD ReadingQuestionRow KEY questionId COUNT 1..
  questionId: string
  passageRef: ref ReadingPassageRow
  questionType: enum multiple_choice|true_false_not_given|match_heading|fill_gap|short_answer
  questionText: string
  options: string OPTIONAL
  correctAnswer: string
  points: int
  relevantParagraph: int OPTIONAL
END

# Grammar
RECORD GrammarTestRow KEY testId COUNT 1..
  testId: string
  examRef: ref ExamRow
  taskNumber: int
  instruction: string
END

RECORD GrammarItemRow KEY itemId COUNT 1..
  itemId: string
  testRef: ref GrammarTestRow
  questionType: enum choose_form|fill_gap|transform|word_formation
  context: string
  gap: string OPTIONAL
  options: string OPTIONAL
  correctAnswer: string
  points: int
END

# Writing
RECORD WritingTaskRow KEY taskId COUNT 1..
  taskId: string
  examRef: ref ExamRow
  taskNumber: int
  instruction: string
  promptText: string
  wordLimit: int OPTIONAL
  genre: enum letter|description|argument|essay
END
```

### Структура данных

```
exams/
  pl/
    certyfikat-polski/
      B1/2023/
        exam.core-data        # ExamRow
        listening.core-data   # ListeningTaskRow + ListeningQuestionRow
        reading.core-data     # ReadingPassageRow + ReadingQuestionRow
        grammar.core-data     # GrammarTestRow + GrammarItemRow
        writing.core-data     # WritingTaskRow
      B2/2023/
        ...
  en/
    ielts/
      academic/2024/
        ...
```

### Аудио

- НЕ коммитить MP3/M4A (copyright)
- `audioUrl` → ссылка на источник или CDN
- Платформенные плееры: Android MediaPlayer, web Audio API, iOS/macOS AVPlayer
- Общий контракт `AudioPlayer` в shared (expect/actual)

### UI flow

1. Выбор экзамена (provider → level → year)
2. Выбор модуля (listening/reading/grammar/writing)
3. Пролистывание заданий (page-based)
4. Таймер (опционально, как на реальном экзамене)
5. Результаты с баллами

### Порядок реализации

1. DSL контракт + data pipeline (import PDF → .core-data)
2. Grammar module (самый простой — нет аудио, нет длинных текстов)
3. Reading module (тексты + вопросы)
4. Listening module (аудио + вопросы)
5. Writing module (текстовый ввод)

## Решения

- **Генерализованный**: один контракт для всех языков и экзаменов
- **DSL для данных**: вопросы/ответы в .core-data, проверяются data-contract
- **Native для UI**: аудио-плеер, split view, text input — платформенные
- **Модульный**: `:exam-data` отдельно от `:shared`
- **Copyright**: только транскрипция + sourceUrl, не файлы издателя
