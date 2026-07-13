import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Landing from './pages/Landing';
import LoginRegister from './pages/LoginRegister';
import FarmerDashboard from './pages/FarmerDashboard';
import TransporterDashboard from './pages/TransporterDashboard';
import AdminDashboard from './pages/AdminDashboard';
import './index.css';

// Header component shared across pages
const NavigationHeader = () => {
  const { user, logout } = useAuth();

  return (
    <header className="app-header">
      <div className="app-container header-content">
        <Link to="/" className="logo">
          🌾 Krishi<span>Bridge</span>
        </Link>
        
        <nav style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          {user ? (
            <>
              <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                Hello, <strong style={{ color: 'var(--text-main)' }}>{user.name}</strong> 
                <span className="badge badge-info" style={{ marginLeft: '8px' }}>{user.role}</span>
              </span>
              <Link to="/dashboard" className="btn btn-secondary btn-sm">
                Dashboard
              </Link>
              <button onClick={logout} className="btn btn-primary btn-sm" style={{ backgroundColor: 'var(--danger)' }}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-secondary btn-sm">
                Login / Sign Up
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
};

// Route Guard to protect auth-only routes
const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: 'var(--bg-main)' }}>
        <div style={{ color: 'var(--primary)', fontFamily: 'var(--font-heading)', fontSize: '1.5rem', fontWeight: 'bold' }}>
          Loading Krishi Bridge... 🌾
        </div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

// Dynamically route to correct dashboard based on role
const RoleDashboardRouter = () => {
  const { user } = useAuth();

  if (user.role === 'FARMER') {
    return <FarmerDashboard />;
  } else if (user.role === 'TRANSPORTER') {
    return <TransporterDashboard />;
  } else if (user.role === 'ADMIN') {
    return <AdminDashboard />;
  }

  return <Navigate to="/" replace />;
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <NavigationHeader />
        <main style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
          <Routes>
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<LoginRegister />} />
            <Route 
              path="/dashboard" 
              element={
                <ProtectedRoute>
                  <RoleDashboardRouter />
                </ProtectedRoute>
              } 
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </Router>
    </AuthProvider>
  );
}

export default App;
