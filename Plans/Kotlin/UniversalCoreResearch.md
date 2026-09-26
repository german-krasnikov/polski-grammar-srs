# Универсальное ядро языкового курса: исследование и архитектурное предложение

Дата: 2026-09-26. Статус: исследование (read-only, код не менялся). Главный критерий пользователя: **гибкая расширяемость** — новый изучаемый язык, новый родной язык и новый стиль подачи добавляются данными, а не кодом.

Легенда доказательности: **[E]** — мета-анализ/контролируемые исследования; **[P]** — практика/методика без контролируемой проверки; **[S]** — моя спекуляция/прогноз, требует проверки.

---

## 0. Короткий ответ

1. Универсальный скелет существует и хорошо ложится на данные: **конструкция (смысл) × признаки (tense/polarity/clause type/person/number/...)** — это и есть обобщённая «таблица Петрова». Признаки берём из **Universal Dependencies features + UniMorph schema** как канонический словарь, не изобретая свой.
2. Все 16 нынешних польских навыков — это **преобразования одного пакета признаков в другой** (acc→gen при отрицании, present→past, my→owner, sg→pl, statement→question). Значит, генератор упражнений можно сделать языконезависимым: `exercise = realize(construction, bundleA) → realize(construction, bundleB)`.
3. Морфология: **таблицы во время выполнения, правила — только при сборке пакета.** Kotlin runtime читает материализованные парадигмы (lookup) и шаблоны конструкций; генерация форм по парадигмам/правилам/импорту (SGJP, UniMorph, Wiktionary) — Node-скрипт автора. Тогда добавление языка = 0 строк Kotlin в норме.
4. Родной язык — **тоже языковой пакет**. Контраст «по-русски так → по-польски так» вычисляется реализацией *той же* конструкции с *тем же* пакетом признаков на L1 плюс авторские заметки о переносе в парном пакете `pl-ru`.
5. Четыре стиля — **рецепты порядка блоков** над одними и теми же слотами контента; одинаковые упражнения, ответы и FSRS. Честная подача: «предпочтение», а не «тип мозга» (Pashler 2008).
6. pl-ru сохраняется: skill IDs и ключи прогресса не переименовываются; новый слой добавляет `skillMap` (legacy ID → конструкция + дельта признаков) и проходит parity по pinned fixtures до переключения.

---

## 1. Что уже есть в репозитории (переиспользуем)

- `courses/pl-ru/course.json` (schemaVersion 1, contentVersion 1): `targetLanguage=pl`, `nativeLanguage=ru`; 14 nouns, 7 adjectives, 11 verbs, 9 personalPronouns, 7 possessives (явные 4×49 форм + 3 инварианта), `morphology.futureAuxiliary`, 16 skills с `methods.logic/situations.{introduce,retrieve,feedback,review}`, `sentenceSeeds`, `caseSentencePrefixes`, `exerciseCopy` (70 ключей), `exercisePatterns` (13 шаблонов), `reference.*` (таблицы, опора на русский, pipeline, verbTeaching, pronounTeaching…), `vocabulary`.
- Skill IDs: `case.acc.n, case.acc.f, case.acc.m, case.gen.neg, case.inst, case.loc, case.dat, agreement.my, verb.present, verb.past, verb.future, aspect, pronouns, mixed, sentence.question, sentence.plural`.
- Прогресс: `Progress{version=1, cards:[StoredCard(skillId, SrsCard)], stats: Map<skillId, SkillStats>}` — грамматические ключи **без префикса пары**. Словарь: `pl-ru:vocabulary:{ru-pl|pl-ru}:{id}` (с парой и направлением). Preferences: `coursePair="pl-ru"` жёстко, `PreferredMethod {Logic, Situations}`.
- Kotlin-ядро уже почти табличное: `GrammarEngine.kt` (94 строки) — lookup существительных/прилагательных/притяжательных + одно правило прошедшего времени (stem + `m/em/ś/eś/śmy/ście`) + сборка `będę + lemma`. Но модель — **польские enum’ы** (`GramCase` 7 значений, `Gender` 5 значений с `m-personal`, `PossessiveId` 7), `ExerciseFactory` содержит `when(skillId)` с польской логикой (фильтры по роду, выбор падежа).
- Уже принятые решения, которые переносятся в универсальную версию без изменений: стили — предпочтение, а не диагноз (CoursePacksPlan §Продуктовый контракт); цикл `introduce → retrieve → feedback → review` (MethodologyCycleBlueprint); контраст «было → стало» с явными сегментами, эвристика префикса — лишь fallback (ContrastHighlightPlan); частота ≠ уровень CEFR (Leksjo/NKJP CC BY 4.0); два направления словаря — два FSRS ID; валидатор пакета в CI, runtime не «чинит» пакет; pinned fixtures (490 possessive, 386 verb) как оракул parity.

Вывод: ~70% «данных вместо кода» уже сделано для одной пары. Не хватает **языконезависимой модели признаков**, **генератора конструкций вместо `when(skillId)`** и **разделения пакета на core / language / pair / style**.

---

## 2. (a) Метод «Полиглот» и родственные «скелетные» методы

### 2.1 Петров

- Фиксированная базовая таблица урока 1: **3 времени (будущее/настоящее/прошедшее) × 3 типа предложения (вопрос/утверждение/отрицание)**, подлежащее — личные местоимения, V — глагол; отдельно правило 3-го лица (англ. -s) ([englishtexts.ru, урок 1](https://englishtexts.ru/misc/poliglot-angliyskiy-za-16-chasov); [english-polyglot.ru/tablicy](https://english-polyglot.ru/tablicy.html); [poliglot16.ru урок 1](https://poliglot16.ru/en/urok1/)).
- Дальше: урок 2 — местоимения (объектные/притяжательные), вопросительные слова, предлоги; урок 3 — «быть»; далее прилагательные, диалоги, фразовые глаголы и применение ([там же](https://englishtexts.ru/misc/poliglot-angliyskiy-za-16-chasov)).
- Принцип: «отсечь лишнее» — минимум грамматики, ~300 слов, 50–60 самых частых глаголов ([am-en.ru](https://am-en.ru/techniques/polyglot.html)). Телепроект: 8 сезонов — английский, итальянский, французский, испанский, немецкий, хинди/урду, португальский, китайский ([Википедия: Полиглот (телепередача)](https://ru.wikipedia.org/wiki/%D0%9F%D0%BE%D0%BB%D0%B8%D0%B3%D0%BB%D0%BE%D1%82_(%D1%82%D0%B5%D0%BB%D0%B5%D0%BF%D0%B5%D1%80%D0%B5%D0%B4%D0%B0%D1%87%D0%B0))). Один и тот же скелет применён к типологически разным языкам (включая китайский без словоизменения) — это прямое практическое доказательство, что скелет — **смысловой**, а не формальный.
- Доказательность: **[P]**. Контролируемых исследований нет; «16 часов» — маркетинг. Но компоненты совпадают с [E]-принципами: явное правило + много управляемой практики + частотный словарь.

**Что обобщается:** оси «время × полярность × тип предложения × лицо/число» — универсальный смысловой каркас. **Что не обобщается напрямую:** вид (pl/ru), род в прошедшем (pl/ru), вежливость (ja/ko), эвиденциальность (tr), отсутствие времени (zh — вместо него аспектные частицы 了/过/在). Эти оси должны быть **дополнительными измерениями, которые язык объявляет**, а не частью фиксированной 3×3.

### 2.2 Родственные методы

| Метод | Скелет | Что брать | Доказательность |
| --- | --- | --- | --- |
| Michel Thomas | Структуры строятся из известных элементов; перевод L1→L2 «в голове», без заучивания | Порождение фраз из малого ядра, опора на L1-когнаты | [P]; автор отказывался от контролируемой проверки ([обзор](https://www.fluentin3months.com/reviews/michel-thomas-review/)) |
| Language Transfer (Thinking Method) | То же + явный перенос из L1 (англ.) и объяснение «почему» | Стиль 3 (контраст) и стиль 4 («почему?» по запросу) | [P] ([languagetransfer.org/courses](https://www.languagetransfer.org/courses)) |
| Glossika | Тысячи фраз, повторение по расписанию, минимум правил | Стиль 4 (паттерны без теории) | [P] для продукта; spacing/retrieval — [E] |
| Assimil | Диалоги, пассивная фаза → активная (обратный перевод) | Стиль 2 (ситуация) и двунаправленные карточки | [P] |
| Duolingo Grammar lessons | Одно правило, таблицы окончаний | Уже учтено в ContrastHighlightPlan | [P] ([блог](https://blog.duolingo.com/language-rules-learning-grammar-on-duolingo/)) |
| Grammatical Framework RGL | **Общий абстрактный синтаксис** + конкретизация в 30+ языков (в т.ч. Polish, Russian, Chinese, Japanese, Arabic, Hindi) | Модель «конструкция = абстрактное дерево, язык = линеаризация» | Инженерно проверено ([GF RGL synopsis](https://www.grammaticalframework.org/lib/doc/synopsis.html); [LiLT paper](https://journals.colorado.edu/index.php/lilt/article/view/1205)); LGPL |

Главный вывод: **ни один «скелетный» метод не имеет собственной доказательной базы**, но все сходятся в одном: малый набор высокочастотных конструкций, порождение новых фраз, опора на L1. Доказательную часть обеспечиваем через [E]-механизмы из §5, а скелет берём из типологии (UD/UniMorph/GF), а не из авторской методики.

---

## 3. (b) Универсальные схемы признаков

### 3.1 Источники

- **UniMorph schema**: 23 измерения, 212+ значений (Aktionsart, animacy, aspect, case (39 значений), comparison, definiteness, deixis, evidentiality, finiteness, gender, information structure, interrogativity, mood, number, POS, person, polarity, politeness, switch-reference, tense, valency, voice). Запись пакета: `FIN;IND;PFV;PST;2;SG;INFM`. Стремится к **тождеству значения** между языками, а не терминологии; парадигматичен (слово целиком ↔ пакет признаков, без членения на морфемы) ([UniMorph schema](https://unimorph.github.io/schema/); [Sylak-Glassman, schema doc](https://unimorph.github.io/doc/unimorph-schema.pdf); [UniMorph 4.0](https://arxiv.org/pdf/2205.03608)).
- **Universal Dependencies features**: лексические (PronType, NumType, Poss…), именные (Gender, Animacy, Number, Case, Definite, Degree, Polarity), глагольные (VerbForm, Mood, Tense, Aspect, Voice, Person, Evident, Polite, Clusivity) ([UD features](https://universaldependencies.org/u/feat/all.html)). Значения: `Polite=Infm|Form|Elev|Humb`, `Definite=Def|Ind|Spec|Cons|Com`, `Evident=Fh|Nfh`, `Number=Sing|Plur|Dual|Paucal|Trial|…`, `Animacy=Anim|Hum|Nhum|Inan`.
- Маппинг UD↔UniMorph существует ([McCarthy et al., «Marrying UD and UniMorph»](https://arxiv.org/pdf/1810.06743)) — значит, мы можем хранить **UD-имена как канонические ключи** (читаемы, стабильны) и держать UniMorph-алиасы для импорта данных.
- Уже доказана применимость к японскому/корейскому ([J-UniMorph](https://arxiv.org/html/2402.14411); [K-UniMorph](https://arxiv.org/pdf/2305.06335)).
- **CEFR-инвентари**: English Grammar Profile — 1200+ эмпирических «can-do» утверждений о грамматике по уровням на базе Cambridge Learner Corpus ([Cambridge blog](https://www.cambridge.org/elt/blog/2015/11/11/introducing-english-grammar-profile-1-building-profile/); [методология](https://orca.cardiff.ac.uk/id/eprint/166496/1/ijcl.14086.oke.pdf)); Council of Europe **Reference Level Descriptions** — национальные инвентари форм по уровням для de, en, fr, it, es, pt, cs, hr и др. ([CoE RLD](https://www.coe.int/en/web/common-european-framework-reference-languages/reference-level-descriptions)). Для польского — **государственные стандарты сертификационных экзаменов** (приложение 1 к распоряжению; падежи, спряжение, времена, вид, наклонения, местоимения, степени сравнения, союзы) ([Dz.U. 2016](https://certyfikatpolski.pl/wp-content/uploads/2018/05/rozp_26_2_16.pdf); [стандарты](https://www.certyfikatpolski.uni.lodz.pl/informacje-dla-zdajacych/standardy-wymagan-egzaminacyjnych)).

### 3.2 Покрытие типологически разных языков

| Ось | pl / ru (слав.) | es/fr/it (ром.) | de | en | tr (агглют.) | zh (изолир.) | ja | ar |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Tense | Past/Pres/Fut (pl fut: аналит. + перф. презенс) | Pres/Past(Pret/Imp)/Fut/Cond | Pres/Past/Perf/Fut аналит. | Pres/Past + аналит. | Past/Pres/Fut/Aorist | **нет**, аспектные частицы | Past / NonPast | Perf/Imperf (аспектно-временная) |
| Aspect | Imp/Perf лексически | Imp/Perf (прош.) | – | Prog/Perf аналит. | Prog/Hab | 了 Perf, 过 Exper, 在/着 Prog | Prog (-te iru) | Perf/Imp |
| Mood | Ind/Imp/Cnd | Ind/Sub/Imp/Cnd | Ind/Sub/Imp | Ind/Imp (+ modals) | Ind/Imp/Cnd/Nec/Opt… | частицы | Imp/Pot/Vol/Cnd | Ind/Sub/Jus/Imp |
| Person×Number | 1–3 × Sg/Pl | 1–3 × Sg/Pl | 1–3 × Sg/Pl | 3Sg -s | 1–3 × Sg/Pl | нет согласования | нет согласования | 1–3 × Sg/**Dual**/Pl + Gender |
| Gender | pl: m-pers/m-anim/m-inan/f/n (+ vir/nonvir в мн.) | M/F | M/F/N | – | – | – | – | M/F |
| Case | 7 | – (кроме местоим.) | 4 | местоим. | 6 + притяж. суффиксы | – | **частицы** は/が/を/に | 3 (Nom/Acc/Gen) |
| Definiteness | – | Def/Ind (артикль) | Def/Ind | Def/Ind | Ind (bir) | – | – | Def/Ind/**Cons** |
| Politeness | Pan/Pani (3 л.) | tu/usted (T/V) | du/Sie | – | sen/siz | 您 | **Polite=Form/Elev/Humb** (грамматикализована) | – |
| Evidentiality | – | – | – | – | **-mIş (Nfh)** | – | ~そう/らしい | – |
| Question | Czy… / интонация | инверсия/интонация | инверсия | do-support | частица **mI** (гармония) | 吗 | か | هل |
| Negation | nie + **Gen при отрицании** | no/ne…pas | nicht/kein | do not | суффикс **-mA-** в глаголе | 不/没 (зависит от аспекта!) | -nai / -masen | لا/لم/لن (зависит от времени!) |

Ключевое: **смысловые оси универсальны, способ выражения — нет**. Отрицание в ar/zh выбирает частицу по времени/аспекту, в tr — суффикс внутри глагола, в pl — меняет падеж дополнения. Поэтому «реализация» конструкции должна быть языковыми данными (шаблоны + правила согласования/управления), а «конструкция» и «признаки» — общими.

### 3.3 Предлагаемый core-словарь

- `core/features.json`: UD-ключи и значения + `unimorph` алиас + `gloss` на языках интерфейса. Пакет языка **объявляет подмножество** (`pl.features.Case = [Nom,Gen,Dat,Acc,Ins,Loc,Voc]`, `zh.features.Case = []`), и **может расширять** значением с префиксом языка, если UD не хватает (`Gender=pl:MascPers` — ср. UD Polish: Gender=Masc + Animacy=Hum/Anim/Inan; лучше выразить польские 5 родов как `Gender×Animacy`, что точно соответствует UD и не требует расширения).
- `core/constructions.json` — **универсальный скелет курса** (обобщённый Петров + CEFR A1–B1). Пример инвентаря (~40 конструкций до B1):
  - Предикация: `clause.action` (S V O), `clause.identity` (X есть Y), `clause.property` (X какой), `clause.existence` (есть / нет X), `clause.location` (X находится в Y), `clause.possession` (у меня есть X).
  - Оси клаузы (признаки, не отдельные конструкции): `Tense`, `Aspect`, `Polarity`, `ClauseType{Decl,YesNoQ,WhQ,Imp}`, `Person`, `Number`, `Gender` субъекта, `Polite`, `Mood{Cnd}`, `Modality{can,must,want}`.
  - Именная группа: `np.basic`, `np.adjective` (согласование), `np.possessor`, `np.demonstrative`, `np.quantity` (числ. + сущ.; pl: 2–4 vs 5+ с Gen Pl), `np.plural`.
  - Роли/отношения: `role.object` (управление глагола), `role.recipient`, `role.instrument/comitative` (с кем/чем), `role.topic` (о ком/чём), `motion.to/from`, `place.in/on`, `time.when`.
  - Вопросы: `wh.who/what/where/when/why/how`.
  - Сложное: `clause.coordination`, `clause.because`, `clause.if`, `comparison.more/most`.
- Каждая конструкция: `id`, `slots` (роли с типами), `axes` (какие признаки варьируются), `level` (локальная метка, сверяемая с RLD/EGP/польскими стандартами), `prerequisites`. **Без форм.**

---

## 4. (c) Источники данных и лицензии (коммерческое приложение)

Не юридическая консультация; перед релизом — проверка юристом. ShareAlike-данные внутри пакета делают **сам пакет данных** производной работой (публиковать пакет под той же лицензией); код приложения, как правило, не становится производной — но это стоит проверить **[S]**.

| Источник | Что даёт | Лицензия | Коммерция | Рекомендация |
| --- | --- | --- | --- | --- |
| SGJP/PoliMorf (Morfeusz 2 inflection data) | Полные польские парадигмы 450k+ лемм | **BSD-2** (только флективные данные; полный SGJP — нет) | Да, с сохранением авторства | **Основной источник форм для pl** ([Morfeusz license](https://morfeusz.sgjp.pl/doc/license/)) |
| UniMorph (per-language repos) | Парадигмы 180+ языков в `lemma \t form \t FEATS` | Разные: напр. `unimorph/pol` — **CC BY-SA 3.0** (из SGJP); часть релизов 4.0 указана как CC BY-NC | Проверять каждый репозиторий; NC — нельзя | Формат импорта + источник там, где лицензия BY/BY-SA ([unimorph/pol](https://github.com/unimorph/pol); [UniMorph 4.0](https://arxiv.org/pdf/2205.03608)) |
| Wiktionary / kaikki.org (wiktextract) | Формы с тегами, значения, переводы, IPA | **CC BY-SA 4.0 + GFDL** | Да, с атрибуцией и SA на данные | Импорт парадигм/переводов для новых языков ([Wiktionary:Copyrights](https://en.wiktionary.org/wiki/Wiktionary:Copyrights); [kaikki](https://kaikki.org/dictionary/rawdata.html)) |
| Wikidata Lexemes | Леммы, формы с грамматическими признаками (Q-items), смыслы | **CC0** | Да, без условий | Лучший юридически, но покрытие неравномерное ([Lexemes docs](https://www.wikidata.org/wiki/Wikidata:Lexicographical_data/Documentation)) |
| OpenCorpora (ru) | Русская морфология (основа pymorphy2) | **CC BY-SA 3.0** (код pymorphy2 — MIT) | Да, SA на данные | Для L1-пакета `ru` ([pymorphy2](https://github.com/pymorphy2/pymorphy2)) |
| Tatoeba | Параллельные предложения | **CC BY 2.0 FR** (есть CC0-часть); аудио — по автору | Да, с указанием автора каждого предложения | Кандидаты примеров; хранить `sentenceId`+`author` ([Tatoeba](https://en.wiki.tatoeba.org/articles/show/using-the-tatoeba-corpus)) |
| Leksjo / NKJP freq | Частоты польских лемм | **CC BY 4.0** | Да | Уже используется |
| FrequencyWords (OpenSubtitles) | Частоты 60+ языков | Код MIT, данные **CC BY-SA 4.0** | Да, SA | Для новых языков ([FrequencyWords](https://github.com/hermitdave/FrequencyWords)) |
| wordfreq | Частоты 40+ языков | Код Apache, данные **CC BY-SA 4.0** | Да, SA | Альтернатива ([wordfreq](https://github.com/rspeer/wordfreq)) |
| UD treebanks | Реальные предложения с признаками | По treebank’у (BY-SA, иногда NC) | Проверять | Для проверки/примеров, не как контент по умолчанию |
| Unicode CLDR | Plural-категории (pl: one/few/many/other; ar: 6; ja/zh: other), названия языков, частично грамм. род/падеж единиц | **Unicode License v3** (пермиссивная) | Да | Правило «2–4 vs 5+» и локализация UI ([CLDR plurals](https://cldr.unicode.org/translation/getting-started/plurals)) |
| GF RGL | Абстрактный синтаксис + грамматики 30+ языков | **LGPL** | Да (как справочник/сборочный инструмент) | Референс для инвентаря конструкций и линеаризаций; runtime не встраивать |
| Apertium | FST-словари | **GPL v3** | Рискованно для закрытого клиента | Только как справка; не встраивать |
| English Grammar Profile, CoE RLD | Инвентари грамматики по CEFR | Проприетарно (свободный просмотр) | Нельзя копировать | Только как чек-лист уровней |
| Польские стандарты экзаменов (Dz.U.) | Официальный инвентарь A1–C2 для pl | Нормативный акт — вне авторского права (ст. 4 закона об авторском праве PL) **[S: проверить юристом]** | Вероятно да | Источник уровней для pl-пакета |

Правило пайплайна: каждая запись пакета несёт `source` (id источника + ревизия) → генерируемый `ATTRIBUTION.md` на пакет; валидатор отвергает запись без источника и NC-источники в коммерческой сборке.

---

## 5. (d) Доказательная база для подачи

| Утверждение | Доказательства | Как применяем |
| --- | --- | --- |
| Явное объяснение правила + практика эффективнее имплицитного | Norris & Ortega 2000 (49 исследований): явное — крупнее и устойчивее ([Wiley](https://onlinelibrary.wiley.com/doi/abs/10.1111/0023-8333.00136)); Spada & Tomita 2010 — для простых и сложных признаков ([Wiley](https://onlinelibrary.wiley.com/doi/abs/10.1111/j.1467-9922.2010.00562.x)); Goo et al. 2015 — g≈1.44 на свободной продукции ([Benjamins](https://benjamins.com/catalog/sibil.48.18goo)) **[E]** | Правило доступно **во всех 4 стилях** (в стиле 4 — по «почему?»). Стиль меняет порядок, а не наличие правила. |
| Явная информация об **L1** снижает межъязыковую интерференцию | McManus & Marsden 2017: L2+L1 explicit info + L1-практика улучшили и точность, и скорость онлайн-обработки L2 французского Imparfait ([ERIC](https://eric.ed.gov/?id=EJ1152557)); репликация 2018 ([SSLA](https://www.cambridge.org/core/journals/studies-in-second-language-acquisition/article/online-and-offline-effects-of-l1-practice-in-l2-grammar-learning/CA0DB509C645F4BF815CC6A69F6251D0)); устная продукция 2019 ([MLJ](https://onlinelibrary.wiley.com/doi/abs/10.1111/modl.12567)) **[E, малые выборки]** | Контрастные заметки нужны **для всех стилей** в feedback; стиль 3 выносит их на первое место. |
| Учащиеся ищут сходства; близкие языки дают положительный перенос | Ringbom 2007 ([Multilingual Matters](https://www.multilingual-matters.com/page/detail/Crosslinguistic-Similarity-in-Foreign-Language-Learning/?k=9781853599354)) **[E/теория]** | Маркировать тип переноса: `same`, `similar-form-diff-use`, `absent-in-L1`, `absent-in-L2`, `false-friend`. Для pl-ru самое ценное — «похоже, но иначе». |
| Retrieval practice | Adesope et al. 2017, 217 исследований ([AERA](https://journals.sagepub.com/doi/abs/10.3102/0034654316689306)); смешанные форматы тестов — сильнее **[E]** | Все стили заканчиваются самостоятельным ответом; смешивать устный/печатный/выбор. |
| Spacing в L2 | Kim & Webb 2022, 48 экспериментов ([Wiley](https://onlinelibrary.wiley.com/doi/abs/10.1111/lang.12479)) **[E]** | FSRS одинаков для всех стилей (уже так). |
| Interleaving | Brunmair & Richter 2019: работает для индуктивных категорий, **плохо для слов** ([PDF](https://www.uni-wuerzburg.de/fileadmin/06020400/2019/Brunmair_Richter_in_press__2019_META-ANALYSIS_OF_INTERLEAVED_LEARNING.pdf)); Nakata & Suzuki 2019: смешивание грамматических упражнений лучше через неделю ([MLJ](https://onlinelibrary.wiley.com/doi/10.1111/modl.12581)) **[E]** | Грамматика: сначала блок одного признака, затем смешивание (increasing). Словарь: не интерливить похожие слова насильно. |
| Практика → процедурализация | DeKeyser & Suzuki 2025, Skill Acquisition Theory ([preprint](https://yuichisuzuki.net/wp-content/uploads/2025/07/PreprintDeKeyser-R.-M.-Suzuki-Y.-2025.-Skill-acquisition-theory.-In-B.-VanPatten-G.-D.-Keating-S.-Wulff-Eds.-Theories-in-second-language-acquisition-An-introduction-4th-ed.-pp.-157-182-.pdf)) **[E/теория]** | Декларативное правило → много однотипных трансформаций → смешанные цепочки → свободная продукция. Прямо ложится на цепочку и дельты признаков. |
| Двойное кодирование / мультимедиа | Mayer CTML на базе Paivio ([Springer 2023](https://link.springer.com/article/10.1007/s10648-023-09842-1)); мета-анализ границ применимости ([ScienceDirect 2025](https://www.sciencedirect.com/science/article/pii/S1747938X25000673)) **[E]** | Схемы/таблицы — **всем** (вторичный канал), не только «технарям». |
| Визуальное выделение форм | Небольшой средний эффект, возможна цена для понимания смысла ([SSLA meta](https://www.cambridge.org/core/journals/studies-in-second-language-acquisition/article/abs/visual-input-enhancement-and-grammar-learning-a-metaanalytic-review/B9D0C50B09928C20C94548B37B29A042)) **[E]** | Контраст сохраняем, но не как основное средство. |
| Learning styles meshing | Pashler, McDaniel, Rohrer, Bjork 2008: нет адекватных доказательств взаимодействия «стиль × метод» ([PSPI](https://journals.sagepub.com/doi/full/10.1111/j.1539-6053.2009.01038.x)); мета-анализ 2024: g=0.31, но crossover лишь в 26% мер, низкое качество исследований, «слишком мало и редко» для внедрения ([Frontiers](https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2024.1428732/full)) **[E]** | Стили — **предпочтение и мотивация**, переключаемы в любой момент, без теста «определи свой тип». |

### Честная формулировка стилей для UI

> «Выберите, как вам удобнее начинать тему. Упражнения, повторения и прогресс одинаковы во всех вариантах; правило и сравнение с родным языком всегда доступны. Можно переключить в любой момент.»

Никаких «визуал/аудиал», «тип мышления», «подстроим под ваш мозг». В аналитике — сравнивать отложенное удержание при одинаковых заданиях и интервалах; если стиль не влияет на удержание, но влияет на completion/retention пользователей — это уже продуктовая ценность (мотивация), не когнитивная **[S]**.

---

## 6. (e) Морфология: правила vs таблицы vs гибрид

| Подход | Плюсы | Минусы | Для нас |
| --- | --- | --- | --- |
| FST/правила (foma — Apache 2.0, HFST, Apertium — GPL) | Компактно, покрывает любые леммы, агглютинация | Отдельный язык правил, отладка, лицензии, большой runtime; ошибки «переобобщения» видны учащемуся | Нет в runtime |
| Полные таблицы (UniMorph/SGJP/Wiktionary) | Нулевой runtime-код, точность источника, просто валидировать | Объём (но нам нужны сотни–тысячи лемм, не 450k), нет форм для новых слов пользователя | **Да, runtime** |
| Гибрид: парадигмы-шаблоны (класс склонения + основы + переопределения) | Автор пишет мало, покрывает новые слова, генерирует таблицы | Нужен мини-интерпретатор | **Да, но только на этапе сборки пакета** (+ опционально для пользовательских слов) |

Исследовательская опора гибрида: обобщение таблиц в парадигмы через LCS и компиляция в FST на 55 языках Wiktionary ([Forsberg/Hulden, paradigm extraction](https://www.researchgate.net/publication/301405072_Generalizing_Inflection_Tables_into_Paradigms_with_Finite_State_Operations)); ручные грамматики не хуже сильных нейросетей в SIGMORPHON на 13 языках ([Linguist vs Machine](https://lacuna.tiptreesystems.com/work/linguist-vs-machine-rapid-development-of-finite-state-morphological-grammars/wrk_a78b629796b0f8330e969dd1ff9c2f32)).

### Рекомендуемый декларативный мини-формат (сборка, Node)

```json
{
  "paradigms": {
    "pl.noun.f-a-hard": {
      "stems": ["base"],
      "cells": { "Nom|Sing": "{base}a", "Gen|Sing": "{base}y", "Acc|Sing": "{base}ę",
                 "Dat|Sing": "{base:soft}e", "Ins|Sing": "{base}ą", "...": "..." }
    }
  },
  "rewrites": {
    "soft": [["k$", "c"], ["g$", "dz"], ["r$", "rz"], ["n$", "ni"], ["t$", "ci"]]
  },
  "lexemes": [
    { "id": "wife", "lemma": "żona", "pos": "NOUN", "paradigm": "pl.noun.f-a-hard",
      "stems": { "base": "żon" }, "features": { "Gender": "Fem" },
      "overrides": { "Voc|Sing": "żono" }, "source": "sgjp@2024" }
  ]
}
```

- Турецкий: `{A}`/`{I}` архифонемы + правило гармонии в `rewrites` (ev-ler, kitap-lar). Арабский: `stems` = корень C1C2C3 и шаблоны `"{C1}a{C2}a{C3}a"`, ломаные множественные — `overrides`. Японский: godan/ichidan как парадигмы с таблицей мутации основы. Китайский: парадигм нет, всё в шаблонах конструкций.
- На выходе сборки **в пакет попадают только материализованные формы** (`forms: {"Nom|Sing": "żona", …}`) — именно так, как сегодня `course.json` хранит nouns/possessives. Runtime Kotlin = `lookup(lexemeId, bundle)`.
- Бонус для подсветки: генератор знает `stem` и `ending` каждой ячейки → **надёжная морфемная граница** в данных (`segments: ["żon","ę"]`) вместо эвристики общего префикса (ContrastHighlightPlan §1 это прямо требует).
- Периферастические формы (pl `będę robić`, `robiłem` с подвижным -em, en `will do`, de Perfekt) — не в морфологии, а в **шаблонах конструкций** (см. §7.3). Текущее польское правило прошедшего времени (stem + `m/em/ś/eś/śmy/ście`) материализуется при сборке в таблицу `Past|Person|Number|Gender`.
- Пользовательские слова: опционально подключить тот же интерпретатор парадигм в Kotlin (≈100 строк, язык-независимый) — либо запрашивать формы у пользователя/не тренировать их грамматически **[решение продукта]**.

---

## 7. Архитектура «универсального ядра»

### 7.1 Слои данных

```
courses/
  core/                         # один на продукт, язык-независим
    features.json               # UD-ключи/значения + UniMorph-алиасы + глоссы UI
    constructions.json          # скелет: конструкции, слоты, оси, уровни, prerequisites
    styles/                     # рецепты подачи (4 файла), без языкового текста
      rule-first.json  situation-first.json  l1-contrast.json  minimal.json
    exercise-kinds.json         # transform, fill-slot, translate-L1→L2, recognize, chain
  lang/
    pl/  lang.json              # inventory признаков, орфография, нормализация ответа, CLDR plural
         morphology/*.json      # парадигмы+rewrites (сборка) → forms (runtime)
         lexicon.json           # леммы + признаки + управление (valency: widzieć obj=Acc)
         realization.json       # как язык выражает каждую конструкцию/ось (шаблоны)
         curriculum.json        # какие конструкции/оси, порядок, уровни, skill IDs
    ru/  ...                    # родной язык — тот же формат (можно без curriculum)
    en/  ...
  pairs/
    pl-ru/ pair.json            # L1-контрасты, сцены, объяснения на ru, переводы, словарь
           legacy/course.json   # нынешний v1 — источник правды до parity gate
  build/ (generated)            # материализованные пакеты для хостов
```

Принципы расширяемости:
1. **Реестры по строковым ID, не enum’ы.** `Case`, `Gender`, `Tense` — значения из `core/features.json`, допустимость проверяется по `lang.json`. Kotlin-тип: `FeatureBundle = Map<FeatureKey, FeatureValue>` (value classes над String).
2. **Открытые множества с неймспейсом.** Язык может ввести `pl:` признак/конструкцию; core их не знает, но валидатор и UI работают (UI показывает `gloss` из пакета).
3. **Три независимых оси выбора пользователя:** target × L1 × style. Пара нужна только для L1-зависимого контента; если `pairs/pl-en` нет, но есть `lang/en` — работает **деградированный режим**: объяснения на en из generic-текстов `lang/pl` (написанных на «языке-посреднике»), без контрастов **[S: продуктовое решение]**.
4. **Каждый слой версионируется отдельно** (`coreVersion`, `langVersion`, `pairVersion`), пакет сборки фиксирует хеши.
5. **Никакого исполняемого кода в данных** (как сейчас: no HTML, no scripts). Шаблоны — плоский язык слотов с ограниченной грамматикой, валидируемый.

### 7.2 Модель навыка и упражнения

```json
{ "id": "case.gen.neg",
  "construction": "clause.action",
  "focus": { "feature": "Polarity", "from": "Pos", "to": "Neg" },
  "fixed": { "Tense": "Pres", "Person": "1", "Number": "Sing" },
  "lexicalFilter": { "slot": "object", "where": { "pos": "NOUN" } },
  "observes": ["Case:Acc→Gen@object"],
  "level": "A1", "prerequisites": ["case.acc.f"] }
```

- Упражнение = `realize(C, B₀)` → «задание: смени Polarity» → эталон `realize(C, B₁)`. Изменения (`changes`) = **поэлементный diff реализованных слотов** (слот `object`: `moją żonę → mojej żony`, причина из `realization.json`: «после nie дополнение в Dopełniaczu»). Это обобщает нынешние `exerciseCopy`+`exercisePatterns`+`when(skillId)` в `ExerciseFactory.kt`.
- Цепочка (5 шагов «Вижу → Прошлое → Отрицание → Владелец → Говорю о») = **последовательность дельт** над одним бандлом — уже данные, не код.
- Таблица Петрова = **декартово произведение осей** конструкции (Tense × ClauseType × Polarity × Person) → генерируется автоматически для любого языка из тех же шаблонов. Это даёт стиль 1 «из коробки».
- Навыки, специфичные для языка (pl `case.acc.m` с разветвлением по одушевлённости; ja вежливость; tr эвиденциальность), — те же объекты с `focus` по любому признаку; core не нужно знать о них заранее.

### 7.3 Реализация (realization.json) — минимальный язык шаблонов

```json
"clause.action": {
  "order": ["subject?", "neg?", "verb", "object"],
  "pro-drop": true,
  "slots": {
    "neg":    { "when": "Polarity=Neg", "text": "nie" },
    "verb":   { "lexeme": "$verb", "agree": ["Person","Number","Gender@subject"], "Tense": "$Tense" },
    "object": { "np": "$object", "Case": { "default": "gov($verb)", "Polarity=Neg&gov=Acc": "Gen" } }
  },
  "ClauseType=YesNoQ": { "prepend": "Czy", "punct": "?" },
  "Tense=Fut&Aspect=Imp": { "verb": { "periphrasis": ["aux:być|Fut", "lemma"] } }
}
```

- Признаки текут по согласованию (`agree`), управление (`gov`) — из лексикона, условные переопределения — плоские `when`-правила. Это «GF-lite»: абстрактное дерево = конструкция + бандл; линеаризация = данные. Для en `do-support`, для tr частица `mI` с гармонией (через `rewrites`), для zh выбор 不/没 по `Aspect`, для ar выбор لا/لم/لن по `Tense` — всё выражается `when`-правилами.
- Граница сложности: клитики (pl `-em` можно ставить в Wackernagel-позицию — учебно достаточно формы на глаголе), свободный порядок слов, эллипсис. Курс учит **каноническому** варианту + `accepted` альтернативы (уже есть в модели `Exercise.accepted`).
- Риск **[S]**: язык шаблонов разрастается в полноценный GF. Митигировать: держать ~10 операторов (`order`, `when`, `agree`, `gov`, `periphrasis`, `prepend/append`, `rewrite`, `optional`, `alt`, `punct`), всё, что сложнее, — `overrides` целыми фразами в `curriculum`.

### 7.4 Парный пакет pl-ru (L1-контраст)

```json
{ "constructionNotes": {
    "clause.action/Polarity=Neg": {
      "transfer": "similar-form-diff-use",
      "l1Realization": "auto",                 // realize(C, B) в пакете ru
      "note": "В русском «не вижу жену/жены» — оба возможны; в польском после nie только Dopełniacz: nie widzę żony.",
      "pitfall": "nie widzę żonę ✗"
  } },
  "scenes": { "clause.action/Polarity=Neg": [ { "id": "lost-keys", "text": "…", "constraints": {"object.animacy": "any"} } ] },
  "glosses": { "Case=Gen": "Dopełniacz (≈ родительный)" }
}
```

- `l1Realization: "auto"` — реализуем тот же бандл в `lang/ru` и показываем параллельно («не вижу жену» ↔ «nie widzę żony») с выравниванием слотов → контраст выделяется **по слотам**, а не эвристикой. Это новая возможность, которой, насколько я нашёл, нет в массовых продуктах **[S]**.
- `scenes` имеют `constraints` на признаки/лексемы — закрывает найденную в MethodologyCycleBlueprint проблему «сцена о книге друга, а генератор выдал другое» (валидатор перебирает генератор и проверяет совместимость).
- Если L1 не имеет морфологии в пакете — `l1Realization` задаётся авторскими строками или отсутствует.

### 7.5 Четыре стиля как данные

```json
{ "id": "l1-contrast", "label": {"ru": "Через родной язык", "en": "Through your language"},
  "phases": {
    "introduce": ["l1.parallel", "pair.note", "rule.short"],
    "retrieve":  ["prompt"],
    "feedback":  ["diff", "pair.pitfall", "rule.short", "table.link"],
    "review":    ["rating"] },
  "requires": ["pair.constructionNotes"], "fallback": "rule-first" }
```

| Стиль | introduce | feedback | Источник блоков |
| --- | --- | --- | --- |
| 1 rule-first («технарь») | `table.axes` (авто-таблица Петрова), `rule.formula`, `pipeline` | `diff`, `rule.full`, `table.highlight` | Генерируется из `realization`+`morphology`; формулы из `pair` |
| 2 situation-first | `scene`, `intent.question` | `diff`, `rule.short`, `scene.echo` | `pair.scenes` |
| 3 L1-contrast | `l1.parallel`, `pair.note` | `diff`, `pair.pitfall`, `rule.short` | `lang/ru` + `pair.constructionNotes` |
| 4 minimal | `examples(3)` (одна ось меняется) | `diff`, кнопка «почему?» → `rule.short` | Генерируется; правило скрыто за раскрытием |

- Хосты рендерят **типизированные блоки** (`table`, `parallel`, `scene`, `examples`, `rule`, `diff`) — добавление стиля = новый JSON, без кода, пока используются существующие типы блоков. Новый тип блока — единственный случай изменения хостов.
- `requires` + `fallback`: стиль недоступен для пары без контента → UI показывает его отключённым с объяснением, а не падает.
- Совместимость: `PreferredMethod.Logic → "rule-first"`, `Situations → "situation-first"`; кодек предпочтений читает старые значения, неизвестное → `rule-first`.

### 7.6 Прогресс и ID

- **Никогда не переименовывать** существующие skill IDs и vocab-ключи. `curriculum.json` pl содержит `skills[].id` ровно как сейчас; универсальная структура (`construction`/`focus`) — дополнительные поля.
- Рекомендация: грамматический прогресс привязать к **target language**, не к паре: `pl:case.gen.neg` (логически), а физически для pl оставить безпрефиксные ключи v1 и считать их неявным неймспейсом `pl`. Тогда смена L1 (ru→en) не сбрасывает польскую грамматику — ведь навык польский **[рекомендация]**. Словарь остаётся парным (`pl-ru:vocabulary:ru-pl:id`), потому что направление включает L1.
- Новые языки: ключи `{target}:{skillId}`; `ProgressCodec` v1 читается как `pl` с сохранением неизвестных полей (уже так устроен `ProgressDocument.source`).
- `UserPreferences.coursePair` → `{target, native, style}`; миграция: `"pl-ru"` → `{pl, ru}`.

### 7.7 Что меняется в Kotlin (разовая работа, затем 0 кода на язык)

| Сейчас | Станет |
| --- | --- |
| `GramCase/Gender/Person/Tense/PossessiveId` enum | `FeatureKey/FeatureValue` value classes + inventory из пакета; польские enum можно оставить как адаптер на переходе |
| `GrammarEngine.verbForm` c польским правилом суффиксов | `Morphology.lookup(lexeme, bundle)`; правило уходит в сборку (материализация) |
| `ExerciseFactory.when(skillId)` | `ConstructionRealizer` + `SkillSpec(construction, focus, fixed, filter)` |
| `exerciseCopy`/`exercisePatterns` | `pair` тексты + `realization` шаблоны |
| `methods.logic/situations` | `styles/*.json` + блоки из `pair` |
| `EndingHighlight` эвристика | сегменты `stem|ending` из сборки + слотовый diff |
| `CourseData.kt` (598 строк, pl-ru жёстко) | загрузчик слоёв core/lang/pair + валидация |

Оценка **[S]**: ядро realizer+morph lookup+style composer ≈ 600–900 строк common Kotlin + тесты; Node-сборщик парадигм/импорта ≈ 500–800 строк. Язык после этого — только данные.

---

## 8. Миграция pl-ru без поломки

1. **Shadow-режим.** Построить `lang/pl` + `pairs/pl-ru` из текущего `course.json` скриптом (формы копируются как есть; парадигмы — позже). Новый realizer генерирует упражнения параллельно старому `ExerciseFactory`; сравнение по pinned fixtures (`tests/fixtures/kotlin-parity/grammar.json`: 490 possessive, verb forms; P02/P03 exercise/chain) — **побайтно**, включая `changes`, `accepted`, `explanation`.
2. **skillMap**: для каждого из 16 ID — `construction+focus`. `mixed` и `aspect` — составные/лексические навыки: `mixed` = случайный выбор фокуса из списка; `aspect` = лексическая пара (`perfectivePair`) — фокус `Aspect Imp→Perf` при `Tense=Fut`.
3. **Переключение** только после parity на React/Kotlin JS/Wasm/native (как в предыдущих партиях); v1 `course.json` остаётся читаемым до удаления React baseline.
4. **Стили 3 и 4** добавляются как новые JSON; стиль 3 для pl-ru сначала использует существующий `reference.russianSupport` (4 строки) + авторские заметки для 16 навыков; авто-`l1Realization` появляется, когда будет `lang/ru` морфология (OpenCorpora, CC BY-SA 3.0 → SA для данных пакета).
5. **Второй язык как тест архитектуры.** Выбрать типологически **далёкий** второй target (например, en или es для ru-носителей — спрос, либо tr для проверки агглютинации), чтобы core не оказался «польским в костюме универсального» **[рекомендация]**. Критерий успеха: ноль изменений Kotlin, кроме новых типов блоков, если понадобились.

---

## 9. Проверка качества контента (масштабирование)

- **Комбинаторная валидация:** сборщик перебирает все `(skill × допустимые лексемы × оси)` и выдаёт список предложений на ревью; одобрение хранится хешем предложения (как editorial-журнал словаря). Неодобренные комбинации не попадают в очередь.
- **Round-trip-проверка признаков** через UD-парсер (Stanza/UDPipe) на этапе сборки: реализованное предложение → парсинг → сверка признаков с бандлом **[S: полезный автоматический фильтр, не замена носителю]**.
- **LLM-ассистент для авторинга** сцен/заметок L1 с обязательной человеческой проверкой — ускоряет пары N×M **[S]**; runtime-LLM для «почему?» — только как опция онлайн, не часть офлайн-ядра.
- Уровни: локальные метки, сверяемые с RLD/EGP/польскими стандартами; не заявлять «официальный CEFR».

---

## 10. Риски и открытые вопросы

1. **Переусложнение шаблонного языка** (→ GF). Митигировать списком операторов и overrides.
2. **ShareAlike** данных (Wiktionary/UniMorph/OpenCorpora/FrequencyWords): пакеты данных публикуются под BY-SA; при нежелании — предпочитать SGJP (BSD), Wikidata (CC0), CLDR, собственные данные.
3. **Взрыв пар N×M.** Парный слой обязателен только для стиля 3 и L1-объяснений; нужен «посредник» (en) как fallback.
4. **Грамматика ≠ парадигма**: числительные (pl 2–4/5+), вид (лексическая пара), вежливость (ja — регистр всей клаузы), классификаторы (zh 个/本 — атрибут лексемы `classifier`). Все выражаются признаками/лексиконом, но требуют авторской дисциплины.
5. **Прогресс при смене L1** — решить продуктово (моя рекомендация: грамматика по target, словарь по паре).
6. **Эффект стилей** не доказан — закладывать аналитику (completion, точность, отложенное удержание) с одинаковыми заданиями/интервалами.

---

## 11. Источники (сводно)

Петров: https://englishtexts.ru/misc/poliglot-angliyskiy-za-16-chasov · https://english-polyglot.ru/tablicy.html · https://poliglot16.ru/en/urok1/ · https://am-en.ru/techniques/polyglot.html · https://ru.wikipedia.org/wiki/Полиглот_(телепередача)
Методы: https://www.fluentin3months.com/reviews/michel-thomas-review/ · https://www.languagetransfer.org/courses · https://blog.duolingo.com/language-rules-learning-grammar-on-duolingo/ · https://www.grammaticalframework.org/lib/doc/synopsis.html · https://journals.colorado.edu/index.php/lilt/article/view/1205
Схемы: https://unimorph.github.io/schema/ · https://unimorph.github.io/doc/unimorph-schema.pdf · https://arxiv.org/pdf/2205.03608 · https://universaldependencies.org/u/feat/all.html · https://universaldependencies.org/u/feat/Polite.html · https://arxiv.org/pdf/1810.06743 · https://arxiv.org/html/2402.14411 · https://arxiv.org/pdf/2305.06335
CEFR: https://www.cambridge.org/elt/blog/2015/11/11/introducing-english-grammar-profile-1-building-profile/ · https://orca.cardiff.ac.uk/id/eprint/166496/1/ijcl.14086.oke.pdf · https://www.coe.int/en/web/common-european-framework-reference-languages/reference-level-descriptions · https://certyfikatpolski.pl/wp-content/uploads/2018/05/rozp_26_2_16.pdf · https://www.certyfikatpolski.uni.lodz.pl/informacje-dla-zdajacych/standardy-wymagan-egzaminacyjnych
Данные: https://morfeusz.sgjp.pl/doc/license/ · https://github.com/unimorph/pol · https://en.wiktionary.org/wiki/Wiktionary:Copyrights · https://kaikki.org/dictionary/rawdata.html · https://www.wikidata.org/wiki/Wikidata:Lexicographical_data/Documentation · https://github.com/pymorphy2/pymorphy2 · https://en.wiki.tatoeba.org/articles/show/using-the-tatoeba-corpus · https://github.com/hermitdave/FrequencyWords · https://github.com/rspeer/wordfreq · https://cldr.unicode.org/translation/getting-started/plurals · https://github.com/KubaCiolo/leksjo-dane
Доказательства: https://onlinelibrary.wiley.com/doi/abs/10.1111/0023-8333.00136 · https://onlinelibrary.wiley.com/doi/abs/10.1111/j.1467-9922.2010.00562.x · https://benjamins.com/catalog/sibil.48.18goo · https://eric.ed.gov/?id=EJ1152557 · https://onlinelibrary.wiley.com/doi/abs/10.1111/modl.12567 · https://www.multilingual-matters.com/page/detail/Crosslinguistic-Similarity-in-Foreign-Language-Learning/?k=9781853599354 · https://journals.sagepub.com/doi/abs/10.3102/0034654316689306 · https://onlinelibrary.wiley.com/doi/abs/10.1111/lang.12479 · https://www.uni-wuerzburg.de/fileadmin/06020400/2019/Brunmair_Richter_in_press__2019_META-ANALYSIS_OF_INTERLEAVED_LEARNING.pdf · https://onlinelibrary.wiley.com/doi/10.1111/modl.12581 · https://link.springer.com/article/10.1007/s10648-023-09842-1 · https://www.cambridge.org/core/journals/studies-in-second-language-acquisition/article/abs/visual-input-enhancement-and-grammar-learning-a-metaanalytic-review/B9D0C50B09928C20C94548B37B29A042 · https://journals.sagepub.com/doi/full/10.1111/j.1539-6053.2009.01038.x · https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2024.1428732/full · https://yuichisuzuki.net/wp-content/uploads/2025/07/PreprintDeKeyser-R.-M.-Suzuki-Y.-2025.-Skill-acquisition-theory.-In-B.-VanPatten-G.-D.-Keating-S.-Wulff-Eds.-Theories-in-second-language-acquisition-An-introduction-4th-ed.-pp.-157-182-.pdf
Морфология: https://www.researchgate.net/publication/301405072_Generalizing_Inflection_Tables_into_Paradigms_with_Finite_State_Operations · https://lacuna.tiptreesystems.com/work/linguist-vs-machine-rapid-development-of-finite-state-morphological-grammars/wrk_a78b629796b0f8330e969dd1ff9c2f32 · https://en.wikipedia.org/wiki/Foma_(software) · https://en.wikipedia.org/wiki/Apertium
