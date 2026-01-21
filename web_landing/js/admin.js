import {
  initFirebase,
  listenAuth,
  signInGoogle,
  signOutUser,
  upsertUserProfile,
  isAdminEmail,
  getSubscription,
  setSubscription,
  listenConversations,
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

function renderMessages(container, adminUid, messages) {
  container.innerHTML = "";
  messages.forEach((m) => {
    const mine = m.authorUid === adminUid;
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
    meta.textContent = (m.authorEmail ? m.authorEmail + " · " : "") + fmtTime(m.createdAt);
    bubble.appendChild(meta);

    row.appendChild(bubble);
    container.appendChild(row);
  });
  container.scrollTop = container.scrollHeight;
}

let selectedClientUid = null;
let unsubMessages = null;
let unsubConvos = null;

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
  const listEl = qs("[data-admin-list]");
  const titleEl = qs("[data-admin-chat-title]");
  const messagesEl = qs("[data-admin-messages]");
  const inputEl = qs("[data-admin-input]");
  const sendBtn = qs("[data-admin-send]");
  const subEl = qs("[data-admin-sub]");
  const planSel = qs("[data-admin-plan]");
  const statusSel = qs("[data-admin-status]");
  const saveBtn = qs("[data-admin-save]");

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

  listenAuth(fb.auth, async (user) => {
    setHidden('[data-auth="forbidden"]', true);
    setHidden('[data-auth="signedin"]', true);
    if (signinBtn) signinBtn.hidden = !!user;
    if (signoutBtn) signoutBtn.hidden = !user;

    if (unsubConvos) {
      unsubConvos();
      unsubConvos = null;
    }
    if (unsubMessages) {
      unsubMessages();
      unsubMessages = null;
    }

    selectedClientUid = null;
    if (titleEl) titleEl.textContent = "Seleccioná un cliente";
    if (messagesEl) messagesEl.innerHTML = "";
    if (listEl) listEl.innerHTML = "";

    if (!user) return;

    if (!isAdminEmail(fb.cfg, user.email)) {
      setHidden('[data-auth="forbidden"]', false);
      return;
    }

    await upsertUserProfile(fb.db, user, "admin");
    setHidden('[data-auth="signedin"]', false);

    function selectClient(convo) {
      selectedClientUid = convo.clientUid;
      if (titleEl) titleEl.textContent = convo.clientName || convo.clientEmail || convo.clientUid;

      if (unsubMessages) {
        unsubMessages();
        unsubMessages = null;
      }

      if (messagesEl) {
        unsubMessages = listenMessages(fb.db, selectedClientUid, (msgs) => renderMessages(messagesEl, user.uid, msgs));
      }

      if (subEl) {
        subEl.textContent = "Cargando…";
        getSubscription(fb.db, selectedClientUid)
          .then((sub) => {
            if (!sub) {
              subEl.textContent = "Sin plan";
              if (planSel) planSel.value = "MENSUAL_2";
              if (statusSel) statusSel.value = "past_due";
              return;
            }
            const plan = sub.planKey || "MENSUAL_2";
            const status = sub.status || "active";
            subEl.textContent = `Plan: ${plan} · Estado: ${status}`;
            if (planSel) planSel.value = plan;
            if (statusSel) statusSel.value = status;
          })
          .catch(() => {
            subEl.textContent = "Sin datos";
          });
      }
    }

    unsubConvos = listenConversations(fb.db, (items) => {
      if (!listEl) return;
      listEl.innerHTML = "";
      items.forEach((c) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "list__item" + (selectedClientUid === c.clientUid ? " is-active" : "");
        btn.innerHTML = `
          <div class="list__top">
            <div class="list__name">${(c.clientName || "Cliente").replace(/</g, "&lt;")}</div>
            <div class="list__time">${fmtTime(c.updatedAt)}</div>
          </div>
          <div class="list__meta">${(c.clientEmail || c.clientUid || "").replace(/</g, "&lt;")}</div>
        `;
        btn.addEventListener("click", () => selectClient(c));
        listEl.appendChild(btn);
      });

      if (!selectedClientUid && items[0]) selectClient(items[0]);
    });

    async function sendCurrent(text) {
      if (!selectedClientUid) return;
      const clean = String(text || "").trim();
      if (!clean) return;
      const payload = {
        authorUid: user.uid,
        authorName: user.displayName || "",
        authorEmail: user.email || "",
        text: clean,
        side: "admin"
      };

      await sendMessage(fb.db, selectedClientUid, payload);
    }

    if (saveBtn) {
      saveBtn.onclick = async () => {
        try {
          if (!selectedClientUid) return;
          const plan = planSel ? planSel.value : "";
          const status = statusSel ? statusSel.value : "";
          await setSubscription(fb.db, selectedClientUid, { planKey: plan, status });
          if (subEl) subEl.textContent = `Plan: ${plan} · Estado: ${status}`;
          alert("Guardado");
        } catch (e) {
          alert("No se pudo guardar");
        }
      };
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
