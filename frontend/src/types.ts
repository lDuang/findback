export type Role = 'ADMIN' | 'USER' | string;

export interface AuthUser {
  userId: number | string;
  username: string;
  role: Role;
}

export interface LostItem {
  id: number | string;
  userId: number | string;
  username?: string;
  title: string;
  description?: string;
  status: string;
  location?: string;
  imageUrl?: string;
  createdAt?: string;
}

export interface Claim {
  id: number | string;
  itemId: number | string;
  userId: number | string;
  username?: string;
  reason?: string;
  evidenceUrl?: string;
  status: string;
  createdAt?: string;
}

export interface Announcement {
  id: number | string;
  title: string;
  content?: string;
  createdAt?: string;
}

export interface Banner {
  id: number | string;
  title: string;
  imageUrl: string;
  link?: string;
  description?: string;
  createdAt?: string;
}

export interface StatsOverview {
  totalItems: number;
  openItems: number;
  claimedItems: number;
  totalClaims: number;
  pendingClaims: number;
  approvedClaims: number;
  totalUsers: number;
}
