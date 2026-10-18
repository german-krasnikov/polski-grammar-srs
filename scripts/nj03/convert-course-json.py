#!/usr/bin/env python3
"""One-time converter: course JSON → Biome value syntax (.core-data files).

Read-only historical input. Never writes JSON. Produces Biome record/list
literals under courses-biome/. Run once; result is the new authority.

Usage: python3 convert-course-json.py
"""
from __future__ import annotations
import hashlib, json, sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse, emit as bv  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]

SOURCES = {
    "pl-course":     ROOT / "courses/pl-ru/course.json",
    "pl-pair":       ROOT / "courses/pairs/pl-ru/pair.json",
    "en-pair":       ROOT / "courses/pairs/en-ru/pair.json",
    "pl-lexicon":    ROOT / "courses/lang/pl/lexicon.json",
    "en-lexicon":    ROOT / "courses/lang/en/lexicon.json",
    "pl-curriculum": ROOT / "courses/lang/pl/curriculum.json",
    "en-curriculum": ROOT / "courses/lang/en/curriculum.json",
}

FROZEN_SHA = {
    "pl-course":     "7255e8c686ceee17a72d280e5876b0cf6776dd9a1743884cd75c724fb8180dcc",
    "pl-pair":       "dadb5eabaa02fa7dcf300f01bc3aa079cec43567247c249b3476d5c8a32215b7",
    "en-pair":       "55f79438cf1511b1061d389f4d74117112c910092e7515c73c57aa0d3af61862",
    "pl-lexicon":    "f7e7e83a4c95f7113b8d495451b4c6ad2809d70afa73bbebba745a3b441f5838",
    "en-lexicon":    "f96da7fc14e569d2e061a9f0b39a6d97ead26eb7f21d6521d9ed457a8d0abd80",
    "pl-curriculum": "4f98cb4e0a5bc5b5f21890120fe25e077cedc2ba5e65852bff4b1e252b4234b7",
    "en-curriculum": "4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6",
}

OUT = ROOT / "courses-biome"


def load_verified(key: str) -> dict | list:
    path = SOURCES[key]
    raw = path.read_bytes()
    actual = hashlib.sha256(raw).hexdigest()
    if actual != FROZEN_SHA[key]:
        print(f"STOP: {key} SHA changed: {actual[:16]}...", file=sys.stderr)
        sys.exit(1)
    return json.loads(raw.decode("utf-8"))


def write_section(pack_key: str, section: str, value: object, tag: str, out_dir: Path) -> Path:
    """Write one section as one TAG per item (line-oriented for the parser)."""
    out_dir.mkdir(parents=True, exist_ok=True)
    items = value if isinstance(value, list) else [value]
    lines = [
        f"# {pack_key}/{section} — source SHA-256: {FROZEN_SHA[pack_key][:16]}...",
        f"SURFACE course_{section}",
        *[f"{tag} {bv(item, inline=True)}" for item in items],
    ]
    out_path = out_dir / f"{section}.core-data"
    out_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return out_path


def verify_roundtrip(original: list | dict, out_path: Path, tag: str) -> bool:
    lines = out_path.read_text("utf-8").splitlines()
    parsed = [parse(line[len(tag) + 1:]) for line in lines if line.startswith(tag + " ")]
    original_list = original if isinstance(original, list) else [original]
    if parsed != original_list:
        for i, (p, o) in enumerate(zip(parsed, original_list)):
            if p != o:
                print(f"  FAIL item[{i}]", file=sys.stderr)
        return False
    return True


def convert_all(data: dict, pack_key: str, out_dir: Path) -> None:
    # Sections: (json_key, tag_name)
    sections = [
        ("skills",               "SKILL"),
        ("nouns",                "NOUN"),
        ("adjectives",           "ADJ"),
        ("verbs",                "VERB"),
        ("possessives",          "POSSESSIVE"),
        ("sentenceSeeds",        "SEED"),
        ("exerciseCopy",         "COPY"),
        ("exercisePatterns",     "PATTERN"),
        ("caseSentencePrefixes", "PREFIX"),
        ("personalPronouns",     "PRONOUNS"),
        ("training",             "TRAINING"),
        ("reference",            "REFERENCE"),
        ("vocabulary",           "VOCABULARY"),
        ("morphology",           "MORPHOLOGY"),
        ("stemAlternations",     "STEM_ALT"),
    ]
    failures = 0
    for section, tag in sections:
        if section not in data:
            print(f"  SKIP {section} (not in {pack_key})")
            continue
        value = data[section]
        out_path = write_section(pack_key, section, value, tag, out_dir)
        if verify_roundtrip(value, out_path, tag):
            count = len(value) if isinstance(value, list) else 1
            print(f"  PASS {section}: {count} item(s), {out_path.stat().st_size} bytes")
        else:
            print(f"  FAIL {section}")
            failures += 1
    if failures:
        print(f"\nFAIL: {failures} sections failed round-trip", file=sys.stderr)
        sys.exit(1)


def main() -> None:
    print("=== B1: verifying source hashes ===")
    data = {}
    for key in SOURCES:
        data[key] = load_verified(key)
        print(f"  OK {key}: {FROZEN_SHA[key][:16]}...")

    print("\n=== B2 full: converting pl-ru/course.json ===")
    convert_all(data["pl-course"], "pl-course", OUT / "pl-ru")

    print("\nB2 full complete.")
    print("NOT RUN: pair, lexicon, curriculum, forms, en-pack, Kotlin consumption.")


if __name__ == "__main__":
    main()
