#!/usr/bin/env python3
"""Independent A/C byte-count arithmetic; deliberately no wire printer or codec call."""


def digits(number: int) -> int:
    return len(str(number))


def row(arm: str, tag: str, key: str | None, payload_units: int, payload_bytes: int) -> int:
    if key is None:
        key_bytes = 1  # root/list child sentinel
    elif arm == "A":
        key_bytes = 2 + len(key.encode("utf-8"))  # K: plus unescaped ASCII name
    else:
        key_bytes = 3 + digits(len(key)) + 4 * len(key)  # K + U<count>: + uppercase units
    if tag == "S" and arm == "C":
        payload = 2 + digits(payload_units) + 4 * payload_units  # U<count>: + unit groups
    else:
        payload = payload_bytes
    return 1 + 1 + key_bytes + 1 + payload + 1  # tag TAB key TAB payload LF


def size(arm: str, many: bool) -> int:
    header = 25 if arm == "A" else 26
    digest = 74
    base = (
        row(arm, "M", None, 0, 1)
        + row(arm, "I", "version", 0, 1)
        + row(arm, "L", "cards", 0, 1)
        + row(arm, "M", "stats", 0, 1)
        + row(arm, "I", "reviewsToday", 0, 1)
        + row(arm, "S", "lastDay", 10, 10)
        + row(arm, "I", "totalReviews", 0, 1)
    )
    if many:
        # Root has eight children; 32768 array items each own a one-field map.
        base += row(arm, "L", "unknown", 0, 5)
        base += sum(
            row(arm, "M", None, 0, 1) + row(arm, "I", "i", 0, digits(i))
            for i in range(32_768)
        )
        base += row(arm, "S", "padding", 9_617_777, 9_617_777)
    else:
        base += row(arm, "S", "unknown", 9_999_895, 9_999_895)
    return header + base + digest


# The root child counts use one byte; the 32768 list child count uses five.
for arm, expected_single, expected_many in (
    ("A", 10_000_112, 10_196_726),
    ("C", 40_000_022, 39_214_145),
):
    single = size(arm, False)
    many = size(arm, True)
    if (single, many) != (expected_single, expected_many):
        raise SystemExit(f"FAIL {arm}: {(single, many)}")
    print(f"PASS {arm}: single {single} bytes; 32768 rows {many} bytes")
print("PASS 4 independent size checks; no encoder invoked")
