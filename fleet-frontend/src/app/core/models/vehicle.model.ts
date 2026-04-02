export interface Vehicle {
  id: number;
  vin: string;
  brand?: string | null;
  model?: string | null;
  licensePlate?: string | null;
  inServiceDate?: string | null;
  active?: boolean | null;
}
