import { api } from "@/lib/api";
import type { LoginRequest, LoginResponse, UserProfile } from "@/types/auth";

export const login = (values:LoginRequest) => api.post<LoginResponse>('/api/auth/login', values)

export const register = (values:LoginRequest) =>api.post<LoginResponse>('/api/auth/register', values)

export const logout = () => api.post<void>('/api/auth/logout')

export const fetchCurrentUser = () => api.get<UserProfile>('/api/users/me')