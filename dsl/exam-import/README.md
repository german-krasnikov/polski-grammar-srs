# Exam data import pipeline

One-off converters that parse exam PDFs into `.core-data` rows checked against `dsl/exam.data-contract`.

## Workflow

1. Download the exam PDF + audio from the provider (e.g. certyfikatpolski.pl)
2. Run the import script:
   ```bash
   node dsl/exam-import/import-certyfikat.mjs \
     --pdf path/to/exam.pdf \
     --level B1 --year 2023 \
     --out exams/pl/certyfikat-polski/B1/2023/
   ```
3. Review the generated `.core-data` files (the script is a draft, not a finished product)
4. Run `biome fmt` on each generated file
5. Run `biome generate --check --project dsl/biome.project` to validate against the contract

## Audio timestamps

Each exam has one audio file. Listening tasks reference segments by `segmentStartMs`/`segmentEndMs` (milliseconds). Extract timestamps from the exam instructions (e.g. "Nagranie 1: 0:00–3:45" → startMs=0, endMs=225000).

Audio files are NOT committed — `audioUrl` points to the provider's hosted file or a private CDN.

## Converters

- `import-certyfikat.mjs` — Certyfikat Polski (B1/B2/C1/B1D), uses `pdf-parse`
- `import-ielts.mjs` — IELTS Academic/General (future)

Each converter is run once per exam year, by hand, and its output is reviewed before commit. These are NOT wired into `dsl/biome.project` as GENERATE steps.
