import { useState, useEffect, useMemo, useCallback } from 'react';
import { useClients } from '../hooks';
import {
  ClientRequest,
  ClientResponse,
  PatientRequest,
  PatientResponse,
  VisitResponse,
  Species,
  PatientLabel,
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
  Select,
  TextArea,
  Text,
  Loading,
  PageHeader,
  useToast,
} from '../components/ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../theme';
import {
  SPECIES_OPTIONS,
  PATIENT_LABELS,
  getSpeciesInfo,
  getLabelInfo,
  VISIT_STATUS_CONFIG,
  PAGINATION,
  UI,
} from '../constants';
import { formatDateTime, formatDate, formatTime } from '../utils';

// Type guard for Species
const isSpecies = (value: string): value is Species => {
  const validSpecies: Species[] = ['DOG', 'CAT', 'BIRD', 'RABBIT', 'HAMSTER', 'FISH', 'REPTILE', 'OTHER'];
  return validSpecies.includes(value as Species);
};

export function ClientsPage() {
  const { clients, loading, error, createClient, updateClient, deleteClient, refresh } = useClients();
  const { success, error: showError } = useToast();

  // Search and selection state
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedClient, setSelectedClient] = useState<ClientResponse | null>(null);
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null);
  const [selectedVisit, setSelectedVisit] = useState<VisitResponse | null>(null);

  // Data state - all patients for search
  const [allPatients, setAllPatients] = useState<PatientResponse[]>([]);
  const [patients, setPatients] = useState<PatientResponse[]>([]);
  const [visits, setVisits] = useState<VisitResponse[]>([]);
  const [loadingAllPatients, setLoadingAllPatients] = useState(false);
  const [loadingPatients, setLoadingPatients] = useState(false);
  const [loadingVisits, setLoadingVisits] = useState(false);

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

  // Load all patients for search functionality
  useEffect(() => {
    setLoadingAllPatients(true);
    api.getPatients()
      .then(setAllPatients)
      .catch(() => { /* Silently fail - patients will be empty */ })
      .finally(() => setLoadingAllPatients(false));
  }, []);

  // Helper to get pets for a client - memoized
  const getPetsForClient = useCallback(
    (clientId: string): PatientResponse[] => {
      return allPatients.filter((p) => p.ownerId === clientId);
    },
    [allPatients]
  );

  // Filter clients by search (including pet names) - memoized for performance
  const filteredClients = useMemo(() => {
    const query = searchQuery.toLowerCase();
    return clients.filter((client) => {
      // Check client fields
      const matchesClient =
        client.firstName.toLowerCase().includes(query) ||
        client.lastName.toLowerCase().includes(query) ||
        client.email.toLowerCase().includes(query) ||
        client.phone?.toLowerCase().includes(query);

      if (matchesClient) return true;

      // Check pet names for this client
      const clientPets = getPetsForClient(client.id);
      const matchesPet = clientPets.some((pet) =>
        pet.name.toLowerCase().includes(query) ||
        pet.breed?.toLowerCase().includes(query)
      );

      return matchesPet;
    });
  }, [clients, searchQuery, getPetsForClient]);

  // Reset clients page when search changes
  useEffect(() => {
    setClientsPage(0);
  }, [searchQuery]);

  // Load patients when client selected
  useEffect(() => {
    if (selectedClient) {
      setLoadingPatients(true);
      setSelectedPatient(null);
      setVisits([]);
      api.getPatients(selectedClient.id)
        .then(setPatients)
        .catch(() => showError('Failed to load patients'))
        .finally(() => setLoadingPatients(false));
    } else {
      setPatients([]);
    }
  }, [selectedClient]);

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
        .catch(() => showError('Failed to load visits'))
        .finally(() => setLoadingVisits(false));
    } else {
      setVisits([]);
    }
  }, [selectedPatient]);

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
        success('Client updated');
      } else {
        await createClient(clientFormData);
        success('Client created');
      }
      setShowClientForm(false);
    } catch {
      showError('Failed to save client');
    } finally {
      setIsSubmittingClient(false);
    }
  };

  const handleDeleteClient = async (client: ClientResponse) => {
    if (window.confirm(`Delete ${client.firstName} ${client.lastName}?`)) {
      try {
        await deleteClient(client.id);
        if (selectedClient?.id === client.id) {
          setSelectedClient(null);
        }
        success('Client deleted');
      } catch {
        showError('Failed to delete client');
      }
    }
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
        success('Patient updated');
      } else {
        const created = await api.createPatient({ ...patientFormData, ownerId: selectedClient?.id });
        setPatients((prev) => [...prev, created]);
        setAllPatients((prev) => [...prev, created]);
        success('Patient added');
      }
      setShowPatientForm(false);
    } catch {
      showError('Failed to save patient');
    } finally {
      setIsSubmittingPatient(false);
    }
  };

  const handleDeletePatient = async (patient: PatientResponse) => {
    if (window.confirm(`Delete ${patient.name}?`)) {
      try {
        await api.deletePatient(patient.id);
        setPatients((prev) => prev.filter((p) => p.id !== patient.id));
        setAllPatients((prev) => prev.filter((p) => p.id !== patient.id));
        if (selectedPatient?.id === patient.id) setSelectedPatient(null);
        success('Patient deleted');
      } catch {
        showError('Failed to delete patient');
      }
    }
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

  if (loading) return <Loading text="Loading clients..." />;
  if (error) return <Card style={{ padding: spacing.xl, textAlign: 'center' }}><Text variant="muted">Error: {error}</Text><Button onClick={refresh}>Retry</Button></Card>;

  return (
    <div>
      <PageHeader title="Clients" actions={<Button variant="primary" onClick={openCreateClient}>+ Add Client</Button>} />

      <div style={{ display: 'grid', gridTemplateColumns: '350px 1fr', gap: spacing.md, height: 'calc(100vh - 180px)' }}>
        {/* Left Panel - Clients List */}
        <div style={{ display: 'flex', flexDirection: 'column', backgroundColor: colors.neutral.white, borderRadius: borderRadius.md, border: `1px solid ${colors.neutral.border}`, overflow: 'hidden' }}>
          {/* Search */}
          <div style={{ padding: spacing.md, borderBottom: `1px solid ${colors.neutral.border}` }}>
            <Input
              type="text"
              placeholder="Search clients or pets..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{ width: '100%' }}
            />
            {loadingAllPatients && (
              <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>Loading pets...</Text>
            )}
          </div>

          {/* Clients List */}
          <div style={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column' }}>
            {filteredClients.length === 0 ? (
              <div style={{ padding: spacing.xl, textAlign: 'center' }}>
                <Text variant="muted">No clients found</Text>
              </div>
            ) : (
              <>
                {/* Client count */}
                <div style={{ padding: `${spacing.xs} ${spacing.md}`, backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                  <Text variant="muted" size="sm">
                    Showing {Math.min(filteredClients.length, clientsPage * PAGINATION.PANEL + 1)}-{Math.min(filteredClients.length, (clientsPage + 1) * PAGINATION.PANEL)} of {filteredClients.length} clients
                  </Text>
                </div>

                {/* Paginated clients */}
                <div style={{ flex: 1, overflowY: 'auto' }}>
                  {filteredClients
                    .slice(clientsPage * PAGINATION.PANEL, (clientsPage + 1) * PAGINATION.PANEL)
                    .map((client) => {
                      const clientPets = getPetsForClient(client.id);
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
                              <Text style={{ fontWeight: fontWeight.medium }}>{client.firstName} {client.lastName}</Text>
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
              <Text variant="muted">Select a client to view details</Text>
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
                    <Button variant="ghost" size="sm" onClick={() => openEditClient(selectedClient)}>Edit</Button>
                    <Button variant="danger" size="sm" onClick={() => handleDeleteClient(selectedClient)}>Delete</Button>
                  </div>
                </div>
              </Card>

              {/* Patients and Visits Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md, flex: 1, overflow: 'hidden' }}>
                {/* Patients Panel */}
                <Card style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.md }}>
                    <Text style={{ fontWeight: fontWeight.semibold }}>Patients ({patients.length})</Text>
                    <Button variant="primary" size="sm" onClick={openCreatePatient}>+ Add</Button>
                  </div>

                  <div style={{ flex: 1, overflowY: 'auto' }}>
                    {loadingPatients ? (
                      <Loading text="Loading patients..." />
                    ) : patients.length === 0 ? (
                      <div style={{ textAlign: 'center', padding: spacing.xl }}>
                        <Text variant="muted">No patients yet</Text>
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
                                <Button variant="ghost" size="sm" onClick={(e) => { e.stopPropagation(); openEditPatient(patient); }}>Edit</Button>
                                <Button variant="danger" size="sm" onClick={(e) => { e.stopPropagation(); handleDeletePatient(patient); }}>Del</Button>
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
                      {selectedPatient ? `Visit History - ${selectedPatient.name}` : 'Visit History'}
                      {visits.length > 0 && <span style={{ color: colors.neutral.textMuted, fontWeight: fontWeight.normal }}> ({visits.length})</span>}
                    </Text>
                  </div>

                  <div style={{ flex: 1, overflowY: 'auto' }}>
                    {!selectedPatient ? (
                      <div style={{ textAlign: 'center', padding: spacing.xl }}>
                        <Text variant="muted">Select a patient to view visits</Text>
                      </div>
                    ) : loadingVisits ? (
                      <Loading text="Loading visits..." />
                    ) : visits.length === 0 ? (
                      <div style={{ textAlign: 'center', padding: spacing.xl }}>
                        <Text variant="muted">No visits recorded</Text>
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
                                      🕐 {formatTime(visit.visitDate)} • {visit.veterinarianName ? `Dr. ${visit.veterinarianName}` : 'No vet'}
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
                                      <strong>Reason:</strong> {visit.reason}
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
                                        <strong>🔍 Diagnosis:</strong> {visit.diagnosis.length > UI.TRUNCATE.SHORT ? `${visit.diagnosis.substring(0, UI.TRUNCATE.SHORT)}...` : visit.diagnosis}
                                      </Text>
                                    )}
                                    {visit.treatment && (
                                      <Text size="sm">
                                        <strong>💊 Treatment:</strong> {visit.treatment.length > UI.TRUNCATE.SHORT ? `${visit.treatment.substring(0, UI.TRUNCATE.SHORT)}...` : visit.treatment}
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
                                      🏥 Procedures & Materials ({materials.length}):
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
                                          +{materials.length - UI.PREVIEW.MATERIALS} more
                                        </span>
                                      )}
                                    </div>
                                  </div>
                                )}
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
        <ModalTitle>{editingClient ? 'Edit Client' : 'New Client'}</ModalTitle>
        <form onSubmit={handleClientSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <FormField label="First Name" required>
              <Input value={clientFormData.firstName} onChange={(e) => setClientFormData({ ...clientFormData, firstName: e.target.value })} required />
            </FormField>
            <FormField label="Last Name" required>
              <Input value={clientFormData.lastName} onChange={(e) => setClientFormData({ ...clientFormData, lastName: e.target.value })} required />
            </FormField>
          </div>
          <FormField label="Email" required>
            <Input type="email" value={clientFormData.email} onChange={(e) => setClientFormData({ ...clientFormData, email: e.target.value })} required />
          </FormField>
          <FormField label="Phone">
            <Input value={clientFormData.phone || ''} onChange={(e) => setClientFormData({ ...clientFormData, phone: e.target.value })} />
          </FormField>
          <FormField label="Address">
            <Input value={clientFormData.address || ''} onChange={(e) => setClientFormData({ ...clientFormData, address: e.target.value })} />
          </FormField>
          <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: spacing.md }}>
            <FormField label="City">
              <Input value={clientFormData.city || ''} onChange={(e) => setClientFormData({ ...clientFormData, city: e.target.value })} />
            </FormField>
            <FormField label="Postal Code">
              <Input value={clientFormData.postalCode || ''} onChange={(e) => setClientFormData({ ...clientFormData, postalCode: e.target.value })} />
            </FormField>
          </div>
          <ModalActions>
            <Button type="button" variant="ghost" onClick={() => setShowClientForm(false)} disabled={isSubmittingClient}>Cancel</Button>
            <Button type="submit" variant="primary" disabled={isSubmittingClient}>
              {isSubmittingClient ? 'Saving...' : editingClient ? 'Save' : 'Create'}
            </Button>
          </ModalActions>
        </form>
      </Modal>

      {/* Patient Form Modal */}
      <Modal open={showPatientForm} onClose={() => setShowPatientForm(false)}>
        <ModalTitle>{editingPatient ? 'Edit Patient' : 'Add Patient'}</ModalTitle>
        <form onSubmit={handlePatientSubmit}>
          <FormField label="Name" required>
            <Input value={patientFormData.name} onChange={(e) => setPatientFormData({ ...patientFormData, name: e.target.value })} required />
          </FormField>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <FormField label="Species" required>
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
            <FormField label="Breed">
              <Input value={patientFormData.breed || ''} onChange={(e) => setPatientFormData({ ...patientFormData, breed: e.target.value })} />
            </FormField>
          </div>
          <div style={{ marginBottom: spacing.md }}>
            <Text size="sm" style={{ fontWeight: fontWeight.medium, marginBottom: spacing.xs }}>Labels</Text>
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
          <FormField label="Notes">
            <TextArea value={patientFormData.notes || ''} onChange={(e) => setPatientFormData({ ...patientFormData, notes: e.target.value })} rows={2} />
          </FormField>
          <ModalActions>
            <Button type="button" variant="ghost" onClick={() => setShowPatientForm(false)} disabled={isSubmittingPatient}>Cancel</Button>
            <Button type="submit" variant="primary" disabled={isSubmittingPatient}>
              {isSubmittingPatient ? 'Saving...' : editingPatient ? 'Save' : 'Add'}
            </Button>
          </ModalActions>
        </form>
      </Modal>

      {/* Visit Details Modal */}
      <Modal open={!!selectedVisit} onClose={() => setSelectedVisit(null)}>
        {selectedVisit && (
          <>
            <ModalTitle>Visit Details</ModalTitle>
            <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">Date & Time</Text>
                  <Text style={{ fontWeight: fontWeight.medium }}>{formatDateTime(selectedVisit.visitDate)}</Text>
                </div>
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">Duration</Text>
                  <Text style={{ fontWeight: fontWeight.medium }}>{selectedVisit.durationMinutes || 30} minutes</Text>
                </div>
              </div>

              <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                <Text variant="muted" size="sm">Veterinarian</Text>
                <Text style={{ fontWeight: fontWeight.medium }}>{selectedVisit.veterinarianName ? `Dr. ${selectedVisit.veterinarianName}` : 'Not assigned'}</Text>
              </div>

              <div style={{ backgroundColor: VISIT_STATUS_CONFIG[selectedVisit.status]?.bg || colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                <Text variant="muted" size="sm">Status</Text>
                <Text style={{ fontWeight: fontWeight.medium, color: VISIT_STATUS_CONFIG[selectedVisit.status]?.color }}>{VISIT_STATUS_CONFIG[selectedVisit.status]?.label}</Text>
              </div>

              {selectedVisit.reason && (
                <div style={{ backgroundColor: colors.warning.light, padding: spacing.sm, borderRadius: borderRadius.sm, borderLeft: `3px solid ${colors.warning.main}` }}>
                  <Text variant="muted" size="sm">Reason for Visit</Text>
                  <Text>{selectedVisit.reason}</Text>
                </div>
              )}

              {selectedVisit.diagnosis && (
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">Diagnosis</Text>
                  <Text>{selectedVisit.diagnosis}</Text>
                </div>
              )}

              {selectedVisit.treatment && (
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">Treatment</Text>
                  <Text>{selectedVisit.treatment}</Text>
                </div>
              )}

              {selectedVisit.notes && (
                <div style={{ backgroundColor: colors.neutral.background, padding: spacing.sm, borderRadius: borderRadius.sm }}>
                  <Text variant="muted" size="sm">Notes</Text>
                  <Text>{selectedVisit.notes}</Text>
                </div>
              )}
            </div>
            <ModalActions>
              <Button variant="ghost" onClick={() => setSelectedVisit(null)}>Close</Button>
            </ModalActions>
          </>
        )}
      </Modal>
    </div>
  );
}
