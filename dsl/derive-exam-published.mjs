import { resolve } from 'node:path';
import { aggregate, validateSources } from './exam-published/pipeline.mjs';

export const OUT = resolve(import.meta.dirname, 'exam-published/generated/ExamRow.core-data');

export async function generate({ cli }) {
  if (!cli) throw new Error('Biome CLI context is required');
  const papers = await validateSources(resolve(cli));
  return aggregate(resolve(cli), papers);
}
