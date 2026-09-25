export interface InventoryFile {
  path: string;
  sha256: string;
  literalLines: Array<{ line: number; source: string }>;
}

export interface Inventory {
  schemaVersion?: number;
  files: InventoryFile[];
}

export interface InventoryCandidate {
  path: string;
  line: number;
  source: string;
  symbol: string;
  sourceFingerprint: string;
  occurrence: number;
  tokens: string[];
}

export interface InventoryPart {
  ordinal: number;
  category: string;
  reason: string;
  packPointer?: string;
}

export interface InventoryDecision {
  path: string;
  symbol: string;
  sourceFingerprint: string;
  occurrence: number;
  category?: string;
  reason?: string;
  packPointer?: string;
  parts?: InventoryPart[];
}

export function buildCandidates(inventory: Inventory, sourceTextByPath: Map<string, string>): InventoryCandidate[];
export function scanLines(source: string, path: string): Array<{ tokens: string[]; tokenStarts: number[]; braces: string[] }>;
export function checkInventory(args: {
  inventory: Inventory;
  decisions: { schemaVersion: 1; decisions: InventoryDecision[] };
  course: object;
  sourceTextByPath: Map<string, string>;
}): {
  schemaVersion: 1;
  candidateCount: number;
  tokenCount: number;
  rows: Array<InventoryCandidate & { parts: InventoryPart[] }>;
};
