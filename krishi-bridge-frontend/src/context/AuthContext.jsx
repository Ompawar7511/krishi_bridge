import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [toasts, setToasts] = useState([]);

  // Toast utilities
  const showToast = (message, type = 'success') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  };

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  // Session Restore / Token Refresh on mount
  useEffect(() => {
    const restoreSession = async () => {
      try {
        const response = await fetch('/api/v1/auth/refresh', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' }
        });
        if (response.ok) {
          const json = await response.json();
          if (json.success && json.data) {
            setUser(json.data);
          }
        }
      } catch (err) {
        console.error('Failed to restore session:', err);
      } finally {
        setLoading(false);
      }
    };
    restoreSession();
  }, []);

  const login = async (phoneNumber, password) => {
    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phoneNumber, password })
      });
      const json = await response.json();
      if (response.ok && json.success) {
        setUser(json.data);
        showToast('Logged in successfully!', 'success');
        return { success: true };
      } else {
        const errMsg = json.message || 'Login failed. Please check credentials.';
        showToast(errMsg, 'error');
        return { success: false, message: errMsg };
      }
    } catch (err) {
      showToast('Server error. Failed to login.', 'error');
      return { success: false, message: 'Server communication error' };
    }
  };

  const register = async (name, phoneNumber, password, role) => {
    try {
      const response = await fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, phoneNumber, password, role })
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Registration successful! You can now log in.', 'success');
        return { success: true };
      } else {
        const errMsg = json.message || 'Registration failed.';
        showToast(errMsg, 'error');
        return { success: false, message: errMsg };
      }
    } catch (err) {
      showToast('Server error. Failed to register.', 'error');
      return { success: false, message: 'Server communication error' };
    }
  };

  const logout = async () => {
    try {
      await fetch('/api/v1/auth/logout', { method: 'POST' });
    } catch (err) {
      console.error('Logout request failed:', err);
    } finally {
      setUser(null);
      showToast('Logged out successfully.', 'success');
    }
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, showToast, toasts, removeToast }}>
      {children}
      {/* Toast Render Overlay */}
      <div className="toast-container">
        {toasts.map((toast) => (
          <div key={toast.id} className={`toast toast-${toast.type}`} onClick={() => removeToast(toast.id)}>
            <span>{toast.type === 'success' ? '🌾' : '⚠️'}</span>
            <div>{toast.message}</div>
          </div>
        ))}
      </div>
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
