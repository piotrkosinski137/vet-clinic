# Vet Clinic Frontend

React + TypeScript + Vite frontend for the Vet Clinic application.

## Quick Start

```bash
# Install dependencies
npm install

# Start development server (backend must be running on :8080)
npm run dev
```

Open http://localhost:3000

## API Client Generation

The frontend uses **OpenAPI Generator** to generate TypeScript API client from the backend's OpenAPI spec. This ensures type safety and keeps frontend/backend in sync.

### Generate API Client

1. **Start the backend** (must be running to fetch OpenAPI spec):
   ```bash
   cd ../backend
   mvn spring-boot:run -pl application
   ```

2. **Generate the client**:
   ```bash
   npm run api:generate
   ```

This creates typed API client in `src/api/generated/`.

### When to Regenerate

Run `npm run api:generate` after:
- Adding new endpoints to the backend
- Changing request/response DTOs
- Modifying API paths or methods

### Generated vs Manual Client

The project includes both:
- `src/api/client.ts` - Manual fetch wrapper (works without generation)
- `src/api/generated/` - Auto-generated client (after running api:generate)

You can choose which to use. The generated client provides complete type safety.

## Project Structure

```
frontend/
├── src/
│   ├── api/
│   │   ├── client.ts       # Manual API client
│   │   ├── types.ts        # Type definitions
│   │   └── generated/      # Auto-generated (after npm run api:generate)
│   ├── components/         # Reusable UI components
│   ├── hooks/              # Custom React hooks
│   │   ├── usePatients.ts
│   │   └── useClients.ts
│   ├── pages/              # Page components
│   │   ├── PatientsPage.tsx
│   │   └── ClientsPage.tsx
│   ├── App.tsx             # Main app with routing
│   ├── main.tsx            # Entry point
│   └── index.css           # Global styles
├── index.html
├── vite.config.ts          # Vite config with proxy
├── tsconfig.json
├── openapitools.json       # OpenAPI Generator config
└── package.json
```

## Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run preview` | Preview production build |
| `npm run lint` | Run ESLint |
| `npm run format` | Format code with Prettier |
| `npm run api:generate` | Generate API client from running backend |

## Development

### Proxy Configuration

Vite is configured to proxy `/api` requests to `http://localhost:8080`:

```typescript
// vite.config.ts
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
},
```

### Adding New Pages

1. Create component in `src/pages/`
2. Add route in `src/App.tsx`
3. Add navigation link in App's nav

### Adding New API Calls

After regenerating the API client:

```typescript
import { PatientsApi, Configuration } from './api/generated';

const api = new PatientsApi(new Configuration({
  basePath: '/api/v1',
}));

// Use typed methods
const patients = await api.getAllPatients();
```
