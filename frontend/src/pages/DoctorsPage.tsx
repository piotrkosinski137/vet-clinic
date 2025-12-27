import { useState, useEffect, useMemo } from 'react';
import { useI18n } from '../i18n';
import {
  VeterinarianResponse,
  DoctorInvitationRequest,
  api,
} from '../api';
import {
  Button,
  Card,
  Modal,
  ModalTitle,
  ModalActions,
  Badge,
  FormField,
  Input,
  TextArea,
  Text,
  Loading,
  PageHeader,
  useToast,
} from '../components/ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../theme';

const ITEMS_PER_PAGE = 6;

export function DoctorsPage() {
  const { t } = useI18n();
  const { success, error: showError } = useToast();

  // Data state
  const [doctors, setDoctors] = useState<VeterinarianResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [showActiveOnly, setShowActiveOnly] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);

  // Modal state
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState<VeterinarianResponse | null>(null);
  const [inviting, setInviting] = useState(false);

  // Form data
  const [inviteForm, setInviteForm] = useState<DoctorInvitationRequest>({
    email: '',
    firstName: '',
    lastName: '',
    phone: '',
    specialization: '',
    licenseNumber: '',
    notes: '',
  });

  // Load doctors
  const loadDoctors = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getVeterinarians();
      setDoctors(data);
    } catch {
      setError(t('errors.failedToLoad'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDoctors();
  }, []);

  // Filter doctors
  const filteredDoctors = useMemo(() => {
    let result = doctors;

    // Filter by active status
    if (showActiveOnly) {
      result = result.filter((d) => d.active);
    }

    // Filter by search query
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      result = result.filter(
        (d) =>
          d.firstName.toLowerCase().includes(query) ||
          d.lastName.toLowerCase().includes(query) ||
          d.email.toLowerCase().includes(query) ||
          d.specialization?.toLowerCase().includes(query)
      );
    }

    return result;
  }, [doctors, searchQuery, showActiveOnly]);

  // Pagination
  const totalPages = Math.ceil(filteredDoctors.length / ITEMS_PER_PAGE);
  const paginatedDoctors = filteredDoctors.slice(
    currentPage * ITEMS_PER_PAGE,
    (currentPage + 1) * ITEMS_PER_PAGE
  );

  // Reset page when filters change
  useEffect(() => {
    setCurrentPage(0);
  }, [searchQuery, showActiveOnly]);

  // Handlers
  const openInviteModal = () => {
    setInviteForm({
      email: '',
      firstName: '',
      lastName: '',
      phone: '',
      specialization: '',
      licenseNumber: '',
      notes: '',
    });
    setShowInviteModal(true);
  };

  const handleInviteSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setInviting(true);
    try {
      const newDoctor = await api.inviteDoctor(inviteForm);
      setDoctors((prev) => [...prev, newDoctor]);
      setShowInviteModal(false);
      success(t('doctors.invitationSent'));
    } catch (err: unknown) {
      const apiError = err as { detail?: string };
      if (apiError.detail?.includes('already exists')) {
        showError(t('doctors.emailAlreadyExists'));
      } else {
        showError(t('doctors.invitationFailed'));
      }
    } finally {
      setInviting(false);
    }
  };

  const handleViewDetails = (doctor: VeterinarianResponse) => {
    setSelectedDoctor(doctor);
    setShowDetailsModal(true);
  };

  const handleToggleActive = async (doctor: VeterinarianResponse) => {
    const newActive = !doctor.active;
    if (!newActive && !window.confirm(t('doctors.confirmDeactivate'))) {
      return;
    }
    try {
      const updated = await api.toggleVeterinarianActive(doctor.id, newActive);
      setDoctors((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
      if (selectedDoctor?.id === doctor.id) {
        setSelectedDoctor(updated);
      }
      success(newActive ? t('doctors.doctorActivated') : t('doctors.doctorDeactivated'));
    } catch {
      showError(t('errors.failedToSave'));
    }
  };

  if (loading) return <Loading text={t('common.loading')} />;
  if (error) {
    return (
      <Card style={{ padding: spacing.xl, textAlign: 'center' }}>
        <Text variant="muted">{error}</Text>
        <Button onClick={loadDoctors} style={{ marginTop: spacing.md }}>
          {t('errors.retry')}
        </Button>
      </Card>
    );
  }

  return (
    <div>
      <PageHeader
        title={t('doctors.title')}
        actions={
          <Button variant="primary" onClick={openInviteModal}>
            + {t('doctors.inviteDoctor')}
          </Button>
        }
      />

      {/* Filters */}
      <Card style={{ padding: spacing.md, marginBottom: spacing.md }}>
        <div style={{ display: 'flex', gap: spacing.md, alignItems: 'center', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '200px' }}>
            <Input
              type="text"
              placeholder={t('doctors.searchDoctors')}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ width: '100%' }}
            />
          </div>
          <div style={{ display: 'flex', gap: spacing.xs }}>
            <Button
              variant={showActiveOnly ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => setShowActiveOnly(true)}
            >
              {t('doctors.showActive')}
            </Button>
            <Button
              variant={!showActiveOnly ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => setShowActiveOnly(false)}
            >
              {t('doctors.showAll')}
            </Button>
          </div>
        </div>
      </Card>

      {/* Doctors Grid */}
      {filteredDoctors.length === 0 ? (
        <Card style={{ padding: spacing.xl, textAlign: 'center' }}>
          <div style={{ fontSize: '48px', marginBottom: spacing.md }}>👨‍⚕️</div>
          <Text variant="muted">{t('doctors.noDoctorsYet')}</Text>
          <Button variant="primary" onClick={openInviteModal} style={{ marginTop: spacing.md }}>
            {t('doctors.inviteDoctor')}
          </Button>
        </Card>
      ) : (
        <>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))',
              gap: spacing.md,
            }}
          >
            {paginatedDoctors.map((doctor) => (
              <Card
                key={doctor.id}
                style={{
                  padding: spacing.lg,
                  cursor: 'pointer',
                  transition: 'box-shadow 0.2s, transform 0.2s',
                  opacity: doctor.active ? 1 : 0.6,
                }}
                onClick={() => handleViewDetails(doctor)}
                onMouseEnter={(e) => {
                  e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.1)';
                  e.currentTarget.style.transform = 'translateY(-2px)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.boxShadow = 'none';
                  e.currentTarget.style.transform = 'translateY(0)';
                }}
              >
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: spacing.md }}>
                  {/* Avatar with color */}
                  <div
                    style={{
                      width: '56px',
                      height: '56px',
                      borderRadius: borderRadius.full,
                      backgroundColor: doctor.colorCode || colors.primary.main,
                      color: colors.neutral.white,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: fontWeight.bold,
                      fontSize: fontSize.lg,
                      flexShrink: 0,
                    }}
                  >
                    {doctor.firstName[0]}
                    {doctor.lastName[0]}
                  </div>

                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                      <Text
                        style={{
                          fontWeight: fontWeight.bold,
                          fontSize: fontSize.lg,
                        }}
                      >
                        Dr. {doctor.firstName} {doctor.lastName}
                      </Text>
                      <Badge variant={doctor.active ? 'success' : 'secondary'}>
                        {doctor.active ? t('doctors.active') : t('doctors.inactive')}
                      </Badge>
                    </div>

                    <Text
                      variant="muted"
                      size="sm"
                      style={{
                        display: 'block',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {doctor.specialization || t('doctors.generalPractice')}
                    </Text>

                    <Text
                      variant="muted"
                      size="sm"
                      style={{
                        display: 'block',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        marginTop: spacing.xs,
                      }}
                    >
                      {doctor.email}
                    </Text>

                    {doctor.phone && (
                      <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
                        {doctor.phone}
                      </Text>
                    )}
                  </div>
                </div>

                {/* Action buttons */}
                <div
                  style={{
                    display: 'flex',
                    gap: spacing.xs,
                    marginTop: spacing.md,
                    paddingTop: spacing.md,
                    borderTop: `1px solid ${colors.neutral.border}`,
                  }}
                >
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleViewDetails(doctor);
                    }}
                  >
                    {t('doctors.viewDetails')}
                  </Button>
                  <Button
                    variant={doctor.active ? 'danger' : 'primary'}
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleToggleActive(doctor);
                    }}
                  >
                    {doctor.active ? t('doctors.deactivate') : t('doctors.activate')}
                  </Button>
                </div>
              </Card>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div
              style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                gap: spacing.md,
                marginTop: spacing.lg,
              }}
            >
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                disabled={currentPage === 0}
              >
                ← Previous
              </Button>
              <Text size="sm" style={{ color: colors.neutral.textMuted }}>
                {currentPage + 1} / {totalPages}
              </Text>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={currentPage >= totalPages - 1}
              >
                Next →
              </Button>
            </div>
          )}
        </>
      )}

      {/* Invite Doctor Modal */}
      <Modal open={showInviteModal} onClose={() => setShowInviteModal(false)}>
        <ModalTitle>{t('doctors.inviteNewDoctor')}</ModalTitle>
        <form onSubmit={handleInviteSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <FormField label={t('doctors.firstName')} required>
              <Input
                value={inviteForm.firstName}
                onChange={(e) => setInviteForm({ ...inviteForm, firstName: e.target.value })}
                required
              />
            </FormField>
            <FormField label={t('doctors.lastName')} required>
              <Input
                value={inviteForm.lastName}
                onChange={(e) => setInviteForm({ ...inviteForm, lastName: e.target.value })}
                required
              />
            </FormField>
          </div>

          <FormField label={t('doctors.email')} required>
            <Input
              type="email"
              value={inviteForm.email}
              onChange={(e) => setInviteForm({ ...inviteForm, email: e.target.value })}
              required
            />
          </FormField>

          <FormField label={t('doctors.phone')}>
            <Input
              value={inviteForm.phone || ''}
              onChange={(e) => setInviteForm({ ...inviteForm, phone: e.target.value })}
            />
          </FormField>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <FormField label={t('doctors.specialization')}>
              <Input
                value={inviteForm.specialization || ''}
                onChange={(e) => setInviteForm({ ...inviteForm, specialization: e.target.value })}
                placeholder={t('doctors.generalPractice')}
              />
            </FormField>
            <FormField label={t('doctors.licenseNumber')}>
              <Input
                value={inviteForm.licenseNumber || ''}
                onChange={(e) => setInviteForm({ ...inviteForm, licenseNumber: e.target.value })}
              />
            </FormField>
          </div>

          <FormField label={t('doctors.notes')}>
            <TextArea
              value={inviteForm.notes || ''}
              onChange={(e) => setInviteForm({ ...inviteForm, notes: e.target.value })}
              rows={2}
            />
          </FormField>

          <ModalActions>
            <Button type="button" variant="ghost" onClick={() => setShowInviteModal(false)}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" variant="primary" disabled={inviting}>
              {inviting ? t('doctors.inviting') : t('doctors.inviteDoctor')}
            </Button>
          </ModalActions>
        </form>
      </Modal>

      {/* Doctor Details Modal */}
      <Modal open={showDetailsModal} onClose={() => setShowDetailsModal(false)}>
        {selectedDoctor && (
          <>
            <ModalTitle>{t('doctors.doctorDetails')}</ModalTitle>
            <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
              {/* Header */}
              <div style={{ display: 'flex', alignItems: 'center', gap: spacing.md }}>
                <div
                  style={{
                    width: '72px',
                    height: '72px',
                    borderRadius: borderRadius.full,
                    backgroundColor: selectedDoctor.colorCode || colors.primary.main,
                    color: colors.neutral.white,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: fontWeight.bold,
                    fontSize: fontSize.xl,
                  }}
                >
                  {selectedDoctor.firstName[0]}
                  {selectedDoctor.lastName[0]}
                </div>
                <div>
                  <Text style={{ fontWeight: fontWeight.bold, fontSize: fontSize.xl }}>
                    Dr. {selectedDoctor.firstName} {selectedDoctor.lastName}
                  </Text>
                  <Badge variant={selectedDoctor.active ? 'success' : 'secondary'}>
                    {selectedDoctor.active ? t('doctors.active') : t('doctors.inactive')}
                  </Badge>
                </div>
              </div>

              {/* Details */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: spacing.md,
                }}
              >
                <div
                  style={{
                    backgroundColor: colors.neutral.background,
                    padding: spacing.sm,
                    borderRadius: borderRadius.sm,
                  }}
                >
                  <Text variant="muted" size="sm">
                    {t('doctors.email')}
                  </Text>
                  <Text>{selectedDoctor.email}</Text>
                </div>

                <div
                  style={{
                    backgroundColor: colors.neutral.background,
                    padding: spacing.sm,
                    borderRadius: borderRadius.sm,
                  }}
                >
                  <Text variant="muted" size="sm">
                    {t('doctors.phone')}
                  </Text>
                  <Text>{selectedDoctor.phone || '-'}</Text>
                </div>

                <div
                  style={{
                    backgroundColor: colors.neutral.background,
                    padding: spacing.sm,
                    borderRadius: borderRadius.sm,
                  }}
                >
                  <Text variant="muted" size="sm">
                    {t('doctors.specialization')}
                  </Text>
                  <Text>{selectedDoctor.specialization || t('doctors.generalPractice')}</Text>
                </div>

                <div
                  style={{
                    backgroundColor: colors.neutral.background,
                    padding: spacing.sm,
                    borderRadius: borderRadius.sm,
                  }}
                >
                  <Text variant="muted" size="sm">
                    {t('doctors.licenseNumber')}
                  </Text>
                  <Text>{selectedDoctor.licenseNumber || '-'}</Text>
                </div>
              </div>

              {selectedDoctor.notes && (
                <div
                  style={{
                    backgroundColor: colors.neutral.background,
                    padding: spacing.sm,
                    borderRadius: borderRadius.sm,
                  }}
                >
                  <Text variant="muted" size="sm">
                    {t('doctors.notes')}
                  </Text>
                  <Text>{selectedDoctor.notes}</Text>
                </div>
              )}

              {selectedDoctor.colorCode && (
                <div
                  style={{
                    backgroundColor: colors.neutral.background,
                    padding: spacing.sm,
                    borderRadius: borderRadius.sm,
                    display: 'flex',
                    alignItems: 'center',
                    gap: spacing.sm,
                  }}
                >
                  <Text variant="muted" size="sm">
                    {t('doctors.colorCode')}
                  </Text>
                  <div
                    style={{
                      width: '24px',
                      height: '24px',
                      borderRadius: borderRadius.sm,
                      backgroundColor: selectedDoctor.colorCode,
                      border: `1px solid ${colors.neutral.border}`,
                    }}
                  />
                  <Text>{selectedDoctor.colorCode}</Text>
                </div>
              )}
            </div>

            <ModalActions>
              <Button variant="ghost" onClick={() => setShowDetailsModal(false)}>
                {t('common.close')}
              </Button>
              <Button
                variant={selectedDoctor.active ? 'danger' : 'primary'}
                onClick={() => handleToggleActive(selectedDoctor)}
              >
                {selectedDoctor.active ? t('doctors.deactivate') : t('doctors.activate')}
              </Button>
            </ModalActions>
          </>
        )}
      </Modal>
    </div>
  );
}
