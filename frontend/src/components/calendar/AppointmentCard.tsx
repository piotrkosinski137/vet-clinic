import { forwardRef, HTMLAttributes, DragEvent } from 'react';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { VisitResponse, VisitStatus } from '../../api/types';
import { Badge } from '../ui/Badge';
import { useI18n } from '../../i18n';
import { formatTimeWithLocale } from '../../utils/dateFormatting';
import { VISIT_TYPE_CONFIG, VisitTypeKey } from '../../constants/visitTypes';

export interface AppointmentCardProps extends Omit<HTMLAttributes<HTMLDivElement>, 'onSelect'> {
  visit: VisitResponse;
  onSelect?: (visit: VisitResponse) => void;
  compact?: boolean;
  draggable?: boolean;
}

const statusColors: Record<VisitStatus, { bg: string; text: string }> = {
  SCHEDULED: { bg: colors.primary.light, text: colors.primary.main },
  CHECKED_IN: { bg: '#F3E5F5', text: '#7B1FA2' },  // Purple - waiting in lobby
  IN_PROGRESS: { bg: colors.warning.light, text: colors.warning.main },  // Orange - active visit
  COMPLETED: { bg: colors.success.light, text: colors.success.main },
  CANCELLED: { bg: colors.neutral.border, text: colors.neutral.textMuted },
  NO_SHOW: { bg: '#FFEBEE', text: '#D32F2F' },  // Red - no show
};

const statusTranslationKeys: Record<VisitStatus, string> = {
  SCHEDULED: 'visits.scheduled',
  CHECKED_IN: 'visits.checkedIn',
  IN_PROGRESS: 'visits.inProgress',
  COMPLETED: 'visits.completed',
  CANCELLED: 'visits.cancelled',
  NO_SHOW: 'visits.noShow',
};

const statusVariants: Record<VisitStatus, 'primary' | 'warning' | 'success' | 'secondary' | 'danger'> = {
  SCHEDULED: 'primary',
  CHECKED_IN: 'secondary',  // Purple badge
  IN_PROGRESS: 'warning',   // Orange badge
  COMPLETED: 'success',
  CANCELLED: 'secondary',
  NO_SHOW: 'danger',
};

function formatTime(dateString: string, language: string): string {
  const date = new Date(dateString);
  return formatTimeWithLocale(date, { hour: '2-digit', minute: '2-digit', hour12: false }, language);
}

export const AppointmentCard = forwardRef<HTMLDivElement, AppointmentCardProps>(
  ({ visit, onSelect, compact = false, draggable = true, style, ...props }, ref) => {
    const { t, language } = useI18n();
    const statusColor = statusColors[visit.status];
    const visitTypeConfig = VISIT_TYPE_CONFIG[(visit.visitType || 'CONSULTATION') as VisitTypeKey];

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
      padding: spacing.xs,
      cursor: draggable ? 'grab' : onSelect ? 'pointer' : 'default',
      transition: 'transform 0.1s ease, box-shadow 0.1s ease, opacity 0.2s ease',
      overflow: 'hidden',
      boxSizing: 'border-box',
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
        {/* Time and Status */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: spacing.xs }}>
          <span style={{ fontWeight: fontWeight.semibold, fontSize: fontSize.xs }}>
            {formatTime(visit.visitDate, language)}
          </span>
          <Badge variant={statusVariants[visit.status]} style={{ fontSize: '10px', padding: '1px 4px' }}>
            {t(statusTranslationKeys[visit.status])}
          </Badge>
        </div>
        {/* Visit type icon + Patient name (Client name) */}
        <div
          style={{
            fontSize: fontSize.xs,
            color: colors.neutral.text,
            marginTop: '2px',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
          }}
        >
          <span title={t(`visitTypes.${visit.visitType || 'CONSULTATION'}`)}>{visitTypeConfig.icon}</span>
          <span>
            <span style={{ fontWeight: fontWeight.medium }}>{visit.patientName || '—'}</span>
            {visit.clientName && (
              <span style={{ color: colors.neutral.textMuted }}> ({visit.clientName})</span>
            )}
          </span>
        </div>
        {!compact && (
          <div style={{ fontSize: fontSize.xs, color: colors.neutral.textMuted, marginTop: '2px' }}>
            {visit.durationMinutes || 30} min
          </div>
        )}
      </div>
    );
  }
);

AppointmentCard.displayName = 'AppointmentCard';
