import { forwardRef, HTMLAttributes, useMemo } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';
import { VisitResponse } from '../../api/types';
import { AppointmentCard } from './AppointmentCard';

export interface CalendarDayProps extends HTMLAttributes<HTMLDivElement> {
  date: Date;
  visits: VisitResponse[];
  startHour?: number;
  endHour?: number;
  onVisitSelect?: (visit: VisitResponse) => void;
  onSlotClick?: (date: Date, hour: number) => void;
  showHeader?: boolean;
}

function formatDate(date: Date): string {
  return date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
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
    const hours = useMemo(() => {
      const h: number[] = [];
      for (let i = startHour; i <= endHour; i++) {
        h.push(i);
      }
      return h;
    }, [startHour, endHour]);

    const visitsByHour = useMemo(() => {
      const map: Record<number, VisitResponse[]> = {};
      visits.forEach((visit) => {
        const visitDate = new Date(visit.visitDate);
        const hour = visitDate.getHours();
        if (!map[hour]) {
          map[hour] = [];
        }
        map[hour].push(visit);
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
            {formatDate(date)}
          </div>
        )}
        <div style={{ flex: 1 }}>
          {hours.map((hour) => {
            const hourVisits = visitsByHour[hour] || [];
            return (
              <div
                key={hour}
                style={{
                  display: 'flex',
                  minHeight: '60px',
                  borderBottom: `1px solid ${colors.neutral.border}`,
                  cursor: onSlotClick ? 'pointer' : 'default',
                }}
                onClick={() => onSlotClick?.(date, hour)}
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
                <div
                  style={{
                    flex: 1,
                    padding: spacing.xs,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: spacing.xs,
                  }}
                >
                  {hourVisits.map((visit) => (
                    <AppointmentCard key={visit.id} visit={visit} onSelect={onVisitSelect} />
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  }
);

CalendarDay.displayName = 'CalendarDay';
