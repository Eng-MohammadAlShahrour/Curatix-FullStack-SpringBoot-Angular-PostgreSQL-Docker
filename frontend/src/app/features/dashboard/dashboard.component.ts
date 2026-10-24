import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { MuseumService } from '../../core/services/museum.service';
import { ArtifactService } from '../../core/services/artifact.service';
import { LoanService } from '../../core/services/loan.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="animate-fade-in">
      <h1 class="text-3xl font-serif font-semibold">Dashboard</h1>
      <p class="mt-1" style="color: var(--text-secondary)">
        Welcome back, {{ auth.user()?.fullName }}
      </p>

      <div class="mt-8 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="card">
          <div class="flex items-center justify-between">
            <div class="text-xs uppercase tracking-wider font-medium"
                 style="color: var(--text-muted)">Museums</div>
            <div class="w-8 h-8 rounded-lg flex items-center justify-center bg-navy-50 text-navy-900">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2">
                <path d="M3 21h18M5 21V7l7-4 7 4v14M9 9h2M9 13h2M9 17h2M15 9h2M15 13h2M15 17h2"/>
              </svg>
            </div>
          </div>
          <div class="mt-3 text-3xl font-serif font-semibold text-navy-900">
            @if (loading()) { â€” } @else { {{ stats().museums }} }
          </div>
        </div>

        <div class="card">
          <div class="flex items-center justify-between">
            <div class="text-xs uppercase tracking-wider font-medium"
                 style="color: var(--text-muted)">Artifacts</div>
            <div class="w-8 h-8 rounded-lg flex items-center justify-center"
                 style="background: #FBF7ED; color: #B08D45;">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
            </div>
          </div>
          <div class="mt-3 text-3xl font-serif font-semibold text-navy-900">
            @if (loading()) { â€” } @else { {{ stats().artifacts }} }
          </div>
        </div>

        <div class="card">
          <div class="flex items-center justify-between">
            <div class="text-xs uppercase tracking-wider font-medium"
                 style="color: var(--text-muted)">Active Loans</div>
            <div class="w-8 h-8 rounded-lg flex items-center justify-center"
                 style="background: #ECFDF5; color: #059669;">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
          </div>
          <div class="mt-3 text-3xl font-serif font-semibold text-emerald-700">
            @if (loading()) { â€” } @else { {{ stats().activeLoans }} }
          </div>
        </div>

        <div class="card">
          <div class="flex items-center justify-between">
            <div class="text-xs uppercase tracking-wider font-medium"
                 style="color: var(--text-muted)">Pending</div>
            <div class="w-8 h-8 rounded-lg flex items-center justify-center"
                 style="background: #FEF3C7; color: #D97706;">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
              </svg>
            </div>
          </div>
          <div class="mt-3 text-3xl font-serif font-semibold text-amber-700">
            @if (loading()) { â€” } @else { {{ stats().pendingLoans }} }
          </div>
        </div>
      </div>

      <div class="mt-8 grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div class="card">
          <h3 class="font-serif font-semibold text-lg mb-1">System Overview</h3>
          <p class="text-sm mb-4" style="color: var(--text-secondary)">
            Real-time status across all museums
          </p>
          <div class="space-y-3 text-sm">
            <div class="flex items-center justify-between">
              <span style="color: var(--text-secondary)">Available Artifacts</span>
              <span class="font-semibold text-emerald-700">{{ stats().availableArtifacts }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span style="color: var(--text-secondary)">On Loan</span>
              <span class="font-semibold text-blue-700">{{ stats().onLoanArtifacts }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span style="color: var(--text-secondary)">Reserved</span>
              <span class="font-semibold text-amber-700">{{ stats().reservedArtifacts }}</span>
            </div>
          </div>
        </div>

        <div class="card bg-navy-900 !text-white border-0">
          <h3 class="font-serif font-semibold text-lg mb-2 !text-white">Quick Actions</h3>
          <p class="text-sm mb-4 text-white/70">
            Jump to the most common tasks
          </p>
          <div class="space-y-2">
            <a routerLink="/artifacts" class="block w-full px-4 py-2.5 rounded-lg bg-white/10 hover:bg-white/20 transition-colors text-sm font-medium">
              -> Browse Artifacts
            </a>
            <a routerLink="/loans" class="block w-full px-4 py-2.5 rounded-lg bg-white/10 hover:bg-white/20 transition-colors text-sm font-medium">
              -> View Loan Requests
            </a>
            @if (auth.hasRole('ADMIN')) {
              <a routerLink="/museums" class="block w-full px-4 py-2.5 rounded-lg bg-gold-500/20 hover:bg-gold-500/30 transition-colors text-sm font-medium text-gold-400">
                -> Manage Museums
              </a>
            }
          </div>
        </div>
      </div>

      @if (error()) {
        <div class="mt-6 card border-red-200 bg-red-50">
          <div class="text-sm text-red-700">Failed to load stats: {{ error() }}</div>
        </div>
      }
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private museumSvc = inject(MuseumService);
  private artifactSvc = inject(ArtifactService);
  private loanSvc = inject(LoanService);

  loading = signal(true);
  error = signal<string | null>(null);
  stats = signal({
    museums: 0,
    artifacts: 0,
    activeLoans: 0,
    pendingLoans: 0,
    availableArtifacts: 0,
    onLoanArtifacts: 0,
    reservedArtifacts: 0,
  });

  ngOnInit(): void {
    forkJoin({
      museums: this.museumSvc.list(),
      artifacts: this.artifactSvc.list(),
      loans: this.loanSvc.list(),
    }).subscribe({
      next: ({ museums, artifacts, loans }) => {
        const pending = loans.filter(l => l.status === 'PENDING').length;
        const active = loans.filter(l =>
          l.status === 'APPROVED' || l.status === 'SHIPPED'
        ).length;
        const available = artifacts.filter(a => a.status === 'AVAILABLE').length;
        const onLoan = artifacts.filter(a => a.status === 'ON_LOAN').length;
        const reserved = artifacts.filter(a => a.status === 'RESERVED').length;

        this.stats.set({
          museums: museums.length,
          artifacts: artifacts.length,
          activeLoans: active,
          pendingLoans: pending,
          availableArtifacts: available,
          onLoanArtifacts: onLoan,
          reservedArtifacts: reserved,
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.message ?? 'Unknown error');
        this.loading.set(false);
      },
    });
  }
}