import http from "./http";

export function login(data) {
  return http.post("/api/auth/login", data);
}
