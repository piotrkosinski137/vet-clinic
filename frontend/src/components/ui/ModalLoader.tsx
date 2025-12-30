import { Spinner } from './Spinner';
import { Text } from './Text';
import { spacing } from '../../theme';

interface ModalLoaderProps {
  /** Optional loading text to display below spinner */
  text?: string;
  /** Minimum height to prevent layout shift when content loads */
  minHeight?: string;
}

/**
 * Centered loader for modal content areas.
 * Use this when a modal needs to fetch data before displaying content.
 * The modal should open immediately and show this loader until data is ready.
 */
export function ModalLoader({ text, minHeight = '200px' }: ModalLoaderProps) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight,
        padding: spacing.xl,
        gap: spacing.md,
      }}
    >
      <Spinner size="lg" />
      {text && <Text variant="muted">{text}</Text>}
    </div>
  );
}
