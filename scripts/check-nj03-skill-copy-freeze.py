#!/usr/bin/env python3
"""Read-only, independent old-source oracle for the NJ03 skill-copy cutover.

The .cases file is an authored/frozen expectation, not a generated output of a
candidate DSL printer. This checker reads historical source documents only; it
never writes an application document or updates an expectation.
"""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import sys


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_CASES = ROOT / "Plans/Biome/NJ03SkillCopyIndependent.cases"
NEGATIVE_CASES = ROOT / "Plans/Biome/NJ03SkillCopyNegative.cases"
SEALED_CASES_SHA256 = "1e7933b61b666397fa23d40b80a9e197674bfd7148465cc92b2219c6589dea82"
SEALED_NEGATIVES_SHA256 = "aa9d4d72ddab9f46fd5002d0e31832bd40362edb402e366133e1250efa3afbc9"
SOURCES = (
    ("pl-v1", "courses/pl-ru/course.json", "7255e8c686ceee17a72d280e5876b0cf6776dd9a1743884cd75c724fb8180dcc"),
    ("pl-ru", "courses/pairs/pl-ru/pair.json", "dadb5eabaa02fa7dcf300f01bc3aa079cec43567247c249b3476d5c8a32215b7"),
    ("en-ru", "courses/pairs/en-ru/pair.json", "55f79438cf1511b1061d389f4d74117112c910092e7515c73c57aa0d3af61862"),
    ("en-curriculum", "courses/lang/en/curriculum.json", "4c8f1c21595479a1b49ebb38a6fa4df9fff15f02bbe6d6cdd2b8ed4ecaf0afe6"),
    ("pl-curriculum", "courses/lang/pl/curriculum.json", "4f98cb4e0a5bc5b5f21890120fe25e077cedc2ba5e65852bff4b1e252b4234b7"),
)
PAIRS = (("en-ru", "en-curriculum"), ("pl-ru", "pl-curriculum"))


class Rejected(Exception):
    def __init__(self, code: str, detail: str):
        super().__init__(f"{code}: {detail}")
        self.code = code


def require(condition: bool, code: str, detail: str) -> None:
    if not condition:
        raise Rejected(code, detail)


def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def decode_text(value: str) -> str:
    pieces: list[str] = []
    i = 0
    while i < len(value):
        char = value[i]
        if char != "\\":
            require(ord(char) >= 32, "E_ESCAPE", "unescaped control character")
            pieces.append(char)
            i += 1
            continue
        require(i + 1 < len(value), "E_ESCAPE", "trailing backslash")
        escape = value[i + 1]
        require(escape in ("\\", "n", "r", "t"), "E_ESCAPE", f"unknown escape \\{escape}")
        pieces.append({"\\": "\\", "n": "\n", "r": "\r", "t": "\t"}[escape])
        i += 2
    return "".join(pieces)


def parse_node(line: str) -> tuple[str, int, str, str, object]:
    bits = line.split(" | ", 5)
    require(len(bits) == 6 and bits[0] == "NODE", "E_FORMAT", line[:120])
    _, pair, position, path, kind, payload = bits
    require(position.isdecimal() and len(position) == 2, "E_FORMAT", line[:120])
    require(path == "$" or path.startswith("$/"), "E_FORMAT", line[:120])
    if kind == "TEXT":
        meta = payload.split(":", 2)
        require(len(meta) == 3 and meta[0].isdecimal() and len(meta[1]) == 64, "E_FORMAT", path)
        value = decode_text(meta[2])
        raw = value.encode("utf-8")
        require(len(raw) == int(meta[0]) and digest(raw) == meta[1], "E_TEXT_BYTES", path)
    elif kind == "OBJECT":
        value = tuple(payload.split(",")) if payload != "EMPTY" else ()
        require(len(value) == len(set(value)) and all(k and "/" not in k and "~" not in k for k in value), "E_FORMAT", path)
    elif kind == "LIST":
        require(payload.isdecimal(), "E_FORMAT", path)
        value = int(payload)
    elif kind == "BOOL":
        require(payload in ("true", "false"), "E_FORMAT", path)
        value = payload == "true"
    elif kind == "NULL":
        require(payload == "-", "E_FORMAT", path)
        value = None
    elif kind == "INT":
        require(payload.lstrip("-").isdecimal(), "E_FORMAT", path)
        value = int(payload)
    else:
        raise Rejected("E_FORMAT", f"unknown node kind at {path}: {kind}")
    return pair, int(position), path, kind, value


def walk(value: object, path: str = "$") -> list[tuple[str, str, object]]:
    if isinstance(value, dict):
        rows = [(path, "OBJECT", tuple(value))]
        for key, child in value.items():
            rows.extend(walk(child, f"{path}/{key}"))
        return rows
    if isinstance(value, list):
        rows = [(path, "LIST", len(value))]
        for index, child in enumerate(value):
            rows.extend(walk(child, f"{path}/{index}"))
        return rows
    if isinstance(value, str):
        return [(path, "TEXT", value)]
    if isinstance(value, bool):
        return [(path, "BOOL", value)]
    if value is None:
        return [(path, "NULL", None)]
    if type(value) is int:
        return [(path, "INT", value)]
    raise Rejected("E_SOURCE_SHAPE", f"unsupported old-source value at {path}: {type(value)}")


def read_old_sources() -> dict[str, object]:
    old: dict[str, object] = {}
    for key, relpath, expected_hash in SOURCES:
        path = ROOT / relpath
        raw = path.read_bytes()
        require(digest(raw) == expected_hash, "E_SOURCE_HASH", relpath)
        old[key] = json.loads(raw.decode("utf-8"))
    require(walk(old["pl-v1"]["skills"]) == walk(old["pl-ru"]["skills"]), "E_PROJECTION", "pl v1 → pair skill projection differs")
    return old


def check_lines(lines: list[str], old: dict[str, object], enforce_seal: bool) -> tuple[int, int]:
    raw = ("\n".join(lines) + "\n").encode("utf-8")
    if enforce_seal:
        require(digest(raw) == SEALED_CASES_SHA256, "E_CASES_HASH", "frozen expectation bytes changed")
    meaningful = [line for line in lines if line and not line.startswith("#")]
    expected_source_lines = [f"SOURCE | {key} | {relpath} | {sha}" for key, relpath, sha in SOURCES]
    require(meaningful[:len(SOURCES)] == expected_source_lines, "E_SOURCE_DECL", "source list/order/seal differs")
    body = meaningful[len(SOURCES):]
    headers: dict[str, list[tuple[int, str]]] = {pair: [] for pair, _ in PAIRS}
    nodes: dict[tuple[str, int], list[tuple[str, str, object]]] = {}
    seen_header: set[tuple[str, str]] = set()
    current: tuple[str, int] | None = None
    pair_order = {pair: i for i, (pair, _) in enumerate(PAIRS)}
    last_pair = 0
    for line in body:
        if line.startswith("SKILL | "):
            bits = line.split(" | ")
            require(len(bits) == 4 and bits[1] in headers and bits[2].isdecimal() and len(bits[2]) == 2, "E_FORMAT", line)
            pair, position, skill_id = bits[1], int(bits[2]), bits[3]
            require((pair, skill_id) not in seen_header, "E_DUP_ID", f"{pair}/{skill_id}")
            seen_header.add((pair, skill_id))
            require(pair_order[pair] >= last_pair, "E_ORDER", f"pair {pair} came after later pair")
            last_pair = pair_order[pair]
            headers[pair].append((position, skill_id))
            current = (pair, position)
            require(current not in nodes, "E_DUP_POSITION", f"{pair}/{position}")
            nodes[current] = []
            continue
        pair, position, path, kind, value = parse_node(line)
        require(current == (pair, position), "E_ORDER", f"node {pair}/{position}/{path} outside its skill block")
        require(path not in {p for p, _, _ in nodes[current]}, "E_DUP_NODE", f"{pair}/{position}/{path}")
        nodes[current].append((path, kind, value))

    count = 0
    node_count = 0
    for pair, curriculum_key in PAIRS:
        source_rows = old[pair]["skills"]
        curriculum_rows = old[curriculum_key]
        require(isinstance(curriculum_rows, list) and len(curriculum_rows) == len(source_rows) == 16, "E_SOURCE_SHAPE", pair)
        expected_ids = [row["id"] for row in curriculum_rows]
        got = headers[pair]
        require(len(got) == 16, "E_COUNT", f"{pair}: {len(got)} rows")
        for i, (position, skill_id) in enumerate(got):
            require(skill_id in expected_ids, "E_REF", f"{pair}/{skill_id} absent from {curriculum_key}")
            require(position == i + 1 and skill_id == expected_ids[i] == source_rows[i]["id"], "E_ORDER", f"{pair}/{i + 1}: {skill_id}")
            for field in ("level", "prerequisites"):
                require(source_rows[i][field] == curriculum_rows[i][field], "E_REF", f"{pair}/{skill_id}/{field}")
            actual = walk(source_rows[i])
            frozen = nodes[(pair, position)]
            require(frozen == actual, "E_COVERAGE", f"{pair}/{position}/{skill_id}: expected {len(actual)} nodes, got {len(frozen)}")
            count += 1
            node_count += len(frozen)
    return count, node_count


def mutate(lines: list[str], action: str, pair: str, position: int, argument: str) -> list[str]:
    result = lines.copy()
    prefix = f"SKILL | {pair} | {position:02d} | "
    start = next(i for i, line in enumerate(result) if line.startswith(prefix))
    end = next((i for i in range(start + 1, len(result)) if result[i].startswith("SKILL | ")), len(result))
    if action == "replace-reference":
        result[start] = prefix + argument
    elif action == "duplicate":
        result[end:end] = result[start:end]
    elif action == "swap-order":
        other = int(argument)
        other_prefix = f"SKILL | {pair} | {other:02d} | "
        second = next(i for i, line in enumerate(result) if line.startswith(other_prefix))
        second_end = next((i for i in range(second + 1, len(result)) if result[i].startswith("SKILL | ")), len(result))
        first_block, second_block = result[start:end], result[second:second_end]
        require(start < second, "E_NEGATIVE", "swap-order requires ascending positions")
        result = result[:start] + second_block + result[end:second] + first_block + result[second_end:]
    elif action in ("bad-escape", "drop-node", "null-node", "empty-text"):
        needle = f"NODE | {pair} | {position:02d} | {argument} | "
        index = next(i for i in range(start + 1, end) if result[i].startswith(needle))
        if action == "bad-escape":
            require(" | TEXT | " in result[index], "E_NEGATIVE", "bad-escape target is not text")
            result[index] += "\\q"
        elif action == "drop-node":
            del result[index]
        elif action == "null-node":
            result[index] = needle + "NULL | -"
        else:
            result[index] = needle + f"TEXT | 0:{digest(b'')}:"
    else:
        raise Rejected("E_NEGATIVE", f"unknown mutation {action}")
    return result


def check_negatives(lines: list[str], old: dict[str, object]) -> int:
    raw = NEGATIVE_CASES.read_bytes()
    require(digest(raw) == SEALED_NEGATIVES_SHA256, "E_NEGATIVES_HASH", "negative expectation bytes changed")
    negatives = raw.decode("utf-8").splitlines()
    count = 0
    for line in negatives:
        if not line or line.startswith("#"):
            continue
        bits = line.split(" | ")
        require(len(bits) == 7 and bits[0] == "NEGATIVE", "E_NEGATIVE", line)
        _, name, action, pair, position, argument, expected = bits
        try:
            check_lines(mutate(lines, action, pair, int(position), argument), old, False)
        except Rejected as error:
            require(error.code == expected, "E_NEGATIVE", f"{name}: expected {expected}, got {error.code}")
        else:
            raise Rejected("E_NEGATIVE", f"{name}: corrupted source was accepted")
        count += 1
    require(count >= 6, "E_NEGATIVE", f"only {count} negative cases")
    return count


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cases", type=Path, default=DEFAULT_CASES)
    args = parser.parse_args()
    try:
        raw = args.cases.read_bytes()
        lines = raw.decode("utf-8").splitlines()
        require(raw == ("\n".join(lines) + "\n").encode("utf-8"), "E_CASES_FORMAT", "must be UTF-8 LF with final newline")
        old = read_old_sources()
        skills, nodes = check_lines(lines, old, True)
        negatives = check_negatives(lines, old)
    except (OSError, UnicodeError, Rejected) as error:
        print(f"FAIL {error}", file=sys.stderr)
        return 1
    print(f"PASS source seals 5; Polish v1/pair projection; curriculum refs/order; {skills} skills; {nodes} complete nodes; {negatives} negative mutations; no output")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
