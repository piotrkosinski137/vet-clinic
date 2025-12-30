import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useVeterinarians, useVisits, formatCurrency, useConfirmDialog } from "../../hooks";
import { api } from "../../api";
import type { VeterinarianResponse, VeterinarianRequest, VisitResponse, UsedMaterialDto, PatientResponse } from "../../api/types";
import {
  Button,
  Modal,
  ModalTitle,
  ModalActions,
  FormField,
  Input,
  Text,
  Badge,
  ModalLoader,
  useToast,
  ConfirmDialog,
} from "../ui";
import { MaterialsSelector, ProceduresSelector } from "../materials";
import { useI18n } from "../../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight } from "../../theme";
import { formatTimeWithLocale } from "../../utils/dateFormatting";

const PRESET_COLORS = [
  "#4CAF50", "#2196F3", "#9C27B0", "#FF9800", "#E91E63",
  "#00BCD4", "#795548", "#607D8B", "#F44336", "#3F51B5",
];

// Type guard for UsedMaterialDto
function isUsedMaterialDto(item: unknown): item is UsedMaterialDto {
  if (!item || typeof item !== 'object') return false;
  const m = item as Partial<UsedMaterialDto>;
  return (
    typeof m.materialId === 'string' &&
    typeof m.name === 'string' &&
    typeof m.quantity === 'number' &&
    typeof m.costPrice === 'number' &&
    typeof m.sellPrice === 'number' &&
    typeof m.unit === 'string'
  );
}

// Safely get used materials array from visit
function getUsedMaterials(visit: VisitResponse): UsedMaterialDto[] {
  if (!visit.usedMaterials || !Array.isArray(visit.usedMaterials)) {
    return [];
  }
  return visit.usedMaterials.filter(isUsedMaterialDto);
}

// Calculate profit from a visit (sell - cost of materials)
function calculateVisitProfit(visit: VisitResponse): number {
  const materials = getUsedMaterials(visit);
  const totalSell = materials.reduce((sum, m) => sum + (m.quantity * m.sellPrice), 0);
  const totalCost = materials.reduce((sum, m) => sum + (m.quantity * m.costPrice), 0);
  return totalSell - totalCost;
}

function calculateVisitRevenue(visit: VisitResponse): number {
  const materials = getUsedMaterials(visit);
  return materials.reduce((sum, m) => sum + (m.quantity * m.sellPrice), 0);
}

function formatTime(dateString: string, language: string): string {
  const date = new Date(dateString);
  return formatTimeWithLocale(date, { hour: "2-digit", minute: "2-digit", hour12: false }, language);
}

function formatDateForApi(date: Date): string {
  return date.toISOString().split("T")[0];
}

// Badge variant type for status display
type StatusBadgeVariant = "primary" | "warning" | "success" | "secondary";
const statusConfig: Record<string, { label: string; variant: StatusBadgeVariant; icon: string }> = {
  SCHEDULED: { label: "Scheduled", variant: "primary", icon: "📅" },
  IN_PROGRESS: { label: "In Progress", variant: "warning", icon: "🔄" },
  COMPLETED: { label: "Completed", variant: "success", icon: "✅" },
  CANCELLED: { label: "Cancelled", variant: "secondary", icon: "❌" },
};

export interface DoctorsManagementModalProps {
  open: boolean;
  onClose: () => void;
  onDoctorsChange?: () => void;
}

export function DoctorsManagementModal({
  open,
  onClose,
  onDoctorsChange,
}: DoctorsManagementModalProps) {
  const navigate = useNavigate();
  const { t, language } = useI18n();
  const { success, error: showError } = useToast();
  const { dialogState, showConfirm, closeDialog, handleConfirm } = useConfirmDialog();
  const {
    veterinarians,
    loading,
    error,
    refetch,
    createVeterinarian,
    deleteVeterinarian,
  } = useVeterinarians();
  const { updateVisitStatus, updateVisit } = useVisits();

  const [selectedDoctor, setSelectedDoctor] = useState<VeterinarianResponse | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    specialization: "",
    colorCode: PRESET_COLORS[0],
  });

  // Today's visits state
  const [todaysVisits, setTodaysVisits] = useState<VisitResponse[]>([]);
  const [loadingVisits, setLoadingVisits] = useState(false);
  const [selectedVisit, setSelectedVisit] = useState<VisitResponse | null>(null);
  const [patientsMap, setPatientsMap] = useState<Record<string, PatientResponse>>({});
  const [notesValue, setNotesValue] = useState("");
  const [materialsValue, setMaterialsValue] = useState<UsedMaterialDto[]>([]);
  const [proceduresValue, setProceduresValue] = useState<UsedMaterialDto[]>([]);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  // Fetch today's visits for the selected doctor
  const fetchTodaysVisits = useCallback(async (doctorName: string) => {
    setLoadingVisits(true);
    try {
      const today = formatDateForApi(new Date());
      const [allVisits, allPatients] = await Promise.all([
        api.getVisits({ dateFrom: today, dateTo: today }),
        api.getPatients(),
      ]);

      // Create patients lookup map
      const patientLookup: Record<string, PatientResponse> = {};
      allPatients.forEach((p) => {
        patientLookup[p.id] = p;
      });
      setPatientsMap(patientLookup);

      const doctorVisits = allVisits.filter(
        (v) => v.veterinarianName === doctorName && v.status !== "CANCELLED"
      );
      // Sort by time
      doctorVisits.sort((a, b) => new Date(a.visitDate).getTime() - new Date(b.visitDate).getTime());
      setTodaysVisits(doctorVisits);
    } catch {
      setTodaysVisits([]);
    } finally {
      setLoadingVisits(false);
    }
  }, []);

  useEffect(() => {
    if (open) {
      refetch();
      setSelectedDoctor(null);
      setShowAddForm(false);
      setTodaysVisits([]);
      setSelectedVisit(null);
      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        specialization: "",
        colorCode: PRESET_COLORS[0],
      });
    }
  }, [open, refetch]);

  // Fetch visits when doctor is selected
  useEffect(() => {
    if (selectedDoctor) {
      fetchTodaysVisits(selectedDoctor.fullName);
    }
  }, [selectedDoctor, fetchTodaysVisits]);

  const handleAdd = async () => {
    if (!formData.firstName.trim()) {
      showError("Please enter a first name");
      return;
    }
    if (!formData.lastName.trim()) {
      showError("Please enter a last name");
      return;
    }
    if (!formData.email.trim()) {
      showError("Please enter an email");
      return;
    }
    if (!formData.specialization.trim()) {
      showError("Please enter a specialization");
      return;
    }

    setSubmitting(true);
    try {
      const request: VeterinarianRequest = {
        firstName: formData.firstName.trim(),
        lastName: formData.lastName.trim(),
        email: formData.email.trim(),
        specialization: formData.specialization.trim(),
        colorCode: formData.colorCode,
        active: true,
      };

      await createVeterinarian(request);
      setFormData({
        firstName: "",
        lastName: "",
        email: "",
        specialization: "",
        colorCode: PRESET_COLORS[0],
      });
      setShowAddForm(false);
      success(t('success.doctorAdded'));
      onDoctorsChange?.();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (doctor: VeterinarianResponse) => {
    showConfirm(
      t('common.confirm'),
      t('doctors.confirmRemove', { name: doctor.fullName }),
      async () => {
        try {
          await deleteVeterinarian(doctor.id);
          if (selectedDoctor?.id === doctor.id) {
            setSelectedDoctor(null);
          }
          success(t('success.doctorRemoved'));
          onDoctorsChange?.();
        } catch (err) {
          showError(err instanceof Error ? err.message : t('errors.failedToDelete'));
        }
      }
    );
  };

  const handleGoToCalendar = (doctor: VeterinarianResponse) => {
    onClose();
    navigate(`/schedule?doctor=${encodeURIComponent(doctor.fullName)}`);
  };

  const handleBackToList = () => {
    setSelectedDoctor(null);
    setTodaysVisits([]);
    setSelectedVisit(null);
  };

  const handleViewVisit = (visit: VisitResponse) => {
    setSelectedVisit(visit);
    setNotesValue(visit.notes || "");
    // Split usedMaterials into materials (costPrice > 0) and procedures (costPrice === 0)
    const allItems = getUsedMaterials(visit);
    setMaterialsValue(allItems.filter((m) => m.costPrice > 0));
    setProceduresValue(allItems.filter((m) => m.costPrice === 0));
    setHasUnsavedChanges(false);
  };

  const handleBackToDoctor = () => {
    setSelectedVisit(null);
    // Refresh visits in case something changed
    if (selectedDoctor) {
      fetchTodaysVisits(selectedDoctor.fullName);
    }
  };

  const handleStatusChange = async (status: "IN_PROGRESS" | "COMPLETED" | "CANCELLED") => {
    if (!selectedVisit) return;
    try {
      const updated = await updateVisitStatus(selectedVisit.id, status);
      setSelectedVisit(updated);
      success(t('success.statusUpdated'));
    } catch (err) {
      showError(t('errors.failedToSave'));
    }
  };

  const handleSaveAll = async () => {
    if (!selectedVisit) return;
    try {
      // Merge materials and procedures back together
      const allItems = [...materialsValue, ...proceduresValue];
      await updateVisit(selectedVisit.id, {
        ...selectedVisit,
        notes: notesValue,
        usedMaterials: allItems
      });
      setSelectedVisit({ ...selectedVisit, notes: notesValue, usedMaterials: allItems });
      setHasUnsavedChanges(false);
      success(t('success.changesSaved'));
    } catch (err) {
      showError(t('errors.failedToSave'));
    }
  };

  const getInitials = (doctor: VeterinarianResponse) => {
    return `${doctor.firstName[0]}${doctor.lastName[0]}`.toUpperCase();
  };

  const getDoctorColor = (doctor: VeterinarianResponse) => {
    return doctor.colorCode || PRESET_COLORS[0];
  };

  const getPatientName = (patientId: string): string => {
    const patient = patientsMap[patientId];
    return patient ? patient.name : "Unknown";
  };

  // Calculate totals for today
  const totalRevenue = todaysVisits.reduce((sum, v) => sum + calculateVisitRevenue(v), 0);
  const totalProfit = todaysVisits.reduce((sum, v) => sum + calculateVisitProfit(v), 0);

  // Loading state
  if (loading && !veterinarians.length) {
    return (
      <Modal open={open} onClose={onClose}>
        <ModalTitle>{t('doctors.manageDoctors')}</ModalTitle>
        <ModalLoader text={t('common.loading')} minHeight="300px" />
      </Modal>
    );
  }

  // Error state
  if (error && !veterinarians.length) {
    return (
      <Modal open={open} onClose={onClose}>
        <ModalTitle>{t('doctors.manageDoctors')}</ModalTitle>
        <div style={{ padding: spacing.xl, textAlign: "center" }}>
          <Text style={{ color: colors.danger.main }}>{error}</Text>
          <Button variant="primary" onClick={refetch} style={{ marginTop: spacing.md }}>
            {t('errors.retry')}
          </Button>
        </div>
      </Modal>
    );
  }

  // Visit Details View
  if (selectedVisit && selectedDoctor) {
    return (
      <Modal open={open} onClose={onClose}>
        <div style={{ display: "flex", alignItems: "center", gap: spacing.sm, marginBottom: spacing.md }}>
          <button
            onClick={handleBackToDoctor}
            style={{
              background: "none",
              border: "none",
              cursor: "pointer",
              fontSize: fontSize.lg,
              padding: spacing.xs,
            }}
          >
            ←
          </button>
          <ModalTitle style={{ margin: 0 }}>{t('visits.appointmentDetails')}</ModalTitle>
          <div style={{ marginLeft: "auto" }}>
            <Badge variant={statusConfig[selectedVisit.status]?.variant || "secondary"}>
              {statusConfig[selectedVisit.status]?.icon} {statusConfig[selectedVisit.status]?.label}
            </Badge>
          </div>
        </div>

        {/* Visit Info */}
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "1fr 1fr",
            gap: spacing.sm,
            marginBottom: spacing.md,
          }}
        >
          <div style={{ padding: spacing.sm, backgroundColor: colors.primary.light, borderRadius: borderRadius.sm, borderLeft: `3px solid ${colors.primary.main}` }}>
            <Text variant="muted" size="sm">{t('visits.time')}</Text>
            <Text style={{ fontWeight: fontWeight.medium }}>{formatTime(selectedVisit.visitDate, language)}</Text>
          </div>
          <div style={{ padding: spacing.sm, backgroundColor: colors.neutral.background, borderRadius: borderRadius.sm }}>
            <Text variant="muted" size="sm">{t('visits.duration')}</Text>
            <Text style={{ fontWeight: fontWeight.medium }}>{selectedVisit.durationMinutes || 30} min</Text>
          </div>
        </div>

        {selectedVisit.reason && (
          <div style={{ padding: spacing.sm, backgroundColor: colors.warning.light, borderRadius: borderRadius.sm, borderLeft: `3px solid ${colors.warning.main}`, marginBottom: spacing.md }}>
            <Text variant="muted" size="sm">{t('visits.reason')}</Text>
            <Text>{selectedVisit.reason}</Text>
          </div>
        )}

        {/* Notes Section */}
        <div style={{ backgroundColor: colors.neutral.background, padding: spacing.md, borderRadius: borderRadius.md, marginBottom: spacing.md }}>
          <Text size="sm" style={{ fontWeight: fontWeight.semibold, marginBottom: spacing.sm, display: "block" }}>{t('visits.visitNotes')}</Text>
          <textarea
            value={notesValue}
            onChange={(e) => { setNotesValue(e.target.value); setHasUnsavedChanges(true); }}
            placeholder={t('visits.addNotesHere')}
            style={{ width: "100%", minHeight: "80px", padding: spacing.sm, borderRadius: borderRadius.sm, border: `1px solid ${colors.neutral.border}`, fontFamily: "inherit", fontSize: fontSize.sm, resize: "vertical" }}
          />
        </div>

        {/* Materials Section */}
        <div style={{ backgroundColor: colors.neutral.background, padding: spacing.md, borderRadius: borderRadius.md, marginBottom: spacing.md }}>
          <div style={{ display: "flex", alignItems: "center", gap: spacing.sm, marginBottom: spacing.sm }}>
            <Text size="sm" style={{ fontWeight: fontWeight.semibold }}>{t('materials.materialsUsed')}</Text>
            {materialsValue.length > 0 && (
              <Badge variant="success">{formatCurrency(materialsValue.reduce((sum, m) => sum + m.quantity * m.sellPrice, 0))}</Badge>
            )}
          </div>
          <MaterialsSelector value={materialsValue} onChange={(v) => { setMaterialsValue(v); setHasUnsavedChanges(true); }} />
        </div>

        {/* Procedures Section - 100% profit services */}
        <div style={{ backgroundColor: colors.success.light, padding: spacing.md, borderRadius: borderRadius.md, marginBottom: spacing.md, borderLeft: `3px solid ${colors.success.main}` }}>
          <div style={{ display: "flex", alignItems: "center", gap: spacing.sm, marginBottom: spacing.sm }}>
            <Text size="sm" style={{ fontWeight: fontWeight.semibold }}>{t('procedures.title')} ({t('procedures.subtitle')})</Text>
            {proceduresValue.length > 0 && (
              <Badge variant="success">+{formatCurrency(proceduresValue.reduce((sum, p) => sum + p.quantity * p.sellPrice, 0))}</Badge>
            )}
          </div>
          <ProceduresSelector value={proceduresValue} onChange={(v) => { setProceduresValue(v); setHasUnsavedChanges(true); }} />
        </div>

        {/* Status Actions */}
        {selectedVisit.status !== "COMPLETED" && selectedVisit.status !== "CANCELLED" && (
          <div style={{ display: "flex", gap: spacing.sm, marginBottom: spacing.md }}>
            {selectedVisit.status === "SCHEDULED" && (
              <Button variant="secondary" onClick={() => handleStatusChange("IN_PROGRESS")} style={{ flex: 1 }}>
                {t('visits.startVisit')}
              </Button>
            )}
            {selectedVisit.status === "IN_PROGRESS" && (
              <Button variant="success" onClick={() => handleStatusChange("COMPLETED")} style={{ flex: 1 }}>
                {t('visits.completeVisit')}
              </Button>
            )}
            <Button variant="danger" onClick={() => handleStatusChange("CANCELLED")} style={{ flex: 1 }}>
              {t('visits.cancelVisit')}
            </Button>
          </div>
        )}

        <ModalActions>
          <Button variant="ghost" onClick={handleBackToDoctor}>{t('common.back')}</Button>
          <Button variant="primary" onClick={handleSaveAll} disabled={!hasUnsavedChanges}>
            {t('common.saveChanges')}
          </Button>
        </ModalActions>
      </Modal>
    );
  }

  // Doctor Details View
  if (selectedDoctor) {
    return (
      <Modal open={open} onClose={onClose}>
        <div style={{ display: "flex", alignItems: "center", gap: spacing.sm, marginBottom: spacing.md }}>
          <button
            onClick={handleBackToList}
            style={{
              background: "none",
              border: "none",
              cursor: "pointer",
              fontSize: fontSize.lg,
              padding: spacing.xs,
            }}
          >
            ←
          </button>
          <ModalTitle style={{ margin: 0 }}>{t('doctors.doctorDetails')}</ModalTitle>
        </div>

        {/* Doctor Profile Card */}
        <div
          style={{
            backgroundColor: colors.neutral.background,
            borderRadius: borderRadius.md,
            padding: spacing.lg,
            marginBottom: spacing.md,
            borderTop: `4px solid ${getDoctorColor(selectedDoctor)}`,
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: spacing.md }}>
            <div
              style={{
                width: "64px",
                height: "64px",
                borderRadius: borderRadius.full,
                backgroundColor: getDoctorColor(selectedDoctor),
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: colors.neutral.white,
                fontWeight: fontWeight.bold,
                fontSize: fontSize.xl,
              }}
            >
              {getInitials(selectedDoctor)}
            </div>
            <div>
              <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold }}>
                Dr. {selectedDoctor.fullName}
              </Text>
              <Text variant="muted" size="sm">
                {selectedDoctor.specialization || t('doctors.generalPractice')}
              </Text>
            </div>
          </div>
        </div>

        {/* Today's Visits Table */}
        <div style={{ marginBottom: spacing.md }}>
          <Text style={{ fontWeight: fontWeight.semibold, marginBottom: spacing.sm }}>
            {t('visits.todaysAppointments')}
          </Text>

          {loadingVisits ? (
            <ModalLoader text={t('visits.loading')} minHeight="150px" />
          ) : todaysVisits.length === 0 ? (
            <div style={{ padding: spacing.md, textAlign: "center", backgroundColor: colors.neutral.background, borderRadius: borderRadius.md }}>
              <Text variant="muted">{t('visits.noAppointmentsToday')}</Text>
            </div>
          ) : (
            <>
              {/* Table Header */}
              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "60px 1fr 100px 100px 80px",
                  gap: spacing.sm,
                  padding: spacing.sm,
                  backgroundColor: colors.neutral.text,
                  color: colors.neutral.white,
                  borderRadius: `${borderRadius.sm} ${borderRadius.sm} 0 0`,
                  fontSize: fontSize.sm,
                  fontWeight: fontWeight.semibold,
                }}
              >
                <div>{t('visits.time')}</div>
                <div>{t('visits.patient')}</div>
                <div style={{ textAlign: "right" }}>{t('common.revenue')}</div>
                <div style={{ textAlign: "right" }}>{t('common.profit')}</div>
                <div></div>
              </div>

              {/* Table Rows */}
              {todaysVisits.map((visit) => {
                const revenue = calculateVisitRevenue(visit);
                const profit = calculateVisitProfit(visit);
                return (
                  <div
                    key={visit.id}
                    style={{
                      display: "grid",
                      gridTemplateColumns: "60px 1fr 100px 100px 80px",
                      gap: spacing.sm,
                      padding: spacing.sm,
                      backgroundColor: colors.neutral.background,
                      borderBottom: `1px solid ${colors.neutral.border}`,
                      alignItems: "center",
                      fontSize: fontSize.sm,
                    }}
                  >
                    <div style={{ fontWeight: fontWeight.medium }}>
                      {formatTime(visit.visitDate, language)}
                    </div>
                    <div>
                      <Text style={{ fontWeight: fontWeight.medium }}>{getPatientName(visit.patientId)}</Text>
                      {visit.reason && (
                        <Text variant="muted" size="sm" style={{
                          overflow: "hidden",
                          textOverflow: "ellipsis",
                          whiteSpace: "nowrap",
                          maxWidth: "200px"
                        }}>
                          {visit.reason}
                        </Text>
                      )}
                    </div>
                    <div style={{ textAlign: "right", fontWeight: fontWeight.medium }}>
                      {formatCurrency(revenue)}
                    </div>
                    <div style={{ textAlign: "right", fontWeight: fontWeight.medium, color: profit >= 0 ? colors.success.main : colors.danger.main }}>
                      {formatCurrency(profit)}
                    </div>
                    <div style={{ textAlign: "right" }}>
                      <Button variant="ghost" size="sm" onClick={() => handleViewVisit(visit)}>
                        {t('common.view')}
                      </Button>
                    </div>
                  </div>
                );
              })}

              {/* Summary Row */}
              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "60px 1fr 100px 100px 80px",
                  gap: spacing.sm,
                  padding: spacing.sm,
                  backgroundColor: colors.primary.light,
                  borderRadius: `0 0 ${borderRadius.sm} ${borderRadius.sm}`,
                  fontSize: fontSize.sm,
                  fontWeight: fontWeight.bold,
                }}
              >
                <div></div>
                <div>{t('common.total')} ({todaysVisits.length})</div>
                <div style={{ textAlign: "right" }}>{formatCurrency(totalRevenue)}</div>
                <div style={{ textAlign: "right", color: totalProfit >= 0 ? colors.success.main : colors.danger.main }}>
                  {formatCurrency(totalProfit)}
                </div>
                <div></div>
              </div>
            </>
          )}
        </div>

        {/* Actions */}
        <div style={{ display: "flex", flexDirection: "column", gap: spacing.sm }}>
          <Button
            variant="primary"
            onClick={() => handleGoToCalendar(selectedDoctor)}
            style={{ width: "100%" }}
          >
            {t('doctors.goToCalendar')}
          </Button>
          <Button
            variant="danger"
            onClick={() => handleDelete(selectedDoctor)}
            style={{ width: "100%" }}
          >
            {t('doctors.removeDoctor')}
          </Button>
        </div>

        <ModalActions>
          <Button variant="ghost" onClick={onClose}>
            {t('common.close')}
          </Button>
        </ModalActions>
      </Modal>
    );
  }

  // Doctors List View
  return (
    <Modal open={open} onClose={onClose}>
      <ModalTitle>{t('doctors.manageDoctors')}</ModalTitle>

      {/* Doctors List */}
      <div style={{ marginBottom: spacing.md }}>
        {veterinarians.length === 0 ? (
          <div style={{ textAlign: "center", padding: spacing.xl }}>
            <Text variant="muted">{t('doctors.noDoctorsYet')}</Text>
          </div>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: spacing.sm }}>
            {veterinarians.map((doctor) => (
              <div
                key={doctor.id}
                onClick={() => setSelectedDoctor(doctor)}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: spacing.sm,
                  padding: spacing.sm,
                  backgroundColor: colors.neutral.background,
                  borderRadius: borderRadius.sm,
                  borderLeft: `4px solid ${getDoctorColor(doctor)}`,
                  cursor: "pointer",
                  transition: "background-color 0.15s",
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.backgroundColor = colors.primary.light;
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = colors.neutral.background;
                }}
              >
                <div
                  style={{
                    width: "32px",
                    height: "32px",
                    borderRadius: borderRadius.full,
                    backgroundColor: getDoctorColor(doctor),
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: colors.neutral.white,
                    fontWeight: fontWeight.bold,
                    fontSize: fontSize.sm,
                  }}
                >
                  {getInitials(doctor)}
                </div>
                <div style={{ flex: 1 }}>
                  <Text style={{ fontWeight: fontWeight.medium }}>Dr. {doctor.fullName}</Text>
                  <Text variant="muted" size="sm">{doctor.specialization || t('doctors.generalPractice')}</Text>
                </div>
                <Text variant="muted" size="sm">→</Text>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Doctor Form */}
      {showAddForm ? (
        <div
          style={{
            padding: spacing.md,
            backgroundColor: colors.neutral.background,
            borderRadius: borderRadius.md,
            marginBottom: spacing.md,
          }}
        >
          <Text style={{ fontWeight: fontWeight.semibold, marginBottom: spacing.sm }}>
            {t('doctors.addDoctor')}
          </Text>

          <FormField label={t('doctors.firstName')} required>
            <Input
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              placeholder="e.g., Anna"
            />
          </FormField>

          <FormField label={t('doctors.lastName')} required>
            <Input
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              placeholder="e.g., Kowalska"
            />
          </FormField>

          <FormField label={t('doctors.email')} required>
            <Input
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              placeholder="e.g., anna.kowalska@vetclinic.com"
            />
          </FormField>

          <FormField label={t('doctors.specialization')} required>
            <Input
              value={formData.specialization}
              onChange={(e) => setFormData({ ...formData, specialization: e.target.value })}
              placeholder="e.g., Surgery, Dermatology"
            />
          </FormField>

          <FormField label={t('doctors.color')}>
            <div style={{ display: "flex", gap: spacing.xs, flexWrap: "wrap" }}>
              {PRESET_COLORS.map((color) => (
                <button
                  key={color}
                  type="button"
                  onClick={() => setFormData({ ...formData, colorCode: color })}
                  style={{
                    width: "32px",
                    height: "32px",
                    borderRadius: borderRadius.full,
                    backgroundColor: color,
                    border: formData.colorCode === color ? `3px solid ${colors.neutral.text}` : "none",
                    cursor: "pointer",
                    transition: "transform 0.15s",
                  }}
                />
              ))}
            </div>
          </FormField>

          <div style={{ display: "flex", gap: spacing.sm, marginTop: spacing.md }}>
            <Button variant="primary" onClick={handleAdd} disabled={submitting}>
              {submitting ? t('doctors.adding') : t('doctors.addDoctor')}
            </Button>
            <Button variant="ghost" onClick={() => setShowAddForm(false)} disabled={submitting}>
              {t('common.cancel')}
            </Button>
          </div>
        </div>
      ) : (
        <Button
          variant="primary"
          onClick={() => setShowAddForm(true)}
          style={{ width: "100%", marginBottom: spacing.md }}
        >
          + {t('doctors.addDoctor')}
        </Button>
      )}

      <ModalActions>
        <Button variant="ghost" onClick={onClose}>
          {t('common.close')}
        </Button>
      </ModalActions>

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
    </Modal>
  );
}
