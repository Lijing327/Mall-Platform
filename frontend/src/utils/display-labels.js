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

const USER_ROLE_ZH = {
  ADMIN: "管理员",
  USER: "普通用户"
};

function displayValue(code, mapper) {
  if (code == null || code === "") {
    return "—";
  }
  return mapper[code] || String(code);
}

export function orderStatusZh(code) {
  return displayValue(code, ORDER_STATUS_ZH);
}

export function payTypeZh(code) {
  return displayValue(code, PAY_TYPE_ZH);
}

export function saleStatusZh(code) {
  return displayValue(code, SALE_STATUS_ZH);
}

export function applyStatusZh(code) {
  return displayValue(code, APPLY_STATUS_ZH);
}

export function userRoleZh(role) {
  return displayValue(role, USER_ROLE_ZH);
}
