#!/usr/bin/env python3
"""Generate GeneratedCourseReference.kt: caseReferenceRows, referenceGenderNames."""
from __future__ import annotations
import hashlib, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseReference.kt"


def ks(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def main() -> None:
    lines = (BIOME / "reference.core-data").read_text("utf-8").splitlines()
    ref = [parse(l[10:]) for l in lines if l.startswith("REFERENCE ")][0]

    # caseReferenceRows
    case_rows = ref["caseRows"]
    case_lines = []
    for r in case_rows:
        skill = f'"{ks(r["skill"])}"' if r.get("skill") else "null"
        case_lines.append(
            f'    CaseReferenceRow(GramCase.fromId("{ks(r["id"])}"), "{ks(r["pl"])}", '
            f'"{ks(r["ru"])}", "{ks(r["question"])}", "{ks(r["trigger"])}", {skill}),'
        )

    # referenceGenderNames
    gn = ref["genderNames"]
    gn_entries = ", ".join(f'"{ks(k)}" to "{ks(v)}"' for k, v in gn.items())

    sha = hashlib.sha256((BIOME / "reference.core-data").read_bytes()).hexdigest()

    content = f"""\
// Generated from courses-biome/pl-ru/reference.core-data; do not edit.
// SHA-256: {sha}
package polski.data

import polski.model.GramCase

/** Typed pl-ru caseReferenceRows from Biome literal source. */
internal val generatedPlRuCaseReferenceRows: List<CaseReferenceRow> = listOf(
{"".join(l + chr(10) for l in case_lines)}
)

/** Typed pl-ru referenceGenderNames from Biome literal source. */
internal val generatedPlRuReferenceGenderNames: Map<String, String> = mapOf({gn_entries})
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")
    print(f"  caseRows={len(case_rows)}, genderNames={len(gn)}")


if __name__ == "__main__":
    main()
