#!/usr/bin/env python3
"""Check N17 input-only recipes and frozen UTF-16 key spans, without Kotlin code.

Python's old-input parser supplies an independent semantic pair oracle. No JSON is
written, and this script does not implement the proposed Kotlin preflight scanner.
"""

import hashlib
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
PREFIX = "tests/fixtures/textwire/"
CASES = PREFIX + "legacy-duplicate-preflight.cases"
SEAL = PREFIX + "legacy-duplicate-preflight.sha256"
OLD = PREFIX + "inputs/legacy-duplicate-key-progress.input"
OLD_SEAL = PREFIX + "retained-number.sha256"
E2 = PREFIX + "inputs/legacy-representability-progress.input"
E2_SEAL = PREFIX + "legacy-representability.sha256"
EXPECTED_IDS = tuple(
    [f"D{i:02d}" for i in range(6)]
    + [f"U{i:02d}" for i in range(1, 6)]
    + [f"M{i:02d}" for i in range(1, 8)]
    + ["B01", "S01", "S02"]
)


def fail(message: str) -> None:
    raise SystemExit(f"FAIL N17 {message}")


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def sealed(path: str, ledger: str) -> bytes:
    data = (ROOT / path).read_bytes()
    if not matches_seal(path, ledger, data):
        fail(f"exact-path seal {path}")
    return data


def matches_seal(path: str, ledger: str, data: bytes) -> bool:
    line = f"{sha(data)}  {path}"
    return line in (ROOT / ledger).read_text(encoding="ascii").splitlines()


def utf16_position(source: str, index: int) -> int:
    return len(source[:index].encode("utf-16-le", "surrogatepass")) // 2


def expand_recipe(fragment: str) -> str:
    return fragment.replace("${U+0001}", "\u0001").replace("${U+D800}", "\ud800")


class Pairs(list):
    """Retain every old-input object entry in source order for read-only inspection."""


class NumberToken(str):
    """Old numeric lexeme, disjoint from an old quoted string."""


def old_tree(source: str):
    return json.loads(
        source,
        object_pairs_hook=Pairs,
        parse_int=NumberToken,
        parse_float=NumberToken,
        parse_constant=lambda value: fail(f"nonstandard old token {value}"),
    )


def first_duplicate(node, path=()):
    if isinstance(node, Pairs):
        seen = set()
        for key, value in node:
            if key in seen:
                return path + (key,)
            seen.add(key)
            found = first_duplicate(value, path + (key,))
            if found is not None:
                return found
    elif isinstance(node, list):
        for index, value in enumerate(node):
            found = first_duplicate(value, path + (str(index),))
            if found is not None:
                return found
    return None


def selected_cases(text: str) -> dict[str, list[str]]:
    cases = {}
    current = None
    for line in text.splitlines():
        if line.startswith("CASE "):
            if current is not None:
                fail("nested CASE")
            ident = line.split(" ", 2)[1]
            if ident in cases:
                fail(f"duplicate case {ident}")
            current = ident
            cases[ident] = []
        elif line == "END":
            if current is None:
                fail("orphan END")
            current = None
        elif current is not None:
            cases[current].append(line)
    if current is not None or tuple(cases) != EXPECTED_IDS:
        fail(f"selected case order/count {tuple(cases)}")
    return cases


def one(lines: list[str], prefix: str) -> str:
    found = [line[len(prefix) :] for line in lines if line.startswith(prefix)]
    if len(found) != 1:
        fail(f"expected one {prefix!r}, found {len(found)}")
    return found[0]


def source_for(ident: str, lines: list[str], old: str, clean: str):
    base = one(lines, "from ")
    if base not in ("sealed", "clean"):
        fail(f"{ident} unknown base")
    if ident == "D00" and base != "sealed" or ident != "D00" and base != "clean":
        fail(f"{ident} base changed")
    source = old if base == "sealed" else clean
    edits = [line for line in lines if line.startswith("replace ")]
    tail_edits = [line for line in lines if line.startswith("replace-tail ")]
    byte_edits = [line for line in lines if line.startswith("replace-first-utf8 ")]
    if len(edits) + len(tail_edits) + len(byte_edits) != (0 if ident == "D00" else 1):
        fail(f"{ident} recipe count")
    if edits:
        before, after = edits[0][len("replace ") :].split(" => ", 1)
        after = expand_recipe(after)
        if source.count(before) != 1:
            fail(f"{ident} replacement anchor cardinality")
        source = source.replace(before, after, 1)
    if tail_edits:
        before, after = tail_edits[0][len("replace-tail ") :].split(" => ", 1)
        if source.count(before) != 1:
            fail(f"{ident} truncation anchor cardinality")
        source = source[: source.index(before)] + expand_recipe(after)
    if byte_edits:
        before, hex_bytes = byte_edits[0][len("replace-first-utf8 ") :].split(" => hex ", 1)
        data = source.encode("utf-8")
        needle = before.encode("utf-8")
        if data.count(needle) != 1:
            fail(f"{ident} byte anchor cardinality")
        return data.replace(needle, bytes.fromhex(hex_bytes), 1), None
    if ident.startswith("S"):
        try:
            source.encode("utf-8", "strict")
        except UnicodeEncodeError:
            if "expect no-utf8-file-recipe" not in lines:
                fail(f"{ident} host-string boundary not declared")
            return None, source
        fail(f"{ident} unexpectedly represents a UTF-8 file")
    return source.encode("utf-8", "strict"), source


def check_bytes(ident: str, lines: list[str], data: bytes) -> None:
    claim = one(lines, "expect bytes ")
    match = re.fullmatch(r"(\d+) sha256 ([0-9a-f]{64})", claim)
    if match is None or (len(data), sha(data)) != (int(match[1]), match[2]):
        fail(f"{ident} frozen length/SHA")


def check_duplicate(ident: str, lines: list[str], source: str, path: tuple[str, ...]) -> None:
    label = "expect DuplicateIfLegacyAccepts path " if ident == "S02" else "expect Duplicate path "
    claim = one(lines, label)
    match = re.fullmatch(r"(.+) first (\d+):(\d+) second (\d+):(\d+)", claim)
    claimed_path = tuple(match[1].split("/")) if match is not None else ()
    if ident == "S02":
        claimed_path = claimed_path[:-1] + ("\ud800",)
    if match is None or claimed_path != path:
        fail(f"{ident} decoded duplicate path")
    container = one(lines, "container ")
    keys = one(lines, "keys ").split(" | ")
    if len(keys) != 2:
        fail(f"{ident} key spelling count")
    start = source.index(container)
    spans = []
    for key in keys:
        key = expand_recipe(key)
        if json.loads(key) != path[-1]:
            fail(f"{ident} raw spelling does not decode to path key")
        index = source.find(key + ":", start)
        if index < 0:
            fail(f"{ident} expected raw key not found after container")
        spans.append((utf16_position(source, index), utf16_position(source, index + len(key))))
        start = index + len(key) + 1
    expected = ((int(match[2]), int(match[3])), (int(match[4]), int(match[5])))
    if tuple(spans) != expected or spans[0][1] > spans[1][0]:
        fail(f"{ident} frozen UTF-16 key spans: {spans} != {expected}")


def check_malformed(ident: str, lines: list[str], source: str) -> None:
    trigger = expand_recipe(one(lines, "trigger-fragment "))
    marker = source.index('"nested":')
    offset = source.find(trigger, marker)
    expected = int(one(lines, "expect UnsafeInput trigger-at "))
    if offset < 0 or utf16_position(source, offset) != expected:
        fail(f"{ident} independently frozen malformed trigger")
    try:
        old_tree(source)
    except (json.JSONDecodeError, ValueError):
        return
    fail(f"{ident} malformed input accepted by old parser")


def main() -> None:
    old_bytes = sealed(OLD, OLD_SEAL)
    if len(old_bytes) != 674 or sha(old_bytes) != "8cc8e3335dce5e58c242b8dc9fbcd30fac2d156f03f534aa4ef79e207515be12":
        fail("old input independent 674-byte control")
    e2_bytes = sealed(E2, E2_SEAL)
    cases_bytes = sealed(CASES, SEAL)
    if (ROOT / SEAL).read_text(encoding="ascii").splitlines() != [f"{sha(cases_bytes)}  {CASES}"]:
        fail("new ledger must own only its one exact-path case file")
    old = old_bytes.decode("utf-8", "strict")
    clean = old.replace('"wide":1,"wide":2', '"wide":1', 1)
    if old.count('"wide":1,"wide":2') != 1:
        fail("sealed original duplicate anchor")
    cases = selected_cases(cases_bytes.decode("utf-8", "strict"))
    if "BASE clean sealed replace \"wide\":1,\"wide\":2 => \"wide\":1" not in cases_bytes.decode():
        fail("declared shared clean base")

    counts = {"D": 0, "U": 0, "M": 0, "B": 0, "S": 0}
    for ident, lines in cases.items():
        data, source = source_for(ident, lines, old, clean)
        counts[ident[0]] += 1
        if data is not None:
            check_bytes(ident, lines, data)
        if ident.startswith("D"):
            path = first_duplicate(old_tree(source))
            if path is None:
                fail(f"{ident} does not contain a decoded duplicate")
            check_duplicate(ident, lines, source, path)
        elif ident.startswith("U"):
            if "expect Unique" not in lines or first_duplicate(old_tree(source)) is not None:
                fail(f"{ident} false key/duplicate control")
        elif ident.startswith("M"):
            check_malformed(ident, lines, source)
        elif ident == "B01":
            if "expect HostDecodeReject before-string before-write raw-bytes-unchanged" not in lines:
                fail("B01 host decode boundary")
            try:
                data.decode("utf-8", "strict")
            except UnicodeDecodeError:
                pass
            else:
                fail("B01 invalid UTF-8 accepted")
        elif ident == "S01":
            if "expect UniqueIfLegacyAcceptsOrUnsafeInputIfLegacyRejects; probe Kotlin old-reader admission before changing it" not in lines:
                fail("S01 host-string boundary")
        elif ident == "S02":
            path = first_duplicate(old_tree(source))
            if path != ("unknown", "nested", "\ud800"):
                fail("S02 decoded raw/escaped surrogate duplicate")
            check_duplicate(ident, lines, source, path)
            if "expect RecoveryRequiredIfLegacyAccepts before-write raw-unchanged zero-put zero-new-key" not in lines:
                fail("S02 conditional no-write boundary")

    text = cases_bytes.decode("utf-8")
    if text.count("expect RecoveryRequired before-write raw-unchanged zero-put zero-new-key") != 2:
        fail("two no-write recovery declarations")
    for marker in ("CONTROL E2 unknown-order ", "CONTROL E2 numeric-spelling ", "CONTROL admitted-progress PG-10000000", "COST scanner and old-parser separately", "HOST web ", "HOST Android ", "HOST macOS ", "HOST iOS ", "HOST Desktop ", "HOST shared "):
        if text.count(marker) != 1:
            fail(f"missing independent control {marker}")
    # Existing E2 oracle is still source-owned; verify its named order without a new writer.
    e2 = old_tree(e2_bytes.decode("utf-8", "strict"))
    root = dict(e2)
    unknown = dict(root["unknown"])
    named_order = ["longKey" if key.startswith("longKey-") else key for key, _ in root["unknown"]]
    ordered = one(text.splitlines(), "CONTROL E2 unknown-order ").split(",")
    if named_order != ordered or list(unknown)[:5] != ["wide", "huge", "decimal", "exponentOne", "negativeZero"]:
        fail("E2 unknown order")
    tokens = dict(item.split("=", 1) for item in one(text.splitlines(), "CONTROL E2 numeric-spelling ").split(" "))
    if len(tokens) != 5 or any(type(unknown[key]) is not NumberToken or unknown[key] != value for key, value in tokens.items()):
        fail("E2 five numeric token spellings/types")
    if (ROOT / OLD).read_bytes() != old_bytes or (ROOT / E2).read_bytes() != e2_bytes:
        fail("old-user input modified during static check")
    if matches_seal(CASES, SEAL, cases_bytes + b"x"):
        fail("new-case seal mutation admitted")
    if matches_seal(OLD, OLD_SEAL, old_bytes + b"x"):
        fail("old-input seal mutation admitted")
    print(f"PASS N17 {len(cases)}/21 selected static recipes: D={counts['D']} U={counts['U']} M={counts['M']} B={counts['B']} S={counts['S']}")
    print("PASS N17 exact-path new-case and immutable old-input seals; 2/2 in-memory mutation controls")
    print("PASS N17 six UTF-8 duplicate paths plus one conditional host-string pair; five Unique; seven malformed old-parser rejects")
    print("NOT RUN Kotlin scanner/no-write fake, Gradle, common targets, browser, native hosts, P23 cost")


if __name__ == "__main__":
    main()
