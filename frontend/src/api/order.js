import http from "./http";

export function createOrder(data) {
  return http.post("/api/orders/create", data);
}

export function payOrder(data) {
  return http.post("/api/orders/pay", data);
}

/**
 * 当前后端暂无 /api/orders/my，这里先走管理员订单列表接口做 MVP 联调。
 */
export function fetchMyOrders(params) {
  return http.get("/api/admin/orders", { params });
}
