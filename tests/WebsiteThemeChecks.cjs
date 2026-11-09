const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const source = fs.readFileSync(require('node:path').join(__dirname, '../web/theme.js'), 'utf8');
for (const systemDark of [false, true]) for (const saved of [null, 'light', 'dark', 'invalid']) for (const blocked of [false, true]) {
  let stored = saved, ready, click, changed;
  const root = { dataset: {} }, attrs = {}, meta = {};
  const button = { setAttribute(k, v) { attrs[k] = v; }, addEventListener(_, fn) { click = fn; } };
  const system = { matches: systemDark, addEventListener(_, fn) { changed = fn; } };
  const context = {
    document: { documentElement: root, querySelector(s) { return s === '.theme-toggle' ? button : meta; }, addEventListener(_, fn) { ready = fn; } },
    window: { matchMedia() { return system; } },
    localStorage: { getItem() { if (blocked) throw Error('blocked'); return stored; }, setItem(_, v) { if (blocked) throw Error('blocked'); stored = v; } },
  };
  vm.runInNewContext(source, context);
  ready();
  const preference = blocked ? null : saved;
  const dark = preference === 'dark' || (preference !== 'light' && systemDark);
  assert.equal(root.dataset.theme, dark ? 'dark' : 'light');
  assert.equal(attrs['aria-pressed'], String(dark));
  system.matches = !systemDark; changed();
  const followsSystem = !['dark', 'light'].includes(preference);
  assert.equal(root.dataset.theme, (followsSystem ? !systemDark : dark) ? 'dark' : 'light');
  const prior = root.dataset.theme;
  click();
  assert.notEqual(root.dataset.theme, prior);
  assert.equal(attrs['aria-pressed'], String(root.dataset.theme === 'dark'));
  assert.equal(meta.content, root.dataset.theme === 'dark' ? '#201F1C' : '#FAF6EF');
  assert.equal(button.title, attrs['aria-label']);
  if (!blocked) assert.equal(stored, root.dataset.theme);
  const chosen = root.dataset.theme;
  system.matches = !system.matches; changed();
  assert.equal(root.dataset.theme, chosen);
}
console.log('Theme checks passed: system preference, saved choice, switching, accessibility and unavailable storage.');
