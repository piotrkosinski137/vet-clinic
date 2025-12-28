/**
 * API Types - These match the backend DTOs
 * After running `npm run api:generate`, these will be replaced by generated types.
 */

export type Species = 'DOG' | 'CAT' | 'BIRD' | 'RABBIT' | 'HAMSTER' | 'FISH' | 'REPTILE' | 'OTHER';

export type Gender = 'MALE' | 'FEMALE' | 'UNKNOWN';

export type PatientLabel =
  | 'AGGRESSIVE'
  | 'ALLERGIC'
  | 'VIP'
  | 'CHRONIC'
  | 'SENIOR'
  | 'SPECIAL_DIET'
  | 'UNDER_TREATMENT'
  | 'FLIGHT_RISK';

export interface PatientRequest {
  name: string;
  species: Species;
  breed?: string;
  dateOfBirth?: string;
  weight?: number;
  ownerId?: string;
  microchipNumber?: string;
  color?: string;
  gender?: Gender;
  neutered?: boolean;
  labels?: PatientLabel[];
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
  microchipNumber?: string;
  color?: string;
  gender?: Gender;
  neutered?: boolean;
  labels?: PatientLabel[];
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
// Visit/Appointment Types
export type VisitStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export type VisitType = 'CONSULTATION' | 'VACCINATION' | 'LAB_WORK' | 'ULTRASOUND' | 'CARDIOLOGY' | 'SURGERY' | 'DENTAL' | 'GROOMING' | 'EMERGENCY' | 'FOLLOW_UP' | 'CHECKUP';

export interface MedicationDto {
  medicationName: string;
  dosage?: string;
  frequency?: string;
  duration?: string;
  medicationNotes?: string;
}

export interface UsedMaterialDto {
  materialId: string;
  name: string;
  quantity: number;
  costPrice: number;      // Cost per unit (warehouse price)
  sellPrice: number;      // Sell per unit (client price with margin)
  unit: string;
}

export interface VisitRequest {
  patientId: string;
  clientId?: string;
  veterinarianId?: string;
  veterinarianName?: string;
  visitDate: string; // ISO datetime
  durationMinutes?: number;
  status?: VisitStatus;
  visitType?: VisitType;
  reason?: string;
  interview?: string;
  examination?: string;
  diagnosis?: string;
  treatment?: string;
  recommendations?: string;
  medications?: MedicationDto[];
  usedMaterials?: UsedMaterialDto[];
  notes?: string;
  weight?: number;
  temperature?: number;
  nextVisitDate?: string; // ISO date
}

export interface VisitResponse {
  id: string;
  patientId: string;
  clientId?: string;
  veterinarianId?: string;
  veterinarianName?: string;
  visitDate: string;
  durationMinutes?: number;
  status: VisitStatus;
  visitType?: VisitType;
  reason?: string;
  interview?: string;
  examination?: string;
  diagnosis?: string;
  treatment?: string;
  recommendations?: string;
  medications?: MedicationDto[];
  usedMaterials?: UsedMaterialDto[];
  notes?: string;
  weight?: number;
  temperature?: number;
  nextVisitDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface VisitFilters {
  patientId?: string;
  clientId?: string;
  veterinarianId?: string;
  status?: VisitStatus;
  dateFrom?: string;
  dateTo?: string;
}

// Client Debt Types
export interface ClientDebtResponse {
  clientId: string;
  totalOutstanding: number;
  totalInvoiced: number;
  totalPaid: number;
  unpaidInvoiceCount: number;
  overdueInvoiceCount: number;
}

// Price List / Materials Types
export type ItemCategory =
  | 'SERVICE'
  | 'MEDICATION'
  | 'PRODUCT'
  | 'PROCEDURE'
  | 'CONSULTATION'
  | 'LAB_TEST'
  | 'VACCINATION'
  | 'OTHER';

export interface PriceListItemRequest {
  name: string;
  description?: string;
  category: ItemCategory;
  costPrice: number;
  sellPrice: number;
  unit?: string;
  active?: boolean;
  code?: string;
}

export interface PriceListItemResponse {
  id: string;
  name: string;
  description?: string;
  category: ItemCategory;
  costPrice: number;
  sellPrice: number;
  unit?: string;
  active?: boolean;
  code?: string;
  stockQuantity?: number;
  reorderPoint?: number;
  barcode?: string;
  supplierCode?: string;
  expirationDate?: string;
  batchNumber?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PriceListFilters {
  active?: boolean;
  category?: ItemCategory;
  name?: string;
}

// Veterinarian Types
export interface VeterinarianRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  specialization?: string;
  licenseNumber?: string;
  colorCode?: string;
  active?: boolean;
  notes?: string;
}

export interface VeterinarianResponse {
  id: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone?: string;
  specialization?: string;
  licenseNumber?: string;
  colorCode?: string;
  active: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface VeterinarianFilters {
  active?: boolean;
  specialization?: string;
  name?: string;
}

// Veterinarian Schedule Types
export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
export type DayOffType = 'VACATION' | 'SICK_LEAVE' | 'PERSONAL' | 'OTHER';

export interface VeterinarianScheduleRequest {
  dayOfWeek: DayOfWeek;
  startTime: string | null; // "HH:mm" format or null
  endTime: string | null;
  workingDay: boolean;
}

export interface VeterinarianScheduleResponse {
  id: string;
  veterinarianId: string;
  dayOfWeek: DayOfWeek;
  startTime: string | null;
  endTime: string | null;
  workingDay: boolean;
}

export interface WeeklyScheduleRequest {
  schedules: VeterinarianScheduleRequest[];
}

export interface VeterinarianDayOffRequest {
  startDate: string; // ISO date format
  endDate: string;
  type: DayOffType;
  description?: string;
}

export interface VeterinarianDayOffResponse {
  id: string;
  veterinarianId: string;
  startDate: string;
  endDate: string;
  type: DayOffType;
  description?: string;
  approved: boolean;
  createdAt: string;
}

export interface VeterinarianAvailabilityResponse {
  veterinarianId: string;
  date: string;
  workingDay: boolean;
  startTime: string | null;
  endTime: string | null;
  isDayOff: boolean;
  dayOffType?: DayOffType;
  dayOffDescription?: string;
}

// Doctor Invitation Types
export interface DoctorInvitationRequest {
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  specialization?: string;
  licenseNumber?: string;
  notes?: string;
}

// Inventory Types
export type TransactionType = 'RECEIPT' | 'USAGE' | 'ADJUSTMENT' | 'EXPIRED' | 'RETURN';
export type SupplierInvoiceStatus = 'PENDING' | 'PROCESSED' | 'ERROR';

export interface InventoryTransaction {
  id: string;
  itemId: string;
  transactionType: TransactionType;
  quantity: number;
  quantityBefore: number;
  quantityAfter: number;
  referenceId?: string;
  referenceType?: string;
  batchNumber?: string;
  expirationDate?: string;
  unitCost?: number;
  notes?: string;
  createdBy?: string;
  createdAt: string;
}

export interface SupplierInvoice {
  id: string;
  invoiceNumber: string;
  supplierName?: string;
  supplierNip?: string;
  invoiceDate?: string;
  saleDate?: string;
  paymentDueDate?: string;
  paymentMethod?: string;
  totalNet?: number;
  totalGross?: number;
  totalVat?: number;
  fileName?: string;
  status: SupplierInvoiceStatus;
  processedAt?: string;
  items: SupplierInvoiceItem[];
  createdAt: string;
}

export interface SupplierInvoiceItem {
  id: string;
  itemId?: string;
  productCode?: string;
  productName: string;
  quantity: number;
  unit?: string;
  netPrice?: number;
  grossPrice?: number;
  discountPercent?: number;
  vatRate?: number;
  vatAmount?: number;
  batchNumber?: string;
  expirationDate?: string;
  barcode?: string;
  matched: boolean;
}

export interface StockAdjustmentRequest {
  newQuantity: number;
  reason: string;
}

// Audit Log Types
export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'VIEW' | 'EXPORT' | 'PRINT' | 'LOGIN' | 'LOGOUT';

export interface AuditLogResponse {
  id: string;
  entityType: string;
  entityId: string;
  action: AuditAction;
  userId: string;
  userName: string;
  timestamp: string;
  oldValue?: string;
  newValue?: string;
  changedFields?: string;
  ipAddress?: string;
  userAgent?: string;
  description?: string;
}

export interface AuditLogFilters {
  entityType?: string;
  entityId?: string;
  userId?: string;
  action?: AuditAction;
  dateFrom?: string;
  dateTo?: string;
}

// Dashboard Types
export interface DashboardStatsResponse {
  totalPatients: number;
  visitsToday: number;
  pendingInvoices: number;
  lowStockItems: number;
  totalClients: number;
  completedVisitsToday: number;
  scheduledVisitsToday: number;
}

// GDPR Consent Types
export type ConsentType = 'DATA_PROCESSING' | 'MARKETING' | 'THIRD_PARTY_SHARING' | 'MEDICAL_RECORDS' | 'PHOTO_VIDEO' | 'RESEARCH';
export type ConsentStatus = 'PENDING' | 'GRANTED' | 'REVOKED' | 'EXPIRED';

export interface GdprConsentRequest {
  clientId: string;
  consentType: ConsentType;
  consentText: string;
  consentVersion?: string;
  expiresAt?: string;
  ipAddress: string;
  notes?: string;
}

export interface GdprConsentResponse {
  id: string;
  clientId: string;
  consentType: ConsentType;
  status: ConsentStatus;
  consentText?: string;
  consentVersion?: string;
  requestedAt: string;
  grantedAt?: string;
  revokedAt?: string;
  expiresAt?: string;
  ipAddress?: string;
  signatureReference?: string;
  notes?: string;
  active: boolean;
}

// Vaccination Certificate Types
export type CertificateType = 'RABIES' | 'DISTEMPER' | 'PARVOVIRUS' | 'HEPATITIS' | 'LEPTOSPIROSIS' | 'BORDETELLA' | 'FELINE_LEUKEMIA' | 'FELINE_CALICIVIRUS' | 'OTHER';

export interface VaccinationCertificateRequest {
  certificateType: CertificateType;
  patientId: string;
  patientName: string;
  patientSpecies?: string;
  patientBreed?: string;
  microchipNumber?: string;
  clientId: string;
  clientName: string;
  visitId?: string;
  vaccineName: string;
  vaccineManufacturer?: string;
  batchNumber?: string;
  administrationDate?: string;
  expirationDate?: string;
  nextDueDate?: string;
  veterinarianId?: string;
  veterinarianName: string;
  veterinarianLicenseNumber?: string;
  notes?: string;
}

export interface VaccinationCertificateResponse {
  id: string;
  certificateNumber: string;
  patientId: string;
  clientId?: string;
  certificateType: CertificateType;
  vaccineName: string;
  manufacturer?: string;
  batchNumber?: string;
  administrationDate: string;
  expirationDate?: string;
  nextDueDate?: string;
  veterinarianId?: string;
  veterinarianName?: string;
  isValid: boolean;
  invalidatedAt?: string;
  invalidationReason?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

// Invoice/Payment Types
export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PAID' | 'CANCELLED' | 'OVERDUE';
export type PaymentMethod = 'CASH' | 'CARD' | 'TRANSFER' | 'OTHER';

export interface InvoiceItemDto {
  name: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  total: number;
}

export interface InvoiceRequest {
  clientId: string;
  patientId?: string;
  visitId?: string;
  items: InvoiceItemDto[];
  notes?: string;
  dueDate?: string;
}

export interface InvoiceResponse {
  id: string;
  invoiceNumber: string;
  clientId: string;
  patientId?: string;
  visitId?: string;
  status: InvoiceStatus;
  items: InvoiceItemDto[];
  subtotal: number;
  taxRate?: number;
  taxAmount?: number;
  totalAmount: number;
  paidAmount: number;
  notes?: string;
  issuedAt?: string;
  dueDate?: string;
  paidAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentRequest {
  amount: number;
  paymentMethod: PaymentMethod;
  notes?: string;
}

export interface PaymentResponse {
  id: string;
  invoiceId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  paymentDate: string;
  transactionReference?: string;
  notes?: string;
  createdAt: string;
}
