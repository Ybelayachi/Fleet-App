import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { roleGuard } from './role.guard';
import { AuthService } from './auth.service';

describe('roleGuard', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let loginUrlTree: UrlTree;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['ensureProfile']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['parseUrl']);
    loginUrlTree = {} as UrlTree;
    routerSpy.parseUrl.and.returnValue(loginUrlTree);

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  it('should allow navigation when role is authorized', (done) => {
    authServiceSpy.ensureProfile.and.returnValue(of('ROLE_ADMIN'));

    const guard = roleGuard('ROLE_ADMIN', 'ROLE_FLEET_MANAGER');
    const result$ = TestBed.runInInjectionContext(
      () => guard({} as never, {} as never)
    ) as Observable<boolean | UrlTree>;

    result$.subscribe((result) => {
      expect(result).toBeTrue();
      expect(routerSpy.parseUrl).not.toHaveBeenCalled();
      done();
    });
  });

  it('should redirect to /login when role is not authorized', (done) => {
    authServiceSpy.ensureProfile.and.returnValue(of('ROLE_DRIVER'));

    const guard = roleGuard('ROLE_ADMIN');
    const result$ = TestBed.runInInjectionContext(
      () => guard({} as never, {} as never)
    ) as Observable<boolean | UrlTree>;

    result$.subscribe((result) => {
      expect(result).toBe(loginUrlTree);
      expect(routerSpy.parseUrl).toHaveBeenCalledWith('/login');
      done();
    });
  });

  it('should redirect to /login when role is missing', (done) => {
    authServiceSpy.ensureProfile.and.returnValue(of(null));

    const guard = roleGuard('ROLE_ADMIN');
    const result$ = TestBed.runInInjectionContext(
      () => guard({} as never, {} as never)
    ) as Observable<boolean | UrlTree>;

    result$.subscribe((result) => {
      expect(result).toBe(loginUrlTree);
      expect(routerSpy.parseUrl).toHaveBeenCalledWith('/login');
      done();
    });
  });
});
