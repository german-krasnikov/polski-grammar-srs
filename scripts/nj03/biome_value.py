"""Minimal Biome value syntax parser. Read by generate-exercise-oracle-test.py for the sealed requirements.core-data."""
from __future__ import annotations
import re


# --- parser ---

def _ws(s, i):
    while i < len(s) and s[i] in ' \t\r\n':
        i += 1
    return i


def _str(s, i):
    i += 1
    parts = []
    while s[i] != '"':
        if s[i] == '\\':
            i += 1
            c = s[i]
            parts.append({'n': '\n', 't': '\t', 'r': '\r'}.get(c, c))
        else:
            parts.append(s[i])
        i += 1
    return ''.join(parts), i + 1


def _val(s, i):
    i = _ws(s, i)
    if s[i] == '"':
        return _str(s, i)
    if s[i] == '{':
        i = _ws(s, i + 1)
        rec = {}
        while s[i] != '}':
            if s[i] == '"':  # a quoted key: how Core data writes a key that is no word ("1")
                k, i = _str(s, i)
            else:
                k = re.match(r'[a-zA-Z_0-9][\w.-]*', s[i:]).group()
                i += len(k)
            i = _ws(s, i)
            assert s[i] == ':'
            i = _ws(s, i + 1)
            v, i = _val(s, i)
            rec[k] = v
            i = _ws(s, i)
        return rec, i + 1
    if s[i] == '[':
        i = _ws(s, i + 1)
        lst = []
        while s[i] != ']':
            v, i = _val(s, i)
            lst.append(v)
            i = _ws(s, i)
        return lst, i + 1
    if s[i:i + 4] == 'null':
        return None, i + 4
    if s[i:i + 4] == 'true':
        return True, i + 4
    if s[i:i + 5] == 'false':
        return False, i + 5
    m = re.match(r'-?\d+(?:\.\d+)?', s[i:])
    if m:
        t = m.group()
        return (float(t) if '.' in t else int(t)), i + len(t)
    m = re.match(r'[a-zA-Z_][\w.:-]*', s[i:])
    if m:
        return m.group(), i + len(m.group())
    raise ValueError(f"at {i}: {s[i:i+20]}")


def parse(text: str) -> object:
    return _val(text, 0)[0]
