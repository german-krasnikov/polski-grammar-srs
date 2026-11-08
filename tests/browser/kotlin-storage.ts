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
        const escapedField = field.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const re = new RegExp('^I\\tK:' + escapedField + '\\t(-?\\d+)$', 'm');
        const m = raw.match(re);
        return m ? parseInt(m[1], 10) : 0;
      }
      // Parse full object (flat fields only — enough for totalReviews, reviewsToday, lastDay)
      const obj: any = {};
      for (const line of raw.split('\n')) {
        const p = line.split('\t');
        if (p.length >= 3 && p[1]?.startsWith('K:')) {
          const key = p[1].slice(2);
          if (p[0] === 'I') obj[key] = parseInt(p[2], 10);
          else if (p[0] === 'S') obj[key] = p[2];
          else if (p[0] === 'B') obj[key] = p[2] === '1';
        }
      }
      return obj;
    };
  });
}
