# Проверка 13 сентября 2026

Это историческая проверка исходной версии. Текущие Kotlin/browser проверки и открытые gate перечислены в [Plans/Kotlin/Evidence.md](../Plans/Kotlin/Evidence.md) и [CoursePacksPlan.md](../Plans/Kotlin/CoursePacksPlan.md).

Рабочая копия: `/Users/german/Work/JS/polski-grammar-srs`.

## Результат

- Установлены зависимости, `package-lock.json` зафиксирован.
- Vitest обновлён до 4.1.11; `npm audit`: 0 уязвимостей.
- `npm test`: 38 проверок, 4 файла, всё прошло.
- `npm run build`: TypeScript и production-сборка Vite прошли.
- `git diff --check`: без ошибок.

## Что проверяется

- Польские формы, согласование и все варианты ответов генератора.
- Связность пяти предложений цепочки и корректные формы жена / коллега / ребёнок.
- Новые притяжательные формы, единственное и множественное число.
- FSRS, устная самооценка, печатная проверка, дневной счётчик.
- Сохранение старого прогресса и добавление новых навыков.
- React-интерфейс в jsdom: скрытый ответ, раскрытие и пять оценок, клавиатура, запрет двойной оценки, печатный ответ, контекстная таблица, селекторы полной матрицы, переход из таблицы в карточку, пустая очередь и повторная загрузка сохранённого прогресса.

## Браузерная проверка

Production-preview отвечает HTTP 200 на `http://127.0.0.1:4173/`.

В Brave визуально открыта новая версия, проверены лицевая сторона полной фразы, скрытый ответ, раскрытие эталона и переход клавишей 3 ко второй карточке с сохранением первого предложения и увеличением счётчика.

Полный повторный визуальный прогон, все таблицы в настоящем браузере и мобильная проверка не завершены: инструмент управления приложениями перестал получать элементы интерфейса и скриншоты; попытка отдельного Chrome завершилась таймаутом. Проверки React в jsdom не выдаются за полноценные браузерные или визуальные тесты.

## Репозиторий и хостинг

Репозиторий: [german-krasnikov/polski-grammar-srs](https://github.com/german-krasnikov/polski-grammar-srs).

SSH-ключ GitHub в окружении не сработал, поэтому remote переведён на HTTPS; отправка выполняется через авторизованный GitHub CLI без сохранения токенов в файлах.

Ветка `main` отправлена в репозиторий через HTTPS. Коммит функциональности: `dd67dff`.

13 сентября включение Pages для приватного репозитория было отклонено тарифом (HTTP 422).

14 сентября с явного разрешения пользователя репозиторий сделан публичным. API подтвердил создание GitHub Pages с HTTPS и публикацией через Actions. Workflow `.github/workflows/deploy.yml` собирает и публикует приложение после push в `main`.

Постоянный адрес: https://german-krasnikov.github.io/polski-grammar-srs/.

Локальная установка для использования опубликованного тренажёра не нужна. Для разработки: `npm run dev`. Для постоянного прогресса использовать обычное, а не приватное окно браузера. Прогресс опубликованного сайта хранится отдельно от localhost.

## Более поздняя проверка Kotlin-ветки

Исторический отчёт выше относится к React baseline сентября 2026 года и не подтверждает новую Kotlin-сборку. На 24 сентября отдельная [виртуальная матрица приёмки](../Plans/Kotlin/FinalVirtualAcceptance-Tester.md) фиксирует React unit 201/201, React browser 21/21, Kotlin JS/Wasm browser по 174/174 в Chromium/Firefox/Playwright WebKit, iPhone/iPad Simulator XCTest по 14/14, iPad large/dark/landscape 1/1, iPhone Reduce Motion 1/1, Mac Compose Desktop 38/38 и свежий пакет `.app`. [Манифест](../Plans/Kotlin/Stage5FinalVirtualArtifactProvenance.md#fv-11) связывает исходники и сборки хешами. Взаимодействие с узким окном готового Mac-приложения, физические устройства, реальные IME и screen readers остаются **NOT RUN**. Независимый Reviewer не принял полный виртуальный gate из-за отсутствия Mac host UI evidence. Kotlin не переключён на опубликованный сайт, и эти результаты не являются проверкой его production deployment.

## 2026-11-12: опубликованный Kotlin/Wasm и официальные экзамены

Kotlin runtime `a753640` опубликован на GitHub Pages: 16 официальных экзаменов, все четыре письменных раздела. Основной Kotlin CI и Pages deployment PASS. Публичный сайт проверен Playwright в Chromium, Firefox и WebKit: реальный Wasm, аудио >41 секунды с движением прогресса и паузой; byte-range доставка 16/16 и source images 7/7 PASS. Полный локальный Chromium exam 92/92 PASS. Подробные команды/артефакты и неуспешные промежуточные прогоны — [итоговое evidence](../Plans/Architecture/P9-PUBLIC-DEPLOYMENT-EVIDENCE.md).

План закрыт на 10/12: недоступна исправная запись B2/2022-06; настоящее изменение размера окна iPad Simulator не подтверждено. Физический iPad отложен владельцем. Эти ограничения не скрываются за успешной сборкой или landscape-проверкой.

По последующему запросу владельца демонстрационная карточка удалена. Runtime `739ebca` опубликован через Pages run `43317062454`; сам сайт снова проверен в Chromium/Firefox/WebKit: 16 официальных карточек, sample отсутствует, четыре раздела и timed entry доступны, 3/3 PASS. Полные опубликованные MP3 в предшествующем срезе проверены 16/16 по SHA-256; media follow-up не менял.

Исходные P1–P12 закрыты: повреждённый B2/2022-06 исключён владельцем, P11 принят после настоящего resize 834→412→834 pt и пяти целевых installed UI проверок, независимое review APPROVED. [Нативное evidence](../Plans/Architecture/P11-WINDOW-AND-CONTROLS-ACCEPTANCE-20261112.md). Новые обязательные P13–P19 открыты отдельно: аудит пустых ответов, immediate feedback, отчёт, lifecycle, язык матрицы и расширение учебного содержания; общий текущий статус 12/19.


## 2026-11-13: принятые P13, P17 и P18

Общий статус плана — 16/19. P13 завершён после повторной сверки 2420 оцениваемых строк и 91 письменного задания всех 16 официальных сдач; критерии письма добавлены без выдуманной автоматической оценки. Независимые source/browser/iPad Simulator проверки и review PASS/APPROVED; опубликованный `ea81239` отдельно проверен live. [Аудит и receipts](../Plans/Architecture/P13-OFFICIAL-SOURCE-REAUDIT.md).

P17 принят: английская матрица отсутствует в польском курсе и остаётся в английском, смена пары сохраняет прогресс. P18 принят и объединён в `f8bb4fb`: 26 польских навыков, десять новых тем/60 упражнений, четыре стиля и сводки. Main integration: Biome generate/check, typecheck, focused shared и JS/Wasm build PASS; Chromium 5/5 и WebKit motion 1/1 PASS. Нативные новые темы проверены на iPad Simulator. [P17](../Plans/Architecture/P17-INDEPENDENT-VERIFICATION.md), [P18](../Plans/Architecture/P18-SLICES-BD-INDEPENDENT-VERIFICATION.md). Pages `43359159946` для `d57dd97` PASS; live Chromium 140 на 375×667 подтвердил 26 навыков, реальное новое упражнение/эталон, четыре стиля, сводку и каталог 16 экзаменов без page errors. Kotlin CI `43359160604` также PASS.

P14/P15 остаются открыты до нативной приёмки: web feedback/report приняты в отдельной рабочей ветке, SwiftUI ещё проверяется. P16 принят после независимого JS/Wasm 34/34 на каждом target и реального hidden; Main integration Chromium 6/6 и WebKit 3/3 на каждом target PASS. [P16 evidence](../Plans/Architecture/P16-RIVE-LIFECYCLE-INDEPENDENT-VERIFICATION.md). P19 — итоговая интеграционная приёмка впереди. Физический iPad отложен владельцем, NOT RUN. [Текущий план](../Plans/Architecture/NEXT-SESSION-PLAN.md).


Web P14/P15 объединён в `c2902b7` и принят через P19 integration `2dacf34` (Reviewer APPROVED): shared Desktop 41/41, JS/Wasm по 479/479, Vitest 322/322, Biome/typecheck/production build/media PASS. Реальные JS/Wasm browser, настройки import/OFF/recovery и настоящий hidden прошли; исходные две JS ошибки fixtures и точечные исправления сохранены в [итоговом web evidence](../Plans/Architecture/P19-INTEGRATED-WEB-VERIFICATION.md). Этот web-срез отправляется на Pages отдельно. Native independent gate остаётся открытым, выявленный Settings-sheet timer исправляется; весь P19 пока не завершён.
