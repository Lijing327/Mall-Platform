/**
 * 后端枚举/code → 中文展示（与 Java 枚举名一致）。
 */

const ORDER_STATUS_ZH = {
  PENDING_PAYMENT: "待支付",
  PAID: "已支付",
  SHIPPED: "已发货",
  COMPLETED: "已完成"
};

const PAY_TYPE_ZH = {
  MOCK: "模拟支付"
};

const SALE_STATUS_ZH = {
  ON_SHELF: "已上架",
  OFF_SHELF: "已下架"
};

const APPLY_STATUS_ZH = {
  PENDING: "待审核",
  APPROVED: "已通过",
  REJECTED: "已拒绝"
};

/** @param {string|null|undefined} code */
export function orderStatusZh(code) {
  if (code == null || code === "") return "—";
  return ORDER_STATUS_ZH[code] || String(code);
}

/** @param {string|null|undefined} code */
export function payTypeZh(code) {
  if (code == null || code === "") return "—";
  return PAY_TYPE_ZH[code] || String(code);
}

/** @param {string|null|undefined} code */
export function saleStatusZh(code) {
  if (code == null || code === "") return "—";
  return SALE_STATUS_ZH[code] || String(code);
}

/** @param {string|null|undefined} code */
export function applyStatusZh(code) {
  if (code == null || code === "") return "—";
  return APPLY_STATUS_ZH[code] || String(code);
}

const USER_ROLE_ZH = {
  ADMIN: "管理员",
  USER: "普通用户"
};

/** @param {string|null|undefined} role */
export function userRoleZh(role) {
  if (role == null || role === "") return "—";
  return USER_ROLE_ZH[role] || String(role);
}
