# Android: стоимость лёгкой полупрозрачной панели

Дата: 2026-09-25. **Итог:** на физическом Pixel 4 XL (90 Гц) разница между непрозрачной и полупрозрачной панелью высотой 72 dp лежала внутри шума контрольных пар: медиана парной B−A **−0,14 мс p95 задержки завершения кадра**, диапазон **−0,24…+0,16 мс**. Это поддерживает ограниченную alpha-панель как кандидат для Android; фоновое размытие и водяная анимация не измерялись. Эмуляторные прогоны ниже оказались слишком нестабильными для такого вывода.

## Метод

Изолированная debug-only Compose Activity показывает `LazyColumn` из 200 строк и панель навигации высотой 72 dp поверх списка. Вариант **A** заливает панель цветом `#1B2A3B` непрозрачно; **B** использует тот же цвет с alpha 0,72. Граница, размеры, текст и прокрутка одинаковы. Activity переключает варианты через `onNewIntent` (`singleTop`), без перезапуска процесса. По два разминочных прохода вверх-вниз, затем двенадцать чередующихся свайпов на вариант; порядок A/B меняется каждый раунд. `dumpsys gfxinfo ... framestats` сбрасывается после прогрева и сохраняется целиком. Показатель — p95 `(FrameCompleted − IntendedVsync)` среди кадров с `Flags=0`; это **задержка завершения кадра, не GPU time и не интервал между кадрами**. Дополнительно считаются кадры дольше 33,3 мс.

Окружение: Android 15 / API 35, AVD `Polski_ARM35`, ARM64, 1080 × 2400 px, density 420; MacBook Pro M1 Max, macOS 26.6. Для основного повтора эмулятор запущен с `-gpu host`; `dumpsys SurfaceFlinger` сообщил `Android Emulator OpenGL ES Translator (Apple M1 Max)`. Это программно-виртуализированное устройство даже при ускоренной графике; тактирование, ввод через ADB и фоновые задачи влияют на результат. Debug APK собран задачей `:androidApp:assembleDebug`.

| Прогон | Раундов | A: медиана p95 | B: медиана p95 | Медиана парной B−A | Диапазон парной B−A |
| --- | ---: | ---: | ---: | ---: | ---: |
| Первый host-GPU, перезапуск между вариантами | 5 | 18,50 мс | 19,03 мс | +0,03 мс | −15,79…+100,34 мс |
| Host-GPU, один процесс, `singleTop` | 9 | 19,17 мс | 33,94 мс | **+0,18 мс** | −255,97…+48,44 мс |

Во втором прогоне раунды 5, 6, 8 и 9 имели p95 около 18–19 мс у обоих вариантов. В раунде 2 непрозрачная панель достигла 316 мс p95, полупрозрачная — 60 мс; в раунде 1 полупрозрачная достигла 68 мс при 19 мс у непрозрачной. Такие скачки в противоположных направлениях делают медиану отдельного варианта и диапазон непригодными для заявления о точной стоимости alpha. Медианная парная разница описывает центр этих данных, но сама по себе не проходит предложенный performance gate: у B были раунды с дополнительными пропущенными кадрами. Отдельный прогон на SwiftShader (software GPU) дал p95 порядка 149–227 мс и не используется для оценки телефона.

Проверка переключения материала: последовательные screenshots A/B при одной позиции списка показали разные пиксели панели (например, x=100, y=2250: `#1B2A3B` против скомпозитного `#192A3B`), поэтому `singleTop` не оставил один вариант на оба измерения.

## Повтор с автоматической прокруткой и контрольными парами

Чтобы убрать ADB-свайпы, Activity прокручивала список сама со скоростью 1400 px/с. Перед каждой записью выполнялись 1,8 секунды прогрева; `gfxinfo` сбрасывался, затем прокрутка продолжалась ещё 2,5 секунды. Результат содержал последние 118–119 кадров на прогон. Каждый вариант запускался через `singleTop`, PID оставался одним и тем же. Между восемью парами A/B были распределены четыре пары A/A, показывающие шум без смены материала. Отдельная benchmark-сборка имеет R8, `debuggable=false`, `profileable` для shell и отдельный package `dev.polski.grammarmatrix.benchmark`, поэтому не затрагивает данные основного приложения. Это приближение к release, но не полноценный Macrobenchmark реального учебного экрана.

| Устройство | A/B: медиана парной B−A p95 | A/B: диапазон | A/A: диапазон | Кадры A/B >11,1 мс |
| --- | ---: | ---: | ---: | ---: |
| Pixel 4 XL, Android 13, 1440 × 3040, Adreno 640, 90 Гц | **−0,14 мс** | −0,24…+0,16 мс | −0,23…+0,36 мс | **0 из 1898** |
| AVD Android 15, host GPU, debug-сборка | −1,86 мс | −20,98…+32,51 мс | −23,61…+132,13 мс | не сравнивать с телефоном |

На телефоне объединённый p95 по A/B составил 5,735/5,729 мс, p99 — 6,114/6,034 мс; ни один из 1898 A/B кадров не превысил 11,1 мс по `(FrameCompleted − IntendedVsync)`. Это **не** измеренный `frameOverrunMs` и не оценка энергии. В контрольных A/A парах отличие достигало 0,36 мс, то есть наблюдаемые изменения A/B меньше естественного разброса в этом сценарии. Заряд оставался 89%, температура батареи по `dumpsys battery` изменилась с 23,7 до 26,0 °C во время USB-подключения; по этому нельзя вывести стоимость энергии эффекта.

На AVD даже A/A иногда отличались на 132 мс p95. Поэтому его новая серия служит отрицательным контролем качества среды, а не аргументом за или против материала. Сырые данные: [Pixel 4 XL](artifacts/glass-android/pixel4xl-auto-scroll/raw-results.json), [AVD auto-scroll](artifacts/glass-android/auto-scroll/raw-results.json). Код повторяемого сценария: [benchmark-android-glass-auto.mjs](../../scripts/benchmark-android-glass-auto.mjs), [benchmark Activity](../../kotlin/androidApp/src/benchmark/java/dev/polski/grammarmatrix/GlassBenchmarkActivity.kt).

## Решение

Ограниченную полупрозрачную панель Android можно переводить в прототип: на одном физическом Pixel 4 XL не выявлено ухудшения p95 сверх контрольного шума при непрерывной прокрутке. Это **не разрешение** на фоновый blur, водяные анимации, стеклянные карточки или автоматическое включение эффекта во всём приложении. Перед выпуском проверить реальный учебный экран с вводом и свайпами в profile/release-сборке через `FrameTimingMetric`/Perfetto, контраст и системное Reduce Motion; для слабых устройств отдельно проверить батарею. Если появятся заметные хичи, оставить непрозрачный Material 3 surface. **Android backdrop blur и водяная анимация — NOT RUN**.

Воспроизведение: собрать `ANDROID_HOME=/Users/german/Library/Android/sdk ./gradlew :androidApp:assembleDebug` из `kotlin/`, установить debug APK, затем из корня проекта выполнить `GLASS_ANDROID_ROUNDS=9 GLASS_ANDROID_OUT=Plans/Kotlin/artifacts/glass-android/host-gpu-single-process node scripts/benchmark-android-glass.mjs`. Метод и парсер: [benchmark-android-glass.mjs](../../scripts/benchmark-android-glass.mjs); debug Activity: [GlassBenchmarkActivity.kt](../../kotlin/androidApp/src/debug/java/dev/polski/grammarmatrix/GlassBenchmarkActivity.kt). Сырые результаты и полные выгрузки `gfxinfo`: [host-GPU один процесс](artifacts/glass-android/host-gpu-single-process/raw-results.json), [host-GPU первый прогон](artifacts/glass-android/host-gpu/raw-results.json), [software-GPU контроль](artifacts/glass-android/raw-results.json).

Benchmark APK собирается задачей `:androidApp:assembleBenchmark`; после установки на телефон с отдельным package повтор: `GLASS_ANDROID_SERIAL=<adb-serial> GLASS_ANDROID_PAIRS=8 GLASS_ANDROID_CONTROLS=4 GLASS_ANDROID_MEASURE_MS=2500 GLASS_ANDROID_OUT=Plans/Kotlin/artifacts/glass-android/pixel4xl-auto-scroll node scripts/benchmark-android-glass-auto.mjs`. Из-за сравнения локального `gfxinfo`, а не системного `frameOverrunMs`, этот тест — этап отбора визуального кандидата, не окончательный release gate.

Методические источники: [Android emulator graphics acceleration](https://developer.android.com/studio/run/emulator-acceleration), [FrameTimingMetric](https://developer.android.com/reference/androidx/benchmark/macro/FrameTimingMetric), [Compose performance](https://developer.android.com/develop/ui/compose/performance), [Android overdraw](https://developer.android.com/topic/performance/rendering/overdraw).
