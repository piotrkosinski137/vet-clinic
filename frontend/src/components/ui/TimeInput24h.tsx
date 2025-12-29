import { useState, useCallback, useRef, useEffect } from 'react';
import { colors, spacing, borderRadius, fontSize } from '../../theme';

export interface TimeInput24hProps {
  value: string; // Format: "HH:mm"
  onChange: (value: string) => void;
  disabled?: boolean;
  style?: React.CSSProperties;
}

const MINUTE_OPTIONS = ['00', '15', '30', '45'];

/**
 * Custom 24-hour time input component.
 * Always displays time in 24h format regardless of browser locale.
 * Minutes are restricted to 15-minute intervals (00, 15, 30, 45).
 */
export function TimeInput24h({ value, onChange, disabled = false, style }: TimeInput24hProps) {
  const [hours, minutes] = (value || '08:00').split(':').map(s => s || '00');
  const [localHours, setLocalHours] = useState(hours.padStart(2, '0'));
  const hoursRef = useRef<HTMLInputElement>(null);

  // Round minutes to nearest 15-minute interval
  const roundedMinutes = MINUTE_OPTIONS.reduce((prev, curr) => {
    const prevDiff = Math.abs(parseInt(prev, 10) - parseInt(minutes, 10));
    const currDiff = Math.abs(parseInt(curr, 10) - parseInt(minutes, 10));
    return currDiff < prevDiff ? curr : prev;
  }, '00');

  const [localMinutes, setLocalMinutes] = useState(roundedMinutes);

  // Sync local state with prop value
  useEffect(() => {
    const [h, m] = (value || '08:00').split(':');
    setLocalHours((h || '08').padStart(2, '0'));
    // Round to nearest 15 min
    const mNum = parseInt(m || '0', 10);
    const rounded = MINUTE_OPTIONS.reduce((prev, curr) => {
      const prevDiff = Math.abs(parseInt(prev, 10) - mNum);
      const currDiff = Math.abs(parseInt(curr, 10) - mNum);
      return currDiff < prevDiff ? curr : prev;
    }, '00');
    setLocalMinutes(rounded);
  }, [value]);

  const handleHoursChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value.replace(/\D/g, '').slice(0, 2);
    setLocalHours(val);
  }, []);

  const handleHoursBlur = useCallback(() => {
    let num = parseInt(localHours, 10);
    if (isNaN(num) || num < 0) num = 0;
    if (num > 23) num = 23;
    const finalH = num.toString().padStart(2, '0');
    setLocalHours(finalH);
    onChange(`${finalH}:${localMinutes}`);
  }, [localHours, localMinutes, onChange]);

  const handleMinutesChange = useCallback((e: React.ChangeEvent<HTMLSelectElement>) => {
    const val = e.target.value;
    setLocalMinutes(val);
    // Commit immediately on select change
    const h = localHours.padStart(2, '0');
    let hNum = parseInt(h, 10);
    if (isNaN(hNum) || hNum < 0) hNum = 0;
    if (hNum > 23) hNum = 23;
    const finalH = hNum.toString().padStart(2, '0');
    setLocalHours(finalH);
    onChange(`${finalH}:${val}`);
  }, [localHours, onChange]);

  const handleHoursKeyDown = useCallback((e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
      e.preventDefault();
      const delta = e.key === 'ArrowUp' ? 1 : -1;
      let num = parseInt(localHours || '0', 10);
      num = (num + delta + 24) % 24;
      const newVal = num.toString().padStart(2, '0');
      setLocalHours(newVal);
      onChange(`${newVal}:${localMinutes}`);
    }
  }, [localHours, localMinutes, onChange]);

  const containerStyle: React.CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '2px',
    padding: `${spacing.xs} ${spacing.sm}`,
    border: `1px solid ${colors.neutral.border}`,
    borderRadius: borderRadius.md,
    fontSize: fontSize.sm,
    backgroundColor: disabled ? colors.neutral.background : colors.neutral.white,
    opacity: disabled ? 0.5 : 1,
    cursor: disabled ? 'not-allowed' : 'text',
    ...style,
  };

  const hoursInputStyle: React.CSSProperties = {
    width: '28px',
    border: 'none',
    outline: 'none',
    textAlign: 'center',
    fontSize: fontSize.sm,
    fontFamily: 'inherit',
    backgroundColor: 'transparent',
    padding: 0,
  };

  const selectStyle: React.CSSProperties = {
    border: 'none',
    outline: 'none',
    fontSize: fontSize.sm,
    fontFamily: 'inherit',
    backgroundColor: 'transparent',
    padding: 0,
    cursor: disabled ? 'not-allowed' : 'pointer',
    appearance: 'none',
    WebkitAppearance: 'none',
    MozAppearance: 'none',
    width: '28px',
    textAlign: 'center',
  };

  return (
    <div style={containerStyle} onClick={() => !disabled && hoursRef.current?.focus()}>
      <input
        ref={hoursRef}
        type="text"
        inputMode="numeric"
        value={localHours}
        onChange={handleHoursChange}
        onBlur={handleHoursBlur}
        onKeyDown={handleHoursKeyDown}
        disabled={disabled}
        style={hoursInputStyle}
        maxLength={2}
        placeholder="08"
      />
      <span style={{ color: colors.neutral.textLight }}>:</span>
      <select
        value={localMinutes}
        onChange={handleMinutesChange}
        onClick={(e) => e.stopPropagation()}
        disabled={disabled}
        style={selectStyle}
      >
        {MINUTE_OPTIONS.map(m => (
          <option key={m} value={m}>{m}</option>
        ))}
      </select>
    </div>
  );
}
