import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoanService } from '../../core/services/loan.service';
import { ArtifactService } from '../../core/services/artifact.service';
import { AuthService } from '../../core/auth/auth.service';
import { Loan, LoanStatus } from '../../core/models/loan.model';
import { Artifact } from '../../core/models/artifact.model';

@Component({
  selector: 'app-loans',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 class="text-3xl font-serif font-semibold">Loan Requests</h1>
          <p class="mt-1" style="color: var(--text-secondary)">
            Manage artifact loans between museums
          </p>
        </div>
        @if (auth.hasRole('MUSEUM')) {
          <button (click)="openCreate.set(true)" class="btn-gold">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Request Loan
          </button>
        }
      </div>

      <!-- Status Filter -->
      <div class="mt-6 flex flex-wrap gap-2">
        <button (click)="setFilter(null)"
                class="px-3 py-1.5 rounded-lg text-xs font-medium transition-all"
                [ngClass]="filter() === null ? 'bg-navy-900 text-white' : 'bg-white text-navy-900 border'"
                [style.border-color]="filter() === null ? 'transparent' : 'var(--border)'">
          All ({{ loans().length }})
        </button>
        @for (s of statuses; track s) {
          <button (click)="setFilter(s)"
                  class="px-3 py-1.5 rounded-lg text-xs font-medium transition-all"
                  [ngClass]="filter() === s ? 'bg-navy-900 text-white' : 'bg-white text-navy-900 border'"
                  [style.border-color]="filter() === s ? 'transparent' : 'var(--border)'">
            {{ s | titlecase }} ({{ countByStatus(s) }})
          </button>
        }
      </div>

      @if (loading()) {
        <div class="mt-8 card text-center py-12" style="color: var(--text-muted)">
          Loading loans...
        </div>
      } @else if (filtered().length === 0) {
        <div class="mt-8 card text-center py-12">
          <div class="text-lg font-serif text-navy-900 mb-2">No loan requests</div>
          <p style="color: var(--text-secondary)">
            @if (auth.hasRole('MUSEUM')) {
              Request a loan from another museum's collection.
            } @else {
              No loans match the current filter.
            }
          </p>
        </div>
      } @else {
        <div class="mt-6 space-y-3">
          @for (loan of filtered(); track loan.id) {
            <div class="card">
              <div class="flex flex-col lg:flex-row lg:items-center gap-4">

                <!-- Artifact info -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap mb-1">
                    <h3 class="font-serif font-semibold text-lg text-navy-900">
                      {{ loan.artifactName }}
                    </h3>
                    <span class="badge" [ngClass]="statusClass(loan.status)">
                      {{ loan.status }}
                    </span>
                  </div>
                  <div class="text-sm" style="color: var(--text-secondary)">
                    <span class="font-medium text-navy-900">{{ loan.fromMuseumName }}</span>
                    <span class="mx-2">→</span>
                    <span class="font-medium text-navy-900">{{ loan.toMuseumName }}</span>
                  </div>
                  <div class="text-xs mt-1" style="color: var(--text-muted)">
                    Requested by {{ loan.requestedByName }} · {{ loan.createdAt | date:'short' }}
                  </div>
                  @if (loan.notes) {
                    <div class="text-xs mt-2 px-3 py-2 rounded-lg"
                         style="background: #F8F7F4; color: var(--text-secondary)">
                      {{ loan.notes }}
                    </div>
                  }
                </div>

                <!-- State machine progress -->
                <div class="flex items-center gap-1 lg:gap-2 shrink-0 order-3 lg:order-2">
                  @for (step of stateSteps; track step.key; let i = $index; let last = $last) {
                    <div class="flex items-center">
                      <div class="w-2 h-2 rounded-full"
                           [ngClass]="stepIndex(loan.status) >= i ? 'bg-navy-900' : 'bg-gray-300'">
                      </div>
                      @if (!last) {
                        <div class="w-4 lg:w-6 h-0.5"
                             [ngClass]="stepIndex(loan.status) > i ? 'bg-navy-900' : 'bg-gray-300'">
                        </div>
                      }
                    </div>
                  }
                </div>

                <!-- Actions -->
                <div class="flex flex-wrap gap-2 order-2 lg:order-3 shrink-0">
                  @if (loan.status === 'PENDING' && auth.hasRole('REVIEWER')) {
                    <button (click)="action('reject', loan.id)"
                            [disabled]="busy()"
                            class="px-3 py-2 rounded-lg text-xs font-medium bg-red-50 text-red-700
                                   hover:bg-red-100 transition-colors">
                      Reject
                    </button>
                    <button (click)="action('approve', loan.id)"
                            [disabled]="busy()"
                            class="px-3 py-2 rounded-lg text-xs font-medium bg-emerald-50 text-emerald-700
                                   hover:bg-emerald-100 transition-colors">
                      Approve
                    </button>
                  }
                  @if (loan.status === 'APPROVED' && auth.hasRole('MUSEUM')
                       && auth.user()?.userId !== loan.requestedById) {
                    <button (click)="action('ship', loan.id)"
                            [disabled]="busy()"
                            class="px-3 py-2 rounded-lg text-xs font-medium bg-blue-50 text-blue-700
                                   hover:bg-blue-100 transition-colors">
                      Ship Artifact
                    </button>
                  }
                  @if (loan.status === 'SHIPPED' && auth.hasRole('MUSEUM')
                       && auth.user()?.userId === loan.requestedById) {
                    <button (click)="action('return', loan.id)"
                            [disabled]="busy()"
                            class="px-3 py-2 rounded-lg text-xs font-medium bg-navy-900 text-white
                                   hover:bg-navy-700 transition-colors">
                      Return Artifact
                    </button>
                  }
                </div>
              </div>
            </div>
          }
        </div>
      }

      @if (error()) {
        <div class="mt-4 px-4 py-3 rounded-lg bg-red-50 text-red-700 text-sm border border-red-100">
          {{ error() }}
        </div>
      }
    </div>

    <!-- Create Modal -->
    @if (openCreate()) {
      <div class="fixed inset-0 bg-navy-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
           (click)="openCreate.set(false)">
        <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 animate-fade-in"
             (click)="$event.stopPropagation()">
          <h2 class="text-2xl font-serif font-semibold text-navy-900 mb-1">Request Loan</h2>
          <p class="text-sm mb-5" style="color: var(--text-secondary)">
            Choose an artifact to borrow for exhibition
          </p>

          @if (formError()) {
            <div class="mb-4 px-4 py-3 rounded-lg bg-red-50 text-red-700 text-sm border border-red-100">
              {{ formError() }}
            </div>
          }

          <form (ngSubmit)="create()" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Artifact *</label>
              <select class="input" [(ngModel)]="form.artifactId" name="artifactId" required>
                <option [ngValue]="0">Select an artifact...</option>
                @for (a of availableArtifacts(); track a.id) {
                  <option [ngValue]="a.id">
                    {{ a.name }} — {{ a.museumName }}
                  </option>
                }
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Notes</label>
              <textarea class="input" [(ngModel)]="form.notes" name="notes" rows="3"
                        placeholder="Purpose of the loan..."></textarea>
            </div>
            <div class="flex gap-2 pt-2">
              <button type="button" (click)="openCreate.set(false)"
                      class="btn-secondary flex-1">Cancel</button>
              <button type="submit" [disabled]="saving() || form.artifactId === 0"
                      class="btn-primary flex-1">
                @if (saving()) { Submitting... } @else { Submit Request }
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
})
export class LoansComponent implements OnInit {
  auth = inject(AuthService);
  private loanSvc = inject(LoanService);
  private artifactSvc = inject(ArtifactService);

  loans = signal<Loan[]>([]);
  artifacts = signal<Artifact[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  filter = signal<LoanStatus | null>(null);
  openCreate = signal(false);
  saving = signal(false);
  busy = signal(false);
  formError = signal<string | null>(null);

  form = { artifactId: 0, notes: '' };

  statuses: LoanStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'SHIPPED', 'RETURNED'];

  stateSteps = [
    { key: 'PENDING',  label: 'Pending' },
    { key: 'APPROVED', label: 'Approved' },
    { key: 'SHIPPED',  label: 'Shipped' },
    { key: 'RETURNED', label: 'Returned' },
  ];

  ngOnInit(): void {
    this.reload();
    this.artifactSvc.list().subscribe(d => this.artifacts.set(d));
  }

  reload(): void {
    this.loading.set(true);
    this.loanSvc.list().subscribe({
      next: (data) => { this.loans.set(data); this.loading.set(false); },
      error: (err) => {
        this.error.set(err?.message ?? 'Failed to load');
        this.loading.set(false);
      },
    });
  }

  filtered(): Loan[] {
    const f = this.filter();
    return f ? this.loans().filter(l => l.status === f) : this.loans();
  }

  setFilter(s: LoanStatus | null): void { this.filter.set(s); }

  countByStatus(s: LoanStatus): number {
    return this.loans().filter(l => l.status === s).length;
  }

  availableArtifacts(): Artifact[] {
    return this.artifacts().filter(a => a.status === 'AVAILABLE');
  }

  stepIndex(status: LoanStatus): number {
    const map: Record<LoanStatus, number> = {
      PENDING: 0, APPROVED: 1, SHIPPED: 2, RETURNED: 3, REJECTED: -1,
    };
    return map[status];
  }

  statusClass(s: LoanStatus): string {
    const map: Record<LoanStatus, string> = {
      PENDING:   'badge-pending',
      APPROVED:  'badge-approved',
      REJECTED:  'badge-rejected',
      SHIPPED:   'badge-shipped',
      RETURNED:  'badge-returned',
    };
    return map[s];
  }

  create(): void {
    if (this.form.artifactId === 0) return;
    this.saving.set(true);
    this.formError.set(null);
    this.loanSvc.create(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.openCreate.set(false);
        this.form = { artifactId: 0, notes: '' };
        this.reload();
        this.artifactSvc.list().subscribe(d => this.artifacts.set(d));
      },
      error: (err) => {
        this.formError.set(err?.error?.error ?? 'Failed to create loan');
        this.saving.set(false);
      },
    });
  }

  action(type: 'approve' | 'reject' | 'ship' | 'return', id: number): void {
    this.busy.set(true);
    const obs$ =
      type === 'approve' ? this.loanSvc.approve(id) :
      type === 'reject'  ? this.loanSvc.reject(id)  :
      type === 'ship'    ? this.loanSvc.ship(id)    :
                           this.loanSvc.returnLoan(id);

    obs$.subscribe({
      next: () => { this.busy.set(false); this.reload(); },
      error: (err) => {
        this.error.set(err?.error?.error ?? 'Action failed');
        this.busy.set(false);
      },
    });
  }
}