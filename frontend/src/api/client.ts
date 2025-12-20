/**
 * API Client - Simple fetch wrapper with authentication
 */

import type {
  PatientRequest,
  PatientResponse,
  ClientRequest,
  ClientResponse,
  ApiError,
} from './types';
import keycloak from '../auth/keycloak';

const API_BASE = '/api/v1';

class ApiClient {
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    // Add auth token if available
    if (keycloak.authenticated && keycloak.token) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      if (response.status === 401) {
        // Token might be expired, try to refresh
        try {
          const refreshed = await keycloak.updateToken(30);
          if (refreshed && keycloak.token) {
            // Retry with new token
            headers['Authorization'] = `Bearer ${keycloak.token}`;
            const retryResponse = await fetch(`${API_BASE}${endpoint}`, {
              ...options,
              headers,
            });
            if (retryResponse.ok) {
              if (retryResponse.status === 204) return undefined as T;
              return retryResponse.json();
            }
          }
        } catch {
          // Refresh failed, redirect to login
          keycloak.login();
        }
      }
      const error: ApiError = await response.json();
      throw error;
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json();
  }

  // Patients API
  async getPatients(ownerId?: string): Promise<PatientResponse[]> {
    const query = ownerId ? `?ownerId=${ownerId}` : '';
    return this.request<PatientResponse[]>(`/patients${query}`);
  }

  async getPatient(id: string): Promise<PatientResponse> {
    return this.request<PatientResponse>(`/patients/${id}`);
  }

  async createPatient(patient: PatientRequest): Promise<PatientResponse> {
    return this.request<PatientResponse>('/patients', {
      method: 'POST',
      body: JSON.stringify(patient),
    });
  }

  async updatePatient(id: string, patient: PatientRequest): Promise<PatientResponse> {
    return this.request<PatientResponse>(`/patients/${id}`, {
      method: 'PUT',
      body: JSON.stringify(patient),
    });
  }

  async deletePatient(id: string): Promise<void> {
    return this.request<void>(`/patients/${id}`, {
      method: 'DELETE',
    });
  }

  // Clients API
  async getClients(): Promise<ClientResponse[]> {
    return this.request<ClientResponse[]>('/clients');
  }

  async getClient(id: string): Promise<ClientResponse> {
    return this.request<ClientResponse>(`/clients/${id}`);
  }

  async createClient(client: ClientRequest): Promise<ClientResponse> {
    return this.request<ClientResponse>('/clients', {
      method: 'POST',
      body: JSON.stringify(client),
    });
  }

  async updateClient(id: string, client: ClientRequest): Promise<ClientResponse> {
    return this.request<ClientResponse>(`/clients/${id}`, {
      method: 'PUT',
      body: JSON.stringify(client),
    });
  }

  async deleteClient(id: string): Promise<void> {
    return this.request<void>(`/clients/${id}`, {
      method: 'DELETE',
    });
  }
}

export const api = new ApiClient();
