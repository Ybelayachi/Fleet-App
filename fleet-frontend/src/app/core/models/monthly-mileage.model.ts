import { User } from './user.model';
import { Vehicle } from './vehicle.model';

export interface MonthlyMileage {
  id: number;
  vehicle: Vehicle;
  year: number;
  month: number;
  mileage: number;
  declaredAt?: string | null;
  declaredBy?: User | null;
  lastModifiedAt?: string | null;
  lastModifiedBy?: User | null;
}

export interface MileageRequest {
  vehicleId: number;
  year: number;
  month: number;
  mileage: number;
}
