import { ChangeDetectorRef } from '@angular/core';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MileageService } from '../../core/services/mileage.service';
import { MonthlyMileage, MileageRequest } from '../../core/models/monthly-mileage.model';
import { Vehicle } from '../../core/models/vehicle.model';
import { Page } from '../../core/models/page.model';
import { DriverMileageComponent } from './driver-mileage.component';

describe('DriverMileageComponent', () => {
  let fixture: ComponentFixture<DriverMileageComponent>;
  let component: DriverMileageComponent;
  let mileageServiceSpy: jasmine.SpyObj<MileageService>;
  let detectChangesSpy: jasmine.Spy;

  beforeEach(async () => {
    mileageServiceSpy = jasmine.createSpyObj<MileageService>('MileageService', [
      'getDriverVehicles',
      'declareMileage'
    ]);

    await TestBed.configureTestingModule({
      imports: [DriverMileageComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: MileageService, useValue: mileageServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap({ vehicleId: '1' })
            }
          }
        }
      ]
    }).compileComponents();
  });

  it('should create and load vehicles with selected query param', () => {
    mileageServiceSpy.getDriverVehicles.and.returnValue(of(vehiclePage([
      { id: 1, vin: 'VIN-1', licensePlate: 'AA-111-BB', brand: 'Tesla', model: 'Model 3' }
    ])));

    fixture = TestBed.createComponent(DriverMileageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(mileageServiceSpy.getDriverVehicles).toHaveBeenCalled();
    expect(component.vehicles.length).toBe(1);
    expect(component.form.get('vehicleId')?.value).toBe(1);
  });

  it('should not submit when form is invalid', () => {
    mileageServiceSpy.getDriverVehicles.and.returnValue(of(vehiclePage([])));

    fixture = TestBed.createComponent(DriverMileageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.submit();

    expect(mileageServiceSpy.declareMileage).not.toHaveBeenCalled();
  });

  it('should submit mileage and show success message', () => {
    mileageServiceSpy.getDriverVehicles.and.returnValue(of(vehiclePage([
      { id: 1, vin: 'VIN-1' }
    ])));
    mileageServiceSpy.declareMileage.and.returnValue(of(monthlyMileage()));

    fixture = TestBed.createComponent(DriverMileageComponent);
    component = fixture.componentInstance;
    detectChangesSpy = spyOn(component['cdr'] as ChangeDetectorRef, 'detectChanges');
    fixture.detectChanges();

    component.form.patchValue({ vehicleId: 1, mileage: 1200 });
    component.submit();

    const expectedPayload: MileageRequest = {
      vehicleId: 1,
      mileage: 1200,
      year: component.year,
      month: component.month
    };

    expect(mileageServiceSpy.declareMileage).toHaveBeenCalledWith(expectedPayload);
    expect(component.successMessage).toBe('Kilométrage enregistré avec succès.');
    expect(component.errorMessage).toBeNull();
    expect(detectChangesSpy).toHaveBeenCalled();
  });

  it('should show API error reason when submit fails', () => {
    mileageServiceSpy.getDriverVehicles.and.returnValue(of(vehiclePage([
      { id: 1, vin: 'VIN-1' }
    ])));
    mileageServiceSpy.declareMileage.and.returnValue(
      throwError(() => ({ error: '{"reason":"Déjà déclaré"}' }))
    );

    fixture = TestBed.createComponent(DriverMileageComponent);
    component = fixture.componentInstance;
    detectChangesSpy = spyOn(component['cdr'] as ChangeDetectorRef, 'detectChanges');
    fixture.detectChanges();

    component.form.patchValue({ vehicleId: 1, mileage: 1200 });
    component.submit();

    expect(component.errorMessage).toBe('Déjà déclaré');
    expect(component.successMessage).toBeNull();
    expect(detectChangesSpy).toHaveBeenCalled();
  });
});

function vehiclePage(content: Vehicle[]): Page<Vehicle> {
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

function monthlyMileage(): MonthlyMileage {
  return {
    id: 10,
    vehicle: { id: 1, vin: 'VIN-1' },
    year: 2026,
    month: 3,
    mileage: 1200
  };
}
