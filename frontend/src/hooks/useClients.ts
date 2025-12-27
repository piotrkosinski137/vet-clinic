import { useMemo } from 'react';
import { api, ClientResponse, ClientRequest } from '../api';
import { useCrud, UseCrudResult } from './useCrud';

/**
 * Hook for managing clients with CRUD operations.
 * Built on the generic useCrud hook for consistency.
 */
export interface UseClients {
  clients: ClientResponse[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
  createClient: (client: ClientRequest) => Promise<ClientResponse>;
  updateClient: (id: string, client: ClientRequest) => Promise<ClientResponse>;
  deleteClient: (id: string) => Promise<void>;
}

export function useClients(): UseClients {
  const config = useMemo(
    () => ({
      fetchAll: () => api.getClients(),
      create: (data: ClientRequest) => api.createClient(data),
      update: (id: string, data: ClientRequest) => api.updateClient(id, data),
      remove: (id: string) => api.deleteClient(id),
      fetchErrorMessage: 'Failed to fetch clients',
    }),
    []
  );

  const crud: UseCrudResult<ClientResponse, ClientRequest> = useCrud(config);

  return {
    clients: crud.items,
    loading: crud.loading,
    error: crud.error,
    refresh: crud.refresh,
    createClient: crud.create,
    updateClient: crud.update,
    deleteClient: crud.remove,
  };
}
