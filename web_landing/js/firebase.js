import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
  getAuth,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithPopup,
  signInWithRedirect,
  getRedirectResult,
  signOut
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import {
  getFirestore,
  serverTimestamp,
  doc,
  getDoc,
  setDoc,
  updateDoc,
  collection,
  addDoc,
  query,
  orderBy,
  onSnapshot,
  limit
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

function getConfig() {
  return window.VS_FIREBASE;
}

export function initFirebase() {
  const cfg = getConfig();
  if (!cfg || !cfg.apiKey || !cfg.projectId) {
    throw new Error("FIREBASE_CONFIG_MISSING");
  }

  const app = initializeApp(cfg);
  const auth = getAuth(app);
  const db = getFirestore(app);

  return { app, auth, db, cfg };
}

export function createGoogleProvider() {
  return new GoogleAuthProvider();
}

export async function signInGoogle(auth) {
  const provider = createGoogleProvider();
  const useRedirect =
    /Android|iPhone|iPad|iPod/i.test(navigator.userAgent) ||
    (window.matchMedia && window.matchMedia("(max-width: 980px)").matches);

  if (useRedirect) {
    await signInWithRedirect(auth, provider);
    return null;
  }

  const res = await signInWithPopup(auth, provider);
  return res.user;
}

export async function completeRedirectSignIn(auth) {
  const res = await getRedirectResult(auth);
  return res ? res.user : null;
}

export async function signOutUser(auth) {
  await signOut(auth);
}

export function listenAuth(auth, cb) {
  return onAuthStateChanged(auth, cb);
}

export async function upsertUserProfile(db, user, role) {
  const refUser = doc(db, "users", user.uid);
  const snap = await getDoc(refUser);
  const base = {
    uid: user.uid,
    email: user.email || "",
    displayName: user.displayName || "",
    photoURL: user.photoURL || "",
    updatedAt: serverTimestamp()
  };

  if (!snap.exists()) {
    await setDoc(refUser, {
      ...base,
      role,
      createdAt: serverTimestamp()
    });
    return;
  }

  await setDoc(refUser, base, { merge: true });
}

export function isAdminEmail(cfg, email) {
  const list = Array.isArray(cfg?.adminEmails) ? cfg.adminEmails : [];
  return !!email && list.map((e) => String(e).toLowerCase()).includes(String(email).toLowerCase());
}

export function convoDoc(db, clientUid) {
  return doc(db, "conversations", clientUid);
}

export async function ensureConversation(db, clientUser) {
  const refConvo = convoDoc(db, clientUser.uid);
  const snap = await getDoc(refConvo);
  if (snap.exists()) return;

  await setDoc(refConvo, {
    clientUid: clientUser.uid,
    clientEmail: clientUser.email || "",
    clientName: clientUser.displayName || "",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
    lastMessageAt: null
  });
}

export async function sendMessage(db, clientUid, message) {
  const refConvo = convoDoc(db, clientUid);
  const refMessages = collection(refConvo, "messages");

  await addDoc(refMessages, {
    ...message,
    createdAt: serverTimestamp()
  });

  await setDoc(refConvo, { updatedAt: serverTimestamp(), lastMessageAt: serverTimestamp() }, { merge: true });
}

export function listenMessages(db, clientUid, cb) {
  const refConvo = convoDoc(db, clientUid);
  const refMessages = collection(refConvo, "messages");
  const q = query(refMessages, orderBy("createdAt", "desc"), limit(60));
  return onSnapshot(q, (snap) => {
    const items = [];
    snap.forEach((d) => items.push({ id: d.id, ...d.data() }));
    items.reverse();
    cb(items);
  });
}

export function listenConversations(db, cb) {
  const ref = collection(db, "conversations");
  const q = query(ref, orderBy("updatedAt", "desc"), limit(200));
  return onSnapshot(q, (snap) => {
    const items = [];
    snap.forEach((d) => items.push({ id: d.id, ...d.data() }));
    cb(items);
  });
}

export async function getSubscription(db, clientUid) {
  const ref = doc(db, "subscriptions", clientUid);
  const snap = await getDoc(ref);
  if (!snap.exists()) return null;
  return { id: snap.id, ...snap.data() };
}

export async function requestPlanChange(db, clientUser, planKey) {
  const ref = collection(db, "plan_requests");
  await addDoc(ref, {
    clientUid: clientUser.uid,
    clientEmail: clientUser.email || "",
    clientName: clientUser.displayName || "",
    planKey,
    status: "pending",
    createdAt: serverTimestamp()
  });
}

export async function setSubscription(db, clientUid, patch) {
  const ref = doc(db, "subscriptions", clientUid);
  const snap = await getDoc(ref);
  const data = {
    ...patch,
    updatedAt: serverTimestamp()
  };

  if (!snap.exists()) {
    await setDoc(ref, {
      clientUid,
      createdAt: serverTimestamp(),
      ...data
    });
    return;
  }

  await updateDoc(ref, data);
}
