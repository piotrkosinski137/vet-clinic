import { forwardRef, HTMLAttributes, useMemo, useState, DragEvent } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';
import { VisitResponse, VeterinarianResponse } from '../../api/types';
import { AppointmentCard } from './AppointmentCard';

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
}

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
      style,
      ...props
    },
    ref
  ) => {
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
              backgroundColor: doctorColor,
              textAlign: 'center',
              fontWeight: fontWeight.medium,
              color: colors.neutral.white,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: spacing.xs,
            }}
          >
            <span style={{ fontSize: fontSize.md }}>👨‍⚕️</span>
            <span>Dr. {veterinarian.fullName}</span>
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

            return (
              <div
                key={hour}
                style={{
                  display: 'flex',
                  minHeight: '60px',
                  borderBottom: `1px solid ${colors.neutral.border}`,
                  cursor: onSlotClick ? 'pointer' : 'default',
                  backgroundColor: isDragOver
                    ? `${doctorColor}30`
                    : hourVisits.length > 0
                      ? `${doctorColor}10`
                      : 'transparent',
                  transition: 'background-color 0.15s ease',
                  border: isDragOver ? `2px dashed ${doctorColor}` : '2px solid transparent',
                }}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onClick={(e) => {
                  // Only trigger slot click if clicking the slot itself, not an appointment
                  if ((e.target as HTMLElement).closest('[data-appointment]')) return;
                  onSlotClick?.(date, hour, veterinarian.id);
                }}
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
