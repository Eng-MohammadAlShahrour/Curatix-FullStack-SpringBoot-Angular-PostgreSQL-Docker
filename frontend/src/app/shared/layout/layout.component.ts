import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth/auth.service';
import { LogoComponent } from '../logo/logo.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, LogoComponent],
  template: `
    <div class="min-h-screen bg-cream">

      <!-- Mobile top bar -->
      <header class="lg:hidden sticky top-0 z-40 bg-navy-900 text-white px-4 py-3 flex items-center justify-between shadow-md">
        <div class="flex items-center gap-2.5">
          <app-logo [size]="28"></app-logo>
          <div>
            <div class="font-serif font-semibold text-[15px] leading-tight">Curatix</div>
            <div class="text-[10px] text-white/60 uppercase tracking-wider">Museum Loans</div>
          </div>
        </div>
        <button (click)="menuOpen.set(!menuOpen())"
                class="p-2 -mr-2 rounded-lg hover:bg-white/10 transition-colors"
                aria-label="Toggle menu">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            @if (!menuOpen()) {
              <line x1="3" y1="6" x2="21" y2="6"/>
              <line x1="3" y1="12" x2="21" y2="12"/>
              <line x1="3" y1="18" x2="21" y2="18"/>
            } @else {
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            }
          </svg>
        </button>
      </header>

      <!-- Overlay -->
      @if (menuOpen()) {
        <div (click)="menuOpen.set(false)"
             class="lg:hidden fixed inset-0 bg-navy-900/60 z-40 backdrop-blur-sm"></div>
      }

      <div class="flex">
        <!-- Sidebar -->
        <aside
          class="fixed lg:sticky top-0 left-0 bottom-0 lg:top-0 lg:h-screen w-72 bg-navy-900 text-white z-50
                 transition-transform duration-300 flex flex-col shrink-0"
          [class.-translate-x-full]="!menuOpen()"
          [class.lg:translate-x-0]="true">

          <div class="p-5 flex items-center gap-3 border-b border-white/10">
            <app-logo [size]="40"></app-logo>
            <div>
              <div class="font-serif font-semibold text-lg tracking-tight">Curatix</div>
              <div class="text-[10px] text-white/60 uppercase tracking-widest">Museum Loans</div>
            </div>
          </div>

          <nav class="flex-1 p-3 space-y-1 overflow-y-auto">
            <div class="px-3 py-2 text-[10px] uppercase tracking-widest text-white/40 font-medium">
              Workspace
            </div>

            <a routerLink="/dashboard" routerLinkActive="bg-white/10 text-white"
               [routerLinkActiveOptions]="{exact: true}"
               (click)="menuOpen.set(false)"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-white/80
                      hover:bg-white/5 hover:text-white transition-all">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="7" rx="1"/>
                <rect x="14" y="3" width="7" height="7" rx="1"/>
                <rect x="14" y="14" width="7" height="7" rx="1"/>
                <rect x="3" y="14" width="7" height="7" rx="1"/>
              </svg>
              Dashboard
            </a>

            <a routerLink="/museums" routerLinkActive="bg-white/10 text-white"
               (click)="menuOpen.set(false)"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-white/80
                      hover:bg-white/5 hover:text-white transition-all">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 21h18M5 21V7l7-4 7 4v14M9 9h2M9 13h2M9 17h2M15 9h2M15 13h2M15 17h2"/>
              </svg>
              Museums
            </a>

            <a routerLink="/artifacts" routerLinkActive="bg-white/10 text-white"
               (click)="menuOpen.set(false)"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-white/80
                      hover:bg-white/5 hover:text-white transition-all">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
              Artifacts
            </a>

            <a routerLink="/loans" routerLinkActive="bg-white/10 text-white"
               (click)="menuOpen.set(false)"
               class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-white/80
                      hover:bg-white/5 hover:text-white transition-all">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="9" y1="15" x2="15" y2="15"/>
                <line x1="9" y1="11" x2="15" y2="11"/>
              </svg>
              Loans
            </a>
          </nav>

          <div class="p-3 border-t border-white/10">
            <div class="px-3 py-2 mb-2">
              <div class="text-sm font-medium truncate">{{ auth.user()?.fullName }}</div>
              <div class="text-[11px] text-white/60 truncate">{{ auth.user()?.email }}</div>
              <div class="mt-1.5 inline-flex items-center px-2 py-0.5 rounded text-[10px]
                          uppercase tracking-wider font-semibold
                          bg-gold-500/20 text-gold-400">
                {{ auth.user()?.role }}
              </div>
            </div>
            <button (click)="auth.logout()"
                    class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
                           text-red-300 hover:bg-red-500/10 transition-colors">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
              Sign out
            </button>
          </div>
        </aside>

        <!-- Main -->
        <main class="flex-1 min-w-0">
          <div class="max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
            <router-outlet />
          </div>
        </main>
      </div>
    </div>
  `,
})
export class LayoutComponent {
  menuOpen = signal(false);
  constructor(public auth: AuthService) {}
}