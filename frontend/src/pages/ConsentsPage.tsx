import { useState, useEffect, useMemo } from 'react';
import { api } from '../api';
import { useI18n } from '../i18n';
import { getLocaleForLanguage } from '../constants/locale';
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
  TextArea,
  Modal,
  ModalTitle,
  ModalActions,
  FormField,
  Pagination,
  useToast,
  ConfirmDialog,
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

function formatDate(dateStr?: string, language?: string): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString(getLocaleForLanguage(language || 'en'));
}

export function ConsentsPage() {
  const { t, language } = useI18n();
  const [consents, setConsents] = useState<GdprConsentResponse[]>([]);
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<ConsentStatus | ''>('');
  const [page, setPage] = useState(1);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    type: 'revoke' | 'delete';
    consent: GdprConsentResponse | null;
  }>({ open: false, type: 'delete', consent: null });
  const { success, error: showError } = useToast();

  const [formData, setFormData] = useState({
    clientId: '',
    consentType: 'DATA_PROCESSING' as ConsentType,
    consentText: '',
    expiresAt: '',
    notes: '',
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
      setError(err instanceof Error ? err.message : t('errors.failedToLoad'));
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
    return result.sort((a, b) => new Date(b.requestedAt).getTime() - new Date(a.requestedAt).getTime());
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
      showError(t('errors.somethingWentWrong'));
      return;
    }
    if (!formData.consentText.trim()) {
      showError(t('errors.somethingWentWrong'));
      return;
    }
    setActionLoading(true);
    try {
      await api.createConsent({
        clientId: formData.clientId,
        consentType: formData.consentType,
        consentText: formData.consentText,
        expiresAt: formData.expiresAt || undefined,
        ipAddress: '0.0.0.0',
        notes: formData.notes || undefined,
      });
      success(t('success.saved'));
      setShowCreateModal(false);
      setFormData({ clientId: '', consentType: 'DATA_PROCESSING', consentText: '', expiresAt: '', notes: '' });
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleGrant = async (consent: GdprConsentResponse) => {
    setActionLoading(true);
    try {
      await api.grantConsent(consent.id);
      success(t('success.statusUpdated'));
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setActionLoading(false);
    }
  };

  const openRevokeDialog = (consent: GdprConsentResponse) => {
    setConfirmDialog({ open: true, type: 'revoke', consent });
  };

  const openDeleteDialog = (consent: GdprConsentResponse) => {
    setConfirmDialog({ open: true, type: 'delete', consent });
  };

  const closeConfirmDialog = () => {
    setConfirmDialog({ open: false, type: 'delete', consent: null });
  };

  const handleConfirmAction = async (inputValue?: string) => {
    if (!confirmDialog.consent) return;
    setActionLoading(true);
    try {
      if (confirmDialog.type === 'revoke') {
        await api.revokeConsent(confirmDialog.consent.id, inputValue || undefined);
        success(t('success.statusUpdated'));
      } else {
        await api.deleteConsent(confirmDialog.consent.id);
        success(t('success.deleted'));
      }
      closeConfirmDialog();
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div>
      <PageHeader
        title={t('consents.title')}
        actions={
          <Button variant="primary" onClick={() => setShowCreateModal(true)}>
            + {t('consents.requestConsent')}
          </Button>
        }
      />

      {/* Filters */}
      <Card variant="default" style={{ marginBottom: spacing.lg }}>
        <CardContent>
          <div style={{ display: 'flex', gap: spacing.md, alignItems: 'flex-end' }}>
            <div style={{ minWidth: '200px' }}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>
                {t('common.status')}
              </label>
              <Select
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value as ConsentStatus | ''); setPage(1); }}
              >
                <option value="">{t('common.allStatuses')}</option>
                {CONSENT_STATUSES.map(s => (
                  <option key={s} value={s}>{t(`consents.statuses.${s}`)}</option>
                ))}
              </Select>
            </div>
            <Button variant="ghost" onClick={() => { setStatusFilter(''); setPage(1); }}>
              {t('common.clear')}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Content */}
      <Card variant="default">
        {loading && (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Loading text={t('consents.loading')} />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text>{error}</Text>
            <Button variant="ghost" onClick={fetchData} style={{ marginTop: spacing.md }}>{t('errors.retry')}</Button>
          </div>
        )}

        {!loading && !error && filteredConsents.length === 0 && (
          <EmptyState
            icon="🔒"
            title={t('consents.noConsents')}
            description={t('consents.noConsentsHint')}
          />
        )}

        {!loading && !error && filteredConsents.length > 0 && (
          <>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('payments.client')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('common.type')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('common.status')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('consents.requestedAt')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('consents.grantedAt')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('consents.expiresAt')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('common.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedConsents.map(consent => (
                    <tr key={consent.id} style={{ borderBottom: `1px solid ${colors.neutral.borderLight}` }}>
                      <td style={{ padding: spacing.md }}>{getClientName(consent.clientId)}</td>
                      <td style={{ padding: spacing.md }}>
                        <Badge variant="secondary">{t(`consents.types.${consent.consentType}`) || consent.consentType}</Badge>
                      </td>
                      <td style={{ padding: spacing.md }}>
                        <Badge variant={statusColors[consent.status]}>{t(`consents.statuses.${consent.status}`)}</Badge>
                      </td>
                      <td style={{ padding: spacing.md }}>{formatDate(consent.requestedAt, language)}</td>
                      <td style={{ padding: spacing.md }}>{formatDate(consent.grantedAt, language)}</td>
                      <td style={{ padding: spacing.md }}>{formatDate(consent.expiresAt, language)}</td>
                      <td style={{ padding: spacing.md, textAlign: 'right' }}>
                        <div style={{ display: 'flex', gap: spacing.xs, justifyContent: 'flex-end' }}>
                          {consent.status === 'PENDING' && (
                            <Button size="sm" variant="success" onClick={() => handleGrant(consent)} disabled={actionLoading}>
                              {t('common.grant')}
                            </Button>
                          )}
                          {consent.status === 'GRANTED' && (
                            <Button size="sm" variant="secondary" onClick={() => openRevokeDialog(consent)} disabled={actionLoading}>
                              {t('common.revoke')}
                            </Button>
                          )}
                          <Button size="sm" variant="danger" onClick={() => openDeleteDialog(consent)} disabled={actionLoading}>
                            {t('common.delete')}
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
        <ModalTitle>{t('consents.requestConsent')}</ModalTitle>
        <FormField label={t('payments.client')} required>
          <Select
            value={formData.clientId}
            onChange={(e) => setFormData({ ...formData, clientId: e.target.value })}
          >
            <option value="">{t('common.selectClient')}</option>
            {clients.map(c => (
              <option key={c.id} value={c.id}>{c.firstName} {c.lastName}</option>
            ))}
          </Select>
        </FormField>
        <FormField label={t('consents.consentType')} required>
          <Select
            value={formData.consentType}
            onChange={(e) => setFormData({ ...formData, consentType: e.target.value as ConsentType })}
          >
            {CONSENT_TYPES.map(t_type => (
              <option key={t_type} value={t_type}>{t(`consents.types.${t_type}`) || t_type}</option>
            ))}
          </Select>
        </FormField>
        <FormField label={t('consents.consentText')} required>
          <TextArea
            value={formData.consentText}
            onChange={(e) => setFormData({ ...formData, consentText: e.target.value })}
            placeholder={t('consents.consentTextPlaceholder')}
            rows={3}
          />
        </FormField>
        <FormField label={t('consents.expirationDate')}>
          <Input
            type="date"
            value={formData.expiresAt}
            onChange={(e) => setFormData({ ...formData, expiresAt: e.target.value })}
          />
        </FormField>
        <FormField label={t('common.notes')}>
          <Input
            value={formData.notes}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            placeholder={t('payments.optionalNotes')}
          />
        </FormField>
        <ModalActions>
          <Button variant="ghost" onClick={() => setShowCreateModal(false)}>{t('common.cancel')}</Button>
          <Button variant="primary" onClick={handleCreateConsent} disabled={actionLoading}>
            {actionLoading ? t('common.creating') : t('consents.createRequest')}
          </Button>
        </ModalActions>
      </Modal>

      {/* Confirm Dialog */}
      <ConfirmDialog
        open={confirmDialog.open}
        onClose={closeConfirmDialog}
        onConfirm={handleConfirmAction}
        title={confirmDialog.type === 'revoke' ? t('common.revoke') : t('common.delete')}
        message={
          confirmDialog.type === 'revoke'
            ? t('consents.confirmRevoke')
            : t('consents.confirmDelete')
        }
        confirmLabel={confirmDialog.type === 'revoke' ? t('common.revoke') : t('common.delete')}
        variant="danger"
        inputRequired={false}
        loading={actionLoading}
      />
    </div>
  );
}
