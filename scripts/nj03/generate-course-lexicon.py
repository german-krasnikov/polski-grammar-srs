#!/usr/bin/env python3
"""B7 prep: Generate GeneratedCourseLexicon.kt from courses-biome/pl-ru/*.core-data.

Generates typed Kotlin for Noun, Adjective, Verb, possessives, personalPronouns.
These are the remaining sections needed to eliminate JSON from the production path.
"""
from __future__ import annotations
import hashlib, sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseLexicon.kt"

FROZEN = {
    "pl-course": "7255e8c686ceee17a72d280e5876b0cf6776dd9a1743884cd75c724fb8180dcc",
}


def ks(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def read(fname: str, tag: str) -> list:
    return [parse(l[len(tag)+1:]) for l in (BIOME / fname).read_text("utf-8").splitlines()
            if l.startswith(tag + " ")]


# --- Noun ---
def emit_case_map(forms: dict) -> str:
    entries = ", ".join(
        f'GramCase.fromId("{ks(k)}") to "{ks(v)}"'
        for k, v in forms.items()
    )
    return f"mapOf({entries})"


def emit_noun(n: dict) -> str:
    sg = emit_case_map(n["forms"]["sg"])
    pl = emit_case_map(n["forms"]["pl"])
    return (
        f'    Noun("{ks(n["id"])}", "{ks(n["lemma"])}", "{ks(n["meaning"])}",\n'
        f'        Gender.fromId("{ks(n["gender"])}"),\n'
        f'        mapOf(NumberGram.SG to {sg},\n'
        f'              NumberGram.PL to {pl})),'
    )


# --- Adjective ---
def emit_adj(a: dict) -> str:
    sg = a["forms"]["sg"]
    pl = a["forms"]["pl"]

    def gender_map(gmap: dict) -> str:
        entries = ", ".join(
            f'Gender.fromId("{ks(g)}") to {emit_case_map(cases)}'
            for g, cases in gmap.items()
        )
        return f"mapOf({entries})"

    sg_str = gender_map(sg)
    pl_str = gender_map(pl)
    return (
        f'    Adjective("{ks(a["id"])}", "{ks(a["lemma"])}", "{ks(a["meaning"])}",\n'
        f'        mapOf(NumberGram.SG to {sg_str},\n'
        f'              NumberGram.PL to {pl_str})),'
    )


# --- Verb ---
def emit_verb(v: dict) -> str:
    past = v["pastStem"]
    past_n = f'"{ks(past["n"])}"' if past.get("n") else "null"
    past_str = (
        f'PastStem("{ks(past["m"])}", "{ks(past["f"])}", {past_n},'
        f' "{ks(past["mp"])}", "{ks(past["np"])}")'
    )

    present = v.get("present")
    if present:
        num_entries = []
        for num, persons in present.items():
            p_entries = ", ".join(
                f'Person.fromId({int(p)}) to "{ks(form)}"'
                for p, form in persons.items()
            )
            num_entries.append(f'NumberGram.fromId("{ks(num)}") to mapOf({p_entries})')
        present_str = f"mapOf({', '.join(num_entries)})"
    else:
        present_str = "null"

    pair = v.get("perfectivePair")
    pair_str = f'"{ks(pair)}"' if pair else "null"

    return (
        f'    Verb("{ks(v["id"])}", "{ks(v["lemma"])}", "{ks(v["meaning"])}",\n'
        f'        Aspect.valueOf("{v["aspect"].upper()}"),\n'
        f'        {present_str}, {past_str},\n'
        f'        FutureType.valueOf("{v["futureType"].upper()}"), {pair_str}),'
    )


def main() -> None:
    nouns = read("nouns.core-data", "NOUN")
    adjs = read("adjectives.core-data", "ADJ")
    verbs = read("verbs.core-data", "VERB")

    noun_lines = "\n".join(emit_noun(n) for n in nouns)
    adj_lines = "\n".join(emit_adj(a) for a in adjs)
    verb_lines = "\n".join(emit_verb(v) for v in verbs)

    sha = hashlib.sha256((BIOME / "nouns.core-data").read_bytes()).hexdigest()

    content = f"""\
// Generated from courses-biome/pl-ru/; do not edit.
// nouns SHA-256: {sha}
package polski.data

import polski.model.*

/** Typed pl-ru nouns from Biome literal source. */
internal val generatedPlRuNouns: List<Noun> = listOf(
{noun_lines}
)

/** Typed pl-ru adjectives from Biome literal source. */
internal val generatedPlRuAdjectives: List<Adjective> = listOf(
{adj_lines}
)

/** Typed pl-ru verbs from Biome literal source. */
internal val generatedPlRuVerbs: List<Verb> = listOf(
{verb_lines}
)
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")
    print(f"  nouns={len(nouns)}, adjectives={len(adjs)}, verbs={len(verbs)}")


if __name__ == "__main__":
    main()
