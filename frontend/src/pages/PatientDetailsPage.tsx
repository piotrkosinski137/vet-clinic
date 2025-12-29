import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { PatientResponse, VisitResponse, VaccinationCertificateResponse } from '../api/types';
import {
  Button,
  Card,
  Badge,
  Text,
  Loading,
  PageHeader,
} from '../components/ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../theme';
import { getSpeciesInfo, getLabelInfo, VISIT_TYPE_OPTIONS } from '../constants';
import { getVisitStatusConfig } from '../constants/visitStatus';
import { useI18n } from '../i18n';

export function PatientDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { t } = useI18n();

  const [patient, setPatient] = useState<PatientResponse | null>(null);
  const [visits, setVisits] = useState<VisitResponse[]>([]);
  const [certificates, setCertificates] = useState<VaccinationCertificateResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;

    const loadData = async () => {
      setLoading(true);
      setError(null);
      try {
        const [patientData, visitsData, certificatesData] = await Promise.all([
          api.getPatient(id),
          api.getPatientVisits(id),
          api.getCertificatesByPatient(id),
        ]);
        setPatient(patientData);
        setVisits(visitsData);
        setCertificates(certificatesData);
      } catch (err) {
        setError(err instanceof Error ? err.message : t('errors.failedToLoad'));
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [id, t]);

  if (loading) {
    return <Loading text={t('patients.loading')} />;
  }

  if (error || !patient) {
    return (
      <Card style={{ textAlign: 'center', padding: spacing.xl }}>
        <Text variant="muted" style={{ marginBottom: spacing.md }}>
          {error || t('patients.patientNotFound')}
        </Text>
        <Button variant="primary" onClick={() => navigate('/patients')}>
          {t('patients.backToPatients')}
        </Button>
      </Card>
    );
  }

  const speciesInfo = getSpeciesInfo(patient.species);

  return (
    <div>
      <PageHeader
        title={
          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.md }}>
            <span style={{ fontSize: '32px' }}>{speciesInfo.emoji}</span>
            <div>
              <div style={{ fontSize: fontSize.xxl, fontWeight: fontWeight.bold }}>
                {patient.name}
              </div>
              <Text variant="muted" size="sm">
                {speciesInfo.label} {patient.breed && `• ${patient.breed}`}
              </Text>
            </div>
          </div>
        }
        actions={
          <Button variant="ghost" onClick={() => navigate('/patients')}>
            ← {t('patients.backToPatients')}
          </Button>
        }
      />

      {/* Patient Info Section */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.lg, marginBottom: spacing.xl }}>
        <Card style={{ padding: spacing.lg }}>
          <Text size="lg" weight="semibold" style={{ marginBottom: spacing.md }}>
            {t('patients.patientInformation')}
          </Text>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
            <InfoRow label={t('patients.species')} value={speciesInfo.label} />
            <InfoRow label={t('patients.breed')} value={patient.breed || '-'} />
            <InfoRow label={t('patients.gender')} value={patient.gender ? t(`common.${patient.gender.toLowerCase()}`) : t('common.unknown')} />
            <InfoRow label={t('patients.neutered')} value={patient.neutered ? t('common.yes') : t('common.no')} />
            <InfoRow label={t('patients.weight')} value={patient.weight ? `${patient.weight} kg` : '-'} />
            <InfoRow label={t('patients.dateOfBirth')} value={patient.dateOfBirth || '-'} />
            <InfoRow label={t('patients.microchip')} value={patient.microchipNumber || '-'} />
            <InfoRow label={t('patients.color')} value={patient.color || '-'} />
          </div>

          {patient.labels && patient.labels.length > 0 && (
            <div style={{ marginTop: spacing.md }}>
              <Text size="sm" weight="medium" style={{ marginBottom: spacing.xs }}>{t('patients.labels')}</Text>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: spacing.xs }}>
                {patient.labels.map((label) => {
                  const info = getLabelInfo(label);
                  return (
                    <Badge key={label} variant={info.variant}>
                      {info.icon} {info.display}
                    </Badge>
                  );
                })}
              </div>
            </div>
          )}

          {patient.notes && (
            <div style={{ marginTop: spacing.md }}>
              <Text size="sm" weight="medium" style={{ marginBottom: spacing.xs }}>{t('patients.notes')}</Text>
              <Text variant="muted" size="sm">{patient.notes}</Text>
            </div>
          )}
        </Card>

        {/* Certificates Section */}
        <Card style={{ padding: spacing.lg }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.md }}>
            <Text size="lg" weight="semibold">
              {t('patients.certificates')} ({certificates.length})
            </Text>
          </div>

          {certificates.length === 0 ? (
            <div style={{ textAlign: 'center', padding: spacing.lg }}>
              <Text variant="muted">{t('patients.noCertificates')}</Text>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: spacing.sm }}>
              {certificates.map((cert) => (
                <div
                  key={cert.id}
                  style={{
                    padding: spacing.sm,
                    backgroundColor: cert.isValid ? colors.success.light : colors.danger.light,
                    borderRadius: borderRadius.md,
                    border: `1px solid ${cert.isValid ? colors.success.main : colors.danger.main}`,
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <Text weight="medium" size="sm">{cert.vaccineName}</Text>
                      <Text variant="muted" size="sm">
                        {cert.certificateType} • {cert.batchNumber || t('certificates.noBatch')}
                      </Text>
                    </div>
                    <Badge variant={cert.isValid ? 'success' : 'danger'}>
                      {cert.isValid ? t('certificates.valid') : t('certificates.invalid')}
                    </Badge>
                  </div>
                  {cert.expirationDate && (
                    <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
                      {t('certificates.expires')}: {new Date(cert.expirationDate).toLocaleDateString()}
                    </Text>
                  )}
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>

      {/* Visits Section */}
      <Card style={{ padding: spacing.lg }}>
        <Text size="lg" weight="semibold" style={{ marginBottom: spacing.md }}>
          {t('patients.visitHistory')} ({visits.length})
        </Text>

        {visits.length === 0 ? (
          <div style={{ textAlign: 'center', padding: spacing.xl }}>
            <Text variant="muted">{t('patients.noVisitsRecorded')}</Text>
          </div>
        ) : (
          <div style={{
            backgroundColor: colors.neutral.white,
            borderRadius: borderRadius.md,
            overflow: 'hidden',
            border: `1px solid ${colors.neutral.border}`,
          }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `2px solid ${colors.neutral.border}` }}>
                  <TableHeader>{t('patients.date')}</TableHeader>
                  <TableHeader>{t('patients.type')}</TableHeader>
                  <TableHeader>{t('patients.status')}</TableHeader>
                  <TableHeader>{t('patients.veterinarian')}</TableHeader>
                  <TableHeader>{t('patients.diagnosis')}</TableHeader>
                </tr>
              </thead>
              <tbody>
                {visits.map((visit) => {
                  const statusConfig = getVisitStatusConfig(visit.status);
                  const visitType = VISIT_TYPE_OPTIONS.find(t => t.value === visit.visitType);
                  return (
                    <tr
                      key={visit.id}
                      style={{
                        borderBottom: `1px solid ${colors.neutral.border}`,
                        cursor: 'pointer',
                      }}
                      onClick={() => navigate(`/schedule`)}
                      onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = colors.primary.light; }}
                      onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; }}
                    >
                      <TableCell>
                        {new Date(visit.visitDate).toLocaleDateString()} {new Date(visit.visitDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })}
                      </TableCell>
                      <TableCell>
                        {visitType ? `${visitType.icon} ${visitType.label}` : visit.visitType || '-'}
                      </TableCell>
                      <TableCell>
                        <Badge style={{ backgroundColor: statusConfig.bgColor, color: statusConfig.color }}>
                          {statusConfig.label}
                        </Badge>
                      </TableCell>
                      <TableCell>{visit.veterinarianName || '-'}</TableCell>
                      <TableCell style={{ maxWidth: '300px' }}>
                        <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {visit.diagnosis || '-'}
                        </div>
                      </TableCell>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <Text variant="muted" size="sm">{label}</Text>
      <Text weight="medium">{value}</Text>
    </div>
  );
}

function TableHeader({ children }: { children: React.ReactNode }) {
  return (
    <th style={{
      textAlign: 'left',
      padding: spacing.md,
      fontSize: fontSize.sm,
      fontWeight: fontWeight.semibold,
      color: colors.neutral.textLight,
    }}>
      {children}
    </th>
  );
}

function TableCell({ children, style }: { children: React.ReactNode; style?: React.CSSProperties }) {
  return (
    <td style={{
      padding: spacing.md,
      fontSize: fontSize.sm,
      ...style,
    }}>
      {children}
    </td>
  );
}
