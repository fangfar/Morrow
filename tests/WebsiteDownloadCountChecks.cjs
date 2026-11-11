const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');

const script = fs.readFileSync('web/download-count.js', 'utf8');
const asset = (name, download_count) => ({ name, download_count });
const release = assets => ({ draft: false, prerelease: false, assets });

async function run(pages) {
  const display = { hidden: true, textContent: '' };
  let calls = 0;
  vm.runInNewContext(script, {
    document: { querySelector: () => display },
    fetch: async () => {
      const result = pages[calls++];
      if (result instanceof Error) throw result;
      return { ok: true, json: async () => result };
    },
  });
  await new Promise(resolve => setImmediate(resolve));
  return { display, calls };
}

(async () => {
  const first = Array(100).fill(release([]));
  first[0] = release([asset('Morrow-3.1.1.apk', 9), asset('Morrow-OCR-1.0.1.apk', 4)]);
  const result = await run([first, [release([asset('CourseLens-2.7.4-vc16.apk', 6)]), { prerelease: true, assets: [asset('Morrow-4.0.apk', 99)] }]]);
  assert.equal(result.calls, 2);
  assert.equal(result.display.textContent, '累计下载 15 次');
  assert.equal(result.display.hidden, false);
  assert.equal((await run([new Error('offline')])).display.hidden, true);
  console.log('Website download count checks passed');
})().catch(error => { console.error(error); process.exitCode = 1; });
