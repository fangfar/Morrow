const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const source = fs.readFileSync('web/download.js', 'utf8');
const apk = 'https://github.com/fangfar/Morrow/releases/download/v3.1.1/Morrow-3.1.1.apk';
const button = { href: apk, addEventListener: (_, fn) => { button.click = fn; } };
const close = { addEventListener: (_, fn) => { close.click = fn; } };
const feedback = { hidden: true, querySelector: () => close };
const status = {};
const frame = {};
vm.runInNewContext(source, {
  document: {
    querySelectorAll: () => [button],
    querySelector: selector => ({ '.download-feedback': feedback, '[data-download-status]': status, '[data-download-frame]': frame })[selector],
  },
  URL,
});
const click = () => ({ button: 0, preventDefault() { this.prevented = true; } });
const normal = click();
button.click(normal);
assert.equal(normal.prevented, true);
assert.equal(frame.src, apk);
assert.equal(feedback.hidden, false);
assert.match(status.textContent, /手动下载/);
close.click();
assert.equal(feedback.hidden, true);
const modified = { ...click(), ctrlKey: true };
button.click(modified);
assert.equal(modified.prevented, undefined);
button.href = 'https://example.com/app.apk';
const invalid = click();
button.click(invalid);
assert.equal(invalid.prevented, true);
assert.equal(frame.src, apk);
console.log('Website download checks passed');
require('./WebsiteThemeChecks.cjs');
require('./WebsiteDisclosureChecks.cjs');
