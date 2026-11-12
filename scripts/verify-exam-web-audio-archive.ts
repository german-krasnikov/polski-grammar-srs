import { createHash } from 'node:crypto';
import { execFileSync, spawn } from 'node:child_process';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { EXAM_AUDIO_ASSET_ROW_ROWS, type ExamAudioAssetRow } from '../dsl/exam-assets/generated/ExamAudioRows.ts';
import { EXAM_PUBLICATION_ROW_ROWS } from '../dsl/exam-published/generated/ExamPublicationRows.ts';
import { verifyPublishedAssetCoverage } from './prepare-exam-audio.ts';

function archiveMembers(archive: string): string[] {
  const listing = execFileSync('tar', ['-tf', archive], { encoding: 'utf8' });
  return listing.trimEnd().split('\n');
}

async function archiveMemberHash(archive: string, member: string): Promise<string> {
  const child = spawn('tar', ['-xOf', archive, member], { stdio: ['ignore', 'pipe', 'pipe'] });
  const closed = new Promise<{ code: number | null; error?: Error }>(resolveExit => {
    child.once('error', error => resolveExit({ code: null, error }));
    child.once('close', code => resolveExit({ code }));
  });
  const hash = createHash('sha256');
  let error = '';
  child.stderr.setEncoding('utf8');
  child.stderr.on('data', chunk => { error += chunk; });
  for await (const chunk of child.stdout) hash.update(chunk);
  const exit = await closed;
  if (exit.code !== 0) throw new Error(`Archive member ${member}: tar failed: ${error || exit.error?.message || exit.code}`);
  return hash.digest('hex');
}

/** Checks exact effective web members and bytes before any archive extraction. */
export async function verifyWebAudioArchive(archive: string, rows: readonly ExamAudioAssetRow[]): Promise<void> {
  verifyPublishedAssetCoverage(rows, rows.map(row => row.examId));
  const expected = new Map(rows.map(row => [row.webLocalSourcePath ?? row.localSourcePath, row.webSha256 ?? row.sha256]));
  if (expected.size !== rows.length) throw new Error('Archive source paths are not unique');
  const members = archiveMembers(archive);
  if (members.length !== expected.size || new Set(members).size !== members.length ||
      members.some(member => !expected.has(member))) {
    throw new Error(`Archive member list differs from ${expected.size} declared effective web files`);
  }
  for (const [member, digest] of expected) {
    if (await archiveMemberHash(archive, member) !== digest) throw new Error(`Archive member ${member}: SHA-256 mismatch`);
  }
}

async function main(): Promise<void> {
  const archive = process.argv[2];
  if (!archive) throw new Error('Usage: tsx scripts/verify-exam-web-audio-archive.ts <archive.tar>');
  verifyPublishedAssetCoverage(EXAM_AUDIO_ASSET_ROW_ROWS, EXAM_PUBLICATION_ROW_ROWS.map(row => row.examRef));
  await verifyWebAudioArchive(resolve(archive), EXAM_AUDIO_ASSET_ROW_ROWS);
  console.log(`PASS web audio archive: ${EXAM_AUDIO_ASSET_ROW_ROWS.length} exact members`);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().catch(error => { console.error(error); process.exitCode = 1; });
}
