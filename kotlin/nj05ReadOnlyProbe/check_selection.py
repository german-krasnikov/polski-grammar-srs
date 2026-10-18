#!/usr/bin/env python3
"""Static, independent selection audit; it never launches Gradle or application code."""

from pathlib import Path
import argparse
import hashlib
import re


ROOT = Path(__file__).resolve().parents[2]
MODULE = ROOT / "kotlin/nj05ReadOnlyProbe"
SOURCE_DIR = ROOT / "kotlin/shared/src/commonTest/kotlin/polski/textwire"
EXPECTED_CLASSES = {
    "LegacyRepresentabilityProbeTest": 1,
    "RetainedNumberPrototypeTest": 4,
    "RetainedNumberIndependentMutationTest": 1,
}
FORBIDDEN_CALL = re.compile(
    r"\b(?:encodeLegacyV1|importJson|exportJson|saveRaw|migrateLegacy|backupJson)\s*\("
    r"|\.\s*(?:encodeToString|stringify|serialize|save|put|remove)\s*\("
)
TEST_ANNOTATION = re.compile(r"^\s*@Test\s*$", re.M)
TOP_LEVEL_CLASS = re.compile(r"^class\s+([A-Za-z][A-Za-z0-9]*)\b", re.M)


def validate(rows: list[list[str]], read_source) -> None:
    if len(rows) != 5 or len({row[1] for row in rows}) != 5:
        raise AssertionError("Selection must contain five distinct source paths")
    classes = {}
    supports = 0
    for row in rows:
        if len(row) not in (3, 5) or row[0] not in ("support", "test"):
            raise AssertionError(f"Invalid selection row: {row}")
        path = ROOT / row[1]
        if path.parent != SOURCE_DIR or not path.is_file():
            raise AssertionError(f"Source escaped exact textwire directory: {path}")
        data = read_source(path)
        expected_hash = row[-1]
        if not re.fullmatch(r"[0-9a-f]{64}", expected_hash) or hashlib.sha256(data).hexdigest() != expected_hash:
            raise AssertionError(f"Reviewed NJ05 source identity drift: {path}")
        source = data.decode("utf-8", "strict")
        if FORBIDDEN_CALL.search(source):
            raise AssertionError(f"Old JSON writer/import/save in: {path}")
        count = len(TEST_ANNOTATION.findall(source))
        if row[0] == "support":
            if len(row) != 3 or count:
                raise AssertionError(f"Support source contains tests: {path}")
            supports += 1
        else:
            if len(row) != 5 or count != int(row[3]):
                raise AssertionError(f"Unexpected test count: {path}")
            if TOP_LEVEL_CLASS.findall(source) != [row[2]]:
                raise AssertionError(f"Unexpected test class: {path}")
            classes[row[2]] = count
    if classes != EXPECTED_CLASSES or supports != 2:
        raise AssertionError(f"Read-only class selection drift: {classes}")


rows = [
    line.split()
    for line in (MODULE / "read-only-sources.list").read_text().splitlines()
    if line and not line.startswith("#")
]
validate(rows, Path.read_bytes)
generated_rows = [line.split("  ") for line in (MODULE / "generated-sources.sha256").read_text().splitlines()]
if len(generated_rows) != 2 or {row[1] for row in generated_rows} != {
    "LegacyAdmissionLedger.kt", "LegacyRepresentabilityFixture.kt",
} or any(len(row) != 2 or not re.fullmatch(r"[0-9a-f]{64}", row[0]) for row in generated_rows):
    raise AssertionError("Generated fixture source seal path/hash set changed")
assert 'include(":nj05ReadOnlyProbe")' in (ROOT / "kotlin/settings.gradle.kts").read_text()
build = (MODULE / "build.gradle.kts").read_text()
for fragment in (
    "from(sourceFiles)",
    'implementation(project(":shared"))',
    "kotlin.srcDir(prepareNj05ReadOnlyTestSources.map { it.outputs.files.singleFile })",
    "dependsOn(verifyNj05ReadOnlySelection)",
    "generateLegacyAdmissionTestSource",
    "generateRepresentabilityTestSource",
    "generatedSourceSeal",
    "sha256(copied) == source.sha256",
    "sha256(bytes) == expectedHash",
    "Reviewed NJ05 generated test source changed before copy",
):
    if fragment not in build:
        raise AssertionError(f"Missing isolated Gradle edge: {fragment}")
print("PASS exact selected scope: 3 read-only classes, 6 tests, 2 helpers, 2 pinned generated fixtures")

parser = argparse.ArgumentParser()
parser.add_argument("--generated-root", type=Path, help="existing shared/build/generated tree to audit read-only")
options = parser.parse_args()
if options.generated_root is not None:
    locations = {
        "LegacyAdmissionLedger.kt": "legacyAdmissionTest/kotlin/polski/textwire/LegacyAdmissionLedger.kt",
        "LegacyRepresentabilityFixture.kt": "representabilityTest/kotlin/polski/textwire/LegacyRepresentabilityFixture.kt",
    }
    for digest, name in generated_rows:
        data = (options.generated_root / locations[name]).read_bytes()
        if hashlib.sha256(data).hexdigest() != digest:
            raise AssertionError(f"Generated source differs before copy: {name}")
        if hashlib.sha256(data + b"X").hexdigest() == digest:
            raise AssertionError(f"Mutated generated source admitted before copy: {name}")
    print("PASS pre-copy generated fixture hashes 2/2; byte mutations rejected 2/2")

bad_class = rows + [[
    "test",
    "kotlin/shared/src/commonTest/kotlin/polski/textwire/LegacyBoundaryAdmissionTest.kt",
    "LegacyBoundaryAdmissionTest",
    "4",
    "0" * 64,
]]
for label, candidate, reader, expected_error in (
    ("extra class", bad_class, Path.read_bytes, "Selection must contain five"),
    ("fully qualified extra @Test", rows,
     lambda path: Path.read_bytes(path) + b"\n@kotlin.test.Test fun addedUnreviewedTest() {}\n",
     "Reviewed NJ05 source identity drift"),
    ("callable old-writer reference", rows,
     lambda path: Path.read_bytes(path) + b"\nval writer = ProgressCodec::encodeLegacyV1; writer(document)\n",
     "Reviewed NJ05 source identity drift"),
):
    try:
        validate(candidate, reader)
    except AssertionError as error:
        if expected_error not in str(error):
            raise
        print(f"PASS negative selection guard rejects {label}")
    else:
        raise AssertionError(f"Selection guard admitted {label}")
