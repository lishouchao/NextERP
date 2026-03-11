import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, Menu, LoginRequest, LoginResponse } from '@/types';
import { authApi } from '@/lib/api/auth';

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  permissions: string[];
  roles: string[];
  menus: Menu[];
  isAuthenticated: boolean;
  loading: boolean;

  login: (data: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  setUser: (user: User) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      permissions: [],
      roles: [],
      menus: [],
      isAuthenticated: false,
      loading: false,

      login: async (data: LoginRequest) => {
        set({ loading: true });
        try {
          const response = await authApi.login(data);
          if (response.success && response.data) {
            const { accessToken, refreshToken, userInfo, permissions, roles, menus } = response.data;
            set({
              user: userInfo,
              accessToken,
              refreshToken,
              permissions: permissions || [],
              roles: roles || [],
              menus: menus || [],
              isAuthenticated: true,
              loading: false,
            });
            // 同时存储到 localStorage（供 axios 使用）
            if (typeof window !== 'undefined') {
              localStorage.setItem('accessToken', accessToken);
              localStorage.setItem('refreshToken', refreshToken);
            }
          } else {
            throw new Error(response.message || '登录失败');
          }
        } catch (error) {
          set({ loading: false });
          throw error;
        }
      },

      logout: async () => {
        try {
          await authApi.logout();
        } catch {
          // 忽略登出错误
        } finally {
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            permissions: [],
            roles: [],
            menus: [],
            isAuthenticated: false,
          });
          if (typeof window !== 'undefined') {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
          }
        }
      },

      setUser: (user: User) => set({ user }),
      setTokens: (accessToken: string, refreshToken: string) => {
        set({ accessToken, refreshToken });
        if (typeof window !== 'undefined') {
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', refreshToken);
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        permissions: state.permissions,
        roles: state.roles,
        menus: state.menus,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
