package polski.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * EN-21 (`Plans/Kotlin/EnRuPackPlan.md` §4.2/§6): [LifehackProvider] is the reserved port
 * (`Plans/Kotlin/UniversalCorePlan.md` §5.4's `ExplanationProvider`/`AudioProvider` pattern),
 * [StaticPackLifehackProvider] its only implementation today — reads the active pack's
 * `pairs/<pairId>/lifehacks.json` (ADR-15: lifehacks are pair-scoped, outside core).
 */
class LifehackTest {
    // Parser correctness against a local fixture, the same way StyleRegistryTest exercises
    // parseStyleRecipesJson without going through the real embedded JSON.
    @Test fun parsesEveryFieldFromLifehacksV1Json() {
        val json = """
            {
              "schemaVersion": 1,
              "pairId": "en-ru",
              "lifehacks": [
                {
                  "id": "en-ru.role.object.article",
                  "skillId": "en:role.object",
                  "topic": null,
                  "text": "В русском нет артиклей вообще.",
                  "source": { "kind": "research", "citation": "Master, P. (1997).", "url": "https://doi.org/10.1016/S0346-251X(96)00063-8" },
                  "status": "editorial",
                  "votes": { "helpful": 0, "notHelpful": 0 }
                },
                {
                  "id": "en-ru.articles.general",
                  "skillId": null,
                  "topic": "articles",
                  "text": "Кросс-скилловый совет про артикли.",
                  "source": { "kind": "teaching-practice", "citation": "Language Transfer." },
                  "status": "community",
                  "votes": { "helpful": 0, "notHelpful": 0 }
                }
              ]
            }
        """.trimIndent()
        val parsed = parseLifehacksJson(json)
        assertEquals(2, parsed.size)
        val first = parsed[0]
        assertEquals("en-ru.role.object.article", first.id)
        assertEquals("en:role.object", first.skillId)
        assertEquals(null, first.topic)
        assertEquals("В русском нет артиклей вообще.", first.text)
        assertEquals(LifehackSourceKind.Research, first.source.kind)
        assertEquals("Master, P. (1997).", first.source.citation)
        assertEquals("https://doi.org/10.1016/S0346-251X(96)00063-8", first.source.url)
        assertEquals(LifehackStatus.Editorial, first.status)
        val second = parsed[1]
        assertEquals(null, second.skillId)
        assertEquals("articles", second.topic)
        assertEquals(LifehackSourceKind.TeachingPractice, second.source.kind)
        assertEquals(null, second.source.url)
        assertEquals(LifehackStatus.Community, second.status)
    }

    @Test fun parserNeverThrowsOnAnEmptyLifehackList() {
        val json = """{"schemaVersion":1,"pairId":"en-ru","lifehacks":[]}"""
        assertEquals(emptyList(), parseLifehacksJson(json))
    }

    // Real content, both packs: pl-ru's first authored 5 (EN-20) prove the port isn't en-ru-only.
    @Test fun staticProviderReturnsPlRusRealAuthoredLifehackForCaseGenNeg() {
        val hacks = StaticPackLifehackProvider.forSkill("case.gen.neg")
        assertTrue(hacks.isNotEmpty(), "case.gen.neg should have pl-ru's EN-20 authored lifehack")
        assertTrue(hacks.all { it.skillId == "case.gen.neg" })
        assertEquals(LifehackStatus.Editorial, hacks.first().status)
    }

    // Empty, not a crash and not a placeholder entry — §4.3's "пусто -> блок не рисуется вообще".
    @Test fun staticProviderReturnsEmptyForASkillWithNoAuthoredLifehack() {
        assertEquals(emptyList(), StaticPackLifehackProvider.forSkill("no.such.skill.exists"))
    }

    // A cross-skill lifehack (skillId == null) must never surface through forSkill(realId) —
    // §4.1 reserves null+topic for cross-skill advice the per-skill card doesn't render (EN-21 web
    // scope is the per-skill collapsible block only).
    @Test fun aCrossSkillLifehackNeverMatchesAnyRealSkillId() {
        val json = """
            {"schemaVersion":1,"pairId":"en-ru","lifehacks":[
              {"id":"x","skillId":null,"topic":"t","text":"t","source":{"kind":"project-authored","citation":"c"},"status":"editorial","votes":{"helpful":0,"notHelpful":0}}
            ]}
        """.trimIndent()
        assertEquals(emptyList(), parseLifehacksJson(json).filter { it.skillId == "role.object" })
    }
}
