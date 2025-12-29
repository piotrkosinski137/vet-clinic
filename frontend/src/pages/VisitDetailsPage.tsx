import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { VisitResponse, PatientResponse, ClientResponse } from '../api/types';
import { useI18n } from '../i18n';
import { getVisitTypeInfo, LOCALE, DATE_FORMAT_OPTIONS } from '../constants';
import { getVisitStatusConfig } from '../constants/visitStatus';
import { formatCurrency } from '../hooks/usePriceList';
import { formatWeightDisplay, formatTemperatureDisplay } from '../utils/formatting';
import {
  PageHeader,
  Card,
  CardContent,
  Button,
  Badge,
  Text,
  Loading,
  EmptyState,
} from '../components/ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../theme';

export function VisitDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { t } = useI18n();
  const [visit, setVisit] = useState<VisitResponse | null>(null);
  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [client, setClient] = useState<ClientResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!id) return;
      setLoading(true);
      setError(null);
      try {
        const visitData = await api.getVisit(id);
        setVisit(visitData);

        // Fetch patient and client info
        if (visitData.patientId) {
          const patientData = await api.getPatient(visitData.patientId);
          setPatient(patientData);
        }
        if (visitData.clientId) {
          const clientData = await api.getClient(visitData.clientId);
          setClient(clientData);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : t('errors.failedToLoad'));
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id, t]);

  if (loading) {
    return (
      <div style={{ padding: spacing.xxl, textAlign: 'center' }}>
        <Loading text={t('common.loading')} />
      </div>
    );
  }

  if (error || !visit) {
    return (
      <EmptyState
        icon="❌"
        title={t('errors.notFound')}
        description={error || t('visits.visitNotFound')}
        action={
          <Button variant="primary" onClick={() => navigate('/schedule')}>
            {t('visits.schedule')}
          </Button>
        }
      />
    );
  }

  const visitTypeInfo = getVisitTypeInfo(visit.visitType || 'CONSULTATION');
  const statusConfig = getVisitStatusConfig(visit.status);
  const visitDate = new Date(visit.visitDate);
  const formattedDate = visitDate.toLocaleDateString(LOCALE.PL);
  const formattedTime = visitDate.toLocaleTimeString(LOCALE.PL, DATE_FORMAT_OPTIONS.TIME_SHORT);

  const totalCost = (visit.usedMaterials || []).reduce(
    (sum, m) => sum + m.quantity * m.sellPrice,
    0
  );

  const sectionStyle: React.CSSProperties = {
    marginBottom: spacing.lg,
  };

  const sectionHeaderStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    gap: spacing.sm,
    marginBottom: spacing.md,
    paddingBottom: spacing.sm,
    borderBottom: `2px solid ${colors.neutral.border}`,
  };

  const sectionIconStyle: React.CSSProperties = {
    fontSize: fontSize.xl,
  };

  const sectionTitleStyle: React.CSSProperties = {
    fontSize: fontSize.lg,
    fontWeight: fontWeight.semibold,
    color: colors.secondary.main,
  };

  const infoGridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
    gap: spacing.md,
  };

  const infoItemStyle: React.CSSProperties = {
    padding: spacing.md,
    backgroundColor: colors.neutral.background,
    borderRadius: borderRadius.md,
  };

  const labelStyle: React.CSSProperties = {
    fontSize: fontSize.xs,
    color: colors.neutral.textMuted,
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    marginBottom: spacing.xs,
  };

  const valueStyle: React.CSSProperties = {
    fontSize: fontSize.md,
    fontWeight: fontWeight.medium,
    color: colors.neutral.text,
  };

  const contentBoxStyle: React.CSSProperties = {
    padding: spacing.md,
    backgroundColor: colors.neutral.background,
    borderRadius: borderRadius.md,
    whiteSpace: 'pre-wrap',
    lineHeight: 1.6,
  };

  return (
    <div>
      <PageHeader title={t('visits.appointmentDetails')} />

      {/* Header Card with Visit Overview */}
      <Card variant="elevated" style={{ marginBottom: spacing.xl }}>
        <CardContent>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: spacing.lg }}>
            {/* Left: Visit Info */}
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: spacing.md, marginBottom: spacing.md }}>
                <span style={{ fontSize: '32px' }}>{visitTypeInfo.icon}</span>
                <div>
                  <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold }}>
                    {visitTypeInfo.label}
                  </Text>
                  <Text variant="muted">
                    {formattedDate} {t('common.at')} {formattedTime}
                  </Text>
                </div>
                <Badge
                  style={{
                    backgroundColor: statusConfig.bgColor,
                    color: statusConfig.color,
                    padding: `${spacing.xs} ${spacing.md}`,
                    fontSize: fontSize.sm,
                  }}
                >
                  {statusConfig.label}
                </Badge>
              </div>
              {visit.reason && (
                <Text variant="muted" style={{ marginTop: spacing.sm }}>
                  <strong>{t('visits.reason')}:</strong> {visit.reason}
                </Text>
              )}
            </div>

            {/* Right: Patient & Client Info */}
            <div style={{ display: 'flex', gap: spacing.xl }}>
              {patient && (
                <div
                  style={{
                    padding: spacing.md,
                    backgroundColor: colors.primary.light,
                    borderRadius: borderRadius.md,
                    cursor: 'pointer',
                  }}
                  onClick={() => navigate(`/patients/${patient.id}`)}
                >
                  <Text variant="muted" size="sm">{t('visits.patient')}</Text>
                  <Text style={{ fontWeight: fontWeight.semibold, fontSize: fontSize.lg }}>
                    🐾 {patient.name}
                  </Text>
                  <Text variant="muted" size="sm">{patient.species} {patient.breed && `• ${patient.breed}`}</Text>
                </div>
              )}
              {client && (
                <div
                  style={{
                    padding: spacing.md,
                    backgroundColor: colors.info.light,
                    borderRadius: borderRadius.md,
                    cursor: 'pointer',
                  }}
                  onClick={() => navigate('/clients')}
                >
                  <Text variant="muted" size="sm">{t('visits.owner')}</Text>
                  <Text style={{ fontWeight: fontWeight.semibold, fontSize: fontSize.lg }}>
                    👤 {client.firstName} {client.lastName}
                  </Text>
                  <Text variant="muted" size="sm">{client.phone}</Text>
                </div>
              )}
              {visit.veterinarianName && (
                <div style={{ padding: spacing.md, backgroundColor: colors.success.light, borderRadius: borderRadius.md }}>
                  <Text variant="muted" size="sm">{t('visits.veterinarian')}</Text>
                  <Text style={{ fontWeight: fontWeight.semibold, fontSize: fontSize.lg }}>
                    👨‍⚕️ Dr. {visit.veterinarianName}
                  </Text>
                </div>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 5 Sections Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: spacing.lg }}>
        {/* 1. Interview Section */}
        <Card variant="default" style={sectionStyle}>
          <CardContent>
            <div style={sectionHeaderStyle}>
              <span style={sectionIconStyle}>🗣️</span>
              <span style={sectionTitleStyle}>{t('visits.tabInterview')}</span>
            </div>
            {visit.interview ? (
              <div style={contentBoxStyle}>{visit.interview}</div>
            ) : (
              <Text variant="muted" style={{ fontStyle: 'italic' }}>{t('common.noData')}</Text>
            )}
          </CardContent>
        </Card>

        {/* 2. Examination Section */}
        <Card variant="default" style={sectionStyle}>
          <CardContent>
            <div style={sectionHeaderStyle}>
              <span style={sectionIconStyle}>🩺</span>
              <span style={sectionTitleStyle}>{t('visits.tabExamination')}</span>
            </div>
            <div style={infoGridStyle}>
              <div style={infoItemStyle}>
                <div style={labelStyle}>{t('visits.weightKg')}</div>
                <div style={valueStyle}>{formatWeightDisplay(visit.weight)}</div>
              </div>
              <div style={infoItemStyle}>
                <div style={labelStyle}>{t('visits.temperatureC')}</div>
                <div style={valueStyle}>{formatTemperatureDisplay(visit.temperature)}</div>
              </div>
            </div>
            {visit.examination && (
              <div style={{ ...contentBoxStyle, marginTop: spacing.md }}>{visit.examination}</div>
            )}
          </CardContent>
        </Card>

        {/* 3. Diagnostics Section */}
        <Card variant="default" style={sectionStyle}>
          <CardContent>
            <div style={sectionHeaderStyle}>
              <span style={sectionIconStyle}>🧪</span>
              <span style={sectionTitleStyle}>{t('visits.tabDiagnostics')}</span>
            </div>
            {visit.diagnosis ? (
              <div style={contentBoxStyle}>{visit.diagnosis}</div>
            ) : (
              <Text variant="muted" style={{ fontStyle: 'italic' }}>{t('common.noData')}</Text>
            )}
          </CardContent>
        </Card>

        {/* 4. Recommendations Section */}
        <Card variant="default" style={sectionStyle}>
          <CardContent>
            <div style={sectionHeaderStyle}>
              <span style={sectionIconStyle}>📋</span>
              <span style={sectionTitleStyle}>{t('visits.tabRecommendations')}</span>
            </div>
            {visit.treatment && (
              <div style={{ marginBottom: spacing.md }}>
                <Text size="sm" weight="medium" style={{ marginBottom: spacing.xs }}>
                  {t('visits.treatmentPlan')}
                </Text>
                <div style={contentBoxStyle}>{visit.treatment}</div>
              </div>
            )}
            {visit.recommendations && (
              <div style={{ marginBottom: spacing.md }}>
                <Text size="sm" weight="medium" style={{ marginBottom: spacing.xs }}>
                  {t('visits.recommendations')}
                </Text>
                <div style={contentBoxStyle}>{visit.recommendations}</div>
              </div>
            )}
            {visit.nextVisitDate && (
              <div style={{ ...infoItemStyle, display: 'inline-block' }}>
                <div style={labelStyle}>{t('visits.nextVisitDate')}</div>
                <div style={valueStyle}>📅 {new Date(visit.nextVisitDate).toLocaleDateString(LOCALE.PL)}</div>
              </div>
            )}
            {!visit.treatment && !visit.recommendations && !visit.nextVisitDate && (
              <Text variant="muted" style={{ fontStyle: 'italic' }}>{t('common.noData')}</Text>
            )}
          </CardContent>
        </Card>

        {/* 5. Billing Section - Full Width */}
        <Card variant="default" style={{ ...sectionStyle, gridColumn: 'span 2' }}>
          <CardContent>
            <div style={sectionHeaderStyle}>
              <span style={sectionIconStyle}>💰</span>
              <span style={sectionTitleStyle}>{t('visits.tabBilling')}</span>
              {totalCost > 0 && (
                <Badge variant="success" style={{ marginLeft: 'auto', fontSize: fontSize.md, padding: `${spacing.xs} ${spacing.md}` }}>
                  {t('common.total')}: {formatCurrency(totalCost)}
                </Badge>
              )}
            </div>
            {visit.usedMaterials && visit.usedMaterials.length > 0 ? (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `2px solid ${colors.neutral.border}` }}>
                      <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('materials.name')}</th>
                      <th style={{ padding: spacing.md, textAlign: 'center' }}>{t('materials.quantity')}</th>
                      <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('materials.unitPrice')}</th>
                      <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('common.total')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {visit.usedMaterials.map((material, idx) => (
                      <tr key={idx} style={{ borderBottom: `1px solid ${colors.neutral.borderLight}` }}>
                        <td style={{ padding: spacing.md }}>{material.name}</td>
                        <td style={{ padding: spacing.md, textAlign: 'center' }}>
                          {material.quantity} {material.unit}
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right' }}>
                          {formatCurrency(material.sellPrice)}
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right', fontWeight: fontWeight.medium }}>
                          {formatCurrency(material.quantity * material.sellPrice)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr style={{ backgroundColor: colors.success.light }}>
                      <td colSpan={3} style={{ padding: spacing.md, textAlign: 'right', fontWeight: fontWeight.semibold }}>
                        {t('materials.clientTotal')}
                      </td>
                      <td style={{ padding: spacing.md, textAlign: 'right', fontWeight: fontWeight.bold, fontSize: fontSize.lg, color: colors.success.main }}>
                        {formatCurrency(totalCost)}
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>
            ) : (
              <Text variant="muted" style={{ fontStyle: 'italic' }}>{t('materials.noMaterialsUsed')}</Text>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Notes Section */}
      {visit.notes && (
        <Card variant="default" style={{ marginTop: spacing.lg }}>
          <CardContent>
            <div style={sectionHeaderStyle}>
              <span style={sectionIconStyle}>📝</span>
              <span style={sectionTitleStyle}>{t('common.notes')}</span>
            </div>
            <div style={contentBoxStyle}>{visit.notes}</div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
