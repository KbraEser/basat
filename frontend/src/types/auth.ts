// Mirrors the auth-service DTOs one to one, so a renamed field breaks the build instead of the page.

export type Role = 'ADMIN' | 'ENERGY_MANAGER' | 'VIEWER'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  userId: string
  tenantId: string
  role: Role
}

export interface RegisterRequest {
  companyName: string
  fullName: string
  email: string
  password: string
}

export interface RegisterResponse {
  tenantId: string
  userId: string
  email: string
  role: Role
}

// GET /api/users/me
export interface UserProfile {
  userId: string
  tenantId: string
  companyName: string
  email: string
  fullName: string
  role: Role
}