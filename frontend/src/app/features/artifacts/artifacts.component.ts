import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ArtifactService } from '../../core/services/artifact.service';
import { MuseumService } from '../../core/services/museum.service';
import { AuthService } from '../../core/auth/auth.service';
import { Artifact } from '../../core/models/artifact.model';
import { Museum } from '../../core/models/museum.model';

@Component({
  selector: 'app-artifacts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="animate-fade-in">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <h1 class="text-3xl font-serif font-semibold">Artifacts</h1>
          <p class="mt-1" style="color: var(--text-secondary)">
            Browse and manage the collection
          </p>
        </div>
        @if (auth.hasRole('MUSEUM')) {
          <button (click)="openCreate.set(true)" class="btn-gold">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                 stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            Add Artifact
          </button>
        }
      </div>

      <!-- Filter -->
      <div class="mt-6 flex flex-wrap gap-2 items-center">
        <label class="text-sm font-medium mr-2" style="color: var(--text-secondary)">Filter:</label>
        <button (click)="setFilter(null)"
                class="px-3 py-1.5 rounded-lg text-xs font-medium transition-all"
                [ngClass]="filter() === null ? 'bg-navy-900 text-white' : 'bg-white text-navy-900 border'"
                [style.border-color]="filter() === null ? 'transparent' : 'var(--border)'">
          All
        </button>
        @for (m of museums(); track m.id) {
          <button (click)="setFilter(m.id)"
                  class="px-3 py-1.5 rounded-lg text-xs font-medium transition-all"
                  [ngClass]="filter() === m.id ? 'bg-navy-900 text-white' : 'bg-white text-navy-900 border'"
                  [style.border-color]="filter() === m.id ? 'transparent' : 'var(--border)'">
            {{ m.name }}
          </button>
        }
      </div>

      @if (loading()) {
        <div class="mt-8 card text-center py-12" style="color: var(--text-muted)">
          Loading artifacts...
        </div>
      } @else if (filtered().length === 0) {
        <div class="mt-8 card text-center py-12">
          <div class="text-lg font-serif text-navy-900 mb-2">No artifacts found</div>
          <p style="color: var(--text-secondary)">
            @if (auth.hasRole('MUSEUM')) {
              Add the first artifact to your museum's collection.
            } @else {
              Try changing the filter.
            }
          </p>
        </div>
      } @else {
        <div class="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          @for (a of filtered(); track a.id) {
            <div class="card">
              <div class="flex items-start justify-between mb-3">
                <div class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0"
                     style="background: #FBF7ED; color: #B08D45;">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none"
                       stroke="currentColor" stroke-width="2">
                    <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                  </svg>
                </div>
                <span class="badge"
                      [ngClass]="{
                        'badge-approved': a.status === 'AVAILABLE',
                        'badge-pending':  a.status === 'RESERVED',
                        'badge-shipped':  a.status === 'ON_LOAN'
                      }">
                  {{ a.status === 'ON_LOAN' ? 'On Loan' : a.status }}
                </span>
              </div>
              <h3 class="font-serif font-semibold text-lg text-navy-900 mb-1">{{ a.name }}</h3>
              <div class="text-sm mb-1" style="color: var(--text-secondary)">
                {{ a.museumName }}
              </div>
              @if (a.era) {
                <div class="text-xs" style="color: var(--text-muted)">{{ a.era }}</div>
              }
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
          <h2 class="text-2xl font-serif font-semibold text-navy-900 mb-1">Add Artifact</h2>
          <p class="text-sm mb-5" style="color: var(--text-secondary)">
            Register a new artifact to your museum's collection
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
                     placeholder="e.g. Mona Lisa" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Era</label>
              <input class="input" [(ngModel)]="form.era" name="era"
                     placeholder="e.g. 1503-1519" />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-navy-900">Description</label>
              <textarea class="input" [(ngModel)]="form.description" name="description"
                        rows="3" placeholder="Short description..."></textarea>
            </div>
            <div class="flex gap-2 pt-2">
              <button type="button" (click)="openCreate.set(false)"
                      class="btn-secondary flex-1">Cancel</button>
              <button type="submit" [disabled]="saving()"
                      class="btn-primary flex-1">
                @if (saving()) { Saving... } @else { Create Artifact }
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
})
export class ArtifactsComponent implements OnInit {
  auth = inject(AuthService);
  private artifactSvc = inject(ArtifactService);
  private museumSvc = inject(MuseumService);

  artifacts = signal<Artifact[]>([]);
  museums = signal<Museum[]>([]);
  loading = signal(true);
  filter = signal<number | null>(null);
  openCreate = signal(false);
  saving = signal(false);
  formError = signal<string | null>(null);

  form = { name: '', era: '', description: '' };

  ngOnInit(): void {
    forkJoin({
      artifacts: this.artifactSvc.list(),
      museums: this.museumSvc.list(),
    }).subscribe({
      next: ({ artifacts, museums }) => {
        this.artifacts.set(artifacts);
        this.museums.set(museums);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  setFilter(id: number | null): void { this.filter.set(id); }

  filtered(): Artifact[] {
    const f = this.filter();
    return f ? this.artifacts().filter(a => a.museumId === f) : this.artifacts();
  }

  create(): void {
    if (!this.form.name) return;
    this.saving.set(true);
    this.formError.set(null);
    this.artifactSvc.create(this.form).subscribe({
      next: () => {
        this.saving.set(false);
        this.openCreate.set(false);
        this.form = { name: '', era: '', description: '' };
        this.artifactSvc.list().subscribe(d => this.artifacts.set(d));
      },
      error: (err) => {
        this.formError.set(err?.error?.error ?? 'Failed to create');
        this.saving.set(false);
      },
    });
  }
}