import { forwardRef, HTMLAttributes, useMemo } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';
import { VisitResponse } from '../../api/types';
import { AppointmentCard } from './AppointmentCard';
import { useI18n } from '../../i18n';
import { formatDateWithLocale } from '../../utils/dateFormatting';

export interface CalendarDayProps extends HTMLAttributes<HTMLDivElement> {
  date: Date;
  visits: VisitResponse[];
  startHour?: number;
  endHour?: number;
  onVisitSelect?: (visit: VisitResponse) => void;
  onSlotClick?: (date: Date, hour: number, minute: number) => void;
  showHeader?: boolean;
}

const QUARTER_HOURS = [0, 15, 30, 45];

function formatDate(date: Date, language: string): string {
  return formatDateWithLocale(date, { weekday: 'short', month: 'short', day: 'numeric' }, language);
}

function isToday(date: Date): boolean {
  const today = new Date();
  return (
    date.getDate() === today.getDate() &&
    date.getMonth() === today.getMonth() &&
    date.getFullYear() === today.getFullYear()
  );
}

export const CalendarDay = forwardRef<HTMLDivElement, CalendarDayProps>(
  (
    {
      date,
      visits,
      startHour = 8,
      endHour = 18,
      onVisitSelect,
      onSlotClick,
      showHeader = true,
      style,
      ...props
    },
    ref
  ) => {
    const { language } = useI18n();
    const hours = useMemo(() => {
      const h: number[] = [];
      for (let i = startHour; i <= endHour; i++) {
        h.push(i);
      }
      return h;
    }, [startHour, endHour]);

    const visitsBySlot = useMemo(() => {
      const map: Record<string, VisitResponse[]> = {};
      visits.forEach((visit) => {
        const visitDate = new Date(visit.visitDate);
        const hour = visitDate.getHours();
        const minute = visitDate.getMinutes();
        const quarterMinute = Math.floor(minute / 15) * 15;
        const slotKey = `${hour}:${quarterMinute}`;
        if (!map[slotKey]) {
          map[slotKey] = [];
        }
        map[slotKey].push(visit);
      });
      return map;
    }, [visits]);

    const today = isToday(date);

    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          flexDirection: 'column',
          borderRight: `1px solid ${colors.neutral.border}`,
          minWidth: '200px',
          flex: 1,
          ...style,
        }}
        {...props}
      >
        {showHeader && (
          <div
            style={{
              padding: spacing.sm,
              borderBottom: `1px solid ${colors.neutral.border}`,
              backgroundColor: today ? colors.primary.light : colors.neutral.white,
              textAlign: 'center',
              fontWeight: today ? fontWeight.bold : fontWeight.medium,
              color: today ? colors.primary.main : colors.neutral.text,
            }}
          >
            {formatDate(date, language)}
          </div>
        )}
        <div style={{ flex: 1 }}>
          {hours.map((hour) => (
            <div
              key={hour}
              style={{
                display: 'flex',
                minHeight: '60px',
                borderBottom: `1px solid ${colors.neutral.border}`,
              }}
            >
              <div
                style={{
                  width: '50px',
                  padding: spacing.xs,
                  fontSize: fontSize.xs,
                  color: colors.neutral.textMuted,
                  borderRight: `1px solid ${colors.neutral.border}`,
                  backgroundColor: colors.neutral.background,
                  flexShrink: 0,
                }}
              >
                {hour.toString().padStart(2, '0')}:00
              </div>
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                {QUARTER_HOURS.map((minute) => {
                  const slotKey = `${hour}:${minute}`;
                  const slotVisits = visitsBySlot[slotKey] || [];
                  return (
                    <div
                      key={slotKey}
                      style={{
                        flex: 1,
                        minHeight: '15px',
                        borderBottom: minute < 45 ? `1px dashed ${colors.neutral.border}` : 'none',
                        padding: slotVisits.length > 0 ? spacing.xs : '2px',
                        cursor: onSlotClick ? 'pointer' : 'default',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: spacing.xs,
                      }}
                      onClick={() => onSlotClick?.(date, hour, minute)}
                      title={`${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`}
                    >
                      {slotVisits.map((visit) => (
                        <AppointmentCard key={visit.id} visit={visit} onSelect={onVisitSelect} />
                      ))}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }
);

CalendarDay.displayName = 'CalendarDay';
