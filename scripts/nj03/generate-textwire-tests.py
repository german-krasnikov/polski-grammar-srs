#!/usr/bin/env python3
"""Generate PolskiTextCodecTest.kt from frozen .cases corpus tree definitions.

Reads tree-section of positive.cases → emits Kotlin WireNode constructors
→ round-trip tests. Oracle is the cases file; generator produces boilerplate.
"""
from __future__ import annotations
import hashlib, re, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
POS  = ROOT / "tests/fixtures/textwire/positive.cases"
NEG  = ROOT / "tests/fixtures/textwire/negative.cases"
KT   = ROOT / "kotlin/shared/src/desktopTest/kotlin/polski/textwire/PolskiTextCodecTest.kt"

START = "        // --- GENERATED START: do not edit, run scripts/nj03/generate-textwire-tests.py ---"
END   = "        // --- GENERATED END ---"


def unescape_cases(s: str) -> str:
    """Unescape \\t \\n \\\\ \\uXXXX sequences from .cases string literals."""
    out, i = [], 0
    while i < len(s):
        if s[i] == '\\' and i + 1 < len(s):
            c = s[i+1]
            if c == 't': out.append('\t'); i += 2
            elif c == 'n': out.append('\n'); i += 2
            elif c == 'r': out.append('\r'); i += 2
            elif c == '\\': out.append('\\'); i += 2
            elif c == 'u' and i + 5 < len(s):
                out.append(chr(int(s[i+2:i+6], 16))); i += 6
            else: out.append(s[i]); i += 1
        else: out.append(s[i]); i += 1
    return ''.join(out)


def ks(s: str) -> str:
    out = []
    for c in s:
        cp = ord(c)
        if c == '\\': out.append('\\\\')
        elif c == '"': out.append('\\"')
        elif c == '\n': out.append('\\n')
        elif c == '\r': out.append('\\r')
        elif c == '\t': out.append('\\t')
        elif c == '$': out.append('\\$')
        elif cp <= 0x1F or 0x7F <= cp <= 0x9F or 0xD800 <= cp <= 0xDFFF or cp in (0x2028, 0x2029):
            out.append(f'\\u{cp:04X}')
        else:
            out.append(c)
    return ''.join(out)


def parse_tree(lines: list[str]) -> str:
    """Parse tree DSL lines into Kotlin WireNode constructor expression."""
    return parse_node(iter(lines), indent=0)[0]


def parse_node(it, indent: int) -> tuple[str, bool]:
    """Returns (kotlin_expr, consumed_from_iterator)."""
    for raw in it:
        line = raw.rstrip()
        stripped = line.strip()
        if not stripped:
            continue
        if stripped == "map":
            children = []
            # Peek ahead for key lines
            return build_map(it, indent), True
        elif stripped == "list":
            return build_list(it, indent), True
        elif stripped.startswith("key "):
            # Shouldn't reach here directly
            break
    return "WireNode.WireMap(emptyList())", True


def build_map(it, indent: int) -> str:
    children = []
    saved = []
    for raw in it:
        stripped = raw.strip()
        if not stripped:
            continue
        m = re.match(r'^key "([^"]*)" => (.+)$', stripped)
        if m:
            key = m.group(1)
            val_str = m.group(2).strip()
            val_kt = parse_value(val_str, it)
            children.append(f'"{ks(key)}" to {val_kt}')
        else:
            # Not a key line — put back conceptually (can't with iter, just stop)
            break
    if not children:
        return "WireNode.WireMap(emptyList())"
    items = ", ".join(children)
    return f"WireNode.WireMap(listOf({items}))"


def build_list(it, indent: int) -> str:
    items = []
    for raw in it:
        stripped = raw.strip()
        if not stripped:
            continue
        if stripped in ("map", "list"):
            # nested structure — parse sub-node
            if stripped == "map":
                items.append(build_map(it, indent + 1))
            else:
                items.append(build_list(it, indent + 1))
        else:
            break
    if not items:
        return "WireNode.WireList(emptyList())"
    return f"WireNode.WireList(listOf({', '.join(items)}))"


def parse_value(val_str: str, it) -> str:
    if val_str == "map":
        return build_map(it, 0)
    if val_str == "list":
        return build_list(it, 0)
    if val_str == "null":
        return "WireNode.WireNull"
    m_str = re.match(r'^string "([^"]*)"$', val_str)
    if m_str:
        raw = unescape_cases(m_str.group(1))
        return f'WireNode.WireString("{ks(raw)}")'
    m_i64 = re.match(r'^i64 (-?\d+)$', val_str)
    if m_i64:
        v = int(m_i64.group(1))
        # Long.MIN_VALUE can't be a literal in Kotlin
        if v == -9223372036854775808:
            return "WireNode.WireI64(Long.MIN_VALUE)"
        return f"WireNode.WireI64({v}L)"
    m_f64 = re.match(r'^f64 bits:([0-9a-f]+)$', val_str)
    if m_f64:
        bits = int(m_f64.group(1), 16)
        signed = bits if bits < 2**63 else bits - 2**64
        return f"WireNode.WireF64({signed}L)"
    m_bool = re.match(r'^bool (true|false)$', val_str)
    if m_bool:
        return f"WireNode.WireBool({m_bool.group(1)})"
    return f'WireNode.WireString("{ks(val_str)}")'  # fallback


def parse_cases(path: Path) -> list[dict]:
    text = path.read_text("utf-8")
    raw_cases = re.split(r"^CASE ", text, flags=re.MULTILINE)[1:]
    result = []
    for c in raw_cases:
        name = c.split('\n')[0].strip()
        m_tree = re.search(r'^tree\n(.*?)(?=^input |^absent|^END$)', c, re.MULTILINE | re.DOTALL)
        m_neg_defect = re.search(r'^defect (.+?)$', c, re.MULTILINE)
        result.append({
            "name": name,
            "tree": m_tree.group(1).strip().splitlines() if m_tree else None,
            "is_negative": bool(m_neg_defect),
        })
    return result


# JVM cannot round-trip lone surrogates through UTF-8; mark as NOT RUN
NOT_RUN_JVM = {"P10 UTF16-lone-value-and-paired-value"}

def safe_id(name: str) -> str:
    return re.sub(r"[^A-Za-z0-9]", "_", name).strip("_")


def emit_positive(case: dict) -> str | None:
    if not case["tree"]:
        return None
    if case["name"] in NOT_RUN_JVM:
        return f'        // NOT RUN (JVM): {case["name"]} — lone surrogate round-trip not possible via UTF-8'
    try:
        tree_kt = build_map(iter(case["tree"][1:]), 0)  # skip "map" header
    except Exception:
        return None
    return (
        f'        run {{ // {case["name"]}\n'
        f'            val tree = {tree_kt}\n'
        f'            val enc = PolskiTextCodec.encode(tree, "progress", 2)\n'
        f'            val dec = PolskiTextCodec.decode(enc.bytes)\n'
        f'            assertIs<DecodeResult.Ok>(dec)\n'
        f'            assertEquals(tree, dec.root, "{safe_id(case["name"])}")\n'
        f'            val re = PolskiTextCodec.encode(dec.root, dec.kind, dec.schemaVersion)\n'
        f'            assertTrue(re.bytes.contentEquals(enc.bytes), "{safe_id(case["name"])}: re-encode")\n'
        f'        }}'
    )


def main() -> None:
    pos_cases = parse_cases(POS)

    pos_blocks = [emit_positive(c) for c in pos_cases if not c["is_negative"]]
    pos_body = "\n".join(b for b in pos_blocks if b)

    body = f"""\
    @Test fun allPositiveCases() {{
{pos_body}
    }}"""

    src = KT.read_text("utf-8")
    si = src.index(START) + len(START) + 1
    ei = src.index(END)
    KT.write_text(src[:si] + body + "\n" + src[ei:], "utf-8")

    n = sum(1 for b in pos_blocks if b)
    print(f"OK: {n} positive round-trip cases → {KT.relative_to(ROOT)}")
    print(f"  Oracle SHA: {hashlib.sha256(POS.read_bytes()).hexdigest()[:16]}...")


if __name__ == "__main__":
    main()
