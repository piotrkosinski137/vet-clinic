import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth';
import { PatientsPage, ClientsPage } from './pages';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppContent />
      </BrowserRouter>
    </AuthProvider>
  );
}

function AppContent() {
  const { isAuthenticated, isLoading, username, login, logout } = useAuth();

  if (isLoading) {
    return (
      <div className="app">
        <div className="loading">Loading...</div>
      </div>
    );
  }

  return (
    <div className="app">
      <nav>
        <ul>
          <li>
            <NavLink to="/" end>
              Home
            </NavLink>
          </li>
          {isAuthenticated && (
            <>
              <li>
                <NavLink to="/patients">Patients</NavLink>
              </li>
              <li>
                <NavLink to="/clients">Clients</NavLink>
              </li>
            </>
          )}
          <li style={{ marginLeft: 'auto' }}>
            {isAuthenticated ? (
              <span style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                <span style={{ color: '#ecf0f1' }}>Hi, {username}</span>
                <button
                  onClick={logout}
                  style={{
                    background: '#e74c3c',
                    color: 'white',
                    border: 'none',
                    padding: '8px 16px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                  }}
                >
                  Logout
                </button>
              </span>
            ) : (
              <button
                onClick={login}
                style={{
                  background: '#27ae60',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '4px',
                  cursor: 'pointer',
                }}
              >
                Login
              </button>
            )}
          </li>
        </ul>
      </nav>

      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route
          path="/patients"
          element={
            <ProtectedRoute>
              <PatientsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/clients"
          element={
            <ProtectedRoute>
              <ClientsPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </div>
  );
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, login } = useAuth();

  if (!isAuthenticated) {
    return (
      <div className="card" style={{ textAlign: 'center', marginTop: '50px' }}>
        <h2>Authentication Required</h2>
        <p style={{ margin: '20px 0', color: '#666' }}>
          You need to be logged in to access this page.
        </p>
        <button className="btn btn-primary" onClick={login}>
          Login with Keycloak
        </button>
      </div>
    );
  }

  return <>{children}</>;
}

function HomePage() {
  const { isAuthenticated, login } = useAuth();

  return (
    <div>
      <h1>Vet Clinic Management</h1>
      <p style={{ marginTop: '20px', color: '#666' }}>
        Welcome to the veterinary clinic management system.
      </p>

      {!isAuthenticated ? (
        <div className="card" style={{ marginTop: '30px', textAlign: 'center' }}>
          <h3>Get Started</h3>
          <p style={{ margin: '15px 0', color: '#666' }}>
            Login to manage patients and clients.
          </p>
          <button className="btn btn-primary" onClick={login}>
            Login with Keycloak
          </button>
          <p style={{ marginTop: '15px', fontSize: '14px', color: '#999' }}>
            Test credentials: user / user
          </p>
        </div>
      ) : (
        <div className="grid" style={{ marginTop: '30px' }}>
          <div className="card">
            <h3>Patients</h3>
            <p>Manage animals and their medical records</p>
            <NavLink
              to="/patients"
              className="btn btn-primary"
              style={{ marginTop: '15px', display: 'inline-block' }}
            >
              View Patients
            </NavLink>
          </div>
          <div className="card">
            <h3>Clients</h3>
            <p>Manage pet owners and their contact info</p>
            <NavLink
              to="/clients"
              className="btn btn-primary"
              style={{ marginTop: '15px', display: 'inline-block' }}
            >
              View Clients
            </NavLink>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
