# VetClinic

## Run the App

### 1. Start Database & Keycloak

```bash
docker-compose up -d
```

Wait ~30 seconds for services to start.

### 2. Start Backend

```bash
cd backend
./mvnw spring-boot:run -pl application
```

Backend runs at http://localhost:8080

### 3. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at http://localhost:3000

### 4. Login

- **Username**: `admin `
- **Password**: `admin`

---

## Other Info

### URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Keycloak Admin | http://localhost:8180 (admin/admin) |

### Stop Everything

```bash
docker-compose down
```

### Reset Database

```bash
docker-compose down -v
docker-compose up -d
```

### Run Tests

```bash
cd backend
./mvnw test
```

### Format Code

```bash
cd backend
./mvnw spotless:apply
```
