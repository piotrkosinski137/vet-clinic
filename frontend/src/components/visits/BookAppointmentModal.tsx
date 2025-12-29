import { useState, useEffect, useRef, useCallback } from "react";
import { api, VisitRequest, PatientResponse, VeterinarianResponse, ClientResponse, VisitType } from "../../api";
import {
  Button,
  Modal,
  ModalTitle,
  ModalActions,
  FormField,
  Input,
  Select,
  TextArea,
  Text,
  useToast,
} from "../ui";
import { useI18n } from "../../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight, zIndex } from "../../theme";
import { VISIT_TYPE_CONFIG, VisitTypeKey } from "../../constants/visitTypes";

// Debounce hook
function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);
  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);
  return debouncedValue;
}

export interface BookAppointmentModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  initialDate?: Date | null;
  initialHour?: number | null;
  initialMinute?: number | null;
  initialVeterinarianId?: string | null;
}

function formatDateTimeLocal(date: Date, hour?: number | null, minute?: number | null): string {
  const d = new Date(date);
  if (hour !== null && hour !== undefined) {
    d.setHours(hour, minute ?? 0, 0, 0);
  }
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  const hours = String(d.getHours()).padStart(2, "0");
  const minutes = String(d.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

export function BookAppointmentModal({
  open,
  onClose,
  onSuccess,
  initialDate,
  initialHour,
  initialMinute,
  initialVeterinarianId,
}: BookAppointmentModalProps) {
  const { t } = useI18n();
  const { error: showError } = useToast();
  const [loading, setLoading] = useState(false);
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [veterinarians, setVeterinarians] = useState<VeterinarianResponse[]>([]);
  const [formData, setFormData] = useState<VisitRequest>({
    patientId: "",
    visitDate: "",
    durationMinutes: 30,
    visitType: "CONSULTATION",
    reason: "",
    veterinarianId: "",
    veterinarianName: "",
  });

  // Patient search state
  const [patientSearch, setPatientSearch] = useState("");
  const [showPatientDropdown, setShowPatientDropdown] = useState(false);
  const [selectedPatient, setSelectedPatient] = useState<PatientResponse | null>(null);
  const [highlightedIndex, setHighlightedIndex] = useState(0);
  const [searchLoading, setSearchLoading] = useState(false);
  const [filteredPatients, setFilteredPatients] = useState<PatientResponse[]>([]);
  const patientInputRef = useRef<HTMLInputElement>(null);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Debounce search query (300ms delay)
  const debouncedSearch = useDebounce(patientSearch, 300);

  // Get client name helper - uses clients cache
  const getClientName = useCallback((clientId: string | undefined) => {
    if (!clientId) return '';
    const client = clients.find(c => c.id === clientId);
    return client ? `${client.firstName} ${client.lastName}` : '';
  }, [clients]);

  // Fetch clients for name display and veterinarians
  useEffect(() => {
    if (open) {
      api.getClients().then(setClients).catch(() => { /* Silently fail */ });
      api.getVeterinarians({ active: true }).then(setVeterinarians).catch(() => { /* Silently fail */ });
    }
  }, [open]);

  // Search patients on backend when debounced search changes
  useEffect(() => {
    if (!open) return;

    const searchPatients = async () => {
      setSearchLoading(true);
      try {
        // Use backend search with 'q' parameter for diacritic-insensitive search
        const results = await api.getPatients(undefined, debouncedSearch || undefined);
        setFilteredPatients(results);
      } catch {
        setFilteredPatients([]);
      } finally {
        setSearchLoading(false);
      }
    };

    searchPatients();
  }, [open, debouncedSearch]);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node) &&
        patientInputRef.current &&
        !patientInputRef.current.contains(event.target as Node)
      ) {
        setShowPatientDropdown(false);
      }
    };
    if (showPatientDropdown) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
    }
  }, [showPatientDropdown]);

  // Handle patient selection
  const handleSelectPatient = (patient: PatientResponse) => {
    setSelectedPatient(patient);
    const clientName = getClientName(patient.ownerId);
    setFormData({
      ...formData,
      patientId: patient.id,
      patientName: patient.name,
      clientId: patient.ownerId,
      clientName: clientName || undefined,
    });
    setPatientSearch(`${patient.name} (${clientName})`);
    setShowPatientDropdown(false);
  };

  // Handle keyboard navigation
  const handlePatientKeyDown = (e: React.KeyboardEvent) => {
    if (!showPatientDropdown) {
      if (e.key === "ArrowDown" || e.key === "Enter") {
        setShowPatientDropdown(true);
      }
      return;
    }
    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setHighlightedIndex(prev => Math.min(prev + 1, filteredPatients.length - 1));
        break;
      case "ArrowUp":
        e.preventDefault();
        setHighlightedIndex(prev => Math.max(prev - 1, 0));
        break;
      case "Enter":
        e.preventDefault();
        if (filteredPatients[highlightedIndex]) {
          handleSelectPatient(filteredPatients[highlightedIndex]);
        }
        break;
      case "Escape":
        setShowPatientDropdown(false);
        break;
    }
  };

  // Reset form and patient selection when modal opens
  useEffect(() => {
    if (open) {
      const dateToUse = initialDate || new Date();
      // Reset patient selection when modal opens
      setPatientSearch("");
      setSelectedPatient(null);
      setFilteredPatients([]);
      setShowPatientDropdown(false);
      setFormData({
        patientId: "",
        visitDate: formatDateTimeLocal(dateToUse, initialHour, initialMinute),
        durationMinutes: 30,
        visitType: "CONSULTATION",
        reason: "",
        veterinarianId: "",
        veterinarianName: "",
      });
    }
  }, [open, initialDate, initialHour, initialMinute]);

  // Set initial veterinarian when modal opens with veterinarian selected
  useEffect(() => {
    if (open && initialVeterinarianId && veterinarians.length > 0) {
      const selectedVet = veterinarians.find((v) => v.id === initialVeterinarianId);
      if (selectedVet) {
        setFormData((prev) => ({
          ...prev,
          veterinarianId: initialVeterinarianId,
          veterinarianName: selectedVet.fullName,
        }));
      }
    }
  }, [open, initialVeterinarianId, veterinarians]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.patientId) {
      showError(t('errors.pleaseSelectPatient'));
      return;
    }
    if (!formData.visitDate) {
      showError(t('errors.pleaseSelectDateTime'));
      return;
    }
    if (!formData.veterinarianId) {
      showError(t('errors.pleaseSelectVeterinarian'));
      return;
    }

    setLoading(true);
    try {
      // Send datetime as-is without UTC conversion (backend uses LocalDateTime)
      await api.createVisit({
        ...formData,
        visitDate: formData.visitDate + ":00",
        status: "SCHEDULED",
      });
      setFormData({
        patientId: "",
        visitDate: "",
        durationMinutes: 30,
        visitType: "CONSULTATION",
        reason: "",
        veterinarianId: "",
        veterinarianName: "",
      });
      onSuccess();
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : t('errors.failedToSave');
      if (typeof err === "object" && err !== null && "detail" in err) {
        showError((err as { detail: string }).detail);
      } else {
        showError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFormData({
      patientId: "",
      visitDate: "",
      durationMinutes: 30,
      visitType: "CONSULTATION",
      reason: "",
      veterinarianId: "",
      veterinarianName: "",
    });
    setPatientSearch("");
    setSelectedPatient(null);
    setShowPatientDropdown(false);
    onClose();
  };

  // Get the selected veterinarian name for the title
  const selectedVetForTitle = initialVeterinarianId
    ? veterinarians.find(v => v.id === initialVeterinarianId)
    : null;

  return (
    <Modal open={open} onClose={handleClose}>
      <ModalTitle>
        {t('visits.bookAppointmentTitle')}
        {selectedVetForTitle && (
          <Text variant="muted" size="sm" style={{ marginTop: spacing.xs, fontWeight: fontWeight.normal }}>
            Dr. {selectedVetForTitle.fullName}
          </Text>
        )}
      </ModalTitle>
      <form onSubmit={handleSubmit}>
        <FormField label={t('visits.patient')} required>
          <div style={{ position: 'relative' }}>
            <Input
              ref={patientInputRef}
              type="text"
              value={patientSearch}
              onChange={(e) => {
                setPatientSearch(e.target.value);
                setShowPatientDropdown(true);
                setHighlightedIndex(0);
                // Clear selection if user types something different
                if (selectedPatient && e.target.value !== `${selectedPatient.name} (${getClientName(selectedPatient.ownerId)})`) {
                  setSelectedPatient(null);
                  setFormData({ ...formData, patientId: "" });
                }
              }}
              onFocus={() => setShowPatientDropdown(true)}
              onKeyDown={handlePatientKeyDown}
              placeholder={t('visits.searchPatientPlaceholder')}
              style={{
                borderColor: selectedPatient ? colors.success.main : undefined,
              }}
            />
            {selectedPatient && (
              <span
                style={{
                  position: 'absolute',
                  right: spacing.sm,
                  top: '50%',
                  transform: 'translateY(-50%)',
                  color: colors.success.main,
                  fontSize: fontSize.lg,
                }}
              >
                ✓
              </span>
            )}

            {/* Dropdown */}
            {showPatientDropdown && (
              <div
                ref={dropdownRef}
                style={{
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  right: 0,
                  backgroundColor: colors.neutral.white,
                  border: `1px solid ${colors.neutral.border}`,
                  borderRadius: borderRadius.sm,
                  boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                  maxHeight: '250px',
                  overflowY: 'auto',
                  zIndex: zIndex.dropdown,
                  marginTop: '2px',
                }}
              >
                {searchLoading ? (
                  <div style={{ padding: spacing.md, textAlign: 'center' }}>
                    <Text variant="muted" size="sm">{t('common.searching')}</Text>
                  </div>
                ) : filteredPatients.length === 0 ? (
                  <div style={{ padding: spacing.md, textAlign: 'center' }}>
                    <Text variant="muted" size="sm">{t('common.noData')}</Text>
                  </div>
                ) : (
                  filteredPatients.slice(0, 50).map((patient, index) => {
                    const clientName = getClientName(patient.ownerId);
                    return (
                      <div
                        key={patient.id}
                        onClick={() => handleSelectPatient(patient)}
                        onMouseEnter={() => setHighlightedIndex(index)}
                        style={{
                          padding: spacing.sm,
                          cursor: 'pointer',
                          backgroundColor: index === highlightedIndex ? colors.primary.light : 'transparent',
                          borderBottom: `1px solid ${colors.neutral.borderLight}`,
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <div>
                            <Text style={{ fontWeight: fontWeight.medium }}>
                              🐾 {patient.name}
                            </Text>
                            <Text variant="muted" size="sm">
                              {patient.species}{patient.breed ? ` • ${patient.breed}` : ''}
                            </Text>
                          </div>
                          {clientName && (
                            <div style={{ textAlign: 'right' }}>
                              <Text variant="muted" size="sm">
                                👤 {clientName}
                              </Text>
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            )}
          </div>
        </FormField>

        <FormField label={t('visits.dateTime')} required>
          <Input
            type="datetime-local"
            lang="en-GB"
            value={formData.visitDate}
            onChange={(e) => setFormData({ ...formData, visitDate: e.target.value })}
            required
          />
        </FormField>

        <FormField label={t('visits.durationMinutes')}>
          <Select
            value={String(formData.durationMinutes || 30)}
            onChange={(e) =>
              setFormData({ ...formData, durationMinutes: parseInt(e.target.value, 10) })
            }
          >
            <option value="15">15 min</option>
            <option value="30">30 min</option>
            <option value="45">45 min</option>
            <option value="60">60 min</option>
            <option value="90">90 min</option>
          </Select>
        </FormField>

        <FormField label={t('visits.veterinarian')} required>
          <Select
            value={formData.veterinarianId || ""}
            onChange={(e) => {
              const selectedVet = veterinarians.find((v) => v.id === e.target.value);
              setFormData({
                ...formData,
                veterinarianId: e.target.value,
                veterinarianName: selectedVet?.fullName || "",
              });
            }}
            required
          >
            <option value="">{t('visits.selectVeterinarian')}</option>
            {veterinarians.map((vet) => (
              <option key={vet.id} value={vet.id}>
                Dr. {vet.fullName} - {vet.specialization || t('doctors.generalPractice')}
              </option>
            ))}
          </Select>
        </FormField>

        <FormField label={t('visits.visitType')}>
          <Select
            value={formData.visitType || "CONSULTATION"}
            onChange={(e) => setFormData({ ...formData, visitType: e.target.value as VisitType })}
          >
            {Object.entries(VISIT_TYPE_CONFIG).map(([key, config]) => (
              <option key={key} value={key}>
                {config.icon} {t(`visitTypes.${key}`)}
              </option>
            ))}
          </Select>
        </FormField>

        <FormField label={t('visits.reason')}>
          <TextArea
            value={formData.reason || ""}
            onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
            rows={3}
            placeholder={t('visits.reasonPlaceholder')}
          />
        </FormField>

        <ModalActions>
          <Button type="button" variant="ghost" onClick={handleClose} disabled={loading}>
            {t('common.cancel')}
          </Button>
          <Button type="submit" variant="primary" disabled={loading}>
            {loading ? t('visits.booking') : t('visits.book')}
          </Button>
        </ModalActions>
      </form>
    </Modal>
  );
}
