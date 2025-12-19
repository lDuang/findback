import { defineStore } from 'pinia';
import client from '../api/client';
import { cacheUser, normalizeRole, restoreCachedUser, type AuthUser } from '../utils/auth';

interface LoginResponse {
  userId?: number | string;
  username?: string;
  role?: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as AuthUser | null,
    isRestoring: true
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.user)
  },
  actions: {
    restoreFromCache() {
      this.user = restoreCachedUser();
      this.isRestoring = false;
    },
    async login(username: string, password: string) {
      const { data } = await client.post<LoginResponse>('/auth/login', { username, password });
      const userId = data?.userId;
      const usernameFromResponse = data?.username ?? '';
      const role = normalizeRole(data?.role ?? '');

      if (!userId || !role) {
        throw new Error('登录响应异常：缺少用户信息');
      }

      const normalizedUser = { userId, username: usernameFromResponse || username, role };

      this.user = normalizedUser;
      cacheUser(normalizedUser);
      this.isRestoring = false;
    },
    async register(username: string, password: string) {
      const { data } = await client.post<LoginResponse>('/auth/register', { username, password });
      const userId = data?.userId;
      const usernameFromResponse = data?.username ?? '';
      const role = normalizeRole(data?.role ?? '');

      if (!userId || !role) {
        throw new Error('注册响应异常：缺少用户信息');
      }

      const normalizedUser = { userId, username: usernameFromResponse || username, role };

      this.user = normalizedUser;
      cacheUser(normalizedUser);
      this.isRestoring = false;
    },
    logout() {
      this.user = null;
      cacheUser(null);
      this.isRestoring = false;
    }
  }
});
