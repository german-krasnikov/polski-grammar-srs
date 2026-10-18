// Generated from swipe.logic; source-sha256=4944f9411f45dd1f2c6bbc49b7009b06d7fef2bfd292268e5cfe1aa9479a845f
// contract-sha256=11fb3b7da4d623e677053e38e260d0c1847f6b5679ec9c10edc03e362ba251e8
export interface RatingInput { readonly swipeBelowThreshold: boolean; readonly swipeDxPositive: boolean; readonly swipeYDominates: boolean; }
export type RatingValue = "again" | "good" | "none";
export function rating(input: { swipeBelowThreshold(): boolean; swipeDxPositive(): boolean; swipeYDominates(): boolean; }): RatingValue;
export declare const RatingValues: readonly RatingValue[];
export interface Policy { rating(input: RatingInput): RatingValue; }
export declare const policy: Policy;
