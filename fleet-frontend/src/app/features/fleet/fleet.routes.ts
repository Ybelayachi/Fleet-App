import { Routes } from '@angular/router';

export const FLEET_ROUTES: Routes = [
  {
    path: 'dashboard',
    loadComponent: () => import('./fleet-dashboard.component').then((m) => m.FleetDashboardComponent)
  }
];
