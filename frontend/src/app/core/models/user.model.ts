export type UserRole = 'ADMIN' | 'MUSEUM' | 'REVIEWER';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: UserRole;
  museumId?: number | null;
  museumName?: string | null;
  createdAt: string;
}