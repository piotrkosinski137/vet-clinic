export const VISIT_TYPE_CONFIG = {
  CONSULTATION: {
    label: 'Consultation',
    icon: '💬',
    color: '#2196F3',  // blue
  },
  VACCINATION: {
    label: 'Vaccination',
    icon: '💉',
    color: '#4CAF50',  // green
  },
  LAB_WORK: {
    label: 'Lab Work',
    icon: '🧪',
    color: '#9C27B0',  // purple
  },
  ULTRASOUND: {
    label: 'Ultrasound',
    icon: '📡',
    color: '#00BCD4',  // cyan
  },
  CARDIOLOGY: {
    label: 'Cardiology',
    icon: '❤️',
    color: '#E91E63',  // pink
  },
  SURGERY: {
    label: 'Surgery',
    icon: '🔪',
    color: '#F44336',  // red
  },
  DENTAL: {
    label: 'Dental',
    icon: '🦷',
    color: '#FFEB3B',  // yellow
  },
  GROOMING: {
    label: 'Grooming',
    icon: '✂️',
    color: '#FF9800',  // orange
  },
  EMERGENCY: {
    label: 'Emergency',
    icon: '🚨',
    color: '#F44336',  // red
  },
  FOLLOW_UP: {
    label: 'Follow-up',
    icon: '🔄',
    color: '#607D8B',  // blue-grey
  },
  CHECKUP: {
    label: 'Checkup',
    icon: '✅',
    color: '#8BC34A',  // light green
  },
} as const;

export type VisitTypeKey = keyof typeof VISIT_TYPE_CONFIG;

export const VISIT_TYPE_OPTIONS = Object.entries(VISIT_TYPE_CONFIG).map(([value, config]) => ({
  value,
  label: config.label,
  icon: config.icon,
}));

export function getVisitTypeInfo(type: string) {
  return VISIT_TYPE_CONFIG[type as VisitTypeKey] || VISIT_TYPE_CONFIG.CONSULTATION;
}
