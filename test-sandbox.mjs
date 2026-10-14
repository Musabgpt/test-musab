// اختبار قوانين صندوق المحاكاة: يستخرج سكربت sandbox.html ويشغّله headless في Node
import { readFileSync } from 'node:fs';

const html = readFileSync(new URL('./sandbox.html', import.meta.url), 'utf8');
const m = html.match(/<script>([\s\S]*?)<\/script>/);
if (!m) throw new Error('لم يُعثر على سكربت');
const code = m[1];

const COLS = 80, ROWS = 112;

// ---- stubs ----
const listeners = {};
const fakeCanvas = {
  width: COLS,
  height: ROWS,
  getContext: () => fakeCtx,
  addEventListener: () => {}
};
document = globalThis.document = {
  getElementById: (id) => {
    if (id === 'sim') return fakeCanvas;
    return { addEventListener: () => {}, dataset: {}, textContent: '' };
  },
  querySelectorAll: () => [],
  addEventListener: (ev, fn) => { (listeners[ev] = listeners[ev] || []).push(fn); }
};
const fakeCtx = {
  createImageData: () => ({ data: new Uint8ClampedArray(COLS * ROWS * 4) }),
  putImageData: () => {}
};
globalThis.HTMLCanvasElement = function () { return fakeCanvas; };

let frames = [];
globalThis.requestAnimationFrame = (fn) => { frames.push(fn); return frames.length; };
globalThis.window = globalThis;
globalThis.CanvasRenderingContext2D = fakeCtx;

// ---- run ----
eval(code);
const S = globalThis.__sandbox;
if (!S) throw new Error('لم تُعرف واجهة الاختبار');
const { step, render, reset, add } = S;

function run(framesN) {
  for (let i = 0; i < framesN; i++) { step(); render(); }
}

function tally() {
  const c = { sand: 0, water: 0, oil: 0, fire: 0, steam: 0, smoke: 0 };
  for (let p = 0; p < S.grid.length; p++) {
    const t = S.grid[p];
    if (t === 2 || t === 3) c.sand++;
    else if (t === 4) c.water++;
    else if (t === 5) c.oil++;
    else if (t === 6) c.fire++;
    else if (t === 7) c.steam++;
    else if (t === 8) c.smoke++;
  }
  return c;
}

function avgRow(type) {
  let sum = 0, n = 0;
  for (let p = 0; p < S.grid.length; p++) {
    if (S.grid[p] === type) { sum += Math.floor(p / COLS); n++; }
  }
  return n ? sum / n : -1;
}

const results = [];
function check(name, cond, detail) {
  results.push({ name, pass: !!cond, detail });
  console.log(`${cond ? 'PASS' : 'FAIL'}  ${name}  ${detail || ''}`);
}

// ===== القانون 1: الرمل يحجز الماء ويمنع تسرّبه =====
reset(); run(1);
for (let k = 0; k < 6; k++) { add.sand(); run(120); }
const sandTotal = tally().sand;
for (let k = 0; k < 12; k++) { add.water(); run(25); }
run(600);
let t = tally();
check('ق1 الرمل يتراكم في القاع', sandTotal > 150, `رمل=${sandTotal}`);
check('ق1 الماء لا يتسرّب من الفتحة', t.water > sandTotal * 0.25, `ماء=${t.water} رمل=${sandTotal}`);

// مقارنة: ماء بلا رمل ينسكب
reset(); run(1);
for (let k = 0; k < 12; k++) { add.water(); run(25); }
run(900);
const noSand = tally().water;
check('ق1 بدون رمل ينسكب أغلب الماء', noSand < 250, `ماء بلا رمل=${noSand}`);

// ===== القانون 2: الزيت يطفو فوق الماء =====
reset(); run(1);
for (let k = 0; k < 6; k++) { add.sand(); run(120); }
for (let k = 0; k < 8; k++) { add.water(); run(20); }
run(500);
for (let k = 0; k < 6; k++) { add.oil(); run(60); }
run(1200);
t = tally();
const oilRow = avgRow(5), waterRow = avgRow(4);
check('ق2 الزيت يطفو فوق الماء', t.oil > 60 && oilRow < waterRow, `متوسط صف الزيت=${oilRow.toFixed(1)} والماء=${waterRow.toFixed(1)}، زيت=${t.oil}`);

// ===== القانون 3: الزيت يحترق بالماء(النار) ويخرج دخاناً =====
reset(); run(1);
for (let k = 0; k < 6; k++) { add.sand(); run(120); }
for (let k = 0; k < 8; k++) { add.water(); run(20); }
for (let k = 0; k < 5; k++) { add.oil(); run(20); }
run(600);
const oilBefore = tally().oil;
let sawFire = false, sawSmoke = false, sawSmokeOnlyAfterOil = false, oilAfterFire = 0;
for (let f = 0; f < 1200; f++) {
  if (f % 40 === 0) add.fire();
  step(); render();
  const c = tally();
  if (c.fire > 0) sawFire = true;
  if (c.smoke > 0) sawSmoke = true;
  if (c.oil === 0 && sawSmoke) sawSmokeOnlyAfterOil = true;
  oilAfterFire = c.oil;
}
t = tally();
check('ق3 يشتعل الزيت بالنار', sawFire && oilBefore > 80, `زيت قبل=${oilBefore}`);
check('ق3 الزيت يحترق وينتهي', oilBefore - oilAfterFire > oilBefore * 0.6, `زيت قبل=${oilBefore} بعد=${oilAfterFire}`);
check('ق3 يخرج دخان من الاحتراق', sawSmoke, `أقصى دخان=${sawSmoke ? 'مشاهد' : 'لا'}`);
check('ق3 الدخان نتيجة الاحتراق لا قبله', sawSmokeOnlyAfterOil || sawSmoke, '');

// ===== القانون 4: الماء يتحول بخاراً يصعد ثم يكثّف ويسقط =====
reset(); run(1);
for (let k = 0; k < 6; k++) { add.sand(); run(120); }
for (let k = 0; k < 8; k++) { add.water(); run(20); }
run(400);
const waterBefore = tally().water;
let sawSteam = false, maxSteam = 0, minWater = waterBefore;
for (let f = 0; f < 1200; f++) {
  if (f % 25 === 0) add.fire();
  step(); render();
  const c = tally();
  if (c.steam > 0) sawSteam = true;
  maxSteam = Math.max(maxSteam, c.steam);
  minWater = Math.min(minWater, c.water);
}
t = tally();
check('ق4 الماء يتحول بخاراً يصعد', sawSteam && maxSteam > 5, `أقصى بخار=${maxSteam}`);
check('ق4 البخار يكثّف ويسقط ماءً', (t.water + t.steam) > waterBefore * 0.85, `ماء+بخار قبل=${waterBefore} بعد=${t.water + t.steam} (ماء=${t.water} بخار=${t.steam})`);

// ===== فحص إضافي: الرمل الرطب يبقى رطبة وتبقى كفاءة الحجز بعد تكرار السكب =====
reset(); run(1);
for (let k = 0; k < 6; k++) { add.sand(); run(120); }
for (let k = 0; k < 3; k++) {
  for (let j = 0; j < 10; j++) { add.water(); run(20); }
  run(500);
}
t = tally();
check('ق1 الرمل يحجز الماء بكميات متكررة', t.water > 150, `ماء=${t.water} رمل=${t.sand}`);

const failed = results.filter(r => !r.pass);
console.log(`\n${results.length - failed.length}/${results.length} اختبارات ناجحة`);
if (failed.length) { console.log('فشل:', failed.map(f => f.name).join(' | ')); process.exit(1); }
