const TOKEN_KEY = "mall_mvp_token";
const USER_ID_KEY = "mall_mvp_user_id";
const ROLE_KEY = "mall_mvp_role";
const MERCHANT_ID_KEY = "mall_mvp_merchant_id";

export function notifyAuthChanged() {
  window.dispatchEvent(new Event("mall-auth-changed"));
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function setUserId(userId) {
  if (userId == null || userId === "") {
    localStorage.removeItem(USER_ID_KEY);
    return;
  }
  localStorage.setItem(USER_ID_KEY, String(userId));
}

export function setRole(role) {
  if (role) {
    localStorage.setItem(ROLE_KEY, role);
  } else {
    localStorage.removeItem(ROLE_KEY);
  }
}

export function getUserId() {
  const raw = localStorage.getItem(USER_ID_KEY);
  if (raw == null || raw === "") {
    return null;
  }
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
}

export function getRole() {
  return localStorage.getItem(ROLE_KEY) || "";
}

export function isAdmin() {
  return getRole() === "ADMIN";
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_ID_KEY);
  localStorage.removeItem(ROLE_KEY);
}

export function getMerchantId() {
  const raw = localStorage.getItem(MERCHANT_ID_KEY);
  if (raw == null || raw === "") {
    return null;
  }
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
}

export function setMerchantId(merchantId) {
  if (merchantId == null || merchantId === "") {
    localStorage.removeItem(MERCHANT_ID_KEY);
    return;
  }
  localStorage.setItem(MERCHANT_ID_KEY, String(merchantId));
}

/**
 * 登录成功后写入会话（与后端 /api/auth/login 返回一致）。
 */
export function applyLoginSession(loginVO) {
  setToken(loginVO.token);
  setUserId(loginVO.userId);
  setRole(loginVO.role || "USER");
  notifyAuthChanged();
}

export function logout() {
  clearAuth();
  notifyAuthChanged();
}
