import { forwardRef, HTMLAttributes, useMemo, useState, DragEvent } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';
import { VisitResponse, VeterinarianResponse, VeterinarianAvailabilityResponse } from '../../api/types';
import { AppointmentCard } from './AppointmentCard';
import { useI18n } from '../../i18n';

export interface CalendarDoctorColumnProps extends HTMLAttributes<HTMLDivElement> {
  veterinarian: VeterinarianResponse;
  date: Date;
  visits: VisitResponse[];
  startHour?: number;
  endHour?: number;
  onVisitSelect?: (visit: VisitResponse) => void;
  onSlotClick?: (date: Date, hour: number, veterinarianId?: string) => void;
  onVisitDrop?: (
    visit: VisitResponse,
    newDate: Date,
    newHour: number,
    newVeterinarianId?: string,
    newVeterinarianName?: string
  ) => void;
  showHeader?: boolean;
  availability?: VeterinarianAvailabilityResponse;
}

// Parse time string (HH:mm:ss or HH:mm) to hour number
function parseTimeToHour(time: string | null): number | null {
  if (!time) return null;
  const parts = time.split(':');
  return parseInt(parts[0], 10);
}

// Day off type icons
const DAY_OFF_ICONS: Record<string, string> = {
  VACATION: '\u2708\uFE0F',
  SICK_LEAVE: '\uD83E\uDE7A',
  PERSONAL: '\uD83D\uDCC5',
  OTHER: '\uD83D\uDCCB',
};

// Day off background color (red/danger)
const DAY_OFF_COLOR = '#DC3545';

export const CalendarDoctorColumn = forwardRef<HTMLDivElement, CalendarDoctorColumnProps>(
  (
    {
      veterinarian,
      date,
      visits,
      startHour = 8,
      endHour = 18,
      onVisitSelect,
      onSlotClick,
      onVisitDrop,
      showHeader = true,
      availability,
      style,
      ...props
    },
    ref
  ) => {
    const { t } = useI18n();
    const [dragOverHour, setDragOverHour] = useState<number | null>(null);
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

    const doctorColor = veterinarian.colorCode || colors.primary.main;

    // Parse working hours from availability
    const workingStartHour = parseTimeToHour(availability?.startTime ?? null);
    const workingEndHour = parseTimeToHour(availability?.endTime ?? null);
    const isWorkingDay = availability?.workingDay ?? true;
    const isDayOff = availability?.isDayOff ?? false;

    // Check if an hour is within working hours
    const isWithinWorkingHours = (hour: number): boolean => {
      if (isDayOff) return false;
      if (!isWorkingDay) return false;
      if (workingStartHour === null || workingEndHour === null) return true; // No schedule = assume available
      return hour >= workingStartHour && hour < workingEndHour;
    };

    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          flexDirection: 'column',
          borderRight: `1px solid ${colors.neutral.border}`,
          minWidth: '180px',
          flex: 1,
          ...style,
        }}
        {...props}
      >
        {/* Doctor header */}
        {showHeader && (
          <div
            style={{
              padding: spacing.sm,
              borderBottom: `1px solid ${colors.neutral.border}`,
              backgroundColor: isDayOff ? colors.neutral.textMuted : doctorColor,
              textAlign: 'center',
              fontWeight: fontWeight.medium,
              color: colors.neutral.white,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: spacing.xs,
              opacity: isDayOff ? 0.8 : 1,
            }}
          >
            {isDayOff && availability?.dayOffType && (
              <span style={{ fontSize: fontSize.md }}>
                {DAY_OFF_ICONS[availability.dayOffType] || DAY_OFF_ICONS.OTHER}
              </span>
            )}
            <span style={{ fontSize: fontSize.md }}>{isDayOff ? '' : '\uD83D\uDC68\u200D\u2695\uFE0F'}</span>
            <span>Dr. {veterinarian.fullName}</span>
            {isDayOff && (
              <span style={{ fontSize: fontSize.xs, opacity: 0.9 }}>
                ({t(`dayOffType.${availability?.dayOffType}`) || t('schedule.dayOff')})
              </span>
            )}
          </div>
        )}
        {/* Day off full-day indicator */}
        {isDayOff && (
          <div
            style={{
              backgroundColor: DAY_OFF_COLOR,
              color: colors.neutral.white,
              padding: `${spacing.sm} ${spacing.md}`,
              margin: spacing.xs,
              borderRadius: '6px',
              display: 'flex',
              alignItems: 'center',
              gap: spacing.sm,
              fontWeight: fontWeight.medium,
              fontSize: fontSize.sm,
              boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
              cursor: 'not-allowed',
            }}
            title={availability?.dayOffDescription || t(`dayOffType.${availability?.dayOffType}`)}
          >
            <span style={{ fontSize: fontSize.lg }}>
              {DAY_OFF_ICONS[availability?.dayOffType || 'OTHER'] || DAY_OFF_ICONS.OTHER}
            </span>
            <div style={{ flex: 1 }}>
              <div>{t(`dayOffType.${availability?.dayOffType}`) || t('schedule.dayOff')}</div>
              {availability?.dayOffDescription && (
                <div style={{ fontSize: fontSize.xs, opacity: 0.85, marginTop: '2px' }}>
                  {availability.dayOffDescription}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Hour slots */}
        <div style={{ flex: 1 }}>
          {hours.map((hour) => {
            const hourVisits = visitsByHour[hour] || [];
            const isDragOver = dragOverHour === hour;

            const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
              e.preventDefault();
              e.dataTransfer.dropEffect = 'move';
              setDragOverHour(hour);
            };

            const handleDragLeave = () => {
              setDragOverHour(null);
            };

            const handleDrop = (e: DragEvent<HTMLDivElement>) => {
              e.preventDefault();
              setDragOverHour(null);

              try {
                const visitData = e.dataTransfer.getData('application/json');
                if (visitData && onVisitDrop) {
                  const visit: VisitResponse = JSON.parse(visitData);
                  onVisitDrop(visit, date, hour, veterinarian.id, veterinarian.fullName);
                }
              } catch {
                // Invalid drop data - ignore
              }
            };

            const isAvailable = isWithinWorkingHours(hour);

            // Determine background color based on availability
            const getBackgroundColor = (): string => {
              if (isDragOver) return `${doctorColor}40`;
              if (isDayOff) return `${DAY_OFF_COLOR}15`; // Day off - light red background
              if (!isAvailable) return '#f0f0f0'; // Outside working hours - light gray
              if (hourVisits.length > 0) return `${doctorColor}25`; // Working hour with visits
              return `${doctorColor}15`; // Working hour without visits - visible tint
            };

            // Determine if slot is clickable (for booking prevention)
            const isSlotClickable = isAvailable && !isDayOff;

            // Get left border color based on availability
            const getLeftBorderColor = (): string => {
              if (isDayOff) return DAY_OFF_COLOR;
              if (isAvailable) return doctorColor;
              return '#ccc'; // Non-working hours
            };

            return (
              <div
                key={hour}
                style={{
                  display: 'flex',
                  minHeight: '60px',
                  borderBottom: `1px solid ${colors.neutral.border}`,
                  borderLeft: `4px solid ${getLeftBorderColor()}`,
                  cursor: isSlotClickable && onSlotClick ? 'pointer' : 'not-allowed',
                  backgroundColor: getBackgroundColor(),
                  transition: 'background-color 0.15s ease, border-color 0.15s ease',
                  borderTop: isDragOver ? `2px dashed ${doctorColor}` : '2px solid transparent',
                  borderRight: isDragOver ? `2px dashed ${doctorColor}` : '2px solid transparent',
                  opacity: isAvailable ? 1 : 0.5,
                  position: 'relative',
                }}
                onDragOver={isSlotClickable ? handleDragOver : undefined}
                onDragLeave={isSlotClickable ? handleDragLeave : undefined}
                onDrop={isSlotClickable ? handleDrop : undefined}
                onClick={(e) => {
                  // Only trigger slot click if clicking the slot itself, not an appointment
                  if ((e.target as HTMLElement).closest('[data-appointment]')) return;
                  // Prevent booking on unavailable slots
                  if (!isSlotClickable) return;
                  onSlotClick?.(date, hour, veterinarian.id);
                }}
                title={!isAvailable ? t('visits.outsideWorkingHours') : isDayOff ? t('visits.doctorOnDayOff') : ''}
              >
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
                    <div key={visit.id} data-appointment>
                      <AppointmentCard visit={visit} onSelect={onVisitSelect} compact />
                    </div>
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

CalendarDoctorColumn.displayName = 'CalendarDoctorColumn';
