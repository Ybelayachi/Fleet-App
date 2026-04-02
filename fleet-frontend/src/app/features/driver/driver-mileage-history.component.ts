import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { finalize } from 'rxjs/operators';
import { MileageService } from '../../core/services/mileage.service';
import { MonthlyMileage } from '../../core/models/monthly-mileage.model';

@Component({
  selector: 'app-driver-mileage-history',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    InputTextModule,
    SelectModule,
    ButtonModule
  ],
  templateUrl: './driver-mileage-history.component.html',
  styleUrl: './driver-mileage-history.component.css'
})
export class DriverMileageHistoryComponent implements OnInit {
  private static readonly START_YEAR = 2024;

  private readonly mileageService = inject(MileageService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly now = new Date();

  readonly yearOptions: Array<{ label: string; value: number }> = Array.from(
    { length: this.now.getFullYear() - DriverMileageHistoryComponent.START_YEAR + 1 },
    (_, index) => {
      const year = DriverMileageHistoryComponent.START_YEAR + index;
      return { label: year.toString(), value: year };
    }
  );

  readonly monthOptions: Array<{ label: string; value: number }> = Array.from(
    { length: 12 },
    (_, index) => {
      const month = index + 1;
      return { label: month.toString(), value: month };
    }
  );

  readonly form = this.fb.group({
    year: [null as number | null],
    month: [null as number | null]
  });

  history: MonthlyMileage[] = [];
  loading = false;

  ngOnInit(): void {
    this.load();
    this.form.valueChanges.subscribe(() => this.load());
  }

  load(): void {
    const { year, month } = this.form.getRawValue();
    const yearFilter = year ?? undefined;
    const monthFilter = month ?? undefined;

    this.loading = true;

    this.mileageService.getDriverMileages(yearFilter, monthFilter, {
      page: 0,
      size: 200,
      sort: 'year,desc'
    }).pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (page) => {
        this.history = page?.content || [];
      },
      error: () => {
        this.history = [];
      }
    });
  }

  clearFilters(): void {
    this.form.patchValue({ year: null, month: null });
  }
}
