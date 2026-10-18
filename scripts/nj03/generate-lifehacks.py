#!/usr/bin/env python3
"""Generate typed lifehack data from lifehacks.json for both packs."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "kotlin/shared/src/commonMain/kotlin/polski/data/GeneratedLifehacks.kt"


def ks(s):
    if not isinstance(s, str): s = str(s)
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$")


KIND_MAP = {"research": "Research", "teaching-practice": "TeachingPractice", "project-authored": "ProjectAuthored"}
STATUS_MAP = {"approved": "Editorial", "editorial": "Editorial", "community": "Community"}

def source_kt(s):
    kind = KIND_MAP.get(s.get("kind", ""), "ProjectAuthored")
    citation = ks(s.get("citation", ""))
    url = s.get("url")
    url_kt = f'"{ks(url)}"' if url else "null"
    return f'LifehackSource(LifehackSourceKind.{kind}, "{citation}", {url_kt})'


def lifehack_kt(lh):
    src = source_kt(lh.get("source", {}))
    return (f'Lifehack("{ks(lh["id"])}", {f"{chr(34)}{ks(lh["skillId"])}{chr(34)}" if lh.get("skillId") else "null"}, '
            f'{f"{chr(34)}{ks(lh["topic"])}{chr(34)}" if lh.get("topic") else "null"}, '
            f'"{ks(lh["text"])}", {src}, '
            f'LifehackStatus.{STATUS_MAP.get(lh.get("status","approved"), "Editorial")})')


def main():
    packs = {}
    for pair_id in ["pl-ru", "en-ru"]:
        path = ROOT / f"courses/pairs/{pair_id}/lifehacks.json"
        if path.exists():
            data = json.load(open(path))
            packs[pair_id] = [lifehack_kt(lh) for lh in data["lifehacks"]]

    NL = chr(10)
    entries = []
    for pair_id, items in packs.items():
        body = ("," + NL + "        ").join(items)
        entries.append(f'    "{pair_id}" to listOf({NL}        {body}{NL}    )')

    content = f"""\
// Generated from courses/pairs/*/lifehacks.json; do not edit.
package polski.data

import polski.presentation.Lifehack
import polski.presentation.LifehackSource
import polski.presentation.LifehackSourceKind
import polski.presentation.LifehackStatus

internal val generatedLifehacksByPairId: Map<String, List<Lifehack>> = mapOf(
{("," + NL).join(entries)}
)
"""
    OUT.write_text(content, encoding="utf-8")
    total = sum(len(v) for v in packs.values())
    print(f"Generated {OUT.relative_to(ROOT)} ({total} lifehacks)")


if __name__ == "__main__":
    main()
