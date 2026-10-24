import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { LogoComponent } from '../../shared/logo/logo.component';

interface DemoAccount {
  role: string;
  email: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, LogoComponent],
  template: `
    <div class="min-h-screen flex bg-cream">

      <!-- Brand panel (hidden on mobile) -->
      <div class="hidden lg:flex flex-1 bg-navy-900 text-white relative overflow-hidden">
        <div class="absolute inset-0 opacity-10 pointer-events-none"
             style="background-image: radial-gradient(circle at 20% 30%, #C9A961 0%, transparent 40%),
                                       radial-gradient(circle at 80% 70%, #C9A961 0%, transparent 40%);">
        </div>

        <div class="relative z-10 p-12 flex flex-col justify-between w-full">
          <div class="flex items-center gap-3">
            <app-logo [size]="48"></app-logo>
            <div>
              <div class="font-serif font-semibold text-2xl tracking-tight">Curatix</div>
              <div class="text-xs text-white/60 uppercase tracking-widest">Museum Loans</div>
            </div>
          </div>

          <div class="max-w-md">
            <h2 class="text-4xl font-serif font-semibold leading-tight mb-6 !text-white">
              Where masterpieces<br/>
              <span class="text-gold-400">travel with care.</span>
            </h2>
            <p class="text-white/70 text-lg leading-relaxed">
              Enterprise-grade platform for managing artifact loans between the world's leading museums.
            </p>
          </div>

          <div class="flex items-center gap-6 text-sm text-white/50">
            <div>© 2026 Curatix</div>
            <div class="w-px h-4 bg-white/20"></div>
            <div>Enterprise Edition</div>
          </div>
        </div>
      </div>

      <!-- Form panel -->
      <div class="flex-1 flex items-center justify-center p-6 sm:p-8">
        <div class="w-full max-w-md animate-fade-in">

          <div class="lg:hidden mb-8 flex items-center gap-3">
            <app-logo [size]="40"></app-logo>
            <div>
              <div class="font-serif font-semibold text-xl tracking-tight text-navy-900">Curatix</div>
              <div class="text-[10px] uppercase tracking-widest" style="color: var(--text-muted)">
                Museum Loans
              </div>
            </div>
          </div>

          <h1 class="text-3xl font-serif font-semibold mb-2">Welcome back</h1>
          <p class="mb-8" style="color: var(--text-secondary)">
            Sign in to access the loan management portal
          </p>

          @if (error()) {
            <div class="mb-4 px-4 py-3 rounded-lg bg-red-50 text-red-700 text-sm border border-red-100">
              {{ error() }}
            </div>
          }

          <form (ngSubmit)="submit()" #f="ngForm" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Email address</label>
              <input class="input" type="email" name="email" [(ngModel)]="email"
                     required placeholder="you&#64;museum.org" autocomplete="email" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Password</label>
              <input class="input" type="password" name="password" [(ngModel)]="password"
                     required placeholder="••••••••" autocomplete="current-password" />
            </div>
            <button type="submit" class="btn-primary w-full !py-3.5 mt-2"
                    [disabled]="loading() || f.invalid">
              @if (loading()) {
                Signing in...
              } @else {
                Sign in
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2.5">
                  <line x1="5" y1="12" x2="19" y2="12"/>
                  <polyline points="12 5 19 12 12 19"/>
                </svg>
              }
            </button>
          </form>

          <div class="mt-8 pt-6 border-t" style="border-color: var(--border)">
            <div class="text-xs uppercase tracking-wider font-medium mb-3"
                 style="color: var(--text-muted)">Demo accounts</div>
            <div class="space-y-2 text-sm">
              @for (acc of demoAccounts; track acc.email) {
                <div class="flex justify-between">
                  <span style="color: var(--text-secondary)">{{ acc.role }}</span>
                  <code class="text-navy-900 font-mono text-xs">{{ acc.email }}</code>
                </div>
              }
              <div class="flex justify-between">
                <span style="color: var(--text-secondary)">Password</span>
                <code class="text-navy-900 font-mono text-xs">password123</code>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class LoginComponent {
  email = '';
  password = '';
  loading = signal(false);
  error   = signal<string | null>(null);

  demoAccounts: DemoAccount[] = [
    { role: 'Admin',    email: 'admin@curatix.test' },
    { role: 'Reviewer', email: 'reviewer@curatix.test' },
    { role: 'Museum',   email: 'curator@louvre.test' },
  ];

  constructor(private auth: AuthService, private router: Router) {}

  submit(): void {
    if (!this.email || !this.password) return;
    this.loading.set(true);
    this.error.set(null);

    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error.set(err?.error?.error ?? 'Invalid credentials');
        this.loading.set(false);
      },
    });
  }
}