export function validateVocabularyEditorial(
  course: unknown | null,
  frequency: unknown,
  journal: unknown,
  options?: { strict?: boolean; frequencyBytes?: Uint8Array; expectedFrequencySource?: Record<string, string> },
): true;
