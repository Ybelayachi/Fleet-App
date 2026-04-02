import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroupDirective } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { VehicleService } from '../../core/services/vehicle.service';
import { Vehicle } from '../../core/models/vehicle.model';

@Component({
  selector: 'app-admin-vehicles',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    ToggleSwitchModule
  ],
  templateUrl: './admin-vehicles.component.html',
  styleUrl: './admin-vehicles.component.css'
})
export class AdminVehiclesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly vehiclesService = inject(VehicleService);
  vehicles: Vehicle[] = [];
  successMessage: string | null = null;

  @ViewChild(FormGroupDirective)
  private formDirective?: FormGroupDirective;

  readonly form = this.fb.group({
    vin: ['', [Validators.required]],
    brand: [''],
    model: [''],
    licensePlate: [''],
    inServiceDate: [''],
    active: [true]
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.vehiclesService.getFleetVehicles().subscribe((vehicles) => (this.vehicles = vehicles?.content || []));
  }

  create(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.vehiclesService.createVehicle(this.form.getRawValue() as Vehicle).subscribe({
      next: () => {
        this.successMessage = 'Véhicule créé avec succès.';
        this.form.reset({ active: true });
        this.formDirective?.resetForm({ active: true });
        // Reload list after a slight delay to avoid expression changed error
        setTimeout(() => {
          this.load();
        }, 100);
        // Clear success message after 5 seconds
        setTimeout(() => {
          this.successMessage = null;
        }, 5000);
      },
      error: (err) => {
        console.error('Error creating vehicle:', err);
      }
    });
  }
}
