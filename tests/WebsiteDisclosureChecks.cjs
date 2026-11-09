const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const source = fs.readFileSync(path.join(__dirname, '../web/disclosures.js'), 'utf8');

function setup({ reduced = false, hash = '', supported = true } = {}) {
  const events = {};
  const entries = ['changelog', 'privacy'].map(id => {
    let active;
    const animations = [];
    const summary = {
      getBoundingClientRect: () => ({ height: 64 }),
      addEventListener(_, handler) { this.click = handler; },
    };
    const details = {
      id, open: false, dataset: {}, animations, summary,
      querySelector: () => summary,
      getBoundingClientRect() { return { height: active ? active.height : this.open ? 364 : 64 }; },
      scrollIntoView() { this.scrolled = true; },
      animate(frames, options) {
        active = {
          frames, options, height: parseFloat(frames.height[0]),
          cancel() { this.cancelled = true; active = null; },
          finish() { active = null; this.onfinish(); },
        };
        animations.push(active);
        return active;
      },
    };
    if (!supported) delete details.animate;
    return details;
  });
  const footer = { addEventListener(_, handler) { this.click = handler; } };
  const window = {
    location: { hash }, matchMedia: () => ({ matches: reduced }),
    addEventListener(type, handler) { events[type] = handler; },
  };
  vm.runInNewContext(source, {
    document: { querySelectorAll: () => entries, querySelector: () => footer }, window,
  });
  const click = (details, detail = 1) => {
    let prevented = false;
    details.summary.click({ detail, preventDefault() { prevented = true; } });
    assert.equal(prevented, true);
  };
  return { entries, window, events, footer, click };
}

const page = setup();
const [log, privacy] = page.entries;
assert.equal(privacy.open, false);
page.click(privacy);
assert.equal(privacy.open, true);
assert.equal(privacy.animations[0].frames.height.join(','), '64px,364px');
assert.equal(log.open, false, 'Opening privacy does not change the other disclosure');
privacy.animations[0].finish();
page.click(privacy);
assert.equal(privacy.open, true, 'Content stays available during the closing animation');
const closing = privacy.animations[1];
closing.height = 180;
page.click(privacy);
assert.equal(closing.cancelled, true);
assert.equal(privacy.animations[2].frames.height[0], '180px', 'Rapid toggles start at the current height');
privacy.animations[2].finish();
page.click(privacy);
privacy.animations[3].finish();
assert.equal(privacy.open, false);
page.footer.click();
assert.equal(privacy.open, true, 'Footer link also reopens an already visited fragment');
assert.equal(privacy.scrolled, true);

for (const options of [{ reduced: true }, { supported: false }]) {
  const instant = setup(options);
  const item = instant.entries[0];
  instant.click(item);
  assert.equal(item.open, true);
  instant.click(item);
  assert.equal(item.open, false);
  assert.equal(item.animations.length, 0);
}
const keyboard = setup();
keyboard.click(keyboard.entries[0], 0);
assert.equal(keyboard.entries[0].open, true);
assert.equal(keyboard.entries[0].animations.length, 0);
const linked = setup({ hash: '#privacy' });
assert.equal(linked.entries[1].open, true);
linked.window.location.hash = '#changelog';
linked.events.hashchange();
assert.equal(linked.entries[0].open, true);
linked.window.location.hash = '#%invalid';
assert.doesNotThrow(() => linked.events.hashchange());
console.log('Disclosure checks passed: expand/collapse, interrupted motion, keyboard, reduced motion, fallback and fragment links.');
