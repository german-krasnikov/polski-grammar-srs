#!/usr/bin/env python3
"""Static, independent selection audit; it never launches Gradle or application code."""

from pathlib import Path
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
        if len(row) not in (2, 4) or row[0] not in ("support", "test"):
            raise AssertionError(f"Invalid selection row: {row}")
        path = ROOT / row[1]
        if path.parent != SOURCE_DIR or not path.is_file():
            raise AssertionError(f"Source escaped exact textwire directory: {path}")
        source = read_source(path)
        if FORBIDDEN_CALL.search(source):
            raise AssertionError(f"Old JSON writer/import/save in: {path}")
        count = len(TEST_ANNOTATION.findall(source))
        if row[0] == "support":
            if len(row) != 2 or count:
                raise AssertionError(f"Support source contains tests: {path}")
            supports += 1
        else:
            if len(row) != 4 or count != int(row[3]):
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
validate(rows, Path.read_text)
assert 'include(":nj05ReadOnlyProbe")' in (ROOT / "kotlin/settings.gradle.kts").read_text()
build = (MODULE / "build.gradle.kts").read_text()
for fragment in (
    "from(sourceFiles)",
    'implementation(project(":shared"))',
    "kotlin.srcDir(prepareNj05ReadOnlyTestSources.map { it.outputs.files.singleFile })",
    "dependsOn(verifyNj05ReadOnlySelection)",
    "generateLegacyAdmissionTestSource",
    "generateRepresentabilityTestSource",
):
    if fragment not in build:
        raise AssertionError(f"Missing isolated Gradle edge: {fragment}")
print("PASS exact selected scope: 3 read-only classes, 6 tests, 2 helpers, 2 generated sealed fixtures")

bad_class = rows + [[
    "test",
    "kotlin/shared/src/commonTest/kotlin/polski/textwire/LegacyBoundaryAdmissionTest.kt",
    "LegacyBoundaryAdmissionTest",
    "4",
]]
bad_call = lambda path: Path.read_text(path) + "\nfun forbidden() { ProgressCodec.encodeLegacyV1(doc) }\n"
for label, candidate, reader in (
    ("extra class", bad_class, Path.read_text),
    ("old writer call", rows, bad_call),
):
    try:
        validate(candidate, reader)
    except AssertionError:
        print(f"PASS negative selection guard rejects {label}")
    else:
        raise AssertionError(f"Selection guard admitted {label}")
