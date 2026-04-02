import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { AssignmentService } from '../../core/services/assignment.service';
import { UserService } from '../../core/services/user.service';
import { VehicleService } from '../../core/services/vehicle.service';
import { User } from '../../core/models/user.model';
import { Vehicle } from '../../core/models/vehicle.model';

@Component({
  selector: 'app-admin-assignments',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    ButtonModule
  ],
  templateUrl: './admin-assignments.component.html',
  styleUrl: './admin-assignments.component.css'
})
export class AdminAssignmentsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly assignments = inject(AssignmentService);
  private readonly usersService = inject(UserService);
  private readonly vehiclesService = inject(VehicleService);

  allUsers: User[] = [];
  allVehicles: Vehicle[] = [];
  filteredUsers: User[] = [];
  filteredVehicles: Vehicle[] = [];
  
  selectedUser: User | null = null;
  selectedVehicle: Vehicle | null = null;
  
  userSearchTerm = '';
  vehicleSearchTerm = '';
  message: string | null = null;

  readonly form = this.fb.group({
    userId: [null as number | null],
    vehicleId: [null as number | null]
  });

  ngOnInit(): void {
    this.usersService.getUsers({ page: 0, size: 10000 }).subscribe((response) => {
      this.allUsers = response?.content || [];
      this.filteredUsers = [...this.allUsers];
    });
    this.vehiclesService.getFleetVehicles({ page: 0, size: 10000 }).subscribe((response) => {
      this.allVehicles = response?.content || [];
      this.filteredVehicles = [...this.allVehicles];
    });
  }

  filterUsers(searchTerm: string): void {
    this.userSearchTerm = searchTerm;
    if (!searchTerm.trim()) {
      this.filteredUsers = [...this.allUsers];
    } else {
      const term = searchTerm.toLowerCase();
      this.filteredUsers = this.allUsers.filter(user =>
        user.email.toLowerCase().includes(term) ||
        (user.firstName?.toLowerCase().includes(term)) ||
        (user.lastName?.toLowerCase().includes(term))
      );
    }
  }

  onUserInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.filterUsers(input.value);
  }

  filterVehicles(searchTerm: string): void {
    this.vehicleSearchTerm = searchTerm;
    if (!searchTerm.trim()) {
      this.filteredVehicles = [...this.allVehicles];
    } else {
      const term = searchTerm.toLowerCase();
      this.filteredVehicles = this.allVehicles.filter(vehicle =>
        vehicle.licensePlate?.toLowerCase().includes(term)
      );
    }
  }

  onVehicleInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.filterVehicles(input.value);
  }

  selectUser(user: User): void {
    this.selectedUser = user;
    this.userSearchTerm = `${user.email}`;
    this.filteredUsers = [];
    this.form.patchValue({ userId: user.id });
  }

  selectVehicle(vehicle: Vehicle): void {
    this.selectedVehicle = vehicle;
    this.vehicleSearchTerm = `${vehicle.licensePlate}`;
    this.filteredVehicles = [];
    this.form.patchValue({ vehicleId: vehicle.id });
  }

  clearUserSelection(): void {
    this.selectedUser = null;
    this.userSearchTerm = '';
    this.form.patchValue({ userId: null });
    this.filteredUsers = [...this.allUsers];
  }

  clearVehicleSelection(): void {
    this.selectedVehicle = null;
    this.vehicleSearchTerm = '';
    this.form.patchValue({ vehicleId: null });
    this.filteredVehicles = [...this.allVehicles];
  }

  onUserSearchFocus(): void {
    if (this.filteredUsers.length === 0) {
      this.filteredUsers = [...this.allUsers];
    }
  }

  onVehicleSearchFocus(): void {
    if (this.filteredVehicles.length === 0) {
      this.filteredVehicles = [...this.allVehicles];
    }
  }

  assign(): void {
    if (!this.selectedUser || !this.selectedVehicle) {
      this.message = 'Veuillez sélectionner un utilisateur et un véhicule.';
      return;
    }

    this.assignments.assignVehicle({
      userId: this.selectedUser.id,
      vehicleId: this.selectedVehicle.id
    }).subscribe({
      next: () => {
        this.message = 'Affectation enregistrée.';
        this.clearUserSelection();
        this.clearVehicleSelection();
        setTimeout(() => (this.message = null), 3000);
      },
      error: () => {
        this.message = 'Impossible d\'enregistrer l\'affectation.';
        setTimeout(() => (this.message = null), 3000);
      }
    });
  }
}
