import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { expect, test, type Page } from '@playwright/test';
import { runExamPlaytest } from './exam-playtest-host';

const publication = readFileSync(resolve('dsl/exam-published.core-data'), 'utf8');
const papers = [...publication.matchAll(/routeId: ([\w-]+)\s+examRef: [^\n]+\s+displayTitle: "([^"]+)"\s+sourceDir: ([^\n]+)/g)]
  .map(([, routeId, title, sourceDir]) => ({ routeId, title, sourceDir }));

type SourceRow = Record<string, string>;
function sourceRows(sourceDir: string, file: string, kind: string): SourceRow[] {
  const source = readFileSync(resolve(sourceDir, `${file}.core-data`), 'utf8');
  return [...source.matchAll(new RegExp(`^${kind} \\{\\n([\\s\\S]*?)^\\}`, 'gm'))].map(([, body]) =>
    Object.fromEntries([...body.matchAll(/^  (\w+): (.+)$/gm)].map(([, key, raw]) =>
      [key, raw.startsWith('"') ? JSON.parse(raw) as string : raw])));
}

const normalized = (value: string) => value.replace(/\s+/g, ' ').trim();
const expectedBranch = process.env.KOTLIN_SPIKE_BRANCH;

test.beforeEach(async ({ page }) => {
  if (expectedBranch === 'js') {
    await page.addInitScript(() => {
      const validate = WebAssembly.validate;
      WebAssembly.validate = function (bytes: BufferSource): boolean {
        if (bytes instanceof Uint8Array && bytes.length < 200 && bytes[0] === 0 && bytes[1] === 97) return false;
        return validate.call(WebAssembly, bytes);
      };
    });
  }
});

test('publication source selects sixteen distinct papers', () => {
  expect(papers).toHaveLength(16);
  expect(new Set(papers.map(paper => paper.routeId)).size).toBe(16);
  expect(papers.some(paper => paper.routeId === 'b2-2022-06')).toBe(false);
});

test('DSL source to web projection retains the B2 June 2024 printed choices', async ({ page }) => {
  await runExamPlaytest(page, resolve('dsl/exam-web/published-content-audit.playtest'));
});

test('a selected wrong answer remains distinct from an unanswered review slot', async ({ page }) => {
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B2 Certyfikat 2024-06' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'II. Rozumienie tekstów pisanych' })
    .getByRole('button', { name: 'Начать' }).click();
  const chosen = 'a) protestujący rolnicy będą eskortowani na lotnisko przez policję.';
  await page.locator('.exam-reading-q').first().getByRole('button', { name: chosen }).click();
  while (await page.getByRole('button', { name: 'Далее →' }).count()) {
    await page.getByRole('button', { name: 'Далее →' }).click();
  }
  await page.getByRole('button', { name: 'Завершить' }).click();
  const first = page.locator('.exam-review-item').first();
  const second = page.locator('.exam-review-item').nth(1);
  await expect(first).toContainText(chosen);
  await expect(first).not.toContainText('[—]');
  await expect(second).toContainText('[—]');
});

test('the published reading choice remains usable on a narrow screen', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 667 });
  await page.goto('/#/exam');
  await page.locator('.exam-card').filter({ hasText: 'B2 Certyfikat 2024-06' }).getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'II. Rozumienie tekstów pisanych' })
    .getByRole('button', { name: 'Начать' }).click();
  const choice = page.locator('.exam-reading-q').first().getByRole('button', {
    name: 'a) protestujący rolnicy będą eskortowani na lotnisko przez policję.',
  });
  await expect(choice).toBeVisible();
  await choice.click();
  await expect(choice).toHaveClass(/selected/);
});

async function inspectVisibleContent(page: Page, part: string, paper: string, sourceId: string) {
  const defects = await page.evaluate(() => {
    const empty = (element: Element) => !(element.textContent?.trim() || element.getAttribute('aria-label')?.trim());
    const content = document.querySelector('.exam-context, .exam-reading-questions, .exam-listening-questions, .exam-writing-prompt');
    return {
      title: [...document.querySelectorAll('.exam-title')].filter(empty).length,
      controls: [...document.querySelectorAll('.exam-option, .exam-nav-btn:not(:disabled)')].filter(empty).length,
      prompts: [...document.querySelectorAll('.exam-reading-q-text')].filter(empty).length,
      content: !content || empty(content),
      questions: document.querySelectorAll('.exam-reading-q').length,
      options: document.querySelectorAll('.exam-option').length,
      inputs: document.querySelectorAll('.exam-input, .exam-textarea').length,
      missingInputHints: [...document.querySelectorAll<HTMLInputElement>('.exam-input, .exam-textarea')]
        .filter(input => !(input.getAttribute('aria-label')?.trim() || input.placeholder.trim())).length,
    };
  });
  const location = `${paper}/${part}/${sourceId}`;
  expect(defects.title, `${location}: empty heading`).toBe(0);
  expect(defects.controls, `${location}: empty button`).toBe(0);
  expect(defects.prompts, `${location}: empty question label`).toBe(0);
  expect(defects.content, `${location}: empty task content`).toBe(false);
  expect(defects.missingInputHints, `${location}: input without hint`).toBe(0);
  expect(defects.questions + defects.options + defects.inputs, `${location}: no answer control`).toBeGreaterThan(0);
}

for (const paper of papers) {
  test(`${paper.routeId}: all authored writing tasks show their paper guidance`, async ({ page }) => {
    test.setTimeout(120_000);
    const writingTasks = sourceRows(paper.sourceDir, 'writing', 'WRITING_TASK');
    const coverFile = existsSync(resolve(paper.sourceDir, 'modules.core-data')) ? 'modules' : 'exam-modules';
    const cover = sourceRows(paper.sourceDir, coverFile, 'EXAM_MODULE').find(row => row.module === 'writing');
    expect(cover?.writingGuidance?.trim(), paper.routeId).toBeTruthy();
    await page.goto('/#/exam');
    await page.locator('.exam-card').filter({ hasText: paper.title }).getByRole('button', { name: 'Начать' }).click();
    await page.locator('.exam-module-card').filter({ hasText: 'IV. Pisanie' }).getByRole('button', { name: 'Начать' }).click();
    const sets = [...new Set(writingTasks.map(task => task.setId).filter(Boolean))];
    let seen = 0;
    for (const [setIndex, setId] of sets.entries()) {
      await page.getByRole('button', { name: `Набор ${setId}`, exact: true }).click();
      const tasks = writingTasks.filter(task => task.setId === setId);
      for (const [index, task] of tasks.entries()) {
        await page.locator('.exam-writing-details summary').click();
        await expect(page.locator('.exam-writing-details')).toHaveAttribute('open');
        const expectedInstruction = `${task.instruction}\n\n${cover!.writingGuidance}`;
        expect(normalized(await page.locator('.exam-writing-instruction').innerText()), task.taskId).toBe(normalized(expectedInstruction));
        expect(normalized(await page.locator('.exam-writing-prompt').innerText()), task.taskId).toBe(normalized(task.promptText));
        if (task.wordLimit) await expect(page.locator('.exam-word-limit')).toHaveText(`Limit: ${task.wordLimit} słów`);
        seen++;
        if (index < tasks.length - 1) await page.getByRole('button', { name: 'Dalej →' }).click();
      }
      if (setIndex < sets.length - 1) await page.getByRole('button', { name: 'Сменить набор' }).click();
    }
    expect(seen, paper.routeId).toBe(writingTasks.length);
  });

  test(`${paper.routeId}: every task renders a prompt and nonempty controls`, async ({ page }) => {
    test.setTimeout(120_000);
    const grammarTests = sourceRows(paper.sourceDir, 'grammar-tests', 'GRAMMAR_TEST');
    const grammarItems = sourceRows(paper.sourceDir, 'grammar-items', 'GRAMMAR_ITEM');
    const grammarBankTokens = sourceRows(paper.sourceDir, 'grammar-bank-tokens', 'GRAMMAR_BANK_TOKEN');
    const listeningTasks = sourceRows(paper.sourceDir, 'listening-tasks', 'LISTENING_TASK');
    const listeningQuestions = sourceRows(paper.sourceDir, 'listening-questions', 'LISTENING_QUESTION');
    const readingPassages = sourceRows(paper.sourceDir, 'reading-passages', 'READING_PASSAGE');
    const readingQuestions = sourceRows(paper.sourceDir, 'reading-questions', 'READING_QUESTION');
    const writingTasks = sourceRows(paper.sourceDir, 'writing', 'WRITING_TASK');
    const coverFile = existsSync(resolve(paper.sourceDir, 'modules.core-data')) ? 'modules' : 'exam-modules';
    const writingCover = sourceRows(paper.sourceDir, coverFile, 'EXAM_MODULE').find(cover => cover.module === 'writing');
    const writingGuidance = writingCover?.writingGuidance;
    const orderedGrammar = grammarTests.flatMap(task => grammarItems.filter(item => item.testRef === task.testId));
    await page.goto('/#/exam');
    await page.locator('.exam-card').filter({ hasText: paper.title }).getByRole('button', { name: 'Начать' }).click();
    await expect(page.locator(`script[src="origin${expectedBranch === 'js' ? 'Js' : 'Wasm'}ComposeApp.js"]`)).toHaveCount(1);

    for (const [part, label] of [
      ['listening', 'I. Rozumienie ze słuchu'],
      ['reading', 'II. Rozumienie tekstów pisanych'],
      ['grammar', 'III. Poprawność gramatyczna'],
      ['writing', 'IV. Pisanie'],
    ] as const) {
      await page.locator('.exam-module-card').filter({ hasText: label }).getByRole('button', { name: 'Начать' }).click();
      if (part === 'writing') {
        const sets = [...new Set(writingTasks.map(task => task.setId).filter(Boolean))];
        for (const [setIndex, setId] of (sets.length ? sets : ['']).entries()) {
          if (setId) await page.getByRole('button', { name: `Набор ${setId}`, exact: true }).click();
          const tasks = writingTasks.filter(task => (task.setId ?? '') === setId);
          for (const [index, task] of tasks.entries()) {
            await inspectVisibleContent(page, part, paper.routeId, task.taskId);
            await page.locator('.exam-writing-details summary').click();
            await expect(page.locator('.exam-writing-details')).toHaveAttribute('open');
            const expectedInstruction = writingGuidance ? `${task.instruction}\n\n${writingGuidance}` : task.instruction;
            expect(normalized(await page.locator('.exam-writing-instruction').innerText()), `${paper.routeId}/${task.taskId}: authored writing instruction and cover guidance`)
              .toBe(normalized(expectedInstruction));
            expect(normalized(await page.locator('.exam-writing-prompt').innerText()), `${paper.routeId}/${task.taskId}`).toBe(normalized(task.promptText));
            if (task.promptImageUrl) await expect(page.locator('.exam-writing-image')).toHaveAttribute('alt', task.promptImageAlt);
            if (index < tasks.length - 1) await page.getByRole('button', { name: 'Dalej →' }).click();
          }
          if (setIndex < sets.length - 1) await page.getByRole('button', { name: 'Сменить набор' }).click();
          else {
            await page.getByRole('button', { name: 'Zakończ' }).click();
            await expect(page.locator('.exam-writing-review')).toHaveCount(tasks.length);
          }
        }
      } else {
        const tasks = part === 'grammar' ? orderedGrammar : part === 'listening' ? listeningTasks : readingPassages;
        for (const [index, task] of tasks.entries()) {
          const taskId = task.itemId ?? task.taskId ?? task.passageId;
          await inspectVisibleContent(page, part, paper.routeId, taskId);
          if (part === 'grammar') {
            expect(normalized(await page.locator('.exam-context').innerText()), `${paper.routeId}/${taskId}`).toBe(normalized(task.context));
            if (task.questionType === 'choose_form') {
              const rendered = await page.locator('.exam-option').allTextContents();
              expect(rendered, `${paper.routeId}/${taskId}: printed choices`)
                .toEqual(task.options.split('|').map((option, optionIndex) => `${String.fromCharCode(65 + optionIndex)}. ${option}`));
            } else if (task.questionType === 'word_bank') {
              const bank = grammarBankTokens.filter(token => token.testRef === task.testRef);
              const rendered = await page.locator('.exam-word-bank .exam-option').allTextContents();
              expect(rendered, `${paper.routeId}/${taskId}: printed bank`).toEqual(bank.map(token => token.text));
            }
          } else {
            const questions = part === 'listening'
              ? listeningQuestions.filter(question => question.taskRef === taskId)
              : readingQuestions.filter(question => question.passageRef === taskId);
            const rendered = await page.locator('.exam-reading-q-text').allTextContents();
            expect(rendered, `${paper.routeId}/${taskId}: question IDs ${questions.map(q => q.questionId).join(', ')}`)
              .toEqual(questions.map((question, questionIndex) => `${questionIndex + 1}. ${question.questionText}`));
            await expect(page.locator('.exam-reading-q')).toHaveCount(questions.length);
            for (const [questionIndex, question] of questions.entries()) {
              const row = page.locator('.exam-reading-q').nth(questionIndex);
              const options = await row.locator('.exam-option').allTextContents();
              if (question.options) {
                expect(options, `${paper.routeId}/${question.questionId}: printed choices`).toEqual(question.options.split('|'));
              } else {
                expect(options.length + await row.locator('.exam-input').count(),
                  `${paper.routeId}/${question.questionId}: no answer control`).toBeGreaterThan(0);
              }
            }
          }
          if (index < tasks.length - 1) await page.getByRole('button', { name: 'Далее →' }).click();
        }
        await page.getByRole('button', { name: 'Завершить' }).click();
        const expected = part === 'grammar' ? grammarItems.length : part === 'listening' ? listeningQuestions.length : readingQuestions.length;
        await expect(page.locator('.exam-review-item')).toHaveCount(expected);
      }
      await page.getByRole('button', { name: '← К модулям' }).click();
    }
  });
}
