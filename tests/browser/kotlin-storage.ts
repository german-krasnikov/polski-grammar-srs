import type { Page } from '@playwright/test';

/**
 * Install a global `__readProgress(key, field?)` in the browser that handles both JSON
 * and POLSKI-TEXT storage. Call from page.evaluate as `__readProgress(key)` for full object
 * or `__readProgress(key, 'totalReviews')` for a single int field.
 */
export async function installProgressReader(page: Page) {
  await page.addInitScript(() => {
    (window as any).__readProgress = (key: string, field?: string) => {
      const raw = localStorage.getItem(key);
      if (!raw) return field ? 0 : {};
      if (raw.startsWith('{')) {
        try { const obj = JSON.parse(raw); return field ? (obj[field] ?? 0) : obj; }
        catch { return field ? 0 : {}; }
      }
      // POLSKI-TEXT: line-based, tab-delimited
      if (field) {
        const re = new RegExp('^I\\t' + field + '\\t(-?\\d+)$', 'm');
        const m = raw.match(re);
        return m ? parseInt(m[1], 10) : 0;
      }
      // Parse full object (flat fields only — enough for totalReviews, reviewsToday, lastDay)
      const obj: any = {};
      for (const line of raw.split('\n')) {
        const p = line.split('\t');
        if (p.length >= 3 && p[1] !== '-') {
          if (p[0] === 'I') obj[p[1]] = parseInt(p[2], 10);
          else if (p[0] === 'S') obj[p[1]] = p[2];
          else if (p[0] === 'D') obj[p[1]] = parseFloat(p[2]);
        }
      }
      return obj;
    };
  });
}
