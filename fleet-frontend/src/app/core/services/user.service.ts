import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, CreateUserPayload } from '../models/user.model';
import { Page, PaginationParams } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private readonly http: HttpClient) {}

  /**
   * Gets users with pagination
   * @param params pagination parameters (page, size, sort)
   * @returns Observable of paginated users
   */
  getUsers(params?: PaginationParams): Observable<Page<User>> {
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
    return this.http.get<Page<User>>(
      `${environment.apiBaseUrl}/api/admin/users`,
      { params: httpParams }
    );
  }

  createUser(user: CreateUserPayload): Observable<User> {
    return this.http.post<User>(`${environment.apiBaseUrl}/api/admin/users`, user);
  }
}
