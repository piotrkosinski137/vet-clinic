-- Flyway migration V2: Add missing FK constraints and fix uniqueness per clinic

----------------------------------------------
-- VISITS TABLE: Add FK for veterinarian_id
----------------------------------------------
ALTER TABLE visits
ADD CONSTRAINT fk_visits_veterinarian
FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(id) ON DELETE SET NULL;

ALTER TABLE visits
ADD CONSTRAINT fk_visits_client
FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL;

----------------------------------------------
-- GDPR CONSENTS: Add FK for client_id
----------------------------------------------
ALTER TABLE gdpr_consents
ADD CONSTRAINT fk_gdpr_consents_client
FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE;

----------------------------------------------
-- VACCINATION CERTIFICATES: Add FK constraints
----------------------------------------------
ALTER TABLE vaccination_certificates
ADD CONSTRAINT fk_vaccination_certificates_patient
FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE RESTRICT;

ALTER TABLE vaccination_certificates
ADD CONSTRAINT fk_vaccination_certificates_client
FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE RESTRICT;

ALTER TABLE vaccination_certificates
ADD CONSTRAINT fk_vaccination_certificates_visit
FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE SET NULL;

ALTER TABLE vaccination_certificates
ADD CONSTRAINT fk_vaccination_certificates_veterinarian
FOREIGN KEY (veterinarian_id) REFERENCES veterinarians(id) ON DELETE SET NULL;

----------------------------------------------
-- INVOICES: Add FK constraints
----------------------------------------------
ALTER TABLE invoices
ADD CONSTRAINT fk_invoices_client
FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE RESTRICT;

ALTER TABLE invoices
ADD CONSTRAINT fk_invoices_patient
FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE SET NULL;

ALTER TABLE invoices
ADD CONSTRAINT fk_invoices_visit
FOREIGN KEY (visit_id) REFERENCES visits(id) ON DELETE SET NULL;

----------------------------------------------
-- INVOICE ITEMS: Add FK for price_list_item
----------------------------------------------
ALTER TABLE invoice_items
ADD CONSTRAINT fk_invoice_items_price_list_item
FOREIGN KEY (price_list_item_id) REFERENCES price_list_items(id) ON DELETE SET NULL;

----------------------------------------------
-- FIX UNIQUENESS CONSTRAINTS (per clinic, not global)
----------------------------------------------

-- Drop the global unique constraint on veterinarians email
ALTER TABLE veterinarians DROP CONSTRAINT IF EXISTS veterinarians_email_key;

-- Add unique constraint per clinic
ALTER TABLE veterinarians
ADD CONSTRAINT unique_veterinarian_email_per_clinic UNIQUE (clinic_id, email);

-- Drop the global unique constraint on invoice_number
ALTER TABLE invoices DROP CONSTRAINT IF EXISTS invoices_invoice_number_key;

-- Add unique constraint per clinic
ALTER TABLE invoices
ADD CONSTRAINT unique_invoice_number_per_clinic UNIQUE (clinic_id, invoice_number);

-- Drop the global unique constraint on vaccination certificate number
ALTER TABLE vaccination_certificates DROP CONSTRAINT IF EXISTS vaccination_certificates_certificate_number_key;

-- Add unique constraint per clinic
ALTER TABLE vaccination_certificates
ADD CONSTRAINT unique_certificate_number_per_clinic UNIQUE (clinic_id, certificate_number);

----------------------------------------------
-- ADDITIONAL INDEXES FOR PERFORMANCE
----------------------------------------------

-- GDPR Consents indexes
CREATE INDEX idx_gdpr_consents_clinic ON gdpr_consents(clinic_id);
CREATE INDEX idx_gdpr_consents_client ON gdpr_consents(client_id);

-- Vaccination Certificates indexes
CREATE INDEX idx_vaccination_certs_clinic ON vaccination_certificates(clinic_id);
CREATE INDEX idx_vaccination_certs_patient ON vaccination_certificates(patient_id);
CREATE INDEX idx_vaccination_certs_client ON vaccination_certificates(client_id);
CREATE INDEX idx_vaccination_certs_expiration ON vaccination_certificates(expiration_date);
CREATE INDEX idx_vaccination_certs_next_due ON vaccination_certificates(next_due_date);

-- Document Versions indexes
CREATE INDEX idx_document_versions_clinic ON document_versions(clinic_id);
CREATE INDEX idx_document_versions_document ON document_versions(document_type, document_id);

-- Audit Logs indexes
CREATE INDEX idx_audit_logs_clinic ON audit_logs(clinic_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);

-- Visits composite indexes for calendar views
CREATE INDEX idx_visits_clinic_vet_date ON visits(clinic_id, veterinarian_id, visit_date);
CREATE INDEX idx_visits_clinic_date ON visits(clinic_id, visit_date);

-- Invoice items index
CREATE INDEX idx_invoice_items_invoice ON invoice_items(invoice_id);

-- Payments clinic index
CREATE INDEX idx_payments_clinic ON payments(clinic_id);
