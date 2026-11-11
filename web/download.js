(() => {
  const feedback = document.querySelector('.download-feedback');
  const status = document.querySelector('[data-download-status]');
  const frame = document.querySelector('[data-download-frame]');

  feedback.querySelector('button').addEventListener('click', () => {
    feedback.hidden = true;
  });

  document.querySelectorAll('[data-download]').forEach(button => {
    button.addEventListener('click', event => {
      if (event.ctrlKey || event.metaKey || event.shiftKey || event.altKey || event.button > 0) return;
      event.preventDefault();
      const url = new URL(button.href);
      if (url.protocol !== 'https:' || url.hostname !== 'github.com' || url.port
          || url.username || url.password || url.search || url.hash
          || !url.pathname.startsWith('/fangfar/Morrow/releases/download/')
          || !url.pathname.endsWith('.apk')) {
        feedback.hidden = false;
        status.textContent = '下载地址暂不可用，请手动下载。';
        return;
      }
      feedback.hidden = false;
      status.textContent = '已请求下载，请查看浏览器下载提示。若未开始，可手动下载。';
      frame.src = url.href;
    });
  });
})();
