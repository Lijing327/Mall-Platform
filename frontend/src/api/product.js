import http from "./http";

export function fetchProductList(params) {
  return http.get("/api/products", { params });
}

export function fetchProductDetail(id) {
  return http.get(`/api/products/${id}`);
}
