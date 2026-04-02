import { Routes } from '@angular/router';

export const DRIVER_ROUTES: Routes = [
  {
    path: 'vehicles',
    loadComponent: () => import('./driver-vehicles.component').then((m) => m.DriverVehiclesComponent)
  },
  {
    path: 'mileage',
    loadComponent: () => import('./driver-mileage.component').then((m) => m.DriverMileageComponent)
  },
  {
    path: 'history',
    loadComponent: () => import('./driver-mileage-history.component').then((m) => m.DriverMileageHistoryComponent)
  }
];
