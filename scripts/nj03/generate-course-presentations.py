#!/usr/bin/env python3
"""Generate GeneratedCoursePresentations.kt: presentations and styleContent from skills."""
from __future__ import annotations
import hashlib, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCoursePresentations.kt"


def ks(s: str) -> str:
    if not isinstance(s, str): s = str(s)
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def method(m: dict) -> str:
    return (f'MethodPresentation("{ks(m["introduction"])}", "{ks(m["promptLead"])}", '
            f'"{ks(m["introduce"])}", "{ks(m["retrieve"])}", '
            f'"{ks(m["feedback"])}", "{ks(m["review"])}")')


def presentation(s: dict) -> str:
    f = s["focus"]
    meth = s["methods"]
    return (f'SkillPresentation("{ks(f["before"])}", "{ks(f["after"])}", '
            f'{method(meth["logic"])}, {method(meth["situations"])})')


def style_content(s: dict) -> str:
    sc = s.get("styleContent", {})
    np_list = sc.get("nativeParallel", [])
    np_kt = ", ".join(
        f'NativeParallelPair("{ks(p["native"])}", "{ks(p["target"])}", '
        f'"{ks(p["note"])}", {str(p["matches"]).lower()}, emptyList())'
        for p in np_list
    )
    examples = sc.get("examples", [])
    ex_kt = ", ".join(f'"{ks(e)}"' for e in examples)
    why = sc.get("why")
    why_kt = f'"{ks(why)}"' if why else "null"
    rule = sc.get("rule")
    rule_kt = f'"{ks(rule)}"' if rule else "null"
    return (f'SkillStyleContent(rule={rule_kt}, table=null, scene=null, '
            f'nativeParallel=listOf({np_kt}), examples=listOf({ex_kt}), why={why_kt})')


def main() -> None:
    lines = (BIOME / "skills.core-data").read_text("utf-8").splitlines()
    skills = [parse(l[6:]) for l in lines if l.startswith("SKILL ")]

    pres_lines = [f'    "{ks(s["id"])}" to {presentation(s)},' for s in skills]
    style_lines = [f'    "{ks(s["id"])}" to {style_content(s)},' for s in skills]

    sha = hashlib.sha256((BIOME / "skills.core-data").read_bytes()).hexdigest()
    content = f"""\
// Generated from courses-biome/pl-ru/skills.core-data; do not edit.
// SHA-256: {sha}
package polski.data

import polski.presentation.NativeParallelPair

/** Typed pl-ru presentations from Biome literal source. */
internal val generatedPlRuPresentations: Map<String, SkillPresentation> = mapOf(
{"".join(l + chr(10) for l in pres_lines)}
)

/** Typed pl-ru styleContent from Biome literal source. */
internal val generatedPlRuStyleContent: Map<String, SkillStyleContent> = mapOf(
{"".join(l + chr(10) for l in style_lines)}
)
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)} ({len(skills)} skills)")


if __name__ == "__main__":
    main()
