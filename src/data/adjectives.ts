import type { Adjective } from '../types';
import { courseAdjectives } from './course';
export const adjectives: Adjective[] = courseAdjectives;
export const adjectiveById = (id: string) => {
  const adjective = adjectives.find(item => item.id === id);
  if (!adjective) throw new Error(`Unknown adjective ${id}`);
  return adjective;
};
