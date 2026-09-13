import { describe, expect, it } from 'vitest';
import { adjectiveForm, nounPhrase, verbForm, normalize } from '../src/grammar/engine';
import { generateForSkill } from '../src/training/generator';
import { evaluate } from '../src/training/evaluator';
import { skills } from '../src/training/skills';
import { nextSkillId } from '../src/training/queue';
import { newSkillCard, preview, review, serialize, deserialize } from '../src/srs/scheduler';
import { freshProgress } from '../src/progress/storage';

describe('correct Polish forms', () => {
  it('declines drogi with the required i', () => {
    expect(nounPhrase('car', 'gen', { adjectiveId: 'expensive' })).toBe('drogiego samochodu');
    expect(nounPhrase('wife', 'loc', { adjectiveId: 'expensive' })).toBe('drogiej żonie');
    expect(adjectiveForm('expensive', 'n', 'nom')).toBe('drogie');
  });

  it.each([
    ['new', 'nowi'], ['small', 'mali'], ['nice', 'ładni'],
    ['good', 'dobrzy'], ['old', 'starzy'], ['expensive', 'drodzy'],
  ])('uses the masculine personal plural of %s', (id, expected) => {
    expect(adjectiveForm(id, 'm-personal', 'nom', 'pl')).toBe(expected);
  });

  it('uses feminine past endings', () => {
    expect(verbForm('see', 'past', 1, 'sg', 'f')).toBe('widziałam');
    expect(verbForm('see', 'past', 2, 'sg', 'f')).toBe('widziałaś');
    expect(verbForm('go', 'past', 1, 'sg', 'f')).toBe('szłam');
    expect(verbForm('go', 'past', 1, 'sg', 'm-personal')).toBe('szedłem');
  });

  it('handles być and perfective future without inventing a present tense', () => {
    expect(verbForm('be', 'future', 1, 'sg')).toBe('będę');
    expect(verbForm('be', 'future', 2, 'pl')).toBe('będziecie');
    expect(verbForm('buyDone', 'future', 1, 'sg')).toBe('kupię');
    expect(() => verbForm('buyDone', 'present', 1, 'sg')).toThrow();
  });

  it('distinguishes accusative and genitive for kolega', () => {
    expect(nounPhrase('friendM', 'acc', { adjectiveId: 'good', possessive: true })).toBe('mojego dobrego kolegę');
    expect(nounPhrase('friendM', 'gen', { adjectiveId: 'good', possessive: true })).toBe('mojego dobrego kolegi');
  });

  it('normalizes equivalent Unicode without dropping Polish diacritics', () => {
    expect(normalize('  WIDZĘ ŻONĘ!  '.normalize('NFD'))).toBe('widzę żonę');
    expect(normalize('widze zone')).not.toBe('widzę żonę');
  });
});

describe('training', () => {
  it('generates valid, self-consistent exercises for every skill', () => {
    for (const skill of skills) {
      for (let i = 0; i < 100; i++) {
        const exercise = generateForSkill(skill.id);
        expect(exercise.primarySkill).toBe(skill.id);
        expect(exercise.expected).not.toMatch(/undefined|NaN/);
        expect(evaluate(exercise.expected, exercise).correct).toBe(true);
        for (const answer of exercise.accepted ?? []) expect(evaluate(answer, exercise).correct).toBe(true);
      }
    }
  });

  it('selects the next card from updated due dates and respects a focused drill', () => {
    const progress = freshProgress();
    progress.cards[0].card.due = '2099-01-01T00:00:00.000Z';
    progress.cards[1].card.due = '2000-01-01T00:00:00.000Z';
    expect(nextSkillId(progress)).toBe(progress.cards[1].skillId);
    expect(nextSkillId(progress, progress.cards[0].skillId)).toBe(progress.cards[0].skillId);
  });

  it('previews all grades and round-trips a reviewed FSRS card', () => {
    const card = newSkillCard(skills[0].id);
    for (const date of Object.values(preview(card))) expect(Number.isFinite(date.getTime())).toBe(true);
    for (const grade of ['again', 'hard', 'good', 'easy'] as const) {
      const updated = review(card, grade);
      expect(updated.card.reps).toBe(1);
      expect(updated.skillId).toBe(card.skillId);
      expect(serialize(deserialize(updated.card))).toEqual(updated.card);
    }
  });
});
