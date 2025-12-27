import { forwardRef, HTMLAttributes, DragEvent } from 'react';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { VisitResponse, VisitStatus } from '../../api/types';
import { Badge } from '../ui/Badge';
import { useI18n } from '../../i18n';

export interface AppointmentCardProps extends Omit<HTMLAttributes<HTMLDivElement>, 'onSelect'> {
  visit: VisitResponse;
  onSelect?: (visit: VisitResponse) => void;
  compact?: boolean;
  draggable?: boolean;
}

const statusColors: Record<VisitStatus, { bg: string; text: string }> = {
  SCHEDULED: { bg: colors.primary.light, text: colors.primary.main },
  IN_PROGRESS: { bg: colors.warning.light, text: colors.warning.main },
  COMPLETED: { bg: colors.success.light, text: colors.success.main },
  CANCELLED: { bg: colors.neutral.border, text: colors.neutral.textMuted },
};

const statusTranslationKeys: Record<VisitStatus, string> = {
  SCHEDULED: 'visits.scheduled',
  IN_PROGRESS: 'visits.inProgress',
  COMPLETED: 'visits.completed',
  CANCELLED: 'visits.cancelled',
};

const statusVariants: Record<VisitStatus, 'primary' | 'warning' | 'success' | 'secondary'> = {
  SCHEDULED: 'primary',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'secondary',
};

function formatTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false });
}

export const AppointmentCard = forwardRef<HTMLDivElement, AppointmentCardProps>(
  ({ visit, onSelect, compact = false, draggable = true, style, ...props }, ref) => {
    const { t } = useI18n();
    const statusColor = statusColors[visit.status];

    const handleDragStart = (e: DragEvent<HTMLDivElement>) => {
      e.dataTransfer.setData('application/json', JSON.stringify(visit));
      e.dataTransfer.effectAllowed = 'move';
      // Add a visual effect while dragging
      if (e.currentTarget) {
        e.currentTarget.style.opacity = '0.5';
      }
    };

    const handleDragEnd = (e: DragEvent<HTMLDivElement>) => {
      if (e.currentTarget) {
        e.currentTarget.style.opacity = '1';
      }
    };

    const cardStyle: React.CSSProperties = {
      backgroundColor: statusColor.bg,
      borderLeft: `4px solid ${statusColor.text}`,
      borderRadius: borderRadius.sm,
      padding: spacing.sm,
      cursor: draggable ? 'grab' : onSelect ? 'pointer' : 'default',
      transition: 'transform 0.1s ease, box-shadow 0.1s ease, opacity 0.2s ease',
      ...style,
    };

    return (
      <div
        ref={ref}
        style={cardStyle}
        draggable={draggable}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
        onClick={(e) => {
          e.stopPropagation();
          onSelect?.(visit);
        }}
        onMouseEnter={(e) => {
          if (onSelect) {
            e.currentTarget.style.transform = 'scale(1.02)';
            e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)';
          }
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.transform = 'scale(1)';
          e.currentTarget.style.boxShadow = 'none';
        }}
        {...props}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <span style={{ fontWeight: fontWeight.semibold, fontSize: fontSize.sm }}>
            {formatTime(visit.visitDate)}
          </span>
          <Badge variant={statusVariants[visit.status]} style={{ fontSize: fontSize.xs }}>
            {t(statusTranslationKeys[visit.status])}
          </Badge>
        </div>
        {!compact && visit.veterinarianName && (
          <div style={{ fontSize: fontSize.xs, color: colors.neutral.textLight, marginTop: spacing.xs }}>
            Dr. {visit.veterinarianName}
          </div>
        )}
        {visit.reason && (
          <div
            style={{
              fontSize: fontSize.xs,
              color: colors.neutral.text,
              marginTop: spacing.xs,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            {visit.reason}
          </div>
        )}
        <div style={{ fontSize: fontSize.xs, color: colors.neutral.textMuted, marginTop: spacing.xs }}>
          {visit.durationMinutes || 30} min
        </div>
      </div>
    );
  }
);

AppointmentCard.displayName = 'AppointmentCard';
