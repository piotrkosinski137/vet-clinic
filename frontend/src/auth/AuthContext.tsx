import {
  createContext,
  useContext,
  useState,
  useEffect,
  useRef,
  ReactNode,
  useCallback,
} from 'react';
import { config } from '../config/env';

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  token: string | null;
  username: string | null;
  login: () => void;
  loginWithCredentials: (username: string, password: string) => Promise<void>;
  logout: () => void;
  loginError: string | null;
  clearLoginError: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

// Keycloak configuration
const { url: KEYCLOAK_URL, realm: KEYCLOAK_REALM, clientId: KEYCLOAK_CLIENT_ID } = config.keycloak;

const TOKEN_STORAGE_KEY = 'vetclinic_auth_token';
const REFRESH_TOKEN_STORAGE_KEY = 'vetclinic_refresh_token';

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
}

function parseJwt(token: string): { preferred_username?: string; exp?: number } {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return {};
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  // Initialize from localStorage synchronously
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!localStorage.getItem(TOKEN_STORAGE_KEY));
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY));
  const [username, setUsername] = useState<string | null>(() => {
    const t = localStorage.getItem(TOKEN_STORAGE_KEY);
    return t ? (parseJwt(t).preferred_username || null) : null;
  });
  const [loginError, setLoginError] = useState<string | null>(null);
  const loginInProgress = useRef(false);

  // Sync across tabs
  useEffect(() => {
    const handler = (e: StorageEvent) => {
      if (e.key === TOKEN_STORAGE_KEY) {
        if (e.newValue === null) {
          setToken(null);
          setUsername(null);
          setIsAuthenticated(false);
        } else {
          setToken(e.newValue);
          setUsername(parseJwt(e.newValue).preferred_username || null);
          setIsAuthenticated(true);
        }
      }
    };
    window.addEventListener('storage', handler);
    return () => window.removeEventListener('storage', handler);
  }, []);

  const loginWithCredentials = useCallback(async (user: string, pass: string) => {
    if (loginInProgress.current) return;
    loginInProgress.current = true;
    setLoginError(null);

    try {
      const res = await fetch(
        `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams({
            grant_type: 'password',
            client_id: KEYCLOAK_CLIENT_ID,
            username: user,
            password: pass,
            scope: 'openid profile email',
          }),
        }
      );

      if (res.ok) {
        const data: TokenResponse = await res.json();
        localStorage.setItem(TOKEN_STORAGE_KEY, data.access_token);
        localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, data.refresh_token);
        const parsed = parseJwt(data.access_token);
        setToken(data.access_token);
        setUsername(parsed.preferred_username || null);
        setIsAuthenticated(true);
      } else {
        const err = await res.json().catch(() => ({}));
        const msg = err.error === 'invalid_grant'
          ? 'Invalid username or password'
          : (err.error_description || 'Login failed');
        setLoginError(msg);
        throw new Error(msg);
      }
    } finally {
      loginInProgress.current = false;
    }
  }, []);

  const login = useCallback(() => {
    const url = new URL(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/auth`);
    url.searchParams.set('client_id', KEYCLOAK_CLIENT_ID);
    url.searchParams.set('redirect_uri', window.location.origin + '/login');
    url.searchParams.set('response_type', 'code');
    url.searchParams.set('scope', 'openid profile email');
    window.location.href = url.toString();
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
    setToken(null);
    setUsername(null);
    setIsAuthenticated(false);
    window.location.href = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/logout?post_logout_redirect_uri=${encodeURIComponent(window.location.origin)}&client_id=${KEYCLOAK_CLIENT_ID}`;
  }, []);

  const clearLoginError = useCallback(() => setLoginError(null), []);

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading: false,
        token,
        username,
        login,
        loginWithCredentials,
        logout,
        loginError,
        clearLoginError,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
