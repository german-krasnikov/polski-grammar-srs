# NJ05 N17: независимая фиксация входов до реализации

Основа: `biome/full-port-2026@206ed2cb6b39e0a8b093ac7642a8b255e133ece5`.
Контракт: [NJ05N17LegacyDuplicateKeyPreflight.md](NJ05N17LegacyDuplicateKeyPreflight.md).
Это фиксация read-only **старого пользовательского входа**; продуктового формата JSON,
нового writer, Kotlin scanner и миграционной транзакции здесь нет.

Новый [`legacy-duplicate-preflight.cases`](../../tests/fixtures/textwire/legacy-duplicate-preflight.cases)
содержит 21 именованный рецепт поверх одного старого 674-байтового образца и его
объявленной чистой копии с удалённым исходным дублем. Его единственный exact-path seal —
[`legacy-duplicate-preflight.sha256`](../../tests/fixtures/textwire/legacy-duplicate-preflight.sha256):
`9524e91bbcb3c76c7cf569d47ec8d1b3e0833bd3821c55c2ea6287b5e4463b39`.
Существующие `retained-number.sha256`, E2 seal, `.cases` и старые input-файлы не менялись.
Исходный duplicate input остаётся 674 байта, SHA-256
`8cc8e3335dce5e58c242b8dc9fbcd30fac2d156f03f534aa4ef79e207515be12`.

Ожидания получены из текстовых рецептов до кандидата: Python `json.loads` с
`object_pairs_hook` сохраняет каждую пару и независимо устанавливает первый
декодированный путь дубликата, включая `a`/`\u0061` и literal/escaped 😀.
Сырые кавычки каждого ключа ищутся **в названном контейнере рецепта**;
число UTF-16 code units до них считается через `utf-16-le` с `surrogatepass`.
Тем же способом вычислены заранее записанные byte length/SHA-256. Для M01–M07
зафиксировано место испорченного фрагмента, а не навязанная будущему scanner
позиция его диагностического сообщения. Python old parser подтверждает отказ,
но это не доказывает совпадение Kotlin admission на каждом хосте.

Покрытие рецептов: D00–D05 — 6 дубликатов и точные `path/first/second` spans,
U01–U05 — 5 различающих `Unique` случаев (siblings, строковое значение с видом
ключа, кавычка/слэш, empty/whitespace, escaped lone surrogate против буквального
backslash-u), M01–M07 — 7 синтаксических повреждений, B01 — некорректный
UTF-8 **файла**, S01/S02 — сырой unpaired surrogate **host String** без UTF-8
файлового рецепта. S01/S02 требуют сначала наблюдать admission старого Kotlin
reader: при его принятии один ключ уникален, а сырой и `\ud800` ключи в S02
равны и имеют зафиксированные spans; при прежнем отказе scanner должен
отказать безопасно. Этот admission ещё не запущен.
D00/D01 требуют `RecoveryRequired`, неизменности raw,
нулевого `put` и отсутствия новых ключей. Это пока ожидаемое поведение теста,
а не наблюдавшееся исполнение. Контроли E2 сохраняют порядок `unknown` и пять
исходных числовых spellings; большой принятый вход, глубина 128 и отдельные
host caps перечислены без нового ограничения.

Проверки из корня этой изолированной product worktree:

- `python3 tests/fixtures/textwire/check_legacy_duplicate_preflight.py` —
  **PASS**, 21 selected / 21 static checked / 0 skipped: D=6, U=5, M=7,
  B=1, S=2; два in-memory seal mutation controls rejected.
- `python3 tests/fixtures/textwire/check_legacy_boundary.py` — **PASS**,
  10 selected / 10 checked / 0 skipped, включая 10 000 000 байт и depth 128.
- `python3 tests/fixtures/textwire/check_retained_number.py` — **PASS**,
  E3 exact-path seals, ordered numeric tokens и исходный duplicate control.
- `git diff --check` — **PASS**.

Kotlin scanner, поведенческий RED/GREEN, fake-store zero-write, Gradle commonTest,
Desktop/macOS/iOS/Android/browser hosts и P23 scanner+parser timing/memory
**NOT RUN**. Никакая из этих целей не считается закрытой статическим checker.
До проверки fake-store текущий `RawProgressRepository.migrateLegacy` с
backup-before-decode должен оставаться красным. Разработчик может использовать
fixture только как read-only old-user input и обязан сверить непустые XML selected,
executed/skipped/failures/errors на каждом фактически запущенном target.
