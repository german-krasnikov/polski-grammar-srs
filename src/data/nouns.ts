import type { Noun } from '../types';
import { courseNouns } from './course';
export const nouns: Noun[] = courseNouns;
export const nounById = (id: string) => {
  const noun = nouns.find(item => item.id === id);
  if (!noun) throw new Error(`Unknown noun ${id}`);
  return noun;
};
