import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MonthlyMileage, MileageRequest } from '../models/monthly-mileage.model';
import { Vehicle } from '../models/vehicle.model';
import { Page, PaginationParams } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MileageService {
  constructor(private readonly http: HttpClient) {}

  /**
   * Gets driver's vehicles with pagination
   * @param params pagination parameters (page, size, sort)
   * @returns Observable of paginated vehicles
   */
  getDriverVehicles(params?: PaginationParams): Observable<Page<Vehicle>> {
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
      `${environment.apiBaseUrl}/api/driver/vehicles`,
      { params: httpParams }
    );
  }

  declareMileage(payload: MileageRequest): Observable<MonthlyMileage> {
    return this.http.post<MonthlyMileage>(
      `${environment.apiBaseUrl}/api/driver/mileage`,
      payload
    );
  }

  /**
   * Gets driver mileage history with optional period filters and pagination.
   * @param year optional year filter
   * @param month optional month filter
   * @param params pagination parameters
   * @returns Observable of paginated driver mileage history
   */
  getDriverMileages(year?: number, month?: number,
      params?: PaginationParams): Observable<Page<MonthlyMileage>> {
    let httpParams = new HttpParams();

    if (year !== undefined && year !== null) {
      httpParams = httpParams.set('year', year.toString());
    }
    if (month !== undefined && month !== null) {
      httpParams = httpParams.set('month', month.toString());
    }
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

    return this.http.get<Page<MonthlyMileage>>(
      `${environment.apiBaseUrl}/api/driver/mileages`,
      { params: httpParams }
    );
  }

  /**
   * Gets fleet mileage with pagination
   * @param year the year
   * @param month the month
   * @param params pagination parameters
   * @returns Observable of paginated mileage data
   */
  getFleetMileage(year: number, month: number,
      params?: PaginationParams): Observable<Page<MonthlyMileage>> {
    let httpParams = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    
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
    return this.http.get<Page<MonthlyMileage>>(
      `${environment.apiBaseUrl}/api/fleet/mileage`,
      { params: httpParams }
    );
  }

  /**
   * Gets fleet missing vehicles with pagination
   * @param year the year
   * @param month the month
   * @param params pagination parameters
   * @returns Observable of paginated missing vehicles
   */
  getFleetMissing(year: number, month: number,
      params?: PaginationParams): Observable<Page<Vehicle>> {
    let httpParams = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    
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
      `${environment.apiBaseUrl}/api/fleet/missing`,
      { params: httpParams }
    );
  }

  exportCsv(year: number, month: number): Observable<string> {
    return this.http.get(
      `${environment.apiBaseUrl}/api/fleet/export?year=${year}&month=${month}`,
      { responseType: 'text' }
    );
  }
}
