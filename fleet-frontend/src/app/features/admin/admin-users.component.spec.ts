import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';
import { Page } from '../../core/models/page.model';
import { AdminUsersComponent } from './admin-users.component';

describe('AdminUsersComponent', () => {
  let fixture: ComponentFixture<AdminUsersComponent>;
  let component: AdminUsersComponent;
  let userServiceSpy: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    userServiceSpy = jasmine.createSpyObj<UserService>('UserService', ['getUsers', 'createUser']);

    await TestBed.configureTestingModule({
      imports: [AdminUsersComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: UserService, useValue: userServiceSpy }
      ]
    }).compileComponents();
  });

  it('should create and load users on init', (done) => {
    userServiceSpy.getUsers.and.returnValue(of(userPage([
      { id: 1, email: 'admin@test.com', firstName: 'Admin', lastName: 'One', role: 'ROLE_ADMIN', active: true },
      { id: 2, email: 'driver@test.com', firstName: 'Driver', lastName: 'Two', role: 'ROLE_DRIVER', active: true }
    ])));

    fixture = TestBed.createComponent(AdminUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    setTimeout(() => {
      expect(component).toBeTruthy();
      expect(userServiceSpy.getUsers).toHaveBeenCalledWith({ page: 0, size: 10000 });
      expect(component.allUsers.length).toBe(2);
      expect(component.displayedUsers.length).toBe(2);
      done();
    }, 0);
  });

  it('should filter users by search term', (done) => {
    userServiceSpy.getUsers.and.returnValue(of(userPage([
      { id: 1, email: 'admin@test.com', firstName: 'Admin', lastName: 'One' },
      { id: 2, email: 'driver@test.com', firstName: 'Driver', lastName: 'Two' }
    ])));

    fixture = TestBed.createComponent(AdminUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    setTimeout(() => {
      component.searchTerm = 'driver';
      component.filterUsers();

      expect(component.filteredUsers.length).toBe(1);
      expect(component.filteredUsers[0].email).toBe('driver@test.com');
      done();
    }, 0);
  });

  it('should create user and reload list when form is valid', (done) => {
    userServiceSpy.getUsers.and.returnValue(of(userPage([])));
    userServiceSpy.createUser.and.returnValue(of({ id: 3, email: 'new@test.com' } as User));

    fixture = TestBed.createComponent(AdminUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    setTimeout(() => {
      component.form.patchValue({
        email: 'new@test.com',
        firstName: 'New',
        lastName: 'User',
        role: 'ROLE_DRIVER',
        password: 'secret',
        active: true
      });

      component.create();

      setTimeout(() => {
        expect(userServiceSpy.createUser).toHaveBeenCalled();
        expect(userServiceSpy.getUsers).toHaveBeenCalledTimes(2);
        expect(component.form.get('role')?.value).toBe('ROLE_DRIVER');
        done();
      }, 0);
    }, 0);
  });

  it('should not create user when form is invalid', (done) => {
    userServiceSpy.getUsers.and.returnValue(of(userPage([])));

    fixture = TestBed.createComponent(AdminUsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    setTimeout(() => {
      component.form.patchValue({ email: '', password: '' });
      component.create();

      expect(userServiceSpy.createUser).not.toHaveBeenCalled();
      done();
    }, 0);
  });
});

function userPage(content: User[]): Page<User> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    size: content.length,
    number: 0,
    numberOfElements: content.length,
    first: true,
    last: true,
    empty: content.length === 0
  };
}
