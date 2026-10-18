# NJ01 `.measure`: preflight среды и независимой арифметики

Статус на `biome/full-port-2026@0f587dd6f3cb2ceaa6eaef79059bbb8bb62dbb88`: **среда и бюджет зафиксированы до sizing/cost prototype; произведён только статический расчёт строк/байтов**. Test-only prototype, чистая установка зависимостей, runtime wall/CPU/heap, production printer, browser host и N17 boundary/plus-one — **NOT RUN / NOT FROZEN**. Этот preflight применяет принятый [NJ01MeasureWriterContract.md](NJ01MeasureWriterContract.md) и независимые [NJ01PerformanceWriter.cases](NJ01PerformanceWriter.cases); не делает P8/P23 GO. Новых JSON-файлов и переносов старых измерений нет.

## Зафиксированная эталонная среда

Снимок прочитан локально до первой performance-пробы. Reference host: `MacBookPro18,2`, Apple M1 Max, 10 физических/10 логических CPU, 34 359 738 368 байт RAM; macOS 26.6, build `25G72`, Darwin kernel `25.6.0`, `xnu-12377.161.13~4/RELEASE_ARM64_T6000`. Текущий процесс сообщает `uname -m = x86_64`, `sysctl.proc_translated = 1`: **Rosetta 2**. Поэтому следующие цифры, когда они появятся, относятся к x86_64 Node под Rosetta на этом Mac, а не к нативному arm64. Нативный arm64 Node здесь не закреплён и должен получить отдельный заранее объявленный stratum; сопоставлять его с этими порогами как один и тот же host нельзя.

| Роль | Абсолютный исполняемый файл, уже найденный в локальном npm cache | Версия/ABI | SHA-256 точного binary |
| --- | --- | --- | --- |
| Минимальный Node 20 | `/Users/german/.npm/_npx/39eeea3b362d2c2d/node_modules/node/bin/node` | `v20.19.0 darwin x64`, Mach-O x86_64 | `d461a22b5c7690d8e419a1c1febbcaa5d7c666daa2e2de0ffa2e3c8b690151f2` |
| Минимальный Node 22 | `/Users/german/.npm/_npx/f6c81a5e22bed22a/node_modules/node/bin/node` | `v22.12.0 darwin x64`, Mach-O x86_64 | `94f25c85b91eeea4bbe39f4fc9a0f051a7f9c810ef5e610c9b058abe9bb564c1` |

Для обоих при явном `PATH` ниже `npm --version` уже вернул `11.3.0`. Установленный по умолчанию `/usr/local/bin/node` имеет `v24.1.0` и **не входит** в этот измерительный stratum. Манифест продукта: `package.json` SHA-256 `64a6ec95ba892f4a5b33d933df5f0efbae54ed059804cc02e5c98e8cb03375ef`; tool-required `package-lock.json` SHA-256 `bb539b80c7ce45a8e6784761325916efac4fd6690ff4e3b8848a3674f3fc755c`. Пути `~/.npm/_npx` — локальный cache, не поставка продукта: перед каждым запуском сверить version/arch/SHA. Доставка на другой host пока NOT PROVEN.

Чистая установка для **каждой** версии в отдельный каталог должна применить ровно этот Git source и lock. Ниже воспроизводимая команда, **задана, но в этом шаге NOT RUN**; значения `NJ01_*` не переопределяют системные переменные:

```sh
NJ01_SOURCE=/Users/german/Work/JS/polski-grammar-srs-biome-full
NJ01_REV=0f587dd6f3cb2ceaa6eaef79059bbb8bb62dbb88
NJ01_NODE20=/Users/german/.npm/_npx/39eeea3b362d2c2d/node_modules/node/bin/node
NJ01_NODE22=/Users/german/.npm/_npx/f6c81a5e22bed22a/node_modules/node/bin/node
for NJ01_NODE in "$NJ01_NODE20" "$NJ01_NODE22"; do
  NJ01_CLEAN=$(mktemp -d)
  git -C "$NJ01_SOURCE" archive "$NJ01_REV" | tar -x -C "$NJ01_CLEAN"
  shasum -a 256 "$NJ01_NODE" "$NJ01_CLEAN/package-lock.json"
  (cd "$NJ01_CLEAN" && PATH="$(dirname "$NJ01_NODE"):/usr/local/bin:/usr/bin:/bin" npm ci --ignore-scripts --engine-strict)
done
```

Команда лишь устанавливает зависимости для будущей пробы; успешное завершение `npm ci` нельзя предположить из наличия bin или npm version. Перед исполнением проверять два ожидаемых SHA binary и lock **по таблице**, а не просто распечатать их. Нужные Playwright browser binaries и два работающих URL тоже не доказаны; performance-проба текстового writer не должна ложно объявлять browser PASS.

## Замороженная нагрузка и бюджет до первой пробы

Независимый sizing recipe заполняет только неуказанные в P04 metadata, не меняя его требование: один `chromium`, `browserVersion = 155.0.0`, `date = 2026-10-18T12:34:56.789Z`, baseline URL `https://example.test/baseline/`, candidate URL `https://example.test/candidate/`, viewport 390×844 CSS px, dpr 3, `colorScheme = dark`, `reducedMotion = reduce`, `durationMs = 500`, `navFilter = null`, `cardFilter = null`. Все строки ASCII; `s:` входит в их размер. Интервалы и derived values — ровно P04: 30 значений `16` на run, `frames = 30`, p50/p95/p99 `16`, `over25 = 0`, paired deltas `0`, median baseline/candidate p95 `16`, median/range delta `0`.

| N runs | Полных rounds | Порядок | Интервалов |
| ---: | ---: | --- | ---: |
| 10 | 5 | нечётный baseline/candidate, чётный candidate/baseline | 300 |
| 100 | 50 | тот же | 3 000 |
| 1 000 | 500 | тот же | 30 000 |

Первая test-only probe (NEW `scripts/nj01/probe-glass-measure-structural.mjs`, предполагаемый CLI: `<pinned-node> --expose-gc scripts/nj01/probe-glass-measure-structural.mjs --runs <10|100|1000> --warmups 5 --iterations 20 --out <fresh-temp-dir>`) будет только структурно печатать/читать **полный** этот документ и публиковать во временном каталоге; она не станет golden или production helper. Это спецификация будущей команды, её исполнение NOT RUN. Она измеряет отдельный startup; для рабочих выборок — `node --expose-gc`, 5 прогревов и 20 полных encode/decode/temp-write/fsync/link/readback итераций на размер, ровно 5 отдельных процессов на Node/размер: в каждом из пяти проходов все три размера циклически сдвигаются на номер прохода, а порядок Node20/Node22 меняется местами через проход. Каждая публикация использует новый temp/final path, чтобы no-clobber не превращал повтор в ошибку. CPU/wall считаются на одну полную итерацию после прогрева; CPU берётся из `process.cpuUsage`, wall из monotonic clock. Raw samples, RSS/heap, фактические байты/строки, command/cwd/SHA и selected/executed/skipped сохраняются текстом. Не гонять одновременно Gate/Gradle/browser нагрузку на той же машине; если среда или bin меняется, объявить новый stratum **до** замера.

Бюджет уже выбран принятым контрактом, без замера под него: для N=1000 **приёмочный максимум** 1 s wall, 0.5 s CPU и 8 000 000 логических единиц; stop-лимиты 2 s wall, 1 s CPU и 16 000 000 единиц. Приёмочное значение даёт ≥2× запас. Единица: каждый UTF-8 байт на каждом encode/parse/hash/write/readback проходе; каждая emitted/parsed row, numeric scalar cell, interval validation и comparison в percentile sort. По каждому виду публиковать отдельный счётчик, чтобы итог нельзя было уменьшить переименованием операции. Этот budget относится к накладным расходам измерительного файла, не к Playwright времени. Если test-only probe уже выходит за половину stop-лимитов, остановиться до production code и пересмотреть механизм/стоимость отдельным решением; лимит не поднимать задним числом. Даже прошедшая probe **не** закрывает P23: real full validator/codec/writer и обе версии Node должны повторно пройти измерение.

## Независимая арифметика, без emit `.measure`

По точному recipe выше посчитана только длина будущих строк, а не получен document output. Одна строка с `c` ячейками имеет сумму длин ASCII-ячеек + `c−1` байт TAB + 1 байт LF, то есть сумму длин + `c`. Digest имеет ровно 64 lowercase hex независимо от значения; полная `end<TAB>sha256<TAB><digest><LF>` занимает 76 байт и включена. Никакого SHA будущего документа здесь не вычислялось.

Пусть `N` — чётное число runs, `R=N/2`, `d(x)` — число десятичных цифр, `D(N)=Σ(j=0..N−1)d(j)`, `Q(N)=2Σ(r=1..R)d(r)`. Статические 14 строк (header без `rounds`, плюс `engine`, `summary`, `end`) дают **444 байта**. Три переменные счётные строки (`rounds`, `run-count`, `paired-delta-count`) дают `43 + 2d(R) + d(N)` байт. Runs: `83N/2 + D(N) + Q(N)`; interval-count rows: `21N + D(N)`; interval rows: `530N + 30D(N)` (индексы interval `0..29` дают суммарно 50 цифр/run); paired-delta rows: `18R + D(R)`. Независимая прямая сумма длин этих типов строк дала ту же таблицу:

| Runs | Rounds | Все строки, включая end | Статические + счётные байты | Run | Interval-count | Interval | Paired-delta | Все UTF-8 байты, включая TAB/LF/end |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 10 | 5 | 342 | 491 | 435 | 220 | 5 600 | 95 | **6 841** |
| 100 | 50 | 3 267 | 494 | 4 522 | 2 290 | 58 700 | 990 | **66 996** |
| 1 000 | 500 | 32 517 | 497 | 47 174 | 23 890 | 616 700 | 10 390 | **698 651** |

Формула строк: `17 + 32N + R`. Для P04 нижней нагрузки это **32 517 строк, 698 651 байт** при выбранных ASCII metadata и `null` filters. Это не верхняя граница для произвольной версии браузера, URL или filter текста; будущие N17 limits ещё не установлены. Если гипотезы контракта 150 000 строк и 16 MiB примут без изменения, у этого **конкретного** recipe запас 4.61× по строкам и 24.01× по байтам; count-интервалов 120 000/30 000 = 4×, rounds 1 000/500 = 2×. Ни одно из этих отношений не доказывает CPU, wall, memory или корректность codec. Независимый tester сверит arithmetic с test-only probe после отдельного review данного preflight; расхождение остановит N17 freeze, а не исправит требование по output.

## Граница доказанного

Чтения среды: `uname -a`, `sw_vers`, `sysctl`, `file`, exact Node `--version`/`process.arch`, `shasum -a 256`, `npm --version` — **PASS как локальная инвентаризация**. Арифметика: `N=10/100/1000` **3/3 рассчитаны**, альтернативная сумма типов строк согласна с формулой; это **не** исполнение decoder или печать настоящего `.measure`. `npm ci`: 0/2, NOT RUN; prototype: 0/6 Node×N, NOT RUN; full writer, Playwright browser, Node arm64, independent byte-oracle, N17 edge, P8/P23 final: NOT RUN. Следующий шаг — независимое read-only ревью этого preflight и только затем test-only probe в другом изолированном срезе.
