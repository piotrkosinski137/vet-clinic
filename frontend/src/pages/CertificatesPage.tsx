import { useState, useEffect, useMemo } from 'react';
import { api } from '../api';
import { useI18n } from '../i18n';
import { getLocaleForLanguage } from '../constants/locale';
import type { VaccinationCertificateResponse, VaccinationCertificateRequest, CertificateType, PatientResponse, VeterinarianResponse, ClientResponse } from '../api/types';
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
  TextArea,
  Pagination,
  useToast,
  ConfirmDialog,
} from '../components/ui';
import { colors, spacing } from '../theme';
import type { BadgeVariant } from '../components/ui/Badge';

const CERTIFICATE_TYPES: CertificateType[] = ['RABIES', 'DISTEMPER', 'PARVOVIRUS', 'HEPATITIS', 'LEPTOSPIROSIS', 'BORDETELLA', 'FELINE_LEUKEMIA', 'FELINE_CALICIVIRUS', 'OTHER'];
const ITEMS_PER_PAGE = 15;

function formatDate(dateStr?: string, language?: string): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString(getLocaleForLanguage(language || 'en'));
}

function isExpiringSoon(dateStr?: string): boolean {
  if (!dateStr) return false;
  const expDate = new Date(dateStr);
  const now = new Date();
  const daysUntil = (expDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
  return daysUntil <= 30 && daysUntil > 0;
}

function isExpired(dateStr?: string): boolean {
  if (!dateStr) return false;
  return new Date(dateStr) < new Date();
}

export function CertificatesPage() {
  const { t, language } = useI18n();
  const [certificates, setCertificates] = useState<VaccinationCertificateResponse[]>([]);
  const [patients, setPatients] = useState<PatientResponse[]>([]);
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [veterinarians, setVeterinarians] = useState<VeterinarianResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [typeFilter, setTypeFilter] = useState<CertificateType | ''>('');
  const [validityFilter, setValidityFilter] = useState<'all' | 'valid' | 'invalid'>('all');
  const [page, setPage] = useState(1);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    type: 'invalidate' | 'delete';
    certificate: VaccinationCertificateResponse | null;
  }>({ open: false, type: 'delete', certificate: null });
  const { success, error: showError } = useToast();

  const [formData, setFormData] = useState<Partial<VaccinationCertificateRequest>>({
    patientId: '',
    certificateType: 'RABIES',
    vaccineName: '',
    administrationDate: new Date().toISOString().split('T')[0],
  });

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [certsData, patientsData, clientsData, vetsData] = await Promise.all([
        api.getCertificates(),
        api.getPatients(),
        api.getClients(),
        api.getVeterinarians({ active: true }),
      ]);
      setCertificates(certsData);
      setPatients(patientsData);
      setClients(clientsData);
      setVeterinarians(vetsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : t('errors.failedToLoad'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const filteredCertificates = useMemo(() => {
    let result = certificates;
    if (typeFilter) {
      result = result.filter(c => c.certificateType === typeFilter);
    }
    if (validityFilter === 'valid') {
      result = result.filter(c => c.isValid);
    } else if (validityFilter === 'invalid') {
      result = result.filter(c => !c.isValid);
    }
    return result.sort((a, b) => new Date(b.administrationDate).getTime() - new Date(a.administrationDate).getTime());
  }, [certificates, typeFilter, validityFilter]);

  const paginatedCertificates = useMemo(() => {
    const start = (page - 1) * ITEMS_PER_PAGE;
    return filteredCertificates.slice(start, start + ITEMS_PER_PAGE);
  }, [filteredCertificates, page]);

  const getPatientName = (patientId: string) => {
    const patient = patients.find(p => p.id === patientId);
    return patient ? patient.name : patientId.slice(0, 8) + '...';
  };

  const handleCreate = async () => {
    if (!formData.patientId || !formData.vaccineName || !formData.veterinarianId) {
      showError(t('errors.somethingWentWrong'));
      return;
    }

    const patient = patients.find(p => p.id === formData.patientId);
    const client = patient ? clients.find(c => c.id === patient.ownerId) : null;
    const vet = veterinarians.find(v => v.id === formData.veterinarianId);

    if (!patient || !client || !vet) {
      showError(t('errors.somethingWentWrong'));
      return;
    }

    setActionLoading(true);
    try {
      await api.createCertificate({
        certificateType: formData.certificateType || 'RABIES',
        patientId: patient.id,
        patientName: patient.name,
        patientSpecies: patient.species,
        patientBreed: patient.breed,
        clientId: client.id,
        clientName: `${client.firstName} ${client.lastName}`,
        vaccineName: formData.vaccineName || '',
        vaccineManufacturer: formData.vaccineManufacturer,
        batchNumber: formData.batchNumber,
        administrationDate: formData.administrationDate,
        expirationDate: formData.expirationDate,
        nextDueDate: formData.nextDueDate,
        veterinarianId: vet.id,
        veterinarianName: vet.fullName,
        notes: formData.notes,
      });
      success(t('success.saved'));
      setShowCreateModal(false);
      setFormData({
        patientId: '',
        certificateType: 'RABIES',
        vaccineName: '',
        administrationDate: new Date().toISOString().split('T')[0],
      });
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setActionLoading(false);
    }
  };

  const openInvalidateDialog = (cert: VaccinationCertificateResponse) => {
    setConfirmDialog({ open: true, type: 'invalidate', certificate: cert });
  };

  const openDeleteDialog = (cert: VaccinationCertificateResponse) => {
    setConfirmDialog({ open: true, type: 'delete', certificate: cert });
  };

  const closeConfirmDialog = () => {
    setConfirmDialog({ open: false, type: 'delete', certificate: null });
  };

  const handleConfirmAction = async (inputValue?: string) => {
    if (!confirmDialog.certificate) return;
    setActionLoading(true);
    try {
      if (confirmDialog.type === 'invalidate') {
        if (!inputValue) {
          showError(t('errors.somethingWentWrong'));
          setActionLoading(false);
          return;
        }
        await api.invalidateCertificate(confirmDialog.certificate.id, inputValue);
        success(t('success.statusUpdated'));
      } else {
        await api.deleteCertificate(confirmDialog.certificate.id);
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

  const getStatusBadge = (cert: VaccinationCertificateResponse): { variant: BadgeVariant; text: string } => {
    if (!cert.isValid) return { variant: 'danger', text: t('certificates.invalid') };
    if (isExpired(cert.expirationDate)) return { variant: 'danger', text: t('certificates.expired') };
    if (isExpiringSoon(cert.expirationDate)) return { variant: 'warning', text: t('certificates.expiringSoon') };
    return { variant: 'success', text: t('certificates.valid') };
  };

  return (
    <div>
      <PageHeader
        title={t('certificates.title')}
        actions={
          <Button variant="primary" onClick={() => setShowCreateModal(true)}>
            + {t('certificates.newCertificate')}
          </Button>
        }
      />

      {/* Filters */}
      <Card variant="default" style={{ marginBottom: spacing.lg }}>
        <CardContent>
          <div style={{ display: 'flex', gap: spacing.md, alignItems: 'flex-end', flexWrap: 'wrap' }}>
            <div style={{ minWidth: '180px' }}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>{t('common.type')}</label>
              <Select
                value={typeFilter}
                onChange={(e) => { setTypeFilter(e.target.value as CertificateType | ''); setPage(1); }}
              >
                <option value="">{t('certificates.allTypes')}</option>
                {CERTIFICATE_TYPES.map(type => (
                  <option key={type} value={type}>{t(`certificates.types.${type}`)}</option>
                ))}
              </Select>
            </div>
            <div style={{ minWidth: '150px' }}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>{t('certificates.validity')}</label>
              <Select
                value={validityFilter}
                onChange={(e) => { setValidityFilter(e.target.value as 'all' | 'valid' | 'invalid'); setPage(1); }}
              >
                <option value="all">{t('common.all')}</option>
                <option value="valid">{t('certificates.validOnly')}</option>
                <option value="invalid">{t('certificates.invalidOnly')}</option>
              </Select>
            </div>
            <Button variant="ghost" onClick={() => { setTypeFilter(''); setValidityFilter('all'); setPage(1); }}>
              {t('common.clear')}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Content */}
      <Card variant="default">
        {loading && (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Loading text={t('certificates.loading')} />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text>{error}</Text>
            <Button variant="ghost" onClick={fetchData} style={{ marginTop: spacing.md }}>{t('errors.retry')}</Button>
          </div>
        )}

        {!loading && !error && filteredCertificates.length === 0 && (
          <EmptyState
            icon="💉"
            title={t('certificates.noCertificates')}
            description={t('certificates.noCertificatesHint')}
          />
        )}

        {!loading && !error && filteredCertificates.length > 0 && (
          <>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('certificates.certificateNumber')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('certificates.patient')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('common.type')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('certificates.vaccine')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('certificates.administeredDate')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('certificates.expires')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('certificates.nextDueDate')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('common.status')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('common.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedCertificates.map(cert => {
                    const status = getStatusBadge(cert);
                    return (
                      <tr key={cert.id} style={{ borderBottom: `1px solid ${colors.neutral.borderLight}` }}>
                        <td style={{ padding: spacing.md, fontFamily: 'monospace', fontSize: '12px' }}>
                          {cert.certificateNumber}
                        </td>
                        <td style={{ padding: spacing.md }}>{getPatientName(cert.patientId)}</td>
                        <td style={{ padding: spacing.md }}>
                          <Badge variant="secondary">{t(`certificates.types.${cert.certificateType}`)}</Badge>
                        </td>
                        <td style={{ padding: spacing.md }}>{cert.vaccineName}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(cert.administrationDate, language)}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(cert.expirationDate, language)}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(cert.nextDueDate, language)}</td>
                        <td style={{ padding: spacing.md }}>
                          <Badge variant={status.variant}>{status.text}</Badge>
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: spacing.xs, justifyContent: 'flex-end' }}>
                            {cert.isValid && (
                              <Button size="sm" variant="secondary" onClick={() => openInvalidateDialog(cert)} disabled={actionLoading}>
                                {t('certificates.invalidate')}
                              </Button>
                            )}
                            <Button size="sm" variant="danger" onClick={() => openDeleteDialog(cert)} disabled={actionLoading}>
                              {t('common.delete')}
                            </Button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            {filteredCertificates.length > ITEMS_PER_PAGE && (
              <div style={{ padding: spacing.md, display: 'flex', justifyContent: 'center' }}>
                <Pagination
                  currentPage={page - 1}
                  totalItems={filteredCertificates.length}
                  pageSize={ITEMS_PER_PAGE}
                  onPageChange={(p) => setPage(p + 1)}
                />
              </div>
            )}
          </>
        )}
      </Card>

      {/* Create Certificate Modal */}
      <Modal open={showCreateModal} onClose={() => setShowCreateModal(false)}>
        <ModalTitle>{t('certificates.newCertificate')}</ModalTitle>
        <FormField label={t('certificates.patient')} required>
          <Select
            value={formData.patientId || ''}
            onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
          >
            <option value="">{t('certificates.selectPatient')}</option>
            {patients.map(p => (
              <option key={p.id} value={p.id}>{p.name} ({p.species})</option>
            ))}
          </Select>
        </FormField>
        <FormField label={t('common.type')} required>
          <Select
            value={formData.certificateType || 'RABIES'}
            onChange={(e) => setFormData({ ...formData, certificateType: e.target.value as CertificateType })}
          >
            {CERTIFICATE_TYPES.map(type => (
              <option key={type} value={type}>{t(`certificates.types.${type}`)}</option>
            ))}
          </Select>
        </FormField>
        <FormField label={t('certificates.vaccineName')} required>
          <Input
            value={formData.vaccineName || ''}
            onChange={(e) => setFormData({ ...formData, vaccineName: e.target.value })}
            placeholder="e.g., Nobivac Rabies"
          />
        </FormField>
        <FormField label={t('certificates.manufacturer')}>
          <Input
            value={formData.vaccineManufacturer || ''}
            onChange={(e) => setFormData({ ...formData, vaccineManufacturer: e.target.value })}
            placeholder="e.g., MSD Animal Health"
          />
        </FormField>
        <FormField label={t('certificates.batchNumber')}>
          <Input
            value={formData.batchNumber || ''}
            onChange={(e) => setFormData({ ...formData, batchNumber: e.target.value })}
          />
        </FormField>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
          <FormField label={t('certificates.administeredDate')} required>
            <Input
              type="date"
              value={formData.administrationDate || ''}
              onChange={(e) => setFormData({ ...formData, administrationDate: e.target.value })}
            />
          </FormField>
          <FormField label={t('certificates.expirationDate')}>
            <Input
              type="date"
              value={formData.expirationDate || ''}
              onChange={(e) => setFormData({ ...formData, expirationDate: e.target.value })}
            />
          </FormField>
        </div>
        <FormField label={t('certificates.nextDueDate')}>
          <Input
            type="date"
            value={formData.nextDueDate || ''}
            onChange={(e) => setFormData({ ...formData, nextDueDate: e.target.value })}
          />
        </FormField>
        <FormField label={t('visits.veterinarian')} required>
          <Select
            value={formData.veterinarianId || ''}
            onChange={(e) => setFormData({ ...formData, veterinarianId: e.target.value })}
          >
            <option value="">{t('certificates.selectVeterinarian')}</option>
            {veterinarians.map(v => (
              <option key={v.id} value={v.id}>Dr. {v.fullName}</option>
            ))}
          </Select>
        </FormField>
        <FormField label={t('common.notes')}>
          <TextArea
            value={formData.notes || ''}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            rows={2}
          />
        </FormField>
        <ModalActions>
          <Button variant="ghost" onClick={() => setShowCreateModal(false)}>{t('common.cancel')}</Button>
          <Button variant="primary" onClick={handleCreate} disabled={actionLoading}>
            {actionLoading ? t('common.creating') : t('certificates.createCertificate')}
          </Button>
        </ModalActions>
      </Modal>

      {/* Confirm Dialog */}
      <ConfirmDialog
        open={confirmDialog.open}
        onClose={closeConfirmDialog}
        onConfirm={handleConfirmAction}
        title={confirmDialog.type === 'invalidate' ? t('certificates.invalidate') : t('common.delete')}
        message={
          confirmDialog.type === 'invalidate'
            ? t('certificates.confirmInvalidate')
            : t('certificates.confirmDelete')
        }
        confirmLabel={confirmDialog.type === 'invalidate' ? t('certificates.invalidate') : t('common.delete')}
        variant="danger"
        inputLabel={confirmDialog.type === 'invalidate' ? t('certificates.invalidationReason') : undefined}
        inputPlaceholder={confirmDialog.type === 'invalidate' ? t('certificates.invalidationPlaceholder') : undefined}
        inputRequired={confirmDialog.type === 'invalidate'}
        loading={actionLoading}
      />
    </div>
  );
}
