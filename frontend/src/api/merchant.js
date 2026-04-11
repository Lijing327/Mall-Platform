import http from "./http";

/** 当前登录用户是否已申请/入驻商家（需登录） */
export function fetchMerchantMe() {
  return http.get("/api/merchant/me");
}

export function merchantApply(data) {
  return http.post("/api/merchant/apply", stripUserBody(data));
}

export function fetchMerchantProducts(params) {
  return http.get("/api/merchant/products", { params: stripUser(params) });
}

export function fetchMerchantProduct(id, params) {
  return http.get(`/api/merchant/products/${id}`, { params: stripUser(params) });
}

export function createMerchantProduct(data) {
  return http.post("/api/merchant/products", stripUserBody(data));
}

export function updateMerchantProduct(id, data) {
  return http.put(`/api/merchant/products/${id}`, stripUserBody(data));
}

export function onShelfMerchantProduct(id, params) {
  return http.post(`/api/merchant/products/${id}/on-shelf`, null, { params: stripUser(params) });
}

export function offShelfMerchantProduct(id, params) {
  return http.post(`/api/merchant/products/${id}/off-shelf`, null, { params: stripUser(params) });
}

export function deleteMerchantProduct(id, params) {
  return http.delete(`/api/merchant/products/${id}`, { params: stripUser(params) });
}

function stripUser(params) {
  if (!params) {
    return {};
  }
  const { userId: _u, ...rest } = params;
  return rest;
}

export function fetchMerchantOrders(params) {
  return http.get("/api/merchant/orders", { params: stripUser(params) });
}

export function fetchMerchantOrderDetail(id, params) {
  return http.get(`/api/merchant/orders/${id}`, { params: stripUser(params) });
}

export function shipMerchantOrder(id, data) {
  return http.post(`/api/merchant/orders/${id}/ship`, stripUserBody(data));
}

function stripUserBody(body) {
  if (!body || typeof body !== "object") {
    return body;
  }
  const { userId: _u, ...rest } = body;
  return rest;
}
