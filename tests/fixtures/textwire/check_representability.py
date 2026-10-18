#!/usr/bin/env python3
"""Validate sealed, input-only old-user bytes against independent E2 expectations."""

import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
INPUT = "tests/fixtures/textwire/inputs/legacy-representability-progress.input"
CASES = "tests/fixtures/textwire/legacy-representability.cases"
SEAL = ROOT / "tests/fixtures/textwire/legacy-representability.sha256"


def sealed(input_bytes: bytes, cases_bytes: bytes, seal_text: str) -> bool:
    lines = seal_text.splitlines()
    paths = (INPUT, CASES)
    contents = (input_bytes, cases_bytes)
    return len(lines) == len(paths) and all(
        line == f"{hashlib.sha256(content).hexdigest()}  {path}"
        for line, path, content in zip(lines, paths, contents, strict=True)
    )


class NumberToken(str):
    """Keep the original numeric token distinct from a quoted string."""


def object_with_unique_keys(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate old-user key: {key}")
        result[key] = value
    return result


def utf16_hex(value: str) -> str:
    units = value.encode("utf-16-be", "surrogatepass")
    return ",".join(f"{units[index] * 256 + units[index + 1]:04x}" for index in range(0, len(units), 2))


input_bytes = (ROOT / INPUT).read_bytes()
cases_bytes = (ROOT / CASES).read_bytes()
seal_text = SEAL.read_text(encoding="ascii")
if not sealed(input_bytes, cases_bytes, seal_text):
    raise SystemExit("FAIL E2 exact-path SHA seal")
if sealed(input_bytes + b" ", cases_bytes, seal_text) or sealed(input_bytes, cases_bytes + b" ", seal_text):
    raise SystemExit("FAIL E2 seal admitted a changed input or expectation")

root = json.loads(
    input_bytes.decode("utf-8", "strict"),
    parse_int=NumberToken,
    parse_float=NumberToken,
    parse_constant=lambda token: (_ for _ in ()).throw(ValueError(f"non-finite token: {token}")),
    object_pairs_hook=object_with_unique_keys,
)
unknown = root["unknown"]
long_keys = [key for key in unknown if key.startswith("longKey-")]
if len(long_keys) != 1:
    raise SystemExit("FAIL E2 long-key cardinality")
long_key = long_keys[0]

rows = []
for line in cases_bytes.decode("utf-8", "strict").splitlines():
    columns = [part.strip() for part in line.split(" | ")]
    if len(columns) == 3 and columns[0] and all(
        character.isupper() or character.isdigit() or character == "_" for character in columns[0]
    ):
        rows.append(columns)
if len(rows) != 18:
    raise SystemExit(f"FAIL E2 expected 18 rows, got {len(rows)}")

for kind, path, expected in rows:
    if kind == "INPUT_BYTES":
        actual = str(len(input_bytes))
    elif kind == "ROOT_KEYS":
        actual = ",".join(root.keys())
    elif kind == "UNKNOWN_KEYS":
        actual = ",".join("longKey" if key == long_key else key for key in unknown)
    elif kind == "NESTED_KEYS":
        actual = ",".join(unknown["nested"].keys())
    else:
        name = path.rsplit(".", 1)[-1]
        if kind == "NUMBER_TOKEN":
            value = unknown[name]
            actual = str(value) if isinstance(value, NumberToken) else "NOT_A_NUMBER_TOKEN"
        elif kind == "NULL":
            actual = "present" if name in unknown and unknown[name] is None else "missing-or-nonnull"
        elif kind == "EMPTY_STRING":
            actual = "present" if name in unknown and unknown[name] == "" else "missing-or-nonempty"
        elif kind == "EMPTY_LIST":
            actual = "present" if name in unknown and unknown[name] == [] else "missing-or-nonempty"
        elif kind == "EMPTY_RECORD":
            actual = "present" if name in unknown and unknown[name] == {} else "missing-or-nonempty"
        elif kind == "ABSENT":
            actual = "true" if name not in unknown else "false"
        elif kind == "UTF16_HEX":
            actual = utf16_hex(unknown[name])
        elif kind == "LONG_KEY":
            actual = f"{len(long_key)},{unknown[long_key]}"
        else:
            raise SystemExit(f"FAIL E2 unknown requirement kind: {kind}")
    if actual != expected:
        raise SystemExit(f"FAIL E2 {kind} {path}: expected {expected}, observed {actual}")

print("PASS E2 2/2 exact-path SHA seals, 2/2 in-memory mutations rejected")
print(f"PASS E2 {len(rows)}/{len(rows)} independent typed-tree and UTF-16 expectations; 0 skipped")
