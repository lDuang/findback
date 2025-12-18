export type Role = 'ADMIN' | 'USER' | string;

export interface AuthUser {
  userId: number | string;
  username?: string;
  role: Role;
}

export interface LostItem {
  id: number | string;
  userId: number | string;
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
  reason?: string;
  evidenceUrl?: string;
  status?: string;
}

export interface Announcement {
  id: number | string;
  title: string;
  content?: string;
  createdAt?: string;
}
