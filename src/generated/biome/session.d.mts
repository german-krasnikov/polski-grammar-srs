// Generated from session.logic; source-sha256=f535abb2731a42a070bcb8c07dd91891325cfdefc832f4d5bb90930b61e30884
// contract-sha256=7eea90644d523369085c2ff22812dc978e2f8ab6ca78a3e74e6b05a5efefc8ba
export interface CountsCorrectInput { readonly reviewHasTypedResult: boolean; readonly reviewRating: "again" | "hard" | "good" | "easy"; readonly reviewTypedCorrect: boolean; }
export type CountsCorrectValue = boolean;
export function countsCorrect(input: { reviewHasTypedResult(): boolean; reviewRating(): "again" | "hard" | "good" | "easy"; reviewTypedCorrect(): boolean; }): CountsCorrectValue;
export interface Policy { countsCorrect(input: CountsCorrectInput): CountsCorrectValue; }
export declare const policy: Policy;
