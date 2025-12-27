/**
 * Environment configuration with type-safe access to Vite env variables.
 * Default values provided for local development.
 */

export const config = {
  keycloak: {
    url: import.meta.env.VITE_KEYCLOAK_URL || 'http://localhost:8180',
    realm: import.meta.env.VITE_KEYCLOAK_REALM || 'vetclinic',
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'vetclinic-frontend',
  },
  api: {
    baseUrl: import.meta.env.VITE_API_BASE_URL || '/api/v1',
    timeout: Number(import.meta.env.VITE_API_TIMEOUT) || 30000,
  },
} as const;

// Type for the config object
export type AppConfig = typeof config;
