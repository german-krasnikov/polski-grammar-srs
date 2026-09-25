import type { GramCase, Gender, NumberGram, PossessiveId } from '../types';
import { coursePersonalPronouns, coursePossessives, type CoursePossessive } from './course';

export const personalPronouns = coursePersonalPronouns;
export const possessives: ReadonlyArray<CoursePossessive> = coursePossessives;

/** Returns a course-authored form; plural non-personal genders share one paradigm. */
export function possessiveForm(id: PossessiveId, gender: Gender, number: NumberGram, gramCase: GramCase): string {
  const forms = possessives.find(owner => owner.id === id)?.forms;
  if (!forms) throw new Error(`Unknown possessive ${id}`);
  if (forms.kind === 'invariant') return forms.value;
  return number === 'sg' ? forms.sg[gender][gramCase] :
    forms.pl[gender === 'm-personal' ? 'm-personal' : 'other'][gramCase];
}

export function possessiveMy(gender: Gender, number: NumberGram, gramCase: GramCase): string {
  return possessiveForm('my', gender, number, gramCase);
}
