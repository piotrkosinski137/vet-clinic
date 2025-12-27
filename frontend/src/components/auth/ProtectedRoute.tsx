import { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../auth';
import { Loading } from '../ui';

interface ProtectedRouteProps {
  children: ReactNode;
}

/**
 * Protects routes from unauthenticated access.
 * Redirects to login page if user is not authenticated.
 * Preserves the intended destination for redirect after login.
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <Loading text="Checking authentication..." />;
  }

  if (!isAuthenticated) {
    // Save the attempted location for redirect after login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
}
