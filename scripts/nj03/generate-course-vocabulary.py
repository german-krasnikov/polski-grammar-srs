#!/usr/bin/env python3
"""Generate GeneratedCourseVocabulary.kt: vocabularyItems."""
from __future__ import annotations
import hashlib, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse

ROOT = Path(__file__).resolve().parents[2]
BIOME = ROOT / "courses-biome/pl-ru"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseVocabulary.kt"


def ks(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


def main() -> None:
    lines = (BIOME / "vocabulary.core-data").read_text("utf-8").splitlines()
    vocab = [parse(l[11:]) for l in lines if l.startswith("VOCABULARY ")][0]
    items = vocab["items"]

    item_lines = []
    for item in items:
        rank = str(item["frequencyRank"]) if item.get("frequencyRank") is not None else "null"
        item_lines.append(
            f'    VocabularyItem("{ks(item["id"])}", "{ks(item["lemma"])}", '
            f'"{ks(item["translation"])}", "{ks(item["form"])}", '
            f'"{ks(item["example"])}", "{ks(item["level"])}", {rank}),'
        )

    sha = hashlib.sha256((BIOME / "vocabulary.core-data").read_bytes()).hexdigest()
    content = f"""\
// Generated from courses-biome/pl-ru/vocabulary.core-data; do not edit.
// SHA-256: {sha}
package polski.data

/** Typed pl-ru vocabulary items from Biome literal source. */
internal val generatedPlRuVocabularyItems: List<VocabularyItem> = listOf(
{"".join(l + chr(10) for l in item_lines)}
)
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)} ({len(items)} items)")


if __name__ == "__main__":
    main()
