import { describe, expect, test } from 'vitest';
import course from '../courses/pl-ru/course.json';
import { generateChain } from '../src/training/generator';

describe('authored matrix chain', () => {
  test('shows the same Polish sentences as the five-step exercise', () => {
    const generated = generateChain(course.sentenceSeeds[0]);
    expect(generated.map(({ source, expected }) => [source, expected])).toEqual(
      course.reference.chainRows.map(({ from, to }) => [from, to]),
    );
  });
});
