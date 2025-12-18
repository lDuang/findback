import type { AuthUser, Role } from '../types';
export type { AuthUser, Role };

export const STORAGE_KEY = 'lost-found-user';

export function normalizeRole(role?: string | null): Role {
  if (typeof role === 'string') {
    return role.trim().toUpperCase();
  }
  return (role ?? '') as Role;
}

export function cacheUser(user: AuthUser | null): void {
  if (user) {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        userId: user.userId,
        username: user.username,
        role: normalizeRole(user.role)
      })
    );
    return;
  }
  localStorage.removeItem(STORAGE_KEY);
}

export function restoreCachedUser(): AuthUser | null {
  try {
    const cached = localStorage.getItem(STORAGE_KEY);
    if (!cached) return null;
    const parsed = JSON.parse(cached) as Partial<AuthUser>;
    const role = parsed?.role;
    if (!parsed?.userId || !role) return null;
    return {
      userId: parsed.userId,
      username: parsed.username,
      role: normalizeRole(String(role))
    };
  } catch (error) {
    console.warn('Failed to parse cached user', error);
    return null;
  }
}
