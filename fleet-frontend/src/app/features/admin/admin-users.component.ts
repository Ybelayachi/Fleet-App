import { Component, OnInit, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { UserService } from '../../core/services/user.service';
import { User, CreateUserPayload } from '../../core/models/user.model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    InputTextModule,
    SelectModule,
    ButtonModule,
    ToggleSwitchModule
  ],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usersService = inject(UserService);
  private readonly cdr = inject(ChangeDetectorRef);
  allUsers: User[] = [];  // Complete list of all users
  filteredUsers: User[] = [];  // Results after search filter
  displayedUsers: User[] = [];  // Current page of filtered users
  searchTerm = '';

  readonly roleOptions = [
    { label: 'Conducteur', value: 'ROLE_DRIVER' },
    { label: 'Gestionnaire de flotte', value: 'ROLE_FLEET_MANAGER' },
    { label: 'Administrateur', value: 'ROLE_ADMIN' }
  ];
  
  // Pagination for filtered results
  currentPage = 0;
  pageSize = 20;
  filteredTotalPages = 0;
  filteredTotalElements = 0;

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    firstName: [''],
    lastName: [''],
    role: ['ROLE_DRIVER', [Validators.required]],
    password: ['', [Validators.required]],
    active: [true]
  });

  ngOnInit(): void {
    this.loadAllUsers();
  }

  loadAllUsers(): void {
    this.usersService.getUsers({ 
      page: 0, 
      size: 10000
    })
      .subscribe((response) => {
        this.allUsers = response?.content || [];
        this.filterUsers();
        this.cdr.markForCheck();
      });
  }

  updateDisplayedUsers(): void {
    const startIndex = this.currentPage * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedUsers = this.filteredUsers.slice(startIndex, endIndex);
  }

  nextPage(): void {
    if (this.currentPage < this.filteredTotalPages - 1) {
      this.currentPage++;
      this.updateDisplayedUsers();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.updateDisplayedUsers();
    }
  }

  create(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.usersService.createUser(this.form.getRawValue() as CreateUserPayload).subscribe(() => {
      this.form.reset({ role: 'ROLE_DRIVER', active: true });
      this.loadAllUsers();  // Reload all users after creation
    });
  }

  filterUsers(): void {
    if (!this.searchTerm.trim()) {
      this.filteredUsers = [...this.allUsers];
    } else {
      const term = this.searchTerm.toLowerCase();
      this.filteredUsers = this.allUsers.filter(user =>
        user.email.toLowerCase().includes(term) ||
        (user.firstName?.toLowerCase().includes(term)) ||
        (user.lastName?.toLowerCase().includes(term))
      );
    }
    
    // Calculate pagination for filtered results
    this.filteredTotalElements = this.filteredUsers.length;
    this.filteredTotalPages = Math.ceil(this.filteredTotalElements / this.pageSize);
    this.currentPage = 0;
    this.updateDisplayedUsers();
    this.cdr.markForCheck();
  }

  onSearchChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm = input.value;
    this.filterUsers();
  }
}
