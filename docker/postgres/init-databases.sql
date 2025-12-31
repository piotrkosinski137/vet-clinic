-- Initialize multiple databases in single Postgres instance
-- This script runs on first container startup

-- Create keycloak database (vetclinic database is created by POSTGRES_DB env var)
CREATE DATABASE keycloak;

-- Create keycloak user with permissions
CREATE USER keycloak WITH ENCRYPTED PASSWORD 'keycloak';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;

-- Connect to keycloak database and grant schema permissions
\c keycloak
GRANT ALL ON SCHEMA public TO keycloak;
