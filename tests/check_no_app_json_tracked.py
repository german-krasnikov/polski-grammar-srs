#!/usr/bin/env python3
"""Adversarial, temporary-repository checks for the NJ01 path gate."""

from __future__ import annotations

import contextlib
import hashlib
import importlib.util
import io
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock


SCRIPT = Path(__file__).resolve().parents[1] / "scripts/check-no-app-json-tracked.py"
SPEC = importlib.util.spec_from_file_location("no_app_json_gate", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
GATE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(GATE)


class PathGateTest(unittest.TestCase):
    def setUp(self) -> None:
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.git("init", "-q")

    def git(self, *args: str) -> None:
        subprocess.run(["git", *args], cwd=self.root, check=True, stdout=subprocess.PIPE)

    def run_gate(self, legacy: dict[str, str] | None = None) -> tuple[int, str]:
        output = io.StringIO()
        with (
            mock.patch.object(GATE, "ROOT", self.root),
            mock.patch.object(GATE, "LEGACY_INPUT_HASHES", legacy or {}),
            mock.patch.object(sys, "argv", [str(SCRIPT), "--summary"]),
            contextlib.redirect_stdout(output),
        ):
            result = GATE.main()
        return result, output.getvalue()

    def test_case_variants_are_rejected(self) -> None:
        (self.root / "extra.JSON").write_bytes(b"{}")
        (self.root / "other.Json").write_bytes(b"{}")
        result, output = self.run_gate()
        self.assertEqual(result, 1)
        self.assertIn("violations=2", output)
        self.git("add", "extra.JSON")
        result, output = self.run_gate()
        self.assertEqual(result, 1)
        self.assertIn("violations=2", output)

    def test_only_exact_tool_path_is_allowed(self) -> None:
        (self.root / "package.json").write_bytes(b"{}")
        self.assertEqual(self.run_gate()[0], 0)
        nested = self.root / "nested"
        nested.mkdir()
        (nested / "package.json").write_bytes(b"{}")
        result, output = self.run_gate()
        self.assertEqual(result, 1)
        self.assertIn("tool=1", output)
        self.assertIn("violations=1", output)

    def test_legacy_requires_matching_regular_index_and_worktree(self) -> None:
        path = "legacy/user.json"
        candidate = self.root / path
        candidate.parent.mkdir()
        accepted = b'{"old":1}'
        expected = hashlib.sha256(accepted).hexdigest()
        candidate.write_bytes(accepted)
        self.assertEqual(self.run_gate({path: expected})[0], 1, "untracked is not immutable")
        self.git("add", path)
        self.assertEqual(self.run_gate({path: expected})[0], 0)

        candidate.write_bytes(b'{"old":2}')
        self.git("add", path)
        candidate.write_bytes(accepted)
        self.assertEqual(self.run_gate({path: expected})[0], 1, "index bytes differ")

        self.git("add", path)
        outside = self.root / "external.txt"
        outside.write_bytes(accepted)
        candidate.unlink()
        candidate.symlink_to(outside)
        self.assertEqual(self.run_gate({path: expected})[0], 1, "symlink is not a frozen input")


if __name__ == "__main__":
    unittest.main()
