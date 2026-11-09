(() => {
  const root = document.documentElement;
  let preference;
  try { preference = localStorage.getItem('site-theme'); } catch (_) {}
  const system = window.matchMedia('(prefers-color-scheme: dark)');
  root.dataset.theme = preference === 'dark' || (preference !== 'light' && system.matches) ? 'dark' : 'light';
  function sync() {
    const button = document.querySelector('.theme-toggle');
    const dark = root.dataset.theme === 'dark';
    if (button) {
      button.setAttribute('aria-pressed', String(dark));
      button.title = dark ? '切换为浅色模式' : '切换为深色模式';
      button.setAttribute('aria-label', button.title);
    }
    document.querySelector('meta[name="theme-color"]').content = dark ? '#201F1C' : '#FAF6EF';
  }
  document.addEventListener('DOMContentLoaded', () => {
    sync();
    document.querySelector('.theme-toggle').addEventListener('click', () => {
      root.dataset.theme = root.dataset.theme === 'dark' ? 'light' : 'dark';
      preference = root.dataset.theme;
      try { localStorage.setItem('site-theme', preference); } catch (_) {}
      sync();
    });
  });
  system.addEventListener('change', () => {
    if (preference === 'dark' || preference === 'light') return;
    root.dataset.theme = system.matches ? 'dark' : 'light';
    sync();
  });
})();
