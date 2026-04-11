import http from "./http";

export function addCart(data) {
  const { userId: _u, ...rest } = data || {};
  return http.post("/api/cart/add", rest);
}

export function fetchCartList() {
  return http.get("/api/cart/list");
}
