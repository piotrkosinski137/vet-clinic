import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useClients, useConfirmDialog } from '../hooks';
import {
  ClientRequest,
  ClientResponse,
  PatientRequest,
  PatientResponse,
  VisitResponse,
  GdprConsentResponse,
  PatientLabel,
  api,
} from '../api';

// Debounce hook for search
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);
  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);
  return debouncedValue;
}
import {
  Button,
  Card,
  Modal,
  ModalTitle,
  ModalActions,
  Badge,
  FormField,
  Input,
  Select,
  TextArea,
  Text,
  Loading,
  PageHeader,
  useToast,
  ConfirmDialog,
} from '../components/ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../theme';
import { useI18n } from '../i18n';
import {
  SPECIES_OPTIONS,
  PATIENT_LABELS,
  getSpeciesInfo,
  getLabelInfo,
  VISIT_STATUS_CONFIG,
  PAGINATION,
  UI,
} from '../constants';
import { formatDateTime, formatDate, formatTime, isSpecies } from '../utils';

export function ClientsPage() {
  const navigate = useNavigate();
  const { t } = useI18n();
  const { clients, loading, error, createClient, updateClient, deleteClient, refresh } = useClients();
  const { success, error: showError } = useToast();
  const { dialogState, showConfirm, closeDialog, handleConfirm } = useConfirmDialog();

  // Search and selection state
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedClient, setSelectedClient] = useState<ClientResponse | null>(null);
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null);
  const [selectedVisit, setSelectedVisit] = useState<VisitResponse | null>(null);

  // Debounced search for backend queries
  const debouncedSearch = useDebounce(searchQuery, 300);

  // Data state - all patients for search
  const [allPatients, setAllPatients] = useState<PatientResponse[]>([]);
  const [allConsents, setAllConsents] = useState<GdprConsentResponse[]>([]);
  const [patients, setPatients] = useState<PatientResponse[]>([]);
  const [visits, setVisits] = useState<VisitResponse[]>([]);
  const [filteredClients, setFilteredClients] = useState<ClientResponse[]>([]);
  const [loadingAllPatients, setLoadingAllPatients] = useState(false);
  const [loadingPatients, setLoadingPatients] = useState(false);
  const [loadingVisits, setLoadingVisits] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);

  // Pagination
  const [clientsPage, setClientsPage] = useState(0);
  const [visitsPage, setVisitsPage] = useState(0);

  // Modal state
  const [showClientForm, setShowClientForm] = useState(false);
  const [showPatientForm, setShowPatientForm] = useState(false);
  const [editingClient, setEditingClient] = useState<ClientResponse | null>(null);
  const [editingPatient, setEditingPatient] = useState<PatientResponse | null>(null);
  const [isSubmittingClient, setIsSubmittingClient] = useState(false);
  const [isSubmittingPatient, setIsSubmittingPatient] = useState(false);

  // Form data
  const [clientFormData, setClientFormData] = useState<ClientRequest>({
    firstName: '', lastName: '', email: '', phone: '', address: '', city: '', postalCode: '',
  });
  const [patientFormData, setPatientFormData] = useState<PatientRequest>({
    name: '', species: 'DOG', breed: '', notes: '', labels: [],
  });

  // Load all patients and consents for search functionality
  useEffect(() => {
    setLoadingAllPatients(true);
    Promise.all([
      api.getPatients(),
      api.getConsents(),
    ])
      .then(([patientsData, consentsData]) => {
        setAllPatients(patientsData);
        setAllConsents(consentsData);
      })
      .catch(() => { /* Silently fail - data will be empty */ })
      .finally(() => setLoadingAllPatients(false));
  }, []);

  // Helper to check if client has RODO consent
  const getClientConsentStatus = useCallback(
    (clientId: string): 'granted' | 'pending' | 'none' => {
      const clientConsents = allConsents.filter(c => c.clientId === clientId && c.consentType === 'DATA_PROCESSING');
      if (clientConsents.length === 0) return 'none';
      const hasGranted = clientConsents.some(c => c.status === 'GRANTED');
      if (hasGranted) return 'granted';
      return 'pending';
    },
    [allConsents]
  );

  // Helper to get pets for a client - memoized
  const getPetsForClient = useCallback(
    (clientId: string): PatientResponse[] => {
      return allPatients.filter((p) => p.ownerId === clientId);
    },
    [allPatients]
  );

  // Backend search for clients with debounce
  useEffect(() => {
    const searchClients = async () => {
      setSearchLoading(true);
      try {
        // Use backend search with 'q' parameter for diacritic-insensitive search
        const results = await api.getClients(debouncedSearch || undefined);
        setFilteredClients(results);
      } catch {
        // Fall back to all clients from hook if search fails
        setFilteredClients(clients);
      } finally {
        setSearchLoading(false);
      }
    };

    searchClients();
  }, [debouncedSearch, clients]);

  // Reset clients page when search changes
  useEffect(() => {
    setClientsPage(0);
  }, [debouncedSearch]);

  // Load patients when client selected
  useEffect(() => {
    if (selectedClient) {
      setLoadingPatients(true);
      setSelectedPatient(null);
      setVisits([]);
      api.getPatients(selectedClient.id)
        .then(setPatients)
        .catch(() => showError(t('clients.failedToLoadPatients')))
        .finally(() => setLoadingPatients(false));
    } else {
      setPatients([]);
    }
  }, [selectedClient, t]);

  // Load visits when patient selected
  useEffect(() => {
    if (selectedPatient) {
      setLoadingVisits(true);
      setVisitsPage(0); // Reset pagination
      api.getPatientVisits(selectedPatient.id)
        .then((data) => {
          // Sort by date descending (newest first)
          const sorted = [...data].sort((a, b) =>
            new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime()
          );
          setVisits(sorted);
        })
        .catch(() => showError(t('clients.failedToLoadVisits')))
        .finally(() => setLoadingVisits(false));
    } else {
      setVisits([]);
    }
  }, [selectedPatient, t]);

  // Client form handlers
  const openCreateClient = () => {
    setEditingClient(null);
    setClientFormData({ firstName: '', lastName: '', email: '', phone: '', address: '', city: '', postalCode: '' });
    setShowClientForm(true);
  };

  const openEditClient = (client: ClientResponse) => {
    setEditingClient(client);
    setClientFormData({
      firstName: client.firstName, lastName: client.lastName, email: client.email,
      phone: client.phone || '', address: client.address || '', city: client.city || '', postalCode: client.postalCode || '',
    });
    setShowClientForm(true);
  };

  const handleClientSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmittingClient) return;

    setIsSubmittingClient(true);
    try {
      if (editingClient) {
        const updated = await updateClient(editingClient.id, clientFormData);
        if (selectedClient?.id === editingClient.id) setSelectedClient(updated);
        success(t('clients.clientUpdated'));
      } else {
        await createClient(clientFormData);
        success(t('clients.clientCreated'));
      }
      setShowClientForm(false);
    } catch {
      showError(t('clients.failedToSaveClient'));
    } finally {
      setIsSubmittingClient(false);
    }
  };

  const handleDeleteClient = (client: ClientResponse) => {
    showConfirm(
      t('common.confirm'),
      t('clients.confirmDeleteClient', { firstName: client.firstName, lastName: client.lastName }),
      async () => {
        try {
          await deleteClient(client.id);
          if (selectedClient?.id === client.id) {
            setSelectedClient(null);
          }
          success(t('clients.clientDeleted'));
        } catch {
          showError(t('clients.failedToDeleteClient'));
        }
      }
    );
  };

  // Patient form handlers
  const openCreatePatient = () => {
    setEditingPatient(null);
    setPatientFormData({ name: '', species: 'DOG', breed: '', notes: '', labels: [], ownerId: selectedClient?.id });
    setShowPatientForm(true);
  };

  const openEditPatient = (patient: PatientResponse) => {
    setEditingPatient(patient);
    setPatientFormData({
      name: patient.name, species: patient.species, breed: patient.breed || '',
      notes: patient.notes || '', labels: patient.labels || [], ownerId: selectedClient?.id,
    });
    setShowPatientForm(true);
  };

  const handlePatientSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmittingPatient) return;

    setIsSubmittingPatient(true);
    try {
      if (editingPatient) {
        const updated = await api.updatePatient(editingPatient.id, patientFormData);
        setPatients((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
        setAllPatients((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
        if (selectedPatient?.id === editingPatient.id) setSelectedPatient(updated);
        success(t('clients.patientUpdated'));
      } else {
        const created = await api.createPatient({ ...patientFormData, ownerId: selectedClient?.id });
        setPatients((prev) => [...prev, created]);
        setAllPatients((prev) => [...prev, created]);
        success(t('clients.patientAdded'));
      }
      setShowPatientForm(false);
    } catch {
      showError(t('clients.failedToSavePatient'));
    } finally {
      setIsSubmittingPatient(false);
    }
  };

  const handleDeletePatient = (patient: PatientResponse) => {
    showConfirm(
      t('common.confirm'),
      t('clients.confirmDeletePatient', { name: patient.name }),
      async () => {
        try {
          await api.deletePatient(patient.id);
          setPatients((prev) => prev.filter((p) => p.id !== patient.id));
          setAllPatients((prev) => prev.filter((p) => p.id !== patient.id));
          if (selectedPatient?.id === patient.id) setSelectedPatient(null);
          success(t('clients.patientDeleted'));
        } catch {
          showError(t('clients.failedToDeletePatient'));
        }
      }
    );
  };

  const toggleLabel = (label: PatientLabel) => {
    setPatientFormData((prev) => {
      const currentLabels = prev.labels || [];
      if (currentLabels.includes(label)) {
        return { ...prev, labels: currentLabels.filter((l) => l !== label) };
      }
      return { ...prev, labels: [...currentLabels, label] };
    });
  };

  if (loading) return <Loading text={t('clients.loading')} />;
  if (error) return <Card style={{ padding: spacing.xl, textAlign: 'center' }}><Text variant="muted">{t('errors.failedToLoad')}: {error}</Text><Button onClick={refresh}>{t('errors.retry')}</Button></Card>;

  return (
    <div>
      <PageHeader title={t('clients.title')} actions={<Button variant="primary" onClick={openCreateClient}>{t('clients.addClient')}</Button>} />

      <div style={{ display: 'grid', gridTemplateColumns: '350px 1fr', gap: spacing.md, height: 'calc(100vh - 180px)' }}>
        {/* Left Panel - Clients List */}
        <div style={{ display: 'flex', flexDirection: 'column', backgroundColor: colors.neutral.white, borderRadius: borderRadius.md, border: `1px solid ${colors.neutral.border}`, overflow: 'hidden' }}>
          {/* Search */}
          <div style={{ padding: spacing.md, borderBottom: `1px solid ${colors.neutral.border}` }}>
            <Input
              type="text"
              placeholder={t('clients.searchPlaceholder')}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ width: '100%' }}
            />
            {(loadingAllPatients || searchLoading) && (
              <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
                {searchLoading ? t('common.searching') : t('clients.loadingPets')}
              </Text>
            )}
          </div>

          {/* Clients List */}
          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
            {filteredClients.length === 0 ? (
              <div style={{ padding: spacing.xl, textAlign: 'center' }}>
                <Text variant="muted">{t('clients.noClients')}</Text>
              </div>
            ) : (
              <>
                {/* Client count */}
                <div style={{ padding: `${spacing.xs} ${spacing.md}`, backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                  <Text variant="muted" size="sm">
                    {t('clients.showingClients', { from: Math.min(filteredClients.length, clientsPage * PAGINATION.PANEL + 1), to: Math.min(filteredClients.length, (clientsPage + 1) * PAGINATION.PANEL), total: filteredClients.length })}
                  </Text>
                </div>

                {/* Paginated clients */}
                <div style={{ flex: 1, overflowY: 'auto' }}>
                  {filteredClients
                    .slice(clientsPage * PAGINATION.PANEL, (clientsPage + 1) * PAGINATION.PANEL)
                    .map((client) => {
                      const clientPets = getPetsForClient(client.id);
                      const consentStatus = getClientConsentStatus(client.id);
                      return (
                        <div
                          key={client.id}
                          onClick={() => setSelectedClient(client)}
                          style={{
                            padding: spacing.md,
                            borderBottom: `1px solid ${colors.neutral.border}`,
                            cursor: 'pointer',
                            backgroundColor: selectedClient?.id === client.id ? colors.primary.light : 'transparent',
                            transition: 'background-color 0.15s',
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                            <div style={{
                              width: '36px', height: '36px', borderRadius: borderRadius.full,
                              backgroundColor: selectedClient?.id === client.id ? colors.primary.main : colors.neutral.background,
                              color: selectedClient?.id === client.id ? colors.neutral.white : colors.primary.main,
                              display: 'flex', alignItems: 'center', justifyContent: 'center',
                              fontWeight: fontWeight.bold, fontSize: fontSize.sm,
                            }}>
                              {client.firstName[0]}{client.lastName[0]}
                            </div>
                            <div style={{ flex: 1, minWidth: 0 }}>
                              <div style={{ display: 'flex', alignItems: 'center', gap: spacing.xs }}>
                                <Text style={{ fontWeight: fontWeight.medium }}>{client.firstName} {client.lastName}</Text>
                                {consentStatus === 'granted' ? (
                                  <span title={t('clients.rodoGranted')} style={{
                                    fontSize: fontSize.xs,
                                    backgroundColor: colors.success.light,
                                    color: colors.success.main,
                                    padding: '1px 4px',
                                    borderRadius: borderRadius.sm,
                                    fontWeight: fontWeight.medium,
                                  }}>RODO ✓</span>
                                ) : consentStatus === 'pending' ? (
                                  <span title={t('clients.rodoPending')} style={{
                                    fontSize: fontSize.xs,
                                    backgroundColor: colors.warning.light,
                                    color: colors.warning.main,
                                    padding: '1px 4px',
                                    borderRadius: borderRadius.sm,
                                    fontWeight: fontWeight.medium,
                                  }}>RODO ⏳</span>
                                ) : (
                                  <span title={t('clients.rodoNone')} style={{
                                    fontSize: fontSize.xs,
                                    backgroundColor: colors.danger.light,
                                    color: colors.danger.main,
                                    padding: '1px 4px',
                                    borderRadius: borderRadius.sm,
                                    fontWeight: fontWeight.medium,
                                  }}>RODO ✗</span>
                                )}
                              </div>
                              <Text variant="muted" size="sm" style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                {client.phone || client.email}
                              </Text>
                              {/* Show pets */}
                              {clientPets.length > 0 && (
                                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', marginTop: spacing.xs }}>
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
                                      {t('clients.moreItems', { count: clientPets.length - 3 })}
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
                {filteredClients.length > PAGINATION.PANEL && (
                  <div style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    gap: spacing.xs,
                    padding: spacing.sm,
                    borderTop: `1px solid ${colors.neutral.border}`,
                    backgroundColor: colors.neutral.background,
                  }}>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setClientsPage((p) => Math.max(0, p - 1))}
                      disabled={clientsPage === 0}
                    >
                      ←
                    </Button>
                    <Text size="sm" style={{ color: colors.neutral.textMuted }}>
                      {clientsPage + 1} / {Math.ceil(filteredClients.length / PAGINATION.PANEL)}
                    </Text>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setClientsPage((p) => Math.min(Math.ceil(filteredClients.length / PAGINATION.PANEL) - 1, p + 1))}
                      disabled={clientsPage >= Math.ceil(filteredClients.length / PAGINATION.PANEL) - 1}
                    >
                      →
                    </Button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        {/* Right Panel - Detail View */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md, overflow: 'hidden' }}>
          {!selectedClient ? (
            <Card style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column' }}>
              <div style={{ fontSize: '48px', marginBottom: spacing.md }}>👈</div>
              <Text variant="muted">{t('clients.selectClientToView')}</Text>
            </Card>
          ) : (
            <>
              {/* Client Info Header */}
              <Card style={{ padding: spacing.md }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold }}>
                      {selectedClient.firstName} {selectedClient.lastName}
                    </Text>
                    <div style={{ display: 'flex', gap: spacing.md, marginTop: spacing.xs }}>
                      {selectedClient.email && <Text variant="muted" size="sm">📧 {selectedClient.email}</Text>}
                      {selectedClient.phone && <Text variant="muted" size="sm">📱 {selectedClient.phone}</Text>}
                    </div>
                    {(selectedClient.address || selectedClient.city) && (
                      <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
                        📍 {[selectedClient.address, selectedClient.city, selectedClient.postalCode].filter(Boolean).join(', ')}
                      </Text>
                    )}
                  </div>
                  <div style={{ display: 'flex', gap: spacing.xs }}>
                    <Button variant="ghost" size="sm" onClick={() => openEditClient(selectedClient)}>{t('common.edit')}</Button>
                    <Button variant="danger" size="sm" onClick={() => handleDeleteClient(selectedClient)}>{t('common.delete')}</Button>
                  </div>
                </div>
              </Card>

              {/* Patients and Visits Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md, flex: 1, overflow: 'hidden' }}>
                {/* Patients Panel */}
                <Card style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.md }}>
                    <Text style={{ fontWeight: fontWeight.semibold }}>{t('clients.patientsCount')} ({patients.length})</Text>
                    <Button variant="primary" size="sm" onClick={openCreatePatient}>{t('clients.add')}</Button>
                  </div>

                  <div style={{ flex: 1, overflowY: 'auto' }}>
                    {loadingPatients ? (
                      <Loading text={t('clients.loadingPatients')} />
                    ) : patients.length === 0 ? (
                      <div style={{ textAlign: 'center', padding: spacing.xl }}>
                        <Text variant="muted">{t('clients.noPatients')}</Text>
                      </div>
                    ) : (
                      patients.map((patient) => {
                        const speciesInfo = getSpeciesInfo(patient.species);
                        return (
                          <div
                            key={patient.id}
                            onClick={() => setSelectedPatient(patient)}
                            style={{
                              padding: spacing.sm,
                              marginBottom: spacing.xs,
                              borderRadius: borderRadius.sm,
                              cursor: 'pointer',
                              backgroundColor: selectedPatient?.id === patient.id ? colors.primary.light : colors.neutral.background,
                              border: `2px solid ${selectedPatient?.id === patient.id ? colors.primary.main : 'transparent'}`,
                            }}
                          >
                            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                              <span style={{ fontSize: fontSize.lg }}>{speciesInfo.emoji}</span>
                              <div style={{ flex: 1 }}>
                                <Text style={{ fontWeight: fontWeight.medium }}>{patient.name}</Text>
                                <Text variant="muted" size="sm">{speciesInfo.label}{patient.breed ? ` - ${patient.breed}` : ''}</Text>
                              </div>
                              <div style={{ display: 'flex', gap: spacing.xs }}>
                                <Button variant="primary" size="sm" onClick={(e) => { e.stopPropagation(); navigate(`/patients/${patient.id}`); }}>{t('common.details')}</Button>
                                <Button variant="ghost" size="sm" onClick={(e) => { e.stopPropagation(); openEditPatient(patient); }}>{t('common.edit')}</Button>
                                <Button variant="danger" size="sm" onClick={(e) => { e.stopPropagation(); handleDeletePatient(patient); }}>{t('clients.del')}</Button>
                              </div>
                            </div>
                            {patient.labels && patient.labels.length > 0 && (
                              <div style={{ display: 'flex', flexWrap: 'wrap', gap: spacing.xs, marginTop: spacing.xs }}>
                                {patient.labels.map((label) => {
                                  const info = getLabelInfo(label);
                                  return <Badge key={label} variant={info.variant} style={{ fontSize: fontSize.xs }}>{info.icon} {info.display}</Badge>;
                                })}
                              </div>
                            )}
                          </div>
                        );
                      })
                    )}
                  </div>
                </Card>

                {/* Visits Panel - Redesigned with pagination */}
                <Card style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.md }}>
                    <Text style={{ fontWeight: fontWeight.semibold }}>
                      {selectedPatient ? `${t('clients.visitHistory')} - ${selectedPatient.name}` : t('clients.visitHistory')}
                      {visits.length > 0 && <span style={{ color: colors.neutral.textMuted, fontWeight: fontWeight.normal }}> ({visits.length})</span>}
                    </Text>
                  </div>

                  <div style={{ flex: 1, overflowY: 'auto' }}>
                    {!selectedPatient ? (
                      <div style={{ textAlign: 'center', padding: spacing.xl }}>
                        <Text variant="muted">{t('clients.selectPatientToViewVisits')}</Text>
                      </div>
                    ) : loadingVisits ? (
                      <Loading text={t('clients.loadingVisits')} />
                    ) : visits.length === 0 ? (
                      <div style={{ textAlign: 'center', padding: spacing.xl }}>
                        <Text variant="muted">{t('clients.noVisitsRecorded')}</Text>
                      </div>
                    ) : (
                      <>
                        {/* Paginated visits */}
                        {visits
                          .slice(visitsPage * PAGINATION.PANEL, (visitsPage + 1) * PAGINATION.PANEL)
                          .map((visit) => {
                            const status = VISIT_STATUS_CONFIG[visit.status] || VISIT_STATUS_CONFIG.SCHEDULED;
                            const materials = visit.usedMaterials || [];
                            const hasMaterials = materials.length > 0;

                            return (
                              <div
                                key={visit.id}
                                onClick={() => setSelectedVisit(visit)}
                                style={{
                                  padding: spacing.md,
                                  marginBottom: spacing.sm,
                                  borderRadius: borderRadius.md,
                                  cursor: 'pointer',
                                  backgroundColor: colors.neutral.white,
                                  border: `1px solid ${colors.neutral.border}`,
                                  borderLeft: `4px solid ${status.color}`,
                                  transition: 'box-shadow 0.2s',
                                }}
                                onMouseEnter={(e) => (e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)')}
                                onMouseLeave={(e) => (e.currentTarget.style.boxShadow = 'none')}
                              >
                                {/* Header: Date, Time, Status */}
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: spacing.sm }}>
                                  <div>
                                    <Text style={{ fontWeight: fontWeight.bold, fontSize: fontSize.sm }}>
                                      📅 {formatDate(visit.visitDate)}
                                    </Text>
                                    <Text variant="muted" size="sm">
                                      🕐 {formatTime(visit.visitDate)} • {visit.veterinarianName ? `Dr. ${visit.veterinarianName}` : t('clients.noVet')}
                                    </Text>
                                  </div>
                                  <Badge variant={visit.status === 'COMPLETED' ? 'success' : visit.status === 'CANCELLED' ? 'secondary' : 'primary'}>
                                    {status.label}
                                  </Badge>
                                </div>

                                {/* Reason */}
                                {visit.reason && (
                                  <div style={{ marginBottom: spacing.sm }}>
                                    <Text size="sm" style={{ color: colors.neutral.textLight }}>
                                      <strong>{t('clients.reasonForVisit')}:</strong> {visit.reason}
                                    </Text>
                                  </div>
                                )}

                                {/* Notes */}
                                {visit.notes && (
                                  <div style={{
                                    backgroundColor: colors.warning.light,
                                    padding: spacing.xs,
                                    borderRadius: borderRadius.sm,
                                    marginBottom: spacing.sm,
                                  }}>
                                    <Text size="sm">
                                      📝 {visit.notes.length > UI.TRUNCATE.MEDIUM ? `${visit.notes.substring(0, UI.TRUNCATE.MEDIUM)}...` : visit.notes}
                                    </Text>
                                  </div>
                                )}

                                {/* Diagnosis & Treatment */}
                                {(visit.diagnosis || visit.treatment) && (
                                  <div style={{ marginBottom: spacing.sm }}>
                                    {visit.diagnosis && (
                                      <Text size="sm" style={{ marginBottom: spacing.xs }}>
                                        <strong>🔍 {t('clients.diagnosis')}:</strong> {visit.diagnosis.length > UI.TRUNCATE.SHORT ? `${visit.diagnosis.substring(0, UI.TRUNCATE.SHORT)}...` : visit.diagnosis}
                                      </Text>
                                    )}
                                    {visit.treatment && (
                                      <Text size="sm">
                                        <strong>💊 {t('clients.treatment')}:</strong> {visit.treatment.length > UI.TRUNCATE.SHORT ? `${visit.treatment.substring(0, UI.TRUNCATE.SHORT)}...` : visit.treatment}
                                      </Text>
                                    )}
                                  </div>
                                )}

                                {/* Used Materials / Procedures */}
                                {hasMaterials && (
                                  <div style={{
                                    backgroundColor: colors.success.light,
                                    padding: spacing.xs,
                                    borderRadius: borderRadius.sm,
                                    borderLeft: `3px solid ${colors.success.main}`,
                                  }}>
                                    <Text size="sm" style={{ fontWeight: fontWeight.medium, marginBottom: spacing.xs }}>
                                      🏥 {t('clients.proceduresAndMaterials')} ({materials.length}):
                                    </Text>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                                      {materials.slice(0, UI.PREVIEW.MATERIALS).map((m, idx) => (
                                        <span
                                          key={`${visit.id}-material-${idx}`}
                                          style={{
                                            fontSize: fontSize.xs,
                                            backgroundColor: colors.neutral.white,
                                            padding: '2px 6px',
                                            borderRadius: borderRadius.sm,
                                            border: `1px solid ${colors.neutral.border}`,
                                          }}
                                        >
                                          {m.name} x{m.quantity}
                                        </span>
                                      ))}
                                      {materials.length > UI.PREVIEW.MATERIALS && (
                                        <span
                                          style={{
                                            fontSize: fontSize.xs,
                                            backgroundColor: colors.primary.light,
                                            color: colors.primary.main,
                                            padding: '2px 6px',
                                            borderRadius: borderRadius.sm,
                                          }}
                                        >
                                          +{materials.length - UI.PREVIEW.MATERIALS} {t('clients.more')}
                                        </span>
                                      )}
                                    </div>
                                  </div>
                                )}

                                {/* Details Button */}
                                <div style={{ marginTop: spacing.sm, textAlign: 'right' }}>
                                  <Button
                                    variant="primary"
                                    size="sm"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      navigate(`/visit/${visit.id}`);
                                    }}
                                  >
                                    {t('common.details')}
                                  </Button>
                                </div>
                              </div>
                            );
                          })}

                        {/* Pagination Controls */}
                        {visits.length > PAGINATION.PANEL && (
                          <div style={{
                            display: 'flex',
                            justifyContent: 'center',
                            alignItems: 'center',
                            gap: spacing.sm,
                            padding: spacing.md,
                            borderTop: `1px solid ${colors.neutral.border}`,
                            marginTop: spacing.sm,
                          }}>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => setVisitsPage((p) => Math.max(0, p - 1))}
                              disabled={visitsPage === 0}
                            >
                              ← Previous
                            </Button>
                            <Text size="sm" style={{ color: colors.neutral.textMuted }}>
                              Page {visitsPage + 1} of {Math.ceil(visits.length / PAGINATION.PANEL)}
                            </Text>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => setVisitsPage((p) => Math.min(Math.ceil(visits.length / PAGINATION.PANEL) - 1, p + 1))}
                              disabled={visitsPage >= Math.ceil(visits.length / PAGINATION.PANEL) - 1}
                            >
                              Next →
                            </Button>
                          </div>
                        )}
                      </>
                    )}
                  </div>
                </Card>
              </div>
            </>
          )}
        </div>
      </div>

      {/* Client Form Modal */}
      <Modal open={showClientForm} onClose={() => setShowClientForm(false)}>
        <ModalTitle>{editingClient ? t('clients.editClient') : t('clients.newClient')}</ModalTitle>
        <form onSubmit={handleClientSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <FormField label={t('clients.firstName')} required>
              <Input value={clientFormData.firstName} onChange={(e) => setClientFormData({ ...clientFormData, firstName: e.target.value })} required />
            </FormField>
            <FormField label={t('clients.lastName')} required>
              <Input value={clientFormData.lastName} onChange={(e) => setClientFormData({ ...clientFormData, lastName: e.target.value })} required />
            </FormField>
          </div>
          <FormField label={t('clients.email')} required>
            <Input type="email" value={clientFormData.email} onChange={(e) => setClientFormData({ ...clientFormData, email: e.target.value })} required />
          </FormField>
          <FormField label={t('clients.phone')}>
            <Input value={clientFormData.phone || ''} onChange={(e) => setClientFormData({ ...clientFormData, phone: e.target.value })} />
          </FormField>
          <FormField label={t('clients.address')}>
            <Input value={clientFormData.address || ''} onChange={(e) => setClientFormData({ ...clientFormData, address: e.target.value })} />
          </FormField>
          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: spacing.md }}>
            <FormField label={t('clients.city')}>
              <Input value={clientFormData.city || ''} onChange={(e) => setClientFormData({ ...clientFormData, city: e.target.value })} />
            </FormField>
            <FormField label={t('clients.postalCode')}>
              <Input value={clientFormData.postalCode || ''} onChange={(e) => setClientFormData({ ...clientFormData, postalCode: e.target.value })} />
            </FormField>
          </div>
          <ModalActions>
            <Button type="button" variant="ghost" onClick={() => setShowClientForm(false)} disabled={isSubmittingClient}>{t('common.cancel')}</Button>
            <Button type="submit" variant="primary" disabled={isSubmittingClient}>
              {isSubmittingClient ? t('common.saving') : editingClient ? t('common.save') : t('common.create')}
            </Button>
          </ModalActions>
        </form>
      </Modal>

      {/* Patient Form Modal */}
      <Modal open={showPatientForm} onClose={() => setShowPatientForm(false)}>
        <ModalTitle>{editingPatient ? t('patients.editPatient') : t('patients.addPatient')}</ModalTitle>
        <form onSubmit={handlePatientSubmit}>
          <FormField label={t('patients.name')} required>
            <Input value={patientFormData.name} onChange={(e) => setPatientFormData({ ...patientFormData, name: e.target.value })} required />
          </FormField>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <FormField label={t('patients.species')} required>
              <Select
                value={patientFormData.species}
                onChange={(e) => {
                  const value = e.target.value;
                  if (isSpecies(value)) {
                    setPatientFormData({ ...patientFormData, species: value });
                  }
                }}
              >
                {SPECIES_OPTIONS.map((s) => <option key={s.value} value={s.value}>{s.emoji} {s.label}</option>)}
              </Select>
            </FormField>
            <FormField label={t('patients.breed')}>
              <Input value={patientFormData.breed || ''} onChange={(e) => setPatientFormData({ ...patientFormData, breed: e.target.value })} />
            </FormField>
          </div>
          <div style={{ marginBottom: spacing.md }}>
            <Text size="sm" style={{ fontWeight: fontWeight.medium, marginBottom: spacing.xs }}>{t('patients.labels')}</Text>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: spacing.xs }}>
              {PATIENT_LABELS.map(({ label, display, icon }) => {
                const isSelected = patientFormData.labels?.includes(label);
                return (
                  <button
                    key={label}
                    type="button"
                    onClick={() => toggleLabel(label)}
                    style={{
                      padding: `${spacing.xs} ${spacing.sm}`,
                      borderRadius: borderRadius.full,
                      border: `2px solid ${isSelected ? colors.primary.main : colors.neutral.border}`,
                      backgroundColor: isSelected ? colors.primary.light : colors.neutral.white,
                      cursor: 'pointer',
                      fontSize: fontSize.sm,
                    }}
                  >
                    {icon} {display}
                  </button>
                );
              })}
            </div>
          </div>
          <FormField label={t('patients.notes')}>
            <TextArea value={patientFormData.notes || ''} onChange={(e) => setPatientFormData({ ...patientFormData, notes: e.target.value })} rows={2} />
          </FormField>
          <ModalActions>
            <Button type="button" variant="ghost" onClick={() => setShowPatientForm(false)} disabled={isSubmittingPatient}>{t('common.cancel')}</Button>
            <Button type="submit" variant="primary" disabled={isSubmittingPatient}>
              {isSubmittingPatient ? t('common.saving') : editingPatient ? t('common.save') : t('common.add')}
            </Button>
          </ModalActions>
        </form>
      </Modal>

      {/* Visit Details Modal */}
      <Modal open={!!selectedVisit} onClose={() => setSelectedVisit(null)}>
        {selectedVisit && (
          <>
            <ModalTitle>{t('visits.visitDetails')}</ModalTitle>
            <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">{t('clients.dateTime')}</Text>
                  <Text style={{ fontWeight: fontWeight.medium }}>{formatDateTime(selectedVisit.visitDate)}</Text>
                </div>
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">{t('clients.duration')}</Text>
                  <Text style={{ fontWeight: fontWeight.medium }}>{t('clients.minutes', { count: selectedVisit.durationMinutes || 30 })}</Text>
                </div>
              </div>

              <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                <Text variant="muted" size="sm">{t('visits.veterinarian')}</Text>
                <Text style={{ fontWeight: fontWeight.medium }}>{selectedVisit.veterinarianName ? `Dr. ${selectedVisit.veterinarianName}` : t('clients.notAssigned')}</Text>
              </div>

              <div style={{ backgroundColor: VISIT_STATUS_CONFIG[selectedVisit.status]?.bg || colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                <Text variant="muted" size="sm">{t('common.status')}</Text>
                <Text style={{ fontWeight: fontWeight.medium, color: VISIT_STATUS_CONFIG[selectedVisit.status]?.color }}>{VISIT_STATUS_CONFIG[selectedVisit.status]?.label}</Text>
              </div>

              {selectedVisit.reason && (
                <div style={{ backgroundColor: colors.warning.light, padding: spacing.sm, borderRadius: borderRadius.sm, borderLeft: `3px solid ${colors.warning.main}` }}>
                  <Text variant="muted" size="sm">{t('clients.reasonForVisit')}</Text>
                  <Text>{selectedVisit.reason}</Text>
                </div>
              )}

              {selectedVisit.diagnosis && (
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">{t('clients.diagnosis')}</Text>
                  <Text>{selectedVisit.diagnosis}</Text>
                </div>
              )}

              {selectedVisit.treatment && (
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">{t('clients.treatment')}</Text>
                  <Text>{selectedVisit.treatment}</Text>
                </div>
              )}

              {selectedVisit.notes && (
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">{t('common.notes')}</Text>
                  <Text>{selectedVisit.notes}</Text>
                </div>
              )}
            </div>
            <ModalActions>
              <Button variant="ghost" onClick={() => setSelectedVisit(null)}>{t('common.close')}</Button>
            </ModalActions>
          </>
        )}
      </Modal>

      <ConfirmDialog
        open={dialogState.open}
        onClose={closeDialog}
        onConfirm={handleConfirm}
        title={dialogState.title}
        message={dialogState.message}
        confirmLabel={t('common.delete')}
        cancelLabel={t('common.cancel')}
        variant="danger"
      />
    </div>
  );
}
