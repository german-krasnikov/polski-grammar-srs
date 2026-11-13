#!/usr/bin/env python3
"""Generate allExerciseCases() body from the independent exercise oracle.

Reads an oracle → rewrites the GENERATED region of the target test. The surrounding test class stays hand-authored.
Usage: generate-exercise-oracle-test.py ORACLE TARGET EXPECTED_CASES (paths from the repo root; the derive wrappers pass them).
"""
from __future__ import annotations
import hashlib, sys
from pathlib import Path
import sys as _sys
_sys.path.insert(0, str(Path(__file__).parent))
from biome_value import parse  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
if len(sys.argv) != 4:
    sys.exit(__doc__)
ORACLE, TARGET, EXPECTED = ROOT / sys.argv[1], ROOT / sys.argv[2], int(sys.argv[3])

# --- Kotlin printer ---

def ks(s): return s.replace('\\','\\\\').replace('"','\\"').replace('$','\\$')

def kt_str_list(items):
    return 'listOf()' if not items else f'listOf({", ".join(f""""{ks(t)}\"""" for t in items)})'

def kt_str_map(d):
    return 'mapOf()' if not d else f'mapOf({", ".join(f""""{ks(k)}" to "{ks(v)}\"""" for k, v in d.items())})'

def kt_span_map(d):
    return 'mapOf()' if not d else f'mapOf({", ".join(f""""{ks(k)}" to ({v[0]} until {v[1]})""" for k, v in d.items())})'

def kt_tile_phrase(tp):
    if tp is None:
        return 'null'
    return (f'TilePhrase("{ks(tp["construction"])}", {kt_str_map(tp["bundle"])}, '
            f'"{ks(tp["text"])}", {kt_span_map(tp["slotSpans"])})')

def kt_exercise(ex):
    acc = ex.get('accepted') or []
    ch = [f'FormChange("{ks(c["from"])}", "{ks(c["to"])}", "{ks(c["reason"])}")' for c in ex['changes']]
    ch_s = 'listOf()' if not ch else f'listOf({", ".join(ch)})'
    base = (f'Exercise("{ks(ex["id"])}", "{ks(ex["primarySkill"])}", '
            f'"{ks(ex["source"])}", "{ks(ex["prompt"])}", "{ks(ex["expected"])}", '
            f'{kt_str_list(acc)}, "{ks(ex["explanation"])}", '
            f'{kt_str_list(ex["tags"])}, '
            f'"{ks(ex["nounId"])}", "{ks(ex["adjectiveId"])}", '
            f'PossessiveId.fromId("{ks(ex["possessive"])}"), '
            f'NumberGram.fromId("{ks(ex["number"])}"), '
            f'{ch_s}')
    # SB-5 pilot opt-in (case.acc.f/m, case.gen.neg): oracle entries may carry tileTargetSlots/
    # tilePhrase, a Kotlin-only addition with no React equivalent — append only when present so
    # every untouched case keeps relying on Exercise's trailing defaults (emptyList()/null).
    tile_targets = ex.get('tileTargetSlots')
    tile_phrase = ex.get('tilePhrase')
    if tile_targets or tile_phrase:
        base += f', {kt_str_list(tile_targets or [])}, {kt_tile_phrase(tile_phrase)}'
    return base + ')'

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

# --- main ---

cases = []
for line in ORACLE.read_text('utf-8').splitlines():
    if line.startswith('CASE '): cases.append(parse(line[5:]))

assert len(cases) == EXPECTED, f"expected {EXPECTED}, got {len(cases)}"

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
