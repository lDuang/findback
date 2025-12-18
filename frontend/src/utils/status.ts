const STATUS_LABELS: Record<string, string> = {
  OPEN: '待认领',
  CLAIMED: '认领中',
  RESOLVED: '已处理',
  CLOSED: '已关闭',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  PENDING: '待审核'
};

export function statusLabel(status?: string | null): string {
  const key = status?.toString?.().toUpperCase?.() ?? '';
  return STATUS_LABELS[key] || '未知状态';
}

export function statusTagType(status?: string | null): string {
  const key = status?.toString?.().toUpperCase?.() ?? '';
  if (['APPROVED', 'RESOLVED'].includes(key)) return 'success';
  if (key === 'REJECTED') return 'danger';
  if (key === 'CLOSED') return 'info';
  if (['OPEN', 'CLAIMED', 'PENDING'].includes(key)) return 'warning';
  return 'info';
}

export function normalizeStatus(status?: string | null): string {
  return status?.toString?.().toUpperCase?.() || '';
}
