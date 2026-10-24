import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User, UserRole } from '../models/user.model';

export interface CreateUserPayload {
  email: string;
  password: string;
  fullName: string;
  role: UserRole;
  museumId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  private base = '/api/admin/users';
  constructor(private http: HttpClient) {}

  list(): Observable<User[]> {
    return this.http.get<{ users: User[] }>(this.base).pipe(map(r => r.users));
  }

  create(payload: CreateUserPayload): Observable<User> {
    return this.http.post<User>(this.base, payload);
  }

  updateRole(id: number, role: UserRole, museumId?: number | null): Observable<User> {
    return this.http.patch<User>(`${this.base}/${id}/role`, { role, museumId });
  }

  delete(id: number): Observable<{ ok: boolean }> {
    return this.http.delete<{ ok: boolean }>(`${this.base}/${id}`);
  }
}