import type { Progress } from '../types';

// Explicit skill selection keeps a focused drill; automatic mode follows due dates.
export function nextSkillId(progress: Progress, selected: string | null = null): string {
  if (selected && progress.cards.some(card => card.skillId === selected)) return selected;
  const next = [...progress.cards].sort((a, b) =>
    new Date(a.card.due).getTime() - new Date(b.card.due).getTime()
  )[0];
  if (!next) throw new Error('No skills available');
  return next.skillId;
}
