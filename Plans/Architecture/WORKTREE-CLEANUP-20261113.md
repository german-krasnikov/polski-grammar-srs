# Очистка завершённых веток и рабочих деревьев

2026-11-13, по явному поручению владельца. Удалены четыре завершённых рабочих дерева: `p11`, `p9`, `p9-publication`, `p17-language-boundary`; пять локальных веток: `wip/owner-swipe-20261107`, `work/exam-dsl-native-pilot-20261108`, `work/p9-source-drafts-20261108`, `work/p9-publication-20261112`, `work/p17-language-boundary-20261112`. На GitHub удалены три старые экзаменные ветки с проверкой ожидаемого SHA (`--force-with-lease`); текущий `main` и релизные теги сохранены. P17 перед удалением принят независимыми Tester/Reviewer и объединён в `main` (`66e65e9`).

Уникальный старый owner WIP не считается автоматически влитым. Его commit `1814ea4` вместе с полной историей сохранён в проверенном Git bundle. Незакоммиченные изменения старых нативных тестов сохранены как binary patches с хешами; актуальные проверенные тесты уже находятся в `main`. Два принятых `.app` и их code hashes сохранены отдельно от удаляемых build caches.

Архив: `../polski-grammar-srs-worktrees/recovery-logs/20261113/worktree-cleanup/`. `retirement-manifest.json` перечисляет исходные refs/HEAD, уникальные commits, patches, native artifacts и удалённые remote refs. Bundle `retired-branches.bundle`: 124449452 bytes, SHA-256 `716b6ddef677753a5d5f29d9cde978fb118dcc4b26b2b63f918941b25059cba5`. `git bundle verify` PASS, полная история/20 refs. Старые source/media audits и неизменяемые browser snapshots вне рабочих деревьев сохранены. `ExamBin`, release archives и активная разработка P14 не затронуты.

Восстановление архивной ветки при необходимости:

```sh
git fetch ../polski-grammar-srs-worktrees/recovery-logs/20261113/worktree-cleanup/retired-branches.bundle refs/heads/wip/owner-swipe-20261107:refs/heads/recovered/owner-swipe-20261107
```

Завершённые ветки больше не нужны для исполнения текущего плана. Активные задачи остаются изолированы; новые рабочие деревья создаются только для исполняемой задачи и удаляются после принятия/объединения. Исторические evidence могут указывать старые исходные пути: соответствие архивам записано в manifest, а результаты сохраняют свои исходные даты.


## Завершённый P18

После независимой приёмки P18 удалены worktree `p18-course-expansion` и локальная/remote ветка `work/p18-course-expansion-20261113`. HEAD `d88cc86` полностью входит в `main` через `f8bb4fb`; дерево перед удалением чистое. Remote удалён с lease на `23bc7311cd405449d196954a9a928539db059621`. `git branch -d` сначала отказал из-за отставшего upstream; после удаления remote и снятия upstream обычный `-d` прошёл, принудительное удаление не использовалось.

Проверенные JS/Wasm distributions сохранены в `../polski-grammar-srs-worktrees/snapshots/p18-accepted-20261113/`; 22 файла JS/Wasm/CSS/index имеют SHA-256 в `worktree-cleanup/p18-retirement-manifest.json`. Нативные app, source receipts, `.xcresult`, Main app/data backup остаются в `recovery-logs/20261113/course-expansion/`. Активны только P14/native feedback и P16/Rive; их незавершённая работа не удалялась.


## Завершённый P16

Web Rive lifecycle принят независимыми Tester/Reviewer (`56652d0`/`b748832`) и объединён в Main `d155627`. Чистый worktree `p16-rive-lifecycle` и его локальная/remote ветка удалены; все commits входят в Main. Remote удалён с lease на `56652d0dfcc75cb4e01c9d86cb03d12dacf77edd`, локальная ветка — обычным `-d` после снятия upstream. Принятые JS/Wasm distributions сохранены в `../polski-grammar-srs-worktrees/snapshots/p16-accepted-20261113/`, 22 hashes и refs — в `worktree-cleanup/p16-retirement-manifest.json`; независимые логи остаются вне удалённого дерева. Интеграционная проверка объединения продолжается в основном checkout. Активным остаётся только дерево P14/P15 с нативными изменениями и их приёмкой.


## Неактивные Xcode-кэши P18

С согласованным владением runtime нативный разработчик подтвердил, что использует только P14 DerivedData. Два принятых P18 `.app` сохранены в `worktree-cleanup/native-artifacts/p18-{slice-a,slices-bd}-accepted.app`; исходные и сохранённые деревья независимо совпали по hashes всех 38 файлов каждого приложения. Manifest `p18-native-artifacts.json` записывает первоначальные пути, новые пути и tree hashes. После проверки удалены только `course-expansion/{slice-a,slices-bd}/DerivedData` (около 4,7 GB логического размера build-кэшей). `.xcresult`, логи, Main app/data backups и активный P14 cache сохранены. Исторические receipts со старым путём `.app` теперь восстанавливаются по manifest.


## Финальный P14/P15 и P19

Native HEAD `83b3f41` полностью объединён и опубликован через Main `4f51511`. После приёмки и Source equality check сохранены accepted `.app` (38files, debug dylibSHA `8aa79159…`, treeSHA `662d22fe…`) и полный JS/Wasm compatibility snapshot (46files,830494736bytes, CSSSHA `d24d7af8…`). Manifest `worktree-cleanup/p19-final-artifacts.json`; paths `native-artifacts/p14-final-accepted.app` и `snapshots/p19-final-20261113`.

Проверка git status разрешила единственный untracked путь `kotlin/iosApp/build-p14/`. Удалён только этот явно проверенный owned build cache; затем чистый worktree удалён обычным git worktree remove. Remote ветка удалена с lease на `da80678d1d888f9ac02eb7514e93969a0ed55cd7`, локальная — обычным `-d` после снятия upstream. `p14-retirement-manifest.json` хранит refs/результат. Теперь локально/на GitHub только `main`, git worktree list содержит только основной checkout. Owner original backup, архив post-test данных, исходные media/PDF, bundle старого uniqueWIP, test receipts и [инцидент восстановления](SIMULATOR-RESTORE-INCIDENT-20261113.md) сохранены.
