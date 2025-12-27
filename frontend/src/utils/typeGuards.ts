/**
 * Type guard utilities for runtime type checking.
 * Centralizes all type guards to avoid duplication across components (DRY principle).
 */
import type {
  Species,
  Gender,
  PatientLabel,
  VisitStatus,
  VisitType,
  ItemCategory,
  ConsentType,
  ConsentStatus,
  InvoiceStatus,
  PaymentMethod,
  AuditAction,
  CertificateType,
  TransactionType,
} from '../api/types';

// Species type guard
const VALID_SPECIES: readonly Species[] = [
  'DOG', 'CAT', 'BIRD', 'RABBIT', 'HAMSTER', 'FISH', 'REPTILE', 'OTHER'
] as const;

export const isSpecies = (value: string): value is Species =>
  VALID_SPECIES.includes(value as Species);

// Gender type guard
const VALID_GENDERS: readonly Gender[] = ['MALE', 'FEMALE', 'UNKNOWN'] as const;

export const isGender = (value: string): value is Gender =>
  VALID_GENDERS.includes(value as Gender);

// Patient Label type guard
const VALID_PATIENT_LABELS: readonly PatientLabel[] = [
  'AGGRESSIVE', 'ALLERGIC', 'VIP', 'CHRONIC', 'SENIOR',
  'SPECIAL_DIET', 'UNDER_TREATMENT', 'FLIGHT_RISK'
] as const;

export const isPatientLabel = (value: string): value is PatientLabel =>
  VALID_PATIENT_LABELS.includes(value as PatientLabel);

// Visit Status type guard
const VALID_VISIT_STATUSES: readonly VisitStatus[] = [
  'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'
] as const;

export const isVisitStatus = (value: string): value is VisitStatus =>
  VALID_VISIT_STATUSES.includes(value as VisitStatus);

// Visit Type type guard
const VALID_VISIT_TYPES: readonly VisitType[] = [
  'CONSULTATION', 'VACCINATION', 'LAB_WORK', 'ULTRASOUND', 'CARDIOLOGY',
  'SURGERY', 'DENTAL', 'GROOMING', 'EMERGENCY', 'FOLLOW_UP', 'CHECKUP'
] as const;

export const isVisitType = (value: string): value is VisitType =>
  VALID_VISIT_TYPES.includes(value as VisitType);

// Item Category type guard
const VALID_ITEM_CATEGORIES: readonly ItemCategory[] = [
  'SERVICE', 'MEDICATION', 'PRODUCT', 'PROCEDURE',
  'CONSULTATION', 'LAB_TEST', 'VACCINATION', 'OTHER'
] as const;

export const isItemCategory = (value: string): value is ItemCategory =>
  VALID_ITEM_CATEGORIES.includes(value as ItemCategory);

// Consent Type type guard
const VALID_CONSENT_TYPES: readonly ConsentType[] = [
  'DATA_PROCESSING', 'MARKETING', 'THIRD_PARTY_SHARING',
  'MEDICAL_RECORDS', 'PHOTO_VIDEO', 'RESEARCH'
] as const;

export const isConsentType = (value: string): value is ConsentType =>
  VALID_CONSENT_TYPES.includes(value as ConsentType);

// Consent Status type guard
const VALID_CONSENT_STATUSES: readonly ConsentStatus[] = [
  'PENDING', 'GRANTED', 'REVOKED', 'EXPIRED'
] as const;

export const isConsentStatus = (value: string): value is ConsentStatus =>
  VALID_CONSENT_STATUSES.includes(value as ConsentStatus);

// Invoice Status type guard
const VALID_INVOICE_STATUSES: readonly InvoiceStatus[] = [
  'DRAFT', 'ISSUED', 'PAID', 'CANCELLED', 'OVERDUE'
] as const;

export const isInvoiceStatus = (value: string): value is InvoiceStatus =>
  VALID_INVOICE_STATUSES.includes(value as InvoiceStatus);

// Payment Method type guard
const VALID_PAYMENT_METHODS: readonly PaymentMethod[] = [
  'CASH', 'CARD', 'TRANSFER', 'OTHER'
] as const;

export const isPaymentMethod = (value: string): value is PaymentMethod =>
  VALID_PAYMENT_METHODS.includes(value as PaymentMethod);

// Audit Action type guard
const VALID_AUDIT_ACTIONS: readonly AuditAction[] = [
  'CREATE', 'UPDATE', 'DELETE', 'VIEW', 'EXPORT', 'PRINT', 'LOGIN', 'LOGOUT'
] as const;

export const isAuditAction = (value: string): value is AuditAction =>
  VALID_AUDIT_ACTIONS.includes(value as AuditAction);

// Certificate Type type guard
const VALID_CERTIFICATE_TYPES: readonly CertificateType[] = [
  'RABIES', 'DISTEMPER', 'PARVOVIRUS', 'HEPATITIS', 'LEPTOSPIROSIS',
  'BORDETELLA', 'FELINE_LEUKEMIA', 'FELINE_CALICIVIRUS', 'OTHER'
] as const;

export const isCertificateType = (value: string): value is CertificateType =>
  VALID_CERTIFICATE_TYPES.includes(value as CertificateType);

// Transaction Type type guard
const VALID_TRANSACTION_TYPES: readonly TransactionType[] = [
  'RECEIPT', 'USAGE', 'ADJUSTMENT', 'EXPIRED', 'RETURN'
] as const;

export const isTransactionType = (value: string): value is TransactionType =>
  VALID_TRANSACTION_TYPES.includes(value as TransactionType);

// Generic nullable check
export const isNonNullable = <T>(value: T): value is NonNullable<T> =>
  value !== null && value !== undefined;

// String with content check
export const isNonEmptyString = (value: unknown): value is string =>
  typeof value === 'string' && value.trim().length > 0;

// UUID format check
const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export const isValidUUID = (value: string): boolean =>
  UUID_REGEX.test(value);
