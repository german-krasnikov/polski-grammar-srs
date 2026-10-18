#!/usr/bin/env python3
"""Verify input-only numeric legacy recipes without invoking a candidate wire codec."""

import hashlib
import re
from pathlib import Path


HERE = Path(__file__).parent
base = (HERE / "legacy-boundary.cases").read_text(encoding="utf-8")
ledger = (HERE / "numeric-admission.cases").read_text(encoding="utf-8")


def between(source: str, label: str) -> str:
    line = next(line for line in source.splitlines() if line.startswith(label))
    return re.findall(r"`([^`]*)`", line)[0]


def verify(label: str, raw: str) -> None:
    line = next(line for line in ledger.splitlines() if line.startswith(label))
    match = re.search(r"Size ([\d,]+); SHA-256 ([0-9a-f]{64})", line, re.IGNORECASE)
    if match is None:
        raise SystemExit(f"missing size/hash: {label}")
    expected_size = int(match.group(1).replace(",", ""))
    expected_hash = match.group(2)
    data = raw.encode("utf-8")
    actual_hash = hashlib.sha256(data).hexdigest()
    if len(data) != expected_size or actual_hash != expected_hash:
        raise SystemExit(f"FAIL {label}: {len(data)} bytes, {actual_hash}")
    print(f"PASS {label}: {len(data)} bytes, {actual_hash}")


progress = between(base, "Progress prefix P = ")
vocabulary = between(base, "Vocabulary prefix V = ")
preferences = between(base, "Preferences prefix F = ")
for label, prefix, suffix, limit, fill in (
    ("  PG-10000001:", progress, '"}', 10_000_001, "x"),
    ("  VO-10000001:", vocabulary, '"}', 10_000_001, "x"),
    ("  PF-1000001:", preferences, "}", 1_000_001, " "),
):
    raw = prefix + fill * (limit - len(prefix.encode()) - len(suffix.encode())) + suffix
    verify(label, raw)

verify("Progress depth-256 recipe:", progress[:-1] + "[" * 256 + "0" + "]" * 256 + "}")
rows = ",".join('{"i":' + str(i) + "}" for i in range(32_768))
prefix = progress[:-1] + "[" + rows + '],"padding":"'
raw = prefix + "x" * (10_000_000 - len(prefix.encode()) - 2) + '"}'
verify("Progress rows-32768-at-limit recipe:", raw)
verify("Progress UTF-16 recipe:", between(ledger, "Progress UTF-16 recipe:"))
print("PASS 6 selected / 6 checked / 0 skipped; legacy inputs only")
