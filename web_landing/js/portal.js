import {
  initFirebase,
  listenAuth,
  signInGoogle,
  signOutUser,
  upsertUserProfile,
  ensureConversation,
  getSubscription,
  requestPlanChange,
  listenMessages,
  sendMessage
} from "./firebase.js";

import "./theme.js";

function qs(sel) {
  return document.querySelector(sel);
}

function setHidden(sel, hidden) {
  const el = qs(sel);
  if (!el) return;
  el.hidden = hidden;
}

function fmtTime(ts) {
  try {
    const d = ts?.toDate ? ts.toDate() : null;
    if (!d) return "";
    return d.toLocaleString();
  } catch (e) {
    return "";
  }
}

function renderMessages(container, userUid, messages) {
  container.innerHTML = "";
  messages.forEach((m) => {
    const mine = m.authorUid === userUid;
    const row = document.createElement("div");
    row.className = "msg" + (mine ? " msg--me" : "");

    const bubble = document.createElement("div");
    bubble.className = "msg__bubble";

    if (m.text) {
      const t = document.createElement("div");
      t.className = "msg__text";
      t.textContent = m.text;
      bubble.appendChild(t);
    }

    if (m.file?.url) {
      const a = document.createElement("a");
      a.className = "msg__file";
      a.href = m.file.url;
      a.target = "_blank";
      a.rel = "noreferrer";
      a.textContent = m.file.name || "archivo";
      bubble.appendChild(a);
    }

    const meta = document.createElement("div");
    meta.className = "msg__meta";
    meta.textContent = fmtTime(m.createdAt);
    bubble.appendChild(meta);

    row.appendChild(bubble);
    container.appendChild(row);
  });

  container.scrollTop = container.scrollHeight;
}

let unsub = null;

async function main() {
  let fb;
  try {
    fb = initFirebase();
  } catch (e) {
    setHidden('[data-auth="missing"]', false);
    return;
  }

  const signinBtn = qs('[data-action="signin"]');
  const signoutBtn = qs('[data-action="signout"]');

  if (signinBtn) {
    signinBtn.addEventListener("click", async () => {
      try {
        await signInGoogle(fb.auth);
      } catch (e) {
      }
    });
  }

  if (signoutBtn) {
    signoutBtn.addEventListener("click", async () => {
      try {
        await signOutUser(fb.auth);
      } catch (e) {
      }
    });
  }

  const messagesEl = qs("[data-chat-messages]");
  const inputEl = qs("[data-chat-input]");
  const sendBtn = qs("[data-chat-send]");
  const subEl = qs("[data-subscription-status]");
  const planSelect = qs("[data-plan-select]");
  const planBtn = qs("[data-request-plan]");

  listenAuth(fb.auth, async (user) => {
    setHidden('[data-auth="signedout"]', !!user);
    setHidden('[data-auth="signedin"]', !user);
    if (signinBtn) signinBtn.hidden = !!user;
    if (signoutBtn) signoutBtn.hidden = !user;

    if (unsub) {
      unsub();
      unsub = null;
    }

    if (!user) return;

    await upsertUserProfile(fb.db, user, "client");
    await ensureConversation(fb.db, user);

    if (subEl) {
      try {
        const sub = await getSubscription(fb.db, user.uid);
        if (!sub) {
          subEl.textContent = "Sin plan activo";
        } else {
          const plan = sub.planKey || "PLAN";
          const status = sub.status || "unknown";
          subEl.textContent = `Plan: ${plan} · Estado: ${status}`;
        }
      } catch (e) {
        subEl.textContent = "Sin datos";
      }
    }

    if (planBtn) {
      planBtn.onclick = async () => {
        try {
          const v = planSelect ? planSelect.value : "";
          if (!v) return;
          await requestPlanChange(fb.db, user, v);
          alert("Solicitud enviada. Te respondemos por el chat.");
        } catch (e) {
          alert("No se pudo enviar la solicitud.");
        }
      };
    }

    const nameEl = qs("[data-user-name]");
    const emailEl = qs("[data-user-email]");
    if (nameEl) nameEl.textContent = user.displayName || "Cuenta";
    if (emailEl) emailEl.textContent = user.email || "";

    if (messagesEl) {
      unsub = listenMessages(fb.db, user.uid, (msgs) => renderMessages(messagesEl, user.uid, msgs));
    }

    async function sendCurrent(text) {
      const clean = String(text || "").trim();
      if (!clean) return;
      const payload = {
        authorUid: user.uid,
        authorName: user.displayName || "",
        authorEmail: user.email || "",
        text: clean,
        side: "client"
      };

      await sendMessage(fb.db, user.uid, payload);
    }

    if (sendBtn) {
      sendBtn.onclick = async () => {
        try {
          const text = inputEl?.value || "";
          if (inputEl) inputEl.value = "";
          await sendCurrent(text);
        } catch (e) {
        }
      };
    }

    if (inputEl) {
      inputEl.onkeydown = async (ev) => {
        if (ev.key !== "Enter") return;
        ev.preventDefault();
        try {
          const text = inputEl.value || "";
          inputEl.value = "";
          await sendCurrent(text);
        } catch (e) {
        }
      };
    }
  });
}

main();
