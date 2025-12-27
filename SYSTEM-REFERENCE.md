# VetClinic System Reference

**Comprehensive documentation for the VetClinic management system**

---

## Table of Contents

- [System Overview](#system-overview)
- [Roles & Permissions](#roles--permissions)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Multi-tenancy](#multi-tenancy)
- [Authentication](#authentication)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)

---

## System Overview

VetClinic is a modular monolithic veterinary clinic management system built with **hexagonal architecture** (Ports and Adapters pattern). The system provides comprehensive management of:

- **Patients** (animals) and their medical records
- **Clients** (pet owners) and contact information
- **Visits** (appointments and consultations)
- **Billing** (invoices, payments, price lists)
- **Inventory** (stock management and supplier invoices)
- **Veterinarians** (staff management)
- **Compliance** (GDPR consents, audit logs, vaccination certificates, document versioning)

The application supports **multi-tenancy** where each clinic has isolated data, enforced through JWT tokens and database filtering.

---

## Roles & Permissions

The system uses **role-based access control (RBAC)** with roles defined in Keycloak and enforced via Spring Security.

### Role Hierarchy

| Role | Description | Permissions |
|------|-------------|-------------|
| **ADMIN** | Administrator with full system access | Full access to all features including clinic settings, users, audit logs, and all data |
| **VET** (Veterinarian) | Medical staff | Can manage patients, visits, medical records, and delete clients |
| **RECEPTIONIST** | Front desk staff | Can manage clients, appointments, and basic patient information |
| **ACCOUNTANT** | Financial staff | Can view/manage invoices, payments, financial reports, inventory, and price lists |
| **USER** | Basic user | Read-only access to most resources |

### Permission Matrix

| Feature | ADMIN | VET | RECEPTIONIST | ACCOUNTANT | USER |
|---------|-------|-----|--------------|------------|------|
| **Patients** | Create/Update/Delete | Create/Update/Delete | View | View | View |
| **Clients** | Create/Update/Delete | Create/Update/Delete | Create/Update/Delete | View | View |
| **Visits** | Create/Update/Delete | Create/Update/Delete | View | View | View |
| **Invoices** | Create/Update/Delete | View | View | Create/Update/Delete | View |
| **Payments** | Manage | View | View | Manage | View |
| **Inventory** | Manage | View | View | Manage | View |
| **Price List** | Manage | View | View | Manage | View |
| **Veterinarians** | Manage | View | View | View | View |
| **Audit Logs** | View | - | - | - | - |
| **GDPR Consents** | Manage | Manage | Manage | View | View |
| **Vaccination Certificates** | Create/Update/Delete | Create/Update/Delete | View | View | View |
| **Document Versions** | Manage | View | View | View | View |

### Role Constants (from `Roles.java`)

```java
public static final String ADMIN = "admin";
public static final String VET = "vet";
public static final String RECEPTIONIST = "receptionist";
public static final String ACCOUNTANT = "accountant";
public static final String USER = "user";
```

---

## API Endpoints

All endpoints require authentication via JWT Bearer token and are automatically scoped to the user's clinic via `clinic_id` in the token.

### Base URL
```
http://localhost:8080/api/v1
```

---

### Dashboard API
**Base Path:** `/api/v1/dashboard`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/stats` | Get dashboard statistics (patients, clients, visits today, pending invoices, low stock) | Any authenticated user |

---

### Patients API
**Base Path:** `/api/v1/patients`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create a new patient | VET or ADMIN |
| GET | `/` | Get all patients (with optional filters: ownerId, name, species, breed, microchipNumber, labels) | Any authenticated user |
| GET | `/{id}` | Get patient by ID | Any authenticated user |
| GET | `/microchip/{microchipNumber}` | Get patient by microchip number | Any authenticated user |
| PUT | `/{id}` | Update patient | VET or ADMIN |
| DELETE | `/{id}` | Delete patient | VET or ADMIN |
| POST | `/{id}/labels/{label}` | Add label to patient | VET or ADMIN |
| DELETE | `/{id}/labels/{label}` | Remove label from patient | VET or ADMIN |

**Query Parameters:**
- `ownerId` (UUID): Filter by owner ID
- `name` (String): Search by name (partial, case-insensitive)
- `species` (Enum): Filter by species
- `breed` (String): Search by breed (partial, case-insensitive)
- `microchipNumber` (String): Search by microchip number (exact match)
- `labels` (Set<PatientLabel>): Filter by labels

---

### Clients API
**Base Path:** `/api/v1/clients`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create a new client | RECEPTIONIST, VET, or ADMIN |
| GET | `/` | Get all clients (with optional filters: firstName, lastName, email, phone, city) | Any authenticated user |
| GET | `/{id}` | Get client by ID | Any authenticated user |
| GET | `/email/{email}` | Get client by email | Any authenticated user |
| PUT | `/{id}` | Update client | RECEPTIONIST, VET, or ADMIN |
| DELETE | `/{id}` | Delete client | RECEPTIONIST, VET, or ADMIN |

**Query Parameters:**
- `firstName` (String): Search by first name (partial, case-insensitive)
- `lastName` (String): Search by last name (partial, case-insensitive)
- `email` (String): Search by email (partial, case-insensitive)
- `phone` (String): Search by phone (partial match)
- `city` (String): Search by city (partial, case-insensitive)

---

### Visits API
**Base Path:** `/api/v1/visits`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create a new visit | VET or ADMIN |
| GET | `/` | Get all visits (with filters: patientId, clientId, veterinarianId, status, dateFrom, dateTo) | Any authenticated user |
| GET | `/{id}` | Get visit by ID | Any authenticated user |
| GET | `/patient/{patientId}` | Get visit history for patient | Any authenticated user |
| GET | `/date/{date}` | Get visits for specific date | Any authenticated user |
| GET | `/veterinarian/{veterinarianId}` | Get all visits for veterinarian | Any authenticated user |
| GET | `/veterinarian/{veterinarianId}/date/{date}` | Get veterinarian's visits on specific date (calendar day view) | Any authenticated user |
| PUT | `/{id}` | Update visit | VET or ADMIN |
| PUT | `/{id}/status` | Update visit status | VET or ADMIN |
| PATCH | `/{id}/reassign` | Reassign visit to different veterinarian/time (drag-and-drop) | VET or ADMIN |
| DELETE | `/{id}` | Delete visit | VET or ADMIN |
| GET | `/{id}/summary` | Get printable visit summary | Any authenticated user |

**Query Parameters:**
- `patientId` (UUID): Filter by patient
- `clientId` (UUID): Filter by client
- `veterinarianId` (UUID): Filter by veterinarian
- `status` (Enum): Filter by visit status
- `dateFrom` (LocalDate): Filter visits from date
- `dateTo` (LocalDate): Filter visits to date

**Visit Statuses:** `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `NO_SHOW`

---

### Invoices API
**Base Path:** `/api/v1/invoices`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create invoice | ACCOUNTANT or ADMIN |
| GET | `/` | Get invoices (with filters: clientId, patientId, status, dateFrom, dateTo) | ACCOUNTANT or ADMIN |
| GET | `/{id}` | Get invoice by ID | ACCOUNTANT or ADMIN |
| GET | `/number/{invoiceNumber}` | Get invoice by number | ACCOUNTANT or ADMIN |
| GET | `/client/{clientId}` | Get invoices for client | ACCOUNTANT or ADMIN |
| GET | `/client/{clientId}/debt` | Get client debt summary | ACCOUNTANT or ADMIN |
| GET | `/visit/{visitId}` | Get invoices for visit | ACCOUNTANT or ADMIN |
| PUT | `/{id}` | Update invoice | ACCOUNTANT or ADMIN |
| POST | `/{id}/items` | Add item to invoice | ACCOUNTANT or ADMIN |
| POST | `/{id}/issue` | Issue invoice (change status to ISSUED) | ACCOUNTANT or ADMIN |
| POST | `/{id}/cancel` | Cancel invoice | ACCOUNTANT or ADMIN |
| GET | `/{id}/print` | Get printable invoice | ACCOUNTANT or ADMIN |
| DELETE | `/{id}` | Delete invoice | ACCOUNTANT or ADMIN |
| POST | `/{id}/payments` | Record payment for invoice | ACCOUNTANT or ADMIN |
| GET | `/{id}/payments` | Get payments for invoice | ACCOUNTANT or ADMIN |
| DELETE | `/payments/{paymentId}` | Delete payment | ACCOUNTANT or ADMIN |

**Invoice Statuses:** `DRAFT`, `ISSUED`, `PAID`, `PARTIALLY_PAID`, `OVERDUE`, `CANCELLED`

---

### Inventory API
**Base Path:** `/api/v1/inventory`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/invoices/upload` | Upload supplier invoice file | ACCOUNTANT or ADMIN |
| GET | `/invoices` | Get all supplier invoices | ACCOUNTANT or ADMIN |
| GET | `/invoices/{id}` | Get supplier invoice by ID | ACCOUNTANT or ADMIN |
| POST | `/invoices/{id}/process` | Process supplier invoice (add items to stock) | ACCOUNTANT or ADMIN |
| GET | `/stock` | Get inventory stock (optional: lowStockOnly=true) | ACCOUNTANT or ADMIN |
| PATCH | `/stock/{itemId}` | Manually adjust stock quantity | ACCOUNTANT or ADMIN |
| GET | `/transactions` | Get inventory transactions (optional: itemId filter) | ACCOUNTANT or ADMIN |

---

### Price List API
**Base Path:** `/api/v1/price-list`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create price list item | ACCOUNTANT or ADMIN |
| GET | `/` | Get all items (filters: active, category, name) | Any authenticated user |
| GET | `/{id}` | Get item by ID | Any authenticated user |
| PUT | `/{id}` | Update item | ACCOUNTANT or ADMIN |
| PATCH | `/{id}/active` | Toggle item active status | ACCOUNTANT or ADMIN |
| DELETE | `/{id}` | Delete item | ACCOUNTANT or ADMIN |

**Item Categories:** `MEDICATION`, `VACCINATION`, `CONSULTATION`, `LAB_TEST`, `PROCEDURE`, `SERVICE`, `SUPPLIES`, `FOOD`, `OTHER`

---

### Veterinarians API
**Base Path:** `/api/v1/veterinarians`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create veterinarian | ADMIN |
| GET | `/` | Get all veterinarians (filters: active, specialization, name) | Any authenticated user |
| GET | `/{id}` | Get veterinarian by ID | Any authenticated user |
| GET | `/email/{email}` | Get veterinarian by email | Any authenticated user |
| PUT | `/{id}` | Update veterinarian | ADMIN |
| PATCH | `/{id}/active` | Toggle veterinarian active status | ADMIN |
| DELETE | `/{id}` | Delete veterinarian | ADMIN |

---

### Audit Logs API
**Base Path:** `/api/v1/audit-logs`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create audit log | ADMIN |
| GET | `/` | Get all audit logs | ADMIN |
| GET | `/{id}` | Get audit log by ID | ADMIN |
| GET | `/entity/{entityType}/{entityId}` | Get audit logs for specific entity | ADMIN |
| GET | `/user/{userId}` | Get audit logs by user | ADMIN |
| GET | `/action/{action}` | Get audit logs by action | ADMIN |
| GET | `/date-range` | Get audit logs by date range (params: start, end) | ADMIN |
| GET | `/entity-type/{entityType}` | Get audit logs by entity type | ADMIN |

**Audit Actions:** `CREATE`, `UPDATE`, `DELETE`, `VIEW`, `EXPORT`

---

### GDPR Consents API
**Base Path:** `/api/v1/consents`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create consent record | RECEPTIONIST, VET, or ADMIN |
| GET | `/` | Get all consents | Any authenticated user |
| GET | `/{id}` | Get consent by ID | Any authenticated user |
| GET | `/client/{clientId}` | Get consents for client | Any authenticated user |
| GET | `/client/{clientId}/type/{consentType}` | Get consents for client and type | Any authenticated user |
| GET | `/status/{status}` | Get consents by status | Any authenticated user |
| GET | `/client/{clientId}/type/{consentType}/active` | Check if client has active consent of type | Any authenticated user |
| PUT | `/{id}` | Update consent | RECEPTIONIST, VET, or ADMIN |
| PUT | `/{id}/grant` | Grant consent (with optional signature reference) | RECEPTIONIST, VET, or ADMIN |
| PUT | `/{id}/revoke` | Revoke consent (with optional reason) | RECEPTIONIST, VET, or ADMIN |
| DELETE | `/{id}` | Delete consent | RECEPTIONIST, VET, or ADMIN |

**Consent Types:** `DATA_PROCESSING`, `MARKETING`, `THIRD_PARTY_SHARING`

**Consent Statuses:** `PENDING`, `GRANTED`, `REVOKED`, `EXPIRED`

---

### Vaccination Certificates API
**Base Path:** `/api/v1/certificates`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create vaccination certificate | VET or ADMIN |
| GET | `/` | Get all certificates | Any authenticated user |
| GET | `/{id}` | Get certificate by ID | Any authenticated user |
| GET | `/number/{certificateNumber}` | Get certificate by number | Any authenticated user |
| GET | `/patient/{patientId}` | Get certificates for patient | Any authenticated user |
| GET | `/client/{clientId}` | Get certificates for client | Any authenticated user |
| GET | `/type/{certificateType}` | Get certificates by type | Any authenticated user |
| GET | `/patient/{patientId}/type/{certificateType}` | Get certificates for patient and type | Any authenticated user |
| GET | `/patient/{patientId}/valid` | Get valid certificates for patient | Any authenticated user |
| PUT | `/{id}` | Update certificate | VET or ADMIN |
| PUT | `/{id}/invalidate` | Invalidate certificate (with reason) | VET or ADMIN |
| DELETE | `/{id}` | Delete certificate | VET or ADMIN |

**Certificate Types:** `RABIES`, `DHPP`, `FVRCP`, `BORDETELLA`, `LEPTO`, `OTHER`

---

### Document Versions API
**Base Path:** `/api/v1/document-versions`

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| POST | `/` | Create document version | ADMIN |
| GET | `/` | Get all document versions | ADMIN |
| GET | `/{id}` | Get version by ID | ADMIN |
| GET | `/document/{documentType}/{documentId}` | Get version history for document | ADMIN |
| GET | `/document/{documentType}/{documentId}/version/{versionNumber}` | Get specific version | ADMIN |
| GET | `/document/{documentType}/{documentId}/current` | Get current version | ADMIN |

---

## Database Schema

### Core Tables

#### `veterinary_clinics`
Multi-tenant support - each clinic has isolated data.

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| name | VARCHAR(255) | Clinic name |
| slug | VARCHAR(100) | Unique slug for URL |
| email | VARCHAR(255) | Contact email |
| phone | VARCHAR(50) | Contact phone |
| address | VARCHAR(500) | Street address |
| city | VARCHAR(100) | City |
| postal_code | VARCHAR(20) | Postal code |
| active | BOOLEAN | Active status |
| created_at | TIMESTAMP | Creation timestamp |

#### `clients` (Pet Owners)
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| first_name | VARCHAR(255) | Client first name |
| last_name | VARCHAR(255) | Client last name |
| email | VARCHAR(255) | Email (unique per clinic) |
| phone | VARCHAR(50) | Phone number |
| address | VARCHAR(255) | Street address |
| city | VARCHAR(100) | City |
| postal_code | VARCHAR(20) | Postal code |
| notes | TEXT | Additional notes |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

**Indexes:** clinic_id, email, last_name, phone, first_name

#### `patients` (Animals)
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| name | VARCHAR(255) | Patient name |
| species | VARCHAR(50) | Species (DOG, CAT, BIRD, etc.) |
| breed | VARCHAR(100) | Breed |
| date_of_birth | DATE | Birth date |
| weight | DOUBLE PRECISION | Weight |
| owner_id | UUID | Foreign key to clients |
| notes | TEXT | Medical notes |
| microchip_number | VARCHAR(50) | Microchip number |
| color | VARCHAR(255) | Fur/feather color |
| gender | VARCHAR(20) | Gender (MALE, FEMALE, UNKNOWN) |
| is_neutered | BOOLEAN | Neutered status |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

**Indexes:** clinic_id, owner_id, species, microchip_number, name

#### `patient_labels`
Many-to-many relationship for patient labels.

| Column | Type | Description |
|--------|------|-------------|
| patient_id | UUID | Foreign key to patients |
| label | VARCHAR(50) | Label (VIP, AGGRESSIVE, NERVOUS, etc.) |

**Primary Key:** (patient_id, label)

#### `veterinarians`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| first_name | VARCHAR(100) | First name |
| last_name | VARCHAR(100) | Last name |
| email | VARCHAR(255) | Email (unique) |
| phone | VARCHAR(50) | Phone number |
| specialization | VARCHAR(100) | Specialization |
| license_number | VARCHAR(50) | License number |
| color_code | VARCHAR(7) | Color for calendar (hex) |
| active | BOOLEAN | Active status |
| notes | TEXT | Additional notes |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

---

### Visits Tables

#### `visits`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| patient_id | UUID | Foreign key to patients |
| client_id | UUID | Client ID |
| veterinarian_id | UUID | Veterinarian ID |
| veterinarian_name | VARCHAR(255) | Veterinarian name (denormalized) |
| visit_date | TIMESTAMP | Visit date and time |
| visit_type | VARCHAR(50) | Type (CONSULTATION, EMERGENCY, etc.) |
| status | VARCHAR(20) | Status (SCHEDULED, IN_PROGRESS, COMPLETED, etc.) |
| duration_minutes | INTEGER | Duration in minutes |
| reason | VARCHAR(500) | Reason for visit |
| interview | TEXT | Patient interview notes |
| examination | TEXT | Examination findings |
| diagnosis | TEXT | Diagnosis |
| treatment | TEXT | Treatment performed |
| recommendations | TEXT | Recommendations |
| notes | TEXT | Additional notes |
| weight | DOUBLE PRECISION | Weight at visit |
| temperature | DOUBLE PRECISION | Temperature at visit |
| next_visit_date | DATE | Next recommended visit |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

**Indexes:** clinic_id, patient_id, client_id, visit_date, status, veterinarian_id

#### `visit_medications`
Medications prescribed during visit.

| Column | Type | Description |
|--------|------|-------------|
| visit_id | UUID | Foreign key to visits |
| medication_name | VARCHAR(255) | Medication name |
| dosage | VARCHAR(255) | Dosage |
| frequency | VARCHAR(255) | Frequency |
| duration | VARCHAR(255) | Duration |
| medication_notes | TEXT | Notes |

#### `visit_used_materials`
Materials/supplies used during visit.

| Column | Type | Description |
|--------|------|-------------|
| visit_id | UUID | Foreign key to visits |
| material_id | UUID | Price list item ID |
| material_name | VARCHAR(255) | Material name |
| quantity | INTEGER | Quantity used |
| cost_price | DECIMAL(10,2) | Cost price |
| sell_price | DECIMAL(10,2) | Sell price |
| unit | VARCHAR(20) | Unit |

---

### Billing Tables

#### `invoices`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| invoice_number | VARCHAR(50) | Unique invoice number |
| client_id | UUID | Client ID |
| patient_id | UUID | Patient ID |
| visit_id | UUID | Visit ID |
| issue_date | DATE | Issue date |
| due_date | DATE | Due date |
| status | VARCHAR(30) | Status (DRAFT, ISSUED, PAID, etc.) |
| subtotal | DECIMAL(10,2) | Subtotal |
| tax_rate | DECIMAL(5,2) | Tax rate percentage |
| tax_amount | DECIMAL(10,2) | Tax amount |
| total_amount | DECIMAL(10,2) | Total amount |
| paid_amount | DECIMAL(10,2) | Amount paid |
| notes | VARCHAR(1000) | Notes |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

**Indexes:** clinic_id, client_id, status

#### `invoice_items`
| Column | Type | Description |
|--------|------|-------------|
| invoice_id | UUID | Foreign key to invoices |
| item_name | VARCHAR(255) | Item name |
| item_description | VARCHAR(500) | Description |
| item_quantity | INTEGER | Quantity |
| item_unit_price | DECIMAL(10,2) | Unit price |
| item_total | DECIMAL(10,2) | Total price |
| price_list_item_id | UUID | Price list reference |

#### `payments`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| invoice_id | UUID | Foreign key to invoices |
| amount | DECIMAL(10,2) | Payment amount |
| payment_method | VARCHAR(30) | Payment method (CASH, CARD, TRANSFER) |
| payment_date | TIMESTAMP | Payment timestamp |
| transaction_reference | VARCHAR(255) | Transaction reference |
| notes | VARCHAR(500) | Notes |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

#### `price_list_items`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| name | VARCHAR(255) | Item name |
| description | VARCHAR(1000) | Description |
| category | VARCHAR(50) | Category |
| cost_price | DECIMAL(10,2) | Cost price |
| sell_price | DECIMAL(10,2) | Sell price |
| unit | VARCHAR(20) | Unit |
| active | BOOLEAN | Active status |
| code | VARCHAR(50) | Item code |
| stock_quantity | INTEGER | Current stock |
| reorder_point | INTEGER | Reorder threshold |
| barcode | VARCHAR(100) | Barcode |
| supplier_code | VARCHAR(100) | Supplier code |
| expiration_date | DATE | Expiration date |
| batch_number | VARCHAR(100) | Batch number |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

---

### Inventory Tables

#### `supplier_invoices`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| invoice_number | VARCHAR(100) | Supplier invoice number |
| supplier_name | VARCHAR(255) | Supplier name |
| supplier_nip | VARCHAR(20) | Supplier tax ID |
| invoice_date | DATE | Invoice date |
| sale_date | DATE | Sale date |
| payment_due_date | DATE | Payment due date |
| payment_method | VARCHAR(50) | Payment method |
| total_net | DECIMAL(12,2) | Net total |
| total_gross | DECIMAL(12,2) | Gross total |
| total_vat | DECIMAL(12,2) | VAT amount |
| file_name | VARCHAR(255) | Original file name |
| raw_content | TEXT | Raw invoice content |
| status | VARCHAR(20) | Status (PENDING, PROCESSED) |
| processed_at | TIMESTAMP | Processing timestamp |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

#### `supplier_invoice_items`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| invoice_id | UUID | Foreign key to supplier_invoices |
| item_id | UUID | Price list item reference |
| product_code | VARCHAR(50) | Product code |
| product_name | VARCHAR(255) | Product name |
| quantity | INTEGER | Quantity |
| unit | VARCHAR(20) | Unit |
| net_price | DECIMAL(12,2) | Net price |
| gross_price | DECIMAL(12,2) | Gross price |
| discount_percent | INTEGER | Discount percentage |
| vat_rate | INTEGER | VAT rate |
| vat_amount | DECIMAL(12,2) | VAT amount |
| batch_number | VARCHAR(50) | Batch number |
| expiration_date | DATE | Expiration date |
| barcode | VARCHAR(50) | Barcode |
| pkwiu | VARCHAR(20) | Product classification |
| matched | BOOLEAN | Matched to price list |
| created_at | TIMESTAMP | Creation timestamp |

#### `inventory_transactions`
| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| item_id | UUID | Price list item ID |
| transaction_type | VARCHAR(20) | Type (IN, OUT, ADJUSTMENT) |
| quantity | INTEGER | Quantity changed |
| quantity_before | INTEGER | Quantity before |
| quantity_after | INTEGER | Quantity after |
| reference_id | UUID | Reference to source (invoice, visit) |
| reference_type | VARCHAR(100) | Type of reference |
| batch_number | VARCHAR(100) | Batch number |
| expiration_date | DATE | Expiration date |
| unit_cost | DECIMAL(10,2) | Unit cost |
| notes | VARCHAR(1000) | Notes |
| created_by | VARCHAR(255) | User who created |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

---

### Compliance Tables

#### `audit_logs`
Tracks all system changes for compliance.

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| entity_type | VARCHAR(100) | Type of entity (Patient, Client, etc.) |
| entity_id | UUID | Entity ID |
| action | VARCHAR(50) | Action (CREATE, UPDATE, DELETE) |
| user_id | UUID | User who performed action |
| user_name | VARCHAR(255) | User name |
| timestamp | TIMESTAMP | Action timestamp |
| old_value | TEXT | Old value (JSON) |
| new_value | TEXT | New value (JSON) |
| changed_fields | TEXT | Changed fields |
| ip_address | VARCHAR(50) | IP address |
| user_agent | VARCHAR(500) | User agent |
| description | VARCHAR(500) | Description |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

**Indexes:** (entity_type, entity_id)

#### `gdpr_consents`
GDPR consent management.

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| client_id | UUID | Client ID |
| consent_type | VARCHAR(50) | Consent type |
| status | VARCHAR(20) | Status (PENDING, GRANTED, REVOKED) |
| requested_at | TIMESTAMP | Request timestamp |
| granted_at | TIMESTAMP | Grant timestamp |
| revoked_at | TIMESTAMP | Revoke timestamp |
| expires_at | DATE | Expiration date |
| consent_text | TEXT | Consent text |
| consent_version | VARCHAR(50) | Version |
| signature_reference | VARCHAR(255) | Signature reference |
| ip_address | VARCHAR(50) | IP address |
| notes | VARCHAR(500) | Notes |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

#### `vaccination_certificates`
Official vaccination certificates.

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| certificate_number | VARCHAR(50) | Unique certificate number |
| certificate_type | VARCHAR(50) | Type (RABIES, DHPP, etc.) |
| patient_id | UUID | Patient ID |
| patient_name | VARCHAR(255) | Patient name |
| patient_species | VARCHAR(100) | Species |
| patient_breed | VARCHAR(100) | Breed |
| microchip_number | VARCHAR(50) | Microchip |
| client_id | UUID | Client ID |
| client_name | VARCHAR(255) | Client name |
| visit_id | UUID | Visit ID |
| vaccine_name | VARCHAR(255) | Vaccine name |
| vaccine_manufacturer | VARCHAR(255) | Manufacturer |
| batch_number | VARCHAR(100) | Batch number |
| administration_date | DATE | Administration date |
| expiration_date | DATE | Expiration date |
| next_due_date | DATE | Next due date |
| veterinarian_id | UUID | Veterinarian ID |
| veterinarian_name | VARCHAR(255) | Veterinarian name |
| veterinarian_license_number | VARCHAR(100) | License number |
| notes | TEXT | Notes |
| is_valid | BOOLEAN | Valid status |
| invalidation_reason | VARCHAR(500) | Invalidation reason |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

#### `document_versions`
Version control for documents.

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| clinic_id | UUID | Foreign key to veterinary_clinics |
| document_type | VARCHAR(100) | Document type |
| document_id | UUID | Document ID |
| version_number | INTEGER | Version number |
| content | TEXT | Document content |
| created_by_user_id | UUID | User ID |
| created_by_user_name | VARCHAR(255) | User name |
| version_created_at | TIMESTAMP | Version timestamp |
| change_reason | VARCHAR(500) | Reason for change |
| is_current | BOOLEAN | Current version flag |
| checksum | VARCHAR(64) | Content checksum |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic locking version |

**Unique Constraint:** (clinic_id, document_type, document_id, version_number)

---

## Multi-tenancy

The VetClinic system implements **row-level multi-tenancy** where each clinic's data is isolated using a `clinic_id` column in all tables.

### How It Works

1. **JWT Token Contains clinic_id**
   - When users authenticate via Keycloak, the JWT token includes a `clinic_id` claim
   - This is configured in Keycloak via protocol mapper

2. **TenantFilter Sets Context**
   - On each request, `TenantFilter` extracts `clinic_id` from JWT
   - Stores it in `TenantContext` (ThreadLocal)

3. **Automatic Query Filtering**
   - All database queries are automatically filtered by `clinic_id`
   - Implemented via Hibernate filters

4. **Automatic clinic_id Setting**
   - When creating new entities, `clinic_id` is automatically set from TenantContext
   - Implemented via JPA entity listeners

### TenantContext API

```java
// Get current clinic ID for this request thread
UUID clinicId = TenantContext.getCurrentClinicId();

// Verify access to an entity
TenantContext.verifyAccess(entity.getClinicId(), "Patient", entity.getId());

// Check if user has access
boolean hasAccess = TenantContext.hasAccess(entity.getClinicId());
```

### Default Clinic

The system includes a default clinic with ID: `00000000-0000-0000-0000-000000000001`

All test users are assigned to this clinic.

---

## Authentication

### Keycloak Integration

The system uses **Keycloak** for authentication and authorization:

- **Realm:** `vetclinic`
- **Protocol:** OpenID Connect (OIDC)
- **Token Type:** JWT Bearer tokens
- **Token Lifetime:** 30 days (configurable, set long for development)

### Keycloak Clients

1. **vetclinic-app** (Backend)
   - Client ID: `vetclinic-app`
   - Client Secret: `vetclinic-secret`
   - Type: Confidential
   - Direct Access Grants: Enabled

2. **vetclinic-frontend** (Frontend)
   - Client ID: `vetclinic-frontend`
   - Type: Public
   - PKCE: Required (S256)

### Test Users

Pre-configured test users in Keycloak:

| Username | Password | Roles | Email | clinic_id |
|----------|----------|-------|-------|-----------|
| admin | admin | admin, vet, receptionist, accountant, user | admin@vetclinic.com | 00000000-0000-0000-0000-000000000001 |
| vet | vet | vet, user | vet@vetclinic.com | 00000000-0000-0000-0000-000000000001 |
| receptionist | receptionist | receptionist, user | receptionist@vetclinic.com | 00000000-0000-0000-0000-000000000001 |
| accountant | accountant | accountant, user | accountant@vetclinic.com | 00000000-0000-0000-0000-000000000001 |
| user | user | user | user@vetclinic.com | 00000000-0000-0000-0000-000000000001 |

### Getting an Access Token

**Via Backend Auth Controller:**
```bash
curl -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user"}'
```

**Direct Keycloak Token:**
```bash
curl -X POST http://localhost:8180/realms/vetclinic/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=vetclinic-app" \
  -d "client_secret=vetclinic-secret" \
  -d "username=user" \
  -d "password=user"
```

### Using the Token

Include the token in the Authorization header:
```bash
curl http://localhost:8080/api/v1/patients \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Keycloak Admin Console

- **URL:** http://localhost:8180
- **Username:** admin
- **Password:** admin

---

## Tech Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.3.x | Application framework |
| Spring Security | 6.x | Security & OAuth2 |
| Spring Data JPA | 3.x | Data persistence |
| Hibernate | 6.x | ORM |
| PostgreSQL | 16 | Database |
| Flyway | 10.x | Database migrations |
| MapStruct | 1.5.x | DTO mapping |
| Lombok | Latest | Boilerplate reduction |
| Keycloak | 26.0.7 | Authentication & Authorization |
| Testcontainers | Latest | Integration testing |
| JUnit 5 | 5.x | Unit testing |
| Mockito | 5.x | Mocking framework |
| Spotless | Latest | Code formatting |
| Checkstyle | Latest | Code style enforcement |
| SpotBugs | Latest | Static analysis |
| JaCoCo | Latest | Code coverage |
| ArchUnit | Latest | Architecture testing |
| Swagger/OpenAPI | 3.0 | API documentation |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.x | UI framework |
| TypeScript | 5.x | Type safety |
| Vite | 5.x | Build tool |
| React Query | Latest | Server state management |
| Axios | Latest | HTTP client |
| OpenAPI Generator | Latest | API client generation |

### Infrastructure

| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Local orchestration |
| PostgreSQL | Database |
| Keycloak | Identity & Access Management |

---

## Architecture

### Hexagonal Architecture (Ports and Adapters)

Each module follows a clean hexagonal architecture:

```
┌─────────────────────────────────────────────────┐
│                   API LAYER                      │
│         (Controllers, DTOs, Mappers)            │
│  - REST endpoints                                │
│  - Request/Response DTOs                         │
│  - MapStruct mappers                             │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│                 DOMAIN LAYER                     │
│    (Services, Entities, Ports/Interfaces)       │
│  - Business logic                                │
│  - Domain entities                               │
│  - Port interfaces (repositories, services)      │
└─────────────────┬───────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────┐
│            INFRASTRUCTURE LAYER                  │
│         (JPA Repositories, Adapters)            │
│  - JPA entities                                  │
│  - Repository implementations                    │
│  - External service adapters                     │
└─────────────────────────────────────────────────┘
```

### Module Structure

The backend is organized into independent modules:

- **common** - Shared utilities, base entities, security
- **patient-module** - Patient domain
- **client-module** - Client domain
- **visit-module** - Visit domain
- **billing-module** - Invoices, payments, price lists, inventory
- **veterinarian-module** - Veterinarian management
- **compliance-module** - Audit logs, GDPR, certificates, document versions
- **application** - Main Spring Boot application, configuration

### Key Architectural Principles

1. **Domain-Driven Design (DDD)**
   - Clear domain boundaries
   - Rich domain models
   - Ubiquitous language

2. **Dependency Inversion**
   - Domain defines interfaces (ports)
   - Infrastructure implements them (adapters)
   - API depends on domain

3. **Module Independence**
   - Modules communicate via well-defined interfaces
   - No direct dependencies between modules
   - Enforced via ArchUnit tests

4. **Testing Strategy**
   - Unit tests: Services with mocked repositories
   - Integration tests: Full Spring context with Testcontainers
   - Architecture tests: ArchUnit enforces rules

5. **Code Quality**
   - 80% code coverage minimum (JaCoCo)
   - Google Java Format (Spotless)
   - Checkstyle rules enforcement
   - SpotBugs static analysis

---

## Quick Reference

### Common Query Parameters

**Filtering:**
- `ownerId`, `clientId`, `patientId` - Filter by relationship
- `status` - Filter by status enum
- `dateFrom`, `dateTo` - Date range filtering
- `active` - Filter active/inactive items

**Searching:**
- `name` - Partial, case-insensitive search
- `email`, `phone` - Partial match
- `city` - Location search

### Common Response Codes

| Code | Meaning |
|------|---------|
| 200 | OK - Successful GET, PUT, PATCH |
| 201 | Created - Successful POST |
| 204 | No Content - Successful DELETE |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Missing/invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Duplicate or constraint violation |
| 500 | Internal Server Error |

### Environment URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Keycloak Admin | http://localhost:8180 |
| PostgreSQL | localhost:5432 (database: vetclinic) |

### Key Configuration Files

- `docker-compose.yml` - Docker services
- `backend/application/src/main/resources/application.yml` - Backend config
- `docker/keycloak/vetclinic-realm.json` - Keycloak realm
- `CLAUDE.md` - Development guidelines
- `postman-requests.json` - API request collection

---

**Last Updated:** 2025-12-26
**System Version:** 1.0.0
