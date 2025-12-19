export function formatDateTime(value?: string | number | Date | null): string {
  if (!value) return '—';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleString('zh-CN', {
    hour12: false
  });
}

export function formatDateYMD(value?: string | number | Date | null): string {
  if (!value) return '—';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}年${m}月${d}日`;
}

export function formatUserDisplay(username?: string | null, userId?: string | number): string {
  if (username) return username;
  if (userId !== undefined && userId !== null && `${userId}` !== '') return `ID：${userId}`;
  return '未知用户';
}
