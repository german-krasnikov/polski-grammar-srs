#!/usr/bin/env python3
"""Generate allExerciseCases() body from the independent exercise oracle.

Reads requirements.core-data → prints Kotlin assertion blocks to stdout.
The surrounding test class, evaluation/queue/ports tests stay hand-authored.
"""
from __future__ import annotations
import hashlib, sys
from pathlib import Path
import sys as _sys
_sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
ORACLE = ROOT / "tests/fixtures/nj03-exercise-oracle/requirements.core-data"

# --- Kotlin printer ---

def ks(s): return s.replace('\\','\\\\').replace('"','\\"').replace('$','\\$')

def kt_exercise(ex):
    acc = ex.get('accepted') or []
    acc_s = 'listOf()' if not acc else f'listOf({", ".join(f""""{ks(a)}\"""" for a in acc)})'
    ch = [f'FormChange("{ks(c["from"])}", "{ks(c["to"])}", "{ks(c["reason"])}")' for c in ex['changes']]
    ch_s = 'listOf()' if not ch else f'listOf({", ".join(ch)})'
    return (f'Exercise("{ks(ex["id"])}", "{ks(ex["primarySkill"])}", '
            f'"{ks(ex["source"])}", "{ks(ex["prompt"])}", "{ks(ex["expected"])}", '
            f'{acc_s}, "{ks(ex["explanation"])}", '
            f'listOf({", ".join(f""""{ks(t)}\"""" for t in ex["tags"])}), '
            f'"{ks(ex["nounId"])}", "{ks(ex["adjectiveId"])}", '
            f'PossessiveId.fromId("{ks(ex["possessive"])}"), '
            f'NumberGram.fromId("{ks(ex["number"])}"), '
            f'{ch_s})')

def emit(case):
    cid = case['id']; inp = case['input']; ctx = case['context']
    draws = ', '.join(str(d) for d in ctx['randomDraws'])
    ex = case['exercise']
    lines = [f'        run {{ // {cid}',
             f'            val factory = factory(listOf({draws}))']
    if case['kind'] == 'chain':
        s = inp['seed']
        lines.append(f'            val actual = factory.generateChain(SentenceSeed("{s["nounId"]}", "{s["adjectiveId"]}"))[{inp["step"]-1}]')
    else:
        p = inp.get('preferred')
        arg = 'null' if p is None else f'SentenceSeed("{p["nounId"]}", "{p["adjectiveId"]}")'
        lines.append(f'            val actual = factory.generateForSkill("{inp["skillId"]}", {arg})')
    lines += [f'            assertEquals({kt_exercise(ex)}, actual, "{cid}")', '        }']
    return '\n'.join(lines)

START = '        // --- GENERATED START: do not edit, run scripts/nj03/generate-exercise-oracle-test.py ---'
END   = '        // --- GENERATED END ---'
TARGET = ROOT / 'kotlin/shared/src/commonTest/kotlin/polski/training/TrainingParityTest.kt'

# --- main ---

cases = []
for line in ORACLE.read_text('utf-8').splitlines():
    if line.startswith('CASE '): cases.append(parse(line[5:]))

assert len(cases) == 112, f"expected 112, got {len(cases)}"

body = '\n'.join(emit(c) for c in cases)

src = TARGET.read_text('utf-8')
si = src.index(START) + len(START) + 1   # after newline
ei = src.index(END)
assert si <= ei, 'markers out of order'

# ei points at GENERATED END; closing } of allExerciseCases lives outside markers
new_src = src[:si] + body + '\n' + src[ei:]
TARGET.write_text(new_src, encoding='utf-8')

lines_added = body.count('\n') + 1
print(f'OK: {len(cases)} cases → {lines_added} lines in {TARGET.relative_to(ROOT)}')
print(f'Oracle SHA-256: {hashlib.sha256(ORACLE.read_bytes()).hexdigest()[:16]}...')
