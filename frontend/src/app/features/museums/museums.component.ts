import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MuseumService } from '../../core/services/museum.service';
import { AuthService } from '../../core/auth/auth.service';
import { Museum } from '../../core/models/museum.model';

@Component({
  selector: 'app-museums',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 class="text-3xl font-serif font-semibold">Museums</h1>
          <p class="mt-1" style="color: var(--text-secondary)">
            Partner museums in the Curatix network
          </p>
        </div>
        @if (auth.hasRole('ADMIN')) {
          <button (click)="openCreate.set(true)" class="btn-gold">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Add Museum
          </button>
        }
      </div>

      @if (error()) {
        <div class="mt-4 px-4 py-3 rounded-lg bg-red-50 text-red-700 text-sm border border-red-100">
          {{ error() }}
        </div>
      }

      @if (loading()) {
        <div class="mt-8 card text-center py-12" style="color: var(--text-muted)">
          Loading museums...
        </div>
      } @else if (museums().length === 0) {
        <div class="mt-8 card text-center py-12">
          <div class="text-lg font-serif text-navy-900 mb-2">No museums yet</div>
          <p style="color: var(--text-secondary)">
            Add your first museum to start building the network.
          </p>
        </div>
      } @else {
        <div class="mt-8 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          @for (m of museums(); track m.id) {
            <div class="card group">
              <div class="flex items-start justify-between mb-3">
                <div class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0"
                     style="background: #F0F4F8; color: #0A2540;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                       stroke="currentColor" stroke-width="2">
                    <path d="M3 21h18M5 21V7l7-4 7 4v14M9 9h2M9 13h2M9 17h2M15 9h2M15 13h2M15 17h2"/>
                  </svg>
                </div>
                <span class="text-[10px] uppercase tracking-widest font-semibold px-2 py-1 rounded"
                      style="background: #F0F4F8; color: #2D4A6B;">
                  #{{ m.id }}
                </span>
              </div>
              <h3 class="font-serif font-semibold text-lg text-navy-900 mb-1">
                {{ m.name }}
              </h3>
              <div class="text-sm" style="color: var(--text-secondary)">
                {{ m.country }}@if (m.city) { · {{ m.city }} }
              </div>
            </div>
          }
        </div>
      }
    </div>

    @if (openCreate()) {
      <div class="fixed inset-0 bg-navy-900/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
           (click)="openCreate.set(false)">
        <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 animate-fade-in"
             (click)="$event.stopPropagation()">
          <h2 class="text-2xl font-serif font-semibold text-navy-900 mb-1">Add Museum</h2>
          <p class="text-sm mb-5" style="color: var(--text-secondary)">
            Register a new partner museum
          </p>

          @if (formError()) {
            <div class="mb-4 px-4 py-3 rounded-lg bg-red-50 text-red-700 text-sm border border-red-100">
              {{ formError() }}
            </div>
          }

          <form (ngSubmit)="create()" class="space-y-4">
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Name *</label>
              <input class="input" [(ngModel)]="form.name" name="name" required
                     placeholder="e.g. Louvre Museum" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Country *</label>
              <input class="input" [(ngModel)]="form.country" name="country" required
                     placeholder="e.g. France" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">City</label>
              <input class="input" [(ngModel)]="form.city" name="city"
                     placeholder="e.g. Paris" />
            </div>
            <div class="flex gap-2 pt-2">
              <button type="button" (click)="openCreate.set(false)"
                      class="btn-secondary flex-1">Cancel</button>
              <button type="submit" [disabled]="saving()"
                      class="btn-primary flex-1">
                @if (saving()) { Saving... } @else { Create Museum }
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
})
export class MuseumsComponent implements OnInit {
  auth = inject(AuthService);
  private museumSvc = inject(MuseumService);

  museums = signal<Museum[]>([]);
  loading = signal(true);
  error   = signal<string | null>(null);
  openCreate = signal(false);
  saving = signal(false);
  formError = signal<string | null>(null);

  form = { name: '', country: '', city: '' };

  ngOnInit(): void { this.reload(); }

  reload(): void {
    this.loading.set(true);
    this.museumSvc.list().subscribe({
      next: (data) => { this.museums.set(data); this.loading.set(false); },
      error: (err) => { this.error.set(err?.message ?? 'Failed'); this.loading.set(false); },
    });
  }

  create(): void {
    if (!this.form.name || !this.form.country) return;
    this.saving.set(true);
    this.formError.set(null);
    this.museumSvc.create(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.openCreate.set(false);
        this.form = { name: '', country: '', city: '' };
        this.reload();
      },
      error: (err) => {
        this.formError.set(err?.error?.error ?? 'Failed to create');
        this.saving.set(false);
      },
    });
  }
}