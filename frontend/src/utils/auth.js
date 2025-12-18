export const STORAGE_KEY = 'lost-found-user';

export function normalizeRole(role) {
  return typeof role === 'string' ? role.trim().toUpperCase() : role;
}

export function cacheUser(user) {
  if (user) {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        userId: user.userId,
        role: normalizeRole(user.role)
      })
    );
  } else {
    localStorage.removeItem(STORAGE_KEY);
  }
}

export function restoreCachedUser() {
  try {
    const cached = localStorage.getItem(STORAGE_KEY);
    if (!cached) return null;
    const parsed = JSON.parse(cached);
    if (!parsed?.userId || !parsed?.role) return null;
    return {
      userId: parsed.userId,
      role: normalizeRole(parsed.role)
    };
  } catch (error) {
    console.warn('Failed to parse cached user', error);
    return null;
  }
}
