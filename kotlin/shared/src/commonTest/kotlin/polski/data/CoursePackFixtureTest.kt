package polski.data

import polski.model.Skill
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * UniversalCorePlan.md §12 UC-02 acceptance: what pl-ru's skills are at runtime must not change when the way they
 * reach Kotlin changes. [expectedSkills] is the golden fixture of `PolishCourseData.skills` (now [packRegistry]
 * `.active.skills`) pinned 2026-09-27 at commit fa9526a, before that refactor. R1.3 only changed its form: the
 * pinned JSON text became these typed values, field for field (16 skills, same order).
 */
class CoursePackFixtureTest {
    @Test fun skillsEqualThePinnedPreRefactorFixture() {
        assertEquals(expectedSkills, packRegistry.active.skills)
    }
}

private val expectedSkills = listOf(
    Skill("case.acc.n", "Biernik · средний род", "Przypadki", "A1",
        "Средний род: Biernik = Mianownik", "В единственном числе средний род сохраняет базовую форму в Bierniku: widzę moje małe dziecko.", "moje nowe okno → widzę moje nowe okno",
        emptyList()),
    Skill("case.acc.f", "Biernik · żeński", "Przypadki", "A1",
        "-a → -ę; прилагательное -a → -ą", "Прямой объект после widzę, mam, lubię, kupuję обычно стоит в Bierniku.", "Widzę kogo? co?",
        emptyList()),
    Skill("case.acc.m", "Biernik · męski", "Przypadki", "A2",
        "на согласную: живое = Gen; неживое = Nom", "Для мужских существительных на согласную: widzę męża/psa, но widzę dom/samochód. У существительных на -a: widzę kolegę, но nie widzę kolegi.", "Сначала определи: żywotny или nieżywotny.",
        listOf("case.acc.f")),
    Skill("case.gen.neg", "Dopełniacz · negacja", "Przypadki", "A2",
        "Mam X → Nie mam + GEN", "При отрицании прямого объекта Biernik очень часто превращается в Dopełniacz.", "Не меняй только nie — поменяй весь noun phrase.",
        listOf("case.acc.f")),
    Skill("case.inst", "Narzędnik · z / być", "Przypadki", "A1",
        "z + kim? czym? → INST", "После z в значении «с кем/чем» используем Narzędnik. Также: Jestem programistą.", "z moją żoną, z dobrym kolegą",
        emptyList()),
    Skill("case.loc", "Miejscownik · o", "Przypadki", "A2",
        "o + kim? czym? → LOC", "После o (о ком/о чём) нужен Miejscownik.", "mówię o żonie / domu",
        listOf("case.inst")),
    Skill("case.dat", "Celownik · komu?", "Przypadki", "A2",
        "daję/pomagam + DAT", "Celownik отвечает «кому? чему?». Учим через короткие устойчивые конструкции.", "Daję prezent komu?",
        emptyList()),
    Skill("agreement.my", "Мой, твой, его, наш, их", "Zgoda", "A2",
        "Владелец + прилагательное + существительное", "mój, twój, nasz, wasz согласуются с существительным. jego, jej, ich не склоняются.", "moją piękną żonę → ich piękną żonę",
        listOf("case.acc.f", "case.gen.neg")),
    Skill("verb.present", "Czas teraźniejszy", "Czasownik", "A1",
        "лицо × число → окончание", "Автоматизируем 6 лиц на высокочастотных глаголах.", "ja/ty/on/my/wy/oni",
        emptyList()),
    Skill("verb.past", "Czas przeszły", "Czasownik", "A2",
        "основа прошедшего + род + лицо", "В прошедшем времени форма отражает род говорящего/подлежащего и лицо.", "robił-em, robiła-m, robili-śmy",
        listOf("verb.present")),
    Skill("verb.future", "Czas przyszły", "Czasownik", "A2",
        "będę + инфинитив (несов.) / форма настоящего (сов.)", "Для несовершенного вида: będę robić. Совершенный вид имеет простое будущее: zrobię.", "będę czytać / kupię",
        listOf("verb.present")),
    Skill("aspect", "Aspekt · robić/zrobić", "Czasownik", "B1",
        "процесс/повторение ↔ завершённый результат", "Недоконанный вид описывает процесс/повторяемость, dokonany — завершённый результат.", "robiłem vs zrobiłem",
        listOf("verb.past", "verb.future")),
    Skill("pronouns", "Zaimki osobowe", "Zaimki", "A2",
        "ona → ją / jej / nią / niej", "Тренируем местоимение сразу внутри конструкции, а не как отдельную таблицу.", "widzę ją; mówię o niej; idę z nią",
        listOf("case.inst", "case.loc")),
    Skill("mixed", "Смешанные трансформации", "B1 Core", "B1",
        "Лицо + время + отрицание + падеж", "Смешанные карточки проверяют, можешь ли ты объединить правила в живом предложении.", "Сначала структура, затем окончания.",
        listOf("agreement.my", "verb.past")),
    Skill("sentence.question", "Вопрос с Czy", "Предложение", "A1",
        "Czy + утверждение → вопрос", "В общем вопросе czy ставится перед предложением. Падежи при этом не меняются.", "Widzisz… → Czy widzisz…?",
        emptyList()),
    Skill("sentence.plural", "Единственное → множественное", "Предложение", "A2",
        "Местоимение + прилагательное + существительное → множественное число", "Во множественном числе различай мужскую личную группу и все остальные.", "mojego kolegę → moich kolegów; moją książkę → moje książki",
        listOf("case.acc.m", "agreement.my")),
)
