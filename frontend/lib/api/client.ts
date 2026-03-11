import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse } from '@/types';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 防止多次刷新 Token
let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

// 订阅 Token 刷新
function subscribeTokenRefresh(callback: (token: string) => void) {
  refreshSubscribers.push(callback);
}

// 通知所有订阅者
function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
}

// 重定向到登录页（防抖）
let redirectTimer: NodeJS.Timeout | null = null;
function redirectToLogin() {
  if (redirectTimer) return; // 防止多次调用
  redirectTimer = setTimeout(() => {
    redirectTimer = null;
    // 只在非登录页时重定向
    if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      // 使用 replace 避免历史记录问题
      window.location.replace('/login');
    }
  }, 100);
}

// 刷新 Token
async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) {
    return null;
  }

  try {
    const response = await axios.post(
      `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/v1/auth/refresh`,
      { refreshToken },
      { timeout: 10000 }
    );

    if (response.data?.success && response.data?.data?.accessToken) {
      const { accessToken, refreshToken: newRefreshToken } = response.data.data;
      localStorage.setItem('accessToken', accessToken);
      if (newRefreshToken) {
        localStorage.setItem('refreshToken', newRefreshToken);
      }
      return accessToken;
    }
    return null;
  } catch {
    // 刷新失败，清除 token
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    return null;
  }
}

// 请求拦截器
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
api.interceptors.response.use(
  (response) => response.data,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // 401 错误处理
    if (error.response?.status === 401) {
      // 如果是刷新 Token 的请求失败，直接跳转登录
      if (originalRequest.url?.includes('/auth/refresh')) {
        redirectToLogin();
        return Promise.reject(error);
      }

      // 如果已经在刷新 Token，等待刷新完成
      if (isRefreshing) {
        return new Promise((resolve) => {
          subscribeTokenRefresh((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(api(originalRequest));
          });
        });
      }

      // 标记正在刷新
      isRefreshing = true;

      // 尝试刷新 Token
      const newToken = await refreshAccessToken();

      if (newToken) {
        // 刷新成功，重试原请求
        isRefreshing = false;
        onTokenRefreshed(newToken);
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return api(originalRequest);
      } else {
        // 刷新失败，跳转登录
        isRefreshing = false;
        redirectToLogin();
        return Promise.reject(error);
      }
    }

    // 其他错误直接抛出
    return Promise.reject(error);
  }
);

export default api;
