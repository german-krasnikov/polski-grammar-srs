"""Minimal Biome value syntax parser and emitter. Shared by nj03 scripts."""
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
            m = re.match(r'[a-zA-Z_0-9][\w.-]*', s[i:])
            k = m.group()
            i = _ws(s, i + len(k))
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


# --- emitter ---

def emit(v, indent: int = 0, inline: bool = False) -> str:
    """Emit a Python value as Biome value syntax.
    inline=True forces single-line output (for line-oriented .core-data files).
    """
    pad = "  " * indent
    if v is None:
        return "null"
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, int):
        return str(v)
    if isinstance(v, float):
        return str(int(v)) if v == int(v) else str(v)
    if isinstance(v, str):
        esc = (v.replace('\\', '\\\\').replace('"', '\\"')
                .replace('\n', '\\n').replace('\r', '\\r').replace('\t', '\\t'))
        return f'"{esc}"'
    if isinstance(v, list):
        if not v:
            return "[]"
        items = [emit(x, indent + 1, inline=True) for x in v]
        return f'[{" ".join(items)}]'
    if isinstance(v, dict):
        if not v:
            return "{}"
        pairs = [f'{k}: {emit(val, indent + 1, inline=True)}' for k, val in v.items()]
        if inline or len(pairs) <= 3 and all(not isinstance(val, (dict, list)) for val in v.values()):
            return "{" + " ".join(pairs) + "}"
        inner = ("\n" + pad + "  ").join(pairs)
        return f'{{\n{pad}  {inner}\n{pad}}}'
    raise ValueError(f"unsupported type: {type(v)}")
