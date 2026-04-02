import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'users',
    loadComponent: () => import('./admin-users.component').then((m) => m.AdminUsersComponent)
  },
  {
    path: 'vehicles',
    loadComponent: () => import('./admin-vehicles.component').then((m) => m.AdminVehiclesComponent)
  },
  {
    path: 'assignments',
    loadComponent: () => import('./admin-assignments.component').then((m) => m.AdminAssignmentsComponent)
  }
];
