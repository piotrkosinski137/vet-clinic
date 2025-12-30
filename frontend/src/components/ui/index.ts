/**
 * UI Components - Abstraction layer for UI elements.
 *
 * IMPORTANT: Always import UI components from this file, not directly from the library.
 * This allows switching UI libraries or customizing components in one place.
 *
 * Example:
 *   import { Button, Input, Card } from '@/components/ui';
 *
 * To switch to a different UI library (e.g., from custom to Material UI):
 * 1. Update the component implementations in this folder
 * 2. Keep the same exported interface
 * 3. All consuming code continues to work without changes
 */

export { Button, type ButtonProps, type ButtonVariant, type ButtonSize } from './Button';
export { Input, type InputProps } from './Input';
export { Select, type SelectProps } from './Select';
export { TextArea, type TextAreaProps } from './TextArea';
export {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  CardActions,
  StatCard,
  type CardProps,
  type CardHeaderProps,
  type CardTitleProps,
  type CardContentProps,
  type CardActionsProps,
  type StatCardProps,
} from './Card';
export {
  Modal,
  ModalHeader,
  ModalTitle,
  ModalContent,
  ModalActions,
  type ModalProps,
  type ModalHeaderProps,
  type ModalTitleProps,
  type ModalContentProps,
  type ModalActionsProps,
} from './Modal';
export { Badge, type BadgeProps, type BadgeVariant } from './Badge';
export { FormField, type FormFieldProps } from './FormField';
export { Text, type TextProps, type TextVariant, type TextSize } from './Text';
export { Spinner, Loading, type SpinnerProps, type SpinnerSize, type LoadingProps } from './Spinner';
export { ModalLoader } from './ModalLoader';
export { PageHeader, type PageHeaderProps } from './PageHeader';
export { Grid, type GridProps } from './Grid';
export { ToastProvider, useToast, type Toast, type ToastVariant } from './Toast';
export { Pagination, type PaginationProps } from './Pagination';
export { SearchFilter, type SearchFilterProps } from './SearchFilter';
export { InfoCard, type InfoCardProps } from './InfoCard';
export { EmptyState, type EmptyStateProps } from './EmptyState';
export { Tabs, TabList, Tab, TabPanel } from './Tabs';
export { ConfirmDialog, type ConfirmDialogProps } from './ConfirmDialog';
export { TimeInput24h, type TimeInput24hProps } from './TimeInput24h';
