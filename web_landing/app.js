(function () {
  var header = document.querySelector('[data-header]');
  var progress = document.querySelector('[data-scrollbar]');
  var root = document.documentElement;
  var favicon = document.getElementById('site-favicon');
  var brandMark = document.getElementById('brand-mark');

  function onScroll() {
    var y = window.scrollY || 0;
    if (header) header.classList.toggle('is-scrolled', y > 6);
    var h = document.documentElement.scrollHeight - window.innerHeight;
    var p = h > 0 ? Math.min(1, Math.max(0, y / h)) : 0;
    if (progress) progress.style.transform = 'scaleX(' + p + ')';
  }

  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  function getTheme() {
    try {
      return localStorage.getItem('vs_theme') || root.getAttribute('data-theme') || 'app';
    } catch (e) {
      return root.getAttribute('data-theme') || 'app';
    }
  }

  function setTheme(theme) {
    try {
      localStorage.setItem('vs_theme', theme);
    } catch (e) {
    }
    applyTheme(theme);
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

  var reveal = document.querySelectorAll('[data-reveal]');
  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (e) {
          if (e.isIntersecting) {
            e.target.classList.add('is-visible');
            io.unobserve(e.target);
          }
        });
      },
      { threshold: 0.12 }
    );
    reveal.forEach(function (el) {
      io.observe(el);
    });
  } else {
    reveal.forEach(function (el) {
      el.classList.add('is-visible');
    });
  }

  document.querySelectorAll('[data-ripple]').forEach(function (el) {
    el.addEventListener('pointerdown', function (ev) {
      var r = document.createElement('span');
      r.className = 'ripple';
      var rect = el.getBoundingClientRect();
      r.style.left = ev.clientX - rect.left + 'px';
      r.style.top = ev.clientY - rect.top + 'px';
      el.appendChild(r);
      r.addEventListener('animationend', function () {
        r.remove();
      });
    });
  });

  function openModal(key) {
    var m = document.querySelector('[data-modal="' + key + '"]');
    if (!m) return;
    m.classList.add('is-open');
    m.setAttribute('aria-hidden', 'false');
    document.documentElement.style.overflow = 'hidden';
  }

  function closeModal(m) {
    if (!m) return;
    m.classList.remove('is-open');
    m.setAttribute('aria-hidden', 'true');
    document.documentElement.style.overflow = '';
  }

  document.addEventListener('click', function (e) {
    var themeBtn = e.target.closest('[data-set-theme]');
    if (themeBtn) {
      e.preventDefault();
      setTheme(themeBtn.getAttribute('data-set-theme'));
      return;
    }

    var openBtn = e.target.closest('[data-open-modal]');
    if (openBtn) {
      e.preventDefault();
      var msg = openBtn.getAttribute('data-prefill');
      if (msg) {
        var modal = document.querySelector('[data-modal="contacto"]');
        if (modal) {
          var ta = modal.querySelector('textarea');
          if (ta) ta.value = msg;
        }
      }
      openModal(openBtn.getAttribute('data-open-modal'));
      return;
    }

    var tplBtn = e.target.closest('[data-open-template]');
    if (tplBtn) {
      e.preventDefault();
      var card = e.target.closest('[data-tpl-src]');
      if (!card) return;
      var src = card.getAttribute('data-tpl-src');
      var titleEl = card.querySelector('.tpl__name');
      var title = titleEl ? titleEl.textContent : 'Vista previa';

      var frame = document.querySelector('[data-template-frame]');
      var t = document.querySelector('[data-template-title]');
      var open = document.querySelector('[data-template-open]');
      if (t) t.textContent = title;
      if (open) open.setAttribute('href', src);
      if (frame) frame.setAttribute('src', src);
      openModal('template');
      return;
    }

    var planBtn = e.target.closest('[data-plan-button]');
    if (planBtn) {
      e.preventDefault();
      var plan = planBtn.getAttribute('data-plan') || 'PLAN';
      if (isPlanPaid(plan)) {
        alert('Descarga simulada. Cuando conectemos el backend, este botón descarga el ZIP activado.');
        return;
      }

      setCheckoutPlan(plan);
      openModal('checkout');
      return;
    }

    var closeBtn = e.target.closest('[data-close-modal]');
    if (closeBtn) {
      e.preventDefault();
      var modal = e.target.closest('.modal');
      if (modal && modal.getAttribute('data-modal') === 'template') {
        var frame = modal.querySelector('[data-template-frame]');
        if (frame) frame.setAttribute('src', '');
      }
      closeModal(modal);
      return;
    }

    var action = e.target.closest('[data-action]');
    if (action) {
      e.preventDefault();
      var a = action.getAttribute('data-action');
      if (a === 'download-trial' || a === 'whatsapp' || a === 'pay') {
        alert('Este es un mockup local. Cuando lo subamos, conectamos descarga y pago.');
      }
    }
  });

  document.addEventListener('keydown', function (e) {
    if (e.key !== 'Escape') return;
    var open = document.querySelector('.modal.is-open');
    if (open) {
      if (open.getAttribute('data-modal') === 'template') {
        var frame = open.querySelector('[data-template-frame]');
        if (frame) frame.setAttribute('src', '');
      }
      closeModal(open);
    }
  });

  function bindTilt(el) {
    var strength = 10;
    var currentRaf = 0;

    function move(ev) {
      if (currentRaf) cancelAnimationFrame(currentRaf);
      currentRaf = requestAnimationFrame(function () {
        var rect = el.getBoundingClientRect();
        var cx = rect.left + rect.width / 2;
        var cy = rect.top + rect.height / 2;
        var dx = (ev.clientX - cx) / (rect.width / 2);
        var dy = (ev.clientY - cy) / (rect.height / 2);
        dx = Math.max(-1, Math.min(1, dx));
        dy = Math.max(-1, Math.min(1, dy));
        var rx = (-dy * strength).toFixed(2);
        var ry = (dx * strength).toFixed(2);
        el.style.transform = 'rotateX(' + rx + 'deg) rotateY(' + ry + 'deg)';
      });
    }

    function leave() {
      if (currentRaf) cancelAnimationFrame(currentRaf);
      el.style.transform = '';
    }

    el.addEventListener('pointermove', move);
    el.addEventListener('pointerleave', leave);
  }

  document.querySelectorAll('[data-tilt]').forEach(function (el) {
    bindTilt(el);
  });

  var checkoutPayBtn = document.querySelector('[data-action="pay"]');
  if (checkoutPayBtn) {
    checkoutPayBtn.addEventListener('click', function (e) {
      e.preventDefault();
      var plan = getCheckoutPlan();
      if (!plan) {
        alert('Seleccioná un plan.');
        return;
      }

      setPlanPaid(plan, true);
      renderPlanButtons();
      var open = document.querySelector('.modal.is-open');
      if (open) closeModal(open);
      alert('Pago simulado. El botón ahora queda como Descargar.');
    });
  }

  function storeKey() {
    return 'km_paid_plans_v1';
  }

  function readPaidMap() {
    try {
      var raw = localStorage.getItem(storeKey());
      if (!raw) return {};
      var obj = JSON.parse(raw);
      return obj && typeof obj === 'object' ? obj : {};
    } catch (e) {
      return {};
    }
  }

  function writePaidMap(map) {
    try {
      localStorage.setItem(storeKey(), JSON.stringify(map));
    } catch (e) {
    }
  }

  function isPlanPaid(plan) {
    var map = readPaidMap();
    return Boolean(map[plan]);
  }

  function setPlanPaid(plan, value) {
    var map = readPaidMap();
    map[plan] = Boolean(value);
    writePaidMap(map);
  }

  function setCheckoutPlan(plan) {
    try {
      localStorage.setItem('km_checkout_plan', plan);
    } catch (e) {
    }
    var label = document.querySelector('[data-checkout-plan]');
    if (label) label.textContent = plan;
  }

  function getCheckoutPlan() {
    try {
      return localStorage.getItem('km_checkout_plan');
    } catch (e) {
      return null;
    }
  }

  function renderPlanButtons() {
    document.querySelectorAll('[data-plan-button]').forEach(function (btn) {
      var plan = btn.getAttribute('data-plan') || '';
      if (!plan) return;
      if (isPlanPaid(plan)) {
        btn.textContent = 'Descargar';
      } else {
        btn.textContent = 'Comprar';
      }
    });
  }

  renderPlanButtons();

  function setActiveFilter(key) {
    document.querySelectorAll('[data-filter]').forEach(function (b) {
      b.classList.toggle('is-active', b.getAttribute('data-filter') === key);
    });
  }

  function applyFilter(key) {
    document.querySelectorAll('[data-tpl]').forEach(function (card) {
      var k = card.getAttribute('data-tpl');
      var show = key === 'all' || key === k;
      card.classList.toggle('is-hidden', !show);
    });
  }

  document.addEventListener('click', function (e) {
    var fb = e.target.closest('[data-filter]');
    if (!fb) return;
    e.preventDefault();
    var key = fb.getAttribute('data-filter') || 'all';
    setActiveFilter(key);
    applyFilter(key);
  });
})();
