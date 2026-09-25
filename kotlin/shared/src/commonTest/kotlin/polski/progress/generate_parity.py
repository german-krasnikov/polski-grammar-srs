"""Generate direct Kotlin progress transitions from the pinned React parity fixture.

Run from the repository root:
    python3 kotlin/shared/src/commonTest/kotlin/polski/progress/generate_parity.py
"""

import json
from pathlib import Path

root = Path(__file__).resolve().parents[7]
cases = json.loads((root / "tests/fixtures/kotlin-parity/progress.json").read_text())["cases"]
out = Path(__file__).with_name("ProgressFixtureParityTest.kt")


def quoted(value):
    return json.dumps(value, ensure_ascii=False)


def raw_json(value):
    return quoted(json.dumps(value, ensure_ascii=False, separators=(",", ":")))


lines = [
    "package polski.progress",
    "",
    "import kotlin.test.Test",
    "import kotlin.test.assertEquals",
    "import kotlin.test.assertIs",
    "import kotlin.time.Instant",
    "import polski.srs.FsrsScheduler",
    "import polski.srs.Rating",
    "",
    "/** Generated from the pinned React progress oracle by generate_parity.py. */",
    "class ProgressFixtureParityTest {",
    "    private val scheduler = FsrsScheduler()",
    "    private fun expectedProgress(raw: String) = assertIs<DecodeResult.Valid>(ProgressCodec.decode(raw)).document.progress",
    "",
]

for case in cases:
    case_id = case["id"]
    method = case_id.replace("-", "_")
    if case_id.startswith("P-review-midnight-"):
        value = case["input"]
        expected = case["expected"]
        lines += [
            f"    @Test fun {method}() {{",
            f"        val original = assertIs<DecodeResult.Valid>(ProgressCodec.decode({raw_json(value['progress'])})).document",
            f"        val reducer = ReviewReducer(scheduler, setOf({quoted(value['skillId'])}))",
            f"        val first = assertIs<ReviewResult.Reviewed>(reducer.recordReview(original, {quoted(value['skillId'])}, Rating.Good, null, Instant.parse({quoted(value['firstNowIso'])}), {quoted(expected['first']['lastDay'])})).document",
            f"        assertEquals(expectedProgress({raw_json(expected['first'])}), first.progress, {quoted(case_id + ' first')})",
            f"        val second = assertIs<ReviewResult.Reviewed>(reducer.recordReview(first, {quoted(value['skillId'])}, Rating.Good, null, Instant.parse({quoted(value['secondNowIso'])}), {quoted(expected['second']['lastDay'])})).document",
            f"        assertEquals(expectedProgress({raw_json(expected['second'])}), second.progress, {quoted(case_id + ' second')})",
            f"        assertEquals(expectedProgress({raw_json(expected['initialAfter'])}), original.progress, {quoted(case_id + ' original unchanged')})",
            "    }",
            "",
        ]
    elif case_id in {"P-import-invalid-json", "P-import-missing-fields", "P-import-unsupported-version"}:
        result = "Unsupported" if case_id == "P-import-unsupported-version" else "Invalid"
        lines += [
            f"    @Test fun {method}() {{",
            f"        assertIs<DecodeResult.{result}>(ProgressCodec.decode({quoted(case['input']['raw'])}))",
            "    }",
            "",
        ]

lines.append("}")
out.write_text("\n".join(lines) + "\n")
print(f"Wrote {out} with 5 fixture cases")
