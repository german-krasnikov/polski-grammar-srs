import type { Exercise } from '../types';

export interface EndingPart { text: string; isEnding: boolean; isChanged?: boolean }
export interface ContrastPair {
  readonly from: string;
  readonly to: string;
  readonly beforeParts: ReadonlyArray<EndingPart>;
  readonly afterParts: ReadonlyArray<EndingPart>;
}
type Side = 'before' | 'after';

/** Author-supplied parts are shown only when they preserve the literal form. */
export function explicitContrastParts(pair: ContrastPair, side: Side): EndingPart[] {
  const text = side === 'before' ? pair.from : pair.to;
  const parts = side === 'before' ? pair.beforeParts : pair.afterParts;
  let cursor = 0;
  const word = (char: string) => /[\p{L}\p{N}]/u.test(char);
  const valid = parts.length && parts.every(part => {
    if (!part.text || typeof part.isEnding !== 'boolean' || typeof part.isChanged !== 'boolean') return false;
    const next = cursor + part.text.length;
    const before = text[cursor - 1] ?? '';
    const after = text[next] ?? '';
    cursor = next;
    if (part.isEnding) return part.isChanged && part.text.length <= 3 && /^\p{L}+$/u.test(part.text) &&
      word(before) && !word(after);
    return !part.isChanged || (!word(before) && !word(after));
  });
  const otherText = side === 'before' ? pair.to : pair.from;
  const otherParts = side === 'before' ? pair.afterParts : pair.beforeParts;
  const unchanged = (items: ReadonlyArray<EndingPart>) => items.filter(part => !part.isChanged).map(part => part.text).join('');
  if (!valid || cursor !== text.length || otherParts.map(part => part.text).join('') !== otherText ||
      unchanged(parts) !== unchanged(otherParts)) {
    return [{ text, isEnding: false, isChanged: false }];
  }
  return parts.map(part => ({ ...part }));
}

/** A display-only comparison. Short, aligned suffixes and whole-word replacements are distinct. */
export function changeHighlightParts(from: string, to: string, side: Side): EndingPart[] {
  const oldWords = from.split(' ');
  const newWords = to.split(' ');
  const selected = side === 'before' ? oldWords : newWords;
  if (oldWords.length !== newWords.length || oldWords.some(word => !word) || newWords.some(word => !word)) {
    return [{ text: side === 'before' ? from : to, isEnding: false, isChanged: from !== to }];
  }
  return selected.flatMap((word, index) => {
    const old = oldWords[index];
    const next = newWords[index];
    const parts: EndingPart[] = index ? [{ text: ' ', isEnding: false }] : [];
    if (old === next) return [...parts, { text: word, isEnding: false }];
    let prefixLength = 0;
    while (prefixLength < old.length && prefixLength < next.length && old[prefixLength] === next[prefixLength]) prefixLength++;
    const oldSuffix = old.slice(prefixLength);
    const newSuffix = next.slice(prefixLength);
    const reliable = prefixLength >= 3 && newSuffix.length >= 1 && newSuffix.length <= 3 &&
      oldSuffix.length <= 3 && /^\p{L}+$/u.test(newSuffix) && (oldSuffix === '' || /^\p{L}+$/u.test(oldSuffix));
    if (reliable) {
      if (prefixLength) parts.push({ text: word.slice(0, prefixLength), isEnding: false });
      if (word.slice(prefixLength)) parts.push({ text: word.slice(prefixLength), isEnding: true, isChanged: true });
    } else {
      parts.push({ text: word, isEnding: false, isChanged: true });
    }
    return parts;
  });
}

/** Kept for existing callers that render the new form. */
export const endingHighlightParts = (from: string, to: string) => changeHighlightParts(from, to, 'after');

function wholePhraseStart(sentence: string, phrase: string): number {
  if (!phrase) return -1;
  const wordChar = (char: string) => /[\p{L}\p{N}]/u.test(char);
  let start = sentence.indexOf(phrase);
  while (start >= 0) {
    const end = start + phrase.length;
    if ((start === 0 || !wordChar(sentence[start - 1])) &&
        (end === sentence.length || !wordChar(sentence[end]))) return start;
    start = sentence.indexOf(phrase, start + 1);
  }
  return -1;
}

/** Projects explicit form changes into a full sentence without altering the sentence text. */
export function sentenceHighlightParts(sentence: string, changes: Exercise['changes'], side: Side): EndingPart[] {
  const spans = changes.map(change => {
    const phrase = side === 'before' ? change.from : change.to;
    const start = wholePhraseStart(sentence, phrase);
    return { change, phrase, start };
  }).filter(span => span.start >= 0).sort((a, b) => a.start - b.start);
  const result: EndingPart[] = [];
  let cursor = 0;
  for (const span of spans) {
    if (span.start < cursor) continue;
    if (span.start > cursor) result.push({ text: sentence.slice(cursor, span.start), isEnding: false });
    result.push(...changeHighlightParts(span.change.from, span.change.to, side));
    cursor = span.start + span.phrase.length;
  }
  if (cursor < sentence.length) result.push({ text: sentence.slice(cursor), isEnding: false });
  return result;
}
