import { useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Custom event dispatched when a 401 Unauthorized response is received.
 * This allows the app to handle session expiration globally.
 */
export const AUTH_ERROR_EVENT = 'vetclinic:auth:error';

/**
 * Dispatches an auth error event to trigger global 401 handling.
 * Called from the API client when a 401 response is received.
 */
export function dispatchAuthError(): void {
  window.dispatchEvent(new CustomEvent(AUTH_ERROR_EVENT));
}

/**
 * Hook that listens for auth error events and redirects to login page.
 * Should be used in the main App component to handle 401 errors globally.
 *
 * When the API client receives a 401 response, it dispatches an auth error
 * event. This hook catches that event and redirects the user to the login page.
 *
 * Note: The API client already clears tokens from localStorage. The auth context
 * will pick up the change on next render since it reads from localStorage.
 */
export function useAuthErrorHandler(): void {
  const navigate = useNavigate();

  const handleAuthError = useCallback(() => {
    // Navigate to login - auth context will handle state cleanup
    navigate('/login', { replace: true });
  }, [navigate]);

  useEffect(() => {
    window.addEventListener(AUTH_ERROR_EVENT, handleAuthError);
    return () => window.removeEventListener(AUTH_ERROR_EVENT, handleAuthError);
  }, [handleAuthError]);
}
