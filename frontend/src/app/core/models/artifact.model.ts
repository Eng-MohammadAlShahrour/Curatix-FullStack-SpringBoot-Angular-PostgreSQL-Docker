export interface Artifact {
  id: number;
  name: string;
  era?: string | null;
  description?: string | null;
  status: 'AVAILABLE' | 'RESERVED' | 'ON_LOAN';
  museumId: number;
  museumName: string;
  createdAt: string;
}