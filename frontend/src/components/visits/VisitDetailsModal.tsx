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
import { useState, useEffect } from 'react';
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
} from '../ui';
import { MaterialsSelector } from '../materials';
import { VISIT_TYPE_OPTIONS, getVisitTypeInfo } from '../../constants';
import { getVisitStatusConfig } from '../../constants/visitStatus';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { useI18n } from '../../i18n';
import type { VisitResponse, VisitRequest, VisitStatus, VisitType } from '../../api/types';

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

interface VisitDetailsModalProps {
  visit: VisitResponse | null;
  open: boolean;
  onClose: () => void;
  onSave: (data: Partial<VisitRequest>) => Promise<void>;
  onStatusChange: (status: VisitStatus) => Promise<void>;
  onDelete: () => Promise<void>;
}

export function VisitDetailsModal({
  visit,
  open,
  onClose,
  onSave,
  onStatusChange,
  onDelete,
}: VisitDetailsModalProps) {
  const { t } = useI18n();
  const [isSaving, setIsSaving] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);
  const [activeTab, setActiveTab] = useState('interview');

  // Form state
  const [formData, setFormData] = useState<Partial<VisitRequest>>({});

  // Initialize form data when visit changes
  useEffect(() => {
    if (visit) {
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
  }, [visit]);

  // Reset when modal closes
  useEffect(() => {
    if (!open) {
      setActiveTab('interview');
      setHasChanges(false);
    }
  }, [open]);

  // Helper to update form and mark as changed
  const updateField = <K extends keyof Partial<VisitRequest>>(field: K, value: Partial<VisitRequest>[K]) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    setHasChanges(true);
  };

  if (!visit) return null;

  const visitTypeInfo = getVisitTypeInfo(visit.visitType || 'CONSULTATION');
  const statusConfig = getVisitStatusConfig(visit.status);

  const handleSave = async () => {
    if (!hasChanges) return;
    setIsSaving(true);
    try {
      await onSave(formData);
      setHasChanges(false);
    } finally {
      setIsSaving(false);
    }
  };

  const visitDate = new Date(visit.visitDate);
  const formattedDate = visitDate.toLocaleDateString('pl-PL');
  const formattedTime = visitDate.toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' });

  return (
    <Modal open={open} onClose={onClose} maxWidth="900px">
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
        </div>
      </ModalHeader>

      {/* Tabs */}
      <ModalContent>
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
                    step="0.1"
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
                <Input
                  type="date"
                  value={formData.nextVisitDate || ''}
                  onChange={(e) => updateField('nextVisitDate', e.target.value)}
                />
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
      </ModalContent>

      {/* Actions */}
      <ModalActions>
        {/* Status Change Buttons */}
        {visit.status === 'SCHEDULED' && (
          <Button
            variant="success"
            size="sm"
            onClick={() => onStatusChange('IN_PROGRESS')}
          >
            {t('visits.startVisit')}
          </Button>
        )}
        {visit.status === 'IN_PROGRESS' && (
          <Button
            variant="success"
            size="sm"
            onClick={() => onStatusChange('COMPLETED')}
          >
            {t('visits.completeVisit')}
          </Button>
        )}
        {visit.status !== 'CANCELLED' && (
          <Button
            variant="danger"
            size="sm"
            onClick={() => {
              if (window.confirm(t('visits.confirmCancelVisit'))) {
                onStatusChange('CANCELLED');
              }
            }}
          >
            {t('visits.cancelVisit')}
          </Button>
        )}
        <Button
          variant="danger"
          size="sm"
          onClick={() => {
            if (window.confirm(t('visits.confirmDeleteVisit'))) {
              onDelete();
            }
          }}
        >
          {t('common.delete')}
        </Button>
        <div style={{ flex: 1 }} />
        <Button variant="ghost" onClick={onClose}>
          {t('common.close')}
        </Button>
        <Button
          variant="primary"
          onClick={handleSave}
          disabled={!hasChanges || isSaving}
        >
          {isSaving ? t('visits.saving') : t('visits.saveChanges')}
        </Button>
      </ModalActions>
    </Modal>
  );
}
