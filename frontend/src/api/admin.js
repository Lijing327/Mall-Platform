import http from "./http";

export function fetchAdminMerchants(params) {
  return http.get("/api/admin/merchants", { params });
}

export function auditMerchant(data) {
  // 复用现有后端审核接口
  return http.post("/api/admin/merchant/audit", data);
}

export function fetchAdminOrders(params) {
  return http.get("/api/admin/orders", { params });
}

export function fetchAdminProducts(params) {
  return http.get("/api/admin/products", { params });
}

export function adminOffShelfProduct(id) {
  return http.post(`/api/admin/products/${id}/off-shelf`);
}
