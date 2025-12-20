import { useState, useEffect, useCallback } from 'react';
import { api, ClientResponse, ClientRequest } from '../api';

interface UseClients {
  clients: ClientResponse[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
  createClient: (client: ClientRequest) => Promise<ClientResponse>;
  deleteClient: (id: string) => Promise<void>;
}

export function useClients(): UseClients {
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchClients = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getClients();
      setClients(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch clients');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchClients();
  }, [fetchClients]);

  const createClient = async (client: ClientRequest): Promise<ClientResponse> => {
    const created = await api.createClient(client);
    setClients((prev) => [...prev, created]);
    return created;
  };

  const deleteClient = async (id: string): Promise<void> => {
    await api.deleteClient(id);
    setClients((prev) => prev.filter((c) => c.id !== id));
  };

  return {
    clients,
    loading,
    error,
    refresh: fetchClients,
    createClient,
    deleteClient,
  };
}
