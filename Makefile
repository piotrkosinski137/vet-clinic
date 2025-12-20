.PHONY: help build test run run-postgres clean format check db-start db-stop db-reset

help:
	@echo "Available commands:"
	@echo "  make build        - Build all modules"
	@echo "  make test         - Run all tests"
	@echo "  make run          - Run with H2 database"
	@echo "  make run-postgres - Run with PostgreSQL"
	@echo "  make clean        - Clean build artifacts"
	@echo "  make format       - Format code with Spotless"
	@echo "  make check        - Run all quality checks"
	@echo "  make db-start     - Start PostgreSQL container"
	@echo "  make db-stop      - Stop PostgreSQL container"
	@echo "  make db-reset     - Reset PostgreSQL database"

build:
	cd backend && ./mvnw clean install -DskipTests

test:
	cd backend && ./mvnw test

run:
	cd backend && ./mvnw spring-boot:run -pl application

run-postgres: db-start
	@echo "Waiting for database..."
	@sleep 3
	cd backend && ./mvnw spring-boot:run -pl application -Dspring-boot.run.profiles=postgres

clean:
	cd backend && ./mvnw clean

format:
	cd backend && ./mvnw spotless:apply

check:
	cd backend && ./mvnw verify -Pci

db-start:
	docker-compose up -d postgres

db-stop:
	docker-compose down

db-reset:
	docker-compose down -v
	docker-compose up -d postgres
