# Web: стоимость небольшой стеклянной панели

Дата замера: 2026-09-25. **Результат:** на этом Mac маленькая фиксированная панель навигации с `blur(12px)` не дала устойчивого ухудшения плавности относительно непрозрачной панели. Это разрешает переход к Android-измерению, но не доказывает расход GPU/батареи на телефоне или стоимость стеклянной карточки во весь экран.

## Что сравнили

Один и тот же Kotlin browser production artifact на странице «Таблицы и схема»: **A** — непрозрачная панель, **B** — прозрачность 72% без blur, **C** — прозрачность 72% + `backdrop-filter: blur(12px) saturate(1.2)`. У всех вариантов одинаковые `position: sticky`, граница, тень и геометрия. На 390 × 844 CSS px панель занимала около 366 × 64 CSS px (DPR 3), на 1440 × 900 — 1368 × 62 (DPR 1). Эффект задавался временной вставкой CSS при тестировании, production CSS не менялся.

Сценарий: непрерывная вертикальная прокрутка матрицы за панелью, 35 кадров прогрева после каждой смены стиля, затем 2,5 секунды измерения. Пять парных раундов с чередованием порядка вариантов для каждого движка и размера; нестабильный мобильный WebKit отдельно повторён десять раундов. Показатель — **p95 интервала `requestAnimationFrame`** в миллисекундах, медиана по раундам. Он показывает плавность кадров, но **не измеряет GPU time, CPU time, память или энергию**. Кадры дольше 25 мс сохранены в raw data.

Окружение: MacBook Pro 18,2 / Apple M1 Max / 32 GB / macOS 26.6 / Node 24.1.0 / Playwright 1.55.1; headless Chromium 140.0.7339.186, Firefox 141.0, WebKit 26.0. `colorScheme=dark`, `reducedMotion=reduce`, свежий browser context на движок и размер. Совместимый Kotlin JS/Wasm artifact от 2026-09-25 07:02 (его Wasm loader SHA-256 `1efee9de67d6fd6ef33e6d87d0d2171577ee6442dab265bd736e80600469fad5`). Во всех трёх современных движках был выбран Wasm; отдельно проверен прямой JS production artifact в Chromium и WebKit на мобильной ширине.

| Ветка / движок | Размер | A непрозр., p95 | B прозрачн., p95 | C blur, p95 | Парная медиана C−A |
| --- | --- | ---: | ---: | ---: | ---: |
| Wasm / Chromium | 390 × 844 | 16,8 | 16,8 | 16,8 | 0,0 мс |
| Wasm / Chromium | 1440 × 900 | 16,7 | 16,7 | 16,7 | 0,0 мс |
| Wasm / Firefox | 390 × 844 | 9,26 | 9,24 | 9,28 | 0,0 мс |
| Wasm / Firefox | 1440 × 900 | 9,24 | 9,24 | 9,26 | +0,02 мс |
| Wasm / WebKit | 390 × 844 | 27 | 24 | 23 | +1 мс; диапазон −8…+22 |
| Wasm / WebKit, повтор 10× | 390 × 844 | 19 | 19 | 19 | 0 мс; диапазон −7…0 |
| Wasm / WebKit | 1440 × 900 | 18 | 19 | 20 | 0 мс; диапазон 0…+2 |
| JS / Chromium | 390 × 844 | 16,7 | 16,8 | 16,8 | 0 мс |
| JS / WebKit | 390 × 844 | 19 | 19 | 19 | 0 мс |

Все значения в таблице — миллисекунды, кроме последнего столбца, где явно указан парный прирост. Firefox в этой среде планировал кадры примерно с другой частотой, поэтому абсолютные интервалы между движками не сравниваются. В первом WebKit mobile запуске один раунд дал **+22 мс** для C против A; это сохранённый outlier, а в независимом десятираундовом повторе он не воспроизвёлся. Медленная базовая работа WebKit в первом запуске (33 кадра >25 мс для A за пять раундов) также показывает шум окружения; C имел 49 таких кадров, а в повторе A/C — 10/8 за десять раундов. Нельзя объявлять один из материалов ускоряющим рендер по этим данным.

## Решение и ограничения

Локальный эксперимент не выявил воспроизводимого прироста p95 больше 1 мс для **малой панели навигации**. Следующий шаг — Android A/B на том же ограниченном участке. Для web это пока **performance spike**, а не разрешение включить blur в продукте: нужны реальный мобильный Safari/Android Chrome, GPU/энергия, слабое устройство, светлая тема/контраст и взаимодействие с клавиатурой. Headless-браузер на M1 Max и CSS-viewport телефона не заменяют телефон. Фоновое размытие на больших карточках и анимированные стеклянные слои этим замером не проверялись.

Воспроизведение: `python3 -m http.server 4178 --bind 127.0.0.1` из `kotlin/composeApp/build/dist/composeWebCompatibility/productionExecutable`, затем `GLASS_BENCH_ROUNDS=5 GLASS_BENCH_DURATION_MS=2500 node scripts/benchmark-kotlin-glass.mjs` из корня проекта. Тот же скрипт с `GLASS_BENCH_ENGINES=webkit GLASS_BENCH_PROFILES=mobile-390-dpr3 GLASS_BENCH_ROUNDS=10` повторяет WebKit mobile; переменные `GLASS_BENCH_URL` и `GLASS_BENCH_OUT` задают JS-ветку и отдельный output. Код: [benchmark-kotlin-glass.mjs](../../scripts/benchmark-kotlin-glass.mjs). Raw A/B: [основной прогон](artifacts/glass-web/raw-results.json), [повтор WebKit](artifacts/glass-web/webkit-repeat/raw-results.json), [JS mobile](artifacts/glass-web/js-mobile/raw-results.json).

Методические источники: [MDN backdrop-filter](https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/backdrop-filter), [Chrome DevTools Performance](https://developer.chrome.com/docs/devtools/performance). Этот `requestAnimationFrame`-пробник показывает cadence кадров; полный GPU trace потребует отдельного профилирования.
