import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/layout/layout.component').then(m => m.LayoutComponent),
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
      },
      {
        path: 'museums',
        loadComponent: () =>
          import('./features/museums/museums.component').then(m => m.MuseumsComponent),
      },
      {
        path: 'artifacts',
        loadComponent: () =>
          import('./features/artifacts/artifacts.component').then(m => m.ArtifactsComponent),
      },
      {
        path: 'loans',
        loadComponent: () =>
          import('./features/loans/loans.component').then(m => m.LoansComponent),
      },
      {
        path: 'admin/users',
        loadComponent: () =>
          import('./features/users/users.component').then(m => m.UsersComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];