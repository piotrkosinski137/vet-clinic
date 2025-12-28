import { useState, useCallback } from 'react';
import { useI18n } from '../../i18n';
import { colors, spacing, fontSize, fontWeight, shadows, borderRadius } from '../../theme';
import { Button } from '../ui/Button';
import { Modal, ModalTitle } from '../ui/Modal';
import { useToast } from '../ui/Toast';
import type {
  VeterinarianDayOffResponse,
  VeterinarianDayOffRequest,
  DayOffType,
} from '../../api/types';

const DAY_OFF_TYPES: DayOffType[] = ['VACATION', 'SICK_LEAVE', 'PERSONAL', 'OTHER'];

const TYPE_COLORS: Record<DayOffType, string> = {
  VACATION: colors.info.main,
  SICK_LEAVE: colors.danger.main,
  PERSONAL: colors.warning.main,
  OTHER: colors.neutral.textLight,
};

const TYPE_ICONS: Record<DayOffType, string> = {
  VACATION: '\u2708\uFE0F',
  SICK_LEAVE: '\uD83E\uDE7A',
  PERSONAL: '\uD83D\uDCC5',
  OTHER: '\uD83D\uDCCB',
};

interface DaysOffManagerProps {
  daysOff: VeterinarianDayOffResponse[];
  onAdd: (dayOff: VeterinarianDayOffRequest) => Promise<VeterinarianDayOffResponse>;
  onUpdate: (dayOffId: string, dayOff: VeterinarianDayOffRequest) => Promise<VeterinarianDayOffResponse>;
  onDelete: (dayOffId: string) => Promise<void>;
  onApprove: (dayOffId: string, approved: boolean) => Promise<VeterinarianDayOffResponse>;
}

export function DaysOffManager({
  daysOff,
  onAdd,
  onUpdate,
  onDelete,
  onApprove,
}: DaysOffManagerProps) {
  const { t } = useI18n();
  const toast = useToast();
  const [showModal, setShowModal] = useState(false);
  const [editingDayOff, setEditingDayOff] = useState<VeterinarianDayOffResponse | null>(null);

  const handleAdd = useCallback(() => {
    setEditingDayOff(null);
    setShowModal(true);
  }, []);

  const handleEdit = useCallback((dayOff: VeterinarianDayOffResponse) => {
    setEditingDayOff(dayOff);
    setShowModal(true);
  }, []);

  const handleDelete = useCallback(async (dayOffId: string) => {
    if (!confirm(t('schedule.confirmDeleteDayOff'))) return;
    try {
      await onDelete(dayOffId);
      toast.success(t('schedule.dayOffDeleted'));
    } catch (_err) {
      toast.error(t('common.error'));
    }
  }, [onDelete, t, toast]);

  const handleSave = useCallback(async (data: VeterinarianDayOffRequest) => {
    try {
      if (editingDayOff) {
        await onUpdate(editingDayOff.id, data);
        toast.success(t('schedule.dayOffUpdated'));
      } else {
        await onAdd(data);
        toast.success(t('schedule.dayOffAdded'));
      }
      setShowModal(false);
    } catch (_err) {
      toast.error(t('common.error'));
    }
  }, [editingDayOff, onAdd, onUpdate, t, toast]);

  const sortedDaysOff = [...daysOff].sort(
    (a, b) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime()
  );

  return (
    <div style={{
      backgroundColor: colors.neutral.white,
      borderRadius: borderRadius.lg,
      boxShadow: shadows.sm,
      overflow: 'hidden',
    }}>
      <div style={{
        padding: spacing.md,
        borderBottom: `1px solid ${colors.neutral.border}`,
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
      }}>
        <div style={{ fontWeight: fontWeight.semibold, fontSize: fontSize.md }}>
          {t('schedule.daysOff')}
        </div>
        <Button variant="primary" size="sm" onClick={handleAdd}>
          + {t('schedule.addDayOff')}
        </Button>
      </div>

      <div style={{ padding: spacing.md }}>
        {sortedDaysOff.length === 0 ? (
          <div style={{
            textAlign: 'center',
            padding: spacing.xl,
            color: colors.neutral.textLight,
          }}>
            {t('schedule.noDaysOff')}
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.sm }}>
            {sortedDaysOff.map(dayOff => (
              <DayOffCard
                key={dayOff.id}
                dayOff={dayOff}
                onEdit={() => handleEdit(dayOff)}
                onDelete={() => handleDelete(dayOff.id)}
                onApprove={(approved) => onApprove(dayOff.id, approved)}
              />
            ))}
          </div>
        )}
      </div>

      <DayOffModal
        open={showModal}
        dayOff={editingDayOff}
        onSave={handleSave}
        onClose={() => setShowModal(false)}
      />
    </div>
  );
}

interface DayOffCardProps {
  dayOff: VeterinarianDayOffResponse;
  onEdit: () => void;
  onDelete: () => void;
  onApprove: (approved: boolean) => void;
}

function DayOffCard({ dayOff, onEdit, onDelete, onApprove }: DayOffCardProps) {
  const { t } = useI18n();
  const isPast = new Date(dayOff.endDate) < new Date();

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const dateRange = dayOff.startDate === dayOff.endDate
    ? formatDate(dayOff.startDate)
    : `${formatDate(dayOff.startDate)} - ${formatDate(dayOff.endDate)}`;

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: spacing.md,
      padding: spacing.md,
      backgroundColor: isPast ? colors.neutral.background : colors.neutral.white,
      border: `1px solid ${colors.neutral.border}`,
      borderRadius: borderRadius.md,
      opacity: isPast ? 0.7 : 1,
    }}>
      <div style={{
        fontSize: '24px',
        width: 40,
        textAlign: 'center',
      }}>
        {TYPE_ICONS[dayOff.type]}
      </div>

      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
          <span style={{ fontWeight: fontWeight.medium }}>
            {dateRange}
          </span>
          <span style={{
            padding: `2px ${spacing.xs}`,
            borderRadius: borderRadius.sm,
            backgroundColor: TYPE_COLORS[dayOff.type],
            color: colors.neutral.white,
            fontSize: fontSize.xs,
            fontWeight: fontWeight.medium,
          }}>
            {t(`dayOffType.${dayOff.type}`)}
          </span>
          {!dayOff.approved && (
            <span style={{
              padding: `2px ${spacing.xs}`,
              borderRadius: borderRadius.sm,
              backgroundColor: colors.warning.main,
              color: colors.neutral.white,
              fontSize: fontSize.xs,
              fontWeight: fontWeight.medium,
            }}>
              {t('schedule.pending')}
            </span>
          )}
        </div>
        {dayOff.description && (
          <div style={{
            marginTop: spacing.xs,
            fontSize: fontSize.sm,
            color: colors.neutral.textLight,
          }}>
            {dayOff.description}
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: spacing.xs }}>
        {!dayOff.approved && (
          <Button variant="ghost" size="sm" onClick={() => onApprove(true)}>
            {t('schedule.approve')}
          </Button>
        )}
        <Button variant="ghost" size="sm" onClick={onEdit}>
          {t('common.edit')}
        </Button>
        <Button variant="ghost" size="sm" onClick={onDelete} style={{ color: colors.danger.main }}>
          {t('common.delete')}
        </Button>
      </div>
    </div>
  );
}

interface DayOffModalProps {
  open: boolean;
  dayOff: VeterinarianDayOffResponse | null;
  onSave: (data: VeterinarianDayOffRequest) => Promise<void>;
  onClose: () => void;
}

function DayOffModal({ open, dayOff, onSave, onClose }: DayOffModalProps) {
  const { t } = useI18n();
  const [saving, setSaving] = useState(false);
  const [startDate, setStartDate] = useState(dayOff?.startDate ?? '');
  const [endDate, setEndDate] = useState(dayOff?.endDate ?? '');
  const [type, setType] = useState<DayOffType>(dayOff?.type ?? 'VACATION');
  const [description, setDescription] = useState(dayOff?.description ?? '');

  // Reset form when dayOff changes
  useState(() => {
    setStartDate(dayOff?.startDate ?? '');
    setEndDate(dayOff?.endDate ?? '');
    setType(dayOff?.type ?? 'VACATION');
    setDescription(dayOff?.description ?? '');
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!startDate || !endDate) return;

    setSaving(true);
    try {
      await onSave({
        startDate,
        endDate,
        type,
        description: description || undefined,
      });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose}>
      <ModalTitle>{dayOff ? t('schedule.editDayOff') : t('schedule.addDayOff')}</ModalTitle>
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <div>
              <label style={labelStyle}>{t('schedule.startDate')}</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                style={inputStyle}
              />
            </div>
            <div>
              <label style={labelStyle}>{t('schedule.endDate')}</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
                min={startDate}
                style={inputStyle}
              />
            </div>
          </div>

          <div>
            <label style={labelStyle}>{t('schedule.type')}</label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value as DayOffType)}
              style={inputStyle}
            >
              {DAY_OFF_TYPES.map(t_type => (
                <option key={t_type} value={t_type}>
                  {TYPE_ICONS[t_type]} {t(`dayOffType.${t_type}`)}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label style={labelStyle}>{t('schedule.description')}</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder={t('schedule.descriptionPlaceholder')}
              rows={3}
              style={{ ...inputStyle, resize: 'vertical' }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: spacing.sm, marginTop: spacing.md }}>
            <Button type="button" variant="ghost" onClick={onClose} disabled={saving}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? t('common.saving') : t('common.save')}
            </Button>
          </div>
        </div>
      </form>
    </Modal>
  );
}

const labelStyle: React.CSSProperties = {
  display: 'block',
  marginBottom: spacing.xs,
  fontWeight: fontWeight.medium,
  fontSize: fontSize.sm,
  color: colors.neutral.text,
};

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: spacing.sm,
  border: `1px solid ${colors.neutral.border}`,
  borderRadius: borderRadius.md,
  fontSize: fontSize.md,
};
