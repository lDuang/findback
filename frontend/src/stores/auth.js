import { defineStore } from 'pinia';
import client from '../api/client';
import { cacheUser, normalizeRole, restoreCachedUser } from '../utils/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: restoreCachedUser()
  }),
  actions: {
    async login(username, password) {
      const { data } = await client.post('/auth/login', { username, password });
      const userId = data?.userId;
      const role = normalizeRole(data?.role);

      if (!userId || !role) {
        throw new Error('登录响应异常：缺少用户信息');
      }

      this.user = { userId, role };
      cacheUser(this.user);
    },
    logout() {
      this.user = null;
      cacheUser(null);
    }
  }
});
