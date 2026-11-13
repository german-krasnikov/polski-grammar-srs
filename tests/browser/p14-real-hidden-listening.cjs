// Run against an already served immutable compatibility distribution:
// node tests/browser/p14-real-hidden-listening.cjs http://127.0.0.1:4206/ <evidence-dir>
// An owned Chrome profile and browser are removed on every outcome. The practice answer is
// clicked through Playwright, then that process exits before raw CDP activates another real tab.
const { chromium, expect } = require('@playwright/test');
const { spawn } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

async function prepare(endpoint, baseUrl) {
  const browser = await chromium.connectOverCDP(endpoint);
  const page = browser.contexts()[0].pages()[0];
  await page.goto(new URL('/#/exam', baseUrl).href);
  await page.locator('.exam-card').filter({ hasText: 'B1 Certyfikat 2019' })
    .getByRole('button', { name: 'Начать' }).click();
  await page.locator('.exam-module-card').filter({ hasText: 'Rozumienie ze słuchu' })
    .getByRole('button', { name: 'Начать' }).click();
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1 / 5');
  const scripts = await page.locator('script[src]').evaluateAll((elements) => elements.map((element) => element.src));
  await page.evaluate(() => {
    window.__p14VisibilityEvents = [];
    document.addEventListener('visibilitychange', () => {
      window.__p14VisibilityEvents.push({ hidden: document.hidden, at: Date.now() });
    });
  });
  await page.locator('.player-play-btn').click();
  await expect.poll(() => page.locator('audio').evaluate(element => element.paused)).toBe(false);
  const clickedAt = Date.now();
  await page.locator('.exam-reading-q').first().getByRole('button', { name: 'a) w kinie.' }).click();
  const clickReturnedAt = Date.now();
  await expect(page.locator('.exam-reading-q').first().locator('.exam-feedback')).toContainText('Ваш ответ: a) w kinie.');
  return {
    clickedAt,
    clickReturnedAt,
    preparedAt: Date.now(),
    progress: await page.locator('.exam-progress-text').textContent(),
    status: await page.locator('.exam-reading-q').first().locator('.exam-feedback').textContent(),
    scripts,
  };
}

async function check(endpoint, baseUrl, screenshotPath) {
  const browser = await chromium.connectOverCDP(endpoint);
  const page = browser.contexts()[0].pages().find((candidate) => candidate.url().startsWith(baseUrl));
  if (!page) throw new Error('Prepared practice page was not retained');
  await expect(page.locator('.exam-progress-text')).toContainText('Задание 1 / 5');
  await expect(page.locator('.exam-reading-q').first().locator('.exam-feedback')).toHaveCount(0);
  await expect(page.locator('.exam-reading-q').first().locator('.exam-option.selected')).toContainText('a) w kinie.');
  await expect(page.locator('.exam-reading-q').first().getByRole('note')).toContainText('Ответ зафиксирован');
  await expect(page.locator('.exam-reading-q').first().getByRole('note')).toContainText('Ваш ответ: a) w kinie.');
  const options = page.locator('.exam-reading-q').first().locator('.exam-option');
  for (const option of await options.all()) await expect(option).toBeDisabled();
  await expect(page.locator('.exam-reading-q').first().locator('.exam-option.feedback-wrong, .exam-option.feedback-correct')).toHaveCount(0);
  await page.screenshot({ path: screenshotPath });
  return {
    progress: await page.locator('.exam-progress-text').textContent(),
    statusCount: await page.locator('.exam-reading-q').first().locator('.exam-feedback').count(),
    selected: await page.locator('.exam-reading-q').first().locator('.exam-option.selected').textContent(),
    locked: await page.locator('.exam-reading-q').first().getByRole('note').textContent(),
  };
}

function runChild(mode, args) {
  return new Promise((resolve, reject) => {
    const child = spawn(process.execPath, [__filename, mode, ...args], { stdio: ['ignore', 'pipe', 'pipe'] });
    let stdout = '';
    let stderr = '';
    const timeout = setTimeout(() => child.kill('SIGKILL'), 30_000);
    child.stdout.on('data', (chunk) => { stdout += chunk; });
    child.stderr.on('data', (chunk) => { stderr += chunk; });
    child.on('error', reject);
    child.on('exit', (code) => {
      clearTimeout(timeout);
      if (code !== 0) reject(new Error(`${mode} exited ${code}: ${stderr}${stdout}`));
      else {
        try { resolve(JSON.parse(stdout)); } catch (error) { reject(error); }
      }
    });
  });
}

async function parent(baseUrl, evidenceDir) {
  fs.mkdirSync(evidenceDir, { recursive: true });
  const profile = fs.mkdtempSync(path.join(evidenceDir, 'owned-chrome-profile-'));
  const chrome = spawn(chromium.executablePath(), [
    '--headless=new', '--remote-debugging-port=0', `--user-data-dir=${profile}`,
    '--no-first-run', '--no-default-browser-check', 'about:blank',
  ], { stdio: ['ignore', 'ignore', 'pipe'] });
  let chromeStderr = '';
  chrome.stderr.on('data', (chunk) => { chromeStderr += chunk; });
  let socket;
  const pending = new Map();
  let messageId = 0;
  try {
    let port;
    for (let attempt = 0; attempt < 100; attempt += 1) {
      try { port = fs.readFileSync(path.join(profile, 'DevToolsActivePort'), 'utf8').split('\n')[0]; break; }
      catch { await delay(100); }
    }
    if (!port) throw new Error(`Owned Chrome did not start: ${chromeStderr.slice(-1000)}`);
    const endpoint = `http://127.0.0.1:${port}`;
    const version = await (await fetch(`${endpoint}/json/version`)).json();
    socket = new WebSocket(version.webSocketDebuggerUrl);
    await new Promise((resolve, reject) => { socket.onopen = resolve; socket.onerror = reject; });
    socket.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if (!message.id) return;
      const slot = pending.get(message.id);
      pending.delete(message.id);
      if (message.error) slot.reject(new Error(JSON.stringify(message.error)));
      else slot.resolve(message.result);
    };
    const send = (method, params = {}, sessionId) => new Promise((resolve, reject) => {
      const id = ++messageId;
      pending.set(id, { resolve, reject });
      socket.send(JSON.stringify({ id, method, params, ...(sessionId ? { sessionId } : {}) }));
    });
    const original = (await send('Target.getTargets')).targetInfos.find((target) => target.type === 'page');
    if (!original) throw new Error('Owned Chrome has no page target');
    const sessionId = (await send('Target.attachToTarget', { targetId: original.targetId, flatten: true })).sessionId;
    const observe = async () => {
      const response = await send('Runtime.evaluate', {
        expression: '({hidden:document.hidden,progress:document.querySelector(".exam-progress-text")?.textContent,statusCount:document.querySelectorAll(".exam-feedback").length,visibilityEvents:window.__p14VisibilityEvents})',
        returnByValue: true,
      }, sessionId);
      return response.result.value;
    };

    const prepared = await runChild('--prepare', [endpoint, baseUrl]);
    const childExitedAt = Date.now();
    const before = await observe();
    const beforeAt = Date.now();
    if (before.hidden || before.progress !== 'Задание 1 / 5' || before.statusCount !== 1) {
      throw new Error(`Wrong prepared state: ${JSON.stringify(before)}`);
    }
    if (!prepared.scripts.some((script) => script.endsWith('/originWasmComposeApp.js'))) {
      throw new Error('Prepared page did not load the frozen Wasm compatibility runtime');
    }
    const second = await send('Target.createTarget', { url: 'about:blank', newWindow: false });
    const createdAt = Date.now();
    await send('Target.activateTarget', { targetId: second.targetId });
    const activatedAt = Date.now();
    let hidden;
    for (let attempt = 0; attempt < 50; attempt += 1) {
      hidden = await observe();
      if (hidden.hidden) break;
      await delay(20);
    }
    const hiddenAfterClickMs = hidden.visibilityEvents?.find((event) => event.hidden)?.at - prepared.clickedAt;
    if (!hidden.hidden || !Number.isFinite(hiddenAfterClickMs) || hiddenAfterClickMs >= 1_900) {
      throw new Error(`No timely real hidden transition: ${hiddenAfterClickMs}ms ${JSON.stringify({ prepared, childExitedAt, beforeAt, createdAt, activatedAt, hidden })}`);
    }
    await delay(2_400);
    const whileHidden = await observe();
    await send('Target.activateTarget', { targetId: original.targetId });
    let returned;
    for (let attempt = 0; attempt < 50; attempt += 1) {
      returned = await observe();
      if (!returned.hidden) break;
      await delay(20);
    }
    const asserted = await runChild('--check', [endpoint, baseUrl, path.join(evidenceDir, 'real-hidden-after.png')]);
    if (whileHidden.progress !== 'Задание 1 / 5' || whileHidden.statusCount !== 0 ||
        returned.hidden || returned.progress !== 'Задание 1 / 5' || returned.statusCount !== 0) {
      throw new Error(`Feedback advanced or remained visible: ${JSON.stringify({ whileHidden, returned })}`);
    }
    const result = { status: 'PASS', browser: version.Browser, prepared, before, hiddenAfterClickMs,
      hidden, whileHidden, returned, asserted };
    fs.writeFileSync(path.join(evidenceDir, 'real-hidden.json'), `${JSON.stringify(result, null, 2)}\n`);
    console.log(JSON.stringify(result));
  } finally {
    if (socket) socket.close();
    chrome.kill('SIGTERM');
    await delay(150);
    if (chrome.exitCode === null) chrome.kill('SIGKILL');
    fs.rmSync(profile, { recursive: true, force: true });
  }
}

const mode = process.argv[2];
const args = process.argv.slice(3);
const action = mode === '--prepare' ? prepare(...args)
  : mode === '--check' ? check(...args)
    : parent(mode, args[0]);
action.then((result) => {
  if (mode === '--prepare' || mode === '--check') process.stdout.write(JSON.stringify(result), () => process.exit(0));
}).catch((error) => { console.error(error); process.exitCode = 1; });
