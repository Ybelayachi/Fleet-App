import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['login', 'ensureProfile']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);
    routerSpy.navigate.and.resolveTo(true);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not call login when form is invalid', () => {
    component.form.setValue({ email: '', password: '' });

    component.submit();

    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should navigate to admin users for ROLE_ADMIN', () => {
    component.form.setValue({ email: 'admin@fleet.com', password: 'secret' });
    authServiceSpy.login.and.returnValue(of({ token: 'fake-token' }));
    authServiceSpy.ensureProfile.and.returnValue(of('ROLE_ADMIN'));

    component.submit();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBeNull();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/admin/users']);
  });

  it('should navigate to fleet dashboard for ROLE_FLEET_MANAGER', () => {
    component.form.setValue({ email: 'fm@fleet.com', password: 'secret' });
    authServiceSpy.login.and.returnValue(of({ token: 'fake-token' }));
    authServiceSpy.ensureProfile.and.returnValue(of('ROLE_FLEET_MANAGER'));

    component.submit();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/fleet/dashboard']);
  });

  it('should navigate to driver vehicles for any other role', () => {
    component.form.setValue({ email: 'driver@fleet.com', password: 'secret' });
    authServiceSpy.login.and.returnValue(of({ token: 'fake-token' }));
    authServiceSpy.ensureProfile.and.returnValue(of('ROLE_DRIVER'));

    component.submit();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/driver/vehicles']);
  });

  it('should set error message when login fails', () => {
    component.form.setValue({ email: 'driver@fleet.com', password: 'wrong' });
    authServiceSpy.login.and.returnValue(throwError(() => new Error('bad credentials')));

    component.submit();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('E-mail ou mot de passe invalide.');
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });
});
