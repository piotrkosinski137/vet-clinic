-- Flyway migration V1: Initial schema
-- Creates the base tables for the Vet Clinic application

-- Clients table (pet owners)
CREATE TABLE clients (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    address VARCHAR(255),
    city VARCHAR(100),
    postal_code VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT
);

-- Patients table (animals)
CREATE TABLE patients (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(50) NOT NULL,
    breed VARCHAR(100),
    date_of_birth DATE,
    weight DOUBLE PRECISION,
    owner_id UUID,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT,
    CONSTRAINT fk_patients_owner FOREIGN KEY (owner_id) REFERENCES clients(id) ON DELETE SET NULL
);

-- Indexes for common queries
CREATE INDEX idx_patients_owner_id ON patients(owner_id);
CREATE INDEX idx_patients_species ON patients(species);
CREATE INDEX idx_clients_email ON clients(email);
CREATE INDEX idx_clients_last_name ON clients(last_name);
