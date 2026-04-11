import http from "./http";

// data: { addressId, remark? }
export function createOrder(data) {
  const { userId: _u, ...rest } = data || {};
  return http.post("/api/orders/create", rest);
}

export function payOrder(data) {
  const { userId: _u, ...rest } = data || {};
  return http.post("/api/orders/pay", rest);
}

export function fetchMyOrders() {
  return http.get("/api/orders/my");
}

export function confirmReceiveOrder(orderId) {
  return http.post(`/api/orders/${orderId}/confirm-receive`);
}
