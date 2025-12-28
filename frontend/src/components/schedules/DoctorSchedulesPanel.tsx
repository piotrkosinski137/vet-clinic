import { useState, useMemo } from 'react';
import { useVeterinarians, useVeterinarianSchedule } from '../../hooks';
import { useI18n } from '../../i18n';
import { colors, spacing, fontSize, fontWeight, shadows, borderRadius } from '../../theme';
import { Spinner } from '../ui/Spinner';
import { WeeklyScheduleEditor } from './WeeklyScheduleEditor';
import { DaysOffManager } from './DaysOffManager';
import type { VeterinarianResponse } from '../../api/types';

export function DoctorSchedulesPanel() {
  const { t } = useI18n();
  const { veterinarians, loading: vetsLoading } = useVeterinarians({ active: true });
  const [selectedVetId, setSelectedVetId] = useState<string | null>(null);

  const {
    schedule,
    daysOff,
    loading: scheduleLoading,
    updateSchedule,
    addDayOff,
    updateDayOff,
    deleteDayOff,
    approveDayOff,
  } = useVeterinarianSchedule(selectedVetId);

  const selectedVet = useMemo(() =>
    veterinarians.find(v => v.id === selectedVetId) ?? null,
    [veterinarians, selectedVetId]
  );

  // Auto-select first vet if none selected
  useMemo(() => {
    if (!selectedVetId && veterinarians.length > 0) {
      setSelectedVetId(veterinarians[0].id);
    }
  }, [veterinarians, selectedVetId]);

  if (vetsLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: spacing.xl }}>
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div style={{ display: 'flex', gap: spacing.lg, height: '100%' }}>
      {/* Doctor List */}
      <DoctorList
        veterinarians={veterinarians}
        selectedId={selectedVetId}
        onSelect={setSelectedVetId}
      />

      {/* Schedule Editor */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: spacing.lg }}>
        {selectedVet ? (
          <>
            <DoctorHeader vet={selectedVet} />

            {scheduleLoading ? (
              <div style={{ display: 'flex', justifyContent: 'center', padding: spacing.xl }}>
                <Spinner size="md" />
              </div>
            ) : (
              <>
                <WeeklyScheduleEditor
                  schedule={schedule}
                  onUpdate={updateSchedule}
                />

                <DaysOffManager
                  daysOff={daysOff}
                  onAdd={addDayOff}
                  onUpdate={updateDayOff}
                  onDelete={deleteDayOff}
                  onApprove={approveDayOff}
                />
              </>
            )}
          </>
        ) : (
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '200px',
            color: colors.neutral.textLight,
          }}>
            {t('schedule.selectDoctor')}
          </div>
        )}
      </div>
    </div>
  );
}

interface DoctorListProps {
  veterinarians: VeterinarianResponse[];
  selectedId: string | null;
  onSelect: (id: string) => void;
}

function DoctorList({ veterinarians, selectedId, onSelect }: DoctorListProps) {
  const { t } = useI18n();

  return (
    <div style={{
      width: '280px',
      flexShrink: 0,
      backgroundColor: colors.neutral.white,
      borderRadius: borderRadius.lg,
      boxShadow: shadows.sm,
      overflow: 'hidden',
    }}>
      <div style={{
        padding: spacing.md,
        borderBottom: `1px solid ${colors.neutral.border}`,
        fontWeight: fontWeight.semibold,
        fontSize: fontSize.md,
      }}>
        {t('schedule.doctors')}
      </div>
      <div style={{ maxHeight: 'calc(100vh - 300px)', overflowY: 'auto' }}>
        {veterinarians.map(vet => (
          <DoctorListItem
            key={vet.id}
            vet={vet}
            isSelected={vet.id === selectedId}
            onClick={() => onSelect(vet.id)}
          />
        ))}
      </div>
    </div>
  );
}

interface DoctorListItemProps {
  vet: VeterinarianResponse;
  isSelected: boolean;
  onClick: () => void;
}

function DoctorListItem({ vet, isSelected, onClick }: DoctorListItemProps) {
  return (
    <div
      onClick={onClick}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: spacing.sm,
        padding: spacing.md,
        cursor: 'pointer',
        backgroundColor: isSelected ? colors.primary.light : 'transparent',
        borderLeft: `3px solid ${isSelected ? colors.primary.main : 'transparent'}`,
        transition: 'all 0.2s ease',
      }}
    >
      <div
        style={{
          width: 8,
          height: 8,
          borderRadius: '50%',
          backgroundColor: vet.colorCode || colors.primary.main,
          flexShrink: 0,
        }}
      />
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{
          fontWeight: fontWeight.medium,
          fontSize: fontSize.sm,
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
        }}>
          {vet.fullName}
        </div>
        {vet.specialization && (
          <div style={{
            fontSize: fontSize.xs,
            color: colors.neutral.textLight,
            whiteSpace: 'nowrap',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
          }}>
            {vet.specialization}
          </div>
        )}
      </div>
    </div>
  );
}

interface DoctorHeaderProps {
  vet: VeterinarianResponse;
}

function DoctorHeader({ vet }: DoctorHeaderProps) {
  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: spacing.md,
      padding: spacing.md,
      backgroundColor: colors.neutral.white,
      borderRadius: borderRadius.lg,
      boxShadow: shadows.sm,
    }}>
      <div
        style={{
          width: 48,
          height: 48,
          borderRadius: borderRadius.full,
          backgroundColor: vet.colorCode || colors.primary.main,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: colors.neutral.white,
          fontWeight: fontWeight.bold,
          fontSize: fontSize.lg,
        }}
      >
        {vet.firstName[0]}{vet.lastName[0]}
      </div>
      <div>
        <div style={{ fontWeight: fontWeight.semibold, fontSize: fontSize.lg }}>
          {vet.fullName}
        </div>
        {vet.specialization && (
          <div style={{ color: colors.neutral.textLight, fontSize: fontSize.sm }}>
            {vet.specialization}
          </div>
        )}
      </div>
    </div>
  );
}
