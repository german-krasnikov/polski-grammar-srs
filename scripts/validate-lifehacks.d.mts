/** Validates a pair's lifehacks.json; `curriculumIds` null/undefined skips the skillId cross-check. Throws with a field path. */
export declare function validateLifehacks(pairId: string, lifehacks: unknown, curriculumIds?: Iterable<string> | null): true;
