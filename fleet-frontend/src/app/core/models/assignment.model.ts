import { User } from './user.model';
import { Vehicle } from './vehicle.model';

export interface Assignment {
  id: number;
  user: User;
  vehicle: Vehicle;
  startDate?: string | null;
  endDate?: string | null;
}

export interface AssignmentRequest {
  userId: number;
  vehicleId: number;
}
