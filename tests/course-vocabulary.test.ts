// R1.8b (Plans/Biome/R1Design.md): the vocabulary, the training chain and the stem alternations against dsl/course.data-contract: what `biome data` rejects, with the exact position of each problem.
import { describe, expect, it } from 'vitest';
import { hasCli, row, rowsOf } from './support/course-data';

const card = (id: string, change: Record<string, string | undefined> = {}) =>
  row('', { id: `"${id}"`, lemma: '"l"', translation: '"t"', form: '"f"', example: '"e"', level: 'A1', frequencyRank: '7' }, change).slice(1);
const vocabulary = (cards: string[], change: Record<string, string | undefined> = {}) =>
  row('VOCABULARY', { source: '"s"', items: `[${cards.join(' ')}]`, instructions: '{react: "r" web: "w" native: "n" ios: "i"}', unavailableLabel: '"u"' }, change);
const training = (steps: string) =>
  row('TRAINING', { chainPresentation: `{steps: [${steps}] completion: {title: "t" reactEyebrow: "e" reactBody: "b" webBody: "w"}}` });

describe.skipIf(!hasCli)('vocabulary rows', () => {
  const { check, problem } = rowsOf('VocabularyRow');
  it('a card without its translation is DATA_REQUIRED at the { of the card', () =>
    problem([vocabulary([card('a', { translation: undefined })])], 0, '{id: "a"', 'DATA_REQUIRED'));
  it('a rank that is a text is DATA_TYPE at the value', () => problem([vocabulary([card('a', { frequencyRank: '"n/a"' })])], 0, '"n/a"', 'DATA_TYPE'));
  it('a rank left out is DATA_REQUIRED at the { of the card: absence is written as null', () =>
    problem([vocabulary([card('a', { frequencyRank: undefined })])], 0, '{id: "a"', 'DATA_REQUIRED'));
  it('a rank of null passes', () => expect(check(vocabulary([card('a', { frequencyRank: 'null' })]) + '\n')).toEqual({ status: 0, problems: [] }));
  it('instructions without ios are DATA_REQUIRED at the { of the instructions', () =>
    problem([vocabulary([card('a')], { instructions: '{react: "r" web: "w" native: "n"}' })], 0, '{react: "r"', 'DATA_REQUIRED'));
  it('two vocabulary rows are DATA_COUNT at 1:1 (exactly one is expected)', () =>
    expect(check([vocabulary([card('a')]), vocabulary([card('b')])].join('\n') + '\n').problems).toEqual(['vocabulary.core-data:1:1: DATA_COUNT']));
  it('two cards with the same id pass the contract: the mapper refuses them (PlRuVocabularyRowsTest)', () =>
    expect(check(vocabulary([card('a'), card('a')]) + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('training rows', () => {
  const { check, problem } = rowsOf('TrainingRow');
  it('a chain step without its label is DATA_REQUIRED at the { of the step', () => problem([training('{id: "acc"}')], 0, '{id: "acc"}', 'DATA_REQUIRED'));
  it('two steps with the same id pass the contract: the mapper refuses them (PlRuVocabularyRowsTest)', () =>
    expect(check(training('{id: "acc" label: "a"} {id: "acc" label: "b"}') + '\n')).toEqual({ status: 0, problems: [] }));
});

describe.skipIf(!hasCli)('stem alternation rows', () => {
  const { check, problem } = rowsOf('StemAlternationRow');
  it('a letter left out is DATA_REQUIRED at the { of the row', () => problem([row('STEM_ALT', { a: '"ó"' })], 0, '{', 'DATA_REQUIRED'));
  it('two letters pass the contract: the mapper refuses them (PlRuVocabularyRowsTest)', () =>
    expect(check(row('STEM_ALT', { a: '"ób"', b: '"o"' }) + '\n')).toEqual({ status: 0, problems: [] }));
});
