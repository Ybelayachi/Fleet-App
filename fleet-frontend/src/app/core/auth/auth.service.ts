import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthResponse, JwtPayload } from '../models/auth.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  private readonly tokenKey = 'fleet_token';
  private readonly roleKey = 'fleet_role';

  private readonly tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
  private readonly roleSubject = new BehaviorSubject<string | null>(this.getStoredRole());

  readonly token$ = this.tokenSubject.asObservable();
  readonly role$ = this.roleSubject.asObservable();

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  forgotPassword(email: string): Observable<{ message: string; token: string }> {
    return this.http.post<{ message: string; token: string }>(
      `${environment.apiBaseUrl}/api/auth/forgot-password`,
      { email }
    );
  }

  resetPassword(token: string, newPassword: string): Observable<{ status: string }> {
    return this.http.post<{ status: string }>(
      `${environment.apiBaseUrl}/api/auth/reset-password`,
      { token, newPassword }
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/api/auth/login`, {
        email,
        password
      })
      .pipe(
        tap((res) => this.setToken(res.token))
      );
  }

  logout(): void {
    this.clearToken();
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);
  }

  getToken(): string | null {
    return this.tokenSubject.value;
  }

  getRole(): string | null {
    return this.roleSubject.value;
  }

  ensureProfile(): Observable<string | null> {
    const currentRole = this.getRole();
    if (currentRole) {
      return of(currentRole);
    }

    const email = this.getEmailFromToken();
    if (!email) {
      return of(null);
    }

    return this.http.get<{ email: string; role: string }>(`${environment.apiBaseUrl}/api/auth/me`).pipe(
      map((profile) => profile?.role ?? null),
      tap((role) => this.setRole(role)),
      catchError(() => of(null))
    );
  }

  getEmailFromToken(): string | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }
    const payload = this.decodeToken(token);
    return payload?.sub ?? null;
  }

  private setToken(token: string): void {
    if (this.isBrowser) {
      localStorage.setItem(this.tokenKey, token);
    }
    this.tokenSubject.next(token);
  }

  private clearToken(): void {
    if (this.isBrowser) {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.roleKey);
    }
    this.tokenSubject.next(null);
    this.roleSubject.next(null);
  }

  private setRole(role: string | null): void {
    if (this.isBrowser) {
      if (role) {
        localStorage.setItem(this.roleKey, role);
      } else {
        localStorage.removeItem(this.roleKey);
      }
    }
    this.roleSubject.next(role);
  }

  private getStoredToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem(this.tokenKey);
  }

  private getStoredRole(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem(this.roleKey);
  }

  private decodeToken(token: string): JwtPayload | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        return null;
      }
      const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(payload)
          .split('')
          .map((c) => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`)
          .join('')
      );
      return JSON.parse(json) as JwtPayload;
    } catch {
      return null;
    }
  }

  private isTokenExpired(token: string): boolean {
    const payload = this.decodeToken(token);
    if (!payload?.exp) {
      return false;
    }
    const now = Math.floor(Date.now() / 1000);
    return payload.exp <= now;
  }
}
