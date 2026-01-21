import { initFirebase, listenAuth, signInGoogle, completeRedirectSignIn } from "./firebase.js";

let fb;
try {
  fb = initFirebase();
} catch (e) {
  const gate = document.querySelector("[data-gate]");
  if (gate) {
    gate.hidden = false;
    const msg = gate.querySelector("[data-gate-msg]");
    if (msg) msg.textContent = "Falta configurar Firebase.";
  }
}

function setGateOpen(open) {
  const gate = document.querySelector("[data-gate]");
  if (!gate) return;
  gate.hidden = !open;
  document.documentElement.style.overflow = open ? "hidden" : "";
}

function setHeaderUser(user) {
  const avatarBtn = document.querySelector("[data-user-avatar]");
  if (!avatarBtn) return;
  const img = avatarBtn.querySelector("img");
  if (!user) {
    avatarBtn.hidden = true;
    if (img) img.removeAttribute("src");
    return;
  }
  const url = user.photoURL || "";
  if (img && url) img.src = url;
  avatarBtn.hidden = !url;
}

async function main() {
  if (!fb) return;

  try {
    await completeRedirectSignIn(fb.auth);
  } catch (e) {
  }

  const btn = document.querySelector('[data-action="signin-gate"]');
  if (btn) {
    btn.addEventListener("click", async () => {
      try {
        await signInGoogle(fb.auth);
      } catch (e) {
      }
    });
  }

  listenAuth(fb.auth, (user) => {
    setGateOpen(!user);
    setHeaderUser(user);
  });
}

main();
