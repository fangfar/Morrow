(() => {
  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)');
  const disclosures = new Map();

  for (const details of document.querySelectorAll('.support-info details')) {
    const summary = details.querySelector('summary');
    let animation;
    let expanded = details.open;

    function setOpen(open, animate = false) {
      const start = details.getBoundingClientRect().height;
      animation?.cancel();
      animation = null;
      expanded = open;
      details.dataset.expanded = String(open);
      if (!animate || reduce.matches || !details.animate) {
        details.open = open;
        return;
      }

      details.open = true;
      const end = open ? details.getBoundingClientRect().height : summary.getBoundingClientRect().height;
      animation = details.animate({ height: [`${start}px`, `${end}px`] }, {
        duration: 200,
        easing: 'cubic-bezier(0.23, 1, 0.32, 1)',
      });
      animation.onfinish = () => {
        details.open = open;
        animation = null;
      };
    }

    summary.addEventListener('click', event => {
      event.preventDefault();
      setOpen(!(animation ? expanded : details.open), event.detail !== 0);
    });
    disclosures.set(details.id, () => {
      setOpen(true);
      details.scrollIntoView({ block: 'start' });
    });
  }

  function openHash() {
    let id;
    try { id = decodeURIComponent(window.location.hash.slice(1)); } catch (_) { return; }
    disclosures.get(id)?.();
  }
  window.addEventListener('hashchange', openHash);
  // Clicking the same fragment again does not emit hashchange.
  document.querySelector('a[href="#privacy"]').addEventListener('click', () => disclosures.get('privacy')());
  openHash();
})();
