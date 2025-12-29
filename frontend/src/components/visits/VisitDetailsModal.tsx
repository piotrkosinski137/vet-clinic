/**
 * VisitDetailsModal - Comprehensive visit workflow component with 5 tabs
 *
 * This modal provides a complete visit management interface with tabs for:
 * 1. Interview (Wywiad) - Owner's observations and concerns
 * 2. Examination (Badanie) - Clinical examination, vitals, weight, temperature
 * 3. Diagnostics (Diagnostyka) - Lab results and diagnostic findings
 * 4. Recommendations (Zalecenia) - Treatment plan, recommendations, follow-up
 * 5. Billing (Rozliczenie) - Materials/services used with cost tracking
 *
 * @example
 * ```tsx
 * import { VisitDetailsModal } from '../components/visits';
 *
 * function MyComponent() {
 *   const [selectedVisit, setSelectedVisit] = useState<VisitResponse | null>(null);
 *   const { updateVisit, updateVisitStatus, deleteVisit } = useVisits();
 *
 *   return (
 *     <VisitDetailsModal
 *       visit={selectedVisit}
 *       open={!!selectedVisit}
 *       onClose={() => setSelectedVisit(null)}
 *       onSave={async (data) => {
 *         await updateVisit(selectedVisit!.id, data);
 *         setSelectedVisit({ ...selectedVisit!, ...data });
 *       }}
 *       onStatusChange={async (status) => {
 *         const updated = await updateVisitStatus(selectedVisit!.id, status);
 *         setSelectedVisit(updated);
 *       }}
 *       onDelete={async () => {
 *         await deleteVisit(selectedVisit!.id);
 *         setSelectedVisit(null);
 *       }}
 *     />
 *   );
 * }
 * ```
 */
import { useState, useEffect, useCallback } from 'react';
import {
  Modal,
  ModalHeader,
  ModalContent,
  ModalActions,
  Button,
  Badge,
  TextArea,
  Input,
  Select,
  Text,
  Tabs,
  TabList,
  Tab,
  TabPanel,
  ConfirmDialog,
  Loading,
} from '../ui';
import { MaterialsSelector } from '../materials';
import { VISIT_TYPE_OPTIONS, getVisitTypeInfo, getSpeciesInfo, LOCALE, DATE_FORMAT_OPTIONS, getLabelInfo } from '../../constants';
import { getVisitStatusConfig } from '../../constants/visitStatus';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { useI18n } from '../../i18n';
import { useVisitDraft } from '../../hooks';
import { formatRelativeTime } from '../../utils/dateFormatting';
import { formatWeightDisplay, formatTemperatureDisplay } from '../../utils/formatting';
import { api } from '../../api';
import type { VisitResponse, VisitRequest, VisitStatus, VisitType, VisitPriority, CheckInRequest, VisitDraftDto, PatientResponse, ClientResponse } from '../../api/types';

// Type guard for VisitType
const isVisitType = (value: string): value is VisitType => {
  const validTypes: VisitType[] = [
    'CONSULTATION',
    'VACCINATION',
    'LAB_WORK',
    'ULTRASOUND',
    'CARDIOLOGY',
    'SURGERY',
    'DENTAL',
    'GROOMING',
    'EMERGENCY',
    'FOLLOW_UP',
    'CHECKUP'
  ];
  return validTypes.includes(value as VisitType);
};

// View modes based on visit status
type ViewMode = 'pre-start' | 'active' | 'completed';

const getViewMode = (status: VisitStatus): ViewMode => {
  switch (status) {
    case 'IN_PROGRESS':
      return 'active';
    case 'COMPLETED':
      return 'completed';
    default:
      return 'pre-start';
  }
};

interface FollowUpVisitData {
  patientId: string;
  patientName?: string;
  clientId?: string;
  clientName?: string;
  veterinarianId?: string;
  veterinarianName?: string;
  visitDate: string;
  visitType: VisitType;
  reason: string;
  previousVisitId: string;
}

interface VisitDetailsModalProps {
  visit: VisitResponse | null;
  open: boolean;
  onClose: () => void;
  onSave: (data: Partial<VisitRequest>) => Promise<void>;
  onStatusChange: (status: VisitStatus) => Promise<void>;
  onDelete: () => void;
  onCheckIn?: (request?: CheckInRequest) => Promise<void>;
  onBookFollowUp?: (data: FollowUpVisitData) => void;
}

export function VisitDetailsModal({
  visit,
  open,
  onClose,
  onSave,
  onStatusChange,
  onDelete,
  onCheckIn,
  onBookFollowUp,
}: VisitDetailsModalProps) {
  const { t } = useI18n();
  const [isSaving, setIsSaving] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);
  const [activeTab, setActiveTab] = useState('interview');
  const [showCancelConfirm, setShowCancelConfirm] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showCheckInDialog, setShowCheckInDialog] = useState(false);
  const [checkInData, setCheckInData] = useState<CheckInRequest>({ priority: 'NORMAL' });
  const [isCheckingIn, setIsCheckingIn] = useState(false);
  const [isLoadingDraft, setIsLoadingDraft] = useState(false);
  const [draftRestored, setDraftRestored] = useState(false);

  // Patient and client data for pre-start and completed views
  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [client, setClient] = useState<ClientResponse | null>(null);
  const [patientHistory, setPatientHistory] = useState<VisitResponse[]>([]);
  const [loadingPatientData, setLoadingPatientData] = useState(false);

  // Form state
  const [formData, setFormData] = useState<Partial<VisitRequest>>({});

  // Draft auto-save hook
  const {
    saveDraft,
    loadDraft,
    flushDraft,
    clearPending,
    isSaving: isSavingDraft,
    lastSaved: draftLastSaved,
    error: draftError,
  } = useVisitDraft(visit?.id);

  // Helper to create draft DTO from form data
  const createDraftDto = useCallback((data: Partial<VisitRequest>): VisitDraftDto => ({
    visitType: data.visitType,
    interview: data.interview,
    examination: data.examination,
    diagnosis: data.diagnosis,
    treatment: data.treatment,
    recommendations: data.recommendations,
    weight: data.weight,
    temperature: data.temperature,
    nextVisitDate: data.nextVisitDate,
    usedMaterials: data.usedMaterials,
  }), []);

  // Initialize form data and load draft when modal opens
  useEffect(() => {
    if (open && visit) {
      setDraftRestored(false);

      // Only load drafts for IN_PROGRESS visits
      if (visit.status === 'IN_PROGRESS') {
        setIsLoadingDraft(true);
        loadDraft().then(draft => {
          if (draft && draft.savedAt) {
            // Draft exists - merge with visit data (draft takes priority for non-null values)
            setFormData({
              visitType: draft.visitType ?? visit.visitType,
              interview: draft.interview ?? visit.interview ?? '',
              examination: draft.examination ?? visit.examination ?? '',
              diagnosis: draft.diagnosis ?? visit.diagnosis ?? '',
              treatment: draft.treatment ?? visit.treatment ?? '',
              recommendations: draft.recommendations ?? visit.recommendations ?? '',
              weight: draft.weight ?? visit.weight,
              temperature: draft.temperature ?? visit.temperature,
              nextVisitDate: draft.nextVisitDate ?? visit.nextVisitDate,
              usedMaterials: draft.usedMaterials ?? visit.usedMaterials ?? [],
            });
            setHasChanges(true); // Mark as changed since draft differs from saved
            setDraftRestored(true);
          } else {
            // No draft - use visit data
            setFormData({
              visitType: visit.visitType,
              interview: visit.interview || '',
              examination: visit.examination || '',
              diagnosis: visit.diagnosis || '',
              treatment: visit.treatment || '',
              recommendations: visit.recommendations || '',
              weight: visit.weight,
              temperature: visit.temperature,
              nextVisitDate: visit.nextVisitDate,
              usedMaterials: visit.usedMaterials || [],
            });
            setHasChanges(false);
          }
          setIsLoadingDraft(false);
        });
      } else {
        // For non-IN_PROGRESS visits, just load visit data (no drafts)
        setFormData({
          visitType: visit.visitType,
          interview: visit.interview || '',
          examination: visit.examination || '',
          diagnosis: visit.diagnosis || '',
          treatment: visit.treatment || '',
          recommendations: visit.recommendations || '',
          weight: visit.weight,
          temperature: visit.temperature,
          nextVisitDate: visit.nextVisitDate,
          usedMaterials: visit.usedMaterials || [],
        });
        setHasChanges(false);
        setIsLoadingDraft(false);
      }
    }
  }, [open, visit, loadDraft]);

  // Reset when modal closes
  useEffect(() => {
    if (!open) {
      setActiveTab('interview');
      setHasChanges(false);
      setDraftRestored(false);
      clearPending();
      setPatient(null);
      setClient(null);
      setPatientHistory([]);
    }
  }, [open, clearPending]);

  // Fetch patient data, client data, and visit history for pre-start and completed views
  useEffect(() => {
    if (open && visit?.patientId) {
      const loadPatientData = async () => {
        setLoadingPatientData(true);
        try {
          const [patientData, historyData] = await Promise.all([
            api.getPatient(visit.patientId),
            api.getPatientVisits(visit.patientId),
          ]);
          setPatient(patientData);
          // Filter out current visit and sort by date descending (most recent first)
          setPatientHistory(
            historyData
              .filter(v => v.id !== visit.id)
              .sort((a, b) => new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime())
          );
          // Fetch client data if patient has an owner
          if (patientData.ownerId) {
            const clientData = await api.getClient(patientData.ownerId);
            setClient(clientData);
          }
        } catch (err) {
          console.error('Failed to load patient data:', err);
        } finally {
          setLoadingPatientData(false);
        }
      };
      loadPatientData();
    }
  }, [open, visit?.patientId, visit?.id]);

  // Helper to update form, mark as changed, and trigger draft save (only for IN_PROGRESS)
  const updateField = useCallback(<K extends keyof Partial<VisitRequest>>(field: K, value: Partial<VisitRequest>[K]) => {
    setFormData(prev => {
      const newData = { ...prev, [field]: value };
      // Only save drafts for IN_PROGRESS visits
      if (visit?.status === 'IN_PROGRESS') {
        saveDraft(createDraftDto(newData));
      }
      return newData;
    });
    setHasChanges(true);
  }, [saveDraft, createDraftDto, visit?.status]);

  if (!visit) return null;

  const viewMode = getViewMode(visit.status);

  const visitTypeInfo = getVisitTypeInfo(visit.visitType || 'CONSULTATION');
  const statusConfig = getVisitStatusConfig(visit.status);

  const handleSave = async () => {
    if (!hasChanges) return;
    setIsSaving(true);
    try {
      await onSave(formData);
      setHasChanges(false);
      clearPending(); // Clear any pending draft save since data is now committed
    } finally {
      setIsSaving(false);
    }
  };

  // Save data first, then change status to COMPLETED
  const handleCompleteVisit = async () => {
    setIsSaving(true);
    try {
      // Always save current form data before completing
      await onSave(formData);
      setHasChanges(false);
      clearPending(); // Clear any pending draft save
      // Then change status to COMPLETED
      await onStatusChange('COMPLETED');
    } finally {
      setIsSaving(false);
    }
  };

  // Flush draft before closing modal
  const handleClose = async () => {
    await flushDraft();
    onClose();
  };

  const visitDate = new Date(visit.visitDate);
  const formattedDate = visitDate.toLocaleDateString(LOCALE.PL);
  const formattedTime = visitDate.toLocaleTimeString(LOCALE.PL, DATE_FORMAT_OPTIONS.TIME_SHORT);

  // Check if visit is in progress for styling
  const isInProgress = visit.status === 'IN_PROGRESS';

  return (
  <>
    <Modal
      open={open}
      onClose={handleClose}
      maxWidth="900px"
      style={isInProgress ? {
        border: `3px solid ${colors.warning.main}`,
        boxShadow: `0 0 20px ${colors.warning.main}40`,
      } : undefined}
    >
      {/* In Progress Banner */}
      {isInProgress && (
        <div style={{
          backgroundColor: colors.warning.main,
          color: colors.neutral.white,
          padding: `${spacing.sm} ${spacing.md}`,
          textAlign: 'center',
          fontWeight: fontWeight.semibold,
          fontSize: fontSize.sm,
        }}>
          🔄 {t('visits.inProgressBanner')}
        </div>
      )}
      {/* Draft Restored Banner */}
      {draftRestored && !isLoadingDraft && (
        <div style={{
          backgroundColor: colors.info.light,
          color: colors.info.main,
          padding: `${spacing.sm} ${spacing.md}`,
          textAlign: 'center',
          fontSize: fontSize.sm,
          borderBottom: `1px solid ${colors.info.main}`,
        }}>
          📝 {t('visits.draftRestored')}
        </div>
      )}
      {/* Header */}
      <ModalHeader>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: spacing.md }}>
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.xs }}>
              <span style={{ fontSize: fontSize.xl }}>{visitTypeInfo.icon}</span>
              <Select
                value={formData.visitType || 'CONSULTATION'}
                onChange={(e) => {
                  const value = e.target.value;
                  if (isVisitType(value)) {
                    updateField('visitType', value);
                  }
                }}
                style={{ width: '200px', fontSize: fontSize.md, fontWeight: fontWeight.semibold }}
              >
                {VISIT_TYPE_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.icon} {option.label}
                  </option>
                ))}
              </Select>
              <Badge
                style={{
                  backgroundColor: statusConfig.bgColor,
                  color: statusConfig.color,
                }}
              >
                {statusConfig.label}
              </Badge>
            </div>
            <Text variant="muted" size="sm">
              {formattedDate} at {formattedTime}
              {visit.veterinarianName && ` • Dr. ${visit.veterinarianName}`}
            </Text>
          </div>
          {/* Draft save indicator */}
          <div style={{ textAlign: 'right', minWidth: '120px' }}>
            {isSavingDraft && (
              <Text variant="muted" size="sm" style={{ display: 'flex', alignItems: 'center', gap: spacing.xs }}>
                <span style={{ display: 'inline-block', width: '8px', height: '8px', borderRadius: '50%', backgroundColor: colors.warning.main, animation: 'pulse 1s infinite' }} />
                {t('visits.savingDraft')}
              </Text>
            )}
            {!isSavingDraft && draftLastSaved && (
              <Text variant="muted" size="sm">
                {t('visits.draftSaved')} {formatRelativeTime(draftLastSaved)}
              </Text>
            )}
            {draftError && (
              <Text size="sm" style={{ color: colors.danger.main }}>
                {t('visits.draftError')}
              </Text>
            )}
          </div>
        </div>
      </ModalHeader>

      {/* Content based on view mode */}
      <ModalContent>
        {isLoadingDraft || loadingPatientData ? (
          <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: spacing.xl }}>
            <Loading />
          </div>
        ) : viewMode === 'pre-start' ? (
          /* Pre-start View - Patient info, reason, notes, history */
          <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
            {/* Patient Card - Compact */}
            <div style={{
              backgroundColor: colors.neutral.background,
              borderRadius: borderRadius.md,
              padding: spacing.sm,
              border: `1px solid ${colors.neutral.border}`,
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                <span style={{ fontSize: '28px' }}>
                  {patient ? getSpeciesInfo(patient.species).emoji : '🐾'}
                </span>
                <div style={{ flex: 1 }}>
                  <Text size="md" weight="bold">
                    {patient?.name || t('common.loading')}
                  </Text>
                  <Text variant="muted" size="sm">
                    {patient && `${getSpeciesInfo(patient.species).label}${patient.breed ? ` - ${patient.breed}` : ''}`}
                    {client && ` • ${t('patients.owner')}: ${client.firstName} ${client.lastName}`}
                  </Text>
                </div>
                {/* Patient Labels - inline */}
                {patient?.labels && patient.labels.length > 0 && (
                  <div style={{ display: 'flex', gap: spacing.xs, flexWrap: 'wrap' }}>
                    {patient.labels.map((label) => {
                      const labelInfo = getLabelInfo(label);
                      return (
                        <Badge
                          key={label}
                          variant={labelInfo.variant}
                          style={{ fontSize: fontSize.xs }}
                        >
                          {labelInfo.icon} {labelInfo.display}
                        </Badge>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>

            {/* Visit Reason - Compact */}
            <div>
              <Text weight="medium" size="sm" style={{ marginBottom: spacing.xs }}>
                {t('visits.reason')}
              </Text>
              <div style={{
                backgroundColor: colors.neutral.background,
                borderRadius: borderRadius.sm,
                padding: spacing.sm,
                border: `1px solid ${colors.neutral.border}`,
              }}>
                <Text size="sm">{visit.reason || t('visits.noReason')}</Text>
              </div>
            </div>

            {/* Waiting Room Notes (only for CHECKED_IN with notes) */}
            {visit.status === 'CHECKED_IN' && visit.waitingRoomNotes && (
              <div>
                <Text weight="medium" size="sm" style={{ marginBottom: spacing.xs }}>
                  {t('waitingRoom.notes')}
                </Text>
                <div style={{
                  backgroundColor: colors.warning.light,
                  borderRadius: borderRadius.sm,
                  padding: spacing.sm,
                  border: `1px solid ${colors.warning.main}`,
                }}>
                  <Text size="sm">{visit.waitingRoomNotes}</Text>
                </div>
              </div>
            )}

            {/* Veterinarian - Inline */}
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
              <Text weight="medium" size="sm">
                {t('visits.veterinarian')}:
              </Text>
              <Text size="sm">
                {visit.veterinarianName ? `Dr. ${visit.veterinarianName}` : t('visits.notAssigned')}
              </Text>
            </div>

            {/* Recent Visit History */}
            <div>
              <Text weight="medium" size="sm" style={{ marginBottom: spacing.xs }}>
                {t('visits.recentHistory')} ({patientHistory.length})
              </Text>
              {patientHistory.length === 0 ? (
                <Text variant="muted" size="sm">{t('visits.noHistory')}</Text>
              ) : (
                <div style={{
                  backgroundColor: colors.neutral.background,
                  borderRadius: borderRadius.sm,
                  border: `1px solid ${colors.neutral.border}`,
                  overflow: 'hidden',
                }}>
                  {patientHistory.slice(0, 5).map((historyVisit, index) => {
                    const historyDate = new Date(historyVisit.visitDate);
                    const historyTypeInfo = getVisitTypeInfo(historyVisit.visitType || 'CONSULTATION');
                    const historyStatusConfig = getVisitStatusConfig(historyVisit.status);
                    return (
                      <div
                        key={historyVisit.id}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: spacing.sm,
                          padding: `${spacing.xs} ${spacing.sm}`,
                          borderBottom: index < Math.min(patientHistory.length, 5) - 1 ? `1px solid ${colors.neutral.border}` : 'none',
                        }}
                      >
                        <Text variant="muted" size="xs" style={{ minWidth: '70px' }}>
                          {historyDate.toLocaleDateString(LOCALE.PL)}
                        </Text>
                        <span style={{ fontSize: fontSize.sm }}>{historyTypeInfo.icon}</span>
                        <Text size="xs" style={{ flex: 1 }}>{historyTypeInfo.label}</Text>
                        <Badge
                          style={{
                            backgroundColor: historyStatusConfig.bgColor,
                            color: historyStatusConfig.color,
                            fontSize: fontSize.xs,
                            padding: `2px ${spacing.xs}`,
                          }}
                        >
                          {historyStatusConfig.label}
                        </Badge>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => window.open(`/visit/${historyVisit.id}`, '_blank')}
                          style={{ padding: `2px ${spacing.xs}`, fontSize: fontSize.xs }}
                        >
                          {t('common.details')}
                        </Button>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Start Visit Button - Centered, not full width */}
            <div style={{ display: 'flex', justifyContent: 'center', marginTop: spacing.sm }}>
              <Button
                variant="success"
                size="md"
                onClick={() => onStatusChange('IN_PROGRESS')}
                style={{
                  padding: `${spacing.sm} ${spacing.xl}`,
                }}
              >
                ▶️ {t('visits.startVisit')}
              </Button>
            </div>
          </div>
        ) : viewMode === 'completed' ? (
          /* Completed View - Read-only medical details */
          <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
            {/* Patient Card - Compact (same as pre-start) */}
            <div style={{
              backgroundColor: colors.neutral.background,
              borderRadius: borderRadius.md,
              padding: spacing.sm,
              border: `1px solid ${colors.neutral.border}`,
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                <span style={{ fontSize: '28px' }}>
                  {patient ? getSpeciesInfo(patient.species).emoji : '🐾'}
                </span>
                <div style={{ flex: 1 }}>
                  <Text size="md" weight="bold">
                    {patient?.name || t('common.loading')}
                  </Text>
                  <Text variant="muted" size="sm">
                    {patient && `${getSpeciesInfo(patient.species).label}${patient.breed ? ` - ${patient.breed}` : ''}`}
                    {client && ` • ${t('patients.owner')}: ${client.firstName} ${client.lastName}`}
                  </Text>
                </div>
                {/* Patient Labels - inline */}
                {patient?.labels && patient.labels.length > 0 && (
                  <div style={{ display: 'flex', gap: spacing.xs, flexWrap: 'wrap' }}>
                    {patient.labels.map((label) => {
                      const labelInfo = getLabelInfo(label);
                      return (
                        <Badge
                          key={label}
                          variant={labelInfo.variant}
                          style={{ fontSize: fontSize.xs }}
                        >
                          {labelInfo.icon} {labelInfo.display}
                        </Badge>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>

            {/* Interview */}
            {formData.interview && (
              <div>
                <Text weight="medium" style={{ marginBottom: spacing.xs }}>
                  🗣️ {t('visits.tabInterview')}
                </Text>
                <div style={{
                  backgroundColor: colors.neutral.background,
                  borderRadius: borderRadius.md,
                  padding: spacing.md,
                  border: `1px solid ${colors.neutral.border}`,
                }}>
                  <Text style={{ whiteSpace: 'pre-wrap' }}>{formData.interview}</Text>
                </div>
              </div>
            )}

            {/* Examination */}
            <div>
              <Text weight="medium" style={{ marginBottom: spacing.xs }}>
                🩺 {t('visits.tabExamination')}
              </Text>
              <div style={{
                backgroundColor: colors.neutral.background,
                borderRadius: borderRadius.md,
                padding: spacing.md,
                border: `1px solid ${colors.neutral.border}`,
              }}>
                <div style={{ display: 'flex', gap: spacing.lg, marginBottom: formData.examination ? spacing.md : 0 }}>
                  {formData.weight && (
                    <div>
                      <Text variant="muted" size="sm">{t('visits.weightKg')}</Text>
                      <Text weight="medium">{formatWeightDisplay(formData.weight)}</Text>
                    </div>
                  )}
                  {formData.temperature && (
                    <div>
                      <Text variant="muted" size="sm">{t('visits.temperatureC')}</Text>
                      <Text weight="medium">{formatTemperatureDisplay(formData.temperature)}</Text>
                    </div>
                  )}
                </div>
                {formData.examination && (
                  <Text style={{ whiteSpace: 'pre-wrap' }}>{formData.examination}</Text>
                )}
                {!formData.weight && !formData.temperature && !formData.examination && (
                  <Text variant="muted">{t('visits.noExaminationNotes')}</Text>
                )}
              </div>
            </div>

            {/* Diagnosis */}
            {formData.diagnosis && (
              <div>
                <Text weight="medium" style={{ marginBottom: spacing.xs }}>
                  🧪 {t('visits.tabDiagnostics')}
                </Text>
                <div style={{
                  backgroundColor: colors.neutral.background,
                  borderRadius: borderRadius.md,
                  padding: spacing.md,
                  border: `1px solid ${colors.neutral.border}`,
                }}>
                  <Text style={{ whiteSpace: 'pre-wrap' }}>{formData.diagnosis}</Text>
                </div>
              </div>
            )}

            {/* Treatment & Recommendations */}
            {(formData.treatment || formData.recommendations) && (
              <div>
                <Text weight="medium" style={{ marginBottom: spacing.xs }}>
                  📋 {t('visits.tabRecommendations')}
                </Text>
                <div style={{
                  backgroundColor: colors.neutral.background,
                  borderRadius: borderRadius.md,
                  padding: spacing.md,
                  border: `1px solid ${colors.neutral.border}`,
                }}>
                  {formData.treatment && (
                    <div style={{ marginBottom: formData.recommendations ? spacing.md : 0 }}>
                      <Text variant="muted" size="sm" style={{ marginBottom: spacing.xs }}>{t('visits.treatmentPlan')}</Text>
                      <Text style={{ whiteSpace: 'pre-wrap' }}>{formData.treatment}</Text>
                    </div>
                  )}
                  {formData.recommendations && (
                    <div>
                      <Text variant="muted" size="sm" style={{ marginBottom: spacing.xs }}>{t('visits.recommendations')}</Text>
                      <Text style={{ whiteSpace: 'pre-wrap' }}>{formData.recommendations}</Text>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Used Materials */}
            {formData.usedMaterials && formData.usedMaterials.length > 0 && (
              <div>
                <Text weight="medium" style={{ marginBottom: spacing.xs }}>
                  💰 {t('visits.tabBilling')}
                </Text>
                <MaterialsSelector
                  value={formData.usedMaterials}
                  onChange={() => {}}
                  readOnly={true}
                />
              </div>
            )}
          </div>
        ) : (
        /* Active View - Full 5-tab form for IN_PROGRESS */
        <Tabs defaultTab={activeTab} onChange={setActiveTab}>
          <TabList>
            <Tab value="interview" icon="🗣️">
              {t('visits.tabInterview')}
            </Tab>
            <Tab value="examination" icon="🩺">
              {t('visits.tabExamination')}
            </Tab>
            <Tab value="diagnostics" icon="🧪">
              {t('visits.tabDiagnostics')}
            </Tab>
            <Tab value="recommendations" icon="📋">
              {t('visits.tabRecommendations')}
            </Tab>
            <Tab value="billing" icon="💰">
              {t('visits.tabBilling')}
            </Tab>
          </TabList>

          {/* Interview Tab */}
          <TabPanel value="interview">
            <div>
              <Text size="sm" weight="medium" style={{ marginBottom: spacing.xs }}>
                {t('visits.interview')}
              </Text>
              <Text variant="muted" size="sm" style={{ marginBottom: spacing.sm }}>
                {t('visits.interviewDescription')}
              </Text>
              <TextArea
                value={formData.interview || ''}
                onChange={(e) => updateField('interview', e.target.value)}
                placeholder={t('visits.interviewPlaceholder')}
                rows={8}
              />
            </div>
          </TabPanel>

          {/* Examination Tab */}
          <TabPanel value="examination">
            <div>
              <Text size="sm" weight="medium" style={{ marginBottom: spacing.sm }}>
                {t('visits.clinicalExamination')}
              </Text>

              {/* Weight and Temperature */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md, marginBottom: spacing.md }}>
                <div>
                  <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: fontSize.sm, fontWeight: fontWeight.medium }}>
                    {t('visits.weightKg')}
                  </label>
                  <Input
                    type="number"
                    step="0.01"
                    min="0.01"
                    max="500"
                    value={formData.weight || ''}
                    onChange={(e) => updateField('weight', parseFloat(e.target.value) || undefined)}
                    placeholder="e.g., 12.5"
                  />
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: fontSize.sm, fontWeight: fontWeight.medium }}>
                    {t('visits.temperatureC')}
                  </label>
                  <Input
                    type="number"
                    step="0.1"
                    min="30"
                    max="45"
                    value={formData.temperature || ''}
                    onChange={(e) => updateField('temperature', parseFloat(e.target.value) || undefined)}
                    placeholder="e.g., 38.5"
                  />
                </div>
              </div>

              {/* Examination Notes */}
              <div>
                <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: fontSize.sm, fontWeight: fontWeight.medium }}>
                  {t('visits.examinationNotes')}
                </label>
                <TextArea
                  value={formData.examination || ''}
                  onChange={(e) => updateField('examination', e.target.value)}
                  placeholder={t('visits.examinationPlaceholder')}
                  rows={8}
                />
              </div>
            </div>
          </TabPanel>

          {/* Diagnostics Tab */}
          <TabPanel value="diagnostics">
            <div>
              <div
                style={{
                  padding: spacing.lg,
                  backgroundColor: colors.primary.light,
                  borderRadius: borderRadius.md,
                  border: `1px dashed ${colors.primary.main}`,
                  textAlign: 'center',
                  marginBottom: spacing.md,
                }}
              >
                <span style={{ fontSize: fontSize.xl }}>🧪</span>
                <Text size="sm" variant="muted" style={{ marginTop: spacing.xs }}>
                  {t('visits.labResultsComingSoon')}
                </Text>
              </div>

              <Text size="sm" weight="medium" style={{ marginBottom: spacing.xs }}>
                {t('visits.diagnosisNotes')}
              </Text>
              <Text variant="muted" size="sm" style={{ marginBottom: spacing.sm }}>
                {t('visits.diagnosisDescription')}
              </Text>
              <TextArea
                value={formData.diagnosis || ''}
                onChange={(e) => updateField('diagnosis', e.target.value)}
                placeholder={t('visits.diagnosisPlaceholder')}
                rows={8}
              />
            </div>
          </TabPanel>

          {/* Recommendations Tab */}
          <TabPanel value="recommendations">
            <div>
              {/* Treatment */}
              <div style={{ marginBottom: spacing.md }}>
                <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: fontSize.sm, fontWeight: fontWeight.medium }}>
                  {t('visits.treatmentPlan')}
                </label>
                <TextArea
                  value={formData.treatment || ''}
                  onChange={(e) => updateField('treatment', e.target.value)}
                  placeholder={t('visits.treatmentPlaceholder')}
                  rows={5}
                />
              </div>

              {/* Recommendations */}
              <div style={{ marginBottom: spacing.md }}>
                <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: fontSize.sm, fontWeight: fontWeight.medium }}>
                  {t('visits.recommendations')}
                </label>
                <TextArea
                  value={formData.recommendations || ''}
                  onChange={(e) => updateField('recommendations', e.target.value)}
                  placeholder={t('visits.recommendationsPlaceholder')}
                  rows={5}
                />
              </div>

              {/* Next Visit Date */}
              <div>
                <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: fontSize.sm, fontWeight: fontWeight.medium }}>
                  {t('visits.nextVisitDate')}
                </label>

                {/* Shortcut buttons */}
                <div style={{ display: 'flex', gap: spacing.xs, marginBottom: spacing.sm }}>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      const visitTime = visit?.visitDate ? new Date(visit.visitDate) : new Date();
                      const nextDate = new Date();
                      nextDate.setDate(nextDate.getDate() + 7);
                      nextDate.setHours(visitTime.getHours(), visitTime.getMinutes(), 0, 0);
                      updateField('nextVisitDate', nextDate.toISOString().slice(0, 16));
                    }}
                  >
                    {t('visits.inOneWeek')}
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      const visitTime = visit?.visitDate ? new Date(visit.visitDate) : new Date();
                      const nextDate = new Date();
                      nextDate.setMonth(nextDate.getMonth() + 1);
                      nextDate.setHours(visitTime.getHours(), visitTime.getMinutes(), 0, 0);
                      updateField('nextVisitDate', nextDate.toISOString().slice(0, 16));
                    }}
                  >
                    {t('visits.inOneMonth')}
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      const visitTime = visit?.visitDate ? new Date(visit.visitDate) : new Date();
                      const nextDate = new Date();
                      nextDate.setFullYear(nextDate.getFullYear() + 1);
                      nextDate.setHours(visitTime.getHours(), visitTime.getMinutes(), 0, 0);
                      updateField('nextVisitDate', nextDate.toISOString().slice(0, 16));
                    }}
                  >
                    {t('visits.inOneYear')}
                  </Button>
                </div>

                {/* Date and Time pickers */}
                <div style={{ display: 'flex', gap: spacing.sm, marginBottom: spacing.sm }}>
                  <div style={{ flex: 1 }}>
                    <Input
                      type="date"
                      value={formData.nextVisitDate ? formData.nextVisitDate.slice(0, 10) : ''}
                      onChange={(e) => {
                        const currentTime = formData.nextVisitDate ? formData.nextVisitDate.slice(11, 16) : '10:00';
                        updateField('nextVisitDate', e.target.value ? `${e.target.value}T${currentTime}` : '');
                      }}
                    />
                  </div>
                  <div style={{ width: '120px' }}>
                    <Input
                      type="time"
                      value={formData.nextVisitDate ? formData.nextVisitDate.slice(11, 16) : ''}
                      onChange={(e) => {
                        const currentDate = formData.nextVisitDate ? formData.nextVisitDate.slice(0, 10) : new Date().toISOString().slice(0, 10);
                        updateField('nextVisitDate', e.target.value ? `${currentDate}T${e.target.value}` : '');
                      }}
                    />
                  </div>
                </div>

                {/* Book Follow-up Button */}
                {formData.nextVisitDate && onBookFollowUp && (
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => {
                      const nextVisitDateTime = new Date(formData.nextVisitDate!);
                      onBookFollowUp({
                        patientId: visit!.patientId,
                        patientName: visit!.patientName,
                        clientId: visit!.clientId,
                        clientName: visit!.clientName,
                        veterinarianId: visit!.veterinarianId,
                        veterinarianName: visit!.veterinarianName,
                        visitDate: nextVisitDateTime.toISOString(),
                        visitType: 'FOLLOW_UP',
                        reason: t('visits.followUpVisit'),
                        previousVisitId: visit!.id,
                      });
                    }}
                    style={{ width: '100%' }}
                  >
                    {t('visits.bookFollowUp')}
                  </Button>
                )}
              </div>
            </div>
          </TabPanel>

          {/* Billing Tab */}
          <TabPanel value="billing">
            <div>
              <Text size="sm" weight="medium" style={{ marginBottom: spacing.sm }}>
                {t('visits.materialsAndServices')}
              </Text>
              <MaterialsSelector
                value={formData.usedMaterials || []}
                onChange={(materials) => {
                  updateField('usedMaterials', materials);
                }}
                readOnly={false}
              />
            </div>
          </TabPanel>
        </Tabs>
        )}
      </ModalContent>

      {/* Actions based on view mode */}
      <ModalActions>
        {viewMode === 'pre-start' && (
          <>
            {/* Pre-start: Check In, Cancel, Delete, Close */}
            {visit.status === 'SCHEDULED' && onCheckIn && (
              <Button
                variant="primary"
                size="sm"
                onClick={() => {
                  setCheckInData({ priority: 'NORMAL' });
                  setShowCheckInDialog(true);
                }}
              >
                🪑 {t('visits.checkIn')}
              </Button>
            )}
            {visit.status === 'SCHEDULED' && (
              <Button
                variant="danger"
                size="sm"
                onClick={() => setShowCancelConfirm(true)}
              >
                {t('visits.cancelVisit')}
              </Button>
            )}
            <Button
              variant="danger"
              size="sm"
              onClick={() => setShowDeleteConfirm(true)}
            >
              {t('common.delete')}
            </Button>
            <div style={{ flex: 1 }} />
            <Button variant="ghost" onClick={handleClose}>
              {t('common.close')}
            </Button>
          </>
        )}

        {viewMode === 'active' && (
          <>
            {/* Active: Complete, Delete, Close */}
            <Button
              variant="success"
              size="sm"
              onClick={handleCompleteVisit}
              disabled={isSaving}
            >
              {isSaving ? t('visits.saving') : t('visits.completeVisit')}
            </Button>
            <Button
              variant="danger"
              size="sm"
              onClick={() => setShowDeleteConfirm(true)}
            >
              {t('common.delete')}
            </Button>
            <div style={{ flex: 1 }} />
            <Button variant="ghost" onClick={handleClose}>
              {t('common.close')}
            </Button>
          </>
        )}

        {viewMode === 'completed' && (
          <>
            {/* Completed: Just Close */}
            <div style={{ flex: 1 }} />
            <Button variant="ghost" onClick={handleClose}>
              {t('common.close')}
            </Button>
          </>
        )}
      </ModalActions>
    </Modal>

    {/* Confirmation Dialogs */}
    <ConfirmDialog
      open={showCancelConfirm}
      title={t('visits.cancelVisit')}
      message={t('visits.confirmCancelVisit')}
      confirmLabel={t('visits.cancelVisit')}
      onConfirm={() => {
        setShowCancelConfirm(false);
        onStatusChange('CANCELLED');
      }}
      onClose={() => setShowCancelConfirm(false)}
      variant="danger"
    />
    <ConfirmDialog
      open={showDeleteConfirm}
      title={t('common.delete')}
      message={t('visits.confirmDeleteVisit')}
      confirmLabel={t('common.delete')}
      onConfirm={() => {
        setShowDeleteConfirm(false);
        onDelete();
      }}
      onClose={() => setShowDeleteConfirm(false)}
      variant="danger"
    />

    {/* Check-In Dialog */}
    <Modal open={showCheckInDialog} onClose={() => setShowCheckInDialog(false)} maxWidth="450px">
      <ModalHeader>
        <Text style={{ fontSize: fontSize.lg, fontWeight: fontWeight.semibold }}>
          🪑 {t('visits.checkIn')}
        </Text>
      </ModalHeader>
      <ModalContent>
        <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
          <div>
            <label style={{ display: 'block', marginBottom: spacing.xs, fontWeight: fontWeight.medium }}>
              {t('waitingRoom.priority')}
            </label>
            <Select
              value={checkInData.priority || 'NORMAL'}
              onChange={(e) => setCheckInData(prev => ({ ...prev, priority: e.target.value as VisitPriority }))}
            >
              <option value="LOW">{t('waitingRoom.priorities.LOW')}</option>
              <option value="NORMAL">{t('waitingRoom.priorities.NORMAL')}</option>
              <option value="HIGH">{t('waitingRoom.priorities.HIGH')}</option>
              <option value="URGENT">{t('waitingRoom.priorities.URGENT')}</option>
            </Select>
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: spacing.xs, fontWeight: fontWeight.medium }}>
              {t('waitingRoom.notes')} ({t('common.optional')})
            </label>
            <TextArea
              value={checkInData.waitingRoomNotes || ''}
              onChange={(e) => setCheckInData(prev => ({ ...prev, waitingRoomNotes: e.target.value }))}
              placeholder={t('waitingRoom.notesPlaceholder')}
              rows={3}
            />
          </div>
        </div>
      </ModalContent>
      <ModalActions>
        <Button variant="ghost" onClick={() => setShowCheckInDialog(false)} disabled={isCheckingIn}>
          {t('common.cancel')}
        </Button>
        <Button
          variant="primary"
          onClick={async () => {
            if (!onCheckIn) return;
            setIsCheckingIn(true);
            try {
              await onCheckIn(checkInData);
              setShowCheckInDialog(false);
              onClose();
            } finally {
              setIsCheckingIn(false);
            }
          }}
          disabled={isCheckingIn}
        >
          {isCheckingIn ? t('visits.checkingIn') : t('visits.checkIn')}
        </Button>
      </ModalActions>
    </Modal>
  </>
  );
}
