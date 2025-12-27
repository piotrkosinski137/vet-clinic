import { ClientResponse, PatientResponse } from '../../api';
import { Button, Input, Text } from '../ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { PAGINATION, getSpeciesInfo } from '../../constants';

interface ClientListPanelProps {
  clients: ClientResponse[];
  selectedClient: ClientResponse | null;
  onSelectClient: (client: ClientResponse) => void;
  onCreateClient: () => void;
  searchQuery: string;
  onSearchChange: (query: string) => void;
  loading: boolean;
  error: string | null;
  allPatients: PatientResponse[];
  loadingAllPatients: boolean;
}

export function ClientListPanel({
  clients,
  selectedClient,
  onSelectClient,
  onCreateClient,
  searchQuery,
  onSearchChange,
  loading,
  error,
  allPatients,
  loadingAllPatients,
}: ClientListPanelProps) {
  const [clientsPage, setClientsPage] = React.useState(0);

  const getPetsForClient = (clientId: string): PatientResponse[] => {
    return allPatients.filter((p) => p.ownerId === clientId);
  };

  // Reset clients page when search changes
  React.useEffect(() => {
    setClientsPage(0);
  }, [searchQuery]);

  if (loading) {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: colors.neutral.white,
          borderRadius: borderRadius.md,
          border: `1px solid ${colors.neutral.border}`,
          overflow: 'hidden',
          alignItems: 'center',
          justifyContent: 'center',
          padding: spacing.xl,
        }}
      >
        <Text variant="muted">Loading clients...</Text>
      </div>
    );
  }

  if (error) {
    return (
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: colors.neutral.white,
          borderRadius: borderRadius.md,
          border: `1px solid ${colors.neutral.border}`,
          overflow: 'hidden',
          alignItems: 'center',
          justifyContent: 'center',
          padding: spacing.xl,
        }}
      >
        <Text variant="muted">Error: {error}</Text>
      </div>
    );
  }

  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: colors.neutral.white,
        borderRadius: borderRadius.md,
        border: `1px solid ${colors.neutral.border}`,
        overflow: 'hidden',
      }}
    >
      {/* Search */}
      <div style={{ padding: spacing.md, borderBottom: `1px solid ${colors.neutral.border}` }}>
        <Input
          type="text"
          placeholder="Search clients or pets..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          style={{ width: '100%' }}
        />
        {loadingAllPatients && (
          <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
            Loading pets...
          </Text>
        )}
      </div>

      {/* Clients List */}
      <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
        {clients.length === 0 ? (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Text variant="muted">No clients found</Text>
            <Button variant="primary" size="sm" onClick={onCreateClient} style={{ marginTop: spacing.md }}>
              + Add Client
            </Button>
          </div>
        ) : (
          <>
            {/* Client count */}
            <div
              style={{
                padding: `${spacing.xs} ${spacing.md}`,
                backgroundColor: colors.neutral.background,
                borderBottom: `1px solid ${colors.neutral.border}`,
              }}
            >
              <Text variant="muted" size="sm">
                Showing {Math.min(clients.length, clientsPage * PAGINATION.PANEL + 1)}-
                {Math.min(clients.length, (clientsPage + 1) * PAGINATION.PANEL)} of {clients.length} clients
              </Text>
            </div>

            {/* Paginated clients */}
            <div style={{ flex: 1, overflowY: 'auto' }}>
              {clients
                .slice(clientsPage * PAGINATION.PANEL, (clientsPage + 1) * PAGINATION.PANEL)
                .map((client) => {
                  const clientPets = getPetsForClient(client.id);
                  return (
                    <div
                      key={client.id}
                      onClick={() => onSelectClient(client)}
                      style={{
                        padding: spacing.md,
                        borderBottom: `1px solid ${colors.neutral.border}`,
                        cursor: 'pointer',
                        backgroundColor:
                          selectedClient?.id === client.id ? colors.primary.light : 'transparent',
                        transition: 'background-color 0.15s',
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                        <div
                          style={{
                            width: '36px',
                            height: '36px',
                            borderRadius: borderRadius.full,
                            backgroundColor:
                              selectedClient?.id === client.id
                                ? colors.primary.main
                                : colors.neutral.background,
                            color:
                              selectedClient?.id === client.id
                                ? colors.neutral.white
                                : colors.primary.main,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: fontWeight.bold,
                            fontSize: fontSize.sm,
                          }}
                        >
                          {client.firstName[0]}
                          {client.lastName[0]}
                        </div>
                        <div style={{ flex: 1, minWidth: 0 }}>
                          <Text style={{ fontWeight: fontWeight.medium }}>
                            {client.firstName} {client.lastName}
                          </Text>
                          <Text
                            variant="muted"
                            size="sm"
                            style={{
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            {client.phone || client.email}
                          </Text>
                          {/* Show pets */}
                          {clientPets.length > 0 && (
                            <div
                              style={{
                                display: 'flex',
                                flexWrap: 'wrap',
                                gap: '4px',
                                marginTop: spacing.xs,
                              }}
                            >
                              {clientPets.slice(0, 3).map((pet) => {
                                const speciesInfo = getSpeciesInfo(pet.species);
                                return (
                                  <span
                                    key={pet.id}
                                    style={{
                                      fontSize: fontSize.xs,
                                      backgroundColor: colors.secondary.light,
                                      color: colors.secondary.main,
                                      padding: '2px 6px',
                                      borderRadius: borderRadius.full,
                                      display: 'inline-flex',
                                      alignItems: 'center',
                                      gap: '2px',
                                    }}
                                  >
                                    {speciesInfo.emoji} {pet.name}
                                  </span>
                                );
                              })}
                              {clientPets.length > 3 && (
                                <span style={{ fontSize: fontSize.xs, color: colors.neutral.textMuted }}>
                                  +{clientPets.length - 3} more
                                </span>
                              )}
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
            </div>

            {/* Pagination Controls */}
            {clients.length > PAGINATION.PANEL && (
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  gap: spacing.xs,
                  padding: spacing.sm,
                  borderTop: `1px solid ${colors.neutral.border}`,
                  backgroundColor: colors.neutral.background,
                }}
              >
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setClientsPage((p) => Math.max(0, p - 1))}
                  disabled={clientsPage === 0}
                >
                  ←
                </Button>
                <Text size="sm" style={{ color: colors.neutral.textMuted }}>
                  {clientsPage + 1} / {Math.ceil(clients.length / PAGINATION.PANEL)}
                </Text>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() =>
                    setClientsPage((p) => Math.min(Math.ceil(clients.length / PAGINATION.PANEL) - 1, p + 1))
                  }
                  disabled={clientsPage >= Math.ceil(clients.length / PAGINATION.PANEL) - 1}
                >
                  →
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

// Add React import for hooks
import React from 'react';
