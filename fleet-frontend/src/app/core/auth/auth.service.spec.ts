import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';

function createJwt(payload: Record<string, unknown>): string {
  const header = { alg: 'HS256', typ: 'JWT' };
  const base64Url = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');

  return `${base64Url(header)}.${base64Url(payload)}.signature`;
}

describe('AuthService', () => {
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        provideZonelessChangeDetection()
      ]
    });

    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    const service = TestBed.inject(AuthService);
    expect(service).toBeTruthy();
  });

  it('should login, persist token and become authenticated', () => {
    const service = TestBed.inject(AuthService);
    const token = createJwt({ sub: 'user@fleet.com', exp: Math.floor(Date.now() / 1000) + 3600 });

    service.login('user@fleet.com', 'secret').subscribe((res) => {
      expect(res.token).toBe(token);
      expect(service.getToken()).toBe(token);
      expect(service.isAuthenticated()).toBeTrue();
      expect(localStorage.getItem('fleet_token')).toBe(token);
    });

    const req = httpMock.expectOne('/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'user@fleet.com', password: 'secret' });
    req.flush({ token });
  });

  it('should logout, clear storage and navigate to login', () => {
    localStorage.setItem('fleet_token', createJwt({ sub: 'user@fleet.com' }));
    localStorage.setItem('fleet_role', 'ROLE_ADMIN');

    const service = TestBed.inject(AuthService);
    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.getRole()).toBeNull();
    expect(localStorage.getItem('fleet_token')).toBeNull();
    expect(localStorage.getItem('fleet_role')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should extract email from token payload', () => {
    const token = createJwt({ sub: 'driver@fleet.com', exp: Math.floor(Date.now() / 1000) + 3600 });
    localStorage.setItem('fleet_token', token);

    const service = TestBed.inject(AuthService);
    expect(service.getEmailFromToken()).toBe('driver@fleet.com');
  });

  it('ensureProfile should return current role without HTTP call', () => {
    localStorage.setItem('fleet_role', 'ROLE_FLEET_MANAGER');
    const service = TestBed.inject(AuthService);

    service.ensureProfile().subscribe((role) => {
      expect(role).toBe('ROLE_FLEET_MANAGER');
    });

    httpMock.expectNone('/api/auth/me');
  });

  it('ensureProfile should fetch role from /me and store it', () => {
    localStorage.setItem('fleet_token', createJwt({ sub: 'admin@fleet.com', exp: Math.floor(Date.now() / 1000) + 3600 }));
    const service = TestBed.inject(AuthService);

    service.ensureProfile().subscribe((role) => {
      expect(role).toBe('ROLE_ADMIN');
      expect(service.getRole()).toBe('ROLE_ADMIN');
    });

    const req = httpMock.expectOne('/api/auth/me');
    expect(req.request.method).toBe('GET');
    req.flush({ email: 'admin@fleet.com', role: 'ROLE_ADMIN' });
  });
});
