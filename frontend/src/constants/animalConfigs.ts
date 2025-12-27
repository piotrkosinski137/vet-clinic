import { Species, PatientLabel } from '../api';
import { BadgeVariant } from '../components/ui';

/**
 * Species configuration options for animal types.
 * Used across patient forms and displays.
 */
export const SPECIES_OPTIONS: { value: Species; label: string; emoji: string }[] = [
  { value: 'DOG', label: 'Dog', emoji: '🐕' },
  { value: 'CAT', label: 'Cat', emoji: '🐈' },
  { value: 'BIRD', label: 'Bird', emoji: '🐦' },
  { value: 'RABBIT', label: 'Rabbit', emoji: '🐰' },
  { value: 'HAMSTER', label: 'Hamster', emoji: '🐹' },
  { value: 'FISH', label: 'Fish', emoji: '🐠' },
  { value: 'REPTILE', label: 'Reptile', emoji: '🦎' },
  { value: 'OTHER', label: 'Other', emoji: '🐾' },
];

/**
 * Patient label configurations for quick identification tags.
 * Used for visual badges and special handling indicators.
 */
export const PATIENT_LABELS: {
  label: PatientLabel;
  display: string;
  variant: BadgeVariant;
  icon: string;
}[] = [
  { label: 'AGGRESSIVE', display: 'Aggressive', variant: 'danger', icon: '⚠️' },
  { label: 'ALLERGIC', display: 'Allergic', variant: 'warning', icon: '💊' },
  { label: 'VIP', display: 'VIP', variant: 'primary', icon: '⭐' },
  { label: 'CHRONIC', display: 'Chronic', variant: 'secondary', icon: '🏥' },
  { label: 'SENIOR', display: 'Senior', variant: 'secondary', icon: '👴' },
  { label: 'SPECIAL_DIET', display: 'Special Diet', variant: 'warning', icon: '🥗' },
  { label: 'UNDER_TREATMENT', display: 'Under Treatment', variant: 'primary', icon: '💉' },
  { label: 'FLIGHT_RISK', display: 'Flight Risk', variant: 'danger', icon: '🏃' },
];

/**
 * Get species display info by species value.
 */
export function getSpeciesInfo(species: Species) {
  return (
    SPECIES_OPTIONS.find((s) => s.value === species) || {
      value: species,
      label: species,
      emoji: '🐾',
    }
  );
}

/**
 * Get label display info by label value.
 */
export function getLabelInfo(label: PatientLabel) {
  return (
    PATIENT_LABELS.find((l) => l.label === label) || {
      label,
      display: label,
      variant: 'secondary' as BadgeVariant,
      icon: '🏷️',
    }
  );
}
