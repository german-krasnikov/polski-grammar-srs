#!/usr/bin/env python3
"""Generate GeneratedCourseTeaching.kt: caseTeaching, verbTeaching, pronounTeaching,
russianSupport, futureAuxiliary."""
from __future__ import annotations
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseTeaching.kt"


def ks(s: str) -> str:
    if not isinstance(s, str): s = str(s)
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def read_tag(fname: str, tag: str) -> object:
    lines = (BIOME / fname).read_text("utf-8").splitlines()
    for l in lines:
        if l.startswith(tag + " "):
            return parse(l[len(tag)+1:])
    raise ValueError(f"{tag} not found in {fname}")


def verb_label(vl: dict) -> str:
    return f'VerbLabel("{ks(vl["full"])}", "{ks(vl["compact"])}")'


def main() -> None:
    ref = read_tag("reference.core-data", "REFERENCE")
    morph = read_tag("morphology.core-data", "MORPHOLOGY")

    ct = ref["caseTeaching"]
    cn = ct["caseNote"]
    case_teaching_kt = (f'CaseTeaching("{ks(cn["react"])}", "{ks(cn["compact"])}", '
                        f'"{ks(ct["comparisonReadingHint"])}")')

    # VerbTeaching
    vt = ref["verbTeaching"]
    subj_list = []
    for s in vt["subjects"]:
        gm = s.get("genderMode", "fixed")
        selected = "true" if gm == "selected" else "false"
        person_map = {"1": "FIRST", "2": "SECOND", "3": "THIRD"}
        person_kt = f'Person.{person_map[str(s["person"])]}'
        number_kt = f'NumberGram.fromId("{ks(s["number"])}")'
        gender_raw = s.get("fixedGender")
        gender_kt = f'Gender.fromId("{ks(gender_raw)}")' if gender_raw else "null"
        subj_list.append(
            f'VerbSubject("{ks(s["id"])}", {person_kt}, {number_kt}, '
            f'{selected}, {gender_kt}, {verb_label(s["label"])})'
        )
    go_list = [f'VerbGenderOption("{ks(g["id"])}", {verb_label(g["label"])})' for g in vt["genderOptions"]]
    tense_map_entries = []
    tense_kotlin = {"present": "Tense.PRESENT", "past": "Tense.PAST", "future": "Tense.FUTURE"}
    for t_id, t_label in vt["tenseLabels"].items():
        tense_map_entries.append(f'{tense_kotlin[t_id]} to {verb_label(t_label)}')
    fu_exp = vt.get("futureExplanation", {})
    fu_react = ks(fu_exp.get("react", "")) if isinstance(fu_exp, dict) else ""
    fu_compact = ks(fu_exp.get("compact", "")) if isinstance(fu_exp, dict) else ""
    verb_teaching_kt = (
        f'VerbTeaching(listOf({", ".join(subj_list)}), '
        f'{verb_label(vt["genderControlLabel"])}, '
        f'listOf({", ".join(go_list)}), '
        f'mapOf({", ".join(tense_map_entries)}), '
        f'"{fu_react}", "{fu_compact}")'
    )

    # PronounTeaching
    pt = ref["pronounTeaching"]
    pers = pt["personal"]
    poss = pt["possessive"]
    demo = poss.get("demo", {})
    demo_noun = ks(demo.get("nounId", "wife"))
    demo_adj = ks(demo.get("adjectiveId", "beautiful"))
    demo_number = f'NumberGram.fromId("{ks(demo.get("number", "sg"))}")'  
    invar_ids = demo.get("invariableOwnerIds", [])
    invar_kt = ", ".join(f'PossessiveId.fromId("{ks(i)}")' for i in invar_ids)
    demo_rule = demo.get("rule", {})
    demo_cases = []
    for dc in demo.get("cases", []):
        caption = dc.get("caption", {})
        demo_cases.append(
            f'PossessiveDemoCase(GramCase.fromId("{ks(dc["id"])}"), '
            f'"{ks(caption.get("web",""))}", "{ks(caption.get("desktop",""))}", '
            f'"{ks(caption.get("ios",""))}")')
    poss_demo_kt = (f'PossessiveDemo("{demo_noun}", "{demo_adj}", {demo_number}, '
                    f'listOf({", ".join(demo_cases)}), setOf({invar_kt}), '
                    f'"{ks(demo_rule.get("invariable",""))}", '
                    f'"{ks(demo_rule.get("variable",""))}")')

    ctx_list = []
    for c in pt.get("contexts", []):
        cue = c["cue"]
        spec = c.get("specialValues", {})
        spec_entries = ", ".join(f'"{ks(k)}" to "{ks(v)}"' for k, v in spec.items())
        ctx_list.append(
            f'PronounContext(GramCase.fromId("{ks(c["caseId"])}"), '
            f'PronounCue("{ks(cue["full"])}", "{ks(cue["compact"])}", "{ks(cue.get("ios", cue["compact"]))}"), '
            f'"{ks(c.get("caseName",""))}", "{ks(c.get("valuePrefix",""))}", '
            f'mapOf({spec_entries}))'
        )
    pron_intro = pers.get("intro", {})
    footer = pers.get("footer", {})
    pronounIds_kt = ", ".join(f'"{ks(pid)}"' for pid in pers.get("pronounIds", []))
    pron_teaching_kt = (
        f'PronounTeaching("{ks(pers.get("title",""))}", '
        f'"{ks(pron_intro.get("react","") if isinstance(pron_intro, dict) else "")}", '
        f'"{ks(pron_intro.get("compact","") if isinstance(pron_intro, dict) else "")}", '
        f'"{ks(footer.get("react",""))}", "{ks(footer.get("web",""))}", "{ks(footer.get("native",""))}", '
        f'listOf({pronounIds_kt}), '
        f'listOf({", ".join(ctx_list)}), '
        f'"{ks(poss.get("title",""))}", {poss_demo_kt})'
    )

    # RussianSupport
    rs = ref["russianSupport"]
    cols = ", ".join(f'"{ks(c)}"' for c in rs.get("columns", []))

    def ending_parts(parts):
        ep = [f'EndingPart("{ks(p["text"])}", {str(p.get("isEnding", False)).lower()}, {str(p.get("isChanged", False)).lower()})' for p in parts]
        return f'listOf({", ".join(ep)})'

    def rs_table(t):
        return f'RussianSupportTable("{ks(t.get("construction",""))}", "{ks(t.get("check",""))}")'

    rs_rows = []
    for r in rs.get("rows", []):
        contrasts = ", ".join(
            f'ContrastPair("{ks(c["from"])}", "{ks(c["to"])}", {ending_parts(c.get("beforeParts",[]))}, {ending_parts(c.get("afterParts",[]))})' for c in r.get("comparisons", [])
        )
        rs_rows.append(
            f'RussianSupportRow("{ks(r["id"])}", "{ks(r["cue"])}", ' +
            f'{rs_table(r.get("react",{}))}, {rs_table(r.get("web",{}))}, ' +
            f'{rs_table(r.get("desktop",{}))}, "{ks(r.get("mobileLine",""))}", ' +
            f'listOf({contrasts}))'
        )
    rs_title = rs.get("title", {})
    if isinstance(rs_title, dict):
        full_title = ks(rs_title.get("full", ""))
        compact_title = ks(rs_title.get("compact", ""))
    else:
        full_title = compact_title = ks(rs_title)
    russian_support_kt = (f'RussianSupport("{full_title}", "{compact_title}", listOf({cols}), listOf({", ".join(rs_rows)}))')

        # FutureAuxiliary
    fa = morph.get("futureAuxiliary", {})
    fa_verb = fa.get("verbId", "be")
    fa_forms = fa.get("forms", {})
    num_entries = []
    for num_id, persons in fa_forms.items():
        p_entries = ", ".join(f'Person.fromId({int(p)}) to "{ks(form)}"' for p, form in persons.items())
        num_entries.append(f'NumberGram.fromId("{ks(num_id)}") to mapOf({p_entries})')
    future_aux_kt = f'FutureAuxiliary("{ks(fa_verb)}", mapOf({", ".join(num_entries)}))'

    content = f"""\
// Generated from courses-biome/pl-ru/; do not edit.
package polski.data

import polski.model.Gender
import polski.model.GramCase
import polski.model.NumberGram
import polski.model.Person
import polski.model.PossessiveId
import polski.model.Tense
import polski.presentation.ContrastPair
import polski.presentation.EndingPart

internal val generatedPlRuCaseTeaching: CaseTeaching = {case_teaching_kt}
internal val generatedPlRuVerbTeaching: VerbTeaching = {verb_teaching_kt}
internal val generatedPlRuPronounTeaching: PronounTeaching = {pron_teaching_kt}
internal val generatedPlRuRussianSupport: RussianSupport = {russian_support_kt}
internal val generatedPlRuFutureAuxiliary: FutureAuxiliary = {future_aux_kt}
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
