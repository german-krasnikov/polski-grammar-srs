#!/usr/bin/env python3
"""Check independently authored E3 bytes, typed distinctions and input-only seals.

This is fixture consistency arithmetic, not an A/C codec or an application writer.
Only the two exact-path old-user inputs are parsed; no JSON is emitted.
"""

import hashlib
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
BASE = "tests/fixtures/textwire/"
CASES = BASE + "retained-number.cases"
OLD = BASE + "inputs/legacy-representability-progress.input"
DUPLICATE = BASE + "inputs/legacy-duplicate-key-progress.input"
SEAL = BASE + "retained-number.sha256"
E2_CASES = BASE + "legacy-representability.cases"
E2_SEAL = BASE + "legacy-representability.sha256"
PATHS = (OLD, DUPLICATE, CASES)


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def verify_seal(paths: tuple[str, ...], seal_path: str) -> tuple[bytes, ...]:
    contents = tuple((ROOT / path).read_bytes() for path in paths)
    actual = (ROOT / seal_path).read_text(encoding="ascii").splitlines()
    expected = [f"{sha(data)}  {path}" for path, data in zip(paths, contents, strict=True)]
    if actual != expected:
        raise SystemExit(f"FAIL exact-path SHA-256 seal: {seal_path}")
    for index, data in enumerate(contents):
        changed = list(contents)
        changed[index] = data + b"x"
        mutated = [f"{sha(blob)}  {path}" for path, blob in zip(paths, changed, strict=True)]
        if mutated == actual:
            raise SystemExit(f"FAIL seal mutation admitted: {paths[index]}")
    return contents


class OldNumber(str):
    """Read-only old numeric token, deliberately disjoint from a quoted string."""


def unique_pairs(pairs):
    output = {}
    for key, value in pairs:
        if key in output:
            raise ValueError(f"duplicate old-user key: {key}")
        output[key] = value
    return output


def parse_old(data: bytes, unique: bool):
    return json.loads(
        data.decode("utf-8", "strict"),
        parse_int=OldNumber,
        parse_float=OldNumber,
        parse_constant=lambda token: (_ for _ in ()).throw(ValueError(token)),
        object_pairs_hook=unique_pairs if unique else dict,
    )


def case_block(cases: str, marker: str, end: str) -> str:
    if cases.count(marker) != 1:
        raise SystemExit(f"FAIL case marker cardinality: {marker!r}")
    return cases.split(marker, 1)[1].split(end, 1)[0]


def wire_bytes(block: str, arm: str) -> tuple[bytes, bytes]:
    start = f"input {arm} utf8-lines\n"
    end = "input C utf8-lines\n" if arm == "A" else "expect A "
    literal = case_block(block, start, end).encode("utf-8")
    prefix, final = literal.split(b"H\tsha256\t", 1)
    if len(final) != 65 or not final.endswith(b"\n"):
        raise SystemExit(f"FAIL {arm} digest row/final LF")
    digest = final[:-1]
    if not re.fullmatch(rb"[0-9a-f]{64}", digest) or digest.decode() != sha(prefix):
        raise SystemExit(f"FAIL {arm} literal prefix digest")
    full = prefix + b"H\tsha256\t" + digest + b"\n"
    match = re.search(rf"^expect {arm} (\d+) bytes SHA-256 ([0-9a-f]{{64}})$", block, re.M)
    if match is None or (len(full), sha(full)) != (int(match[1]), match[2]):
        raise SystemExit(f"FAIL {arm} literal byte length/full SHA-256")
    return prefix, full


def retained_valid(token: str) -> bool:
    return re.fullmatch(r"-?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]+)?", token, re.ASCII) is not None


def recalc(prefix: bytes) -> bytes:
    return prefix + b"H\tsha256\t" + sha(prefix).encode("ascii") + b"\n"


old_bytes, duplicate_bytes, cases_bytes = verify_seal(PATHS, SEAL)
verify_seal((OLD, E2_CASES), E2_SEAL)
cases = cases_bytes.decode("utf-8", "strict")
if cases.count("CASE R01 ") != 1 or cases.count("CASE R02 ") != 1:
    raise SystemExit("FAIL E3 case count")
r01 = case_block(cases, "CASE R01 ", "\nEND\n")
r02 = case_block(cases, "CASE R02 ", "\nEND\n")
if "full E2 A/C bytes NOT FROZEN" not in r02:
    raise SystemExit("FAIL full-E2 scope label")

old = parse_old(old_bytes, True)
unknown = old["unknown"]
expected_order = re.search(r"^order source\.unknown: (.+)$", r02, re.M)
if expected_order is None:
    raise SystemExit("FAIL missing E2 order")
named_order = ["longKey" if key.startswith("longKey-") else key for key in unknown]
if named_order != expected_order[1].split(", "):
    raise SystemExit("FAIL E2 unknown field order")
retained = re.findall(r"^retained source\.unknown\.([^ ]+) ([^\n]+)$", r02, re.M)
if len(retained) != 5 or [name for name, _ in retained] != named_order[:5]:
    raise SystemExit("FAIL five ordered E2 retained tokens")
for name, token in retained:
    if type(unknown[name]) is not OldNumber or unknown[name] != token or not retained_valid(token):
        raise SystemExit(f"FAIL E2 numeric node/type/lexeme {name}")
tree_tokens = re.findall(r'^    key "([^"]+)" => retained-number "([^"]+)"$', r01, re.M)
if tree_tokens != retained:
    raise SystemExit("FAIL R01 tree differs from E2 five tokens/order")
if not all(text in r01 for text in (
    'key "zero" => i64 0',
    'key "negativeZeroBits" => f64bits 8000000000000000',
    'key "numericText" => string "-0"',
    'key "nullValue" => null',
    'item => retained-number "1e0"',
)):
    raise SystemExit("FAIL typed R/I/D/S/N/list distinctions")

try:
    parse_old(duplicate_bytes, True)
except ValueError as error:
    if str(error) != "duplicate old-user key: wide":
        raise SystemExit(f"FAIL duplicate-key error drift: {error}")
else:
    raise SystemExit("FAIL duplicate old-user key admitted by preflight")
collapsed = parse_old(duplicate_bytes, False)
if collapsed["unknown"]["wide"] != "2":
    raise SystemExit("FAIL duplicate control does not demonstrate last-value collapse")

prefixes = {}
for arm in ("A", "C"):
    prefix, full = wire_bytes(r01, arm)
    prefixes[arm] = prefix
    rows = prefix.decode("utf-8").splitlines()
    if rows[1] != "M\t-\t10" or len(rows) != 13 or rows[-1] != "R\t-\t1e0":
        raise SystemExit(f"FAIL {arm} root/row/list framing")
    for index, (name, token) in enumerate(retained, 2):
        key = "K:" + name if arm == "A" else "KU" + str(len(name)) + ":" + name.encode("utf-16-be").hex().upper()
        if rows[index] != f"R\t{key}\t{token}":
            raise SystemExit(f"FAIL {arm} R row {name}")
    if len(full) != (316 if arm == "A" else 618):
        raise SystemExit(f"FAIL {arm} small-root size")

negative = re.findall(r"^NEGATIVE (N\d\d) \| (.*?) \| (.*?) \| (.*?)$", cases, re.M)
if len(negative) != 17 or [n[0] for n in negative] != [f"N{i:02d}" for i in range(1, 18)]:
    raise SystemExit("FAIL E3 negative row selection")
expected_labels = {
    10: ("R wide payload one-byte HT", "Reject RowArity", "Generic"),
    11: ("R wide payload 1 then one-byte LF then 0", "Reject RowArityOrTrailingRow", "Generic"),
    12: ("R wide key sentinel -", "Reject MapKey", "Generic"),
    13: ("R list item key named wide", "Reject ListItemKey", "Generic"),
    14: ("R wide key reviewsToday, payload 1", "Reject KnownTypedFieldR", "Domain"),
    15: ("flip one covered R wide payload byte; retain original H", "Reject DigestMismatch", "Generic"),
    16: ("R wide token longer than selected N26 scalar limit", "PENDING N26 numeric bound", "N26"),
    17: ("old input duplicate unknown key with indistinguishable parser result", "RecoveryRequired; sealed input-only control, Kotlin preflight NOT RUN", "Legacy"),
}
for index, label in expected_labels.items():
    if negative[index - 1][1:] != label:
        raise SystemExit(f"FAIL N{index:02d} description/outcome/boundary")
invalid = ("+1", "01", ".5", "1.", "1e", "--1", "NaN", "Infinity", "\u0661")
for index, token in enumerate(invalid, 1):
    _, label, expected, boundary = negative[index - 1]
    if not label.startswith("R wide payload ") or expected != "Reject RetainedNumberSyntax" or boundary != "Generic":
        raise SystemExit(f"FAIL N{index:02d} negative label")
    if retained_valid(token):
        raise SystemExit(f"FAIL N{index:02d} invalid token accepted")

for arm, prefix in prefixes.items():
    wide_key = b"K:wide" if arm == "A" else b"KU4:0077006900640065"
    wide = b"R\t" + wide_key + b"\t9223372036854775808\n"
    if prefix.count(wide) != 1:
        raise SystemExit(f"FAIL {arm} wide row cardinality")
    for index, token in enumerate(invalid, 1):
        mutated = prefix.replace(wide, b"R\t" + wide_key + b"\t" + token.encode("utf-8") + b"\n")
        full = recalc(mutated)
        if full.rsplit(b"H\tsha256\t", 1)[1][:-1].decode() != sha(mutated):
            raise SystemExit(f"FAIL N{index:02d} {arm} mutated digest")
    special = (
        (10, b"R\t" + wide_key + b"\t\t\n"),
        (11, b"R\t" + wide_key + b"\t1\n0\n"),
        (12, b"R\t-\t9223372036854775808\n"),
        (14, b"R\t" + (b"K:reviewsToday" if arm == "A" else b"KU12:00720065007600690065007700730054006F006400610079") + b"\t1\n"),
    )
    for number, row in special:
        mutated = prefix.replace(wide, row)
        if mutated == prefix or not recalc(mutated).endswith(sha(mutated).encode() + b"\n"):
            raise SystemExit(f"FAIL N{number:02d} {arm} mutated digest")
        if number == 10 and len(row[:-1].split(b"\t")) != 4:
            raise SystemExit(f"FAIL N10 {arm} no extra TAB field")
        if number == 11 and len(mutated.splitlines()) != len(prefix.splitlines()) + 1:
            raise SystemExit(f"FAIL N11 {arm} no embedded LF row")
        if number == 12 and row.split(b"\t")[1] != b"-":
            raise SystemExit(f"FAIL N12 {arm} map key not sentinel")
        if number == 14 and b"reviewsToday" not in row and arm == "A":
            raise SystemExit("FAIL N14 known field key")
    list_item = b"R\t-\t1e0\n"
    named_list = b"R\t" + wide_key + b"\t1e0\n"
    if prefix.count(list_item) != 1 or not recalc(prefix.replace(list_item, named_list)).endswith(b"\n"):
        raise SystemExit(f"FAIL N13 {arm} list key/digest")
    original = recalc(prefix)
    corrupt = prefix.replace(wide, wide.replace(b"922337", b"822337", 1))
    if sha(corrupt) == sha(prefix) or sha(corrupt) == original.rsplit(b"\t", 1)[1][:-1].decode():
        raise SystemExit(f"FAIL N15 {arm} bad digest control")

if (ROOT / OLD).read_bytes() != old_bytes or (ROOT / DUPLICATE).read_bytes() != duplicate_bytes:
    raise SystemExit("FAIL old-user input changed during static check")
print("PASS E3 3/3 exact-path SHA-256 seals, 3/3 byte mutation controls")
print("PASS E3 5/5 ordered non-string E2 tokens; duplicate old-user key preflight recovery")
print("PASS E3 A 316/C 618 exact literal bytes and digests; 14/14 recomputed mutation recipes plus 1 bad-digest control per arm")
print("PENDING N16 numeric bound; N17 host old-reader/preflight and full E2 A/C bytes NOT RUN")
