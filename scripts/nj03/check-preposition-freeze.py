#!/usr/bin/env python3
"""Read-only seal and coordinate check for the independent NJ03 preposition fixture.

This checks frozen inputs and expectations. It deliberately does not claim that a
Biome compiler, Kotlin printer, TableMorphology, or ConstructionRealizer ran.
"""

from __future__ import annotations

import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
CORPUS = ROOT / "Plans/Biome/NJ03PrepositionIndependent.cases"
CORPUS_SHA256 = "c7888ad050df4f1c96e74c06fd8fe4122cbd0568b118c8adea5deb8d176b1eb1"
EXPECTED_FORMS = (
    ("Subj", ""),
    ("Obj", ""),
    ("In", "in"),
    ("With", "with"),
    ("To", "to"),
    ("About", "about"),
    ("Of", "of"),
)
FIXTURE = "tests/fixtures/nj03-preposition/"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def verify_seal(data: bytes, expected: str, label: str) -> None:
    require(digest(data) == expected, f"stale/tampered {label}: SHA-256 differs")


def sealed(relative: str, expected: str) -> bytes:
    path = ROOT / relative
    data = path.read_bytes()
    verify_seal(data, expected, relative)
    require(b"\r" not in data and data.endswith(b"\n"), f"{relative}: expected UTF-8 LF")
    data.decode("utf-8", errors="strict")
    return data


def rows(data: bytes) -> dict[str, list[list[str]]]:
    parsed: dict[str, list[list[str]]] = {}
    for line in data.decode("utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        fields = line.split(" | ")
        parsed.setdefault(fields[0], []).append(fields[1:])
    return parsed


def one(table: dict[str, list[list[str]]], tag: str) -> list[str]:
    found = table.get(tag, [])
    require(len(found) == 1, f"{tag}: expected exactly one row")
    return found[0]


def u16_column(line: str, offset: int) -> int:
    return len(line[:offset].encode("utf-16-le")) // 2 + 1


def negative_expectations(positive: str) -> dict[str, tuple[str, str, str, str, str]]:
    def replace(old: str, new: str) -> str:
        require(positive.count(old) == 1, f"ambiguous source mutation {old!r}")
        return positive.replace(old, new)

    return {
        "missing-subj": (replace('Subj: "" ', ""), "E_CASE_MISSING", "$/forms/Subj", "{", "record"),
        "missing-obj": (replace('Obj: "" ', ""), "E_CASE_MISSING", "$/forms/Obj", "{", "record"),
        "duplicate-in": (replace("In: in With:", "In: in In: inside With:"), "E_DUP_KEY", "$/forms/In", "In", "second"),
        "unknown-case": (replace("Of: of", "Of: of Onto: onto"), "E_CASE_UNKNOWN", "$/forms/Onto", "Onto", "first"),
        "lowercase-case": (replace("In: in With:", "In: in in: inside With:"), "E_CASE_UNKNOWN", "$/forms/in", "in", "second"),
        "wrong-type": (replace("In: in", "In: 7"), "E_TYPE", "$/forms/In", "7", "first"),
        "null-value": (replace('Subj: ""', "Subj: null"), "E_TYPE", "$/forms/Subj", "null", "first"),
        "wrong-key": (replace("Of: of}}", "Of: of} formz: ignored}"), "E_FIELD_UNKNOWN", "$/formz", "formz", "first"),
        "utf16-wrong-type": (
            replace('Subj: ""', 'Subj: "😀"').replace("In: in", "In: 7"),
            "E_TYPE", "$/forms/In", "7", "first",
        ),
    }


def check() -> None:
    corpus_bytes = sealed("Plans/Biome/NJ03PrepositionIndependent.cases", CORPUS_SHA256)
    table = rows(corpus_bytes)
    require(set(table) == {"HISTORICAL", "PROFILE", "SOURCE", "TREE", "LOOKUP", "LOOKUP_ERROR", "REALIZER", "REALIZER_ERROR", "NEGATIVE", "ARTIFACT"}, "unexpected/missing corpus section")

    historical_path, historical_sha = one(table, "HISTORICAL")
    profile_path, profile_sha = one(table, "PROFILE")
    source_path, source_sha = one(table, "SOURCE")
    require(historical_path == "courses/lang/en/prepositions.json", "wrong historical input")
    require(profile_path == "courses/lang/en/lang.json", "wrong historical profile")
    require(source_path == FIXTURE + "role.core-data", "wrong typed fixture")
    historical = json.loads(sealed(historical_path, historical_sha))  # old authored input, read only
    profile = json.loads(sealed(profile_path, profile_sha))  # old authored input, read only
    require(len(historical["prepositions"]) == 1, "old preposition row count changed")
    old = historical["prepositions"][0]
    require(old["id"] == "role" and tuple(old["forms"].items()) == EXPECTED_FORMS, "old authored preposition differs from independent contract")
    require(tuple(profile["case"]) == tuple(case for case, _ in EXPECTED_FORMS), "old Case profile differs")
    require("Case" in profile["usesFeatures"], "old Case profile disabled")

    positive_bytes = sealed(source_path, source_sha)
    positive = positive_bytes.decode("utf-8")
    require(positive == 'SURFACE preposition\nPREPOSITION {id: role forms: {Subj: "" Obj: "" In: in With: with To: to About: about Of: of}}\n', "typed source differs from frozen semantics")
    expected_tree = [["$", "record", "id,forms"], ["$/id", "text", "role"], ["$/forms", "record", ",".join(case for case, _ in EXPECTED_FORMS)]]
    expected_tree.extend([f"$/forms/{case}", "text", value or "<empty>"] for case, value in EXPECTED_FORMS)
    require(table["TREE"] == expected_tree, "typed-tree expectation differs")
    expected_lookups = [[f"{i:02}", "prep:role", f"Case={case}", value or "<empty>"] for i, (case, value) in enumerate(EXPECTED_FORMS, 1)]
    require(table["LOOKUP"] == expected_lookups, "lookup expectation differs")
    require(table["LOOKUP_ERROR"] == [
        ["prep:role", "Case=MissingCase", "no form"],
        ["prep:missing", "Case=In", "no form"],
        ["prep:Role", "Case=In", "no form"],
        ["prep:role", "Case=in", "no form"],
        ["prep:role", "<empty-bundle>", "no form"],
    ], "lookup failure expectations differ")
    require(table["REALIZER"] == [
        ["fixture.preposition", "Case=In", "text=in", "slot=prep", "span=0 until 2"],
        ["fixture.preposition", "Case=Obj", "text=<empty>", "slot=prep", "span=0 until 0"],
    ], "realizer expectation differs")
    require(table["REALIZER_ERROR"] == [["fixture.preposition", "Case=In", "missing lexical slot prep", "throws"]], "realizer failure expectation differs")

    negatives = negative_expectations(positive)
    require(len(table["NEGATIVE"]) == len(negatives), "negative case count differs")
    for fields in table["NEGATIVE"]:
        require(len(fields) == 8, "malformed NEGATIVE row")
        name, path, sha, code, tree_path, position, length, token = fields
        require(name in negatives and path == FIXTURE + name + ".core-data", f"unexpected negative source {name}")
        expected_source, expected_code, expected_path, expected_token, occurrence = negatives.pop(name)
        source = sealed(path, sha).decode("utf-8")
        require(source == expected_source, f"{name}: source differs from frozen one-fault mutation")
        require((code, tree_path, token) == (expected_code, expected_path, expected_token), f"{name}: diagnostic contract differs")
        lines = source.splitlines()
        require(len(lines) == 2, f"{name}: expected two physical lines")
        line = lines[1]
        if occurrence == "record":
            offset = line.index("forms: {") + len("forms: ")
        elif occurrence == "second":
            offset = line.index(token, line.index(token) + len(token))
        else:
            offset = line.index(token)
        actual_position = f"2:{u16_column(line, offset)}"
        require(position == actual_position and int(length) == len(token.encode("utf-16-le")) // 2, f"{name}: UTF-16 span differs; expected {actual_position}")
    require(not negatives, f"unlisted negative cases: {sorted(negatives)}")
    require(len(table["ARTIFACT"]) == 8, "artifact freshness obligations differ")

    # In-memory tamper controls: no temporary application format or generated output is written.
    for label, data, sha in (
        ("corpus", corpus_bytes, CORPUS_SHA256),
        ("historical", (ROOT / historical_path).read_bytes(), historical_sha),
        ("profile", (ROOT / profile_path).read_bytes(), profile_sha),
        ("positive", positive_bytes, source_sha),
        ("negative", (ROOT / (FIXTURE + "wrong-type.core-data")).read_bytes(), next(row[2] for row in table["NEGATIVE"] if row[0] == "wrong-type")),
    ):
        try:
            verify_seal(data + b"tamper", sha, label)
        except ValueError:
            pass
        else:
            raise ValueError(f"{label}: tamper escaped seal")
    print("PASS: 7 old/typed facts, 7 lookup expectations, 2 realizer expectations, 9 source negatives, 5 in-memory tamper controls")
    print("NOT RUN: Core parser/type validator, generated Kotlin freshness, Kotlin consumers, Gradle, hosts and P23")


if __name__ == "__main__":
    check()
