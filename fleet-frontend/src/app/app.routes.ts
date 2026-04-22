import { Routes } from '@angular/router';
import { ShellComponent } from './shared/components/shell/shell.component';
import { authGuard } from './core/auth/auth.guard';
import { roleGuard } from './core/auth/role.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/forgot-password/forgot-password.component').then((m) => m.ForgotPasswordComponent)
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'home'
      },
      {
        path: 'home',
        loadComponent: () => import('./features/home/home-redirect.component').then((m) => m.HomeRedirectComponent)
      },
      {
        path: 'driver',
        canActivate: [roleGuard('ROLE_DRIVER')],
        loadChildren: () => import('./features/driver/driver.routes').then((m) => m.DRIVER_ROUTES)
      },
      {
        path: 'fleet',
        canActivate: [roleGuard('ROLE_FLEET_MANAGER', 'ROLE_ADMIN')],
        loadChildren: () => import('./features/fleet/fleet.routes').then((m) => m.FLEET_ROUTES)
      },
      {
        path: 'admin',
        canActivate: [roleGuard('ROLE_ADMIN')],
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
