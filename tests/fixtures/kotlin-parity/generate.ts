import { createHash } from 'node:crypto';
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';
import { capture, sourceRevision, timeZone } from './capture';
import { generatorParameters } from 'ts-fsrs';

const directory = dirname(fileURLToPath(import.meta.url));
const root = join(directory, '../../..');
const lock = JSON.parse(readFileSync(join(root, 'package-lock.json'), 'utf8')) as { packages: Record<string, { version?: string }> };
const sourcePaths = [
  'src/types.ts', 'src/data/nouns.ts', 'src/data/adjectives.ts', 'src/data/verbs.ts', 'src/data/pronouns.ts',
  'src/grammar/engine.ts', 'src/training/skills.ts', 'src/training/generator.ts', 'src/training/evaluator.ts',
  'src/training/queue.ts', 'src/srs/scheduler.ts', 'src/progress/storage.ts', 'src/progress/review.ts',
];
const hash = (data: string | Buffer) => createHash('sha256').update(data).digest('hex');
const files = capture();
const manifest = {
  schemaVersion: 1,
  sourceRevision,
  generator: 'npm run fixtures:generate',
  node: process.version,
  tsx: lock.packages['node_modules/tsx'].version,
  tsFsrs: lock.packages['node_modules/ts-fsrs'].version,
  fsrsParameters: generatorParameters({ request_retention: 0.9, maximum_interval: 3650, enable_fuzz: true, enable_short_term: true, learning_steps: ['1m', '10m'], relearning_steps: ['10m'] }),
  timeZone,
  sourceSha256: Object.fromEntries(sourcePaths.map(path => [path, hash(readFileSync(join(root, path)))])),
  lockSha256: hash(readFileSync(join(root, 'package-lock.json'))),
  comparison: 'Exact JSON values and array order. Exercise IDs use <generated-id>; original IDs are checked nonempty. FSRS numeric tolerances for Kotlin require later analysis.',
  files: Object.fromEntries(Object.entries(files).map(([name, file]) => {
    const data = `${JSON.stringify(file, null, 2)}\n`;
    writeFileSync(join(directory, `${name}.json`), data);
    return [name, { sha256: hash(data), caseIds: file.cases.map(item => item.id) }];
  })),
};
writeFileSync(join(directory, 'manifest.json'), `${JSON.stringify(manifest, null, 2)}\n`);
