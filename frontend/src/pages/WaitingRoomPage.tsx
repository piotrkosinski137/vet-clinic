import { useState, useCallback, useMemo } from 'react';
import { useWaitingRoom, useConfirmDialog, useVisits } from '../hooks';
import { WaitingRoomVisitResponse, VisitPriority, VisitResponse, VisitRequest, VisitStatus } from '../api/types';
import { VisitDetailsModal } from '../components/visits';
import {
  Button,
  Card,
  Text,
  Loading,
  PageHeader,
  useToast,
  Badge,
  EmptyState,
  ConfirmDialog,
  Modal,
  ModalHeader,
  ModalTitle,
  ModalContent,
  ModalActions,
  TextArea,
  Select,
} from '../components/ui';
import { colors, spacing, borderRadius, fontWeight, fontSize, shadows } from '../theme';
import { useI18n } from '../i18n';
import { formatCurrency } from '../hooks/usePriceList';

type SortOption = 'priority' | 'waitingTime' | 'checkedInAt';

const priorityColors: Record<VisitPriority, { bg: string; text: string; border: string }> = {
  LOW: { bg: colors.neutral.background, text: colors.neutral.textMuted, border: colors.neutral.border },
  NORMAL: { bg: colors.primary.light, text: colors.primary.main, border: colors.primary.main },
  HIGH: { bg: colors.warning.light, text: colors.warning.main, border: colors.warning.main },
  URGENT: { bg: '#FFEBEE', text: '#D32F2F', border: '#D32F2F' },
};

const priorityOrder: Record<VisitPriority, number> = {
  URGENT: 0,
  HIGH: 1,
  NORMAL: 2,
  LOW: 3,
};

function formatWaitingTime(checkedInAt: string | undefined, t: (key: string, params?: Record<string, string | number>) => string): string {
  if (!checkedInAt) return '-';

  const checkedIn = new Date(checkedInAt);
  const now = new Date();
  const diffMs = now.getTime() - checkedIn.getTime();
  const diffMinutes = Math.floor(diffMs / 60000);

  if (diffMinutes < 1) return t('waitingRoom.justNow');
  if (diffMinutes < 60) return t('waitingRoom.minutes', { count: diffMinutes });

  const hours = Math.floor(diffMinutes / 60);
  const minutes = diffMinutes % 60;
  return t('waitingRoom.hours', { count: hours, minutes });
}

function formatTime(dateString: string | undefined): string {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

interface WaitingRoomCardProps {
  visit: WaitingRoomVisitResponse;
  t: (key: string, params?: Record<string, string | number>) => string;
  onStartVisit: (visit: WaitingRoomVisitResponse) => void;
  onMarkNoShow: (visit: WaitingRoomVisitResponse) => void;
  onEditInfo: (visit: WaitingRoomVisitResponse) => void;
}

function WaitingRoomCard({ visit, t, onStartVisit, onMarkNoShow, onEditInfo }: WaitingRoomCardProps) {
  const priority = visit.priority || 'NORMAL';
  const priorityStyle = priorityColors[priority];
  const hasDebt = visit.clientDebt > 0;

  return (
    <Card
      style={{
        padding: spacing.md,
        borderLeft: `4px solid ${priorityStyle.border}`,
        marginBottom: spacing.md,
        transition: 'box-shadow 0.2s ease',
      }}
      hoverable
    >
      <div style={{ display: 'flex', gap: spacing.lg }}>
        {/* Left: Patient & Client Info */}
        <div style={{ flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.sm }}>
            <span style={{ fontSize: fontSize.xl }}>🐾</span>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                <Text style={{ fontWeight: fontWeight.bold, fontSize: fontSize.lg }}>
                  {visit.patientName || t('waitingRoom.unknownPatient')}
                </Text>
                {visit.species && (
                  <Badge variant="secondary" style={{ fontSize: fontSize.xs }}>
                    {visit.species}
                  </Badge>
                )}
                {visit.patientLabels?.map((label) => (
                  <Badge
                    key={label}
                    variant={label === 'AGGRESSIVE' ? 'danger' : label === 'VIP' ? 'warning' : 'secondary'}
                    style={{ fontSize: fontSize.xs }}
                  >
                    {label}
                  </Badge>
                ))}
              </div>
              {visit.breed && (
                <Text variant="muted" size="sm">{visit.breed}</Text>
              )}
              {visit.reason && (
                <Text variant="muted" size="sm">
                  {visit.reason}
                </Text>
              )}
            </div>
          </div>

          {/* Client Info */}
          {visit.clientName && (
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.xs }}>
              <Text variant="muted" size="sm">👤 {visit.clientName}</Text>
              {visit.clientPhone && (
                <Text variant="muted" size="sm">📞 {visit.clientPhone}</Text>
              )}
            </div>
          )}

          {/* Veterinarian */}
          {visit.veterinarianName && (
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.xs, marginBottom: spacing.xs }}>
              <Text variant="muted" size="sm">🩺 Dr. {visit.veterinarianName}</Text>
            </div>
          )}

          {/* Waiting Room Notes */}
          {visit.waitingRoomNotes && (
            <div
              style={{
                marginTop: spacing.sm,
                padding: spacing.sm,
                backgroundColor: colors.neutral.background,
                borderRadius: borderRadius.sm,
                fontSize: fontSize.sm,
              }}
            >
              <Text variant="muted" size="sm">📝 {visit.waitingRoomNotes}</Text>
            </div>
          )}
        </div>

        {/* Center: Waiting Time & Priority */}
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: `${spacing.sm} ${spacing.lg}`,
            backgroundColor: priorityStyle.bg,
            borderRadius: borderRadius.md,
            minWidth: '120px',
          }}
        >
          <Text size="sm" variant="muted">{t('waitingRoom.waitingTime')}</Text>
          <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: priorityStyle.text }}>
            {formatWaitingTime(visit.checkedInAt, t)}
          </Text>
          <Badge
            variant={priority === 'URGENT' ? 'danger' : priority === 'HIGH' ? 'warning' : priority === 'LOW' ? 'secondary' : 'primary'}
            style={{ marginTop: spacing.xs }}
          >
            {t(`waitingRoom.priorities.${priority}`)}
          </Badge>
        </div>

        {/* Financial Info */}
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: `${spacing.sm} ${spacing.md}`,
            backgroundColor: hasDebt ? colors.danger.light : colors.success.light,
            borderRadius: borderRadius.md,
            minWidth: '100px',
          }}
        >
          <Text size="sm" variant="muted">{t('waitingRoom.clientDebt')}</Text>
          <Text
            style={{
              fontSize: fontSize.lg,
              fontWeight: fontWeight.bold,
              color: hasDebt ? colors.danger.main : colors.success.main
            }}
          >
            {formatCurrency(visit.clientDebt || 0)}
          </Text>
          {hasDebt && (
            <Badge variant="danger" style={{ marginTop: spacing.xs, fontSize: fontSize.xs }}>
              {t('waitingRoom.hasDebt')}
            </Badge>
          )}
        </div>

        {/* Right: Actions */}
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            gap: spacing.xs,
            justifyContent: 'center',
            minWidth: '140px',
          }}
        >
          <Button variant="primary" size="sm" onClick={() => onStartVisit(visit)}>
            {t('waitingRoom.startVisit')}
          </Button>
          <Button variant="ghost" size="sm" onClick={() => onEditInfo(visit)}>
            {t('waitingRoom.updateInfo')}
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onMarkNoShow(visit)}
            style={{ color: colors.danger.main }}
          >
            {t('visits.markNoShow')}
          </Button>
        </div>
      </div>

      {/* Footer: Check-in time */}
      <div
        style={{
          marginTop: spacing.sm,
          paddingTop: spacing.sm,
          borderTop: `1px solid ${colors.neutral.borderLight}`,
          display: 'flex',
          justifyContent: 'space-between',
          fontSize: fontSize.xs,
          color: colors.neutral.textMuted,
        }}
      >
        <span>{t('waitingRoom.checkedInAt')}: {formatTime(visit.checkedInAt)}</span>
        <span>ID: {visit.id.slice(0, 8)}</span>
      </div>
    </Card>
  );
}

interface EditInfoModalProps {
  visit: WaitingRoomVisitResponse | null;
  open: boolean;
  onClose: () => void;
  onSave: (visitId: string, notes: string, priority: VisitPriority) => Promise<void>;
  t: (key: string) => string;
}

function EditInfoModal({ visit, open, onClose, onSave, t }: EditInfoModalProps) {
  const [notes, setNotes] = useState(visit?.waitingRoomNotes || '');
  const [priority, setPriority] = useState<VisitPriority>(visit?.priority || 'NORMAL');
  const [saving, setSaving] = useState(false);

  // Reset form when visit changes
  useMemo(() => {
    if (visit) {
      setNotes(visit.waitingRoomNotes || '');
      setPriority(visit.priority || 'NORMAL');
    }
  }, [visit]);

  const handleSave = async () => {
    if (!visit) return;
    setSaving(true);
    try {
      await onSave(visit.id, notes, priority);
      onClose();
    } finally {
      setSaving(false);
    }
  };

  if (!visit) return null;

  return (
    <Modal open={open} onClose={onClose}>
      <ModalHeader>
        <ModalTitle>{t('waitingRoom.updateInfo')}</ModalTitle>
      </ModalHeader>
      <ModalContent>
        <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.md }}>
          <div>
            <label style={{ display: 'block', marginBottom: spacing.xs, fontWeight: fontWeight.medium }}>
              {t('waitingRoom.priority')}
            </label>
            <Select
              value={priority}
              onChange={(e) => setPriority(e.target.value as VisitPriority)}
            >
              <option value="LOW">{t('waitingRoom.priorities.LOW')}</option>
              <option value="NORMAL">{t('waitingRoom.priorities.NORMAL')}</option>
              <option value="HIGH">{t('waitingRoom.priorities.HIGH')}</option>
              <option value="URGENT">{t('waitingRoom.priorities.URGENT')}</option>
            </Select>
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: spacing.xs, fontWeight: fontWeight.medium }}>
              {t('waitingRoom.notes')}
            </label>
            <TextArea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder={t('waitingRoom.notesPlaceholder')}
              rows={4}
            />
          </div>
        </div>
      </ModalContent>
      <ModalActions>
        <Button variant="ghost" onClick={onClose} disabled={saving}>
          {t('common.cancel')}
        </Button>
        <Button variant="primary" onClick={handleSave} disabled={saving}>
          {saving ? t('common.saving') : t('common.save')}
        </Button>
      </ModalActions>
    </Modal>
  );
}

export function WaitingRoomPage() {
  const { t } = useI18n();
  const { success, error: showError } = useToast();
  const { visits, loading, error, startVisit, markNoShow, updateWaitingRoomInfo, refresh } = useWaitingRoom(true);
  const { updateVisit, updateVisitStatus, deleteVisit } = useVisits();
  const { dialogState, showConfirm, closeDialog, handleConfirm } = useConfirmDialog();

  const [sortBy, setSortBy] = useState<SortOption>('priority');
  const [editingVisit, setEditingVisit] = useState<WaitingRoomVisitResponse | null>(null);
  // State for the visit being conducted (opened after "Start Visit")
  const [activeVisit, setActiveVisit] = useState<VisitResponse | null>(null);

  // Sort visits based on selected option
  const sortedVisits = useMemo(() => {
    const sorted = [...visits];

    switch (sortBy) {
      case 'priority':
        return sorted.sort((a, b) => {
          const priorityDiff = priorityOrder[a.priority || 'NORMAL'] - priorityOrder[b.priority || 'NORMAL'];
          if (priorityDiff !== 0) return priorityDiff;
          // Secondary sort by check-in time
          return new Date(a.checkedInAt || 0).getTime() - new Date(b.checkedInAt || 0).getTime();
        });
      case 'waitingTime':
        return sorted.sort((a, b) =>
          new Date(a.checkedInAt || 0).getTime() - new Date(b.checkedInAt || 0).getTime()
        );
      case 'checkedInAt':
        return sorted.sort((a, b) =>
          new Date(b.checkedInAt || 0).getTime() - new Date(a.checkedInAt || 0).getTime()
        );
      default:
        return sorted;
    }
  }, [visits, sortBy]);

  const handleStartVisit = useCallback(async (visit: WaitingRoomVisitResponse) => {
    try {
      const visitResponse = await startVisit(visit.id);
      // Open the visit form modal for the clinician to fill in details
      setActiveVisit(visitResponse);
      success(t('waitingRoom.visitStarted'));
    } catch (err) {
      showError(err instanceof Error ? err.message : t('waitingRoom.failedToStart'));
    }
  }, [startVisit, success, showError, t]);

  // Handlers for the active visit modal
  const handleVisitSave = useCallback(async (data: Partial<VisitRequest>) => {
    if (!activeVisit) return;
    try {
      const updated = await updateVisit(activeVisit.id, {
        ...activeVisit,
        ...data,
      } as VisitRequest);
      setActiveVisit(updated);
      success(t('visits.visitUpdated'));
    } catch (err) {
      showError(t('visits.failedToSave'));
      throw err;
    }
  }, [activeVisit, updateVisit, success, showError, t]);

  const handleVisitStatusChange = useCallback(async (status: VisitStatus) => {
    if (!activeVisit) return;
    try {
      const updatedVisit = await updateVisitStatus(activeVisit.id, status);
      success(t('visits.statusUpdated'));
      // If completed or cancelled, close the modal and refresh waiting room
      if (status === 'COMPLETED' || status === 'CANCELLED') {
        setActiveVisit(null);
        refresh();
      } else {
        // Update active visit with new status to reflect changes in modal
        setActiveVisit(updatedVisit);
        refresh();
      }
    } catch (err) {
      showError(t('visits.failedToUpdateStatus'));
      throw err;
    }
  }, [activeVisit, updateVisitStatus, success, showError, t, refresh]);

  const handleVisitDelete = useCallback(() => {
    if (!activeVisit) return;
    showConfirm(
      t('common.confirm'),
      t('visits.confirmDelete'),
      async () => {
        try {
          await deleteVisit(activeVisit.id);
          setActiveVisit(null);
          success(t('visits.visitDeleted'));
        } catch (err) {
          showError(t('visits.failedToDelete'));
        }
      }
    );
  }, [activeVisit, deleteVisit, showConfirm, success, showError, t]);

  const handleMarkNoShow = useCallback((visit: WaitingRoomVisitResponse) => {
    showConfirm(
      t('visits.markNoShow'),
      t('visits.confirmMarkNoShow'),
      async () => {
        try {
          await markNoShow(visit.id);
          success(t('visits.markedNoShow'));
        } catch (err) {
          showError(err instanceof Error ? err.message : t('waitingRoom.failedToUpdate'));
        }
      }
    );
  }, [showConfirm, markNoShow, success, showError, t]);

  const handleUpdateInfo = useCallback(async (visitId: string, notes: string, priority: VisitPriority) => {
    try {
      await updateWaitingRoomInfo(visitId, notes, priority);
      success(t('waitingRoom.infoUpdated'));
    } catch (err) {
      showError(err instanceof Error ? err.message : t('waitingRoom.failedToUpdate'));
      throw err;
    }
  }, [updateWaitingRoomInfo, success, showError, t]);

  if (loading && visits.length === 0) {
    return <Loading text={t('common.loading')} />;
  }

  return (
    <div>
      <PageHeader
        title={t('waitingRoom.title')}
        subtitle={t('waitingRoom.subtitle')}
        actions={
          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.md }}>
            <Text variant="muted" size="sm">
              {t('waitingRoom.autoRefresh')}
            </Text>
            <Button variant="ghost" size="sm" onClick={refresh}>
              🔄 {loading ? t('waitingRoom.refreshing') : t('common.today')}
            </Button>
          </div>
        }
      />

      {/* Stats Bar */}
      <div
        style={{
          display: 'flex',
          gap: spacing.lg,
          marginBottom: spacing.lg,
          padding: spacing.md,
          backgroundColor: colors.neutral.surface,
          borderRadius: borderRadius.lg,
          boxShadow: shadows.sm,
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
          <span style={{ fontSize: fontSize.xl }}>👥</span>
          <div>
            <Text variant="muted" size="sm">{t('waitingRoom.title')}</Text>
            <Text style={{ fontWeight: fontWeight.bold, fontSize: fontSize.lg }}>{visits.length}</Text>
          </div>
        </div>

        {/* Priority breakdown */}
        {visits.length > 0 && (
          <>
            <div style={{ width: '1px', backgroundColor: colors.neutral.border }} />
            <div style={{ display: 'flex', gap: spacing.md }}>
              {(['URGENT', 'HIGH', 'NORMAL', 'LOW'] as VisitPriority[]).map((p) => {
                const count = visits.filter((v) => (v.priority || 'NORMAL') === p).length;
                if (count === 0) return null;
                return (
                  <Badge key={p} variant={p === 'URGENT' ? 'danger' : p === 'HIGH' ? 'warning' : p === 'LOW' ? 'secondary' : 'primary'}>
                    {t(`waitingRoom.priorities.${p}`)}: {count}
                  </Badge>
                );
              })}
            </div>
          </>
        )}

        {/* Sort control */}
        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: spacing.sm }}>
          <Text variant="muted" size="sm">{t('waitingRoom.sortBy')}:</Text>
          <Select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as SortOption)}
            style={{ minWidth: '160px' }}
          >
            <option value="priority">{t('waitingRoom.sortOptions.priority')}</option>
            <option value="waitingTime">{t('waitingRoom.sortOptions.waitingTime')}</option>
            <option value="checkedInAt">{t('waitingRoom.sortOptions.checkedInAt')}</option>
          </Select>
        </div>
      </div>

      {/* Error display */}
      {error && (
        <Card
          style={{
            padding: spacing.md,
            backgroundColor: colors.danger.light,
            marginBottom: spacing.md,
          }}
        >
          <Text style={{ color: colors.danger.main }}>{error}</Text>
          <Button variant="ghost" size="sm" onClick={refresh} style={{ marginTop: spacing.sm }}>
            {t('errors.retry')}
          </Button>
        </Card>
      )}

      {/* Empty state */}
      {visits.length === 0 && !loading && (
        <EmptyState
          icon="🪑"
          title={t('waitingRoom.noPatients')}
          description={t('waitingRoom.noPatientHint')}
        />
      )}

      {/* Patient cards */}
      <div>
        {sortedVisits.map((visit) => (
          <WaitingRoomCard
            key={visit.id}
            visit={visit}
            t={t}
            onStartVisit={handleStartVisit}
            onMarkNoShow={handleMarkNoShow}
            onEditInfo={setEditingVisit}
          />
        ))}
      </div>

      {/* Edit Info Modal */}
      <EditInfoModal
        visit={editingVisit}
        open={!!editingVisit}
        onClose={() => setEditingVisit(null)}
        onSave={handleUpdateInfo}
        t={t}
      />

      {/* Confirm Dialog for No Show */}
      <ConfirmDialog
        open={dialogState.open}
        onClose={closeDialog}
        onConfirm={handleConfirm}
        title={dialogState.title}
        message={dialogState.message}
        confirmLabel={t('common.confirm')}
        cancelLabel={t('common.cancel')}
        variant="danger"
      />

      {/* Visit Details Modal - opens after starting a visit */}
      <VisitDetailsModal
        visit={activeVisit}
        open={!!activeVisit}
        onClose={() => setActiveVisit(null)}
        onSave={handleVisitSave}
        onStatusChange={handleVisitStatusChange}
        onDelete={handleVisitDelete}
      />
    </div>
  );
}
