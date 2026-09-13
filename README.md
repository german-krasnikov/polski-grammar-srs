# Polski Grammar SRS

Практический тренажёр польской грамматики для русскоязычного ученика с техническим мышлением. Вместо запоминания одной фразы приложение интервально повторяет **грамматический навык**, каждый раз генерируя новую комбинацию.

## Что внутри

- React + TypeScript + Vite
- FSRS (`ts-fsrs`) для интервального повторения
- 7 падежных форм в эталонном словаре
- Biernik: męski żywotny / nieżywotny
- Dopełniacz после отрицания
- Narzędnik, Miejscownik, Celownik
- согласование `mój + adjective + noun`
- настоящее / прошедшее / будущее
- аспект `robić → zrobić`, `kupować → kupić`
- личные местоимения
- mixed B1 transformations
- теория и формулы в приложении
- локальный прогресс без сервера
- экспорт прогресса в JSON
- шаблон GitHub Pages workflow (публикация включается после проверки плана)

## Запуск

Рекомендуется Node.js 22.12+ (в `.nvmrc` указан Node.js 22). Также поддерживается Node.js 20.19+. Требование соответствует Vite 7: https://v7.vite.dev/guide/migration#node-js-support.

```bash
npm install
npm run dev
```

Тесты:

```bash
npm test
```

Production build:

```bash
npm run build
npm run preview
```

## Приватный GitHub repository и Pages

Проект предназначен для приватного репозитория `polski-grammar-srs`.

```bash
git init -b main
git add .
git commit -m "Prepare Polish grammar SRS trainer"
gh repo create polski-grammar-srs --private --source=. --remote=origin --push
```

Перед первым commit выполните `npm install`, `npm test` и `npm run build`.
Сохраните созданный `package-lock.json` в Git. После этого для воспроизводимой установки используйте `npm ci`.

Для личного приватного репозитория Pages требует GitHub Pro; GitHub Free поддерживает Pages только для публичных репозиториев. Не меняйте видимость репозитория ради публикации. [Документация GitHub](https://docs.github.com/en/pages/getting-started-with-github-pages/creating-a-github-pages-site).

Шаблон находится в `docs/github-pages.yml.example` и пока **не запускается автоматически**. После подтверждения поддержки Pages:

1. GitHub → Settings → Pages → Source: GitHub Actions.
2. Скопируйте шаблон в `.github/workflows/deploy.yml`.
3. Добавьте `package-lock.json`, сделайте commit и push в `main`.
4. Проверьте успешное завершение workflow в Actions.

Сам сайт Pages будет доступен публично даже при приватном исходном репозитории. Прогресс сохраняется только в браузере и не отправляется на GitHub.

`vite.config.ts` использует `base: './'`, поэтому сборка работает под путём репозитория.
Локальный `npm run dev` и `npm run preview` не зависят от GitHub Pages.

## Тренировка

При открытии включается режим «По расписанию»: после оценки выбирается следующий навык по дате повторения. Когда повторения закончены, показывается время следующего. Выбор навыка слева включает тренировку именно этого навыка; кнопка «По расписанию» возвращает обычную очередь.

Ответ фиксируется после проверки. Прогресс хранится отдельно для каждого браузера и адреса приложения: локальная версия и Pages используют разные хранилища. Используйте экспорт JSON для резервной копии; интерфейс восстановления из файла пока не реализован.

## Архитектура

```text
src/
├── data/        # проверенные парадигмы слов
├── grammar/     # deterministic grammar engine
├── training/    # skills, generators, evaluator
├── srs/         # FSRS adapter
├── progress/    # persistence
└── ui/          # React
```

Главный цикл:

```text
Skill → Generate → Answer → Evaluate → FSRS → Generate another example
```

FSRS обновляется по `primarySkill`; это предотвращает шум, когда одно сложное предложение одновременно затрагивает 5–7 грамматических тем.

## Почему формы хранятся явно

Польское склонение содержит чередования основы (`książka → książce`, `mąż → mężem`, `pies → psa`). Для учебного ядра безопаснее иметь небольшой проверенный набор эталонных парадигм, чем пытаться «угадывать» все формы универсальным алгоритмом. Расширение словаря происходит добавлением новой парадигмы в `src/data/nouns.ts`.

## Дальнейшее расширение

Максимальный ROI после текущей версии:

- больше B1/B2 конструкций (`gdybym`, `chociaż`, `mimo że`, `powinienem`);
- отдельные plural masculine-personal drills;
- импорт/экспорт пользовательских слов;
- optional TTS;
- parser для детальной диагностики конкретного неправильного окончания.

Лицензия: используй и меняй проект свободно для личного обучения.
