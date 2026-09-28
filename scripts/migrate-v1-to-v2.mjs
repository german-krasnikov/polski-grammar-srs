// UniversalCorePlan.md §3.1/§6/§12 UC-12: splits `courses/pl-ru/course.json` (schema v1, one
// document) into the two schema-v2 layer files §3.1 doesn't already cover by UC-06/07/08
// (curriculum.json/realization.json/exercise-recipes.json/forms.generated.json exist already):
//   - courses/lang/pl/lexicon.json  — lemmas/forms/valency (nouns, adjectives, verbs, pronouns,
//     possessives, morphology, stem alternations): language-scoped, not pair-scoped.
//   - courses/pairs/pl-ru/pair.json — everything else (copy, per-skill content, reference/
//     training screens, vocabulary): pair-scoped, in the pl-ru pair's own Russian.
// `course.json` itself is NOT renamed or deleted (§6: "не переименовывается и не удаляется до
// подтверждённого побайтного паритета движка") — it stays the live source for Gradle's
// generateCoursePackSource and for src/data/course.ts; splitting the file and switching a
// runtime loader to read the split are independent steps (§6, this task is only the split).
//
// The split is purely mechanical (a fixed key partition, §3.1's own field list) — no field is
// renamed, reshaped or reinterpreted, so `reconstructCoursePack(splitCoursePack(course))` must
// recover a value deep-equal to the original `course` for any valid v1 pack. `--check` proves
// exactly that against the checked-in files, which is what "runtime pack loaded from v2 is
// byte-identical to v1" (UC-12's acceptance) means at the JSON level — §6 puts the real
// byte-identity requirement at the engine's *output* (golden fixtures), which this script does
// not touch.
//
// Usage: node scripts/migrate-v1-to-v2.mjs           (writes lexicon.json/pair.json)
//        node scripts/migrate-v1-to-v2.mjs --check   (fails if the checked-in split files are
//                                                      stale, or don't reconstruct course.json)
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { deepStrictEqual } from 'node:assert';

const root = new URL('../', import.meta.url);
const readJson = (relativePath) => JSON.parse(readFileSync(new URL(relativePath, root), 'utf8'));

/** courses/lang/<code>/lexicon.json's fields (UniversalCorePlan.md §3.1: "леммы + признаки + управление"). */
export const LEXICON_KEYS = ['nouns', 'adjectives', 'verbs', 'personalPronouns', 'possessives', 'morphology', 'stemAlternations'];

/** Splits one v1 course pack into `{ lexicon, pair }`; every v1 key lands in exactly one of the two. */
export function splitCoursePack(course) {
  const lexicon = { schemaVersion: 1 };
  const pair = {};
  for (const [key, value] of Object.entries(course)) {
    if (LEXICON_KEYS.includes(key)) lexicon[key] = value;
    else pair[key] = value;
  }
  const missing = LEXICON_KEYS.filter((key) => !(key in lexicon));
  if (missing.length > 0) throw new Error(`splitCoursePack: course pack is missing ${missing.join(', ')}`);
  return { lexicon, pair };
}

/** Inverse of [splitCoursePack]: merges the two v2 layer files back into one v1-shaped pack. */
export function reconstructCoursePack(lexicon, pair) {
  const { schemaVersion: _lexiconSchemaVersion, ...lexiconFields } = lexicon;
  return { ...pair, ...lexiconFields };
}

const CHECK = process.argv.includes('--check');
const lexiconPath = fileURLToPath(new URL('courses/lang/pl/lexicon.json', root));
const pairPath = fileURLToPath(new URL('courses/pairs/pl-ru/pair.json', root));

function run() {
  const course = readJson('courses/pl-ru/course.json');
  const { lexicon, pair } = splitCoursePack(course);

  if (CHECK) {
    const currentLexicon = JSON.parse(readFileSync(lexiconPath, 'utf8'));
    const currentPair = JSON.parse(readFileSync(pairPath, 'utf8'));
    deepStrictEqual(currentLexicon, lexicon, 'courses/lang/pl/lexicon.json is stale — rerun node scripts/migrate-v1-to-v2.mjs');
    deepStrictEqual(currentPair, pair, 'courses/pairs/pl-ru/pair.json is stale — rerun node scripts/migrate-v1-to-v2.mjs');
    const reconstructed = reconstructCoursePack(currentLexicon, currentPair);
    deepStrictEqual(reconstructed, course, 'reconstructCoursePack(lexicon, pair) does not recover courses/pl-ru/course.json');
    process.stdout.write('PASS migrate v1->v2: split files match course.json and round-trip\n');
    return;
  }

  writeFileSync(lexiconPath, `${JSON.stringify(lexicon, null, 2)}\n`);
  writeFileSync(pairPath, `${JSON.stringify(pair, null, 2)}\n`);
  process.stdout.write(`wrote ${lexiconPath}\nwrote ${pairPath}\n`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    run();
  } catch (error) {
    process.stderr.write(`FAIL migrate v1->v2: ${error.message}\n`);
    process.exitCode = 1;
  }
}
