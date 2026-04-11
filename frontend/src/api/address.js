import http from "./http";

export function fetchAddressList() {
  return http.get("/api/user/addresses");
}

export function fetchDefaultAddress() {
  return http.get("/api/user/addresses/default");
}

export function createAddress(data) {
  const { userId: _u, ...rest } = data || {};
  return http.post("/api/user/addresses", rest);
}

export function updateAddress(id, data) {
  const { userId: _u, ...rest } = data || {};
  return http.put(`/api/user/addresses/${id}`, rest);
}

export function deleteAddress(id) {
  return http.delete(`/api/user/addresses/${id}`);
}

export function setDefaultAddress(id) {
  return http.post(`/api/user/addresses/${id}/default`);
}
