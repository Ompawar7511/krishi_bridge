import React from 'react';
import { Link } from 'react-router-dom';

const Landing = () => {
  return (
    <div style={{ padding: '80px 0', minHeight: 'calc(100vh - 80px)', background: 'radial-gradient(circle at top left, rgba(16,185,129,0.05) 0%, transparent 40%)' }}>
      <div className="app-container" style={{ textAlign: 'center' }}>
        
        {/* Hero Section */}
        <h1 style={{ fontSize: '4.5rem', fontWeight: 800, fontFamily: 'var(--font-heading)', background: 'linear-gradient(135deg, var(--text-main) 30%, var(--primary) 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', margin: '0 auto 24px', maxWidth: '800px', lineHeight: 1.1 }}>
          Bridging the Gap Between Farmers and Fleet Transporters
        </h1>
        
        <p style={{ fontSize: '1.25rem', color: 'var(--text-secondary)', maxWidth: '600px', margin: '0 auto 40px', lineHeight: 1.6 }}>
          An automated, transparent agricultural logistics marketplace. Get fair pricing, quick geospatial matches, and verified transporter document compliance.
        </p>

        {/* Portals / CTAs */}
        <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '20px', marginBottom: '80px' }}>
          <Link to="/login?role=FARMER" className="btn btn-primary" style={{ padding: '16px 36px', fontSize: '1.05rem' }}>
            Enter Farmer Portal
          </Link>
          <Link to="/login?role=TRANSPORTER" className="btn btn-secondary" style={{ padding: '16px 36px', fontSize: '1.05rem', borderColor: 'var(--primary)', color: 'var(--primary)' }}>
            Join as Transporter
          </Link>
          <Link to="/login?role=ADMIN" className="btn btn-secondary" style={{ padding: '16px 36px', fontSize: '1.05rem' }}>
            Admin Center
          </Link>
        </div>

        {/* Feature Cards Grid */}
        <h2 style={{ fontSize: '2rem', marginBottom: '40px', fontFamily: 'var(--font-heading)' }}>
          Core Features of Krishi Bridge
        </h2>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '24px', textAlign: 'left' }}>
          
          <div className="card">
            <div style={{ fontSize: '2.5rem', marginBottom: '16px' }}>📍</div>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>Smart Spatial Matching</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Locate nearby vehicles dynamically within a 20km radius that have sufficient capacity and conflict-free schedules.
            </p>
          </div>

          <div className="card">
            <div style={{ fontSize: '2.5rem', marginBottom: '16px' }}>💰</div>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>Fair Dynamic Pricing</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Instant price computation based on agricultural distance, vehicle type, crop type, and admin-defined pricing rules.
            </p>
          </div>

          <div className="card">
            <div style={{ fontSize: '2.5rem', marginBottom: '16px' }}>🛡️</div>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>Document Verification</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Transporters upload compliance documents like licenses and insurance, verified by admins before matching is allowed.
            </p>
          </div>

          <div className="card">
            <div style={{ fontSize: '2.5rem', marginBottom: '16px' }}>⚖️</div>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '10px' }}>Dispute Resolution</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Submit dispute reports with audit logs when transit anomalies occur. Resolved quickly by designated administrators.
            </p>
          </div>

        </div>

      </div>
    </div>
  );
};

export default Landing;
