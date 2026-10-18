#!/usr/bin/env python3
"""Independent E3 A/C byte arithmetic; no candidate encoder or app output."""

import hashlib
import json
import re
import runpy
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
BASE = "tests/fixtures/textwire/"
OLD = BASE + "inputs/legacy-representability-progress.input"
R = BASE + "retained-number.cases"
SIZES = BASE + "retained-number-sizes.cases"
LEDGER = BASE + "retained-number-sizes.sha256"
ADMISSION = BASE + "numeric-admission.cases"
BOUNDARY = BASE + "legacy-boundary.cases"
BASE_SIZES = BASE + "check_numeric_sizes.py"
CHECKER = BASE + "check_retained_number_sizes.py"
SEALED = (OLD, R, ADMISSION, BOUNDARY, BASE_SIZES, CHECKER, SIZES)


def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def check_seal() -> tuple[bytes, ...]:
    content = tuple((ROOT / path).read_bytes() for path in SEALED)
    actual = (ROOT / LEDGER).read_text(encoding="ascii").splitlines()
    expected = [f"{digest(data)}  {path}" for path, data in zip(SEALED, content, strict=True)]
    if actual != expected:
        raise SystemExit("FAIL E3 size exact-path/source seal")
    for index in range(len(content)):
        changed = list(content)
        changed[index] += b"x"
        if [f"{digest(data)}  {path}" for path, data in zip(SEALED, changed, strict=True)] == actual:
            raise SystemExit(f"FAIL E3 size seal mutation {SEALED[index]}")
    return content


class Number(str):
    """Old numeric lexeme, distinct from an old quoted string."""


class I64(str):
    """Known typed integer field in the old progress schema."""


class Bits(str):
    """Exact 16-digit binary64 payload from the R01 independent tree."""


def unique(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate key: {key}")
        result[key] = value
    return result


def units(value: str) -> list[int]:
    raw = value.encode("utf-16-be", "surrogatepass")
    return [int.from_bytes(raw[i:i + 2], "big") for i in range(0, len(raw), 2)]


def a_text_bytes(value: str) -> int:
    code_units = units(value)
    total = 0
    index = 0
    while index < len(code_units):
        unit = code_units[index]
        if 0xD800 <= unit <= 0xDBFF and index + 1 < len(code_units) and 0xDC00 <= code_units[index + 1] <= 0xDFFF:
            total += 4
            index += 2
            continue
        if unit in (0x5C, 0x09, 0x0A, 0x0D):
            total += 2
        elif unit <= 0x1F or 0x7F <= unit <= 0x9F or 0xD800 <= unit <= 0xDFFF:
            total += 6
        elif unit <= 0x7F:
            total += 1
        elif unit <= 0x7FF:
            total += 2
        else:
            total += 3
        index += 1
    return total


def c_text_bytes(value: str) -> int:
    count = len(units(value))
    return 2 + len(str(count)) + 4 * count


def key_bytes(arm: str, key: str | None) -> int:
    if key is None:
        return 1
    if arm == "A":
        return 2 + a_text_bytes(key)
    count = len(units(key))
    return 3 + len(str(count)) + 4 * count


def row_bytes(arm: str, tag: str, key: str | None, payload: int = 0) -> int:
    # tag, HT, key, optional HT and payload, LF; N has no payload column.
    return 1 + 1 + key_bytes(arm, key) + (0 if tag == "N" else 1 + payload) + 1


def subtree(arm: str, value, key: str | None = None) -> tuple[int, int, int]:
    """Return (canonical bytes excluding header/H, preorder rows, R payload bytes)."""
    if isinstance(value, dict):
        head = row_bytes(arm, "M", key, len(str(len(value))))
        parts = [subtree(arm, child, child_key) for child_key, child in value.items()]
    elif isinstance(value, list):
        head = row_bytes(arm, "L", key, len(str(len(value))))
        parts = [subtree(arm, child) for child in value]
    elif isinstance(value, Number):
        if not re.fullmatch(r"-?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]+)?", value, re.ASCII):
            raise SystemExit("FAIL E3 source numeric grammar")
        length = len(value.encode("ascii"))
        return row_bytes(arm, "R", key, length), 1, length
    elif isinstance(value, I64):
        return row_bytes(arm, "I", key, len(value.encode("ascii"))), 1, 0
    elif isinstance(value, Bits):
        return row_bytes(arm, "D", key, 16), 1, 0
    elif isinstance(value, str):
        length = a_text_bytes(value) if arm == "A" else c_text_bytes(value)
        return row_bytes(arm, "S", key, length), 1, 0
    elif value is None:
        return row_bytes(arm, "N", key), 1, 0
    else:
        raise SystemExit(f"FAIL E3 unplanned tree type {type(value).__name__}")
    return head + sum(part[0] for part in parts), 1 + sum(part[1] for part in parts), sum(part[2] for part in parts)


def complete(arm: str, tree) -> tuple[int, int, int, int]:
    header = 25 if arm == "A" else 26
    rows, count, retained = subtree(arm, tree)
    h_row = 74  # H, HT, sha256, HT, 64 hex digits, LF
    return header + rows + h_row, header, rows, retained


def expectation(cases: str, label: str) -> tuple[int, ...]:
    match = re.search(rf"^{re.escape(label)} = ([0-9, ]+)$", cases, re.M)
    if match is None:
        raise SystemExit(f"FAIL E3 size expectation absent: {label}")
    return tuple(int(part) for part in match[1].split(", "))


old_bytes, r_bytes, admission_bytes, boundary_bytes, _, _, size_bytes = check_seal()
old = json.loads(
    old_bytes.decode("utf-8", "strict"),
    parse_int=Number,
    parse_float=Number,
    parse_constant=lambda token: (_ for _ in ()).throw(ValueError(token)),
    object_pairs_hook=unique,
)
source = r_bytes.decode("utf-8")
cases = size_bytes.decode("utf-8")
if len(old_bytes) != 683 or [*old] != ["version", "cards", "stats", "reviewsToday", "lastDay", "totalReviews", "unknown"]:
    raise SystemExit("FAIL E3 old source shape")
unknown = old["unknown"]
if [*unknown][:-1] != ["wide", "huge", "decimal", "exponentOne", "negativeZero", "nullValue", "emptyString", "emptyList", "emptyRecord", "nested", "literal", "escaped", "loneHigh"] or len(unknown) != 14:
    raise SystemExit("FAIL E3 old unknown order")
if [unknown[name] for name in ("wide", "huge", "decimal", "exponentOne", "negativeZero")] != ["9223372036854775808", "1e10000", "1.0", "1e0", "-0"]:
    raise SystemExit("FAIL E3 old number spellings")
if any(type(unknown[name]) is not Number for name in ("wide", "huge", "decimal", "exponentOne", "negativeZero")):
    raise SystemExit("FAIL E3 old number typing")
if [*unknown["nested"]] != ["z", "a", "m"] or [unknown["nested"][key] for key in ("z", "a", "m")] != ["1", "2", "3"]:
    raise SystemExit("FAIL E3 nested number tree")
if not next(reversed(unknown)).startswith("longKey-") or len(next(reversed(unknown))) != 320:
    raise SystemExit("FAIL E3 long key")
if (units(unknown["literal"]), units(unknown["escaped"]), units(unknown["loneHigh"])) != ([0xD83D, 0xDE00], [0xD83D, 0xDE00], [0xD800]):
    raise SystemExit("FAIL E3 Unicode units")
if source.count("CASE R01 ") != 1 or source.count("CASE R02 ") != 1:
    raise SystemExit("FAIL E3 source case count")

# R01's independent tree block is parsed as authored text, never from wire output.
tree_block = source.split("CASE R01 ", 1)[1].split("\norder root:", 1)[0].split("\ntree\n", 1)[1]
r01 = {}
list_key = None
for line in tree_block.splitlines():
    match = re.fullmatch(r'    key "([^"]+)" => (retained-number|i64|f64bits|string|null|list)(?: "([^"]*)"| ([^ ]+))?', line)
    if match:
        key, kind, quoted, bare = match.groups()
        if kind == "retained-number":
            r01[key] = Number(quoted)
        elif kind == "i64":
            r01[key] = I64(bare)
        elif kind == "f64bits":
            r01[key] = Bits(bare)
        elif kind == "string":
            r01[key] = quoted
        elif kind == "null":
            r01[key] = None
        else:
            r01[key] = []
            list_key = key
    elif line == '      item => retained-number "1e0"' and list_key is not None:
        r01[list_key].append(Number("1e0"))
    elif line.strip() != "map":
        raise SystemExit(f"FAIL E3 unsupported authored R01 tree line {line!r}")
if len(r01) != 10 or list_key != "listExample":
    raise SystemExit("FAIL E3 R01 tree cardinality")

for arm, literal_size in (("A", 316), ("C", 618)):
    r01_total, header, rows, retained = complete(arm, r01)
    if (r01_total, header, rows, retained) != expectation(cases, f"R01 {arm}") or r01_total != literal_size:
        raise SystemExit(f"FAIL E3 R01 {arm} independent arithmetic")
    e2_total, e2_header, e2_rows, e2_retained = complete(arm, unknown)
    if (e2_total, e2_header, e2_rows, e2_retained) != expectation(cases, f"E2-unknown {arm}"):
        raise SystemExit(f"FAIL E3 E2-unknown {arm} independent arithmetic")
    if e2_retained != 37:
        raise SystemExit("FAIL E3 all eight numeric lexemes not counted")
    print(f"PASS E3 {arm}: R01 {r01_total} bytes; E2 unknown subtree {e2_total} bytes, {e2_retained} retained-token bytes")

    wider_number = dict(r01)
    wider_number["wide"] = Number(str(r01["wide"]) + "0")
    if complete(arm, wider_number)[0] != r01_total + 1 or subtree(arm, wider_number)[2] != retained + 1:
        raise SystemExit(f"FAIL E3 {arm} token-length negative control")
    unicode_key = {("wïde" if key == "wide" else key): value for key, value in r01.items()}
    if complete(arm, unicode_key)[0] != r01_total + (1 if arm == "A" else 0):
        raise SystemExit(f"FAIL E3 {arm} UTF-8/UTF-16 key negative control")
    changed_e2 = dict(unknown)
    changed_e2["literal"] = "A"
    if complete(arm, changed_e2)[0] != e2_total - (3 if arm == "A" else 4):
        raise SystemExit(f"FAIL E3 {arm} supplementary-scalar negative control")

if (expectation(cases, "PG-10000000 A"), expectation(cases, "PG-10000000 C"), expectation(cases, "PG-rows-32768 A"), expectation(cases, "PG-rows-32768 C")) != ((10_000_112,), (40_000_022,), (10_196_726,), (39_214_145,)):
    raise SystemExit("FAIL E3 inherited large-tree lower floors")
if "10,000,000" not in admission_bytes.decode("utf-8") or "65,545" not in admission_bytes.decode("utf-8") or "PG-10000000" not in boundary_bytes.decode("utf-8"):
    raise SystemExit("FAIL E3 inherited controls absent")
namespace = runpy.run_path(str(ROOT / BASE_SIZES))
for arm, one, many in (("A", 10_000_112, 10_196_726), ("C", 40_000_022, 39_214_145)):
    if (namespace["size"](arm, False), namespace["size"](arm, True)) != (one, many):
        raise SystemExit(f"FAIL E3 inherited {arm} arithmetic")
    if (expectation(cases, f"PG-single durable-floor {arm}"), expectation(cases, f"PG-many durable-floor {arm}")) != ((10_000_000 + one,), (10_000_000 + many,)):
        raise SystemExit(f"FAIL E3 inherited {arm} old-plus-new floor")
print("PASS E3 7/7 source seals, 7/7 seal mutation controls; 4/4 inherited large-tree floors unchanged")
print("PASS E3 6/6 arithmetic perturbations, 4/4 durable old-plus-new floor controls")
print("NOT RUN physical host quotas, heap/peak allocations, P23, N14, N16/N26, N17 Kotlin old-reader preflight")
