import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Loan, LoanStatus } from '../models/loan.model';

@Injectable({ providedIn: 'root' })
export class LoanService {
  private base = '/api/loans';
  constructor(private http: HttpClient) {}

  list(status?: LoanStatus): Observable<Loan[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<{ loans: Loan[] }>(this.base, { params })
      .pipe(map(r => r.loans));
  }

  create(payload: { artifactId: number; notes?: string }): Observable<Loan> {
    return this.http.post<Loan>(this.base, payload);
  }

  approve(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${this.base}/${id}/approve`, {});
  }

  reject(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${this.base}/${id}/reject`, {});
  }

  ship(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${this.base}/${id}/ship`, {});
  }

  returnLoan(id: number): Observable<Loan> {
    return this.http.patch<Loan>(`${this.base}/${id}/return`, {});
  }
}