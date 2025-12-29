import { useState, useEffect } from "react";
import { api, VisitRequest, PatientResponse, VeterinarianResponse } from "../../api";
import {
  Button,
  Modal,
  ModalTitle,
  ModalActions,
  FormField,
  Input,
  Select,
  TextArea,
  useToast,
} from "../ui";
import { useI18n } from "../../i18n";

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
  const [patients, setPatients] = useState<PatientResponse[]>([]);
  const [veterinarians, setVeterinarians] = useState<VeterinarianResponse[]>([]);
  const [formData, setFormData] = useState<VisitRequest>({
    patientId: "",
    visitDate: "",
    durationMinutes: 30,
    reason: "",
    veterinarianId: "",
    veterinarianName: "",
  });

  // Fetch patients and veterinarians for selection
  useEffect(() => {
    if (open) {
      api.getPatients().then(setPatients).catch(() => { /* Silently fail */ });
      api.getVeterinarians({ active: true }).then(setVeterinarians).catch(() => { /* Silently fail */ });
    }
  }, [open]);

  // Set initial date/time and veterinarian when modal opens
  useEffect(() => {
    if (open) {
      const dateToUse = initialDate || new Date();
      setFormData((prev) => ({
        ...prev,
        visitDate: formatDateTimeLocal(dateToUse, initialHour, initialMinute),
      }));
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
      reason: "",
      veterinarianId: "",
      veterinarianName: "",
    });
    onClose();
  };

  return (
    <Modal open={open} onClose={handleClose}>
      <ModalTitle>{t('visits.bookAppointment')}</ModalTitle>
      <form onSubmit={handleSubmit}>
        <FormField label={t('visits.patient')} required>
          <Select
            value={formData.patientId}
            onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
            required
          >
            <option value="">{t('visits.selectPatient')}</option>
            {patients.map((patient) => (
              <option key={patient.id} value={patient.id}>
                {patient.name} ({patient.species})
              </option>
            ))}
          </Select>
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
