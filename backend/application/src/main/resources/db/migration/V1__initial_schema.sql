-- Flyway migration V1: Complete VetClinic Schema
-- All tables and seed data in a single migration

----------------------------------------------
-- EXTENSIONS
----------------------------------------------

-- Enable unaccent extension for diacritic-insensitive search (e.g., "Wozniak" finds "Woźniak")
CREATE EXTENSION IF NOT EXISTS unaccent;

----------------------------------------------
-- CORE TABLES
----------------------------------------------

-- Veterinary Clinics (multi-tenant support)
CREATE TABLE veterinary_clinics (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255),
    phone VARCHAR(50),
    address VARCHAR(500),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_veterinary_clinics_slug ON veterinary_clinics(slug);

-- Default clinic
INSERT INTO veterinary_clinics (id, name, slug, active, created_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Default Clinic', 'default-clinic', true, NOW());

-- Clients table (pet owners)
CREATE TABLE clients (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    address VARCHAR(255),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT,
    CONSTRAINT clients_email_clinic_unique UNIQUE (clinic_id, email)
);

CREATE INDEX idx_clients_clinic_id ON clients(clinic_id);
CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_last_name ON clients(last_name);
CREATE INDEX idx_clients_phone ON clients(phone);
CREATE INDEX idx_clients_first_name ON clients(first_name);

-- Patients table (animals)
CREATE TABLE patients (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    name VARCHAR(255) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100),
    date_of_birth DATE,
    weight DOUBLE PRECISION,
    owner_id UUID REFERENCES clients(id) ON DELETE SET NULL,
    notes TEXT,
    microchip_number VARCHAR(50),
    color VARCHAR(255),
    gender VARCHAR(20),
    is_neutered BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

CREATE INDEX idx_patients_clinic_id ON patients(clinic_id);
CREATE INDEX idx_patients_owner_id ON patients(owner_id);
CREATE INDEX idx_patients_species ON patients(species);
CREATE INDEX idx_patients_microchip ON patients(microchip_number);
CREATE INDEX idx_patients_name ON patients(name);

-- Patient labels
CREATE TABLE patient_labels (
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    label VARCHAR(50) NOT NULL,
    PRIMARY KEY (patient_id, label)
);

CREATE INDEX idx_patient_labels_label ON patient_labels(label);

-- Veterinarians
CREATE TABLE veterinarians (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    specialization VARCHAR(100),
    license_number VARCHAR(50),
    color_code VARCHAR(7),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_veterinarians_clinic ON veterinarians(clinic_id);
CREATE INDEX idx_veterinarians_active ON veterinarians(active);
CREATE INDEX idx_veterinarians_email ON veterinarians(email);

-- Veterinarian weekly schedules
CREATE TABLE veterinarian_schedules (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    veterinarian_id UUID NOT NULL REFERENCES veterinarians(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL,
    start_time TIME,
    end_time TIME,
    is_working_day BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_vet_schedule_day UNIQUE(clinic_id, veterinarian_id, day_of_week)
);

CREATE INDEX idx_vet_schedules_vet ON veterinarian_schedules(veterinarian_id);
CREATE INDEX idx_vet_schedules_clinic ON veterinarian_schedules(clinic_id);

-- Veterinarian days off (vacation, sick leave, etc.)
CREATE TABLE veterinarian_days_off (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    veterinarian_id UUID NOT NULL REFERENCES veterinarians(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    description TEXT,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_vet_days_off_vet ON veterinarian_days_off(veterinarian_id);
CREATE INDEX idx_vet_days_off_clinic ON veterinarian_days_off(clinic_id);
CREATE INDEX idx_vet_days_off_dates ON veterinarian_days_off(start_date, end_date);

----------------------------------------------
-- VISITS
----------------------------------------------

CREATE TABLE visits (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    patient_name VARCHAR(200),
    client_id UUID,
    client_name VARCHAR(200),
    veterinarian_id UUID,
    veterinarian_name VARCHAR(255),
    visit_date TIMESTAMP NOT NULL,
    visit_type VARCHAR(50) DEFAULT 'CONSULTATION',
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    duration_minutes INTEGER DEFAULT 30,
    reason VARCHAR(500),
    interview TEXT,
    examination TEXT,
    diagnosis TEXT,
    treatment TEXT,
    recommendations TEXT,
    notes TEXT,
    weight DOUBLE PRECISION,
    temperature DOUBLE PRECISION,
    next_visit_date DATE,
    -- Waiting room fields
    checked_in_at TIMESTAMP,
    waiting_room_notes VARCHAR(500),
    priority VARCHAR(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_visits_status CHECK (status IN ('SCHEDULED', 'CHECKED_IN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    CONSTRAINT chk_visits_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_visits_clinic_id ON visits(clinic_id);
CREATE INDEX idx_visits_patient_id ON visits(patient_id);
CREATE INDEX idx_visits_client_id ON visits(client_id);
CREATE INDEX idx_visits_visit_date ON visits(visit_date);
CREATE INDEX idx_visits_status ON visits(status);
CREATE INDEX idx_visits_veterinarian ON visits(veterinarian_id);
CREATE INDEX idx_visits_patient_date ON visits(patient_id, visit_date DESC);
CREATE INDEX idx_visits_waiting_room ON visits(clinic_id, status, checked_in_at) WHERE status = 'CHECKED_IN';

CREATE TABLE visit_medications (
    visit_id UUID NOT NULL REFERENCES visits(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(255),
    frequency VARCHAR(255),
    duration VARCHAR(255),
    medication_notes TEXT
);

CREATE INDEX idx_visit_medications_visit_id ON visit_medications(visit_id);

----------------------------------------------
-- VISIT DRAFTS (Auto-save functionality)
----------------------------------------------

-- Visit drafts for auto-save functionality
-- Stores work-in-progress visit data that hasn't been committed yet
CREATE TABLE visit_drafts (
    visit_id UUID PRIMARY KEY REFERENCES visits(id) ON DELETE CASCADE,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),

    -- Draft content (mirrors editable visit fields)
    visit_type VARCHAR(50),
    interview TEXT,
    examination TEXT,
    diagnosis TEXT,
    treatment TEXT,
    recommendations TEXT,
    weight DOUBLE PRECISION,
    temperature DOUBLE PRECISION,
    next_visit_date DATE,
    used_materials JSONB DEFAULT '[]',
    medications JSONB DEFAULT '[]',

    -- Metadata for debugging and conflict resolution
    saved_at TIMESTAMP NOT NULL DEFAULT NOW(),
    saved_by VARCHAR(255)
);

CREATE INDEX idx_visit_drafts_clinic ON visit_drafts(clinic_id);

COMMENT ON TABLE visit_drafts IS 'Temporary storage for visit form data before final save';
COMMENT ON COLUMN visit_drafts.saved_by IS 'Username or ID of the doctor who last edited the draft';

----------------------------------------------
-- BILLING
----------------------------------------------

CREATE TABLE price_list_items (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(50) NOT NULL,
    cost_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    sell_price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    code VARCHAR(50),
    stock_quantity INTEGER DEFAULT 0,
    reorder_point INTEGER DEFAULT 5,
    barcode VARCHAR(100),
    supplier_code VARCHAR(100),
    expiration_date DATE,
    batch_number VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_price_list_items_clinic ON price_list_items(clinic_id);
CREATE INDEX idx_price_list_items_category ON price_list_items(category);
CREATE INDEX idx_price_list_items_active ON price_list_items(active);
CREATE INDEX idx_price_list_items_code ON price_list_items(code);
CREATE INDEX idx_price_list_items_barcode ON price_list_items(barcode);

CREATE TABLE visit_used_materials (
    visit_id UUID NOT NULL REFERENCES visits(id) ON DELETE CASCADE,
    material_id UUID,
    material_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    cost_price DECIMAL(10, 2) NOT NULL,
    sell_price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(20)
);

CREATE INDEX idx_visit_used_materials_visit ON visit_used_materials(visit_id);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    client_id UUID NOT NULL,
    patient_id UUID,
    visit_id UUID,
    issue_date DATE NOT NULL,
    due_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0,
    tax_rate DECIMAL(5, 2) DEFAULT 0,
    tax_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_invoices_clinic ON invoices(clinic_id);
CREATE INDEX idx_invoices_client ON invoices(client_id);
CREATE INDEX idx_invoices_status ON invoices(status);

CREATE TABLE invoice_items (
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    item_name VARCHAR(255) NOT NULL,
    item_description VARCHAR(500),
    item_quantity INTEGER NOT NULL,
    item_unit_price DECIMAL(10, 2) NOT NULL,
    item_total DECIMAL(10, 2) NOT NULL,
    price_list_item_id UUID
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    invoice_id UUID NOT NULL REFERENCES invoices(id),
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    transaction_reference VARCHAR(255),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);

----------------------------------------------
-- INVENTORY
----------------------------------------------

CREATE TABLE supplier_invoices (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    invoice_number VARCHAR(100) NOT NULL,
    supplier_name VARCHAR(255),
    supplier_nip VARCHAR(20),
    invoice_date DATE,
    sale_date DATE,
    payment_due_date DATE,
    payment_method VARCHAR(50),
    total_net DECIMAL(12, 2),
    total_gross DECIMAL(12, 2),
    total_vat DECIMAL(12, 2),
    file_name VARCHAR(255),
    raw_content TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_supplier_invoices_clinic ON supplier_invoices(clinic_id);
CREATE INDEX idx_supplier_invoices_status ON supplier_invoices(status);

CREATE TABLE supplier_invoice_items (
    id UUID PRIMARY KEY,
    invoice_id UUID NOT NULL REFERENCES supplier_invoices(id) ON DELETE CASCADE,
    item_id UUID,
    product_code VARCHAR(50),
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit VARCHAR(20),
    net_price DECIMAL(12, 2),
    gross_price DECIMAL(12, 2),
    discount_percent INTEGER DEFAULT 0,
    vat_rate INTEGER DEFAULT 23,
    vat_amount DECIMAL(12, 2),
    batch_number VARCHAR(50),
    expiration_date DATE,
    barcode VARCHAR(50),
    pkwiu VARCHAR(20),
    matched BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_supplier_invoice_items_invoice ON supplier_invoice_items(invoice_id);

CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    item_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    quantity_before INTEGER,
    quantity_after INTEGER,
    reference_id UUID,
    reference_type VARCHAR(100),
    batch_number VARCHAR(100),
    expiration_date DATE,
    unit_cost DECIMAL(10, 2),
    notes VARCHAR(1000),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_inventory_transactions_clinic ON inventory_transactions(clinic_id);
CREATE INDEX idx_inventory_transactions_item ON inventory_transactions(item_id);

----------------------------------------------
-- COMPLIANCE
----------------------------------------------

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    old_value TEXT,
    new_value TEXT,
    changed_fields TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

CREATE TABLE document_versions (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    document_type VARCHAR(100) NOT NULL,
    document_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    content TEXT NOT NULL,
    created_by_user_id UUID NOT NULL,
    created_by_user_name VARCHAR(255) NOT NULL,
    version_created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason VARCHAR(500),
    is_current BOOLEAN NOT NULL DEFAULT true,
    checksum VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT,
    UNIQUE(clinic_id, document_type, document_id, version_number)
);

CREATE TABLE gdpr_consents (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    client_id UUID NOT NULL,
    consent_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    expires_at DATE,
    consent_text TEXT,
    consent_version VARCHAR(50),
    signature_reference VARCHAR(255),
    ip_address VARCHAR(50) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT
);

CREATE TABLE vaccination_certificates (
    id UUID PRIMARY KEY,
    clinic_id UUID NOT NULL REFERENCES veterinary_clinics(id),
    certificate_number VARCHAR(50) NOT NULL UNIQUE,
    certificate_type VARCHAR(50) NOT NULL,
    patient_id UUID NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    patient_species VARCHAR(100),
    patient_breed VARCHAR(100),
    microchip_number VARCHAR(50),
    client_id UUID NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    visit_id UUID,
    vaccine_name VARCHAR(255) NOT NULL,
    vaccine_manufacturer VARCHAR(255),
    batch_number VARCHAR(100),
    administration_date DATE NOT NULL,
    expiration_date DATE,
    next_due_date DATE,
    veterinarian_id UUID,
    veterinarian_name VARCHAR(255) NOT NULL,
    veterinarian_license_number VARCHAR(100),
    notes TEXT,
    is_valid BOOLEAN NOT NULL DEFAULT true,
    invalidation_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT
);

----------------------------------------------
-- SEED DATA
----------------------------------------------

-- Seed Veterinarians
DO $$
DECLARE
    default_clinic_id UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    INSERT INTO veterinarians (id, clinic_id, first_name, last_name, email, phone, specialization, license_number, color_code, active) VALUES
    (gen_random_uuid(), default_clinic_id, 'Anna', 'Kowalska', 'anna.kowalska@vetclinic.com', '+48 123 456 789', 'General Practice', 'VET-2020-001', '#4CAF50', true),
    (gen_random_uuid(), default_clinic_id, 'Piotr', 'Nowak', 'piotr.nowak@vetclinic.com', '+48 987 654 321', 'Surgery', 'VET-2018-042', '#2196F3', true),
    (gen_random_uuid(), default_clinic_id, 'Magdalena', 'Wisniewska', 'magdalena.wisniewska@vetclinic.com', '+48 555 123 456', 'Dermatology', 'VET-2019-015', '#9C27B0', true);
END $$;

-- Seed Price List
DO $$
DECLARE
    default_clinic_id UUID := '00000000-0000-0000-0000-000000000001';
BEGIN
    -- Medications (with stock)
    INSERT INTO price_list_items (id, clinic_id, name, description, category, cost_price, sell_price, unit, active, code, stock_quantity, reorder_point) VALUES
    (gen_random_uuid(), default_clinic_id, 'Amoxicillin 250mg', 'Antibiotic for bacterial infections', 'MEDICATION', 1.50, 2.50, 'tablet', true, 'MED-001', 150, 20),
    (gen_random_uuid(), default_clinic_id, 'Prednisone 5mg', 'Corticosteroid anti-inflammatory', 'MEDICATION', 0.80, 1.50, 'tablet', true, 'MED-002', 200, 30),
    (gen_random_uuid(), default_clinic_id, 'Rimadyl 100mg', 'Pain relief for dogs', 'MEDICATION', 2.40, 4.00, 'tablet', true, 'MED-003', 80, 15),
    (gen_random_uuid(), default_clinic_id, 'Metronidazole 250mg', 'Antibiotic/antiparasitic', 'MEDICATION', 1.20, 2.00, 'tablet', true, 'MED-004', 100, 20),
    (gen_random_uuid(), default_clinic_id, 'Meloxicam 1.5mg/ml', 'NSAID oral suspension', 'MEDICATION', 8.00, 15.00, 'ml', true, 'MED-005', 50, 10),
    (gen_random_uuid(), default_clinic_id, 'Clavamox 125mg', 'Broad-spectrum antibiotic', 'MEDICATION', 2.00, 3.50, 'tablet', true, 'MED-006', 3, 25),
    (gen_random_uuid(), default_clinic_id, 'Apoquel 16mg', 'Anti-itch medication', 'MEDICATION', 3.50, 6.00, 'tablet', true, 'MED-007', 60, 15),
    (gen_random_uuid(), default_clinic_id, 'Cerenia 24mg', 'Anti-nausea medication', 'MEDICATION', 5.00, 9.00, 'tablet', true, 'MED-008', 0, 10);

    -- Vaccinations (with stock)
    INSERT INTO price_list_items (id, clinic_id, name, description, category, cost_price, sell_price, unit, active, code, stock_quantity, reorder_point) VALUES
    (gen_random_uuid(), default_clinic_id, 'Rabies Vaccine', 'Core vaccine - 3 year protection', 'VACCINATION', 12.00, 25.00, 'dose', true, 'VAC-001', 25, 10),
    (gen_random_uuid(), default_clinic_id, 'DHPP Vaccine', 'Distemper, Hepatitis, Parvo, Parainfluenza', 'VACCINATION', 18.00, 35.00, 'dose', true, 'VAC-002', 30, 10),
    (gen_random_uuid(), default_clinic_id, 'FVRCP Vaccine', 'Feline viral rhinotracheitis, calicivirus, panleukopenia', 'VACCINATION', 15.00, 30.00, 'dose', true, 'VAC-003', 20, 8),
    (gen_random_uuid(), default_clinic_id, 'Bordetella Vaccine', 'Kennel cough prevention', 'VACCINATION', 10.00, 22.00, 'dose', true, 'VAC-004', 4, 10),
    (gen_random_uuid(), default_clinic_id, 'Leptospirosis Vaccine', 'Bacterial disease prevention', 'VACCINATION', 14.00, 28.00, 'dose', true, 'VAC-005', 15, 8);

    -- Products (with stock)
    INSERT INTO price_list_items (id, clinic_id, name, description, category, cost_price, sell_price, unit, active, code, stock_quantity, reorder_point) VALUES
    (gen_random_uuid(), default_clinic_id, 'Frontline Plus Dog L', 'Flea and tick prevention 20-40kg', 'PRODUCT', 18.00, 32.00, 'pipette', true, 'PRD-001', 40, 15),
    (gen_random_uuid(), default_clinic_id, 'Frontline Plus Cat', 'Flea and tick prevention for cats', 'PRODUCT', 15.00, 28.00, 'pipette', true, 'PRD-002', 35, 15),
    (gen_random_uuid(), default_clinic_id, 'Drontal Plus Dog', 'Dewormer for dogs', 'PRODUCT', 8.00, 15.00, 'tablet', true, 'PRD-003', 50, 20),
    (gen_random_uuid(), default_clinic_id, 'Ear Cleaner 100ml', 'Routine ear cleaning solution', 'PRODUCT', 6.00, 12.00, 'bottle', true, 'PRD-004', 25, 10),
    (gen_random_uuid(), default_clinic_id, 'Wound Spray 50ml', 'Antiseptic wound treatment', 'PRODUCT', 4.00, 9.00, 'bottle', true, 'PRD-005', 30, 12);

    -- Services (no stock tracking needed)
    INSERT INTO price_list_items (id, clinic_id, name, description, category, cost_price, sell_price, unit, active, code, stock_quantity, reorder_point) VALUES
    (gen_random_uuid(), default_clinic_id, 'Consultation', 'Standard veterinary consultation', 'CONSULTATION', 0.00, 50.00, 'visit', true, 'CON-001', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Emergency Consultation', 'After-hours emergency visit', 'CONSULTATION', 0.00, 120.00, 'visit', true, 'CON-002', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'X-Ray', 'Digital radiograph imaging', 'LAB_TEST', 25.00, 85.00, 'session', true, 'LAB-001', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Blood Panel - Basic', 'CBC and chemistry panel', 'LAB_TEST', 30.00, 75.00, 'test', true, 'LAB-002', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Ultrasound', 'Abdominal ultrasound', 'LAB_TEST', 40.00, 120.00, 'session', true, 'LAB-003', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Spay - Cat', 'Ovariohysterectomy for cats', 'PROCEDURE', 0.00, 150.00, 'procedure', true, 'PROC-001', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Neuter - Dog', 'Castration for dogs', 'PROCEDURE', 0.00, 180.00, 'procedure', true, 'PROC-002', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Dental Cleaning', 'Teeth cleaning and polish', 'PROCEDURE', 0.00, 200.00, 'procedure', true, 'PROC-003', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Nail Trim', 'Nail clipping service', 'SERVICE', 0.00, 15.00, 'service', true, 'SVC-001', NULL, NULL),
    (gen_random_uuid(), default_clinic_id, 'Microchip Implant', 'Pet identification chip', 'SERVICE', 15.00, 45.00, 'service', true, 'SVC-002', 20, 5);
END $$;
