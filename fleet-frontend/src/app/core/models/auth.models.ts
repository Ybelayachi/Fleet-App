export interface AuthResponse {
  token: string;
}

export interface JwtPayload {
  sub: string;
  exp?: number;
  iat?: number;
}
