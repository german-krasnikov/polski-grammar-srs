import { useId } from 'react';
import { changeHighlightParts, explicitContrastParts, type ContrastPair } from './endingHighlight';

/** A labelled base → result pair for grammar tables and rule examples. */
export function FormContrast({ from, to, beforeParts, afterParts }: { from: string; to: string; beforeParts?: ContrastPair['beforeParts']; afterParts?: ContrastPair['afterParts'] }) {
  const semanticId = useId();
  const explicit = beforeParts && afterParts ? { from, to, beforeParts, afterParts } : null;
  const before = explicit ? explicitContrastParts(explicit, 'before') : changeHighlightParts(from, to, 'before');
  const after = explicit ? explicitContrastParts(explicit, 'after') : changeHighlightParts(from, to, 'after');
  return <span className="form-contrast" role="group" aria-label={`Было: ${from}. Стало: ${to}`} aria-labelledby={semanticId}>
    <span id={semanticId} className="contrast-sr-only" aria-hidden="true">Было: <span lang="pl">{from}</span>. Стало: <span lang="pl">{to}</span></span>
    <span className="form-contrast-label" aria-hidden="true">Было:</span>
    <span className="form-contrast-before" lang="pl" aria-hidden="true">{before.map((part, index) => part.isChanged
      ? <span className="change-before" key={index}>{part.text}</span> : part.text)}</span>
    <span className="form-contrast-arrow" aria-hidden="true">→</span>
    <span className="form-contrast-label" aria-hidden="true">Стало:</span>
    <strong className="form-contrast-after" lang="pl" aria-hidden="true">{after.map((part, index) => part.isChanged
      ? <span className="change-after" key={index}>{part.text}</span> : part.text)}</strong>
  </span>;
}
