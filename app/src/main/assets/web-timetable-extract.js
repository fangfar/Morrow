(function (id) {
  const job = { id, pages: null, error: '' };
  window.__morrowCourseRead = job;
  let size = 0;
  const pages = [];
  function add(url, html, first) {
    size += html.length;
    if (size > 1000000) throw Error('页面过大，请进入单独的个人课表页');
    const page = { url, html };
    if (first) pages.unshift(page); else pages.push(page);
  }
  async function request(url, options) {
    if (url.origin !== location.origin) throw Error('课表接口不在当前网站');
    const abort = new AbortController();
    const timeout = setTimeout(() => abort.abort(), 10000);
    try {
      const response = await fetch(url.href, { ...options, signal: abort.signal, credentials: 'same-origin', redirect: 'error' });
      if (!response.ok) throw Error('课表接口未返回数据');
      const length = Number(response.headers.get('content-length'));
      if (length > 1000000) throw Error('课表数据过大');
      const reader = response.body.getReader(), decoder = new TextDecoder();
      let text = '', bytes = 0;
      try {
        while (true) {
          const part = await reader.read();
          if (part.done) break;
          bytes += part.value.byteLength;
          if (bytes > 1000000) throw Error('课表数据过大');
          text += decoder.decode(part.value, { stream: true });
        }
        return text + decoder.decode();
      } finally { await reader.cancel(); }
    } finally { clearTimeout(timeout); }
  }
  async function read(doc) {
    if (!doc || !doc.documentElement) return;
    const url = new URL(doc.URL);
    const raw = (doc.body?.innerText || '').trim();
    if (raw.startsWith('{') || raw.startsWith('[')) {
      try { JSON.parse(raw); add(url.href, raw, true); return; } catch (ignored) {}
    }
    const copy = doc.documentElement.cloneNode(true);
    const selects = doc.querySelectorAll('select'), copied = copy.querySelectorAll('select');
    selects.forEach((select, index) => {
      Array.from(copied[index].options).forEach((option, i) => {
        if (select.options[i].selected) option.setAttribute('selected', 'selected'); else option.removeAttribute('selected');
      });
    });
    add(url.href, copy.outerHTML, false);
    const year = doc.querySelector('#xnm'), term = doc.querySelector('#xqm');
    // Only known read-only schedule endpoints; no credentials or native bridge are exposed.
    if (url.pathname.includes('/kbcx/') && year && term && /^\d{4}$/.test(year.value) && /^\d{1,2}$/.test(term.value)) {
      try {
        const endpoint = new URL('xskbcx_cxXsgrkb.html?gnmkdm=N2151', url);
        const text = await request(endpoint, { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body: new URLSearchParams({ xnm: year.value, xqm: term.value, kzlx: 'ck' }).toString() });
        if (Array.isArray(JSON.parse(text).kbList)) add(url.href, text, true);
      } catch (error) { job.error = '课表接口读取未成功，可尝试切换课表列表模式'; }
    } else if (url.pathname.includes('/student/courseSelect/thisSemesterCurriculum/')) {
      try {
        const endpoint = new URL(url.pathname.split('/student/courseSelect/thisSemesterCurriculum/')[0] + '/student/courseSelect/thisSemesterCurriculum/callback', url);
        const text = await request(endpoint, { method: 'GET' });
        if (Array.isArray(JSON.parse(text).dateList)) add(url.href, text, true);
      } catch (error) { job.error = 'URP 课表接口读取未成功，请确认当前学期已开放'; }
    }
    for (const frame of doc.querySelectorAll('iframe,frame')) {
      let child = null;
      try { child = frame.contentDocument; } catch (ignored) {}
      if (child) await read(child);
    }
  }
  read(document).then(() => { job.pages = pages; }).catch(() => { job.error = '页面过大或无法读取，请进入单独的个人课表页'; job.pages = []; });
})(__MORROW_REQUEST_ID__);
