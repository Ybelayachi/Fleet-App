import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { forkJoin } from 'rxjs';
import { MileageService } from '../../core/services/mileage.service';
import { VehicleService } from '../../core/services/vehicle.service';
import { MonthlyMileage } from '../../core/models/monthly-mileage.model';
import { Vehicle } from '../../core/models/vehicle.model';

interface FleetRow {
  vehicle: Vehicle;
  mileage?: MonthlyMileage;
  status: 'OK' | 'Missing';
  driver: string;
}

@Component({
  selector: 'app-fleet-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    ButtonModule,
    TableModule
  ],
  templateUrl: './fleet-dashboard.component.html',
  styleUrl: './fleet-dashboard.component.css'
})
export class FleetDashboardComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly mileageService = inject(MileageService);
  private readonly vehiclesService = inject(VehicleService);

  readonly now = new Date();

  readonly form = this.fb.group({
    year: [this.now.getFullYear(), [Validators.required, Validators.min(2000)]],
    month: [this.now.getMonth() + 1, [Validators.required, Validators.min(1), Validators.max(12)]]
  });

  rows: FleetRow[] = [];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const { year, month } = this.form.getRawValue();
    if (!year || !month) {
      return;
    }

    forkJoin({
      vehicles: this.vehiclesService.getFleetVehicles(),
      mileage: this.mileageService.getFleetMileage(year, month),
      missing: this.mileageService.getFleetMissing(year, month)
    }).subscribe(({ vehicles, mileage, missing }) => {
      const mileageMap = new Map((mileage?.content || []).map((entry: MonthlyMileage) => [entry.vehicle.id, entry]));
      const missingSet = new Set((missing?.content || []).map((v: Vehicle) => v.id));

      this.rows = (vehicles?.content || []).map((vehicle: Vehicle) => {
        const entry = mileageMap.get(vehicle.id);
        const driver = entry?.declaredBy
          ? `${entry.declaredBy.firstName ?? ''} ${entry.declaredBy.lastName ?? ''}`.trim() || entry.declaredBy.email
          : '-';
        const status: 'OK' | 'Missing' = entry && !missingSet.has(vehicle.id) ? 'OK' : 'Missing';

        return { vehicle, mileage: entry, status, driver };
      });
    });
  }

  exportCsv(): void {
    const { year, month } = this.form.getRawValue();
    if (!year || !month) {
      return;
    }

    this.mileageService.exportCsv(year, month).subscribe((csv) => {
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `fleet-mileage-${year}-${month}.csv`;
      link.click();
      URL.revokeObjectURL(url);
    });
  }
}
