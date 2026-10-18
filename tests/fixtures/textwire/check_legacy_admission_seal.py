#!/usr/bin/env python3
"""Check the frozen legacy input ledgers and two deliberate freshness mutations."""

import hashlib
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path


HERE = Path(__file__).parent
NAMES = ("legacy-boundary.cases", "numeric-admission.cases")
SEAL = HERE / "legacy-admission.sha256"


def checked_seal(contents: dict[str, bytes], seal_text: str) -> bool:
    lines = seal_text.splitlines()
    if len(lines) != 2:
        return False
    entries: dict[str, str] = {}
    for line in lines:
        match = re.fullmatch(r"([0-9a-f]{64})  tests/fixtures/textwire/([a-z-]+\.cases)", line)
        if match is None or match.group(2) in entries:
            return False
        entries[match.group(2)] = match.group(1)
    return set(entries) == set(NAMES) and all(
        hashlib.sha256(contents[name]).hexdigest() == entries[name] for name in NAMES
    )


contents = {name: (HERE / name).read_bytes() for name in NAMES}
seal_text = SEAL.read_text(encoding="ascii")
if not checked_seal(contents, seal_text):
    raise SystemExit("FAIL original legacy admission ledger seal")
print("PASS 2/2 legacy admission ledger file hashes")

mutations = (
    ("legacy-boundary.cases", b"`[` * 128", b"`[` * 129", "check_legacy_boundary.py"),
    ("numeric-admission.cases", b"`[` * 256", b"`[` * 257", "check_numeric_admission.py"),
)
for name, before, after, checker in mutations:
    if contents[name].count(before) != 1:
        raise SystemExit(f"FAIL nonunique mutation seam in {name}")
    changed = dict(contents)
    changed[name] = changed[name].replace(before, after, 1)
    if checked_seal(changed, seal_text):
        raise SystemExit(f"FAIL seal admitted changed recipe in {name}")
    with tempfile.TemporaryDirectory(prefix="polski-legacy-admission-") as directory:
        for file_name, value in changed.items():
            (Path(directory) / file_name).write_bytes(value)
        environment = dict(os.environ, TEXTWIRE_FIXTURES_DIR=directory)
        result = subprocess.run(
            [sys.executable, str(HERE / checker)],
            env=environment,
            capture_output=True,
            text=True,
            check=False,
        )
        if result.returncode == 0:
            raise SystemExit(f"FAIL independent recipe checker admitted changed {name}")
    print(f"PASS recipe and seal reject changed {name}")

first = seal_text[0]
changed_seal = ("0" if first != "0" else "1") + seal_text[1:]
if checked_seal(contents, changed_seal):
    raise SystemExit("FAIL changed ledger digest admitted")
print("PASS 1/1 changed ledger digest rejected; 3/3 mutations rejected")
