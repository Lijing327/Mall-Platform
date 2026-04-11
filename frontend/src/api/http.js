import axios from "axios";
import { clearAuth, getToken, notifyAuthChanged } from "../utils/user-context";

const http = axios.create({
  baseURL: "/",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (typeof res?.code !== "undefined" && res.code !== 0) {
      if (res.code === 401) {
        const path = window.location.pathname || "";
        if (!path.startsWith("/login")) {
          clearAuth();
          notifyAuthChanged();
          const q = encodeURIComponent(path + window.location.search);
          window.location.assign(`/login?redirect=${q}`);
        }
        return Promise.reject(new Error(res.message || "未登录或令牌无效"));
      }
      return Promise.reject(new Error(res.message || "请求失败"));
    }
    return res;
  },
  (error) => Promise.reject(error)
);

export default http;
