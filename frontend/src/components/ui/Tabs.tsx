import { useState, createContext, useContext, ReactNode } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';

interface TabsContextValue {
  activeTab: string;
  setActiveTab: (tab: string) => void;
}

const TabsContext = createContext<TabsContextValue | null>(null);

interface TabsProps {
  defaultTab: string;
  children: ReactNode;
  onChange?: (tab: string) => void;
}

export function Tabs({ defaultTab, children, onChange }: TabsProps) {
  const [activeTab, setActiveTab] = useState(defaultTab);

  const handleTabChange = (tab: string) => {
    setActiveTab(tab);
    onChange?.(tab);
  };

  return (
    <TabsContext.Provider value={{ activeTab, setActiveTab: handleTabChange }}>
      <div>{children}</div>
    </TabsContext.Provider>
  );
}

interface TabListProps {
  children: ReactNode;
}

export function TabList({ children }: TabListProps) {
  return (
    <div style={{
      display: 'flex',
      gap: spacing.xs,
      borderBottom: `2px solid ${colors.neutral.border}`,
      marginBottom: spacing.md,
      overflowX: 'auto',
    }}>
      {children}
    </div>
  );
}

interface TabProps {
  value: string;
  children: ReactNode;
  icon?: string;
}

export function Tab({ value, children, icon }: TabProps) {
  const context = useContext(TabsContext);
  if (!context) throw new Error('Tab must be used within Tabs');

  const isActive = context.activeTab === value;

  return (
    <button
      onClick={() => context.setActiveTab(value)}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: spacing.xs,
        padding: `${spacing.sm} ${spacing.md}`,
        border: 'none',
        background: 'none',
        cursor: 'pointer',
        fontSize: fontSize.sm,
        fontWeight: isActive ? fontWeight.semibold : fontWeight.normal,
        color: isActive ? colors.primary.main : colors.neutral.textLight,
        borderBottom: `2px solid ${isActive ? colors.primary.main : 'transparent'}`,
        marginBottom: '-2px',
        transition: 'all 0.2s ease',
        whiteSpace: 'nowrap',
      }}
    >
      {icon && <span>{icon}</span>}
      {children}
    </button>
  );
}

interface TabPanelProps {
  value: string;
  children: ReactNode;
}

export function TabPanel({ value, children }: TabPanelProps) {
  const context = useContext(TabsContext);
  if (!context) throw new Error('TabPanel must be used within Tabs');

  if (context.activeTab !== value) return null;

  return <div>{children}</div>;
}
