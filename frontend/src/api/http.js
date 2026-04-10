import axios from "axios";

const http = axios.create({
  baseURL: "/",
  timeout: 10000
});

http.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (typeof res?.code !== "undefined" && res.code !== 0) {
      return Promise.reject(new Error(res.message || "请求失败"));
    }
    return res;
  },
  (error) => Promise.reject(error)
);

export default http;
