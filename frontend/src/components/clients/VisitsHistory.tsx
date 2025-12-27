import React, { useState } from 'react';
import { VisitResponse } from '../../api';
import { Button, Card, Text, Loading, Badge } from '../ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { PAGINATION, VISIT_STATUS_CONFIG } from '../../constants';
import { formatDate, formatTime } from '../../utils';

interface VisitsHistoryProps {
  visits: VisitResponse[];
  loading: boolean;
  selectedPatientName?: string;
  onVisitClick?: (visit: VisitResponse) => void;
}

export function VisitsHistory({
  visits,
  loading,
  selectedPatientName,
  onVisitClick,
}: VisitsHistoryProps) {
  const [visitsPage, setVisitsPage] = useState(0);

  // Reset page when visits change
  React.useEffect(() => {
    setVisitsPage(0);
  }, [visits]);

  return (
    <Card style={{ display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: spacing.md,
        }}
      >
        <Text style={{ fontWeight: fontWeight.semibold }}>
          {selectedPatientName ? `Visit History - ${selectedPatientName}` : 'Visit History'}
          {visits.length > 0 && (
            <span style={{ color: colors.neutral.textMuted, fontWeight: fontWeight.normal }}>
              {' '}
              ({visits.length})
            </span>
          )}
        </Text>
      </div>

      <div style={{ flex: 1, overflowY: 'auto' }}>
        {!selectedPatientName ? (
          <div style={{ textAlign: 'center', padding: spacing.xl }}>
            <Text variant="muted">Select a patient to view visits</Text>
          </div>
        ) : loading ? (
          <Loading text="Loading visits..." />
        ) : visits.length === 0 ? (
          <div style={{ textAlign: 'center', padding: spacing.xl }}>
            <Text variant="muted">No visits recorded</Text>
          </div>
        ) : (
          <>
            {/* Paginated visits */}
            {visits
              .slice(visitsPage * PAGINATION.PANEL, (visitsPage + 1) * PAGINATION.PANEL)
              .map((visit) => {
                const status =
                  VISIT_STATUS_CONFIG[visit.status] || VISIT_STATUS_CONFIG.SCHEDULED;
                const materials = visit.usedMaterials || [];
                const hasMaterials = materials.length > 0;

                return (
                  <div
                    key={visit.id}
                    onClick={() => onVisitClick?.(visit)}
                    style={{
                      padding: spacing.md,
                      marginBottom: spacing.sm,
                      borderRadius: borderRadius.md,
                      cursor: onVisitClick ? 'pointer' : 'default',
                      backgroundColor: colors.neutral.white,
                      border: `1px solid ${colors.neutral.border}`,
                      borderLeft: `4px solid ${status.color}`,
                      transition: 'box-shadow 0.2s',
                    }}
                    onMouseEnter={(e) => {
                      if (onVisitClick) {
                        e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.boxShadow = 'none';
                    }}
                  >
                    {/* Header: Date, Time, Status */}
                    <div
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'flex-start',
                        marginBottom: spacing.sm,
                      }}
                    >
                      <div>
                        <Text style={{ fontWeight: fontWeight.bold, fontSize: fontSize.sm }}>
                          📅 {formatDate(visit.visitDate)}
                        </Text>
                        <Text variant="muted" size="sm">
                          🕐 {formatTime(visit.visitDate)} •{' '}
                          {visit.veterinarianName ? `Dr. ${visit.veterinarianName}` : 'No vet'}
                        </Text>
                      </div>
                      <Badge
                        variant={
                          visit.status === 'COMPLETED'
                            ? 'success'
                            : visit.status === 'CANCELLED'
                            ? 'secondary'
                            : 'primary'
                        }
                      >
                        {status.label}
                      </Badge>
                    </div>

                    {/* Reason */}
                    {visit.reason && (
                      <div style={{ marginBottom: spacing.sm }}>
                        <Text size="sm" style={{ color: colors.neutral.textLight }}>
                          <strong>Reason:</strong> {visit.reason}
                        </Text>
                      </div>
                    )}

                    {/* Notes */}
                    {visit.notes && (
                      <div
                        style={{
                          backgroundColor: colors.warning.light,
                          padding: spacing.xs,
                          borderRadius: borderRadius.sm,
                          marginBottom: spacing.sm,
                        }}
                      >
                        <Text size="sm">
                          📝{' '}
                          {visit.notes.length > 100
                            ? `${visit.notes.substring(0, 100)}...`
                            : visit.notes}
                        </Text>
                      </div>
                    )}

                    {/* Diagnosis & Treatment */}
                    {(visit.diagnosis || visit.treatment) && (
                      <div style={{ marginBottom: spacing.sm }}>
                        {visit.diagnosis && (
                          <Text size="sm" style={{ marginBottom: spacing.xs }}>
                            <strong>🔍 Diagnosis:</strong>{' '}
                            {visit.diagnosis.length > 80
                              ? `${visit.diagnosis.substring(0, 80)}...`
                              : visit.diagnosis}
                          </Text>
                        )}
                        {visit.treatment && (
                          <Text size="sm">
                            <strong>💊 Treatment:</strong>{' '}
                            {visit.treatment.length > 80
                              ? `${visit.treatment.substring(0, 80)}...`
                              : visit.treatment}
                          </Text>
                        )}
                      </div>
                    )}

                    {/* Used Materials / Procedures */}
                    {hasMaterials && (
                      <div
                        style={{
                          backgroundColor: colors.success.light,
                          padding: spacing.xs,
                          borderRadius: borderRadius.sm,
                          borderLeft: `3px solid ${colors.success.main}`,
                        }}
                      >
                        <Text
                          size="sm"
                          style={{ fontWeight: fontWeight.medium, marginBottom: spacing.xs }}
                        >
                          🏥 Procedures & Materials ({materials.length}):
                        </Text>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                          {materials.slice(0, 4).map((m, idx) => (
                            <span
                              key={`${visit.id}-material-${idx}`}
                              style={{
                                fontSize: fontSize.xs,
                                backgroundColor: colors.neutral.white,
                                padding: '2px 6px',
                                borderRadius: borderRadius.sm,
                                border: `1px solid ${colors.neutral.border}`,
                              }}
                            >
                              {m.name} x{m.quantity}
                            </span>
                          ))}
                          {materials.length > 4 && (
                            <span
                              style={{
                                fontSize: fontSize.xs,
                                backgroundColor: colors.primary.light,
                                color: colors.primary.main,
                                padding: '2px 6px',
                                borderRadius: borderRadius.sm,
                              }}
                            >
                              +{materials.length - 4} more
                            </span>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}

            {/* Pagination Controls */}
            {visits.length > PAGINATION.PANEL && (
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  gap: spacing.sm,
                  padding: spacing.md,
                  borderTop: `1px solid ${colors.neutral.border}`,
                  marginTop: spacing.sm,
                }}
              >
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setVisitsPage((p) => Math.max(0, p - 1))}
                  disabled={visitsPage === 0}
                >
                  ← Previous
                </Button>
                <Text size="sm" style={{ color: colors.neutral.textMuted }}>
                  Page {visitsPage + 1} of {Math.ceil(visits.length / PAGINATION.PANEL)}
                </Text>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() =>
                    setVisitsPage((p) =>
                      Math.min(Math.ceil(visits.length / PAGINATION.PANEL) - 1, p + 1)
                    )
                  }
                  disabled={visitsPage >= Math.ceil(visits.length / PAGINATION.PANEL) - 1}
                >
                  Next →
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </Card>
  );
}
