# Очистка завершённых веток и рабочих деревьев

2026-11-13, по явному поручению владельца. Удалены четыре завершённых рабочих дерева: `p11`, `p9`, `p9-publication`, `p17-language-boundary`; пять локальных веток: `wip/owner-swipe-20261107`, `work/exam-dsl-native-pilot-20261108`, `work/p9-source-drafts-20261108`, `work/p9-publication-20261112`, `work/p17-language-boundary-20261112`. На GitHub удалены три старые экзаменные ветки с проверкой ожидаемого SHA (`--force-with-lease`); текущий `main` и релизные теги сохранены. P17 перед удалением принят независимыми Tester/Reviewer и объединён в `main` (`66e65e9`).

Уникальный старый owner WIP не считается автоматически влитым. Его commit `1814ea4` вместе с полной историей сохранён в проверенном Git bundle. Незакоммиченные изменения старых нативных тестов сохранены как binary patches с хешами; актуальные проверенные тесты уже находятся в `main`. Два принятых `.app` и их code hashes сохранены отдельно от удаляемых build caches.

Архив: `../polski-grammar-srs-worktrees/recovery-logs/20261113/worktree-cleanup/`. `retirement-manifest.json` перечисляет исходные refs/HEAD, уникальные commits, patches, native artifacts и удалённые remote refs. Bundle `retired-branches.bundle`: 124449452 bytes, SHA-256 `716b6ddef677753a5d5f29d9cde978fb118dcc4b26b2b63f918941b25059cba5`. `git bundle verify` PASS, полная история/20 refs. Старые source/media audits и неизменяемые browser snapshots вне рабочих деревьев сохранены. `ExamBin`, release archives и активная разработка P14 не затронуты.

Восстановление архивной ветки при необходимости:

```sh
git fetch ../polski-grammar-srs-worktrees/recovery-logs/20261113/worktree-cleanup/retired-branches.bundle refs/heads/wip/owner-swipe-20261107:refs/heads/recovered/owner-swipe-20261107
```

Завершённые ветки больше не нужны для исполнения текущего плана. Активные задачи остаются изолированы; новые рабочие деревья создаются только для исполняемой задачи и удаляются после принятия/объединения. Исторические evidence могут указывать старые исходные пути: соответствие архивам записано в manifest, а результаты сохраняют свои исходные даты.
