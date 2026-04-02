import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AssignmentRequest } from '../models/assignment.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AssignmentService {
  constructor(private readonly http: HttpClient) {}

  assignVehicle(payload: AssignmentRequest): Observable<unknown> {
    return this.http.post(`${environment.apiBaseUrl}/api/admin/assignments`, payload);
  }
}
