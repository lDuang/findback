import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';
import { normalizeRole, restoreCachedUser } from '../utils/auth';

const client: AxiosInstance = axios.create({
  baseURL: '/api'
});

client.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  try {
    const cachedUser = restoreCachedUser();
    if (cachedUser?.userId) {
      config.headers['X-User-Id'] = cachedUser.userId;
    }
    if (cachedUser?.role) {
      config.headers['X-User-Role'] = normalizeRole(cachedUser.role);
    }
  } catch (error) {
    console.warn('Failed to inject auth headers', error);
  }
  return config;
});

export default client;
