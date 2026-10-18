#!/usr/bin/env python3
"""Generate allExerciseCases() body from the independent exercise oracle.

Reads requirements.core-data → prints Kotlin assertion blocks to stdout.
The surrounding test class, evaluation/queue/ports tests stay hand-authored.
"""
from __future__ import annotations
import hashlib, re, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ORACLE = ROOT / "tests/fixtures/nj03-exercise-oracle/requirements.core-data"

# --- minimal Biome value parser ---

def _ws(s, i):
    while i < len(s) and s[i] in ' \t\r\n': i += 1
    return i

def _str(s, i):
    i += 1; parts = []
    while s[i] != '"':
        if s[i] == '\\':
            i += 1; c = s[i]
            parts.append({'n':'\n','t':'\t','r':'\r'}.get(c, c))
        else:
            parts.append(s[i])
        i += 1
    return ''.join(parts), i+1

def _val(s, i):
    i = _ws(s, i)
    if s[i] == '"': return _str(s, i)
    if s[i] == '{':
        i = _ws(s, i+1); rec = {}
        while s[i] != '}':
            m = re.match(r'[a-zA-Z_]\w*', s[i:]); k = m.group(); i = _ws(s, i+len(k))
            assert s[i] == ':'; i = _ws(s, i+1)
            v, i = _val(s, i); rec[k] = v; i = _ws(s, i)
        return rec, i+1
    if s[i] == '[':
        i = _ws(s, i+1); lst = []
        while s[i] != ']': v, i = _val(s, i); lst.append(v); i = _ws(s, i)
        return lst, i+1
    if s[i:i+4] == 'null': return None, i+4
    m = re.match(r'-?\d+(?:\.\d+)?', s[i:])
    if m: t = m.group(); return (float(t) if '.' in t else int(t)), i+len(t)
    m = re.match(r'[a-zA-Z_][\w.:-]*', s[i:])
    if m: return m.group(), i+len(m.group())
    raise ValueError(f"at {i}: {s[i:i+20]}")

def parse(text): return _val(text, 0)[0]

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
