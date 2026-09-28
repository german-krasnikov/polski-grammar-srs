/** Validates the authored wire format and cross-file references; throws with a field path. */
export declare function validateCoursePack(course: unknown, frequency: unknown): true;
/** Throws when a style recipe repeats a block kind within a phase or across front/back. */
export declare function assertNoDuplicateBlockKinds(recipe: { id: string; blocks: { front: string[]; back: string[] } }, path?: string): void;
