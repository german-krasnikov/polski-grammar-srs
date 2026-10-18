#!/usr/bin/env python3
"""B4: Generate GeneratedCourseValueSource.kt from courses-biome/pl-ru/skills.core-data.

Reads Biome value literals → emits typed Kotlin Skill constructors.
Registered in biome.project and in build.gradle.kts as a source directory.
"""
from __future__ import annotations
import hashlib, sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
ORACLE = ROOT / "courses-biome/pl-ru/skills.core-data"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseValueSource.kt"

FROZEN_SHA = "7255e8c686ceee17a72d280e5876b0cf6776dd9a1743884cd75c724fb8180dcc"


def ks(s: str) -> str:
    return (s.replace("\\", "\\\\")
             .replace('"', '\\"')
             .replace("\n", "\\n")
             .replace("\r", "\\r")
             .replace("\t", "\\t")
             .replace("$", "\\$"))


def emit_skill(s: dict) -> str:
    prereqs = ", ".join(f'"{ks(p)}"' for p in s["prerequisites"])
    prereqs_str = f"listOf({prereqs})" if prereqs else "emptyList()"
    return (
        f'    Skill("{ks(s["id"])}", "{ks(s["title"])}", "{ks(s["group"])}", "{ks(s["level"])}",\n'
        f'        "{ks(s["formula"])}", "{ks(s["theory"])}", "{ks(s["hint"])}",\n'
        f'        {prereqs_str}),'
    )


def main() -> None:
    oracle_sha = hashlib.sha256(ORACLE.read_bytes()).hexdigest()
    # Source JSON sha is embedded for freshness cross-check
    source_sha = FROZEN_SHA

    lines = ORACLE.read_text("utf-8").splitlines()
    skills = [parse(l[6:]) for l in lines if l.startswith("SKILL ")]

    if len(skills) != 16:
        print(f"FAIL: expected 16 skills, got {len(skills)}", file=sys.stderr)
        sys.exit(1)

    skill_lines = "\n".join(emit_skill(s) for s in skills)

    content = f"""\
// Generated from courses-biome/pl-ru/skills.core-data; do not edit.
// Oracle SHA-256: {oracle_sha}
// Source JSON SHA-256: {source_sha}
package polski.data

import polski.model.Skill

/** Typed pl-ru skills from Biome literal source. Replaces JSON-based CoursePack.skills read. */
internal val generatedPlRuSkills: List<Skill> = listOf(
{skill_lines}
)
"""
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(content, encoding="utf-8")
    print(f"Generated {OUT.relative_to(ROOT)} ({len(skills)} skills, {OUT.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
