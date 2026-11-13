// A real visibility transition needs a browser-owned tab switch. Playwright's newPage and
// bringToFront keep both pages visible in headless mode, so the click and final assertions run
// in short-lived Playwright children while the parent activates a second tab through raw CDP.
// Usage: node tests/browser/p16-real-hidden-rive.cjs <served-url> <evidence-dir>
const { chromium, expect } = require('@playwright/test');
const { spawn } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

async function prepare(endpoint, baseUrl) {
  const browser = await chromium.connectOverCDP(endpoint);
  const page = browser.contexts()[0].pages()[0];
  await page.goto(baseUrl);
  await page.getByRole('button', { name: /^(Перейти к заданию|Показать ответ|Проверить и показать ответ)/ }).first().waitFor({ timeout: 5_000 });
  const intro = page.getByRole('button', { name: 'Перейти к заданию' });
  if (await intro.count()) await intro.click();
  await page.getByRole('button', { name: 'Показать ответ', exact: true }).click();
  await page.evaluate(() => {
    window.__p16VisibilityEvents = [];
    document.addEventListener('visibilitychange', () => {
      window.__p16VisibilityEvents.push({ hidden: document.hidden, at: Date.now() });
    });
  });
  const clickedAt = Date.now();
  await page.getByRole('button', { name: /2 Вспомнил/ }).click();
  const clickReturnedAt = Date.now();
  await expect(page.locator('#polski-rive-overlay')).toBeVisible();
  return { clickedAt, clickReturnedAt, preparedAt: Date.now(),
    scripts: await page.locator('script[src]').evaluateAll(nodes => nodes.map(node => node.src)) };
}

async function check(endpoint, baseUrl, screenshot) {
  const browser = await chromium.connectOverCDP(endpoint);
  const page = browser.contexts()[0].pages().find(candidate => candidate.url().startsWith(baseUrl));
  if (!page) throw new Error('Prepared training page was not retained');
  await expect(page.locator('#polski-rive-overlay')).toBeHidden();
  await page.screenshot({ path: screenshot });
  return { hidden: await page.evaluate(() => document.hidden), overlayVisible: await page.locator('#polski-rive-overlay').isVisible() };
}

function child(mode, args) {
  return new Promise((resolve, reject) => {
    const processChild = spawn(process.execPath, [__filename, mode, ...args], { stdio: ['ignore', 'pipe', 'pipe'] });
    let stdout = ''; let stderr = '';
    const timeout = setTimeout(() => processChild.kill('SIGKILL'), 30_000);
    processChild.stdout.on('data', chunk => { stdout += chunk; });
    processChild.stderr.on('data', chunk => { stderr += chunk; });
    processChild.on('error', reject);
    processChild.on('exit', code => {
      clearTimeout(timeout);
      if (code !== 0) reject(new Error(`${mode} exited ${code}: ${stderr}${stdout}`));
      else { try { resolve(JSON.parse(stdout)); } catch (error) { reject(error); } }
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
  chrome.stderr.on('data', chunk => { chromeStderr += chunk; });
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
    socket.onmessage = event => {
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
    const original = (await send('Target.getTargets')).targetInfos.find(target => target.type === 'page');
    if (!original) throw new Error('Owned Chrome has no page');
    const sessionId = (await send('Target.attachToTarget', { targetId: original.targetId, flatten: true })).sessionId;
    const observe = async () => {
      const response = await send('Runtime.evaluate', {
        expression: '({hidden:document.hidden,display:document.getElementById("polski-rive-overlay")?.style.display,events:window.__p16VisibilityEvents})',
        returnByValue: true,
      }, sessionId);
      return response.result.value;
    };
    const prepared = await child('--prepare', [endpoint, baseUrl]);
    const childExitedAt = Date.now();
    const before = await observe();
    if (before.hidden || before.display !== 'block') throw new Error(`Cue not active before backgrounding: ${JSON.stringify(before)}`);
    const second = await send('Target.createTarget', { url: 'about:blank', newWindow: false });
    await send('Target.activateTarget', { targetId: second.targetId });
    let hidden;
    for (let attempt = 0; attempt < 50; attempt += 1) {
      hidden = await observe();
      if (hidden.hidden && hidden.display === 'none') break;
      await delay(20);
    }
    const hiddenAfterClickMs = hidden.events?.find(event => event.hidden)?.at - prepared.clickedAt;
    if (!hidden.hidden || hidden.display !== 'none' || !Number.isFinite(hiddenAfterClickMs) || hiddenAfterClickMs >= 2_600) {
      throw new Error(`Cue not cancelled by real hidden transition: ${JSON.stringify({ prepared, childExitedAt, before, hiddenAfterClickMs, hidden })}`);
    }
    await send('Target.activateTarget', { targetId: original.targetId });
    const asserted = await child('--check', [endpoint, baseUrl, path.join(evidenceDir, 'returned.png')]);
    const result = { status: 'PASS', browser: version.Browser, prepared, before, hiddenAfterClickMs, hidden, asserted };
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
const action = mode === '--prepare' ? prepare(...args) : mode === '--check' ? check(...args) : parent(mode, args[0]);
action.then(result => {
  if (mode === '--prepare' || mode === '--check') process.stdout.write(JSON.stringify(result), () => process.exit(0));
}).catch(error => { console.error(error); process.exitCode = 1; });
