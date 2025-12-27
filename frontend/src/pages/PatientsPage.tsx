import { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { usePatients } from '../hooks';
import { PatientRequest, PatientResponse, PatientLabel } from '../api';
import {
  Button,
  Card,
  Modal,
  ModalTitle,
  ModalActions,
  Badge,
  FormField,
  Input,
  Select,
  TextArea,
  Text,
  Loading,
  PageHeader,
  useToast,
  Pagination,
  SearchFilter,
} from '../components/ui';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../theme';
import { useI18n } from '../i18n';
import {
  SPECIES_OPTIONS,
  PATIENT_LABELS,
  getSpeciesInfo,
  getLabelInfo,
  PAGINATION,
  getBreedsForSpecies,
} from '../constants';
import { isSpecies, isGender } from '../utils';

export function PatientsPage() {
  const navigate = useNavigate();
  const { t } = useI18n();
  const { patients, loading, error, createPatient, updatePatient, deletePatient, refresh } = usePatients();
  const { success, error: showError } = useToast();
  const [showForm, setShowForm] = useState(false);
  const [editingPatient, setEditingPatient] = useState<PatientResponse | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState<PatientRequest>({
    name: '',
    species: 'DOG',
    breed: '',
    gender: 'UNKNOWN',
    neutered: false,
    notes: '',
    labels: [],
  });

  // Breed management
  const [availableBreeds, setAvailableBreeds] = useState<readonly string[]>([]);
  const [customBreed, setCustomBreed] = useState('');

  // Search and pagination
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);

  // Update available breeds when species changes
  useEffect(() => {
    setAvailableBreeds(getBreedsForSpecies(formData.species));
  }, [formData.species]);

  // Filter patients by search - memoized to avoid recalculating on every render
  const filteredPatients = useMemo(() => {
    const query = searchQuery.toLowerCase();
    return patients.filter((patient) =>
      patient.name.toLowerCase().includes(query) ||
      patient.species.toLowerCase().includes(query) ||
      patient.breed?.toLowerCase().includes(query) ||
      patient.notes?.toLowerCase().includes(query)
    );
  }, [patients, searchQuery]);

  // Paginated patients - memoized for performance
  const paginatedPatients = useMemo(() =>
    filteredPatients.slice(
      currentPage * PAGINATION.DEFAULT_PAGE_SIZE,
      (currentPage + 1) * PAGINATION.DEFAULT_PAGE_SIZE
    ),
    [filteredPatients, currentPage]
  );

  // Reset page when search changes
  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    setCurrentPage(0);
  };

  const resetForm = () => {
    setFormData({ name: '', species: 'DOG', breed: '', gender: 'UNKNOWN', neutered: false, notes: '', labels: [] });
    setCustomBreed('');
    setEditingPatient(null);
  };

  const openCreateForm = () => {
    resetForm();
    setShowForm(true);
  };

  const openEditForm = (patient: PatientResponse) => {
    setEditingPatient(patient);
    const breedValue = patient.breed || '';
    const breedsForSpecies = getBreedsForSpecies(patient.species);
    const isCustomBreed = breedValue && !breedsForSpecies.includes(breedValue);

    setFormData({
      name: patient.name,
      species: patient.species,
      breed: isCustomBreed ? '__custom__' : breedValue,
      gender: patient.gender || 'UNKNOWN',
      neutered: patient.neutered || false,
      notes: patient.notes || '',
      labels: patient.labels || [],
    });
    setCustomBreed(isCustomBreed ? breedValue : '');
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    resetForm();
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isSubmitting) return;

    setIsSubmitting(true);
    try {
      // Prepare the data, replacing '__custom__' with custom breed value
      const dataToSubmit = {
        ...formData,
        breed: formData.breed === '__custom__' ? customBreed : formData.breed,
      };

      if (editingPatient) {
        await updatePatient(editingPatient.id, dataToSubmit);
        success(t('patients.patientUpdated'));
      } else {
        await createPatient(dataToSubmit);
        success(t('patients.patientCreated'));
      }
      closeForm();
    } catch (err) {
      showError(editingPatient ? t('patients.failedToUpdate') : t('patients.failedToCreate'));
    } finally {
      setIsSubmitting(false);
    }
  };

  const toggleLabel = (label: PatientLabel) => {
    setFormData((prev) => {
      const currentLabels = prev.labels || [];
      if (currentLabels.includes(label)) {
        return { ...prev, labels: currentLabels.filter((l) => l !== label) };
      }
      return { ...prev, labels: [...currentLabels, label] };
    });
  };

  const handleDelete = async (id: string) => {
    if (window.confirm(t('patients.confirmDelete'))) {
      try {
        await deletePatient(id);
        success(t('patients.patientDeleted'));
      } catch (err) {
        showError(t('patients.failedToDelete'));
      }
    }
  };

  if (loading) {
    return <Loading text={t('patients.loading')} />;
  }

  if (error) {
    return (
      <Card style={{ textAlign: 'center', padding: spacing.xl }}>
        <Text variant="muted" style={{ marginBottom: spacing.md }}>{t('errors.failedToLoad')}: {error}</Text>
        <Button variant="primary" onClick={refresh}>
          {t('errors.retry')}
        </Button>
      </Card>
    );
  }

  return (
    <div>
      <PageHeader
        title={t('patients.title')}
        actions={
          <Button variant="primary" onClick={openCreateForm}>
            {t('patients.addPatient')}
          </Button>
        }
      />

      {/* Search */}
      {patients.length > 0 && (
        <SearchFilter
          value={searchQuery}
          onChange={handleSearchChange}
          placeholder={t('patients.searchPlaceholder')}
          resultCount={searchQuery ? filteredPatients.length : undefined}
        />
      )}

      <Modal open={showForm} onClose={closeForm}>
        <ModalTitle>{editingPatient ? t('patients.editPatient') : t('patients.newPatient')}</ModalTitle>
        <form onSubmit={handleSubmit}>
          {/* Basic Info Section */}
          <div style={{
            backgroundColor: colors.primary.light,
            padding: spacing.md,
            borderRadius: borderRadius.md,
            marginBottom: spacing.md
          }}>
            <Text size="sm" style={{ fontWeight: fontWeight.semibold, marginBottom: spacing.sm, color: colors.primary.hover }}>
              {t('patients.basicInfo')}
            </Text>
            <FormField label={t('patients.name')} required>
              <Input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder={t('patients.enterName')}
                required
              />
            </FormField>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
              <FormField label={t('patients.species')} required>
                <Select
                  value={formData.species}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (isSpecies(value)) {
                      setFormData({ ...formData, species: value });
                    }
                  }}
                >
                  {SPECIES_OPTIONS.map((s) => (
                    <option key={s.value} value={s.value}>
                      {s.emoji} {s.label}
                    </option>
                  ))}
                </Select>
              </FormField>
              <FormField label={t('patients.breed')}>
                <Select
                  value={formData.breed || ''}
                  onChange={(e) => setFormData({ ...formData, breed: e.target.value })}
                >
                  <option value="">{t('patients.selectBreed')}</option>
                  {availableBreeds.map((breed) => (
                    <option key={breed} value={breed}>
                      {breed}
                    </option>
                  ))}
                  <option value="__custom__">{t('patients.otherCustom')}</option>
                </Select>
              </FormField>
            </div>
            {formData.breed === '__custom__' && (
              <FormField label={t('patients.customBreed')}>
                <Input
                  type="text"
                  value={customBreed}
                  onChange={(e) => setCustomBreed(e.target.value)}
                  placeholder={t('patients.enterBreedName')}
                />
              </FormField>
            )}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: spacing.md }}>
              <FormField label={t('patients.gender')}>
                <Select
                  value={formData.gender || 'UNKNOWN'}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (isGender(value)) {
                      setFormData({ ...formData, gender: value });
                    }
                  }}
                >
                  <option value="UNKNOWN">{t('common.unknown')}</option>
                  <option value="MALE">{t('common.male')}</option>
                  <option value="FEMALE">{t('common.female')}</option>
                </Select>
              </FormField>
              <FormField label={t('patients.neutered')}>
                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  height: '42px',
                  paddingLeft: spacing.sm
                }}>
                  <input
                    type="checkbox"
                    checked={formData.neutered || false}
                    onChange={(e) => setFormData({ ...formData, neutered: e.target.checked })}
                    style={{
                      width: '18px',
                      height: '18px',
                      cursor: 'pointer',
                      marginRight: spacing.xs
                    }}
                  />
                  <Text size="sm">{t('common.yes')}</Text>
                </div>
              </FormField>
            </div>
          </div>

          {/* Labels Section */}
          <div style={{ marginBottom: spacing.md }}>
            <Text size="sm" style={{ fontWeight: fontWeight.semibold, marginBottom: spacing.sm }}>
              {t('patients.labelsTags')}
            </Text>
            <Text variant="muted" size="sm" style={{ marginBottom: spacing.sm }}>
              {t('patients.clickToSelectLabels')}
            </Text>
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(2, 1fr)',
              gap: spacing.xs,
              backgroundColor: colors.neutral.background,
              padding: spacing.sm,
              borderRadius: borderRadius.md,
            }}>
              {PATIENT_LABELS.map(({ label, display, variant, icon }) => {
                const isSelected = formData.labels?.includes(label);
                return (
                  <div
                    key={label}
                    onClick={() => toggleLabel(label)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: spacing.xs,
                      padding: spacing.sm,
                      borderRadius: borderRadius.sm,
                      cursor: 'pointer',
                      backgroundColor: isSelected ? colors[variant].light : colors.neutral.white,
                      border: `2px solid ${isSelected ? colors[variant].main : colors.neutral.border}`,
                      transition: 'all 0.15s ease',
                    }}
                  >
                    <span>{icon}</span>
                    <Text size="sm" style={{ fontWeight: isSelected ? fontWeight.semibold : fontWeight.normal }}>
                      {display}
                    </Text>
                    {isSelected && (
                      <span style={{ marginLeft: 'auto', color: colors[variant].main }}>✓</span>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Notes Section */}
          <FormField label={t('patients.notes')}>
            <TextArea
              value={formData.notes || ''}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              rows={3}
              placeholder={t('patients.anyNotes')}
            />
          </FormField>

          <ModalActions>
            <Button type="button" variant="ghost" onClick={closeForm} disabled={isSubmitting}>
              {t('common.cancel')}
            </Button>
            <Button type="submit" variant="primary" disabled={isSubmitting}>
              {isSubmitting ? t('common.saving') : editingPatient ? t('patients.saveChanges') : t('patients.createPatient')}
            </Button>
          </ModalActions>
        </form>
      </Modal>

      {patients.length === 0 ? (
        <Card style={{ textAlign: 'center', padding: spacing.xxl }}>
          <div style={{ fontSize: '48px', marginBottom: spacing.md }}>🐾</div>
          <Text style={{ fontSize: fontSize.lg, marginBottom: spacing.sm }}>{t('patients.noPatients')}</Text>
          <Text variant="muted" style={{ marginBottom: spacing.lg }}>
            {t('patients.addFirstPatient')}
          </Text>
          <Button variant="primary" onClick={openCreateForm}>
            {t('patients.addYourFirstPatient')}
          </Button>
        </Card>
      ) : filteredPatients.length === 0 ? (
        <Card style={{ textAlign: 'center', padding: spacing.xl }}>
          <Text variant="muted">{t('patients.noMatch')}</Text>
        </Card>
      ) : (
        <>
          {/* Patients Table */}
          <div style={{
            backgroundColor: colors.neutral.white,
            borderRadius: borderRadius.md,
            overflow: 'hidden',
            border: `1px solid ${colors.neutral.border}`,
          }}>
            <table style={{
              width: '100%',
              borderCollapse: 'collapse',
            }}>
              <thead>
                <tr style={{
                  backgroundColor: colors.neutral.background,
                  borderBottom: `2px solid ${colors.neutral.border}`,
                }}>
                  <th style={{
                    textAlign: 'left',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('patients.name')}</th>
                  <th style={{
                    textAlign: 'left',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('patients.species')}</th>
                  <th style={{
                    textAlign: 'left',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('patients.breed')}</th>
                  <th style={{
                    textAlign: 'left',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('patients.labels')}</th>
                  <th style={{
                    textAlign: 'left',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('patients.weight')}</th>
                  <th style={{
                    textAlign: 'left',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('patients.notes')}</th>
                  <th style={{
                    textAlign: 'left',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('patients.dateAdded')}</th>
                  <th style={{
                    textAlign: 'right',
                    padding: spacing.md,
                    fontSize: fontSize.sm,
                    fontWeight: fontWeight.semibold,
                    color: colors.neutral.textLight,
                  }}>{t('common.actions')}</th>
                </tr>
              </thead>
              <tbody>
                {paginatedPatients.map((patient) => {
                  const speciesInfo = getSpeciesInfo(patient.species);
                  return (
                    <tr
                      key={patient.id}
                      onClick={() => navigate(`/patients/${patient.id}`)}
                      style={{
                        borderBottom: `1px solid ${colors.neutral.border}`,
                        cursor: 'pointer',
                        transition: 'background-color 0.15s ease',
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = colors.primary.light;
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = 'transparent';
                      }}
                    >
                      <td style={{
                        padding: spacing.md,
                        fontSize: fontSize.md,
                        fontWeight: fontWeight.medium,
                        color: colors.secondary.main,
                      }}>
                        {patient.name}
                      </td>
                      <td style={{ padding: spacing.md }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: spacing.xs }}>
                          <span>{speciesInfo.emoji}</span>
                          <Badge variant="secondary" style={{ fontSize: fontSize.xs }}>
                            {speciesInfo.label}
                          </Badge>
                        </div>
                      </td>
                      <td style={{
                        padding: spacing.md,
                        fontSize: fontSize.sm,
                        color: colors.neutral.text,
                      }}>
                        {patient.breed || '-'}
                      </td>
                      <td style={{ padding: spacing.md }}>
                        {patient.labels && patient.labels.length > 0 ? (
                          <div style={{ display: 'flex', flexWrap: 'wrap', gap: spacing.xs }}>
                            {patient.labels.map((label) => {
                              const { display, variant, icon } = getLabelInfo(label);
                              return (
                                <Badge key={label} variant={variant} style={{ fontSize: fontSize.xs }}>
                                  {icon} {display}
                                </Badge>
                              );
                            })}
                          </div>
                        ) : (
                          <Text variant="muted" size="sm">-</Text>
                        )}
                      </td>
                      <td style={{
                        padding: spacing.md,
                        fontSize: fontSize.sm,
                        color: colors.neutral.text,
                      }}>
                        {patient.weight ? `${patient.weight} kg` : '-'}
                      </td>
                      <td style={{
                        padding: spacing.md,
                        fontSize: fontSize.sm,
                        color: colors.neutral.textLight,
                        maxWidth: '200px',
                      }}>
                        {patient.notes ? (
                          <div style={{
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                          }}>
                            {patient.notes}
                          </div>
                        ) : (
                          <Text variant="muted" size="sm">-</Text>
                        )}
                      </td>
                      <td style={{
                        padding: spacing.md,
                        fontSize: fontSize.sm,
                        color: colors.neutral.textLight,
                      }}>
                        {new Date(patient.createdAt).toLocaleDateString()}
                      </td>
                      <td style={{
                        padding: spacing.md,
                        textAlign: 'right',
                      }}>
                        <div style={{ display: 'flex', gap: spacing.xs, justifyContent: 'flex-end' }}>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              openEditForm(patient);
                            }}
                          >
                            {t('common.edit')}
                          </Button>
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleDelete(patient.id);
                            }}
                          >
                            {t('common.delete')}
                          </Button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          <Pagination
            currentPage={currentPage}
            totalItems={filteredPatients.length}
            pageSize={PAGINATION.DEFAULT_PAGE_SIZE}
            onPageChange={setCurrentPage}
            style={{ marginTop: spacing.lg }}
          />
        </>
      )}
    </div>
  );
}
