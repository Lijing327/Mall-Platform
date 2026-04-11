import http from "./http";

export function addCart(data) {
  const { userId: _u, ...rest } = data || {};
  return http.post("/api/cart/add", rest);
}

export function updateCart(data) {
  const { userId: _u, ...rest } = data || {};
  return http.post("/api/cart/update", rest);
}

export function deleteCart(data) {
  const { userId: _u, ...rest } = data || {};
  return http.post("/api/cart/delete", rest);
}

export function fetchCartList() {
  return http.get("/api/cart/list");
}
