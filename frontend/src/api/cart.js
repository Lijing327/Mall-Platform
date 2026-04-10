import http from "./http";

export function addCart(data) {
  return http.post("/api/cart/add", data);
}

export function fetchCartList(userId) {
  return http.get("/api/cart/list", {
    params: { userId }
  });
}
