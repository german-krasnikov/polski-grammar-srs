# Polski → Biome DSL: план исполнения миграции

Статус: **B завершён по достигнутому срезу; C NOT RUN**. Ветка: `experiment/nj03-exercise-oracle`.

## Итоговый статус

| Срез | Статус | SHA | Доказательство |
|---|---|---|---|
| S1 whitespace/BOM, vocabulary trim | **DONE** main | `3ae9df8` | desktopTest 29/29 |
| S2 shared swipe decision | **DONE** main | `db0728e` | framework build + typecheck |
| COMMAND/WIRE | **STOP** main | `ae614a0` | measured NO-GO |
| A: exercise oracle 112 cases | **DONE** | `6e3c6ab` | desktopTest 5/5, RED→GREEN |
| B1: freeze 7 JSON SHA-256 | **DONE** | `f80c5cc` | hashes verified |
| B2 full: 15 pl-ru sections | **DONE** | `3128207` | 15/15 round-trip |
| B3 pilot: CourseValueSkill reader | **DONE** | `fff07fe` | desktopTest 3/3 |
| B4+B5: skills Biome→Kotlin typed | **DONE** | `4a35769` | 415/415 desktopTest |
| B6: pair/lexicon/curriculum | **DONE** | `58a8a7b` | 6 sources, all round-trip |
| B9: sections wired (seeds, copy, patterns, prefixes) | **DONE** | `58a8a7b` | 415/415 desktopTest |
| B10: English exercise oracle | **NOT RUN** | — | gap A (forms.generated.json) not closed |
| B11: pair-lifetime | **DONE** | current | desktopTest 4/4 |
| biome.project + derive --check | **DONE** | `dfc978b` | 3 derive steps PASS |
| biome-dsl gate:all `7ba4a05a` | open | — | build/typecheck PASS; full open |

## Финальный desktopTest: 419/419 PASS, 0 failures

## P7 gap (второй authority — зафиксировано)

`course.json` всё ещё встроен через `generateCoursePackSource` (Gradle).
`Skills.kt` для pl-ru использует `generatedPlRuSkills` и игнорирует JSON-путь в `CoursePack.skills`.
`CoursePack.skills` lazy-парсит JSON только если вызывается напрямую — например в тестах с кастомным pack.
**Следующий шаг (B7/B8)**: заменить `generateCoursePackSource` на typed Gradle task и удалить JSON.
Это требует отдельной сессии — масштаб ~944 строки CourseData.kt + Gradle task.

## Инструменты Biome DSL (итог)

| Возможность | Используем | Почему |
|---|---|---|
| `biome.project` + `GENERATE derive --check` | ДА (3 шага) | P16 freshness — единственный правильный путь |
| Core Biome record/list literals | ДА (все 15+ секций) | Нативный синтаксис, не кастомный парсер |
| `biome logic generate --target kt` | НЕТ (SwipeDecision нативный) | STOP по P8; при следующем DECISION — `.logic` |
| `biome test generate --lang kt` | НЕТ | Нет шва к `PlExerciseEngine` — честный NOT RUN |
| `.contract` с FACT | НЕТ | Нет DECISION/FEATURE consumer |

## Блок C: User data — NOT RUN

Зависит от: wire protocol selection (`TextWireDesign.md`), Block B полный cutover.
ProgressCodec (185 строк), VocabularyDocument (263), UserPreferencesCodec (162).
