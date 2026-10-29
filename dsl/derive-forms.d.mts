/** One form of a lexeme and the bundle of features that asks for it. */
export interface FormEntry { bundle: Record<string, string>; form: string }
/** The rows of the lexicon files (the records of course.data-contract as `biome read` gives them); `morphology` is the one row of morphology.core-data. */
export interface PlruLexicon { nouns: any[]; adjectives: any[]; verbs: any[]; possessives: any[]; morphology: any }
export const OUT_PLRU: string;
/** `{ 'noun:wife': [{ bundle, form }] }`, the lexemes in the order nouns, adjectives, possessives, verbs. */
export function plruForms(lexicon: PlruLexicon): Record<string, FormEntry[]>;
/** The rows of dsl/forms.data-contract as .core-data text, one for each lexeme. */
export function formRows(forms: Record<string, FormEntry[]>): string;
/** The step `forms-plru` of dsl/biome.project: the Kotlin text of the table; `generateArgs` are the five lexicon files. */
export function plru(context?: { cli?: string; node?: string; env?: Record<string, string | undefined>; generateArgs?: string[] }): Promise<string>;
