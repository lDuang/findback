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
    user: restoreCachedUser() as AuthUser | null
  }),
  actions: {
    async login(username: string, password: string) {
      const { data } = await client.post<LoginResponse>('/auth/login', { username, password });
      const userId = data?.userId;
      const usernameFromResponse = data?.username;
      const role = normalizeRole(data?.role ?? '');

      if (!userId || !role) {
        throw new Error('登录响应异常：缺少用户信息');
      }

      this.user = { userId, username: usernameFromResponse || username, role };
      cacheUser(this.user);
    },
    logout() {
      this.user = null;
      cacheUser(null);
    }
  }
});
