import { initFirebase, listenAuth, signOutUser, signUpEmail, signInEmail, upsertUserProfile } from "./firebase.js";

function qs(sel) { return document.querySelector(sel); }
function qsa(sel) { return Array.from(document.querySelectorAll(sel)); }

function setModalOpen(name, open) {
  const modal = qs(`[data-modal="${name}"]`);
  if (!modal) return;
  const isOpen = !!open;
  modal.classList.toggle("is-open", isOpen);
  modal.setAttribute("aria-hidden", isOpen ? "false" : "true");
  document.documentElement.style.overflow = isOpen ? "hidden" : "";
}

function bindModals() {
  qsa("[data-open-modal]").forEach((btn) => {
    btn.addEventListener("click", () => setModalOpen(btn.getAttribute("data-open-modal"), true));
  });
  qsa("[data-close-modal]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const root = btn.closest("[data-modal]");
      if (root) setModalOpen(root.getAttribute("data-modal"), false);
    });
  });
}

function pickAvatarTheme() {
  const key = "vs_avatar_theme";
  let t = localStorage.getItem(key);
  if (!t) {
    const options = ["a","b","c","d","e","f"]; t = options[Math.floor(Math.random()*options.length)];
    localStorage.setItem(key, t);
  }
  return t;
}

function renderSignedOut() {
  qsa('[data-auth-ui="signedout"]').forEach((el) => el.hidden = false);
  qsa('[data-auth-ui="signedin"]').forEach((el) => el.hidden = true);
}

function renderSignedIn(user) {
  qsa('[data-auth-ui="signedout"]').forEach((el) => el.hidden = true);
  qsa('[data-auth-ui="signedin"]').forEach((el) => el.hidden = false);

  const nameEl = qs('[data-profile-name]');
  if (nameEl) nameEl.textContent = user.displayName || (user.email ? user.email.split('@')[0] : "Usuario");
  const avatarEl = qs('[data-profile-avatar]');
  if (avatarEl) {
    const theme = pickAvatarTheme();
    avatarEl.className = `profile__avatar avatar--${theme}`;
  }
}

async function main() {
  bindModals();

  let fb;
  try {
    fb = initFirebase();
  } catch (e) {
    return;
  }

  const loginBtn = qs('[data-action="login"]');
  const registerBtn = qs('[data-action="register"]');
  const signoutBtn = qs('[data-action="signout"]');
  const profileBtn = qs('[data-profile-btn]');
  const profileMenu = qs('[data-profile-menu]');

  if (profileBtn && profileMenu) {
    profileBtn.addEventListener('click', () => {
      const open = profileMenu.hidden;
      profileMenu.hidden = !open;
    });
    document.addEventListener('click', (ev) => {
      if (!profileMenu.hidden && !profileBtn.contains(ev.target) && !profileMenu.contains(ev.target)) {
        profileMenu.hidden = true;
      }
    });
  }

  if (signoutBtn) {
    signoutBtn.addEventListener('click', async () => {
      try { await signOutUser(fb.auth); } catch (e) {}
      if (profileMenu) profileMenu.hidden = true;
    });
  }

  if (loginBtn) {
    loginBtn.addEventListener('click', async () => {
      const email = qs('[data-login-email]')?.value?.trim();
      const pass = qs('[data-login-pass]')?.value?.trim();
      if (!email || !pass) return;
      try {
        await signInEmail(fb.auth, email, pass);
        setModalOpen('login', false);
        const anyOpen = qs('.modal.is-open');
        if (anyOpen) {
          anyOpen.classList.remove('is-open');
          anyOpen.setAttribute('aria-hidden', 'true');
        }
        document.documentElement.style.overflow = '';
      } catch (e) {}
    });
  }

  if (registerBtn) {
    registerBtn.addEventListener('click', async () => {
      const name = qs('[data-register-name]')?.value?.trim();
      const email = qs('[data-register-email]')?.value?.trim();
      const pass = qs('[data-register-pass]')?.value?.trim();
      if (!name || !email || !pass) return;
      try {
        const user = await signUpEmail(fb.auth, email, pass, name);
        await upsertUserProfile(fb.db, user, 'client');
        setModalOpen('register', false);
        const anyOpen = qs('.modal.is-open');
        if (anyOpen) {
          anyOpen.classList.remove('is-open');
          anyOpen.setAttribute('aria-hidden', 'true');
        }
        document.documentElement.style.overflow = '';
      } catch (e) {}
    });
  }

  listenAuth(fb.auth, async (user) => {
    if (!user) {
      renderSignedOut();
      return;
    }
    await upsertUserProfile(fb.db, user, 'client');
    renderSignedIn(user);
  });
}

main();
