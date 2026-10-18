#!/usr/bin/env python3
"""One-time converter: course JSON → Biome value syntax (.core-data files).

Read-only historical input. Never writes JSON. Produces Biome record/list
literals under courses-biome/. Run once, then delete this script from
the recurring build.

Usage: python3 convert-course-json.py [--all | --skills-only]
"""
from __future__ import annotations
import hashlib, json, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse, emit as bv  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]

SOURCES = {
    "pl-course":    ROOT / "courses/pl-ru/course.json",
    "pl-pair":      ROOT / "courses/pairs/pl-ru/pair.json",
    "en-pair":      ROOT / "courses/pairs/en-ru/pair.json",
    "pl-lexicon":   ROOT / "courses/lang/pl/lexicon.json",
    "en-lexicon":   ROOT / "courses/lang/en/lexicon.json",
    "pl-curriculum":ROOT / "courses/lang/pl/curriculum.json",
    "en-curriculum":ROOT / "courses/lang/en/curriculum.json",
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

# ---------------------------------------------------------------------------
# Biome value emitter
# ---------------------------------------------------------------------------

# ---------------------------------------------------------------------------
# Verify source hashes before reading
# ---------------------------------------------------------------------------

def load_verified(key: str) -> dict | list:
    path = SOURCES[key]
    raw = path.read_bytes()
    actual = hashlib.sha256(raw).hexdigest()
    expected = FROZEN_SHA[key]
    if actual != expected:
        print(f"STOP: {key} SHA changed: {actual[:16]}... != {expected[:16]}...", file=sys.stderr)
        sys.exit(1)
    return json.loads(raw.decode("utf-8"))

# ---------------------------------------------------------------------------
# Skills pilot (B2 first slice)
# ---------------------------------------------------------------------------

def convert_skills(pack_key: str, data: dict, out_dir: Path) -> Path:
    skills = data["skills"]
    out_dir.mkdir(parents=True, exist_ok=True)
    lines = [
        f"# {pack_key} skills — converted from {SOURCES[pack_key].name}",
        f"# Source SHA-256: {FROZEN_SHA[pack_key]}",
        f"# {len(skills)} skills. Read-only historical input; this file is the new authority.",
        "SURFACE courseSkills",
    ]
    for skill in skills:
        # One SKILL per line (parser is line-oriented)
        lines.append(f"SKILL {bv(skill, inline=True)}")
    content = "\n".join(lines) + "\n"
    out_path = out_dir / "skills.core-data"
    out_path.write_text(content, encoding="utf-8")
    return out_path

# ---------------------------------------------------------------------------
# Round-trip verification: parse emitted Biome and compare to original JSON
# ---------------------------------------------------------------------------

def verify_roundtrip(original: list, core_data_path: Path) -> bool:
    """Parse SKILL lines back and compare parsed values to original."""
    # Use existing oracle parser from generate-exercise-oracle-test.py logic
    sys.path.insert(0, str(ROOT / "scripts/nj03"))
    from generate_exercise_oracle_test import parse  # noqa: PLC0415

    lines = core_data_path.read_text("utf-8").splitlines()
    parsed = []
    for line in lines:
        if line.startswith("SKILL "):
            parsed.append(parse(line[6:]))

    if len(parsed) != len(original):
        print(f"FAIL: count mismatch {len(parsed)} != {len(original)}")
        return False

    mismatches = 0
    for i, (p, o) in enumerate(zip(parsed, original)):
        if p != o:
            print(f"FAIL: skill[{i}] mismatch")
            print(f"  original:  {json.dumps(o, ensure_ascii=False)[:120]}")
            print(f"  roundtrip: {json.dumps(p, ensure_ascii=False)[:120]}")
            mismatches += 1
    if mismatches:
        return False
    return True

# ---------------------------------------------------------------------------
# main
# ---------------------------------------------------------------------------

def main() -> None:
    skills_only = "--all" not in sys.argv

    print("=== B1: verifying frozen source hashes ===")
    for key in SOURCES:
        data = load_verified(key)
        print(f"  OK {key}: SHA {FROZEN_SHA[key][:16]}...")

    print("\n=== B2 pilot: converting skills ===")
    pl_data = load_verified("pl-course")
    out_path = convert_skills("pl-course", pl_data, OUT / "pl-ru")
    print(f"  Written: {out_path.relative_to(ROOT)} ({out_path.stat().st_size} bytes)")

    print("\n=== Round-trip verification ===")
    lines = out_path.read_text("utf-8").splitlines()
    parsed = [parse(line[6:]) for line in lines if line.startswith("SKILL ")]
    original = pl_data["skills"]

    ok = len(parsed) == len(original) and all(p == o for p, o in zip(parsed, original))
    if ok:
        print(f"  PASS: {len(parsed)} skills round-trip identical")
    else:
        for i, (p, o) in enumerate(zip(parsed, original)):
            if p != o:
                print(f"  FAIL skill[{i}] {o['id']}")
                print(f"    original:  {json.dumps(o, ensure_ascii=False)[:200]}")
                print(f"    roundtrip: {json.dumps(p, ensure_ascii=False)[:200]}")
        sys.exit(1)

    print("\nB2 pilot complete. NOT RUN: full catalog, forms, recipes, en skills.")


if __name__ == "__main__":
    main()
