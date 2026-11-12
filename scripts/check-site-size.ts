import { lstat, readdir } from 'node:fs/promises';
import { join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

export const siteTargetBytes = 850_000_000;
export const pagesMaximumBytes = 1_000_000_000;

/** Counts every assembled file, including dotfiles; symlinks are rejected. */
export async function measureSiteBytes(directory: string): Promise<number> {
  let total = 0;
  for (const name of await readdir(directory)) {
    const path = join(directory, name);
    const info = await lstat(path);
    if (info.isSymbolicLink()) throw new Error(`Site contains a symlink: ${path}`);
    if (info.isDirectory()) total += await measureSiteBytes(path);
    else if (info.isFile()) total += info.size;
    else throw new Error(`Site contains an unsupported entry: ${path}`);
  }
  return total;
}

export function assertSiteBudget(bytes: number): void {
  if (!Number.isSafeInteger(bytes) || bytes < 0) throw new Error(`Invalid site byte count: ${bytes}`);
  if (bytes > siteTargetBytes) throw new Error(`Site ${bytes} bytes exceeds ${siteTargetBytes}-byte target`);
  if (bytes > pagesMaximumBytes) throw new Error(`Site ${bytes} bytes exceeds ${pagesMaximumBytes}-byte Pages maximum`);
}

async function main(): Promise<void> {
  const directory = process.argv[2];
  if (!directory) throw new Error('Usage: tsx scripts/check-site-size.ts <assembled-site-directory>');
  const bytes = await measureSiteBytes(resolve(directory));
  assertSiteBudget(bytes);
  console.log(`PASS site size: ${bytes} bytes; target ${siteTargetBytes}, maximum ${pagesMaximumBytes}`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().catch(error => { console.error(error); process.exitCode = 1; });
}
