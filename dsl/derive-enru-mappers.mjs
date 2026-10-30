// Derive: the en-ru copies of the hand-written mappers that read records both packs share (R1.14b, R1.15, R1.16).
// The generated row classes of the two packs have one shape and two packages (polski.data.rows.plru, polski.data.rows.enru), so no Kotlin function reads both. The pl-ru file is the one
// source of the logic, tested on its own, and the English file is its copy with the package and the names changed: edit the pl-ru file, then run `biome generate`.
// It returns the text and writes nothing: `biome generate` writes OUT (generate) or compares it with the file (--check).
import { readFileSync } from 'node:fs';
import { basename, join, relative, resolve } from 'node:path';

const root = resolve(import.meta.dirname, '..');
const generated = join(root, 'kotlin/shared/src/commonMain/generated/kotlin/polski/data');
/** The file a pl-ru mapper is copied to: `PlRuSkillRows.kt` -> `EnRuSkillRows.kt`, in the generated source root. */
const target = (source) => join(generated, basename(source).replace('PlRu', 'EnRu'));
export const OUT_SKILL_ROWS = target('PlRuSkillRows.kt');
export const OUT_EXERCISE_TEXT_ROWS = target('PlRuExerciseTextRows.kt');
export const OUT_VOCABULARY_ROWS = target('PlRuVocabularyRows.kt');
export const OUT_SKILL_SPECS = target('PlRuSkillSpecs.kt');
export const OUT_RECIPE_ROWS = target('PlRuRecipeRows.kt');
export const OUT_FORM_TABLES = target('PlRuFormTables.kt');
export const OUT_LIFEHACK_ROWS = target('PlRuLifehackRows.kt');

/** `generateArgs[0]` is the pl-ru file of the step. */
export async function copy({ generateArgs: [source] = [] } = {}) {
  if (!source) throw new Error('derive-enru-mappers copy: the pl-ru file to copy');
  const text = readFileSync(resolve(source), 'utf8').replaceAll('polski.data.rows.plru', 'polski.data.rows.enru').replaceAll('PlRu', 'EnRu').replaceAll('pl-ru', 'en-ru');
  return `// Generated from ${relative(root, resolve(source))} by dsl/derive-enru-mappers.mjs: the en-ru copy of that file. Edit the pl-ru file, then run biome generate.\n${text}`;
}
