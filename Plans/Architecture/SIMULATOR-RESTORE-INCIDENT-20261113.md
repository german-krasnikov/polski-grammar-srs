# Инцидент восстановления данных Simulator — 2026-11-13

При завершении нативных тестов агент ошибочно запросил путь data-container после Shutdown Simulator. `simctl` вернул ошибку405 и пустой stdout; без fail-fast проверки shell продолжил `rsync -a --delete … "$current_data"/` с назначением `/`. Команда была прервана Ctrl-C, session80573 завершилась130. Это ошибка исполнения в рамках нашей работы. Дальнейшее восстановление и очистка были остановлены, активного rsync не осталось.

## Проверка последствий

Root выполнил read-only аудит. Git не показывает пропавших tracked файлов. В113272 каталогах Applications и1064 каталогах Desktop/Documents/Downloads не обнаружено изменения mtime в проверенном интервале06:25:30–06:28. В301350 каталогах Work найдено39 изменений: наблюдавшиеся пути относятся к build caches/git refs и работе других активных проектов. FSEvents history с06:24 завершился без dropped flags; уведомлений об удалении в Applications/Desktop/Documents/Downloads не было. Остальные события относятся к Simulator/runtime/build/cache/git/temp; журнал не даёт надёжной привязки каждого события к процессу. Новых `/Documents`, `/SystemData` или app plist в root Library не найдено.

**Непреднамеренной потери данных в проверенных областях не обнаружено. Полной исходной копии всего хоста нет; побайтовая целостность всего диска не подтверждена.** Наличие каталогов само по себе не использовалось как доказательство. Установлен `/usr/bin/rsync`, openrsync protocol29, project189. Его manpage определяет delete-before по умолчанию; опубликованный Apple source сначала строит список удаления, затем выполняет его. Это согласуется с прерыванием на сканировании, но остаётся выводом по совокупности признаков, а не форензикой точного бинарного исполнения. [Apple receiver](https://github.com/apple-oss-distributions/rsync/blob/main/openrsync/receiver.c), [Apple file-list implementation](https://github.com/apple-oss-distributions/rsync/blob/main/openrsync/flist.c).

## Безопасное завершение восстановления

Root подготовил task-owned `safe_owner_restore.py` в `../polski-grammar-srs-worktrees/recovery-logs/20261113/restore-incident-0626/`. Скрипт проверяет непустой абсолютный путь, ожидаемый Simulator UUID, непосредственный parent data/Application, UUID контейнера, владельца и bundle metadata **до** Shutdown, затем повторно проверяет сохранённый путь и остановку устройства. Пустой/root/чужой путь отклоняется. Режим по умолчанию PLAN; явный apply перемещает только четыре app-data каталога в архив отката и копирует оригинал без rsync/delete, сохраняя container metadata. Контрольные суммы проверяются явными исключениями, в том числе под Python optimization.

Read-only Reviewer сначала отклонил отключаемые `assert` guards; они заменены explicit if/raise. Повторные обычный и `-O` guard/PLAN проверки:8 сценариев PASS; финальный Safety Reviewer APPROVED. Apply завершён `RESTORED_AND_HASH_VERIFIED`:19 original file hashes совпали, metadata не менялась, предыдущие data сохранены. Файлы реальных app data не удалялись.

После boot owner-specific read-only XCTest1/1 PASS подтвердил defaultON и каталог экзаменов. Повторное чтение actual plist после установки/запуска подтвердило сохранность всех прежних значений progress и остальных preferences;33 cards/stats стали42 лишь из-за добавления девяти новых topic defaults. Число reviews и даты прежние. Исходный owner backup и post-test data сохранены. Машинные receipts: `incident-report.json`, `owner-validation.json`, `post-test-data-before-safe-restore/manifest.json`; UI evidence `owner-after-safe-restore.log/.xcresult` в том же каталоге.

Очистка завершённых веток продолжится отдельно после принятия исходников; архивы исходных данных и инцидента сохраняются.
