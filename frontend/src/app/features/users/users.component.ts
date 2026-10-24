import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, CreateUserPayload } from '../../core/services/user.service';
import { MuseumService } from '../../core/services/museum.service';
import { AuthService } from '../../core/auth/auth.service';
import { User, UserRole } from '../../core/models/user.model';
import { Museum } from '../../core/models/museum.model';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 class="text-3xl font-serif font-semibold">User Management</h1>
          <p class="mt-1" style="color: var(--text-secondary)">
            Manage system users and their roles
          </p>
        </div>
        <button (click)="openCreate.set(true)" class="btn-gold">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Add User
        </button>
      </div>

      @if (error()) {
        <div class="mt-4 px-4 py-3 rounded-lg bg-red-50 text-red-700 text-sm border border-red-100">
          {{ error() }}
        </div>
      }

      @if (loading()) {
        <div class="mt-8 card text-center py-12" style="color: var(--text-muted)">
          Loading users...
        </div>
      } @else if (users().length === 0) {
        <div class="mt-8 card text-center py-12">
          <div class="text-lg font-serif text-navy-900 mb-2">No users yet</div>
          <p style="color: var(--text-secondary)">Add the first user to get started.</p>
        </div>
      } @else {
        <div class="mt-6 card overflow-hidden !p-0">
          <table class="w-full">
            <thead class="border-b" style="background: var(--cream-100, #F8F7F4); border-color: var(--border)">
              <tr>
                <th class="text-left px-5 py-3 text-xs uppercase tracking-wider font-semibold"
                    style="color: var(--text-secondary)">User</th>
                <th class="text-left px-5 py-3 text-xs uppercase tracking-wider font-semibold hidden md:table-cell"
                    style="color: var(--text-secondary)">Role</th>
                <th class="text-left px-5 py-3 text-xs uppercase tracking-wider font-semibold hidden lg:table-cell"
                    style="color: var(--text-secondary)">Museum</th>
                <th class="text-left px-5 py-3 text-xs uppercase tracking-wider font-semibold hidden lg:table-cell"
                    style="color: var(--text-secondary)">Created</th>
                <th class="text-right px-5 py-3 text-xs uppercase tracking-wider font-semibold"
                    style="color: var(--text-secondary)">Actions</th>
              </tr>
            </thead>
            <tbody>
              @for (u of users(); track u.id) {
                <tr class="border-b last:border-0" style="border-color: var(--border)">
                  <td class="px-5 py-4">
                    <div class="font-medium text-navy-900 text-sm">{{ u.fullName }}</div>
                    <div class="text-xs" style="color: var(--text-muted)">{{ u.email }}</div>
                    <div class="md:hidden mt-1.5">
                      <span class="badge"
                            [ngClass]="{
                              'badge-approved': u.role === 'ADMIN',
                              'badge-shipped':  u.role === 'MUSEUM',
                              'badge-pending':  u.role === 'REVIEWER'
                            }">
                        {{ u.role }}
                      </span>
                    </div>
                  </td>
                  <td class="px-5 py-4 hidden md:table-cell">
                    <span class="badge"
                          [ngClass]="{
                            'badge-approved': u.role === 'ADMIN',
                            'badge-shipped':  u.role === 'MUSEUM',
                            'badge-pending':  u.role === 'REVIEWER'
                          }">
                      {{ u.role }}
                    </span>
                  </td>
                  <td class="px-5 py-4 hidden lg:table-cell text-sm"
                      style="color: var(--text-secondary)">
                    {{ u.museumName || '—' }}
                  </td>
                  <td class="px-5 py-4 hidden lg:table-cell text-xs"
                      style="color: var(--text-muted)">
                    {{ u.createdAt | date:'short' }}
                  </td>
                  <td class="px-5 py-4 text-right">
                    <button (click)="confirmDelete(u)"
                            [disabled]="u.id === auth.user()?.userId || busy()"
                            class="px-3 py-1.5 rounded-lg text-xs font-medium bg-red-50 text-red-700
                                   hover:bg-red-100 transition-colors disabled:opacity-40">
                      Delete
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>

    @if (openCreate()) {
      <div class="fixed inset-0 bg-navy-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
           (click)="openCreate.set(false)">
        <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 animate-fade-in"
             (click)="$event.stopPropagation()">
          <h2 class="text-2xl font-serif font-semibold text-navy-900 mb-1">Add User</h2>
          <p class="text-sm mb-5" style="color: var(--text-secondary)">
            Create a new user account
          </p>

          @if (formError()) {
            <div class="mb-4 px-4 py-3 rounded-lg bg-red-50 text-red-700 text-sm border border-red-100">
              {{ formError() }}
            </div>
          }

          <form (ngSubmit)="create()" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Full Name *</label>
              <input class="input" [(ngModel)]="form.fullName" name="fullName" required
                     placeholder="John Curator" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Email *</label>
              <input class="input" type="email" [(ngModel)]="form.email" name="email" required
                     placeholder="user&#64;museum.org" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Password *</label>
              <input class="input" type="password" [(ngModel)]="form.password" name="password"
                     required minlength="6" placeholder="Min 6 characters" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Role *</label>
              <select class="input" [(ngModel)]="form.role" name="role" required
                      (ngModelChange)="onRoleChange()">
                <option value="ADMIN">Admin</option>
                <option value="REVIEWER">Reviewer</option>
                <option value="MUSEUM">Museum Curator</option>
              </select>
            </div>
            @if (form.role === 'MUSEUM') {
              <div>
                <label class="block text-sm font-medium mb-1.5 text-navy-900">Museum *</label>
                <select class="input" [(ngModel)]="form.museumId" name="museumId" required>
                  <option [ngValue]="0">Select a museum...</option>
                  @for (m of museums(); track m.id) {
                    <option [ngValue]="m.id">{{ m.name }}</option>
                  }
                </select>
              </div>
            }
            <div class="flex gap-2 pt-2">
              <button type="button" (click)="openCreate.set(false)"
                      class="btn-secondary flex-1">Cancel</button>
              <button type="submit" [disabled]="saving()"
                      class="btn-primary flex-1">
                @if (saving()) { Saving... } @else { Create User }
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
})
export class UsersComponent implements OnInit {
  auth = inject(AuthService);
  private userSvc = inject(UserService);
  private museumSvc = inject(MuseumService);

  users = signal<User[]>([]);
  museums = signal<Museum[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  openCreate = signal(false);
  saving = signal(false);
  busy = signal(false);
  formError = signal<string | null>(null);

  form: CreateUserPayload = {
    email: '',
    password: '',
    fullName: '',
    role: 'REVIEWER',
    museumId: null,
  };

  ngOnInit(): void {
    this.reload();
    this.museumSvc.list().subscribe(d => this.museums.set(d));
  }

  reload(): void {
    this.loading.set(true);
    this.userSvc.list().subscribe({
      next: (data) => { this.users.set(data); this.loading.set(false); },
      error: (err) => { this.error.set(err?.message ?? 'Failed'); this.loading.set(false); },
    });
  }

  onRoleChange(): void {
    if (this.form.role !== 'MUSEUM') {
      this.form.museumId = null;
    }
  }

  create(): void {
    if (!this.form.email || !this.form.password || !this.form.fullName) return;
    if (this.form.role === 'MUSEUM' && !this.form.museumId) {
      this.formError.set('Please select a museum for MUSEUM role');
      return;
    }
    this.saving.set(true);
    this.formError.set(null);
    this.userSvc.create(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.openCreate.set(false);
        this.form = { email: '', password: '', fullName: '', role: 'REVIEWER', museumId: null };
        this.reload();
      },
      error: (err) => {
        this.formError.set(err?.error?.error ?? 'Failed to create user');
        this.saving.set(false);
      },
    });
  }

  confirmDelete(u: User): void {
    if (!confirm(`Delete user "${u.fullName}" (${u.email})?`)) return;
    this.busy.set(true);
    this.userSvc.delete(u.id).subscribe({
      next: () => { this.busy.set(false); this.reload(); },
      error: (err) => {
        this.error.set(err?.error?.error ?? 'Failed to delete');
        this.busy.set(false);
      },
    });
  }
}