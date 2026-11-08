import { createHash } from 'node:crypto';

/** Test-side reader for the accepted text-wire transport; expected values stay in frozen fixtures. */
export function readPolskiText(raw: string, kind: string): Record<string, unknown> {
  const lines = raw.split('\n');
  if (lines.at(-1) !== '') throw new Error('POLSKI-TEXT must end with LF');
  lines.pop();
  const header = lines.shift()?.split('\t');
  if (header?.[0] !== 'POLSKI-TEXT' || header[1] !== '1' || header[2] !== kind) {
    throw new Error(`Unexpected POLSKI-TEXT header for ${kind}`);
  }
  const digest = lines.pop()?.split('\t');
  if (digest?.[0] !== 'H' || digest[1] !== 'sha256') throw new Error('Missing text-wire digest');
  const body = `${header.join('\t')}\n${lines.join('\n')}\n`;
  if (createHash('sha256').update(body).digest('hex') !== digest[2]) throw new Error('Text-wire digest mismatch');

  const unescape = (value: string) => value.replace(/\\(?:([tnr\\])|u([0-9a-fA-F]{4}))/g, (_match, short: string | undefined, hex: string | undefined) =>
    hex ? String.fromCharCode(parseInt(hex, 16)) : ({ t: '\t', n: '\n', r: '\r', '\\': '\\' } as Record<string, string>)[short!]);
  let index = 0;
  const node = (expectedKey: string): unknown => {
    const [tag, key, value] = lines[index++]?.split('\t') ?? [];
    if (key !== expectedKey) throw new Error(`Unexpected text-wire key ${key}, wanted ${expectedKey}`);
    if (tag === 'M') {
      const result: Record<string, unknown> = {};
      for (let n = 0; n < Number(value); n += 1) {
        const childKey = lines[index]?.split('\t')[1];
        if (!childKey?.startsWith('K:')) throw new Error('Expected named text-wire child');
        result[unescape(childKey.slice(2))] = node(childKey);
      }
      return result;
    }
    if (tag === 'L') return Array.from({ length: Number(value) }, () => node('-'));
    if (tag === 'S') return unescape(value ?? '');
    if (tag === 'I') return Number(value);
    if (tag === 'D') return Buffer.from(value, 'hex').readDoubleBE();
    if (tag === 'B') return value === '1';
    if (tag === 'N') return null;
    throw new Error(`Unexpected text-wire tag ${tag}`);
  };
  const result = node('-');
  if (index !== lines.length || !result || Array.isArray(result) || typeof result !== 'object') {
    throw new Error('Malformed text-wire root');
  }
  return result as Record<string, unknown>;
}

/** The frozen progress oracle predates text-wire and nests each card payload. */
export function progressAsLegacyShape(raw: string): Record<string, unknown> {
  const document = readPolskiText(raw, 'progress');
  const cards = document.cards as Array<Record<string, unknown>>;
  const stats = document.stats as Array<Record<string, unknown>>;
  return {
    ...document,
    cards: cards.map(({ skillId, ...card }) => ({ skillId, card })),
    stats: Object.fromEntries(stats.map(({ skillId, ...entry }) => [skillId, entry])),
  };
}
