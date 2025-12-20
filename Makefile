.PHONY: help build test run run-postgres clean format check docker-start docker-stop docker-reset frontend frontend-install api-generate dev

help:
	@echo "Available commands:"
	@echo ""
	@echo "  Backend:"
	@echo "    make build        - Build all backend modules"
	@echo "    make test         - Run all backend tests"
	@echo "    make run          - Run backend with H2 database"
	@echo "    make run-postgres - Run backend with PostgreSQL"
	@echo "    make format       - Format backend code with Spotless"
	@echo "    make check        - Run all backend quality checks"
	@echo "    make clean        - Clean build artifacts"
	@echo ""
	@echo "  Frontend:"
	@echo "    make frontend-install - Install frontend dependencies"
	@echo "    make frontend         - Run frontend dev server"
	@echo "    make api-generate     - Generate API client from backend"
	@echo ""
	@echo "  Docker (PostgreSQL + Keycloak):"
	@echo "    make docker-start - Start PostgreSQL and Keycloak"
	@echo "    make docker-stop  - Stop all containers"
	@echo "    make docker-reset - Reset all data"
	@echo ""
	@echo "  Full Stack:"
	@echo "    make dev          - Instructions for full dev environment"

# Backend
build:
	cd backend && ./mvnw clean install -DskipTests

test:
	cd backend && ./mvnw test

run:
	cd backend && ./mvnw spring-boot:run -pl application

run-postgres: docker-start
	@echo "Waiting for services..."
	@sleep 5
	cd backend && ./mvnw spring-boot:run -pl application -Dspring-boot.run.profiles=postgres

clean:
	cd backend && ./mvnw clean
	rm -rf frontend/node_modules frontend/dist

format:
	cd backend && ./mvnw spotless:apply
	cd frontend && npm run format

check:
	cd backend && ./mvnw verify -Pci
	cd frontend && npm run lint

# Frontend
frontend-install:
	cd frontend && npm install

frontend: frontend-install
	cd frontend && npm run dev

api-generate:
	cd frontend && npm run api:generate

# Docker
docker-start:
	docker-compose up -d
	@echo ""
	@echo "Services starting..."
	@echo "  PostgreSQL: localhost:5432"
	@echo "  Keycloak:   http://localhost:8180 (admin/admin)"
	@echo ""
	@echo "Wait ~30 seconds for Keycloak to be ready."

docker-stop:
	docker-compose down

docker-reset:
	docker-compose down -v
	docker-compose up -d
	@echo "All data reset. Wait ~30 seconds for Keycloak."

# Full stack
dev:
	@echo ""
	@echo "=== Full Stack Development ==="
	@echo ""
	@echo "1. Start Docker (PostgreSQL + Keycloak):"
	@echo "   make docker-start"
	@echo ""
	@echo "2. Wait 30 seconds, then start backend:"
	@echo "   make run"
	@echo ""
	@echo "3. In another terminal, start frontend:"
	@echo "   make frontend"
	@echo ""
	@echo "4. Open http://localhost:3000"
	@echo "   Login: user / user"
	@echo ""
