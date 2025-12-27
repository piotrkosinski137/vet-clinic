import { PatientResponse } from '../../api';
import { Button, Card, Text, Loading, Badge } from '../ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';
import { getSpeciesInfo, getLabelInfo } from '../../constants';

interface PatientsListProps {
  patients: PatientResponse[];
  selectedPatient: PatientResponse | null;
  onSelectPatient: (patient: PatientResponse) => void;
  onCreatePatient: () => void;
  onEditPatient: (patient: PatientResponse) => void;
  onDeletePatient: (patient: PatientResponse) => void;
  loading: boolean;
}

export function PatientsList({
  patients,
  selectedPatient,
  onSelectPatient,
  onCreatePatient,
  onEditPatient,
  onDeletePatient,
  loading,
}: PatientsListProps) {
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
        <Text style={{ fontWeight: fontWeight.semibold }}>Patients ({patients.length})</Text>
        <Button variant="primary" size="sm" onClick={onCreatePatient}>
          + Add
        </Button>
      </div>

      <div style={{ flex: 1, overflowY: 'auto' }}>
        {loading ? (
          <Loading text="Loading patients..." />
        ) : patients.length === 0 ? (
          <div style={{ textAlign: 'center', padding: spacing.xl }}>
            <Text variant="muted">No patients yet</Text>
          </div>
        ) : (
          patients.map((patient) => {
            const speciesInfo = getSpeciesInfo(patient.species);
            return (
              <div
                key={patient.id}
                onClick={() => onSelectPatient(patient)}
                style={{
                  padding: spacing.sm,
                  marginBottom: spacing.xs,
                  borderRadius: borderRadius.sm,
                  cursor: 'pointer',
                  backgroundColor:
                    selectedPatient?.id === patient.id
                      ? colors.primary.light
                      : colors.neutral.background,
                  border: `2px solid ${
                    selectedPatient?.id === patient.id ? colors.primary.main : 'transparent'
                  }`,
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
                  <span style={{ fontSize: fontSize.lg }}>{speciesInfo.emoji}</span>
                  <div style={{ flex: 1 }}>
                    <Text style={{ fontWeight: fontWeight.medium }}>{patient.name}</Text>
                    <Text variant="muted" size="sm">
                      {speciesInfo.label}
                      {patient.breed ? ` - ${patient.breed}` : ''}
                    </Text>
                  </div>
                  <div style={{ display: 'flex', gap: spacing.xs }}>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={(e) => {
                        e.stopPropagation();
                        onEditPatient(patient);
                      }}
                    >
                      Edit
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={(e) => {
                        e.stopPropagation();
                        onDeletePatient(patient);
                      }}
                    >
                      Del
                    </Button>
                  </div>
                </div>
                {patient.labels && patient.labels.length > 0 && (
                  <div
                    style={{
                      display: 'flex',
                      flexWrap: 'wrap',
                      gap: spacing.xs,
                      marginTop: spacing.xs,
                    }}
                  >
                    {patient.labels.map((label) => {
                      const info = getLabelInfo(label);
                      return (
                        <Badge key={label} variant={info.variant} style={{ fontSize: fontSize.xs }}>
                          {info.icon} {info.display}
                        </Badge>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>
    </Card>
  );
}
