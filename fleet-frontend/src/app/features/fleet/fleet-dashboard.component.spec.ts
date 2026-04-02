import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MileageService } from '../../core/services/mileage.service';
import { VehicleService } from '../../core/services/vehicle.service';
import { MonthlyMileage } from '../../core/models/monthly-mileage.model';
import { Vehicle } from '../../core/models/vehicle.model';
import { Page } from '../../core/models/page.model';
import { FleetDashboardComponent } from './fleet-dashboard.component';

describe('FleetDashboardComponent', () => {
  let fixture: ComponentFixture<FleetDashboardComponent>;
  let component: FleetDashboardComponent;
  let mileageServiceSpy: jasmine.SpyObj<MileageService>;
  let vehicleServiceSpy: jasmine.SpyObj<VehicleService>;

  beforeEach(async () => {
    mileageServiceSpy = jasmine.createSpyObj<MileageService>('MileageService', [
      'getFleetMileage',
      'getFleetMissing',
      'exportCsv'
    ]);
    vehicleServiceSpy = jasmine.createSpyObj<VehicleService>('VehicleService', ['getFleetVehicles']);

    await TestBed.configureTestingModule({
      imports: [FleetDashboardComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: MileageService, useValue: mileageServiceSpy },
        { provide: VehicleService, useValue: vehicleServiceSpy }
      ]
    }).compileComponents();
  });

  it('should create and load dashboard rows', () => {
    vehicleServiceSpy.getFleetVehicles.and.returnValue(of(vehiclePage([
      { id: 1, vin: 'VIN-1', licensePlate: 'AA-111-BB', brand: 'Tesla', model: 'Model 3' },
      { id: 2, vin: 'VIN-2', licensePlate: 'CC-222-DD', brand: 'Renault', model: 'Zoe' }
    ])));
    mileageServiceSpy.getFleetMileage.and.returnValue(of(mileagePage([
      {
        id: 101,
        vehicle: { id: 1, vin: 'VIN-1', licensePlate: 'AA-111-BB' },
        year: 2026,
        month: 3,
        mileage: 850,
        declaredBy: { id: 7, email: 'fleet@corp.test', firstName: 'Fleet', lastName: 'Manager' }
      }
    ])));
    mileageServiceSpy.getFleetMissing.and.returnValue(of(vehiclePage([
      { id: 2, vin: 'VIN-2' }
    ])));

    fixture = TestBed.createComponent(FleetDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(vehicleServiceSpy.getFleetVehicles).toHaveBeenCalled();
    expect(mileageServiceSpy.getFleetMileage).toHaveBeenCalled();
    expect(mileageServiceSpy.getFleetMissing).toHaveBeenCalled();
    expect(component.rows.length).toBe(2);
    expect(component.rows.find((r) => r.vehicle.id === 1)?.status).toBe('OK');
    expect(component.rows.find((r) => r.vehicle.id === 2)?.status).toBe('Missing');
  });

  it('should not load when year or month is invalid', () => {
    vehicleServiceSpy.getFleetVehicles.and.returnValue(of(vehiclePage([])));
    mileageServiceSpy.getFleetMileage.and.returnValue(of(mileagePage([])));
    mileageServiceSpy.getFleetMissing.and.returnValue(of(vehiclePage([])));

    fixture = TestBed.createComponent(FleetDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.form.patchValue({ year: null, month: null });
    component.load();

    expect(vehicleServiceSpy.getFleetVehicles).toHaveBeenCalledTimes(1);
    expect(mileageServiceSpy.getFleetMileage).toHaveBeenCalledTimes(1);
    expect(mileageServiceSpy.getFleetMissing).toHaveBeenCalledTimes(1);
  });

  it('should export CSV with selected period', () => {
    vehicleServiceSpy.getFleetVehicles.and.returnValue(of(vehiclePage([])));
    mileageServiceSpy.getFleetMileage.and.returnValue(of(mileagePage([])));
    mileageServiceSpy.getFleetMissing.and.returnValue(of(vehiclePage([])));
    mileageServiceSpy.exportCsv.and.returnValue(of('h1,h2\na,b'));

    fixture = TestBed.createComponent(FleetDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const createObjectURLSpy = spyOn(URL, 'createObjectURL').and.returnValue('blob:test-url');
    const revokeObjectURLSpy = spyOn(URL, 'revokeObjectURL');
    const clickSpy = jasmine.createSpy('click');
    spyOn(document, 'createElement').and.returnValue({
      href: '',
      download: '',
      click: clickSpy
    } as unknown as HTMLAnchorElement);

    component.form.patchValue({ year: 2026, month: 3 });
    component.exportCsv();

    expect(mileageServiceSpy.exportCsv).toHaveBeenCalledWith(2026, 3);
    expect(createObjectURLSpy).toHaveBeenCalled();
    expect(clickSpy).toHaveBeenCalled();
    expect(revokeObjectURLSpy).toHaveBeenCalledWith('blob:test-url');
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

function mileagePage(content: MonthlyMileage[]): Page<MonthlyMileage> {
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
