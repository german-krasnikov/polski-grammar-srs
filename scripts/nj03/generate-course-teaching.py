#!/usr/bin/env python3
"""Generate GeneratedCourseTeaching.kt: pronounTeaching and russianSupport (R1.10b takes them over)."""
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


def main() -> None:
    ref = read_tag("reference.core-data", "REFERENCE")

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

    content = f"""\
// Generated from courses-biome/pl-ru/; do not edit.
package polski.data

import polski.model.GramCase
import polski.model.NumberGram
import polski.model.PossessiveId
import polski.presentation.ContrastPair
import polski.presentation.EndingPart

internal val generatedPlRuPronounTeaching: PronounTeaching = {pron_teaching_kt}
internal val generatedPlRuRussianSupport: RussianSupport = {russian_support_kt}
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
