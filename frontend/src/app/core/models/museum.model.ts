export interface Museum {
  id: number;
  name: string;
  country: string;
  city?: string | null;
  createdAt: string;
}