import type { RatingName } from '../types';

/** Physical input adapter for the review policy's three declared facts. */
export function reviewRating(rating: RatingName): RatingName { return rating; }
export function reviewHasTypedResult(typedCorrect?: boolean): boolean { return typedCorrect != null; }
export function reviewTypedCorrect(typedCorrect?: boolean): boolean { return typedCorrect ?? false; }
