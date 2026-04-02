export interface User {
  id: number;
  email: string;
  firstName?: string | null;
  lastName?: string | null;
  role?: string | null;
  active?: boolean | null;
}

export interface CreateUserPayload {
  email: string;
  firstName?: string | null;
  lastName?: string | null;
  role?: string | null;
  active?: boolean | null;
  password: string;
}
