#!/usr/bin/env python3
"""Generate EnRuTypedPack fields from en-ru JSON sources.
One generator, one output file — P7/P22."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PAIR = json.load(open(ROOT / "courses/pairs/en-ru/pair.json"))
LEXICON = json.load(open(ROOT / "courses/lang/en/lexicon.json"))
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedEnRuPack.kt"


def ks(s):
    if not isinstance(s, str): s = str(s)
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def method_kt(m):
    return (f'MethodPresentation("{ks(m["introduction"])}", "{ks(m["promptLead"])}", '
            f'"{ks(m["introduce"])}", "{ks(m["retrieve"])}", "{ks(m["feedback"])}", '
            f'"{ks(m.get("review",""))}")')


def skill_kt(s):
    return (f'Skill("{ks(s["id"])}", "{ks(s["title"])}", "{ks(s["group"])}", "{ks(s["level"])}", '
            f'"{ks(s["formula"])}", "{ks(s["theory"])}", "{ks(s["hint"])}", '
            f'listOf({", ".join(f"{chr(34)}{ks(p)}{chr(34)}" for p in s.get("prerequisites", []))}))')


def pres_kt(s):
    focus = s.get("focus", {})
    methods = s.get("methods", {})
    logic = method_kt(methods.get("logic", {"introduction":"","promptLead":"","introduce":"","retrieve":"","feedback":""}))
    situations = method_kt(methods.get("situations", {"introduction":"","promptLead":"","introduce":"","retrieve":"","feedback":""}))
    return (f'"{ks(s["id"])}" to SkillPresentation("{ks(focus.get("before",""))}", '
            f'"{ks(focus.get("after",""))}", {logic}, {situations})')


def style_content_kt(s):
    sc = s.get("styleContent", {})
    if not sc:
        return None
    np = sc.get("nativeParallel", [])
    np_items = [f'NativeParallelPair("{ks(p["native"])}", "{ks(p["target"])}", "{ks(p.get("note",""))}", {str(p.get("matches",False)).lower()}, emptyList())' for p in np]
    examples = sc.get("examples", [])
    ex_items = [f'"{ks(e)}"' for e in examples]
    rule = sc.get("rule")
    table = sc.get("table")
    scene = sc.get("scene")
    why = sc.get("why")
    parts = []
    if rule: parts.append(f'rule = "{ks(rule)}"')
    if table:
        rows = [f'TableRow({", ".join(f"{chr(34)}{ks(c)}{chr(34)}" for c in r)})' for r in table]
        parts.append(f'table = listOf({", ".join(rows)})')
    if scene: parts.append(f'scene = "{ks(scene)}"')
    if np_items: parts.append(f'nativeParallel = listOf({", ".join(np_items)})')
    if ex_items: parts.append(f'examples = listOf({", ".join(ex_items)})')
    if why: parts.append(f'why = "{ks(why)}"')
    return f'"{ks(s["id"])}" to SkillStyleContent({", ".join(parts)})'


def seed_kt(s):
    return f'SentenceSeed("{ks(s["nounId"])}", "{ks(s["adjectiveId"])}")'


def vocab_kt(v):
    return (f'VocabularyItem("{ks(v["id"])}", "{ks(v["lemma"])}", "{ks(v["translation"])}", '
            f'"{ks(v.get("form", ""))}", "{ks(v.get("example", ""))}", "{ks(v.get("level", "A1"))}", '
            f'null, false)')


def main():
    skills = [skill_kt(s) for s in PAIR["skills"]]
    seeds = [seed_kt(s) for s in PAIR["sentenceSeeds"]]
    pres_entries = [pres_kt(s) for s in PAIR["skills"]]
    style_entries = [style_content_kt(s) for s in PAIR["skills"]]
    style_entries = [e for e in style_entries if e is not None]

    def map_entries(d):
        return ", ".join(f'"{ks(k)}" to "{ks(v)}"' for k, v in d.items())

    copy_kt = map_entries(PAIR["exerciseCopy"])
    patterns_kt = map_entries(PAIR["exercisePatterns"])
    prefixes_kt = map_entries(PAIR["caseSentencePrefixes"])

    chain = PAIR["training"]["chainPresentation"]
    steps = ", ".join(f'ChainStep("{ks(s["id"])}", "{ks(s["label"])}")' for s in chain["steps"])
    compl = chain["completion"]
    completion_kt = f'ChainCompletion("{ks(compl["title"])}", "{ks(compl["reactEyebrow"])}", "{ks(compl["reactBody"])}", "{ks(compl["webBody"])}")'

    voc = PAIR["vocabulary"]
    items = [vocab_kt(v) for v in voc["items"]]
    instr = voc.get("instructions", {})
    unavail = ks(voc.get("unavailableLabel", ""))

    noun_labels = [f'"{ks(n["id"])}" to ("{ks(n["lemma"])}" to "{ks(n["meaning"])}")' for n in LEXICON["nouns"]]

    # en pronouns use Subj/Obj, not GramCase — personalPronouns is empty, raw forms only
    pp = LEXICON.get("personalPronouns", {})
    pp_raw = []
    for pid, forms in pp.items():
        if isinstance(forms, dict):
            raw_inner = ", ".join(f'"{ks(c)}" to "{ks(f)}"' for c, f in forms.items())
            pp_raw.append(f'"{ks(pid)}" to mapOf({raw_inner})')

    poss = LEXICON.get("possessives", [])
    poss_items = [f'Possessive(PossessiveId.fromId("{ks(p["id"])}"), "{ks(p.get("label",""))}")' for p in poss]

    NL = chr(10)
    content = f"""\
// Generated from courses/pairs/en-ru/pair.json + courses/lang/en/lexicon.json; do not edit.
package polski.data

import polski.model.Adjective
import polski.model.GramCase
import polski.model.Noun
import polski.model.PossessiveId
import polski.model.SentenceSeed
import polski.model.Skill
import polski.model.Verb
import polski.presentation.NativeParallelPair

// en nouns/adjectives/verbs have no pl-shaped case/gender forms -> empty.
internal val generatedEnRuNouns: List<Noun> = emptyList()
internal val generatedEnRuAdjectives: List<Adjective> = emptyList()
internal val generatedEnRuVerbs: List<Verb> = emptyList()
internal val generatedEnRuNounLabels: Map<String, Pair<String, String>> = mapOf(
    {("," + NL + "    ").join(noun_labels)}
)
internal val generatedEnRuSkills: List<Skill> = listOf(
    {("," + NL + "    ").join(skills)}
)
internal val generatedEnRuSentenceSeeds: List<SentenceSeed> = listOf(
    {("," + NL + "    ").join(seeds)}
)
internal val generatedEnRuCaseSentencePrefixes: Map<String, String> = mapOf({prefixes_kt})
internal val generatedEnRuExerciseCopy: Map<String, String> = mapOf({copy_kt})
internal val generatedEnRuExercisePatterns: Map<String, String> = mapOf({patterns_kt})
internal val generatedEnRuChainPresentation = ChainPresentation(listOf({steps}), {completion_kt})
internal val generatedEnRuVocabularyItems: List<VocabularyItem> = listOf(
    {("," + NL + "    ").join(items)}
)
internal val generatedEnRuVocabularyInstructions = CourseVocabularyInstructions("{ks(instr.get("react",""))}", "{ks(instr.get("web",""))}", "{ks(instr.get("native",""))}", "{ks(instr.get("ios",""))}")
internal val generatedEnRuVocabularyUnavailableLabel = "{unavail}"
internal val generatedEnRuPresentations: Map<String, SkillPresentation> = mapOf(
    {("," + NL + "    ").join(pres_entries)}
)
internal val generatedEnRuStyleContent: Map<String, SkillStyleContent> = mapOf(
    {("," + NL + "    ").join(style_entries) if style_entries else ""}
)
// en pronouns use Subj/Obj, not GramCase -> typed map is empty.
internal val generatedEnRuPersonalPronouns: Map<String, Map<GramCase, String>> = emptyMap()
internal val generatedEnRuPersonalPronounRawForms: Map<String, Map<String, String>> = mapOf(
    {("," + NL + "    ").join(pp_raw)}
)
internal val generatedEnRuPossessives: List<Possessive> = listOf(
    {("," + NL + "    ").join(poss_items)}
)
internal val generatedEnRuFrequency: List<FrequencyItem> = emptyList()

// En-specific data for EnLexicon.kt (matrix tables)
internal val generatedEnVerbs: List<EnVerb> = listOf(
    {("," + NL + "    ").join(f'EnVerb("{ks(v["id"])}", "{ks(v["lemma"])}", "{ks(v["meaning"])}")' for v in LEXICON["verbs"])}
)
internal val generatedEnPronouns: List<EnPronoun> = listOf(
    {("," + NL + "    ").join(f'EnPronoun("{ks(pid)}", "{ks(forms.get("Subj", pid))}")' for pid, forms in LEXICON["personalPronouns"].items() if isinstance(forms, dict))}
)
"""
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")
    print(f"  skills={len(skills)}, vocab={len(items)}, pres={len(pres_entries)}, style={len(style_entries)}")


if __name__ == "__main__":
    main()
