import { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import pl from './locales/pl.json';
import en from './locales/en.json';

export type Language = 'pl' | 'en';

type TranslationKeys = typeof pl;

interface I18nContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string, params?: Record<string, string | number>) => string;
}

const translations: Record<Language, TranslationKeys> = {
  pl,
  en,
};

const I18nContext = createContext<I18nContextType | undefined>(undefined);

const STORAGE_KEY = 'vetclinic-language';

function getStoredLanguage(): Language {
  if (typeof window !== 'undefined') {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'en' || stored === 'pl') {
      return stored;
    }
  }
  return 'pl'; // Default to Polish
}

/**
 * Get a nested value from an object using dot notation
 * e.g., getNestedValue({ a: { b: 'hello' } }, 'a.b') => 'hello'
 */
function getNestedValue(obj: Record<string, unknown>, path: string): string | undefined {
  const keys = path.split('.');
  let current: unknown = obj;

  for (const key of keys) {
    if (current && typeof current === 'object' && key in current) {
      current = (current as Record<string, unknown>)[key];
    } else {
      return undefined;
    }
  }

  return typeof current === 'string' ? current : undefined;
}

interface I18nProviderProps {
  children: ReactNode;
}

export function I18nProvider({ children }: I18nProviderProps) {
  const [language, setLanguageState] = useState<Language>(getStoredLanguage);

  const setLanguage = useCallback((lang: Language) => {
    setLanguageState(lang);
    localStorage.setItem(STORAGE_KEY, lang);
  }, []);

  const t = useCallback(
    (key: string, params?: Record<string, string | number>): string => {
      const translation = getNestedValue(translations[language] as unknown as Record<string, unknown>, key);

      if (!translation) {
        if (import.meta.env.DEV) {
          // eslint-disable-next-line no-console
          console.warn(`Missing translation for key: ${key} in language: ${language}`);
        }
        return key;
      }

      // Replace parameters like {{name}} with actual values
      if (params) {
        return Object.entries(params).reduce((result, [paramKey, paramValue]) => {
          return result.replace(new RegExp(`{{${paramKey}}}`, 'g'), String(paramValue));
        }, translation);
      }

      return translation;
    },
    [language]
  );

  return (
    <I18nContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </I18nContext.Provider>
  );
}

export function useI18n(): I18nContextType {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useI18n must be used within an I18nProvider');
  }
  return context;
}

// Language selector component with flag buttons
export function LanguageSelector() {
  const { language, setLanguage } = useI18n();

  const containerStyle: React.CSSProperties = {
    display: 'flex',
    gap: '8px',
    padding: '4px',
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
    borderRadius: '8px',
  };

  const flagStyle = (isActive: boolean): React.CSSProperties => ({
    padding: '6px 10px',
    borderRadius: '6px',
    border: 'none',
    background: isActive ? 'rgba(255, 255, 255, 0.15)' : 'transparent',
    cursor: 'pointer',
    fontSize: '20px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'all 0.2s',
    opacity: isActive ? 1 : 0.7,
  });

  return (
    <div style={containerStyle}>
      <button
        onClick={() => setLanguage('pl')}
        style={flagStyle(language === 'pl')}
        title="Polski"
      >
        🇵🇱
      </button>
      <button
        onClick={() => setLanguage('en')}
        style={flagStyle(language === 'en')}
        title="English"
      >
        🇬🇧
      </button>
    </div>
  );
}
