import rawCourse from '../../courses/pl-ru/course.json';
import type { Adjective, Gender, GramCase, Noun, NumberGram, Person, PossessiveId, Skill, Verb } from '../types';

export type PossessiveForms =
  | { readonly kind: 'invariant'; readonly value: string }
  | { readonly kind: 'declined'; readonly sg: Readonly<Record<Gender, Readonly<Record<GramCase, string>>>>;
      readonly pl: Readonly<Record<'m-personal' | 'other', Readonly<Record<GramCase, string>>>> };
export interface CoursePossessive {
  readonly id: PossessiveId;
  readonly label: string;
  readonly forms: PossessiveForms;
}
export interface FutureAuxiliary {
  readonly verbId: string;
  readonly forms: Readonly<Record<NumberGram, Readonly<Record<Person, string>>>>;
}

function validateMorphology() {
  const cases: GramCase[] = ['nom', 'gen', 'dat', 'acc', 'inst', 'loc', 'voc'];
  const genders: Gender[] = ['m-personal', 'm-animate', 'm-inanimate', 'f', 'n'];
  const requiredText = (value: unknown): value is string => typeof value === 'string' && value.trim().length > 0;
  const owners = rawCourse.possessives as unknown as CoursePossessive[];
  const expected = new Map<PossessiveId, PossessiveForms['kind']>([
    ['my', 'declined'], ['your', 'declined'], ['his', 'invariant'], ['her', 'invariant'],
    ['our', 'declined'], ['yourPlural', 'declined'], ['their', 'invariant'],
  ]);
  if (owners.length !== expected.size || new Set(owners.map(owner => owner.id)).size !== expected.size) throw new Error('Invalid possessive IDs');
  for (const owner of owners) {
    if (owner.forms?.kind !== expected.get(owner.id)) throw new Error(`Invalid possessive forms ${owner.id}`);
    if (owner.forms.kind === 'invariant') {
      if (!requiredText(owner.forms.value)) throw new Error(`Invalid possessive forms ${owner.id}`);
    } else {
      for (const gender of genders) for (const gramCase of cases)
        if (!requiredText(owner.forms.sg?.[gender]?.[gramCase])) throw new Error(`Invalid possessive forms ${owner.id}`);
      for (const group of ['m-personal', 'other'] as const) for (const gramCase of cases)
        if (!requiredText(owner.forms.pl?.[group]?.[gramCase])) throw new Error(`Invalid possessive forms ${owner.id}`);
    }
  }
  const auxiliary = rawCourse.morphology.futureAuxiliary as FutureAuxiliary;
  if (!rawCourse.verbs.some(verb => verb.id === auxiliary.verbId && verb.aspect === 'imperfective')) throw new Error('Invalid future auxiliary verb');
  for (const number of ['sg', 'pl'] as const) for (const person of [1, 2, 3] as const)
    if (!requiredText(auxiliary.forms?.[number]?.[person])) throw new Error('Invalid future auxiliary forms');
  return {owners, auxiliary};
}

/** The shipped Polish/Russian content is authored once for React and Kotlin hosts. */
function validateCourse() {
  if (rawCourse.schemaVersion !== 1 || rawCourse.id !== 'pl-ru' ||
      rawCourse.targetLanguage !== 'pl' || rawCourse.nativeLanguage !== 'ru') {
    throw new Error('Unsupported course pack');
  }
  for (const key of ['nouns', 'adjectives', 'verbs', 'skills'] as const) {
    const entries = rawCourse[key];
    if (!entries.length || new Set(entries.map(item => item.id)).size !== entries.length) {
      throw new Error(`Invalid IDs in ${key}`);
    }
  }
  return rawCourse;
}

const course = validateCourse();
const morphology = validateMorphology();
export const courseNouns = course.nouns as Noun[];
export const courseAdjectives = course.adjectives as Adjective[];
export const courseVerbs = course.verbs as Verb[];
export const courseSkills: Skill[] = course.skills.map(skill => ({
  id: skill.id, title: skill.title, group: skill.group, level: skill.level as Skill['level'],
  formula: skill.formula, theory: skill.theory, hint: skill.hint,
  prerequisites: skill.prerequisites,
}));
export const coursePersonalPronouns = course.personalPronouns;
export const coursePossessives: ReadonlyArray<CoursePossessive> = morphology.owners;
export const courseFutureAuxiliary: FutureAuxiliary = morphology.auxiliary;
export const courseVocabulary = course.vocabulary.items;
/** Host-specific course guidance; exact text is authored in the shared pack. */
export interface CourseVocabularyInstructions {
  readonly react: string;
  readonly web: string;
  readonly native: string;
  readonly ios: string;
}
export const courseVocabularyInstructions: CourseVocabularyInstructions = course.vocabulary.instructions;
export const courseVocabularyUnavailableLabel: string = course.vocabulary.unavailableLabel;
export const courseSentenceSeeds = course.sentenceSeeds;
export const courseCaseSentencePrefixes = course.caseSentencePrefixes;
export const courseExerciseCopy = course.exerciseCopy;
export interface ChainPresentation {
  readonly steps: ReadonlyArray<{ readonly id: string; readonly label: string }>;
  readonly completion: {
    readonly title: string;
    readonly reactEyebrow: string;
    readonly reactBody: string;
    readonly webBody: string;
  };
}
export const courseChainPresentation: ChainPresentation = course.training.chainPresentation;
export const courseCaseRows: { id: GramCase; pl: string; ru: string; question: string; trigger: string; skill?: string }[] =
  course.reference.caseRows.map(row => ({ ...row, id: row.id as GramCase }));
export const courseGenderNames: Record<Gender, string> = course.reference.genderNames;
export const courseReferenceChain = course.reference.chainRows;
export const courseReferenceSystemCards = course.reference.systemCards;
export const courseReferencePipeline = course.reference.pipeline;
export const courseRussianSupport = course.reference.russianSupport;
export const courseCaseTeaching = course.reference.caseTeaching;
export const courseVerbTeaching = course.reference.verbTeaching;
export const coursePronounTeaching = course.reference.pronounTeaching;
export function coursePronounContextValue(pronounId: string, context: typeof coursePronounTeaching.contexts[number]): string {
  const override = 'specialValues' in context ? context.specialValues?.ja : undefined;
  if (pronounId === 'ja' && override) return override;
  const forms = (coursePersonalPronouns as Record<string, Record<GramCase, string>>)[pronounId];
  if (!forms) throw new Error(`Unknown pronoun ${pronounId}`);
  return `${'valuePrefix' in context ? context.valuePrefix : ''}${forms[context.caseId as GramCase]}`;
}
export const courseComparisonNounIds = course.reference.comparisonNounIds;
export const courseReferencePipelineSummary = (): string =>
  courseReferencePipeline.steps.map(step => step.question).join(' → ');
export const courseReferenceTenses = course.reference.tenseRows;
export const courseReferenceAspects = course.reference.aspectRows;
export const courseMatrixIntroduction: string = course.reference.matrixIntroduction;
export const courseContextHelp: Readonly<{react: string; compact: string}> = course.reference.contextHelp;
export const courseMaleAccIntro: string = course.reference.maleAccIntro;
export const courseAspectNoPresent: Readonly<{compact: string; ios: string}> = course.reference.aspectNoPresent;
export const courseMaleAccRows = course.reference.maleAccRows;
export function renderCoursePattern(key: keyof typeof course.exercisePatterns, values: Record<string, string>): string {
  return course.exercisePatterns[key].replace(/\{([A-Za-z][A-Za-z0-9]*)\}/g, (_, name: string) => {
    const value = values[name];
    if (!value) throw new Error(`Missing ${name} for ${key}`);
    return value;
  });
}
export type ExplanationMethod = 'logic' | 'situations';
/** Presentation-style content (UC-10); every field is optional and derives from existing
 *  formula/theory/focus/method text when absent. React does not render styles (ADR-5) — this
 *  type only keeps the reader tolerant of the new course.json field. */
export interface SkillStyleContent {
  rule?: string;
  table?: { caption?: string; rows: { label: string; before: string; after: string }[] };
  scene?: string;
  nativeParallel?: { native: string; target: string; note: string; matches: boolean }[];
  examples?: string[];
  why?: string;
}
export interface SkillPresentation {
  focus: { before: string; after: string };
  methods: Record<ExplanationMethod, {
    introduction: string; promptLead: string;
    introduce: string; retrieve: string; feedback: string; review: string;
  }>;
  styleContent?: SkillStyleContent;
}
export const coursePresentations: Record<string, SkillPresentation> = Object.fromEntries(
  course.skills.map(skill => [skill.id, { focus: skill.focus, methods: skill.methods, styleContent: skill.styleContent }]),
);
