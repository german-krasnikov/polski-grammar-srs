import { resolve } from 'node:path';
import { validateSources } from './pipeline.mjs';

const cli = process.env.BIOME_CLI;
if (!cli) throw new Error('BIOME_CLI is required; run through biome generate');
await validateSources(resolve(cli));
