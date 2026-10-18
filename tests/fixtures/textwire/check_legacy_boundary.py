#!/usr/bin/env python3
"""Rebuild the input-only NJ05 recipes and verify every ledger byte hash."""

import hashlib
import re
from pathlib import Path


LEDGER = Path(__file__).with_name("legacy-boundary.cases")
text = LEDGER.read_text(encoding="utf-8")


def line_starting(prefix: str) -> str:
    matches = [line for line in text.splitlines() if line.startswith(prefix)]
    if len(matches) != 1:
        raise SystemExit(f"expected one ledger line starting {prefix!r}; got {len(matches)}")
    return matches[0]


def fragments(prefix: str) -> list[str]:
    return re.findall(r"`([^`]*)`", line_starting(prefix))


def check(case: str, recipe: str, size: int, digest: str) -> None:
    data = recipe.encode("utf-8")
    actual = hashlib.sha256(data).hexdigest()
    if len(data) != size or actual != digest:
        raise SystemExit(
            f"FAIL {case}: bytes {len(data)} expected {size}; sha256 {actual} expected {digest}"
        )
    print(f"PASS {case}: {size} bytes {actual}")


def listed_case(case: str) -> tuple[int, str]:
    line = line_starting(f"  {case}:")
    match = re.search(r"size ([\d,]+); SHA-256 ([0-9a-f]{64});", line)
    if match is None:
        raise SystemExit(f"missing size/hash for {case}")
    return int(match.group(1).replace(",", "")), match.group(2)


def inline_case(label: str) -> tuple[int, str]:
    line = line_starting(label)
    match = re.search(r"Size ([\d,]+); SHA-256 ([0-9a-f]{64});", line)
    if match is None:
        raise SystemExit(f"missing size/hash for {label}")
    return int(match.group(1).replace(",", "")), match.group(2)


progress = fragments("Progress prefix P = ")[0]
vocabulary = fragments("Vocabulary prefix V = ")[0]
preferences = fragments("Preferences prefix F = ")[0]
if (len(progress.encode()), len(vocabulary.encode()), len(preferences.encode())) != (103, 79, 18):
    raise SystemExit("legacy prefix lengths drifted")

checked = 0
for label, prefix, suffix, fill in (
    ("PG", progress, '"}', "x"),
    ("VO", vocabulary, '"}', "x"),
    ("PF", preferences, "}", " "),
):
    for listed_size in ((9_999_999, 10_000_000) if label != "PF" else (999_999, 1_000_000)):
        case = f"{label}-{listed_size}"
        size, digest = listed_case(case)
        if size != listed_size:
            raise SystemExit(f"FAIL {case}: ledger size disagrees with case ID")
        recipe = prefix + fill * (size - len(prefix.encode()) - len(suffix.encode())) + suffix
        check(case, recipe, size, digest)
        checked += 1

size, digest = inline_case("Progress nested recipe = ")
check("progress-depth-128", progress[:-1] + "[" * 128 + "0" + "]" * 128 + "}", size, digest)
checked += 1

size, digest = inline_case("Progress rows recipe = ")
rows = ",".join('{"i":' + str(i) + "}" for i in range(2048))
check("progress-rows-2048", progress[:-1] + "[" + rows + "]}", size, digest)
checked += 1

size, digest = inline_case("Progress Unicode literal = ")
check("progress-unicode", fragments("Progress Unicode literal = ")[0], size, digest)
checked += 1

size, digest = inline_case("Vocabulary domain text recipe: ")
parts = fragments("Vocabulary domain text recipe: ")
if len(parts) != 9 or parts[1::2] != ["ż"] * 4:
    raise SystemExit("vocabulary domain recipe fragments drifted")
domain_recipe = "".join(part + ("ż" * 240 if i < 4 else "") for i, part in enumerate(parts[::2]))
check("vocabulary-240", domain_recipe, size, digest)
checked += 1

if checked != 10:
    raise SystemExit(f"FAIL selected {checked} controls; expected 10")
print("PASS 10 selected / 10 checked / 0 skipped")
