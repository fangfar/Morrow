const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const script = fs.readFileSync('app/src/main/assets/web-timetable-extract.js', 'utf8').replace('__MORROW_REQUEST_ID__', '7');
function doc(url, text = '', html = '<html>合成课表</html>', fields = {}, frames = []) {
  const copy = { outerHTML: html, querySelectorAll: () => [] };
  return { URL: url, body: { innerText: text }, documentElement: { cloneNode: () => copy }, querySelector: key => fields[key], querySelectorAll: key => key === 'iframe,frame' ? frames : [] };
}
async function read(document, response = '{}', fail = false) {
  const calls = [], window = {};
  const context = { document, window, location: new URL(document.URL), URL, URLSearchParams, TextDecoder, AbortController, setTimeout, clearTimeout, fetch: async (url, options) => {
    calls.push({ url, options });
    if (fail) throw Error('synthetic failure');
    let done = false;
    return { ok: true, headers: { get: () => '0' }, body: { getReader: () => ({ read: async () => {
      if (done) return { done: true };
      done = true; return { done: false, value: new TextEncoder().encode(response) };
    }, cancel: async () => {} }) } };
  } };
  vm.runInNewContext(script, context);
  for (let i = 0; i < 100 && window.__morrowCourseRead.pages === null; i++) await new Promise(resolve => setImmediate(resolve));
  assert.notEqual(window.__morrowCourseRead.pages, null);
  return { ...window.__morrowCourseRead, calls };
}
(async () => {
  const plain = await read(doc('https://school.example/table'));
  assert.equal(plain.pages.length, 1); assert.equal(plain.calls.length, 0);
  const data = '{"kbList":[{"kcmc":"合成数学"}]}';
  const zf = await read(doc('http://school.example:8080/jwglxt/kbcx/xskbcx_cxXskbcxIndex.html', '', '<html>课表</html>', { '#xnm': { value: '2026' }, '#xqm': { value: '3' } }), data);
  assert.equal(zf.pages[0].html, data); assert.equal(zf.calls.length, 1);
  assert.equal(zf.calls[0].url, 'http://school.example:8080/jwglxt/kbcx/xskbcx_cxXsgrkb.html?gnmkdm=N2151');
  assert.equal(zf.calls[0].options.body, 'xnm=2026&xqm=3&kzlx=ck');
  assert.equal(zf.calls[0].options.credentials, 'same-origin'); assert.equal(zf.calls[0].options.redirect, 'error');
  const urp = await read(doc('https://school.example/student/courseSelect/thisSemesterCurriculum/index'), '{"dateList":[]}');
  assert.equal(urp.calls[0].url, 'https://school.example/student/courseSelect/thisSemesterCurriculum/callback');
  const json = await read(doc('https://school.example/callback', data));
  assert.equal(json.pages[0].html, data); assert.equal(json.calls.length, 0);
  const child = doc('https://school.example/frame');
  const framed = await read(doc('https://school.example/', '', '<html>parent</html>', {}, [{ contentDocument: child }, { get contentDocument() { throw Error('cross-origin'); } }]));
  assert.equal(framed.pages.length, 2);
  const failed = await read(doc('https://school.example/kbcx/index', '', '<html>课表</html>', { '#xnm': { value: '2026' }, '#xqm': { value: '3' } }), '', true);
  assert.equal(failed.pages.length, 1); assert.ok(failed.error);
  const oversized = await read(doc('https://school.example/', '', 'x'.repeat(1000001)));
  assert.equal(oversized.pages.length, 0); assert.ok(oversized.error);
  console.log('PASS: system endpoints, selected term, same-origin session, JSON, frames, failures and size limits');
})().catch(error => { console.error(error); process.exitCode = 1; });
