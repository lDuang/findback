import type { Announcement, AuthUser, Banner, Claim, LostItem } from '../types';
import { normalizeRole } from './auth';
import { normalizeStatus } from './status';

type Maybe<T> = T | null | undefined;

export function normalizeItem(raw: Maybe<Partial<LostItem>>): LostItem {
  return {
    id: raw?.id ?? raw?.itemId ?? '',
    userId: raw?.userId ?? '',
    username: raw?.username ?? '',
    title: raw?.title ?? '',
    description: raw?.description ?? '',
    status: normalizeStatus(raw?.status ?? 'OPEN'),
    location: raw?.location ?? '',
    imageUrl: raw?.imageUrl ?? '',
    createdAt: raw?.createdAt ?? (raw as Record<string, unknown>)?.created_at?.toString?.() ?? ''
  };
}

export function normalizeClaim(raw: Maybe<Partial<Claim>>): Claim {
  return {
    id: raw?.id ?? '',
    itemId: raw?.itemId ?? '',
    userId: raw?.userId ?? '',
    username: raw?.username ?? '',
    reason: raw?.reason ?? '',
    evidenceUrl: raw?.evidenceUrl ?? '',
    status: normalizeStatus(raw?.status ?? 'PENDING'),
    createdAt: raw?.createdAt ?? (raw as Record<string, unknown>)?.created_at?.toString?.() ?? ''
  };
}

export function normalizeUser(raw: Maybe<Partial<AuthUser>>): AuthUser | null {
  if (!raw?.userId) return null;
  const role = normalizeRole(raw.role);
  if (!role) return null;
  return {
    userId: raw.userId,
    username: raw.username ?? '',
    role
  };
}

export function normalizeAnnouncement(raw: Maybe<Partial<Announcement>>): Announcement {
  return {
    id: raw?.id ?? '',
    title: raw?.title ?? '公告',
    content: raw?.content ?? '',
    createdAt: raw?.createdAt ?? (raw as Record<string, unknown>)?.created_at?.toString?.() ?? ''
  };
}

export function normalizeBanner(raw: Maybe<Partial<Banner>>): Banner {
  return {
    id: raw?.id ?? raw?.bannerId ?? '',
    title: raw?.title ?? 'Banner',
    imageUrl: raw?.imageUrl ?? (raw as Record<string, unknown>)?.image_url?.toString?.() ?? '',
    link: raw?.link ?? (raw as Record<string, unknown>)?.url?.toString?.() ?? '',
    description: raw?.description ?? '',
    createdAt: raw?.createdAt ?? (raw as Record<string, unknown>)?.created_at?.toString?.() ?? ''
  };
}
