/** One form of a lexeme and the bundle of features that asks for it. */
export interface FormEntry { bundle: Record<string, string>; form: string }
/** The rows of the lexicon files (the records of course.data-contract as `biome read` gives them); `morphology` is the one row of morphology.core-data. */
export interface PlruLexicon { nouns: any[]; adjectives: any[]; verbs: any[]; possessives: any[]; morphology: any }
/** The rows of the English lexicon files: `pronouns` are personalPronouns.core-data and `prepositions` prepositions.core-data. */
export interface EnruLexicon { nouns: any[]; adjectives: any[]; verbs: any[]; possessives: any[]; pronouns: any[]; prepositions: any[] }
/** What `biome generate` passes to a derive: the running CLI, node and env, and the files named after OUT in the step. */
export interface DeriveContext { cli?: string; node?: string; env?: Record<string, string | undefined>; generateArgs?: string[] }
export const OUT_PLRU: string;
export const OUT_ENRU: string;
/** `{ 'noun:wife': [{ bundle, form }] }`, the lexemes in the order nouns, adjectives, possessives, verbs. */
export function plruForms(lexicon: PlruLexicon): Record<string, FormEntry[]>;
/** `{ 'noun:wife': [{ bundle, form }] }`, the lexemes in the order nouns, adjectives, possessives, pronouns, `neg:not`, prepositions, then each verb (with its `aux:` after it) and `aux:will`. */
export function enruForms(lexicon: EnruLexicon): Record<string, FormEntry[]>;
/** The rows of dsl/forms.data-contract as .core-data text, one for each lexeme. */
export function formRows(forms: Record<string, FormEntry[]>): string;
/** The step `forms-plru` of dsl/biome.project: the Kotlin text of the table; `generateArgs` are the five lexicon files. */
export function plru(context?: DeriveContext): Promise<string>;
/** The step `forms-enru`: the Kotlin text of the table; `generateArgs` are the six lexicon files. */
export function enru(context?: DeriveContext): Promise<string>;
