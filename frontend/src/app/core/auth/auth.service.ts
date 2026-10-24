import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';

export type UserRole = 'ADMIN' | 'MUSEUM' | 'REVIEWER';

export interface AuthUser {
  userId: number;
  email: string;
  fullName: string;
  role: UserRole;
}

export interface AuthResponse extends AuthUser {
  token: string;
}

const TOKEN_KEY = 'curatix_token';
const USER_KEY  = 'curatix_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBase = '/api';
  user = signal<AuthUser | null>(this.loadUser());

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBase}/auth/login`, { email, password })
      .pipe(tap(res => this.saveSession(res)));
  }

  register(payload: {
    email: string;
    password: string;
    fullName: string;
    role: UserRole;
    museumId?: number | null;
  }): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBase}/auth/register`, payload)
      .pipe(tap(res => this.saveSession(res)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.user.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasRole(...roles: UserRole[]): boolean {
    const u = this.user();
    return !!u && roles.includes(u.role);
  }

  private saveSession(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    const user: AuthUser = {
      userId: res.userId,
      email: res.email,
      fullName: res.fullName,
      role: res.role,
    };
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.user.set(user);
  }

  private loadUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}