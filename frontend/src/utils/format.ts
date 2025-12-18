export function formatDateTime(value?: string | number | Date | null): string {
  if (!value) return '—';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleString('zh-CN', {
    hour12: false
  });
}

export function formatUserDisplay(username?: string | null, userId?: string | number): string {
  if (username) return username;
  if (userId !== undefined && userId !== null && `${userId}` !== '') return `ID：${userId}`;
  return '未知用户';
}
