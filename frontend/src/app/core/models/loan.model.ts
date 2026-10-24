export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SHIPPED' | 'RETURNED';

export interface Loan {
  id: number;
  artifactId: number;
  artifactName: string;
  fromMuseumId: number;
  fromMuseumName: string;
  toMuseumId: number;
  toMuseumName: string;
  requestedById: number;
  requestedByName: string;
  status: LoanStatus;
  notes?: string | null;
  createdAt: string;
  updatedAt: string;
}