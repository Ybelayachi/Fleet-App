import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { MileageService } from '../../core/services/mileage.service';
import { Vehicle } from '../../core/models/vehicle.model';

@Component({
  selector: 'app-driver-vehicles',
  standalone: true,
  imports: [CommonModule, RouterLink, TableModule, ButtonModule],
  templateUrl: './driver-vehicles.component.html',
  styleUrl: './driver-vehicles.component.css'
})
export class DriverVehiclesComponent implements OnInit {
  vehicles: Vehicle[] = [];
  loading = false;

  constructor(
    private readonly mileage: MileageService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadVehicles();
  }

  private loadVehicles(): void {
    this.loading = true;
    this.mileage.getDriverVehicles().subscribe({
      next: (response) => {
        this.vehicles = response?.content || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des véhicules:', err);
        this.vehicles = [];
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
