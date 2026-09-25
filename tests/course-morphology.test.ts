import { describe, expect, it } from 'vitest';
import course from '../courses/pl-ru/course.json';
import grammar from './fixtures/kotlin-parity/grammar.json';
import { possessiveForm } from '../src/data/pronouns';
import { conjugate, verbById } from '../src/data/verbs';
import type { Gender, GramCase, NumberGram, PossessiveId, Person } from '../src/types';

describe('course morphology', () => {
  it('stores each pinned possessive output in the authored pack', () => {
    const owners = course.possessives as unknown as Array<{id: string; forms?: {kind: string; value?: string; sg?: Record<string, Record<string, string>>; pl?: Record<string, Record<string, string>>}}>; 
    const cases = grammar.cases.filter(item => item.id.startsWith('G-POSS-'));
    expect(cases).toHaveLength(490);
    for (const item of cases) {
      const {owner, number, gender, gramCase} = item.input as {owner: PossessiveId; number: NumberGram; gender: Gender; gramCase: GramCase};
      const forms = owners.find(entry => entry.id === owner)?.forms;
      expect(forms, item.id).toBeDefined();
      const stored = forms?.kind === 'invariant' ? forms.value :
        number === 'sg' ? forms?.sg?.[gender]?.[gramCase] :
          forms?.pl?.[gender === 'm-personal' ? 'm-personal' : 'other']?.[gramCase];
      expect(stored, item.id).toBe(item.expected.form);
      expect(possessiveForm(owner, gender, number, gramCase), item.id).toBe(item.expected.form);
    }
  });

  it('stores six pinned future auxiliary outputs and preserves conjugation', () => {
    const morphology = (course as unknown as {morphology?: {futureAuxiliary: {verbId: string; forms: Record<string, Record<string, string>>}}}).morphology;
    expect(morphology?.futureAuxiliary.verbId).toBe('be');
    for (const number of ['sg', 'pl'] as const) for (const person of [1, 2, 3] as const) {
      const item = grammar.cases.find(row => row.id === `G-VERB-be-future-${person}-${number}-m-personal`);
      expect(item).toBeDefined();
      expect(morphology?.futureAuxiliary.forms[number][person]).toBe(item!.expected.form);
      expect(conjugate(verbById('be'), 'future', person as Person, number)).toBe(item!.expected.form);
    }
  });
});
