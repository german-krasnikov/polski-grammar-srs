import { describe, expect, it } from 'vitest';
import { renderCoursePattern } from '../src/data/course';

describe('authored sentence patterns', () => {
  it('fills the Polish form without changing punctuation', () => {
    expect(renderCoursePattern('seenAcc', { acc: 'moją żonę' })).toBe('Widzę moją żonę.');
  });

  it('rejects a missing form instead of showing an unresolved marker', () => {
    expect(() => renderCoursePattern('seenAcc', {})).toThrow(/acc/);
  });
});
