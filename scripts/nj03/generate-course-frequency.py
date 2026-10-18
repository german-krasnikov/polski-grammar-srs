#!/usr/bin/env python3
"""Generate GeneratedCourseFrequency.kt from frequency-top1000.json."""
import json, hashlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "courses/pl-ru/frequency-top1000.json"
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedCourseFrequency.kt"

data = json.load(open(SRC))
items = data["items"]
sha = hashlib.sha256(SRC.read_bytes()).hexdigest()

lines = [f'    FrequencyItem({i["rank"]}, "{i["lemma"]}", {i["count"]}),' for i in items]
content = f"""// Generated from courses/pl-ru/frequency-top1000.json; do not edit.
// SHA-256: {sha}
package polski.data

/** Typed pl-ru frequency items from source. */
internal val generatedPlRuFrequency: List<FrequencyItem> = listOf(
{chr(10).join(lines)}
)
"""
OUT.write_text(content, encoding="utf-8")
print(f"Generated {OUT.relative_to(ROOT)} ({len(items)} items)")
