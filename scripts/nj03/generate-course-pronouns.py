#!/usr/bin/env python3
"""Generate GeneratedCoursePronouns.kt: personalPronouns, possessives, possessiveForms."""
from __future__ import annotations
import hashlib, sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCoursePronouns.kt"


def ks(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def read(fname: str, tag: str) -> list:
    return [parse(l[len(tag)+1:]) for l in (BIOME / fname).read_text("utf-8").splitlines()
            if l.startswith(tag + " ")]


def emit_case_map(forms: dict) -> str:
    entries = ", ".join(f'GramCase.fromId("{ks(k)}") to "{ks(v)}"' for k, v in forms.items())
    return f"mapOf({entries})"


def emit_gender_map(gmap: dict) -> str:
    entries = ", ".join(
        f'Gender.fromId("{ks(g)}") to {emit_case_map(cases)}' for g, cases in gmap.items()
    )
    return f"mapOf({entries})"


def emit_plural_map(pmap: dict) -> str:
    """Plural: keys are gender IDs or 'other'/'m-personal'."""
    entries = ", ".join(f'"{ks(k)}" to {emit_case_map(cases)}' for k, cases in pmap.items())
    return f"mapOf({entries})"


def main() -> None:
    # personalPronouns: Map<String, Map<GramCase, String>>
    pron_raw = read("personalPronouns.core-data", "PRONOUNS")[0]
    pron_lines = []
    for pronoun_id, cases in pron_raw.items():
        case_entries = ", ".join(
            f'GramCase.fromId("{ks(c)}") to "{ks(v)}"' for c, v in cases.items()
        )
        pron_lines.append(f'    "{ks(pronoun_id)}" to mapOf({case_entries}),')

    # possessives: List<Possessive>
    poss_raw = read("possessives.core-data", "POSSESSIVE")
    poss_list_lines = [f'    Possessive(PossessiveId.fromId("{ks(p["id"])}"), "{ks(p["label"])}"),\n'
                       for p in poss_raw]

    # possessiveForms: Map<PossessiveId, CoursePossessiveForms>
    poss_forms_lines = []
    for p in poss_raw:
        pid = f'PossessiveId.fromId("{ks(p["id"])}")'
        forms = p["forms"]
        kind = forms.get("kind", "invariant")
        if kind == "invariant":
            val = f'CoursePossessiveForms.Invariant("{ks(forms["value"])}")'
        else:
            sg = emit_gender_map(forms["sg"])
            pl = emit_plural_map(forms["pl"])
            val = f'CoursePossessiveForms.Declined({sg}, {pl})'
        poss_forms_lines.append(f'    {pid} to {val},')

    sha = hashlib.sha256((BIOME / "personalPronouns.core-data").read_bytes()).hexdigest()

    content = f"""\
// Generated from courses-biome/pl-ru/; do not edit.
// personalPronouns SHA-256: {sha}
package polski.data

import polski.model.GramCase
import polski.model.Gender
import polski.model.PossessiveId

/** Typed pl-ru personalPronouns from Biome literal source. */
internal val generatedPlRuPersonalPronouns: Map<String, Map<GramCase, String>> = mapOf(
{"".join(pron_lines)}
)

/** Typed pl-ru personalPronounRawForms (all case-id strings, no GramCase filtering). */
internal val generatedPlRuPersonalPronounRawForms: Map<String, Map<String, String>> = mapOf(
{"".join(f'    "{ks(pid)}" to mapOf({", ".join(f"{chr(34)}{ks(c)}{chr(34)} to {chr(34)}{ks(v)}{chr(34)}" for c, v in cases.items())}),\n' for pid, cases in pron_raw.items())}
)

/** Typed pl-ru possessives from Biome literal source. */
internal val generatedPlRuPossessives: List<Possessive> = listOf(
{"".join(poss_list_lines)}
)

/** Typed pl-ru possessiveForms from Biome literal source. */
internal val generatedPlRuPossessiveForms: Map<PossessiveId, CoursePossessiveForms> = mapOf(
{"".join(l + chr(10) for l in poss_forms_lines)}
)
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")
    print(f"  pronouns={len(pron_raw)}, possessives={len(poss_raw)}")


if __name__ == "__main__":
    main()
