#!/usr/bin/env python3
"""Reject tracked or untracked application JSON paths in the product tree.

This is the path component of NJ01/NJ08. It deliberately does not certify file
contents, generated Kotlin strings, bundles, stores, exports, or test execution.
Those require separate checks before a no-JSON release can be accepted.
"""

from __future__ import annotations

import argparse
import hashlib
import stat
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]

# Exact tool-owned paths approved by the owner. A same-named file elsewhere is
# application data until its own tool requirement and contents are reviewed.
TOOL_PATHS = frozenset(
    {
        "package.json",
        "package-lock.json",
        "tsconfig.json",
        "tsconfig.app.json",
        "tsconfig.browser.json",
        "tsconfig.node.json",
        "kotlin/iosApp/PolskiGrammar/Assets.xcassets/AccentColor.colorset/Contents.json",
        "kotlin/iosApp/PolskiGrammar/Assets.xcassets/AppIcon.appiconset/Contents.json",
        "kotlin/iosApp/PolskiGrammar/Assets.xcassets/EmphasisAfter.colorset/Contents.json",
        "kotlin/iosApp/PolskiGrammar/Assets.xcassets/EmphasisBefore.colorset/Contents.json",
    }
)

# Test-only old-user inputs may be added only after independent exact-path and
# complete-byte SHA-256 review. None is accepted by this path gate yet.
LEGACY_INPUT_HASHES: dict[str, str] = {}


def matches_legacy_input(path: str) -> bool:
    """Admit only identical, regular, byte-pinned index and worktree inputs."""
    expected = LEGACY_INPUT_HASHES.get(path)
    if expected is None:
        return False

    candidate = ROOT / path
    for part in (candidate, *candidate.parents):
        if part == ROOT.parent:
            break
        try:
            if stat.S_ISLNK(part.lstat().st_mode):
                return False
        except OSError:
            return False
    try:
        if not stat.S_ISREG(candidate.lstat().st_mode):
            return False
        if hashlib.sha256(candidate.read_bytes()).hexdigest() != expected:
            return False
    except OSError:
        return False

    staged = subprocess.run(
        ["git", "ls-files", "--stage", "-z", "--", path],
        cwd=ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if staged.returncode:
        return False
    entries = [entry for entry in staged.stdout.split(b"\0") if entry]
    if len(entries) != 1:
        return False
    try:
        metadata, staged_path = entries[0].split(b"\t", 1)
        mode, _blob, stage = metadata.split(b" ", 2)
    except ValueError:
        return False
    if mode not in (b"100644", b"100755") or stage != b"0":
        return False
    if staged_path.decode("utf-8", "surrogateescape") != path:
        return False
    indexed_bytes = subprocess.run(
        ["git", "show", f":{path}"],
        cwd=ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    return indexed_bytes.returncode == 0 and hashlib.sha256(indexed_bytes.stdout).hexdigest() == expected


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--summary", action="store_true", help="print counts without listing violations")
    args = parser.parse_args()
    result = subprocess.run(
        ["git", "ls-files", "--cached", "--others", "--exclude-standard", "-z"],
        cwd=ROOT,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        check=False,
    )
    if result.returncode:
        sys.stderr.write(result.stderr.decode("utf-8", "replace"))
        return 2

    paths = sorted(
        {
            path
            for raw in result.stdout.split(b"\0")
            if raw
            for path in [raw.decode("utf-8", "surrogateescape")]
            if Path(path).suffix.casefold() == ".json"
        }
    )
    legacy_ok = {path for path in paths if matches_legacy_input(path)}
    violations = [path for path in paths if path not in TOOL_PATHS and path not in legacy_ok]
    if violations and not args.summary:
        for path in violations:
            print(f"application JSON path: {path}")
    print(
        f"NJ01 tracked/untracked path scan: paths={len(paths)} "
        f"tool={sum(path in TOOL_PATHS for path in paths)} "
        f"legacy-input={len(legacy_ok)} "
        f"violations={len(violations)}"
    )
    return 1 if violations else 0


if __name__ == "__main__":
    raise SystemExit(main())
