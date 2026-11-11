(() => {
  const display = document.querySelector('[data-download-count]');

  async function showDownloadCount() {
    let total = 0;
    let page = 1;
    let releases;
    do {
      const response = await fetch(`https://api.github.com/repos/fangfar/Morrow/releases?per_page=100&page=${page++}`, { credentials: 'omit' });
      if (!response.ok) throw new Error('Release request failed');
      releases = await response.json();
      if (!Array.isArray(releases)) throw new Error('Invalid releases');
      for (const release of releases) {
        if (release.draft || release.prerelease || !Array.isArray(release.assets)) continue;
        for (const asset of release.assets) {
          if (/^(Morrow|CourseLens)-\d.*\.apk$/i.test(asset.name) && Number.isSafeInteger(asset.download_count) && asset.download_count >= 0) {
            total += asset.download_count;
          }
        }
      }
    } while (releases.length === 100);
    display.textContent = `累计下载 ${total.toLocaleString('zh-CN')} 次`;
    display.hidden = false;
  }

  showDownloadCount().catch(() => {});
})();
