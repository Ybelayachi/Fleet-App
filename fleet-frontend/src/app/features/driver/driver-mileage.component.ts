import { ChangeDetectorRef, Component, OnInit, ViewChild, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroupDirective } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { MileageService } from '../../core/services/mileage.service';
import { Vehicle } from '../../core/models/vehicle.model';

@Component({
  selector: 'app-driver-mileage',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    SelectModule,
    ButtonModule
  ],
  templateUrl: './driver-mileage.component.html',
  styleUrl: './driver-mileage.component.css'
})
export class DriverMileageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly mileageService = inject(MileageService);
  private readonly route = inject(ActivatedRoute);
  private readonly cdr = inject(ChangeDetectorRef);

  vehicles: Vehicle[] = [];
  vehicleOptions: Array<{ label: string; value: number }> = [];
  successMessage: string | null = null;
  errorMessage: string | null = null;

  readonly now = new Date();
  readonly year = this.now.getFullYear();
  readonly month = this.now.getMonth() + 1;

  readonly form = this.fb.group({
    vehicleId: [null as number | null, [Validators.required]],
    mileage: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  @ViewChild(FormGroupDirective)
  private formDirective?: FormGroupDirective;

  ngOnInit(): void {
    this.mileageService.getDriverVehicles().subscribe((vehicles) => {
      this.vehicles = vehicles?.content || [];
      this.vehicleOptions = this.vehicles.map((vehicle) => ({
        label: `${vehicle.vin} - ${vehicle.licensePlate || 'N/A'}`,
        value: vehicle.id
      }));
      const selected = Number(this.route.snapshot.queryParamMap.get('vehicleId'));
      if (selected) {
        this.form.patchValue({ vehicleId: selected });
      }
    });
  }

  syncMileageFromDom(event: Event): void {
    const raw = (event.target as HTMLInputElement).value;
    const parsed = raw !== '' ? parseFloat(raw) : null;
    // Always patch from the DOM value on blur.
    // In zoneless Angular, sendKeys events may not update the FormControl via
    // NumberValueAccessor, so we explicitly sync on every blur event.
    this.form.patchValue({ mileage: parsed });
    this.cdr.markForCheck();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const vehicleId = this.form.getRawValue().vehicleId;
    const mileage = this.form.getRawValue().mileage;

    if (!vehicleId || mileage === null) {
      return;
    }

    this.successMessage = null;
    this.errorMessage = null;

    this.mileageService
      .declareMileage({
        vehicleId,
        year: this.year,
        month: this.month,
        mileage
      })
      .subscribe({
        next: () => {
          this.successMessage = 'Kilométrage enregistré avec succès.';
          // Reset form while keeping the selected vehicle
          const currentVehicleId = this.form.getRawValue().vehicleId;
          this.form.reset({ vehicleId: currentVehicleId, mileage: null });
          this.formDirective?.resetForm({ vehicleId: currentVehicleId, mileage: null });
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.errorMessage = this.extractErrorMessage(err);
          this.cdr.detectChanges();
        }
      });
  }

  private extractErrorMessage(err: unknown): string {
    const apiError = (err as { error?: unknown })?.error;
    if (typeof apiError === 'string') {
      try {
        const parsed = JSON.parse(apiError) as { reason?: string; message?: string; error?: string };
        return parsed.reason || parsed.message || parsed.error || apiError;
      } catch {
        return apiError;
      }
    }
    if (apiError && typeof apiError === 'object') {
      const parsed = apiError as { reason?: string; message?: string; error?: string };
      return parsed.reason || parsed.message || parsed.error || 'Impossible d\'enregistrer le kilométrage.';
    }
    return 'Impossible d\'enregistrer le kilométrage.';
  }
}
