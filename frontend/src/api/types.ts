/**
 * API Types - These match the backend DTOs
 * After running `npm run api:generate`, these will be replaced by generated types.
 */

export type Species = 'DOG' | 'CAT' | 'BIRD' | 'RABBIT' | 'HAMSTER' | 'FISH' | 'REPTILE' | 'OTHER';

export interface PatientRequest {
  name: string;
  species: Species;
  breed?: string;
  dateOfBirth?: string;
  weight?: number;
  ownerId?: string;
  notes?: string;
}

export interface PatientResponse {
  id: string;
  name: string;
  species: Species;
  breed?: string;
  dateOfBirth?: string;
  weight?: number;
  ownerId?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ClientRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  notes?: string;
}

export interface ClientResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApiError {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  timestamp?: string;
  errors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}
