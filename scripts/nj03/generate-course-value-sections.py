#!/usr/bin/env python3
"""B9: Generate GeneratedCourseSections.kt from courses-biome/pl-ru/*.core-data.

Generates typed Kotlin for sentenceSeeds, exerciseCopy, exercisePatterns,
caseSentencePrefixes — simple sections that replace JSON-based reads in CoursePack.
"""
from __future__ import annotations
import hashlib, sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseSections.kt"


def ks(s: str) -> str:
    return (s.replace("\\", "\\\\").replace('"', '\\"')
             .replace("\n", "\\n").replace("$", "\\$"))


def read_section(fname: str, tag: str) -> list:
    path = BIOME / fname
    lines = path.read_text("utf-8").splitlines()
    return [parse(l[len(tag)+1:]) for l in lines if l.startswith(tag + " ")]


def emit_string_map(data: dict) -> str:
    entries = ", ".join(f'"{ks(k)}" to "{ks(v)}"' for k, v in data.items())
    return f"mapOf({entries})"


def main() -> None:
    # sentenceSeeds: list of {nounId, adjectiveId}
    seeds_raw = read_section("sentenceSeeds.core-data", "SEED")
    seeds_kt = "\n".join(
        f'    SentenceSeed("{ks(s["nounId"])}", "{ks(s["adjectiveId"])}"),'
        for s in seeds_raw
    )

    # exerciseCopy: dict of string→string
    copy_raw = read_section("exerciseCopy.core-data", "COPY")[0]
    copy_kt = emit_string_map(copy_raw)

    # exercisePatterns: dict of string→string
    patterns_raw = read_section("exercisePatterns.core-data", "PATTERN")[0]
    patterns_kt = emit_string_map(patterns_raw)

    # caseSentencePrefixes: dict of string→string
    prefixes_raw = read_section("caseSentencePrefixes.core-data", "PREFIX")[0]
    prefixes_kt = emit_string_map(prefixes_raw)

    oracle_sha = hashlib.sha256((BIOME / "sentenceSeeds.core-data").read_bytes()).hexdigest()

    content = f"""\
// Generated from courses-biome/pl-ru/; do not edit.
// sentenceSeeds SHA-256: {oracle_sha}
package polski.data

import polski.model.SentenceSeed

/** Typed pl-ru sentenceSeeds from Biome literal source. */
internal val generatedPlRuSentenceSeeds: List<SentenceSeed> = listOf(
{seeds_kt}
)

/** Typed pl-ru exerciseCopy from Biome literal source. */
internal val generatedPlRuExerciseCopy: Map<String, String> = {copy_kt}

/** Typed pl-ru exercisePatterns from Biome literal source. */
internal val generatedPlRuExercisePatterns: Map<String, String> = {patterns_kt}

/** Typed pl-ru caseSentencePrefixes from Biome literal source. */
internal val generatedPlRuCaseSentencePrefixes: Map<String, String> = {prefixes_kt}
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)}")
    print(f"  seeds={len(seeds_raw)}, copy={len(copy_raw)}, patterns={len(patterns_raw)}, prefixes={len(prefixes_raw)}")


if __name__ == "__main__":
    main()
