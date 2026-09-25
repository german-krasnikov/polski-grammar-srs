import { describe, expect, it } from 'vitest';
import { changeHighlightParts, endingHighlightParts, explicitContrastParts, sentenceHighlightParts } from '../src/ui/endingHighlight';

describe('changed ending highlight', () => {
  it('marks only the three changed endings in a Polish phrase', () => {
    const parts = endingHighlightParts('moja piękna żona', 'moją piękną żonę');
    expect(parts.map(part => part.text).join('')).toBe('moją piękną żonę');
    expect(parts.filter(part => part.isEnding).map(part => part.text)).toEqual(['ą', 'ą', 'ę']);
  });

  it('does not colour substitutions or uncertain stem changes', () => {
    expect(endingHighlightParts('mojej', 'ich').some(part => part.isEnding)).toBe(false);
    expect(endingHighlightParts('Widzę', 'Widziałem').some(part => part.isEnding)).toBe(false);
    expect(endingHighlightParts('kupuję', 'kupiłem / kupiłam').some(part => part.isEnding)).toBe(false);
  });

  it('marks the old suffixes in context and the new suffixes in the answer', () => {
    const changes = [{ from: 'moja piękna żona', to: 'moją piękną żonę', reason: 'Biernik' }];
    const source = sentenceHighlightParts('To jest moja piękna żona.', changes, 'before');
    const answer = sentenceHighlightParts('Widzę moją piękną żonę.', changes, 'after');
    expect(source.map(part => part.text).join('')).toBe('To jest moja piękna żona.');
    expect(source.filter(part => part.isChanged).map(part => part.text)).toEqual(['a', 'a', 'a']);
    expect(answer.filter(part => part.isChanged).map(part => part.text)).toEqual(['ą', 'ą', 'ę']);
  });

  it('marks whole-word replacement without calling it an ending', () => {
    expect(changeHighlightParts('mojej', 'ich', 'before').filter(part => part.isChanged).map(part => part.text)).toEqual(['mojej']);
    expect(changeHighlightParts('mojej', 'ich', 'after').filter(part => part.isChanged).map(part => part.text)).toEqual(['ich']);
  });

  it('marks an ambiguous alternative pair as a whole-form change', () => {
    expect(changeHighlightParts('robić', 'robiłem / robiłam', 'before').filter(part => part.isChanged).map(part => part.text)).toEqual(['robić']);
    expect(changeHighlightParts('robić', 'robiłem / robiłam', 'after').filter(part => part.isChanged).map(part => part.text)).toEqual(['robiłem / robiłam']);
    expect(changeHighlightParts('robić', 'robiłem / robiłam', 'after').some(part => part.isEnding)).toBe(false);
  });

  it('does not highlight a substring inside a longer Polish word', () => {
    const change = [{ from: 'ona', to: 'nią', reason: 'Biernik' }];
    const parts = sentenceHighlightParts('Moja żona mówi.', change, 'before');
    expect(parts.map(part => part.text).join('')).toBe('Moja żona mówi.');
    expect(parts.some(part => part.isChanged)).toBe(false);
  });

  it('renders an invalid explicit segment as uncoloured literal text', () => {
    const pair = {
      from: 'żona', to: 'żonie',
      beforeParts: [{ text: 'żona', isChanged: true, isEnding: false }],
      afterParts: [{ text: 'żony', isChanged: true, isEnding: false }],
    };
    expect(explicitContrastParts(pair, 'after')).toEqual([{ text: 'żonie', isChanged: false, isEnding: false }]);
  });
});
