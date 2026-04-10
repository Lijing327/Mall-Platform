import http from "./http";

export function merchantApply(data) {
  return http.post("/api/merchant/apply", data);
}

export function fetchMerchantProducts(params) {
  return http.get("/api/merchant/products", { params });
}

export function createMerchantProduct(data) {
  return http.post("/api/merchant/products", data);
}

export function updateMerchantProduct(id, data) {
  return http.put(`/api/merchant/products/${id}`, data);
}

export function onShelfMerchantProduct(id, params) {
  return http.post(`/api/merchant/products/${id}/on-shelf`, null, { params });
}

export function offShelfMerchantProduct(id, params) {
  return http.post(`/api/merchant/products/${id}/off-shelf`, null, { params });
}

export function deleteMerchantProduct(id, params) {
  return http.delete(`/api/merchant/products/${id}`, { params });
}

export function fetchMerchantOrders(params) {
  return http.get("/api/merchant/orders", { params });
}

export function fetchMerchantOrderDetail(id, params) {
  return http.get(`/api/merchant/orders/${id}`, { params });
}

export function shipMerchantOrder(id, data) {
  return http.post(`/api/merchant/orders/${id}/ship`, data);
}
