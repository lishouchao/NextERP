import api from './client';
import type { LoginRequest, LoginResponse, ApiResponse } from '@/types';

export const authApi = {
  login: (data: LoginRequest) =>
    api.post<unknown, ApiResponse<LoginResponse>>('/api/v1/auth/login', data),

  logout: () =>
    api.post<unknown, ApiResponse<void>>('/api/v1/auth/logout'),

  refreshToken: (refreshToken: string) =>
    api.post<unknown, ApiResponse<{ accessToken: string; refreshToken: string }>>(
      '/api/v1/auth/refresh',
      { refreshToken }
    ),

  getCurrentUser: () =>
    api.get<unknown, ApiResponse<LoginResponse>>('/api/v1/auth/me'),
};
