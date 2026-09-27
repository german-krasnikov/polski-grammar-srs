# Аудит выделения изменений и UX — 2026-09-27

Коммит: `934430e`. Контракт, по которому проверяли: [ContrastHighlightPlan.md](ContrastHighlightPlan.md), раздел «Контракт выделения (Emphasis contract)». Скриншоты лежат во временной папке `/private/tmp/claude-501/audit/` и в репозиторий не входят; пути даны для воспроизведения, пока папка жива.

Покрытие:

- **Web.** Chromium через Playwright. 16 навыков × front/back × 1280/390 × светлая/тёмная тема; 4 стиля × 4 навыка только в светлой desktop-теме.
- **Android.** Эмулятор API 35. Цепочка во всех 4 стилях, матрица, словарь, прогресс, настройки, светлая и тёмная тема.
- **iOS.** Симулятор iPhone 17 Pro, только светлая тема. Реально снят только навык `case.acc.f`; `aspect`/`mixed` проверены по исходникам.
- **Код.** shared, web, Android, Desktop JVM, iOS, macOS.

## Итог

Алгоритм выделения окончания в целом точен: основа не красится, диакритики сохраняются, ответ не виден до reveal. Системные дыры такие:

1. Нецветовая опора «пунктир = было, сплошная = стало» есть только в матрице и в паре «Что изменилось». На главном предложении карточки её нет ни на одном хосте. Везде получается различие только по цвету, а на iOS это ещё и близкие по тону красный и оранжевый.
2. Алгоритм `changeHighlightParts` откатывается к выделению целого слова в трёх регулярных случаях: чередование `ó→o` (`mój→mojego`), пунктуация на последнем слове предложения (`żona.→żonie.`) и альтернативы через ` / ` в `FormChange.to`.
3. Частица `Nie` вообще не входит в `FormChange`, поэтому отрицание нигде не подсвечено.
4. Блоки стилей Formula/Rule/Scene/NativeParallel/Examples/Why хранят простые строки, и выделять в них нечего.

## Выделение (emphasis)

| # | Серьёзность | Платформа | Проблема | Доказательство | Причина / исправление |
|---|---|---|---|---|---|
| E1 | high | web, android, desktop, ios, macos | На главном предложении (front «Исходное предложение», back «Эталон») и в строках блоков стилей обе стороны подчёркнуты сплошной линией. Было и стало различаются только цветом. | web: `audit/web/zoom-underline-style.png` против `zoom-whatchanged-line.png`; android: `audit/android/zoom_before_single.png`, `zoom_after_single.png`, `zoom_formula2.png` против `zoom_matrix1.png`; ios: `audit/ios/screens2/crop_table_row.png` | web: `.change-before` в `training.css:110` задаёт сплошную линию, а пунктир есть только в правиле `.form-contrast .change-before` (`:161`). `TrainingWebApp.kt:733/748/848` не оборачивает текст в `.form-contrast`. Compose: у `contrastAnnotatedText` (`ContrastText.kt:31`) нет пунктира, он есть только в `MarkedContrastText`. iOS/macOS: `FlashCardView.swift:202/213` и `PolskiGrammarMacApp.swift:746` вызывают `.underline()` без `pattern`. |
| E2 | high | shared → все | `mój→mojego/moim/mojemu` выделяется целым словом, хотя у соседнего `dobry→dobrego` выделено только окончание. Выглядит как супплетив. | `audit/web/zoom-acc-m-table.png`, `zoom-underline-style.png`, `desktop-light/inst-front.png`, `tables/Карта_системы.png` | `EndingHighlight.kt`: `prefix >= 3` не выполняется, потому что `ó≠o` уже на индексе 1. Нужна нормализация чередований из языкового пакета, используемая только для проверки надёжности. |
| E3 | high | shared → все | Последнее слово предложения выделяется целиком (`żona.→żonie.`, `żona.→żonę.`), потому что точка остаётся внутри токена. | `audit/ios/screens2/784A2F84-…png`, `crop_narz.png`, `crop_narz2.png` | Проверка `newSuffix.all(Char::isLetter)` не проходит из-за `.`. Нужно снимать конечную пунктуацию до diff и возвращать её как стабильный фрагмент. |
| E4 | high | content/shared → все | В `aspect` и `mixed` значение `FormChange.to` имеет вид `"kupiłem / kupiłam"` и `"Nie widziałeś / Nie widziałaś"`. Такой строки нет в эталоне, поэтому `wholePhraseStart` её не находит и в «Эталоне» ничего не выделено. В «Что изменилось» при этом показан весь блок со слэшем. | `course.json` `aspectTo`, `mixedChangeTo`; `ExerciseFactory.kt` около строк 113 и 133; `TrainingParityTest.kt` около строк 492 и 522 | Хранить одну буквальную форму, совпадающую с `expected`. Альтернативу хранить в `accepted` или отдельным `FormChange`. |
| E5 | high | shared/content → все | Частица `Nie` в `case.gen.neg`, шаге 3 цепочки и `mixed` не входит в изменения и не подсвечивается. | `ExerciseFactory.kt:71`; `course.json` `seenGenNeg`, `chainNeg` | Смоделировать вставку `"" → "Nie "` как отдельное изменение вида insertion и выделять её целым словом только на стороне «стало». |
| E6 | high | web (данные общие) | Четыре обзорные карточки «Карта системы» (`żona → żonę → żony` и т. д.) показаны плоским текстом, хотя план требует разметки у явных стрелок. | `audit/web/zoom-mapoverview.png` против `tables/Карта_системы.png` | `ReferenceSystemCard.example: String` (`CourseData.kt:66`), `MatrixWeb.kt:104-114`. Нужна структура из шагов (`steps: [from, to…]`). |
| E7 | high | shared/все хосты | Formula/Rule/Scene/NativeParallel/Examples/WhyOnDemand содержат `String` и не несут `EndingPart`. Выделение в 4 из 7 видов блоков отсутствует по построению модели. | `Block.kt:9-19`; `StyleComposer.kt`; `CardBlocksWeb.kt:97-122`; `AndroidStyleBlocks.kt:81-166`; `FlashCardView.swift:227-229`; `MacStyleBlockView.swift` | Выделять польские формы из явных пар данных курса (`from/to`), а прозу не парсить. Russian-сторону NativeParallel не красить. |
| E8 | medium | web | Панель «Таблица под рукой» до reveal показывает полную строку целевого падежа и подсвечивает её зелёным. Это прямая утечка ответа. | `audit/web/table-at-hand.png` | `MatrixWeb.kt:77-99`, `highlight-row` по `exercise.tags`. До reveal не подсвечивать строку и маскировать её «Стало». |
| E9 | medium | ios | `.underline(true, pattern: .dash)` в `NativeContrastPairView` на симуляторе рисуется сплошной линией. Цвета «было»/«стало» (красный/оранжевый) близки для дейтеранопии. | `audit/ios/screens2/crop_underline_zoom2.png` | Проверить поддержку `pattern` в конкатенированном `Text`. При необходимости рисовать пунктир своей фигурой. Цвет «стало» перевести в холодный тон. |
| E10 | medium | content (все хосты) | В rule-first и native-contrast один и тот же вид блока стоит и во front, и в back. Front-блоки остаются видимыми после reveal, поэтому контент дублируется. | `audit/android/09_rule_scroll.png`, `10_rule_reveal.png`, `16_native_scroll.png`; `courses/styles/rule-first.json`, `native-contrast.json` | back → `["rule","changes","contrast"]` и `["changes","contrast"]`. |
| E11 | low | content | Мнемоника «ЗАПОМНИ» написана по-английски: `past stem + gender + person`. | `course.json:3105`; `audit/android/09_rule_scroll.png` | Перевести на русский (например, `основа прошедшего + род + лицо`), проверить остальные `formula`. |
| E12 | medium | desktop (тест) | `DesktopScreenTest.revealedTrainingHighlightsRuleAndOffersOnlyTwoRatings` падает: ожидает 2 узла, находит 1. | `DesktopScreenTest.kt:351` | Ошибка теста, а не продукта. Заменить на поиск по подстроке в узле эталона. |

## UX

| # | Серьёзность | Платформа | Проблема | Доказательство | Исправление |
|---|---|---|---|---|---|
| U1 | high | desktop (JVM preview) | Доступны только 2 из 4 стилей (RuleFirst/SituationFirst). Сохранённый NativeContrast/MinimalTheory невозможно ни выбрать, ни увидеть. | `DesktopSettingsScreen.kt:62-63`, `TrainingScreen.kt`: 0 совпадений `NativeContrast|MinimalTheory` | Строить выбор из `builtInStyleIds`, как web и Android. |
| U2 | medium | android | Кнопок оценки нет, только свайп и строка-подсказка. Для зрячих основное действие скрыто за жестом. Для TalkBack есть custom actions. | `audit/android/03_reveal_bottom.png`, `30_vocab_reveal.png`, `dump_reveal.xml`; `AndroidFlipCard.kt` («D3 removed the button fallback») | Пересмотреть D3: две видимые кнопки плюс свайп. На iOS то же решение D3; синхронизировать хосты по итогу. |
| U3 | low | web (mobile) | На 390 px показана подсказка про клавиатуру: «Пробел — показать ответ · ← → или 1–2». | `audit/web/mobile-light/acc-f-front.png` | Для `(pointer: coarse)` показывать подсказку про касание. |
| U4 | low | ios | Кнопка выбора слова в списке словаря — SF Symbol без увеличенной зоны нажатия (вероятно < 44 pt). Проверено только по исходнику. | `PolskiGrammarApp.swift` около строк 1160-1166 | `.frame(minWidth:44,minHeight:44).contentShape(Rectangle())`. |

## Что работает

- Ответ не попадает в DOM или семантическое дерево до reveal. Исключение — E8.
- Для слов в середине предложения окончания выделяются точно, диакритики сохраняются.
- Контраст цветов web в обеих темах не ниже 6:1.
- Мобильная вёрстка web без горизонтальной прокрутки.
- Состав блоков в 4 стилях соответствует StylesBlueprint.

## Не покрыто

- Реальные Safari, Firefox и физические устройства.
- Screen reader'ы в живом режиме.
- Жесты свайпа на web.
- `prefers-reduced-motion`.
- Раскраска typed answer.
- iPad; тёмная тема iOS; macOS вживую.
- Экраны словаря на iOS с данными.
- Разделы матрицы iOS «Времена» и «Местоимения».
- Перестановка слов: в текущих навыках не нашлось упражнения с перестановкой в середине предложения. Позиционное сравнение слов при перестановке — открытый риск.
