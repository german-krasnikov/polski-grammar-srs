import { describe, expect, test } from 'vitest';
import { validateCoursePack } from '../scripts/validate-course.mjs';
import course from '../courses/pl-ru/course.json';
import frequency from '../courses/pl-ru/frequency-top1000.json';

const copy = () => [structuredClone(course), structuredClone(frequency)] as const;

describe('authored pl-ru pack', () => {
  test('accepts the published source without changing it', () => {
    const [sample, ranks] = copy();
    expect(() => validateCoursePack(sample, ranks)).not.toThrow();
    expect(sample).toEqual(course);
    expect(ranks).toEqual(frequency);
  });

  const invalidCases: Array<[string, (sample: typeof course) => void, RegExp]> = [
    ['empty Polish form', (sample) => { sample.nouns[0].forms.sg.nom = ''; }, /nouns\/0\/forms\/sg\/nom/],
    ['duplicate skill ID', (sample) => { sample.skills[1].id = sample.skills[0].id; }, /skills\/1\/id/],
    ['unknown prerequisite', (sample) => { sample.skills[0].prerequisites = ['missing.skill']; }, /skills\/0\/prerequisites\/0/],
    ['prerequisite cycle', (sample) => { sample.skills[0].prerequisites = [sample.skills[1].id]; sample.skills[1].prerequisites = [sample.skills[0].id]; }, /prerequisite cycle/],
    ['missing translation', (sample) => { sample.vocabulary.items[0].translation = ''; }, /vocabulary\/items\/0\/translation/],
    ['HTML in a teaching field', (sample) => { sample.skills[0].theory = '<script>alert(1)</script>'; }, /skills\/0\/theory/],
    ['unsupported future conjugation', (sample) => { sample.verbs[0].futureType = 'irregular'; }, /verbs\/0\/futureType/],
    ['self stem alternation', (sample) => { sample.stemAlternations = [{ a: 'o', b: 'o' }]; }, /stemAlternations\/0: a and b must differ/],
    ['duplicate stem alternation (reversed)', (sample) => { sample.stemAlternations = [{ a: 'ó', b: 'o' }, { a: 'o', b: 'ó' }]; }, /stemAlternations\/1: duplicate alternation/],
    ['alternatives via " / " in a FormChange.to copy key', (sample) => { sample.exerciseCopy.aspectTo = 'kupiłem / kupiłam'; }, /exerciseCopy\/aspectTo/],
  ];
  test.each(invalidCases)('rejects %s', (_label, change, message) => {
    const [sample, ranks] = copy();
    change(sample);
    expect(() => validateCoursePack(sample, ranks)).toThrow(message);
  });

  test('rejects a frequency rank that names another lemma', () => {
    const [sample, ranks] = copy();
    sample.vocabulary.items[0].frequencyRank = 1;
    expect(() => validateCoursePack(sample, ranks)).toThrow(/vocabulary\/items\/0\/frequencyRank/);
  });

  test('rejects two selectable senses of one lemma', () => {
    const [sample, ranks] = copy();
    sample.vocabulary.items[1].lemma = sample.vocabulary.items[0].lemma;
    sample.vocabulary.items[1].frequencyRank = sample.vocabulary.items[0].frequencyRank;
    expect(() => validateCoursePack(sample, ranks)).toThrow(/vocabulary\/items\/1\/lemma/);
  });

  test('accepts an explicitly unassigned ready-card level', () => {
    const [sample, ranks] = copy();
    sample.vocabulary.items[0].level = '—';
    expect(() => validateCoursePack(sample, ranks)).not.toThrow();
  });

  test('rejects a sentence seed referencing an unknown noun', () => {
    const [sample, ranks] = copy();
    sample.sentenceSeeds[0].nounId = 'missing';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/sentenceSeeds\/0\/nounId/);
  });

  test('rejects duplicate sentence seeds', () => {
    const [sample, ranks] = copy();
    sample.sentenceSeeds[1] = structuredClone(sample.sentenceSeeds[0]);
    expect(() => validateCoursePack(sample, ranks)).toThrow(/sentenceSeeds\/1/);
  });

  test('rejects duplicate case-reference rows', () => {
    const [sample, ranks] = copy();
    sample.reference.caseRows[1].id = sample.reference.caseRows[0].id;
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/caseRows\/1\/id/);
  });

  test('rejects duplicate system-card IDs', () => {
    const [sample, ranks] = copy();
    sample.reference.systemCards[1].id = sample.reference.systemCards[0].id;
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/systemCards\/1\/id/);
  });

  test('rejects malformed verb teaching subjects, options and text', () => {
    const [missing, ranks] = copy();
    delete (missing.reference as Partial<typeof missing.reference>).verbTeaching;
    expect(() => validateCoursePack(missing, ranks)).toThrow(/reference\/verbTeaching/);
    const [reordered, reorderedRanks] = copy();
    [reordered.reference.verbTeaching.subjects[0], reordered.reference.verbTeaching.subjects[1]] =
      [reordered.reference.verbTeaching.subjects[1], reordered.reference.verbTeaching.subjects[0]];
    expect(() => validateCoursePack(reordered, reorderedRanks)).toThrow(/reference\/verbTeaching\/subjects\/0\/id/);
    const [wrongGender, genderRanks] = copy();
    wrongGender.reference.verbTeaching.subjects[0].genderMode = 'fixed';
    expect(() => validateCoursePack(wrongGender, genderRanks)).toThrow(/reference\/verbTeaching\/subjects\/0\/genderMode/);
    const [wrongFixed, fixedRanks] = copy();
    wrongFixed.reference.verbTeaching.subjects[2].fixedGender = 'f';
    expect(() => validateCoursePack(wrongFixed, fixedRanks)).toThrow(/reference\/verbTeaching\/subjects\/2\/fixedGender/);
    const [duplicate, duplicateRanks] = copy();
    duplicate.reference.verbTeaching.genderOptions[1].id = 'm';
    expect(() => validateCoursePack(duplicate, duplicateRanks)).toThrow(/reference\/verbTeaching\/genderOptions\/1\/id/);
    const [blank, blankRanks] = copy();
    blank.reference.verbTeaching.futureExplanation.compact = '';
    expect(() => validateCoursePack(blank, blankRanks)).toThrow(/reference\/verbTeaching\/futureExplanation\/compact/);
  });

  test('rejects reordered system cards', () => {
    const [sample, ranks] = copy();
    [sample.reference.systemCards[0], sample.reference.systemCards[1]] =
      [sample.reference.systemCards[1], sample.reference.systemCards[0]];
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/systemCards\/0\/id/);
  });

  test('rejects missing Russian-support rows and variants', () => {
    const [short, ranks] = copy();
    short.reference.russianSupport.rows.pop();
    expect(() => validateCoursePack(short, ranks)).toThrow(/reference\/russianSupport\/rows/);
    const [missingVariant, variantRanks] = copy();
    delete (missingVariant.reference.russianSupport.rows[0] as Partial<typeof missingVariant.reference.russianSupport.rows[0]>).desktop;
    expect(() => validateCoursePack(missingVariant, variantRanks)).toThrow(/reference\/russianSupport\/rows\/0\/desktop/);
  });

  test('rejects reordered or duplicate Russian-support IDs', () => {
    const [reordered, ranks] = copy();
    [reordered.reference.russianSupport.rows[0], reordered.reference.russianSupport.rows[1]] =
      [reordered.reference.russianSupport.rows[1], reordered.reference.russianSupport.rows[0]];
    expect(() => validateCoursePack(reordered, ranks)).toThrow(/reference\/russianSupport\/rows\/0\/id/);
    const [duplicate, duplicateRanks] = copy();
    duplicate.reference.russianSupport.rows[1].id = duplicate.reference.russianSupport.rows[0].id;
    expect(() => validateCoursePack(duplicate, duplicateRanks)).toThrow(/reference\/russianSupport\/rows\/1\/id/);
  });

  test('rejects blank Russian-support text and a mismatched mobile cue', () => {
    const [blank, ranks] = copy();
    blank.reference.russianSupport.rows[0].web.check = '';
    expect(() => validateCoursePack(blank, ranks)).toThrow(/reference\/russianSupport\/rows\/0\/web\/check/);
    const [wrongCue, cueRanks] = copy();
    wrongCue.reference.russianSupport.rows[2].mobileLine = 'inny tekst → mówię o żonie.';
    expect(() => validateCoursePack(wrongCue, cueRanks)).toThrow(/reference\/russianSupport\/rows\/2\/mobileLine/);
  });

  test('rejects missing, blank and wrongly typed case teaching text', () => {
    const [missing, ranks] = copy();
    delete (missing.reference as Partial<typeof missing.reference>).caseTeaching;
    expect(() => validateCoursePack(missing, ranks)).toThrow(/reference\/caseTeaching/);
    const [blank, blankRanks] = copy();
    blank.reference.caseTeaching.caseNote.react = '';
    expect(() => validateCoursePack(blank, blankRanks)).toThrow(/reference\/caseTeaching\/caseNote\/react/);
    const [wrongType, typeRanks] = copy();
    (wrongType.reference.caseTeaching as unknown as { comparisonReadingHint: unknown }).comparisonReadingHint = 7;
    expect(() => validateCoursePack(wrongType, typeRanks)).toThrow(/reference\/caseTeaching\/comparisonReadingHint/);
  });

  test('rejects missing, duplicate and unknown comparison nouns', () => {
    const [missing, ranks] = copy();
    missing.reference.comparisonNounIds.pop();
    expect(() => validateCoursePack(missing, ranks)).toThrow(/reference\/comparisonNounIds/);
    const [duplicate, duplicateRanks] = copy();
    duplicate.reference.comparisonNounIds[2] = duplicate.reference.comparisonNounIds[1];
    expect(() => validateCoursePack(duplicate, duplicateRanks)).toThrow(/reference\/comparisonNounIds\/2/);
    const [unknown, unknownRanks] = copy();
    unknown.reference.comparisonNounIds[3] = 'unknown-noun';
    expect(() => validateCoursePack(unknown, unknownRanks)).toThrow(/reference\/comparisonNounIds\/3/);
  });

  test('rejects a missing pipeline step', () => {
    const [sample, ranks] = copy();
    sample.reference.pipeline.steps.pop();
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/pipeline\/steps/);
  });

  test('rejects an empty pipeline question', () => {
    const [sample, ranks] = copy();
    sample.reference.pipeline.steps[1].question = '';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/pipeline\/steps\/1\/question/);
  });

  test.each([
    ['title', (sample: typeof course) => { sample.reference.pipeline.title = ' \t '; }, /reference\/pipeline\/title/],
    ['step example', (sample: typeof course) => { sample.reference.pipeline.steps[2].example = '\n '; }, /reference\/pipeline\/steps\/2\/example/],
    ['compact example', (sample: typeof course) => { sample.reference.pipeline.compactExample = '  '; }, /reference\/pipeline\/compactExample/],
  ])('rejects whitespace-only pipeline %s', (_field, change, message) => {
    const [sample, ranks] = copy();
    change(sample);
    expect(() => validateCoursePack(sample, ranks)).toThrow(message);
  });

  test('rejects reordered and duplicate pipeline IDs', () => {
    const [reordered, ranks] = copy();
    [reordered.reference.pipeline.steps[0], reordered.reference.pipeline.steps[1]] =
      [reordered.reference.pipeline.steps[1], reordered.reference.pipeline.steps[0]];
    expect(() => validateCoursePack(reordered, ranks)).toThrow(/reference\/pipeline\/steps\/0\/id/);
    const [duplicate, duplicateRanks] = copy();
    duplicate.reference.pipeline.steps[1].id = duplicate.reference.pipeline.steps[0].id;
    expect(() => validateCoursePack(duplicate, duplicateRanks)).toThrow(/reference\/pipeline\/steps\/1\/id/);
  });

  test('rejects extra pipeline fields', () => {
    const [sample, ranks] = copy();
    (sample.reference.pipeline as typeof sample.reference.pipeline & { extra?: string }).extra = 'unused';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/pipeline\/extra/);
  });

  test('rejects a case-reference drill with an unknown skill', () => {
    const [sample, ranks] = copy();
    sample.reference.caseRows[1].skill = 'missing.skill';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/caseRows\/1\/skill/);
  });

  test('rejects a broken reference chain', () => {
    const [sample, ranks] = copy();
    sample.reference.chainRows[2].from = 'Inne zdanie.';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/chainRows\/2\/from/);
  });

  test('rejects a tense comparison against the wrong baseline', () => {
    const [sample, ranks] = copy();
    sample.reference.tenseRows[4].from = 'Inne zdanie.';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/tenseRows\/4\/from/);
  });

  test('rejects an aspect row without a matching verb', () => {
    const [sample, ranks] = copy();
    sample.reference.aspectRows[1].from = 'nieznany';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/aspectRows\/1\/from/);
  });

  test('rejects a male accusative example that does not illustrate its target phrase', () => {
    const [sample, ranks] = copy();
    sample.reference.maleAccRows[1].examples[0].sentence = 'Widzę dom.';
    expect(() => validateCoursePack(sample, ranks)).toThrow(/reference\/maleAccRows\/1\/examples\/0\/sentence/);
  });

  test.each(['Widzę {other}.', 'Widzę.'])('rejects a sentence pattern with incompatible placeholders: %s', pattern => {
    const [sample, ranks] = copy();
    sample.exercisePatterns.seenAcc = pattern;
    expect(() => validateCoursePack(sample, ranks)).toThrow(/exercisePatterns\/seenAcc/);
  });

  test('rejects a nonsequential frequency source', () => {
    const [sample, ranks] = copy();
    ranks.items[9].rank = 11;
    expect(() => validateCoursePack(sample, ranks)).toThrow(/frequency\/items\/9\/rank/);
  });

  test('accepts a skill with no authored nativeParallel (native-contrast falls back to rule-first for it, not a build error)', () => {
    const [sample, ranks] = copy();
    delete (sample.skills[0].styleContent as Partial<NonNullable<typeof sample.skills[0]['styleContent']>>).nativeParallel;
    expect(() => validateCoursePack(sample, ranks)).not.toThrow();
    const [noStyleContentAtAll, moreRanks] = copy();
    delete (noStyleContentAtAll.skills[0] as Partial<typeof noStyleContentAtAll.skills[0]>).styleContent;
    expect(() => validateCoursePack(noStyleContentAtAll, moreRanks)).not.toThrow();
  });

  test('rejects an empty nativeParallel/examples/table list where omitting the field is expected', () => {
    const [emptyParallel, ranks] = copy();
    emptyParallel.skills[0].styleContent = { ...emptyParallel.skills[0].styleContent, nativeParallel: [] };
    expect(() => validateCoursePack(emptyParallel, ranks)).toThrow(/skills\/0\/styleContent\/nativeParallel/);
    const [emptyExamples, examplesRanks] = copy();
    emptyExamples.skills[0].styleContent = { ...emptyExamples.skills[0].styleContent, examples: [] };
    expect(() => validateCoursePack(emptyExamples, examplesRanks)).toThrow(/skills\/0\/styleContent\/examples/);
  });
});
