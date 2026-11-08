import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { pathToFileURL } from 'node:url';
import { expect, type Page } from '@playwright/test';

const biomeRoot = resolve(process.env.BIOME_DSL_TS_ROOT ?? '../../Python/biome-dsl/implementations/typescript');
const dialectUrl = pathToFileURL(resolve(biomeRoot, 'packages/dialects/test/dist/index.js')).href;

type ActionArgs = Record<string, string | number | boolean>;
type Snapshot = Record<string, string | number | boolean>;

/** The Playwright adapter owns only browser mechanics; actions and assertions live in .playtest. */
export async function runExamPlaytest(page: Page, sourceFile: string) {
  const { compilePlan, createRuntimeRegistry, runPlaymodePlan } = await import(dialectUrl);
  const registry = createRuntimeRegistry();
  let snapshot: Snapshot = {};
  const pageErrors: string[] = [];
  page.on('pageerror', error => pageErrors.push(error.message));
  let rememberedPosition = 0;
  let audioHandle: Awaited<ReturnType<ReturnType<Page['locator']>['elementHandle']>> = null;

  const refresh = async () => {
    snapshot = await page.evaluate(() => {
      const input = document.querySelector<HTMLInputElement>('.exam-input');
      const audio = document.querySelector<HTMLAudioElement>('audio');
      const selected = document.querySelector<HTMLElement>('.exam-reading-q .exam-option.selected');
      const review = document.querySelectorAll<HTMLElement>('.exam-review-item');
      const answer = review.item(10)?.textContent?.match(/\[([^\]]+)\]/)?.[1] ?? '';
      const time = document.querySelector('.player-time')?.textContent ?? '';
      const readingRows = [...document.querySelectorAll<HTMLElement>('.exam-reading-q')];
      const firstReading = readingRows[0];
      const image = document.querySelector<HTMLImageElement>('.exam-writing-image');
      return {
        'exam.input': input?.value ?? '',
        'exam.focused': !!input && document.activeElement === input,
        'exam.caret': input?.selectionStart ?? -1,
        'exam.score': document.querySelector('.exam-score-big')?.textContent ?? '',
        'exam.scoreDetail': document.querySelector('.exam-score-detail')?.textContent ?? '',
        'exam.reviewAnswer': answer,
        'exam.firstSelected': document.querySelector('.exam-option.selected')?.textContent ?? '',
        'exam.listVisible': !!document.querySelector('.exam-list'),
        'exam.promptImageLoaded': !!image && image.complete && image.naturalWidth > 0,
        'exam.promptImageAlt': image?.alt ?? '',
        'reading.passageOpening': document.querySelector('.exam-reading-text p')?.textContent?.replace(/^\d+\.\s*/, '').split('.')[0] ?? '',
        'reading.passageChars': document.querySelector('.exam-reading-text')?.textContent?.length ?? 0,
        'reading.questionCount': readingRows.length,
        'reading.firstQuestion': firstReading?.querySelector('.exam-reading-q-text')?.textContent ?? '',
        'reading.firstOptions': [...(firstReading?.querySelectorAll<HTMLButtonElement>('.exam-option') ?? [])].map(button => button.textContent).join('|'),
        'reading.genericQuestionCount': readingRows.filter(row => /^\d+\. Pytanie \d+/i.test(row.querySelector('.exam-reading-q-text')?.textContent ?? '')).length,
        'reading.genericOptionCount': readingRows.filter(row => [...row.querySelectorAll<HTMLButtonElement>('.exam-option')].map(button => button.textContent?.trim()).join('|') === 'a|b|c').length,
        'reading.selectedFirst': firstReading?.querySelector('.exam-option.selected')?.textContent ?? '',
        'reading.selectedSecond': readingRows[1]?.querySelector('.exam-option.selected')?.textContent ?? '',
        'writing.setChoiceCount': [...document.querySelectorAll<HTMLButtonElement>('.exam-nav-btn')].filter(button => button.textContent?.startsWith('Набор ')).length,
        'writing.taskLabel': document.querySelector('.exam-progress-text')?.textContent ?? '',
        'writing.prompt': document.querySelector('.exam-writing-prompt')?.textContent ?? '',
        'writing.draft': document.querySelector<HTMLTextAreaElement>('.exam-textarea')?.value ?? '',
        'writing.reviewCount': document.querySelectorAll('.exam-writing-review').length,
        'writing.reviewDrafts': [...document.querySelectorAll('.exam-writing-draft')].map(node => node.textContent).join('|'),
        'writing.manualAssessment': document.querySelector('.exam-writing-done')?.textContent ?? '',
        'writing.autoScoreVisible': !!document.querySelector('.exam-score'),
        'media.playing': !!audio && !audio.paused,
        'media.position': audio?.currentTime ?? 0,
        'media.elapsed': Number(time.split(' / ')[0]?.split(':')[0] ?? 0) * 60 + Number(time.split(' / ')[0]?.split(':')[1] ?? 0),
        'media.durationLabel': time.split(' / ')[1] ?? '',
        'media.taskLabel': document.querySelector('.exam-progress-text')?.textContent ?? '',
        'media.selected': selected?.textContent ?? '',
        'media.unavailable': !!audio?.error && !!document.querySelector<HTMLButtonElement>('.player-play-btn')?.disabled,
      };
    });
    snapshot['media.notRewound'] = Number(snapshot['media.position']) >= rememberedPosition;
    snapshot['media.closed'] = audioHandle
      ? await audioHandle.evaluate(audio => {
          const element = audio as HTMLAudioElement;
          return element.paused && !element.isConnected && !element.hasAttribute('src');
        })
      : false;
  };

  const action = (id: string, params: { name: string; type: string }[], execute: (args: ActionArgs) => Promise<void>) => {
    const descriptor = {
      id, version: '1', profiles: ['playmode'], capabilities: [],
      params: params.map(param => ({ ...param, required: true })), validateArgs: () => [],
    };
    registry.registerPlaymodeAction(id, descriptor, async (args: ActionArgs) => {
      await execute(args);
      await refresh();
      return { outcome: 'completed', evidence: 'input' };
    });
  };
  const string = (name: string) => [{ name, type: 'string' }];
  const button = (label: string) => page.getByRole('button', { name: label }).click();
  action('open', [...string('exam'), ...string('module')], async args => {
    await page.goto('/#/exam');
    await expect(page.locator('.exam-list')).toBeVisible();
    await page.locator('.exam-card').filter({ hasText: String(args.exam) }).getByRole('button', { name: 'Начать' }).click();
    const title = ({ grammar: 'Poprawność gramatyczna', listening: 'Rozumienie ze słuchu', reading: 'Rozumienie tekstów pisanych', writing: 'Pisanie' } as Record<string, string>)[String(args.module)];
    if (!title) throw new Error(`Unknown exam module: ${String(args.module)}`);
    await page.locator('.exam-module-card').filter({ hasText: title }).getByRole('button', { name: 'Начать' }).click();
    audioHandle = await page.locator('audio').elementHandle({ timeout: 500 }).catch(() => null);
  });
  action('advance', [{ name: 'count', type: 'number' }], async args => {
    for (let i = 0; i < Number(args.count); i++) await button('Далее →');
  });
  action('advanceTask', [{ name: 'count', type: 'number' }], async args => {
    for (let i = 0; i < Number(args.count); i++) await button('Далее →');
  });
  action('waitTaskSeek', [{ name: 'start', type: 'number' }], async args => {
    await expect.poll(() => audioHandle?.evaluate(audio => (audio as HTMLAudioElement).currentTime)).toBeGreaterThanOrEqual(Number(args.start));
  });
  action('watchProgress', [{ name: 'seconds', type: 'number' }], async args => {
    const start = await audioHandle?.evaluate(audio => (audio as HTMLAudioElement).currentTime);
    if (start === undefined) throw new Error('No recording');
    await expect.poll(() => audioHandle?.evaluate(audio => (audio as HTMLAudioElement).currentTime), { timeout: (Number(args.seconds) + 8) * 1000 })
      .toBeGreaterThan(start + Number(args.seconds));
  });
  action('next', [], async () => { await button('Далее →'); });
  action('back', [], async () => { await button('← Назад'); });
  action('submit', [], async () => { await button('Завершить'); });
  action('enter', string('text'), async args => {
    const input = page.locator('.exam-input');
    if (!(await input.evaluate(element => element === document.activeElement))) await input.click();
    await page.keyboard.type(String(args.text));
  });
  action('press', string('key'), async args => { await page.keyboard.press(String(args.key)); });
  action('play', [], async () => {
    await expect.poll(() => audioHandle?.evaluate(audio => {
      const media = audio as HTMLAudioElement;
      return media.seekable.length ? media.seekable.end(0) : 0;
    })).toBeGreaterThan(305);
    await page.locator('.player-play-btn').click();
    await expect.poll(() => audioHandle?.evaluate(audio => !(audio as HTMLAudioElement).paused)).toBe(true);
  });
  action('rememberPosition', [], async () => { rememberedPosition = Number(snapshot['media.position']); });
  action('choose', string('answer'), async args => {
    await page.locator('.exam-reading-q').first().getByRole('button', { name: String(args.answer), exact: true }).click();
  });
  action('waitImage', [], async () => {
    await expect.poll(() => page.locator('.exam-writing-image').evaluate(image => {
      const element = image as HTMLImageElement;
      return element.complete && element.naturalWidth > 0;
    })).toBe(true);
  });
  action('chooseGrammar', string('answer'), async args => {
    await page.locator('.exam-option').filter({ hasText: String(args.answer) }).click();
  });
  action('chooseReading', [{ name: 'index', type: 'number' }, ...string('answer')], async args => {
    await page.locator('.exam-reading-q').nth(Number(args.index) - 1).getByRole('button', { name: String(args.answer), exact: true }).click();
  });
  action('selectWritingSet', string('set'), async args => {
    await page.getByRole('button', { name: `Набор ${String(args.set)}`, exact: true }).click();
  });
  action('changeWritingSet', [], async () => { await button('Сменить набор'); });
  action('writeDraft', string('text'), async args => { await page.locator('.exam-textarea').fill(String(args.text)); });
  action('nextWriting', [], async () => { await button('Dalej →'); });
  action('submitWriting', [], async () => { await button('Zakończ'); });
  action('seekEnd', [], async () => {
    const progress = page.locator('.player-progress-bar');
    const box = await progress.boundingBox();
    if (!box) throw new Error('Player progress is not laid out');
    const beforeSeek = await audioHandle?.evaluate(audio => (audio as HTMLAudioElement).currentTime);
    await progress.click({ position: { x: box.width - 1, y: box.height / 2 } });
    const afterSeek = await audioHandle?.evaluate(audio => (audio as HTMLAudioElement).currentTime);
    try {
      await expect.poll(() => audioHandle?.evaluate(audio => (audio as HTMLAudioElement).paused)).toBe(true);
    } catch (error) {
      const media = await audioHandle?.evaluate(audio => {
        const element = audio as HTMLAudioElement;
        return { currentTime: element.currentTime, duration: element.duration, paused: element.paused, seeking: element.seeking, readyState: element.readyState, seekable: element.seekable.length ? [element.seekable.start(0), element.seekable.end(0)] : [], buffered: element.buffered.length ? [element.buffered.start(0), element.buffered.end(0)] : [], error: element.error?.code };
      });
      throw new Error(`Segment did not stop: ${JSON.stringify({ media, beforeSeek, afterSeek, bar: box, time: await page.locator('.player-time').textContent(), pageErrors })}`, { cause: error });
    }
  });
  action('leave', [], async () => {
    await page.locator('#nav-training').click();
    await expect(page.locator('.exam-page')).toHaveCount(0);
  });
  action('reenter', [], async () => {
    await page.locator('#nav-exam').click();
    await expect(page.locator('.exam-list')).toBeVisible();
  });
  action('waitMediaError', [], async () => {
    await expect(page.locator('.player-error')).toHaveText('Аудиозапись недоступна');
  });
  for (const [id, resultType] of Object.entries({
    'exam.input': 'string', 'exam.focused': 'bool', 'exam.caret': 'number',
    'exam.score': 'string', 'exam.scoreDetail': 'string', 'exam.reviewAnswer': 'string',
    'exam.firstSelected': 'string', 'exam.listVisible': 'bool',
    'exam.promptImageLoaded': 'bool', 'exam.promptImageAlt': 'string',
    'reading.passageOpening': 'string', 'reading.passageChars': 'number',
    'reading.questionCount': 'number', 'reading.firstQuestion': 'string',
    'reading.firstOptions': 'string', 'reading.genericQuestionCount': 'number',
    'reading.genericOptionCount': 'number', 'reading.selectedFirst': 'string', 'reading.selectedSecond': 'string',
    'writing.setChoiceCount': 'number', 'writing.taskLabel': 'string',
    'writing.prompt': 'string', 'writing.draft': 'string', 'writing.reviewCount': 'number',
    'writing.reviewDrafts': 'string', 'writing.manualAssessment': 'string', 'writing.autoScoreVisible': 'bool',
    'media.playing': 'bool', 'media.position': 'number', 'media.elapsed': 'number',
    'media.durationLabel': 'string', 'media.taskLabel': 'string', 'media.selected': 'string',
    'media.notRewound': 'bool', 'media.closed': 'bool', 'media.unavailable': 'bool',
  })) registry.registerQuery(id, { id, resultType });

  const source = readFileSync(sourceFile, 'utf8');
  const { plan, diagnostics } = compilePlan(source, sourceFile, registry.toDescriptorRegistry());
  if (!plan || diagnostics.some((diagnostic: { severity: string }) => diagnostic.severity === 'error')) {
    throw new Error(JSON.stringify(diagnostics));
  }
  const receipt = await runPlaymodePlan(plan, registry, {
    mode: 'playmode', generation: 0, capabilities: [],
    clock: { kind: 'real', nowUs: () => Date.now() * 1000 },
    resolveQuery: (path: string[]) => snapshot[path.join('.')],
    async nextBoundary() { await refresh(); },
    dispose() {},
  }, { wallTimeoutMs: 65_000 });
  expect(receipt.verdict, JSON.stringify(receipt)).toBe('PASSED');
  expect(receipt.receipts).toHaveLength(1);
  return receipt;
}
