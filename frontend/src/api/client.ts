/**
 * API Client - Fetch wrapper with authentication, timeout, and error handling
 */

import { dispatchAuthError } from '../hooks/useAuthErrorHandler';
import type {
  PatientRequest,
  PatientResponse,
  ClientRequest,
  ClientResponse,
  ClientDebtResponse,
  VisitRequest,
  VisitResponse,
  VisitFilters,
  VisitStatus,
  CheckInRequest,
  WaitingRoomVisitResponse,
  VisitDraftDto,
  ApiError,
  PriceListItemRequest,
  PriceListItemResponse,
  PriceListFilters,
  VeterinarianRequest,
  VeterinarianResponse,
  VeterinarianFilters,
  VeterinarianScheduleResponse,
  VeterinarianDayOffRequest,
  VeterinarianDayOffResponse,
  VeterinarianAvailabilityResponse,
  WeeklyScheduleRequest,
  DoctorInvitationRequest,
  InventoryTransaction,
  SupplierInvoice,
  StockAdjustmentRequest,
  InventoryBatchResponse,
  BatchStatus,
  CompleteBatchRequest,
  DisposeBatchRequest,
  AuditLogResponse,
  AuditLogFilters,
  AuditAction,
  DashboardStatsResponse,
  GdprConsentRequest,
  GdprConsentResponse,
  ConsentStatus,
  VaccinationCertificateRequest,
  VaccinationCertificateResponse,
  InvoiceRequest,
  InvoiceResponse,
  InvoiceStatus,
  PaymentRequest,
  PaymentResponse,
} from './types';

const API_BASE = '/api/v1';

/** Token storage keys */
const TOKEN_KEYS = {
  ACCESS: 'vetclinic_auth_token',
  REFRESH: 'vetclinic_refresh_token',
} as const;

/** Default request timeout in milliseconds */
const DEFAULT_TIMEOUT_MS = 30000;

/**
 * Custom error class for API errors with additional context.
 */
export class ApiRequestError extends Error {
  public readonly status?: number;
  public readonly detail?: string;
  public readonly apiError?: ApiError;

  constructor(message: string, status?: number, apiError?: ApiError) {
    super(message);
    this.name = 'ApiRequestError';
    this.status = status;
    this.detail = apiError?.detail;
    this.apiError = apiError;
  }
}

/**
 * Custom error class for timeout errors.
 */
export class TimeoutError extends Error {
  constructor(timeoutMs: number) {
    super(`Request timeout after ${timeoutMs}ms`);
    this.name = 'TimeoutError';
  }
}

/**
 * Custom error class for network errors.
 */
export class NetworkError extends Error {
  constructor(message: string = 'Network error. Please check your connection and try again.') {
    super(message);
    this.name = 'NetworkError';
  }
}

/**
 * Safely parses JSON response with proper error handling.
 */
async function safeParseJson<T>(response: Response): Promise<T | undefined> {
  try {
    const text = await response.text();
    if (!text) return undefined;
    return JSON.parse(text) as T;
  } catch (error) {
    const text = await response.text().catch(() => '');
    throw new ApiRequestError(
      `Invalid JSON response: ${text.substring(0, 100)}`,
      response.status
    );
  }
}

class ApiClient {
  /**
   * Makes an HTTP request with timeout and error handling.
   */
  private async request<T>(
    endpoint: string,
    options: RequestInit = {},
    customTimeout?: number
  ): Promise<T> {
    const controller = new AbortController();
    const timeoutMs = customTimeout ?? DEFAULT_TIMEOUT_MS;
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const url = `${API_BASE}${endpoint}`;
      const token = localStorage.getItem(TOKEN_KEYS.ACCESS);

      const response = await fetch(url, {
        ...options,
        signal: controller.signal,
        headers: {
          'Content-Type': 'application/json',
          ...(token && { Authorization: `Bearer ${token}` }),
          ...options.headers,
        },
      });

      if (!response.ok) {
        return await this.handleErrorResponse(response);
      }

      if (response.status === 204) {
        return undefined as T;
      }

      return await this.parseResponse<T>(response);
    } catch (error) {
      if (error instanceof Error && error.name === 'AbortError') {
        throw new TimeoutError(timeoutMs);
      }
      if (error instanceof ApiRequestError) {
        throw error;
      }
      throw new NetworkError();
    } finally {
      clearTimeout(timeoutId);
    }
  }

  /**
   * Handles HTTP error responses.
   */
  private async handleErrorResponse<T>(response: Response): Promise<T> {
    // Handle 401 - session expired
    if (response.status === 401) {
      // Clear stored tokens and dispatch event for global redirect handling
      localStorage.removeItem(TOKEN_KEYS.ACCESS);
      localStorage.removeItem(TOKEN_KEYS.REFRESH);
      dispatchAuthError();
      throw new ApiRequestError('Session expired. Please log in again.', 401);
    }

    // Parse error response
    const apiError = await safeParseJson<ApiError>(response);
    const errorMessage =
      apiError?.detail ||
      apiError?.title ||
      `Request failed with status ${response.status}`;
    throw new ApiRequestError(errorMessage, response.status, apiError);
  }

  /**
   * Safely parses response JSON.
   */
  private async parseResponse<T>(response: Response): Promise<T> {
    const text = await response.text();
    if (!text) {
      return {} as T;
    }
    try {
      return JSON.parse(text) as T;
    } catch {
      throw new ApiRequestError(
        `Invalid JSON response: ${text.substring(0, 100)}`,
        response.status
      );
    }
  }


  // Patients API
  async getPatients(ownerId?: string, q?: string): Promise<PatientResponse[]> {
    const params = new URLSearchParams();
    if (ownerId) params.append('ownerId', ownerId);
    if (q) params.append('q', q);
    const query = params.toString() ? `?${params.toString()}` : '';
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
  async getClients(q?: string): Promise<ClientResponse[]> {
    const query = q ? `?q=${encodeURIComponent(q)}` : '';
    return this.request<ClientResponse[]>(`/clients${query}`);
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

  async getClientDebt(clientId: string): Promise<ClientDebtResponse> {
    return this.request<ClientDebtResponse>(`/invoices/client/${clientId}/debt`);
  }

  // Visits API
  async getVisits(filters?: VisitFilters): Promise<VisitResponse[]> {
    const params = new URLSearchParams();
    if (filters) {
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined) params.append(key, value);
      });
    }
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request<VisitResponse[]>(`/visits${query}`);
  }

  async getVisit(id: string): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${id}`);
  }

  async createVisit(visit: VisitRequest): Promise<VisitResponse> {
    return this.request<VisitResponse>('/visits', {
      method: 'POST',
      body: JSON.stringify(visit),
    });
  }

  async updateVisit(id: string, visit: VisitRequest): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${id}`, {
      method: 'PUT',
      body: JSON.stringify(visit),
    });
  }

  async updateVisitStatus(id: string, status: VisitStatus): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }

  async reassignVisit(
    id: string,
    veterinarianId: string | null,
    veterinarianName: string | null,
    visitDate: string
  ): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${id}/reassign`, {
      method: 'PATCH',
      body: JSON.stringify({ veterinarianId, veterinarianName, visitDate }),
    });
  }

  async deleteVisit(id: string): Promise<void> {
    return this.request<void>(`/visits/${id}`, {
      method: 'DELETE',
    });
  }

  async getVisitsForDate(date: string): Promise<VisitResponse[]> {
    return this.request<VisitResponse[]>(`/visits/date/${date}`);
  }

  async getVisitsForVeterinarian(veterinarianId: string, date: string): Promise<VisitResponse[]> {
    return this.request<VisitResponse[]>(`/visits/veterinarian/${veterinarianId}/date/${date}`);
  }

  async getVisitsForVeterinarianRange(
    veterinarianId: string,
    dateFrom: string,
    dateTo: string
  ): Promise<VisitResponse[]> {
    return this.request<VisitResponse[]>(
      `/visits?veterinarianId=${veterinarianId}&dateFrom=${dateFrom}&dateTo=${dateTo}`
    );
  }

  async getPatientVisits(patientId: string): Promise<VisitResponse[]> {
    return this.request<VisitResponse[]>(`/visits/patient/${patientId}`);
  }

  // === Waiting Room API ===

  async getWaitingRoom(): Promise<VisitResponse[]> {
    return this.request<VisitResponse[]>('/visits/waiting-room');
  }

  /**
   * Get enriched waiting room data with patient, client and financial info.
   * This endpoint is optimized for the waiting room display.
   */
  async getEnrichedWaitingRoom(): Promise<WaitingRoomVisitResponse[]> {
    return this.request<WaitingRoomVisitResponse[]>('/waiting-room');
  }

  async checkIn(visitId: string, request?: CheckInRequest): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${visitId}/check-in`, {
      method: 'POST',
      body: request ? JSON.stringify(request) : undefined,
    });
  }

  async startFromWaitingRoom(visitId: string): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${visitId}/start-from-waiting-room`, {
      method: 'POST',
    });
  }

  async markNoShow(visitId: string): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${visitId}/no-show`, {
      method: 'POST',
    });
  }

  async updateWaitingRoomInfo(visitId: string, request: CheckInRequest): Promise<VisitResponse> {
    return this.request<VisitResponse>(`/visits/${visitId}/waiting-room-info`, {
      method: 'PATCH',
      body: JSON.stringify(request),
    });
  }

  // === Visit Draft API ===

  /**
   * Save or update a draft for a visit.
   * Called automatically by frontend every few seconds while editing.
   */
  async saveVisitDraft(visitId: string, draft: VisitDraftDto): Promise<VisitDraftDto> {
    return this.request<VisitDraftDto>(`/visits/${visitId}/draft`, {
      method: 'PUT',
      body: JSON.stringify(draft),
    });
  }

  /**
   * Get existing draft for a visit.
   * Called when opening VisitDetailsModal to restore unsaved work.
   */
  async getVisitDraft(visitId: string): Promise<VisitDraftDto | null> {
    try {
      return await this.request<VisitDraftDto>(`/visits/${visitId}/draft`);
    } catch (error) {
      // 404 means no draft exists - this is normal
      if (error instanceof ApiRequestError && error.status === 404) {
        return null;
      }
      throw error;
    }
  }

  /**
   * Delete draft for a visit.
   * Called after successful save (also done automatically by updateVisit).
   */
  async deleteVisitDraft(visitId: string): Promise<void> {
    try {
      return await this.request<void>(`/visits/${visitId}/draft`, {
        method: 'DELETE',
      });
    } catch (error) {
      // 404 is okay - draft might not exist
      if (error instanceof ApiRequestError && error.status === 404) {
        return;
      }
      throw error;
    }
  }

  // Price List API
  async getPriceListItems(filters?: PriceListFilters): Promise<PriceListItemResponse[]> {
    const params = new URLSearchParams();
    if (filters) {
      if (filters.active !== undefined) params.append('active', String(filters.active));
      if (filters.category) params.append('category', filters.category);
      if (filters.name) params.append('name', filters.name);
    }
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request<PriceListItemResponse[]>(`/price-list${query}`);
  }

  async getPriceListItem(id: string): Promise<PriceListItemResponse> {
    return this.request<PriceListItemResponse>(`/price-list/${id}`);
  }

  async createPriceListItem(item: PriceListItemRequest): Promise<PriceListItemResponse> {
    return this.request<PriceListItemResponse>('/price-list', {
      method: 'POST',
      body: JSON.stringify(item),
    });
  }

  async updatePriceListItem(id: string, item: PriceListItemRequest): Promise<PriceListItemResponse> {
    return this.request<PriceListItemResponse>(`/price-list/${id}`, {
      method: 'PUT',
      body: JSON.stringify(item),
    });
  }

  async togglePriceListItemActive(id: string, active: boolean): Promise<PriceListItemResponse> {
    return this.request<PriceListItemResponse>(`/price-list/${id}/active?active=${active}`, {
      method: 'PATCH',
    });
  }

  async deletePriceListItem(id: string): Promise<void> {
    return this.request<void>(`/price-list/${id}`, {
      method: 'DELETE',
    });
  }

  // Veterinarians API
  async getVeterinarians(filters?: VeterinarianFilters): Promise<VeterinarianResponse[]> {
    const params = new URLSearchParams();
    if (filters) {
      if (filters.active !== undefined) params.append('active', String(filters.active));
      if (filters.specialization) params.append('specialization', filters.specialization);
      if (filters.name) params.append('name', filters.name);
    }
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request<VeterinarianResponse[]>(`/veterinarians${query}`);
  }

  async getVeterinarian(id: string): Promise<VeterinarianResponse> {
    return this.request<VeterinarianResponse>(`/veterinarians/${id}`);
  }

  async getVeterinarianByEmail(email: string): Promise<VeterinarianResponse> {
    return this.request<VeterinarianResponse>(`/veterinarians/email/${encodeURIComponent(email)}`);
  }

  async createVeterinarian(veterinarian: VeterinarianRequest): Promise<VeterinarianResponse> {
    return this.request<VeterinarianResponse>('/veterinarians', {
      method: 'POST',
      body: JSON.stringify(veterinarian),
    });
  }

  async updateVeterinarian(id: string, veterinarian: VeterinarianRequest): Promise<VeterinarianResponse> {
    return this.request<VeterinarianResponse>(`/veterinarians/${id}`, {
      method: 'PUT',
      body: JSON.stringify(veterinarian),
    });
  }

  async toggleVeterinarianActive(id: string, active: boolean): Promise<VeterinarianResponse> {
    return this.request<VeterinarianResponse>(`/veterinarians/${id}/active?active=${active}`, {
      method: 'PATCH',
    });
  }

  async deleteVeterinarian(id: string): Promise<void> {
    return this.request<void>(`/veterinarians/${id}`, {
      method: 'DELETE',
    });
  }

  // Veterinarian Schedule API
  async getVeterinarianSchedule(id: string): Promise<VeterinarianScheduleResponse[]> {
    return this.request<VeterinarianScheduleResponse[]>(`/veterinarians/${id}/schedule`);
  }

  async updateVeterinarianSchedule(id: string, schedule: WeeklyScheduleRequest): Promise<VeterinarianScheduleResponse[]> {
    return this.request<VeterinarianScheduleResponse[]>(`/veterinarians/${id}/schedule`, {
      method: 'PUT',
      body: JSON.stringify(schedule),
    });
  }

  // Veterinarian Days Off API
  async getVeterinarianDaysOff(id: string, startDate?: string, endDate?: string): Promise<VeterinarianDayOffResponse[]> {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request<VeterinarianDayOffResponse[]>(`/veterinarians/${id}/days-off${query}`);
  }

  async addVeterinarianDayOff(id: string, dayOff: VeterinarianDayOffRequest): Promise<VeterinarianDayOffResponse> {
    return this.request<VeterinarianDayOffResponse>(`/veterinarians/${id}/days-off`, {
      method: 'POST',
      body: JSON.stringify(dayOff),
    });
  }

  async updateVeterinarianDayOff(veterinarianId: string, dayOffId: string, dayOff: VeterinarianDayOffRequest): Promise<VeterinarianDayOffResponse> {
    return this.request<VeterinarianDayOffResponse>(`/veterinarians/${veterinarianId}/days-off/${dayOffId}`, {
      method: 'PUT',
      body: JSON.stringify(dayOff),
    });
  }

  async deleteVeterinarianDayOff(veterinarianId: string, dayOffId: string): Promise<void> {
    return this.request<void>(`/veterinarians/${veterinarianId}/days-off/${dayOffId}`, {
      method: 'DELETE',
    });
  }

  async approveVeterinarianDayOff(veterinarianId: string, dayOffId: string, approved: boolean): Promise<VeterinarianDayOffResponse> {
    return this.request<VeterinarianDayOffResponse>(`/veterinarians/${veterinarianId}/days-off/${dayOffId}/approve?approved=${approved}`, {
      method: 'PATCH',
    });
  }

  // Veterinarian Availability API
  async getVeterinarianAvailability(id: string, date: string): Promise<VeterinarianAvailabilityResponse> {
    return this.request<VeterinarianAvailabilityResponse>(`/veterinarians/${id}/availability?date=${date}`);
  }

  async getAllVeterinariansAvailability(date: string): Promise<VeterinarianAvailabilityResponse[]> {
    return this.request<VeterinarianAvailabilityResponse[]>(`/veterinarians/availability?date=${date}`);
  }

  // Doctor Invitation API
  async inviteDoctor(invitation: DoctorInvitationRequest): Promise<VeterinarianResponse> {
    return this.request<VeterinarianResponse>('/doctors/invite', {
      method: 'POST',
      body: JSON.stringify(invitation),
    });
  }

  // === Inventory API ===

  async getInventoryItems(lowStockOnly: boolean = false): Promise<PriceListItemResponse[]> {
    const params = lowStockOnly ? '?lowStockOnly=true' : '';
    return this.request<PriceListItemResponse[]>(`/inventory/stock${params}`);
  }

  async adjustStock(itemId: string, request: StockAdjustmentRequest): Promise<PriceListItemResponse> {
    return this.request<PriceListItemResponse>(`/inventory/stock/${itemId}`, {
      method: 'PATCH',
      body: JSON.stringify(request),
    });
  }

  async getLowStockItems(): Promise<PriceListItemResponse[]> {
    return this.request<PriceListItemResponse[]>('/inventory/stock?lowStockOnly=true');
  }

  async getInventoryTransactions(itemId?: string): Promise<InventoryTransaction[]> {
    const params = itemId ? `?itemId=${itemId}` : '';
    return this.request<InventoryTransaction[]>(`/inventory/transactions${params}`);
  }

  async getSupplierInvoices(): Promise<SupplierInvoice[]> {
    return this.request<SupplierInvoice[]>('/inventory/invoices');
  }

  async getSupplierInvoice(id: string): Promise<SupplierInvoice> {
    return this.request<SupplierInvoice>(`/inventory/invoices/${id}`);
  }

  async uploadSupplierInvoice(file: File): Promise<SupplierInvoice> {
    const formData = new FormData();
    formData.append('file', file);

    const url = `${API_BASE}/inventory/invoices/upload`;
    const token = localStorage.getItem(TOKEN_KEYS.ACCESS);

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`Upload failed: ${response.statusText}`);
    }

    return response.json();
  }

  async processSupplierInvoice(invoiceId: string): Promise<SupplierInvoice> {
    return this.request<SupplierInvoice>(`/inventory/invoices/${invoiceId}/process`, {
      method: 'POST',
    });
  }

  // === Inventory Batches API (FIFO) ===

  async getInventoryBatches(filters?: { status?: BatchStatus; itemId?: string }): Promise<InventoryBatchResponse[]> {
    const params = new URLSearchParams();
    if (filters) {
      if (filters.status) params.append('status', filters.status);
      if (filters.itemId) params.append('itemId', filters.itemId);
    }
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request<InventoryBatchResponse[]>(`/inventory/batches${query}`);
  }

  async getInventoryBatch(id: string): Promise<InventoryBatchResponse> {
    return this.request<InventoryBatchResponse>(`/inventory/batches/${id}`);
  }

  async getExpiringBatches(days: number = 30): Promise<InventoryBatchResponse[]> {
    return this.request<InventoryBatchResponse[]>(`/inventory/batches/expiring?days=${days}`);
  }

  async getPendingBatchCount(): Promise<number> {
    return this.request<number>('/inventory/batches/pending/count');
  }

  async completeBatch(batchId: string, request: CompleteBatchRequest): Promise<InventoryBatchResponse> {
    return this.request<InventoryBatchResponse>(`/inventory/batches/${batchId}`, {
      method: 'PATCH',
      body: JSON.stringify(request),
    });
  }

  async disposeBatch(batchId: string, request: DisposeBatchRequest): Promise<InventoryBatchResponse> {
    return this.request<InventoryBatchResponse>(`/inventory/batches/${batchId}/dispose`, {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  // === Audit Logs API ===

  async getAuditLogs(filters?: AuditLogFilters): Promise<AuditLogResponse[]> {
    const params = new URLSearchParams();
    if (filters) {
      if (filters.entityType) params.append('entityType', filters.entityType);
      if (filters.entityId) params.append('entityId', filters.entityId);
      if (filters.userId) params.append('userId', filters.userId);
      if (filters.action) params.append('action', filters.action);
      if (filters.dateFrom) params.append('dateFrom', filters.dateFrom);
      if (filters.dateTo) params.append('dateTo', filters.dateTo);
    }
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request<AuditLogResponse[]>(`/audit-logs${query}`);
  }

  async getAuditLogsByEntity(entityType: string, entityId: string): Promise<AuditLogResponse[]> {
    return this.request<AuditLogResponse[]>(`/audit-logs/entity/${entityType}/${entityId}`);
  }

  async getAuditLogsByUser(userId: string): Promise<AuditLogResponse[]> {
    return this.request<AuditLogResponse[]>(`/audit-logs/user/${userId}`);
  }

  async getAuditLogsByAction(action: AuditAction): Promise<AuditLogResponse[]> {
    return this.request<AuditLogResponse[]>(`/audit-logs/action/${action}`);
  }

  async getAuditLogsByDateRange(dateFrom: string, dateTo: string): Promise<AuditLogResponse[]> {
    return this.request<AuditLogResponse[]>(`/audit-logs?dateFrom=${dateFrom}&dateTo=${dateTo}`);
  }

  // === GDPR Consents API ===

  async getConsents(): Promise<GdprConsentResponse[]> {
    return this.request<GdprConsentResponse[]>('/consents');
  }

  async getConsent(id: string): Promise<GdprConsentResponse> {
    return this.request<GdprConsentResponse>(`/consents/${id}`);
  }

  async getConsentsByClient(clientId: string): Promise<GdprConsentResponse[]> {
    return this.request<GdprConsentResponse[]>(`/consents/client/${clientId}`);
  }

  async getConsentsByStatus(status: ConsentStatus): Promise<GdprConsentResponse[]> {
    return this.request<GdprConsentResponse[]>(`/consents/status/${status}`);
  }

  async createConsent(consent: GdprConsentRequest): Promise<GdprConsentResponse> {
    return this.request<GdprConsentResponse>('/consents', {
      method: 'POST',
      body: JSON.stringify(consent),
    });
  }

  async grantConsent(id: string, signatureReference?: string): Promise<GdprConsentResponse> {
    const params = signatureReference ? `?signatureReference=${encodeURIComponent(signatureReference)}` : '';
    return this.request<GdprConsentResponse>(`/consents/${id}/grant${params}`, {
      method: 'PUT',
    });
  }

  async revokeConsent(id: string, reason?: string): Promise<GdprConsentResponse> {
    const params = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    return this.request<GdprConsentResponse>(`/consents/${id}/revoke${params}`, {
      method: 'PUT',
    });
  }

  async deleteConsent(id: string): Promise<void> {
    return this.request<void>(`/consents/${id}`, {
      method: 'DELETE',
    });
  }

  // === Vaccination Certificates API ===

  async getCertificates(): Promise<VaccinationCertificateResponse[]> {
    return this.request<VaccinationCertificateResponse[]>('/certificates');
  }

  async getCertificate(id: string): Promise<VaccinationCertificateResponse> {
    return this.request<VaccinationCertificateResponse>(`/certificates/${id}`);
  }

  async getCertificatesByPatient(patientId: string): Promise<VaccinationCertificateResponse[]> {
    return this.request<VaccinationCertificateResponse[]>(`/certificates/patient/${patientId}`);
  }

  async getCertificatesByClient(clientId: string): Promise<VaccinationCertificateResponse[]> {
    return this.request<VaccinationCertificateResponse[]>(`/certificates/client/${clientId}`);
  }

  async getValidCertificatesByPatient(patientId: string): Promise<VaccinationCertificateResponse[]> {
    return this.request<VaccinationCertificateResponse[]>(`/certificates/patient/${patientId}/valid`);
  }

  async createCertificate(certificate: VaccinationCertificateRequest): Promise<VaccinationCertificateResponse> {
    return this.request<VaccinationCertificateResponse>('/certificates', {
      method: 'POST',
      body: JSON.stringify(certificate),
    });
  }

  async updateCertificate(id: string, certificate: VaccinationCertificateRequest): Promise<VaccinationCertificateResponse> {
    return this.request<VaccinationCertificateResponse>(`/certificates/${id}`, {
      method: 'PUT',
      body: JSON.stringify(certificate),
    });
  }

  async invalidateCertificate(id: string, reason: string): Promise<VaccinationCertificateResponse> {
    return this.request<VaccinationCertificateResponse>(`/certificates/${id}/invalidate?reason=${encodeURIComponent(reason)}`, {
      method: 'PUT',
    });
  }

  async deleteCertificate(id: string): Promise<void> {
    return this.request<void>(`/certificates/${id}`, {
      method: 'DELETE',
    });
  }

  // === Invoices & Payments API ===

  async getInvoices(filters?: { clientId?: string; status?: InvoiceStatus; dateFrom?: string; dateTo?: string }): Promise<InvoiceResponse[]> {
    const params = new URLSearchParams();
    if (filters) {
      if (filters.clientId) params.append('clientId', filters.clientId);
      if (filters.status) params.append('status', filters.status);
      if (filters.dateFrom) params.append('dateFrom', filters.dateFrom);
      if (filters.dateTo) params.append('dateTo', filters.dateTo);
    }
    const query = params.toString() ? `?${params.toString()}` : '';
    return this.request<InvoiceResponse[]>(`/invoices${query}`);
  }

  async getInvoice(id: string): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>(`/invoices/${id}`);
  }

  async getInvoicesByClient(clientId: string): Promise<InvoiceResponse[]> {
    return this.request<InvoiceResponse[]>(`/invoices/client/${clientId}`);
  }

  async createInvoice(invoice: InvoiceRequest): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>('/invoices', {
      method: 'POST',
      body: JSON.stringify(invoice),
    });
  }

  async issueInvoice(id: string): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>(`/invoices/${id}/issue`, {
      method: 'POST',
    });
  }

  async cancelInvoice(id: string): Promise<InvoiceResponse> {
    return this.request<InvoiceResponse>(`/invoices/${id}/cancel`, {
      method: 'POST',
    });
  }

  async deleteInvoice(id: string): Promise<void> {
    return this.request<void>(`/invoices/${id}`, {
      method: 'DELETE',
    });
  }

  async recordPayment(invoiceId: string, payment: PaymentRequest): Promise<PaymentResponse> {
    return this.request<PaymentResponse>(`/invoices/${invoiceId}/payments`, {
      method: 'POST',
      body: JSON.stringify(payment),
    });
  }

  async getPayments(invoiceId: string): Promise<PaymentResponse[]> {
    return this.request<PaymentResponse[]>(`/invoices/${invoiceId}/payments`);
  }

  async deletePayment(paymentId: string): Promise<void> {
    return this.request<void>(`/invoices/payments/${paymentId}`, {
      method: 'DELETE',
    });
  }

  // === Dashboard API ===

  async getDashboardStats(): Promise<DashboardStatsResponse> {
    return this.request<DashboardStatsResponse>('/dashboard/stats');
  }

  // === PDF Generation API ===

  /**
   * Download visit summary as PDF.
   * @param visitId - The visit ID
   * @param lang - Language code ('pl' or 'en')
   */
  async downloadVisitPdf(visitId: string, lang: 'pl' | 'en' = 'pl'): Promise<void> {
    const url = `${API_BASE}/visits/${visitId}/pdf?lang=${lang}`;
    const token = localStorage.getItem(TOKEN_KEYS.ACCESS);

    const response = await fetch(url, {
      headers: {
        ...(token && { Authorization: `Bearer ${token}` }),
      },
    });

    if (!response.ok) {
      throw new ApiRequestError(`Failed to download PDF: ${response.statusText}`, response.status);
    }

    // Extract filename from Content-Disposition header if available
    const contentDisposition = response.headers.get('Content-Disposition');
    let filename = `visit-summary-${visitId}.pdf`;
    if (contentDisposition) {
      const match = contentDisposition.match(/filename="?([^";\n]+)"?/);
      if (match) {
        filename = match[1];
      }
    }

    // Create blob and trigger download
    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
  }
}

export const api = new ApiClient();
