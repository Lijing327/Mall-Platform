export const adminNavigation = [
  { key: "dashboard", label: "仪表盘", to: "/admin/dashboard", icon: "仪" },
  { key: "products", label: "商品管理", to: "/admin/products", icon: "商" },
  { key: "orders", label: "订单管理", to: "/admin/orders", icon: "订" },
  { key: "users", label: "用户管理", to: "/admin/users", icon: "用" }
];

const pageMetaMap = {
  dashboard: {
    title: "仪表盘",
    subtitle: "查看商城当前经营概况，并快速进入核心运营页面。"
  },
  products: {
    title: "商品管理",
    subtitle: "用表格查看在售商品、库存与上下架状态。"
  },
  orders: {
    title: "订单管理",
    subtitle: "按订单号、状态和用户查单，处理待发货与已完成订单。"
  },
  orderDetail: {
    title: "订单详情",
    subtitle: "跟踪订单状态、金额与发货情况；商品明细随接口接入补齐。"
  },
  users: {
    title: "用户管理",
    subtitle: "查看用户基础信息与交易概况。"
  },
  merchants: {
    title: "商家审核",
    subtitle: "保留管理能力，当前不放入主导航"
  }
};

export function getAdminPageMeta(path) {
  if (path.startsWith("/admin/orders/")) {
    return pageMetaMap.orderDetail;
  }
  if (path.startsWith("/admin/dashboard")) {
    return pageMetaMap.dashboard;
  }
  if (path.startsWith("/admin/products")) {
    return pageMetaMap.products;
  }
  if (path.startsWith("/admin/orders")) {
    return pageMetaMap.orders;
  }
  if (path.startsWith("/admin/users")) {
    return pageMetaMap.users;
  }
  if (path.startsWith("/admin/merchants")) {
    return pageMetaMap.merchants;
  }
  return {
    title: "管理后台",
    subtitle: "统一查看与处理商城运营事务"
  };
}

export function formatDateTime(value) {
  if (!value) {
    return "—";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day} ${hour}:${minute}`;
}

export function formatCurrency(value) {
  const amount = Number(value ?? 0);
  if (!Number.isFinite(amount)) {
    return "¥0.00";
  }
  return `¥${amount.toFixed(2)}`;
}

export function formatCount(value) {
  const count = Number(value ?? 0);
  return Number.isFinite(count) ? String(count) : "0";
}

export function getInitial(name) {
  if (!name) {
    return "商";
  }
  return String(name).trim().charAt(0).toUpperCase() || "商";
}

export function getOrderStatusTone(status) {
  switch (status) {
    case "PENDING_PAYMENT":
      return "warning";
    case "PAID":
      return "info";
    case "SHIPPED":
      return "primary";
    case "COMPLETED":
      return "success";
    default:
      return "neutral";
  }
}

export function getSaleStatusTone(status) {
  switch (status) {
    case "ON_SHELF":
      return "success";
    case "OFF_SHELF":
      return "neutral";
    default:
      return "neutral";
  }
}
