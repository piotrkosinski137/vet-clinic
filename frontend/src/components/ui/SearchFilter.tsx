import React from 'react';
import { Input } from './Input';
import { Text } from './Text';
import { spacing } from '../../theme';

export interface SearchFilterProps {
  /** Current search value */
  value: string;
  /** Callback when search value changes */
  onChange: (value: string) => void;
  /** Placeholder text */
  placeholder?: string;
  /** Number of results found (optional, shows count if provided) */
  resultCount?: number;
  /** Maximum width of the search input */
  maxWidth?: string;
  /** Additional container styles */
  style?: React.CSSProperties;
  /** Whether search is currently loading */
  loading?: boolean;
  /** Text to show when loading */
  loadingText?: string;
}

/**
 * Reusable search filter component.
 * Provides consistent search input styling across the application.
 */
export function SearchFilter({
  value,
  onChange,
  placeholder = 'Search...',
  resultCount,
  maxWidth = '400px',
  style,
  loading,
  loadingText = 'Searching...',
}: SearchFilterProps) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.value);
  };

  return (
    <div style={{ marginBottom: spacing.md, ...style }}>
      <Input
        type="text"
        value={value}
        onChange={handleChange}
        placeholder={placeholder}
        style={{ maxWidth }}
      />
      {loading ? (
        <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
          {loadingText}
        </Text>
      ) : (
        value && resultCount !== undefined && (
          <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
            Found {resultCount} result{resultCount !== 1 ? 's' : ''}
          </Text>
        )
      )}
    </div>
  );
}
