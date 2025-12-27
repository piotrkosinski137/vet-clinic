import { useState, useEffect, useMemo } from 'react';
import { api } from '../api';
import type { GdprConsentResponse, ConsentType, ConsentStatus, ClientResponse } from '../api/types';
import {
  PageHeader,
  Card,
  CardContent,
  Button,
  Badge,
  Text,
  Loading,
  EmptyState,
  Select,
  Input,
  Modal,
  ModalTitle,
  ModalActions,
  FormField,
  Pagination,
  useToast,
} from '../components/ui';
import { colors, spacing } from '../theme';
import type { BadgeVariant } from '../components/ui/Badge';

const CONSENT_TYPES: ConsentType[] = ['DATA_PROCESSING', 'MARKETING', 'THIRD_PARTY_SHARING', 'MEDICAL_RECORDS', 'PHOTO_VIDEO', 'RESEARCH'];
const CONSENT_STATUSES: ConsentStatus[] = ['PENDING', 'GRANTED', 'REVOKED', 'EXPIRED'];
const ITEMS_PER_PAGE = 15;

const statusColors: Record<ConsentStatus, BadgeVariant> = {
  PENDING: 'warning',
  GRANTED: 'success',
  REVOKED: 'danger',
  EXPIRED: 'secondary',
};

const consentTypeLabels: Record<ConsentType, string> = {
  DATA_PROCESSING: 'Data Processing',
  MARKETING: 'Marketing',
  THIRD_PARTY_SHARING: 'Third Party Sharing',
  MEDICAL_RECORDS: 'Medical Records',
  PHOTO_VIDEO: 'Photo/Video',
  RESEARCH: 'Research',
};

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('pl-PL');
}

export function ConsentsPage() {
  const [consents, setConsents] = useState<GdprConsentResponse[]>([]);
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<ConsentStatus | ''>('');
  const [page, setPage] = useState(1);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const { success, error: showError } = useToast();

  // Form state for creating consent
  const [formData, setFormData] = useState({
    clientId: '',
    consentType: 'DATA_PROCESSING' as ConsentType,
    expirationDate: '',
  });

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [consentsData, clientsData] = await Promise.all([
        api.getConsents(),
        api.getClients(),
      ]);
      setConsents(consentsData);
      setClients(clientsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const filteredConsents = useMemo(() => {
    let result = consents;
    if (statusFilter) {
      result = result.filter(c => c.status === statusFilter);
    }
    return result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }, [consents, statusFilter]);

  const paginatedConsents = useMemo(() => {
    const start = (page - 1) * ITEMS_PER_PAGE;
    return filteredConsents.slice(start, start + ITEMS_PER_PAGE);
  }, [filteredConsents, page]);

  const getClientName = (clientId: string) => {
    const client = clients.find(c => c.id === clientId);
    return client ? `${client.firstName} ${client.lastName}` : clientId.slice(0, 8) + '...';
  };

  const handleCreateConsent = async () => {
    if (!formData.clientId) {
      showError('Please select a client');
      return;
    }
    setActionLoading(true);
    try {
      await api.createConsent({
        clientId: formData.clientId,
        consentType: formData.consentType,
        expirationDate: formData.expirationDate || undefined,
      });
      success('Consent request created');
      setShowCreateModal(false);
      setFormData({ clientId: '', consentType: 'DATA_PROCESSING', expirationDate: '' });
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to create consent');
    } finally {
      setActionLoading(false);
    }
  };

  const handleGrant = async (consent: GdprConsentResponse) => {
    setActionLoading(true);
    try {
      await api.grantConsent(consent.id);
      success('Consent granted');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to grant consent');
    } finally {
      setActionLoading(false);
    }
  };

  const handleRevoke = async (consent: GdprConsentResponse) => {
    const reason = window.prompt('Reason for revocation (optional):');
    setActionLoading(true);
    try {
      await api.revokeConsent(consent.id, reason || undefined);
      success('Consent revoked');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to revoke consent');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (consent: GdprConsentResponse) => {
    if (!window.confirm('Are you sure you want to delete this consent record?')) return;
    setActionLoading(true);
    try {
      await api.deleteConsent(consent.id);
      success('Consent deleted');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to delete consent');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="GDPR Consents"
        actions={
          <Button variant="primary" onClick={() => setShowCreateModal(true)}>
            + Request Consent
          </Button>
        }
      />

      {/* Filters */}
      <Card variant="default" style={{ marginBottom: spacing.lg }}>
        <CardContent>
          <div style={{ display: 'flex', gap: spacing.md, alignItems: 'flex-end' }}>
            <div style={{ minWidth: '200px' }}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>
                Status
              </label>
              <Select
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value as ConsentStatus | ''); setPage(1); }}
              >
                <option value="">All Statuses</option>
                {CONSENT_STATUSES.map(s => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </Select>
            </div>
            <Button variant="ghost" onClick={() => { setStatusFilter(''); setPage(1); }}>
              Clear
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Content */}
      <Card variant="default">
        {loading && (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Loading text="Loading consents..." />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text>{error}</Text>
            <Button variant="ghost" onClick={fetchData} style={{ marginTop: spacing.md }}>Retry</Button>
          </div>
        )}

        {!loading && !error && filteredConsents.length === 0 && (
          <EmptyState
            icon="🔒"
            title="No consent records"
            description="Create a consent request to get started."
          />
        )}

        {!loading && !error && filteredConsents.length > 0 && (
          <>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Client</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Type</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Status</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Requested</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Granted</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Expires</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedConsents.map(consent => (
                    <tr key={consent.id} style={{ borderBottom: `1px solid ${colors.neutral.borderLight}` }}>
                      <td style={{ padding: spacing.md }}>{getClientName(consent.clientId)}</td>
                      <td style={{ padding: spacing.md }}>
                        <Badge variant="secondary">{consentTypeLabels[consent.consentType]}</Badge>
                      </td>
                      <td style={{ padding: spacing.md }}>
                        <Badge variant={statusColors[consent.status]}>{consent.status}</Badge>
                      </td>
                      <td style={{ padding: spacing.md }}>{formatDate(consent.requestedAt)}</td>
                      <td style={{ padding: spacing.md }}>{formatDate(consent.grantedAt)}</td>
                      <td style={{ padding: spacing.md }}>{formatDate(consent.expirationDate)}</td>
                      <td style={{ padding: spacing.md, textAlign: 'right' }}>
                        <div style={{ display: 'flex', gap: spacing.xs, justifyContent: 'flex-end' }}>
                          {consent.status === 'PENDING' && (
                            <Button size="sm" variant="success" onClick={() => handleGrant(consent)} disabled={actionLoading}>
                              Grant
                            </Button>
                          )}
                          {consent.status === 'GRANTED' && (
                            <Button size="sm" variant="secondary" onClick={() => handleRevoke(consent)} disabled={actionLoading}>
                              Revoke
                            </Button>
                          )}
                          <Button size="sm" variant="danger" onClick={() => handleDelete(consent)} disabled={actionLoading}>
                            Delete
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {filteredConsents.length > ITEMS_PER_PAGE && (
              <div style={{ padding: spacing.md, display: 'flex', justifyContent: 'center' }}>
                <Pagination
                  currentPage={page - 1}
                  totalItems={filteredConsents.length}
                  pageSize={ITEMS_PER_PAGE}
                  onPageChange={(p) => setPage(p + 1)}
                />
              </div>
            )}
          </>
        )}
      </Card>

      {/* Create Consent Modal */}
      <Modal open={showCreateModal} onClose={() => setShowCreateModal(false)}>
        <ModalTitle>Request Consent</ModalTitle>
        <FormField label="Client" required>
          <Select
            value={formData.clientId}
            onChange={(e) => setFormData({ ...formData, clientId: e.target.value })}
          >
            <option value="">Select client...</option>
            {clients.map(c => (
              <option key={c.id} value={c.id}>{c.firstName} {c.lastName}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Consent Type" required>
          <Select
            value={formData.consentType}
            onChange={(e) => setFormData({ ...formData, consentType: e.target.value as ConsentType })}
          >
            {CONSENT_TYPES.map(t => (
              <option key={t} value={t}>{consentTypeLabels[t]}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Expiration Date">
          <Input
            type="date"
            value={formData.expirationDate}
            onChange={(e) => setFormData({ ...formData, expirationDate: e.target.value })}
          />
        </FormField>
        <ModalActions>
          <Button variant="ghost" onClick={() => setShowCreateModal(false)}>Cancel</Button>
          <Button variant="primary" onClick={handleCreateConsent} disabled={actionLoading}>
            {actionLoading ? 'Creating...' : 'Create Request'}
          </Button>
        </ModalActions>
      </Modal>
    </div>
  );
}
