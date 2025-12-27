import { forwardRef, HTMLAttributes, useState, useMemo, useCallback } from 'react';
import { colors, spacing, borderRadius, fontSize, fontWeight, shadows } from '../../theme';
import { VisitResponse, VeterinarianResponse } from '../../api/types';
import { CalendarDay } from './CalendarDay';
import { CalendarDoctorColumn } from './CalendarDoctorColumn';
import { Button } from '../ui/Button';

export type CalendarView = 'day' | 'week';

export interface CalendarProps extends HTMLAttributes<HTMLDivElement> {
  visits: VisitResponse[];
  view?: CalendarView;
  onViewChange?: (view: CalendarView) => void;
  date?: Date;
  onDateChange?: (date: Date) => void;
  onVisitSelect?: (visit: VisitResponse) => void;
  onSlotClick?: (date: Date, hour: number, veterinarianId?: string) => void;
  onVisitDrop?: (
    visit: VisitResponse,
    newDate: Date,
    newHour: number,
    newVeterinarianId?: string,
    newVeterinarianName?: string
  ) => void;
  startHour?: number;
  endHour?: number;
  veterinarians?: VeterinarianResponse[];
}

function getWeekDates(date: Date): Date[] {
  const dates: Date[] = [];
  const dayOfWeek = date.getDay();
  const monday = new Date(date);
  monday.setDate(date.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));

  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    dates.push(d);
  }
  return dates;
}

function formatMonthYear(date: Date): string {
  return date.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
}

export const Calendar = forwardRef<HTMLDivElement, CalendarProps>(
  (
    {
      visits,
      view: controlledView,
      onViewChange,
      date: controlledDate,
      onDateChange,
      onVisitSelect,
      onSlotClick,
      onVisitDrop,
      startHour = 8,
      endHour = 18,
      veterinarians = [],
      style,
      ...props
    },
    ref
  ) => {
    const [internalView, setInternalView] = useState<CalendarView>('day');
    const [internalDate, setInternalDate] = useState<Date>(new Date());

    const view = controlledView ?? internalView;
    const date = controlledDate ?? internalDate;

    const handleViewChange = useCallback(
      (newView: CalendarView) => {
        if (onViewChange) {
          onViewChange(newView);
        } else {
          setInternalView(newView);
        }
      },
      [onViewChange]
    );

    const handleDateChange = useCallback(
      (newDate: Date) => {
        if (onDateChange) {
          onDateChange(newDate);
        } else {
          setInternalDate(newDate);
        }
      },
      [onDateChange]
    );

    const navigatePrevious = useCallback(() => {
      const newDate = new Date(date);
      if (view === 'day') {
        newDate.setDate(date.getDate() - 1);
      } else {
        newDate.setDate(date.getDate() - 7);
      }
      handleDateChange(newDate);
    }, [date, view, handleDateChange]);

    const navigateNext = useCallback(() => {
      const newDate = new Date(date);
      if (view === 'day') {
        newDate.setDate(date.getDate() + 1);
      } else {
        newDate.setDate(date.getDate() + 7);
      }
      handleDateChange(newDate);
    }, [date, view, handleDateChange]);

    const goToToday = useCallback(() => {
      handleDateChange(new Date());
    }, [handleDateChange]);

    const displayDates = useMemo(() => {
      if (view === 'day') {
        return [date];
      }
      return getWeekDates(date);
    }, [view, date]);

    const visitsByDate = useMemo(() => {
      const map: Record<string, VisitResponse[]> = {};
      visits.forEach((visit) => {
        const visitDate = new Date(visit.visitDate);
        const key = visitDate.toISOString().split('T')[0];
        if (!map[key]) {
          map[key] = [];
        }
        map[key].push(visit);
      });
      return map;
    }, [visits]);

    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          flexDirection: 'column',
          backgroundColor: colors.neutral.white,
          borderRadius: borderRadius.md,
          boxShadow: shadows.md,
          overflow: 'hidden',
          ...style,
        }}
        {...props}
      >
        {/* Header */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: spacing.md,
            borderBottom: `1px solid ${colors.neutral.border}`,
            backgroundColor: colors.neutral.white,
          }}
        >
          {/* Navigation */}
          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
            <Button variant="ghost" size="sm" onClick={navigatePrevious}>
              ← Prev
            </Button>
            <Button variant="ghost" size="sm" onClick={goToToday}>
              Today
            </Button>
            <Button variant="ghost" size="sm" onClick={navigateNext}>
              Next →
            </Button>
          </div>

          {/* Current date display */}
          <h2
            style={{
              margin: 0,
              fontSize: fontSize.lg,
              fontWeight: fontWeight.semibold,
              color: colors.secondary.main,
            }}
          >
            {formatMonthYear(date)}
          </h2>

          {/* View toggle */}
          <div
            style={{
              display: 'flex',
              backgroundColor: colors.neutral.background,
              borderRadius: borderRadius.sm,
              padding: '2px',
            }}
          >
            <Button
              variant={view === 'day' ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => handleViewChange('day')}
            >
              Day
            </Button>
            <Button
              variant={view === 'week' ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => handleViewChange('week')}
            >
              Week
            </Button>
          </div>
        </div>

        {/* Calendar body */}
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            flex: 1,
            minHeight: '500px',
            overflow: 'hidden',
          }}
        >
          {view === 'day' && veterinarians.length > 0 ? (
            // Day view with doctors as columns
            <>
              {/* Header row - fixed */}
              <div
                style={{
                  display: 'flex',
                  borderBottom: `1px solid ${colors.neutral.border}`,
                  flexShrink: 0,
                }}
              >
                {/* Date header cell */}
                <div
                  style={{
                    padding: spacing.sm,
                    minHeight: '37px',
                    boxSizing: 'border-box',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: fontWeight.medium,
                    fontSize: fontSize.sm,
                    color: colors.neutral.textMuted,
                    backgroundColor: colors.neutral.background,
                    borderRight: `1px solid ${colors.neutral.border}`,
                    width: '50px',
                    flexShrink: 0,
                  }}
                >
                  {date.toLocaleDateString('en-US', { weekday: 'short', day: 'numeric' })}
                </div>
                {/* Doctor headers */}
                {veterinarians.map((vet) => (
                  <div
                    key={vet.id}
                    style={{
                      flex: 1,
                      minWidth: '180px',
                      padding: spacing.sm,
                      backgroundColor: vet.colorCode || colors.primary.main,
                      textAlign: 'center',
                      fontWeight: fontWeight.medium,
                      color: colors.neutral.white,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: spacing.xs,
                      borderRight: `1px solid ${colors.neutral.border}`,
                    }}
                  >
                    <span style={{ fontSize: fontSize.md }}>👨‍⚕️</span>
                    <span>Dr. {vet.fullName}</span>
                  </div>
                ))}
              </div>
              {/* Scrollable content */}
              <div
                style={{
                  display: 'flex',
                  flex: 1,
                  overflowY: 'auto',
                  overflowX: 'auto',
                }}
              >
                {/* Time column */}
                <div
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    borderRight: `1px solid ${colors.neutral.border}`,
                    backgroundColor: colors.neutral.background,
                    flexShrink: 0,
                    width: '50px',
                  }}
                >
                  {Array.from({ length: endHour - startHour + 1 }, (_, i) => startHour + i).map((hour) => (
                    <div
                      key={hour}
                      style={{
                        minHeight: '60px',
                        padding: spacing.xs,
                        fontSize: fontSize.xs,
                        color: colors.neutral.textMuted,
                        borderBottom: `1px solid ${colors.neutral.border}`,
                        display: 'flex',
                        alignItems: 'flex-start',
                      }}
                    >
                      {hour.toString().padStart(2, '0')}:00
                    </div>
                  ))}
                </div>
                {/* Doctor columns */}
                {veterinarians.map((vet) => {
                  const dateKey = date.toISOString().split('T')[0];
                  const vetVisits = visits.filter(
                    (v) => v.veterinarianName === vet.fullName && v.visitDate.startsWith(dateKey)
                  );
                  return (
                    <CalendarDoctorColumn
                      key={vet.id}
                      veterinarian={vet}
                      date={date}
                      visits={vetVisits}
                      startHour={startHour}
                      endHour={endHour}
                      onVisitSelect={onVisitSelect}
                      onSlotClick={onSlotClick}
                      onVisitDrop={onVisitDrop}
                      showHeader={false}
                    />
                  );
                })}
              </div>
            </>
          ) : (
            // Week view or day view without veterinarians - show dates as columns
            <div
              style={{
                display: 'flex',
                flex: 1,
                overflowX: 'auto',
                overflowY: 'auto',
              }}
            >
              {displayDates.map((d) => {
                const dateKey = d.toISOString().split('T')[0];
                const dayVisits = visitsByDate[dateKey] || [];
                return (
                  <CalendarDay
                    key={dateKey}
                    date={d}
                    visits={dayVisits}
                    startHour={startHour}
                    endHour={endHour}
                    onVisitSelect={onVisitSelect}
                    onSlotClick={onSlotClick}
                    showHeader={true}
                  />
                );
              })}
            </div>
          )}
        </div>
      </div>
    );
  }
);

Calendar.displayName = 'Calendar';
