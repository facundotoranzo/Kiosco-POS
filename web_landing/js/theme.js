var root = document.documentElement;
var favicon = document.getElementById('site-favicon');
var brandMark = document.getElementById('brand-mark');

function getTheme() {
  try {
    return localStorage.getItem('vs_theme') || root.getAttribute('data-theme') || 'app';
  } catch (e) {
    return root.getAttribute('data-theme') || 'app';
  }
}

function applyTheme(theme) {
  var t = theme === 'red' ? 'red' : 'app';
  root.setAttribute('data-theme', t);

  if (brandMark) {
    brandMark.setAttribute('src', t === 'red' ? 'assets/vantasoft-mark.svg' : 'assets/vantasoft-mark-app.svg');
  }
  if (favicon) {
    favicon.setAttribute('href', t === 'red' ? 'assets/vantasoft-favicon.svg' : 'assets/vantasoft-favicon-app.svg');
  }

  document.querySelectorAll('[data-set-theme]').forEach(function (b) {
    b.classList.toggle('is-active', b.getAttribute('data-set-theme') === t);
  });
}

applyTheme(getTheme());

document.addEventListener('click', function (e) {
  var themeBtn = e.target.closest('[data-set-theme]');
  if (!themeBtn) return;
  e.preventDefault();
  try {
    localStorage.setItem('vs_theme', themeBtn.getAttribute('data-set-theme'));
  } catch (err) {
  }
  applyTheme(themeBtn.getAttribute('data-set-theme'));
});

