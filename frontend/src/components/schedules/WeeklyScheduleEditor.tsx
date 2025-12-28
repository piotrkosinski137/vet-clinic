import { useState, useCallback } from 'react';
import { useI18n } from '../../i18n';
import { colors, spacing, fontSize, fontWeight, shadows, borderRadius } from '../../theme';
import { Button } from '../ui/Button';
import { useToast } from '../ui/Toast';
import type {
  VeterinarianScheduleResponse,
  WeeklyScheduleRequest,
  VeterinarianScheduleRequest,
  DayOfWeek,
} from '../../api/types';

const DAYS_OF_WEEK: DayOfWeek[] = [
  'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
];

const DAY_LABELS: Record<DayOfWeek, string> = {
  MONDAY: 'dayOfWeek.MONDAY',
  TUESDAY: 'dayOfWeek.TUESDAY',
  WEDNESDAY: 'dayOfWeek.WEDNESDAY',
  THURSDAY: 'dayOfWeek.THURSDAY',
  FRIDAY: 'dayOfWeek.FRIDAY',
  SATURDAY: 'dayOfWeek.SATURDAY',
  SUNDAY: 'dayOfWeek.SUNDAY',
};

interface WeeklyScheduleEditorProps {
  schedule: VeterinarianScheduleResponse[];
  onUpdate: (schedule: WeeklyScheduleRequest) => Promise<VeterinarianScheduleResponse[]>;
}

interface ScheduleState {
  [key: string]: {
    workingDay: boolean;
    startTime: string;
    endTime: string;
  };
}

function initializeScheduleState(schedule: VeterinarianScheduleResponse[]): ScheduleState {
  const state: ScheduleState = {};
  DAYS_OF_WEEK.forEach(day => {
    const existing = schedule.find(s => s.dayOfWeek === day);
    state[day] = {
      workingDay: existing?.workingDay ?? (day !== 'SATURDAY' && day !== 'SUNDAY'),
      startTime: existing?.startTime ?? '08:00',
      endTime: existing?.endTime ?? '17:00',
    };
  });
  return state;
}

export function WeeklyScheduleEditor({
  schedule,
  onUpdate,
}: WeeklyScheduleEditorProps) {
  const { t } = useI18n();
  const toast = useToast();
  const [editedSchedule, setEditedSchedule] = useState<ScheduleState>(() =>
    initializeScheduleState(schedule)
  );
  const [saving, setSaving] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);

  const handleWorkingDayChange = useCallback((day: DayOfWeek, workingDay: boolean) => {
    setEditedSchedule(prev => ({
      ...prev,
      [day]: { ...prev[day], workingDay },
    }));
    setHasChanges(true);
  }, []);

  const handleTimeChange = useCallback((day: DayOfWeek, field: 'startTime' | 'endTime', value: string) => {
    setEditedSchedule(prev => ({
      ...prev,
      [day]: { ...prev[day], [field]: value },
    }));
    setHasChanges(true);
  }, []);

  const handleSave = useCallback(async () => {
    setSaving(true);
    try {
      const schedules: VeterinarianScheduleRequest[] = DAYS_OF_WEEK.map(day => ({
        dayOfWeek: day,
        workingDay: editedSchedule[day].workingDay,
        startTime: editedSchedule[day].workingDay ? editedSchedule[day].startTime : null,
        endTime: editedSchedule[day].workingDay ? editedSchedule[day].endTime : null,
      }));

      await onUpdate({ schedules });
      setHasChanges(false);
      toast.success(t('schedule.scheduleSaved'));
    } catch (_err) {
      toast.error(t('common.error'));
    } finally {
      setSaving(false);
    }
  }, [editedSchedule, onUpdate, t, toast]);

  const handleReset = useCallback(() => {
    setEditedSchedule(initializeScheduleState(schedule));
    setHasChanges(false);
  }, [schedule]);

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
          {t('schedule.weeklySchedule')}
        </div>
        {hasChanges && (
          <div style={{ display: 'flex', gap: spacing.sm }}>
            <Button variant="ghost" size="sm" onClick={handleReset} disabled={saving}>
              {t('common.cancel')}
            </Button>
            <Button variant="primary" size="sm" onClick={handleSave} disabled={saving}>
              {saving ? t('common.saving') : t('schedule.saveSchedule')}
            </Button>
          </div>
        )}
      </div>

      <div style={{ padding: spacing.md }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr>
              <th style={headerCellStyle}>{t('schedule.day')}</th>
              <th style={headerCellStyle}>{t('schedule.working')}</th>
              <th style={headerCellStyle}>{t('schedule.startTime')}</th>
              <th style={headerCellStyle}>{t('schedule.endTime')}</th>
            </tr>
          </thead>
          <tbody>
            {DAYS_OF_WEEK.map((day) => (
              <DayRow
                key={day}
                day={day}
                dayLabel={t(DAY_LABELS[day])}
                workingDay={editedSchedule[day].workingDay}
                startTime={editedSchedule[day].startTime}
                endTime={editedSchedule[day].endTime}
                onWorkingDayChange={(working) => handleWorkingDayChange(day, working)}
                onStartTimeChange={(time) => handleTimeChange(day, 'startTime', time)}
                onEndTimeChange={(time) => handleTimeChange(day, 'endTime', time)}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

const headerCellStyle: React.CSSProperties = {
  padding: spacing.sm,
  textAlign: 'left',
  fontWeight: fontWeight.medium,
  fontSize: fontSize.sm,
  color: colors.neutral.textLight,
  borderBottom: `1px solid ${colors.neutral.border}`,
};

interface DayRowProps {
  day: DayOfWeek;
  dayLabel: string;
  workingDay: boolean;
  startTime: string;
  endTime: string;
  onWorkingDayChange: (working: boolean) => void;
  onStartTimeChange: (time: string) => void;
  onEndTimeChange: (time: string) => void;
}

function DayRow({
  day,
  dayLabel,
  workingDay,
  startTime,
  endTime,
  onWorkingDayChange,
  onStartTimeChange,
  onEndTimeChange,
}: DayRowProps) {
  const { t } = useI18n();
  const isWeekend = day === 'SATURDAY' || day === 'SUNDAY';

  return (
    <tr style={{
      backgroundColor: isWeekend ? colors.neutral.background : 'transparent',
    }}>
      <td style={cellStyle}>
        <span style={{ fontWeight: fontWeight.medium }}>
          {dayLabel}
        </span>
      </td>
      <td style={cellStyle}>
        <label style={{ display: 'flex', alignItems: 'center', gap: spacing.xs, cursor: 'pointer' }}>
          <input
            type="checkbox"
            checked={workingDay}
            onChange={(e) => onWorkingDayChange(e.target.checked)}
            style={{ width: 16, height: 16, accentColor: colors.primary.main }}
          />
          <span style={{
            fontSize: fontSize.sm,
            color: workingDay ? colors.success.main : colors.neutral.textLight,
          }}>
            {workingDay ? t('common.yes') : t('common.no')}
          </span>
        </label>
      </td>
      <td style={cellStyle}>
        <input
          type="time"
          value={startTime}
          onChange={(e) => onStartTimeChange(e.target.value)}
          disabled={!workingDay}
          style={{
            padding: `${spacing.xs} ${spacing.sm}`,
            border: `1px solid ${colors.neutral.border}`,
            borderRadius: borderRadius.md,
            fontSize: fontSize.sm,
            opacity: workingDay ? 1 : 0.5,
            cursor: workingDay ? 'text' : 'not-allowed',
          }}
        />
      </td>
      <td style={cellStyle}>
        <input
          type="time"
          value={endTime}
          onChange={(e) => onEndTimeChange(e.target.value)}
          disabled={!workingDay}
          style={{
            padding: `${spacing.xs} ${spacing.sm}`,
            border: `1px solid ${colors.neutral.border}`,
            borderRadius: borderRadius.md,
            fontSize: fontSize.sm,
            opacity: workingDay ? 1 : 0.5,
            cursor: workingDay ? 'text' : 'not-allowed',
          }}
        />
      </td>
    </tr>
  );
}

const cellStyle: React.CSSProperties = {
  padding: spacing.sm,
  borderBottom: `1px solid ${colors.neutral.border}`,
};
