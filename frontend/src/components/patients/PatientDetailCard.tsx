import { PatientResponse, VisitResponse } from '../../api';
import { Card, CardHeader, CardContent, Badge, Text, Button } from '../ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { getSpeciesInfo, getLabelInfo, getVisitTypeInfo, getVisitStatusConfig } from '../../constants';
import { formatDate, formatTime } from '../../utils';
import { formatWeightDisplay } from '../../utils/formatting';
import { useI18n } from '../../i18n';

export interface PatientDetailCardProps {
  patient: PatientResponse;
  visits: VisitResponse[];
  onVisitClick?: (visit: VisitResponse) => void;
  onEditPatient?: () => void;
}

function calculateAge(dateOfBirth: string | null, t: (key: string) => string): string {
  if (!dateOfBirth) return t('patients.unknown');
  var birth = new Date(dateOfBirth);
  var now = new Date();
  var years = now.getFullYear() - birth.getFullYear();
  var months = now.getMonth() - birth.getMonth();
  if (years > 0) return `${years} ${years > 1 ? t('patients.yearPlural') : t('patients.yearSingular')}`;
  if (months > 0) return `${months} ${months > 1 ? t('patients.monthPlural') : t('patients.monthSingular')}`;
  return t('patients.lessThanOneMonth');
}

export function PatientDetailCard({
  patient,
  visits,
  onVisitClick,
  onEditPatient,
}: PatientDetailCardProps) {
  const { t } = useI18n();
  const speciesInfo = getSpeciesInfo(patient.species);
  const age = calculateAge(patient.dateOfBirth || null, t);

  // Sort visits by date descending (most recent first)
  const sortedVisits = [...visits].sort(
    (a, b) => new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime()
  );

  return (
    <Card style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Patient Header Section */}
      <CardHeader>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.xs }}>
              <span style={{ fontSize: fontSize.xxl }}>{speciesInfo.emoji}</span>
              <h2
                style={{
                  margin: 0,
                  fontSize: fontSize.xl,
                  fontWeight: fontWeight.bold,
                  color: colors.secondary.main,
                }}
              >
                {patient.name}
              </h2>
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: spacing.sm, alignItems: 'center' }}>
              <Text variant="caption" size="md">
                {speciesInfo.label}
                {patient.breed && ` - ${patient.breed}`}
              </Text>

              {patient.gender && patient.gender !== 'UNKNOWN' && (
                <span style={{ fontSize: fontSize.md }}>
                  {patient.gender === 'MALE' ? '♂️' : '♀️'}
                </span>
              )}

              {patient.neutered && (
                <Badge variant="primary" style={{ fontSize: fontSize.xs }}>
                  {patient.gender === 'FEMALE' ? t('patients.spayed') : t('patients.neuteredMale')}
                </Badge>
              )}

              <Text variant="muted" size="sm">
                {age}
              </Text>
            </div>

            {/* Patient Labels */}
            {patient.labels && patient.labels.length > 0 && (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: spacing.xs, marginTop: spacing.sm }}>
                {patient.labels.map((label) => {
                  const labelInfo = getLabelInfo(label);
                  return (
                    <Badge key={label} variant={labelInfo.variant} style={{ fontSize: fontSize.xs }}>
                      {labelInfo.icon} {labelInfo.display}
                    </Badge>
                  );
                })}
              </div>
            )}
          </div>

          {onEditPatient && (
            <Button variant="ghost" size="sm" onClick={onEditPatient}>
              {t('common.edit')}
            </Button>
          )}
        </div>
      </CardHeader>

      {/* Patient Info Grid */}
      <CardContent>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: spacing.md,
            padding: spacing.md,
            backgroundColor: colors.neutral.background,
            borderRadius: borderRadius.md,
            marginBottom: spacing.lg,
          }}
        >
          {patient.weight && (
            <div>
              <Text variant="muted" size="sm" style={{ marginBottom: spacing.xs }}>
                {t('patients.weight')}
              </Text>
              <Text weight="medium">{formatWeightDisplay(patient.weight)}</Text>
            </div>
          )}

          {patient.microchipNumber && (
            <div>
              <Text variant="muted" size="sm" style={{ marginBottom: spacing.xs }}>
                {t('patients.microchip')}
              </Text>
              <Text weight="medium" style={{ fontFamily: 'monospace', fontSize: fontSize.sm }}>
                {patient.microchipNumber}
              </Text>
            </div>
          )}

          {patient.color && (
            <div>
              <Text variant="muted" size="sm" style={{ marginBottom: spacing.xs }}>
                {t('patients.color')}
              </Text>
              <Text weight="medium">{patient.color}</Text>
            </div>
          )}

          {patient.dateOfBirth && (
            <div>
              <Text variant="muted" size="sm" style={{ marginBottom: spacing.xs }}>
                {t('patients.dateOfBirth')}
              </Text>
              <Text weight="medium">{formatDate(patient.dateOfBirth)}</Text>
            </div>
          )}
        </div>

        {/* Notes */}
        {patient.notes && (
          <div
            style={{
              padding: spacing.md,
              backgroundColor: colors.warning.light,
              borderRadius: borderRadius.md,
              borderLeft: `4px solid ${colors.warning.main}`,
              marginBottom: spacing.lg,
            }}
          >
            <Text variant="muted" size="sm" weight="medium" style={{ marginBottom: spacing.xs }}>
              {t('patients.notes')}
            </Text>
            <Text size="sm">{patient.notes}</Text>
          </div>
        )}

        {/* Visits History Section */}
        <div>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: spacing.md,
              paddingBottom: spacing.sm,
              borderBottom: `2px solid ${colors.neutral.border}`,
            }}
          >
            <Text weight="semibold" size="lg">
              {t('patients.visitHistory')}
            </Text>
            <Badge variant="secondary">{visits.length} {t('patients.visits')}</Badge>
          </div>

          {/* Visits Table */}
          {sortedVisits.length === 0 ? (
            <div style={{ textAlign: 'center', padding: spacing.xl }}>
              <Text variant="muted">{t('patients.noVisitsRecorded')}</Text>
            </div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table
                style={{
                  width: '100%',
                  borderCollapse: 'collapse',
                  fontSize: fontSize.sm,
                }}
              >
                <thead>
                  <tr
                    style={{
                      backgroundColor: colors.neutral.background,
                      borderBottom: `2px solid ${colors.neutral.border}`,
                    }}
                  >
                    <th
                      style={{
                        padding: spacing.sm,
                        textAlign: 'left',
                        fontWeight: fontWeight.semibold,
                        color: colors.neutral.text,
                      }}
                    >
                      {t('patients.date')}
                    </th>
                    <th
                      style={{
                        padding: spacing.sm,
                        textAlign: 'left',
                        fontWeight: fontWeight.semibold,
                        color: colors.neutral.text,
                      }}
                    >
                      {t('patients.type')}
                    </th>
                    <th
                      style={{
                        padding: spacing.sm,
                        textAlign: 'left',
                        fontWeight: fontWeight.semibold,
                        color: colors.neutral.text,
                      }}
                    >
                      {t('patients.veterinarian')}
                    </th>
                    <th
                      style={{
                        padding: spacing.sm,
                        textAlign: 'left',
                        fontWeight: fontWeight.semibold,
                        color: colors.neutral.text,
                      }}
                    >
                      {t('patients.reason')}
                    </th>
                    <th
                      style={{
                        padding: spacing.sm,
                        textAlign: 'left',
                        fontWeight: fontWeight.semibold,
                        color: colors.neutral.text,
                      }}
                    >
                      {t('patients.status')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {sortedVisits.map((visit) => {
                    const statusConfig = getVisitStatusConfig(visit.status);
                    // Try to infer visit type from reason or diagnosis
                    // This is a fallback - ideally visits would have a type field
                    const visitType = inferVisitType(visit);
                    const visitTypeInfo = getVisitTypeInfo(visitType);

                    return (
                      <tr
                        key={visit.id}
                        onClick={() => onVisitClick?.(visit)}
                        style={{
                          borderBottom: `1px solid ${colors.neutral.border}`,
                          cursor: onVisitClick ? 'pointer' : 'default',
                          transition: 'background-color 0.15s ease',
                        }}
                        onMouseEnter={(e) => {
                          if (onVisitClick) {
                            e.currentTarget.style.backgroundColor = colors.primary.light;
                          }
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.backgroundColor = 'transparent';
                        }}
                      >
                        <td style={{ padding: spacing.sm }}>
                          <div>
                            <Text size="sm" weight="medium">
                              {formatDate(visit.visitDate)}
                            </Text>
                            <Text variant="muted" size="xs">
                              {formatTime(visit.visitDate)}
                            </Text>
                          </div>
                        </td>
                        <td style={{ padding: spacing.sm }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.xs }}>
                            <span style={{ fontSize: fontSize.md }}>{visitTypeInfo.icon}</span>
                            <Text size="sm">{visitTypeInfo.label}</Text>
                          </div>
                        </td>
                        <td style={{ padding: spacing.sm }}>
                          <Text size="sm">
                            {visit.veterinarianName ? `Dr. ${visit.veterinarianName}` : '-'}
                          </Text>
                        </td>
                        <td style={{ padding: spacing.sm }}>
                          <Text
                            size="sm"
                            style={{
                              maxWidth: '300px',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            {visit.reason || '-'}
                          </Text>
                        </td>
                        <td style={{ padding: spacing.sm }}>
                          <Badge
                            variant={
                              visit.status === 'COMPLETED'
                                ? 'success'
                                : visit.status === 'CANCELLED'
                                ? 'secondary'
                                : visit.status === 'IN_PROGRESS'
                                ? 'warning'
                                : 'primary'
                            }
                            style={{ fontSize: fontSize.xs }}
                          >
                            {statusConfig.label}
                          </Badge>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

/**
 * Infer visit type from visit data (reason, diagnosis, etc.)
 * This is a simple heuristic - in production, visits should have a type field
 */
function inferVisitType(visit: VisitResponse): string {
  const searchText = `${visit.reason || ''} ${visit.diagnosis || ''} ${visit.treatment || ''}`.toLowerCase();

  if (searchText.includes('vaccin') || searchText.includes('immuniz')) return 'VACCINATION';
  if (searchText.includes('surgery') || searchText.includes('operation')) return 'SURGERY';
  if (searchText.includes('dental') || searchText.includes('teeth') || searchText.includes('tooth')) return 'DENTAL';
  if (searchText.includes('lab') || searchText.includes('test') || searchText.includes('blood')) return 'LAB_WORK';
  if (searchText.includes('ultrasound') || searchText.includes('imaging')) return 'ULTRASOUND';
  if (searchText.includes('cardio') || searchText.includes('heart')) return 'CARDIOLOGY';
  if (searchText.includes('emergency') || searchText.includes('urgent')) return 'EMERGENCY';
  if (searchText.includes('groom') || searchText.includes('bath')) return 'GROOMING';
  if (searchText.includes('follow') || searchText.includes('checkup')) return 'FOLLOW_UP';
  if (searchText.includes('check')) return 'CHECKUP';

  return 'CONSULTATION';
}
