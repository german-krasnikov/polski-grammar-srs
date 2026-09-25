import { expect, test } from 'vitest';
import { courseMaleAccRows } from '../src/data/course';
import { nounPhrase } from '../src/grammar/engine';

test('authored male accusative comparisons agree with the Polish morphology engine', () => {
  const nouns = ['husband', 'friendM', 'dog', 'car'];
  const adjectives = ['good', 'good', 'good', 'new'];
  const examples = courseMaleAccRows.flatMap(row => row.examples);
  expect(examples).toHaveLength(nouns.length);
  examples.forEach((example, index) => {
    const options = { adjectiveId: adjectives[index], possessive: 'my' as const };
    expect(example.from).toBe(nounPhrase(nouns[index], 'nom', options));
    expect(example.to).toBe(nounPhrase(nouns[index], 'acc', options));
  });
});
