import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginRegister = () => {
  const { user, login, register, showToast } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Parse default role from query string if available
  const queryParams = new URLSearchParams(location.search);
  const defaultRole = queryParams.get('role') || 'FARMER';

  const [isLoginMode, setIsLoginMode] = useState(true);
  const [formData, setFormData] = useState({
    name: '',
    phoneNumber: '',
    password: '',
    role: defaultRole
  });
  
  const [loading, setLoading] = useState(false);

  // If already logged in, skip auth screen
  useEffect(() => {
    if (user) {
      navigate('/dashboard');
    }
  }, [user, navigate]);

  // Sync role selector with query param changes
  useEffect(() => {
    const roleParam = queryParams.get('role');
    if (roleParam && ['FARMER', 'TRANSPORTER', 'ADMIN'].includes(roleParam)) {
      setFormData((prev) => ({ ...prev, role: roleParam }));
    }
  }, [location.search]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const validateForm = () => {
    if (!isLoginMode && formData.name.trim().length < 2) {
      showToast('Name must be at least 2 characters.', 'error');
      return false;
    }
    const phonePattern = /^\+?[1-9]\d{1,14}$/;
    if (!phonePattern.test(formData.phoneNumber)) {
      showToast('Please enter a valid phone number format (e.g. +919876543210 or 9876543210).', 'error');
      return false;
    }
    if (formData.password.length < 8) {
      showToast('Password must be at least 8 characters.', 'error');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);

    if (isLoginMode) {
      const res = await login(formData.phoneNumber, formData.password);
      setLoading(false);
      if (res.success) {
        navigate('/dashboard');
      }
    } else {
      const res = await register(
        formData.name,
        formData.phoneNumber,
        formData.password,
        formData.role
      );
      setLoading(false);
      if (res.success) {
        setIsLoginMode(true); // Switch to login mode on success
      }
    }
  };

  return (
    <div style={{ display: 'flex', flex: 1, alignItems: 'center', justifyContent: 'center', padding: '40px 20px', background: 'radial-gradient(circle at bottom right, rgba(99,102,241,0.05) 0%, transparent 40%)' }}>
      <div className="card" style={{ width: '100%', maxWidth: '450px', padding: '36px', boxSizing: 'border-box' }}>
        
        {/* Toggle Mode */}
        <div style={{ display: 'flex', borderBottom: '1px solid var(--border-color)', marginBottom: '28px', paddingBottom: '12px', justifyContent: 'space-around' }}>
          <button
            onClick={() => setIsLoginMode(true)}
            style={{
              background: 'none',
              border: 'none',
              color: isLoginMode ? 'var(--primary)' : 'var(--text-secondary)',
              fontSize: '1.25rem',
              fontWeight: 700,
              fontFamily: 'var(--font-heading)',
              cursor: 'pointer',
              position: 'relative',
              paddingBottom: '8px'
            }}
          >
            Sign In
            {isLoginMode && <div style={{ position: 'absolute', bottom: -13, left: 0, right: 0, height: 3, backgroundColor: 'var(--primary)', borderRadius: '2px' }} />}
          </button>
          
          <button
            onClick={() => setIsLoginMode(false)}
            style={{
              background: 'none',
              border: 'none',
              color: !isLoginMode ? 'var(--primary)' : 'var(--text-secondary)',
              fontSize: '1.25rem',
              fontWeight: 700,
              fontFamily: 'var(--font-heading)',
              cursor: 'pointer',
              position: 'relative',
              paddingBottom: '8px'
            }}
          >
            Create Account
            {!isLoginMode && <div style={{ position: 'absolute', bottom: -13, left: 0, right: 0, height: 3, backgroundColor: 'var(--primary)', borderRadius: '2px' }} />}
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          {/* Name Field (Register Only) */}
          {!isLoginMode && (
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder="Enter your name"
                className="form-input"
                required
              />
            </div>
          )}

          {/* Phone Number */}
          <div className="form-group">
            <label className="form-label">Phone Number</label>
            <input
              type="tel"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              placeholder="e.g. +919876543210"
              className="form-input"
              required
            />
          </div>

          {/* Password */}
          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Min. 8 characters"
              className="form-input"
              required
            />
          </div>

          {/* Role selector (Register Only) */}
          {!isLoginMode && (
            <div className="form-group">
              <label className="form-label">Your Role</label>
              <select
                name="role"
                value={formData.role}
                onChange={handleChange}
                className="form-select"
              >
                <option value="FARMER">Farmer (Looking for transport)</option>
                <option value="TRANSPORTER">Transporter (Offering transit fleet)</option>
                <option value="ADMIN">Marketplace Admin</option>
              </select>
            </div>
          )}

          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
            style={{ width: '100%', marginTop: '16px', padding: '14px' }}
          >
            {loading ? 'Processing...' : isLoginMode ? 'Sign In' : 'Register Account'}
          </button>
        </form>

        <p style={{ marginTop: '24px', fontSize: '0.875rem', color: 'var(--text-secondary)', textAlign: 'center' }}>
          {isLoginMode ? (
            <>
              New to Krishi Bridge?{' '}
              <span
                onClick={() => setIsLoginMode(false)}
                style={{ color: 'var(--primary)', cursor: 'pointer', fontWeight: 600 }}
              >
                Sign up instead
              </span>
            </>
          ) : (
            <>
              Already have an account?{' '}
              <span
                onClick={() => setIsLoginMode(true)}
                style={{ color: 'var(--primary)', cursor: 'pointer', fontWeight: 600 }}
              >
                Log in instead
              </span>
            </>
          )}
        </p>

      </div>
    </div>
  );
};

export default LoginRegister;
