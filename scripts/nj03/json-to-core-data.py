#!/usr/bin/env python3
"""One-off: an input-only historical JSON file -> .core-data rows (R1.8a; R1.14 reuses it for the en-ru files; R1.19 removes it).
The JSON is read from git at a revision, so the working tree may already be without it, and it must have the sealed sha256.
The rows are read back through the Core reader (`biome read`) and compared with the JSON: nothing is dropped, changed or reordered.

usage: json-to-core-data.py --rev REV --path PATH --sha256 HEX --out FILE [--comment KEY,..] [--one TAG[:KEY,..]] [--many TAG:KEY]
  --comment  top-level keys written as `# key: value` header lines (provenance, not data)
  --one      one row `TAG {key: value ..}` of the listed top-level keys (all of them when none is listed)
  --many     one row `TAG {..}` for each element of the array at the top-level KEY
The `--one` row comes first. BIOME_BIN names the biome CLI (default: the sibling checkout)."""
import argparse, hashlib, json, os, subprocess, sys
from pathlib import Path

BIOME = os.environ.get('BIOME_BIN') or str(Path(__file__).resolve().parents[4] / 'Python/biome-dsl/implementations/typescript/packages/biome/src/bin.mjs')


def text(value) -> str:
    """A value in the notation of Kit: records and lists as written in the data, a text in double quotes."""
    if isinstance(value, dict):
        return '{' + ' '.join(f'{k}: {text(v)}' for k, v in value.items()) + '}'
    if isinstance(value, list):
        return '[' + ' '.join(text(v) for v in value) + ']'
    if isinstance(value, str):
        return json.dumps(value, ensure_ascii=False)
    return 'null' if value is None else ('true' if value is True else 'false' if value is False else json.dumps(value))


def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument('--rev', required=True); ap.add_argument('--path', required=True); ap.add_argument('--sha256', required=True); ap.add_argument('--out', required=True)
    ap.add_argument('--comment', default=''); ap.add_argument('--one'); ap.add_argument('--many')
    a = ap.parse_args()
    raw = subprocess.run(['git', 'show', f'{a.rev}:{a.path}'], capture_output=True, check=True).stdout
    if hashlib.sha256(raw).hexdigest() != a.sha256:
        sys.exit(f'{a.path} at {a.rev} is not the sealed source (sha256 {hashlib.sha256(raw).hexdigest()})')
    src = json.loads(raw)
    rows: list[tuple[str, object]] = []
    if a.one:
        tag, _, keys = a.one.partition(':')
        rows.append((tag, {k: src[k] for k in keys.split(',')} if keys else src))
    if a.many:
        tag, key = a.many.split(':')
        rows += [(tag, item) for item in src[key]]
    head = [f'# {a.path} — source SHA-256: {a.sha256[:16]}...'] + [f'# {k}: {src[k]}' for k in a.comment.split(',') if k]
    Path(a.out).write_text('\n'.join(head + [f'{tag} {text(v)}' for tag, v in rows]) + '\n', encoding='utf-8')
    read = subprocess.run(['node', BIOME, 'read', a.out, '--format', 'json'], capture_output=True, text=True)
    if read.returncode != 0:
        sys.exit(read.stderr)
    back = [(r['tag'], r['value']) for r in json.loads(read.stdout)]
    if json.dumps(back, ensure_ascii=False) != json.dumps(rows, ensure_ascii=False):
        sys.exit('the rows read back through `biome read` differ from the JSON')
    print(f'{a.out}: {len(rows)} row(s); read back through the Core reader equal to {a.path}')


if __name__ == '__main__':
    main()
