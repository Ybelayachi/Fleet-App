import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Vehicle } from '../models/vehicle.model';
import { Page, PaginationParams } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class VehicleService {
  constructor(private readonly http: HttpClient) {}

  /**
   * Gets fleet vehicles with pagination
   * @param params pagination parameters (page, size, sort)
   * @returns Observable of paginated vehicles
   */
  getFleetVehicles(params?: PaginationParams): Observable<Page<Vehicle>> {
    let httpParams = new HttpParams();
    if (params) {
      if (params.page !== undefined) {
        httpParams = httpParams.set('page', params.page.toString());
      }
      if (params.size !== undefined) {
        httpParams = httpParams.set('size', params.size.toString());
      }
      if (params.sort) {
        httpParams = httpParams.set('sort', params.sort);
      }
    }
    return this.http.get<Page<Vehicle>>(
      `${environment.apiBaseUrl}/api/fleet/vehicles`,
      { params: httpParams }
    );
  }

  createVehicle(vehicle: Vehicle): Observable<Vehicle> {
    return this.http.post<Vehicle>(
      `${environment.apiBaseUrl}/api/admin/vehicles`,
      vehicle
    );
  }
}
