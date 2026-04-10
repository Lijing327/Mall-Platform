const USER_ID_KEY = "mall_mvp_user_id";
const MERCHANT_ID_KEY = "mall_mvp_merchant_id";

/**
 * MVP 阶段用本地存储模拟登录用户。
 */
export function getUserId() {
  const raw = localStorage.getItem(USER_ID_KEY);
  if (raw) {
    return Number(raw);
  }
  localStorage.setItem(USER_ID_KEY, "1001");
  return 1001;
}

export function setUserId(userId) {
  localStorage.setItem(USER_ID_KEY, String(userId));
}

export function getMerchantId() {
  const raw = localStorage.getItem(MERCHANT_ID_KEY);
  if (raw) {
    return Number(raw);
  }
  localStorage.setItem(MERCHANT_ID_KEY, "1");
  return 1;
}

export function setMerchantId(merchantId) {
  localStorage.setItem(MERCHANT_ID_KEY, String(merchantId));
}
