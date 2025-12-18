import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';
import { normalizeRole, restoreCachedUser } from '../utils/auth';
import type { AuthUser } from '../types';

const baseURL = (import.meta.env.VITE_API_BASE || '').trim() || '/api';

const client: AxiosInstance = axios.create({
  baseURL
});

client.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const method = config.method?.toLowerCase();
  const isWriteMethod = method ? ['post', 'put', 'patch', 'delete'].includes(method) : false;
  const isAuthRequest = Boolean(config.url && config.url.toString().includes('/auth/'));
  let cachedUser: AuthUser | null = null;

  try {
    cachedUser = restoreCachedUser();
  } catch (error) {
    console.warn('Failed to inject auth headers', error);
  }

  if (cachedUser?.userId) {
    config.headers = config.headers || {};
    config.headers['X-User-Id'] = cachedUser.userId;
    config.headers['X-User-Role'] = normalizeRole(cachedUser.role);
  } else if (isWriteMethod && !isAuthRequest) {
    ElMessage.warning('请先登录后再进行此操作');
    return Promise.reject(new Error('Login required'));
  }
  return config;
});

export default client;
