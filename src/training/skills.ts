import type { Skill } from '../types';
import { courseSkills } from '../data/course';
export const skills: Skill[] = courseSkills;
export const skillById = (id: string) => {
  const skill = skills.find(item => item.id === id);
  if (!skill) throw new Error(`Unknown skill ${id}`);
  return skill;
};
