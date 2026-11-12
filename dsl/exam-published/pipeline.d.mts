export interface ExamSource {
  file: string;
  record: string;
  rows: Array<{ tag: string; value: Record<string, unknown> }>;
}

export interface PublishedPaper {
  publication: Record<string, unknown>;
  sources: ExamSource[];
}

export const RECORD_TAGS: Map<string, string>;
export function discoverSources(cli: string, directory: string): ExamSource[];
export function validateForwardSourceRecords(sources: ExamSource[]): void;
export function validateSources(cli: string, options?: { root?: string; publication?: string }): Promise<PublishedPaper[]>;
export function aggregate(cli: string, papers: PublishedPaper[], options?: { outputDir?: string }): Promise<Array<[string, string]>>;
