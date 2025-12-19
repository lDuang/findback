const STATUS_META: Record<
  string,
  {
    label: string;
    type: 'success' | 'warning' | 'danger' | 'info' | 'primary';
    color: string;
  }
> = {
  OPEN: { label: '待认领', type: 'warning', color: '#f59e0b' },
  CLAIMED: { label: '认领中', type: 'warning', color: '#f59e0b' },
  RESOLVED: { label: '已处理', type: 'success', color: '#22c55e' },
  CLOSED: { label: '已关闭', type: 'info', color: '#0ea5e9' },
  APPROVED: { label: '已通过', type: 'success', color: '#22c55e' },
  REJECTED: { label: '已拒绝', type: 'danger', color: '#ef4444' },
  PENDING: { label: '待审核', type: 'warning', color: '#f59e0b' }
};

export function statusLabel(status?: string | null): string {
  return statusMeta(status).label;
}

export function statusTagType(status?: string | null): string {
  return statusMeta(status).type;
}

export function normalizeStatus(status?: string | null): string {
  return status?.toString?.().toUpperCase?.() || '';
}

export function statusMeta(status?: string | null) {
  const key = status?.toString?.().toUpperCase?.() ?? '';
  return STATUS_META[key] || { label: '未知状态', type: 'info', color: '#94a3b8' };
}
