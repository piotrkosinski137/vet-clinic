import { useState, useEffect, useMemo } from 'react';
import { api } from '../api';
import type { VaccinationCertificateResponse, VaccinationCertificateRequest, CertificateType, PatientResponse, VeterinarianResponse } from '../api/types';
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
} from '../components/ui';
import { colors, spacing } from '../theme';
import type { BadgeVariant } from '../components/ui/Badge';

const CERTIFICATE_TYPES: CertificateType[] = ['RABIES', 'DISTEMPER', 'PARVOVIRUS', 'HEPATITIS', 'LEPTOSPIROSIS', 'BORDETELLA', 'FELINE_LEUKEMIA', 'FELINE_CALICIVIRUS', 'OTHER'];
const ITEMS_PER_PAGE = 15;

const typeLabels: Record<CertificateType, string> = {
  RABIES: 'Rabies',
  DISTEMPER: 'Distemper',
  PARVOVIRUS: 'Parvovirus',
  HEPATITIS: 'Hepatitis',
  LEPTOSPIROSIS: 'Leptospirosis',
  BORDETELLA: 'Bordetella',
  FELINE_LEUKEMIA: 'Feline Leukemia',
  FELINE_CALICIVIRUS: 'Feline Calicivirus',
  OTHER: 'Other',
};

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('pl-PL');
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
  const [certificates, setCertificates] = useState<VaccinationCertificateResponse[]>([]);
  const [patients, setPatients] = useState<PatientResponse[]>([]);
  const [veterinarians, setVeterinarians] = useState<VeterinarianResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [typeFilter, setTypeFilter] = useState<CertificateType | ''>('');
  const [validityFilter, setValidityFilter] = useState<'all' | 'valid' | 'invalid'>('all');
  const [page, setPage] = useState(1);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
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
      const [certsData, patientsData, vetsData] = await Promise.all([
        api.getCertificates(),
        api.getPatients(),
        api.getVeterinarians({ active: true }),
      ]);
      setCertificates(certsData);
      setPatients(patientsData);
      setVeterinarians(vetsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data');
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
    if (!formData.patientId || !formData.vaccineName || !formData.administrationDate) {
      showError('Please fill all required fields');
      return;
    }
    setActionLoading(true);
    try {
      await api.createCertificate(formData as VaccinationCertificateRequest);
      success('Certificate created');
      setShowCreateModal(false);
      setFormData({
        patientId: '',
        certificateType: 'RABIES',
        vaccineName: '',
        administrationDate: new Date().toISOString().split('T')[0],
      });
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to create certificate');
    } finally {
      setActionLoading(false);
    }
  };

  const handleInvalidate = async (cert: VaccinationCertificateResponse) => {
    const reason = window.prompt('Reason for invalidation:');
    if (!reason) return;
    setActionLoading(true);
    try {
      await api.invalidateCertificate(cert.id, reason);
      success('Certificate invalidated');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to invalidate certificate');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (cert: VaccinationCertificateResponse) => {
    if (!window.confirm('Are you sure you want to delete this certificate?')) return;
    setActionLoading(true);
    try {
      await api.deleteCertificate(cert.id);
      success('Certificate deleted');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to delete certificate');
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusBadge = (cert: VaccinationCertificateResponse): { variant: BadgeVariant; text: string } => {
    if (!cert.isValid) return { variant: 'danger', text: 'Invalid' };
    if (isExpired(cert.expirationDate)) return { variant: 'danger', text: 'Expired' };
    if (isExpiringSoon(cert.expirationDate)) return { variant: 'warning', text: 'Expiring Soon' };
    return { variant: 'success', text: 'Valid' };
  };

  return (
    <div>
      <PageHeader
        title="Vaccination Certificates"
        actions={
          <Button variant="primary" onClick={() => setShowCreateModal(true)}>
            + New Certificate
          </Button>
        }
      />

      {/* Filters */}
      <Card variant="default" style={{ marginBottom: spacing.lg }}>
        <CardContent>
          <div style={{ display: 'flex', gap: spacing.md, alignItems: 'flex-end', flexWrap: 'wrap' }}>
            <div style={{ minWidth: '180px' }}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>Type</label>
              <Select
                value={typeFilter}
                onChange={(e) => { setTypeFilter(e.target.value as CertificateType | ''); setPage(1); }}
              >
                <option value="">All Types</option>
                {CERTIFICATE_TYPES.map(t => (
                  <option key={t} value={t}>{typeLabels[t]}</option>
                ))}
              </Select>
            </div>
            <div style={{ minWidth: '150px' }}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>Validity</label>
              <Select
                value={validityFilter}
                onChange={(e) => { setValidityFilter(e.target.value as 'all' | 'valid' | 'invalid'); setPage(1); }}
              >
                <option value="all">All</option>
                <option value="valid">Valid Only</option>
                <option value="invalid">Invalid Only</option>
              </Select>
            </div>
            <Button variant="ghost" onClick={() => { setTypeFilter(''); setValidityFilter('all'); setPage(1); }}>
              Clear
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Content */}
      <Card variant="default">
        {loading && (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Loading text="Loading certificates..." />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text>{error}</Text>
            <Button variant="ghost" onClick={fetchData} style={{ marginTop: spacing.md }}>Retry</Button>
          </div>
        )}

        {!loading && !error && filteredCertificates.length === 0 && (
          <EmptyState
            icon="💉"
            title="No certificates found"
            description="Create a vaccination certificate to get started."
          />
        )}

        {!loading && !error && filteredCertificates.length > 0 && (
          <>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Certificate #</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Patient</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Type</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Vaccine</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Administered</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Expires</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Next Due</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Status</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>Actions</th>
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
                          <Badge variant="secondary">{typeLabels[cert.certificateType]}</Badge>
                        </td>
                        <td style={{ padding: spacing.md }}>{cert.vaccineName}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(cert.administrationDate)}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(cert.expirationDate)}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(cert.nextDueDate)}</td>
                        <td style={{ padding: spacing.md }}>
                          <Badge variant={status.variant}>{status.text}</Badge>
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: spacing.xs, justifyContent: 'flex-end' }}>
                            {cert.isValid && (
                              <Button size="sm" variant="secondary" onClick={() => handleInvalidate(cert)} disabled={actionLoading}>
                                Invalidate
                              </Button>
                            )}
                            <Button size="sm" variant="danger" onClick={() => handleDelete(cert)} disabled={actionLoading}>
                              Delete
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
        <ModalTitle>New Vaccination Certificate</ModalTitle>
        <FormField label="Patient" required>
          <Select
            value={formData.patientId || ''}
            onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
          >
            <option value="">Select patient...</option>
            {patients.map(p => (
              <option key={p.id} value={p.id}>{p.name} ({p.species})</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Certificate Type" required>
          <Select
            value={formData.certificateType || 'RABIES'}
            onChange={(e) => setFormData({ ...formData, certificateType: e.target.value as CertificateType })}
          >
            {CERTIFICATE_TYPES.map(t => (
              <option key={t} value={t}>{typeLabels[t]}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Vaccine Name" required>
          <Input
            value={formData.vaccineName || ''}
            onChange={(e) => setFormData({ ...formData, vaccineName: e.target.value })}
            placeholder="e.g., Nobivac Rabies"
          />
        </FormField>
        <FormField label="Manufacturer">
          <Input
            value={formData.manufacturer || ''}
            onChange={(e) => setFormData({ ...formData, manufacturer: e.target.value })}
            placeholder="e.g., MSD Animal Health"
          />
        </FormField>
        <FormField label="Batch Number">
          <Input
            value={formData.batchNumber || ''}
            onChange={(e) => setFormData({ ...formData, batchNumber: e.target.value })}
          />
        </FormField>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
          <FormField label="Administration Date" required>
            <Input
              type="date"
              value={formData.administrationDate || ''}
              onChange={(e) => setFormData({ ...formData, administrationDate: e.target.value })}
            />
          </FormField>
          <FormField label="Expiration Date">
            <Input
              type="date"
              value={formData.expirationDate || ''}
              onChange={(e) => setFormData({ ...formData, expirationDate: e.target.value })}
            />
          </FormField>
        </div>
        <FormField label="Next Due Date">
          <Input
            type="date"
            value={formData.nextDueDate || ''}
            onChange={(e) => setFormData({ ...formData, nextDueDate: e.target.value })}
          />
        </FormField>
        <FormField label="Veterinarian">
          <Select
            value={formData.veterinarianId || ''}
            onChange={(e) => {
              const vet = veterinarians.find(v => v.id === e.target.value);
              setFormData({
                ...formData,
                veterinarianId: e.target.value,
                veterinarianName: vet ? vet.fullName : '',
              });
            }}
          >
            <option value="">Select veterinarian...</option>
            {veterinarians.map(v => (
              <option key={v.id} value={v.id}>Dr. {v.fullName}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Notes">
          <TextArea
            value={formData.notes || ''}
            onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
            rows={2}
          />
        </FormField>
        <ModalActions>
          <Button variant="ghost" onClick={() => setShowCreateModal(false)}>Cancel</Button>
          <Button variant="primary" onClick={handleCreate} disabled={actionLoading}>
            {actionLoading ? 'Creating...' : 'Create Certificate'}
          </Button>
        </ModalActions>
      </Modal>
    </div>
  );
}
