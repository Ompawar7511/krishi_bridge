import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const AdminDashboard = () => {
  const { user, showToast } = useAuth();
  
  // Dashboard Sub-Tabs
  const [activeTab, setActiveTab] = useState('analytics'); // 'analytics', 'compliance', 'disputes', 'pricing'

  // Stats State
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);

  // Compliance State
  const [pendingDocs, setPendingDocs] = useState([]);
  const [docsLoading, setDocsLoading] = useState(false);

  // Disputes State
  const [pendingDisputes, setPendingDisputes] = useState([]);
  const [disputesLoading, setDisputesLoading] = useState(false);
  const [selectedDispute, setSelectedDispute] = useState(null);
  const [resolveForm, setResolveForm] = useState({
    status: 'RESOLVED',
    adminNotes: ''
  });

  // Pricing State
  const [pricingForm, setPricingForm] = useState({
    vehicleType: 'TRUCK',
    baseRatePerKm: 15.0,
    pricePerKg: 0.05,
    minimumCharge: 500.0
  });
  const [pricingSubmitting, setPricingSubmitting] = useState(false);

  // Fetch Analytics Stats
  const fetchStats = async () => {
    setStatsLoading(true);
    try {
      const response = await fetch('/api/v1/admin/analytics/dashboard');
      const json = await response.json();
      if (response.ok && json.success) {
        setStats(json.data);
      } else {
        showToast(json.message || 'Failed to load dashboard statistics', 'error');
      }
    } catch (err) {
      showToast('Error loading stats', 'error');
    } finally {
      setStatsLoading(false);
    }
  };

  // Fetch Pending Compliance Documents
  const fetchPendingDocs = async () => {
    setDocsLoading(true);
    try {
      const response = await fetch('/api/v1/admin/documents/pending');
      const json = await response.json();
      if (response.ok && json.success) {
        setPendingDocs(json.data);
      } else {
        showToast(json.message || 'Failed to load compliance queue', 'error');
      }
    } catch (err) {
      showToast('Error loading compliance documents', 'error');
    } finally {
      setDocsLoading(false);
    }
  };

  // Fetch Pending Disputes
  const fetchPendingDisputes = async () => {
    setDisputesLoading(true);
    try {
      const response = await fetch('/api/v1/admin/disputes/pending');
      const json = await response.json();
      if (response.ok && json.success) {
        setPendingDisputes(json.data);
      } else {
        showToast(json.message || 'Failed to load dispute queue', 'error');
      }
    } catch (err) {
      showToast('Error loading disputes', 'error');
    } finally {
      setDisputesLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'analytics') {
      fetchStats();
    } else if (activeTab === 'compliance') {
      fetchPendingDocs();
    } else if (activeTab === 'disputes') {
      fetchPendingDisputes();
    }
  }, [activeTab]);

  // Document actions
  const handleApproveDoc = async (docId) => {
    try {
      const response = await fetch(`/api/v1/admin/documents/approve/${docId}`, {
        method: 'PUT'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Document approved successfully.', 'success');
        fetchPendingDocs();
      } else {
        showToast(json.message || 'Approval failed.', 'error');
      }
    } catch (err) {
      showToast('Error approving document', 'error');
    }
  };

  const handleRejectDoc = async (docId) => {
    try {
      const response = await fetch(`/api/v1/admin/documents/reject/${docId}`, {
        method: 'PUT'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Document rejected.', 'info');
        fetchPendingDocs();
      } else {
        showToast(json.message || 'Rejection failed.', 'error');
      }
    } catch (err) {
      showToast('Error rejecting document', 'error');
    }
  };

  // Dispute actions
  const handleOpenResolveDispute = (dispute) => {
    setSelectedDispute(dispute);
    setResolveForm({ status: 'RESOLVED', adminNotes: '' });
  };

  const handleResolveDisputeSubmit = async (e) => {
    e.preventDefault();
    if (!resolveForm.adminNotes.trim()) {
      showToast('Resolution notes are required.', 'error');
      return;
    }

    try {
      const response = await fetch(`/api/v1/admin/disputes/resolve/${selectedDispute.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          status: resolveForm.status,
          adminNotes: resolveForm.adminNotes
        })
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Dispute resolved successfully!', 'success');
        setSelectedDispute(null);
        fetchPendingDisputes();
      } else {
        showToast(json.message || 'Failed to resolve dispute', 'error');
      }
    } catch (err) {
      showToast('Error resolving dispute', 'error');
    }
  };

  // Pricing actions
  const handlePricingSubmit = async (e) => {
    e.preventDefault();
    setPricingSubmitting(true);
    try {
      const response = await fetch('/api/v1/admin/pricing/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          vehicleType: pricingForm.vehicleType,
          baseRatePerKm: pricingForm.baseRatePerKm,
          pricePerKg: pricingForm.pricePerKg,
          minimumCharge: pricingForm.minimumCharge
        })
      });
      const json = await response.json();
      setPricingSubmitting(false);
      if (response.ok && json.success) {
        showToast(`Pricing rule created/updated for ${pricingForm.vehicleType}!`, 'success');
      } else {
        showToast(json.message || 'Failed to update pricing rule', 'error');
      }
    } catch (err) {
      setPricingSubmitting(false);
      showToast('Error saving pricing rule', 'error');
    }
  };

  return (
    <div className="app-container" style={{ padding: '40px 0' }}>
      
      {/* Welcome & Navigation tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h1 style={{ fontSize: '2.5rem', fontFamily: 'var(--font-heading)' }}>Admin Console</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Logistics marketplace governance, pricing rules, and compliance control.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button 
            className={`btn ${activeTab === 'analytics' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('analytics')}
          >
            Analytics
          </button>
          <button 
            className={`btn ${activeTab === 'compliance' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('compliance')}
          >
            Verify Docs ({pendingDocs.length})
          </button>
          <button 
            className={`btn ${activeTab === 'disputes' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('disputes')}
          >
            Disputes ({pendingDisputes.length})
          </button>
          <button 
            className={`btn ${activeTab === 'pricing' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('pricing')}
          >
            Pricing Engine
          </button>
        </div>
      </div>

      {/* ANALYTICS SUB-TAB */}
      {activeTab === 'analytics' && (
        <div>
          {statsLoading ? (
            <div className="card" style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading analytics dashboard...</div>
          ) : stats ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
              {/* Stat Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px' }}>
                
                <div className="card" style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Total Requests</div>
                  <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--primary)', marginTop: '8px' }}>{stats.totalBookingsCreated}</div>
                </div>

                <div className="card" style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Deliveries Completed</div>
                  <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--secondary)', marginTop: '8px' }}>{stats.totalDeliveriesCompleted}</div>
                </div>

                <div className="card" style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Cancelled Bookings</div>
                  <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--danger)', marginTop: '8px' }}>{stats.totalBookingsCancelled}</div>
                </div>

                <div className="card" style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Verified Fleets</div>
                  <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--accent)', marginTop: '8px' }}>{stats.totalTransportersApproved}</div>
                </div>

                <div className="card" style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Active Users</div>
                  <div style={{ fontSize: '2.5rem', fontWeight: 800, color: 'var(--text-main)', marginTop: '8px' }}>{stats.activeUsersCount}</div>
                </div>

              </div>

              {/* Detailed stats block */}
              <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '32px' }}>
                <div className="card">
                  <h3 style={{ fontSize: '1.25rem', marginBottom: '16px', fontFamily: 'var(--font-heading)' }}>Platform Operational Health</h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginTop: '20px' }}>
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                        <span>Booking Cancellation Rate</span>
                        <strong>{(stats.cancellationRate * 100).toFixed(1)}%</strong>
                      </div>
                      <div style={{ height: '8px', backgroundColor: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{ width: `${Math.min(stats.cancellationRate * 100, 100)}%`, height: '100%', backgroundColor: 'var(--danger)' }}></div>
                      </div>
                    </div>

                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                        <span>Delivery Fulfillment Rate</span>
                        <strong>{(100 - stats.cancellationRate * 100).toFixed(1)}%</strong>
                      </div>
                      <div style={{ height: '8px', backgroundColor: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{ width: `${Math.max(100 - stats.cancellationRate * 100, 0)}%`, height: '100%', backgroundColor: 'var(--primary)' }}></div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', textAlign: 'center' }}>
                  <div style={{ fontSize: '3rem' }}>🛡️</div>
                  <h3 style={{ fontSize: '1.2rem', marginTop: '12px', marginBottom: '8px' }}>Audit Logging</h3>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
                    All core booking requests, spatial match operations, pricing calculations, and dispute actions register secure hashes in the database audit log.
                  </p>
                </div>
              </div>
            </div>
          ) : (
            <div className="card" style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>No statistics logs generated. Try creating some bookings.</div>
          )}
        </div>
      )}

      {/* COMPLIANCE REVIEW SUB-TAB */}
      {activeTab === 'compliance' && (
        <div className="card">
          <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Pending Transporter Document Verification</h2>
          
          {docsLoading ? (
            <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading compliance queue...</div>
          ) : pendingDocs.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>No transporter documents are pending review.</div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                    <th style={{ padding: '16px' }}>Transporter ID</th>
                    <th style={{ padding: '16px' }}>Document Category</th>
                    <th style={{ padding: '16px' }}>Uploaded Timestamp</th>
                    <th style={{ padding: '16px' }}>Action Links</th>
                    <th style={{ padding: '16px', textAlign: 'right' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingDocs.map(doc => (
                    <tr key={doc.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.95rem' }}>
                      <td style={{ padding: '16px' }}><strong>User #{doc.transporterId}</strong></td>
                      <td style={{ padding: '16px' }}><span className="badge badge-info">{doc.documentType.replace('_', ' ')}</span></td>
                      <td style={{ padding: '16px' }}>{new Date(doc.uploadedAt).toLocaleString()}</td>
                      <td style={{ padding: '16px' }}>
                        <a href={doc.documentUrl} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'underline' }}>
                          View Document File (S3 Mock Link)
                        </a>
                      </td>
                      <td style={{ padding: '16px', textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '8px' }}>
                          <button className="btn btn-primary btn-sm" onClick={() => handleApproveDoc(doc.id)}>
                            Approve
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleRejectDoc(doc.id)}>
                            Reject
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* DISPUTES RESOLUTION SUB-TAB */}
      {activeTab === 'disputes' && (
        <div style={{ display: 'grid', gridTemplateColumns: selectedDispute ? '1.2fr 1fr' : '1fr', gap: '32px' }}>
          {/* Dispute List */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Platform Disputes Queue</h2>
            
            {disputesLoading ? (
              <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading dispute reports...</div>
            ) : pendingDisputes.length === 0 ? (
              <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>No pending customer dispute tickets found.</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {pendingDisputes.map(d => (
                  <div key={d.id} className="card" style={{ padding: '16px', backgroundColor: 'var(--bg-surface-hover)', border: '1px solid rgba(255,255,255,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                        <strong style={{ fontSize: '1.05rem' }}>Dispute #{d.id}</strong>
                        <span className="badge badge-danger" style={{ fontSize: '0.65rem' }}>{d.reason.replace('_', ' ')}</span>
                      </div>
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '8px' }}>
                        Raised By: User #{d.raisedBy} | Booking: #{d.bookingId}
                      </div>
                      <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '8px', fontStyle: 'italic' }}>
                        "{d.description}"
                      </p>
                    </div>
                    <button className="btn btn-secondary btn-sm" onClick={() => handleOpenResolveDispute(d)}>
                      Investigate & Resolve
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Resolve Form Panel */}
          {selectedDispute && (
            <div className="card" style={{ alignSelf: 'start' }}>
              <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Resolve Dispute #{selectedDispute.id}</h2>
              <form onSubmit={handleResolveDisputeSubmit}>
                <div className="form-group">
                  <label className="form-label">Resolution Status</label>
                  <select 
                    className="form-select"
                    value={resolveForm.status}
                    onChange={(e) => setResolveForm(prev => ({ ...prev, status: e.target.value }))}
                  >
                    <option value="RESOLVED">RESOLVED (Apply penalty / fix cargo)</option>
                    <option value="DISMISSED">DISMISSED (Invalid dispute report)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Admin Investigation Notes</label>
                  <textarea 
                    rows="5"
                    className="form-textarea"
                    placeholder="Enter resolution notes, refund allocations, or warnings issued."
                    value={resolveForm.adminNotes}
                    onChange={(e) => setResolveForm(prev => ({ ...prev, adminNotes: e.target.value }))}
                    required
                  />
                </div>

                <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
                  <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Submit Resolution</button>
                  <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setSelectedDispute(null)}>Cancel</button>
                </div>
              </form>
            </div>
          )}
        </div>
      )}

      {/* PRICING ENGINE SUB-TAB */}
      {activeTab === 'pricing' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>
          {/* Create Pricing Rule */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '8px', fontFamily: 'var(--font-heading)' }}>Configure Pricing Rules</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '24px' }}>Set dynamic rates per kilometer and per kilogram of cargo for quotation estimation.</p>
            
            <form onSubmit={handlePricingSubmit}>
              <div className="form-group">
                <label className="form-label">Vehicle Type Category</label>
                <select 
                  className="form-select"
                  value={pricingForm.vehicleType}
                  onChange={(e) => setPricingForm(prev => ({ ...prev, vehicleType: e.target.value }))}
                >
                  <option value="TRUCK">Heavy Duty Truck (TRUCK)</option>
                  <option value="MINI_TRUCK">Mini Truck / Bolero (MINI_TRUCK)</option>
                  <option value="TRACTOR">Tractor Trolley (TRACTOR)</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Base Rate Per Kilometer (₹)</label>
                <input 
                  type="number" 
                  value={pricingForm.baseRatePerKm}
                  onChange={(e) => setPricingForm(prev => ({ ...prev, baseRatePerKm: parseFloat(e.target.value) || 0 }))}
                  min="1" 
                  step="0.1" 
                  className="form-input" 
                  required 
                />
              </div>

              <div className="form-group">
                <label className="form-label">Price Per Kilogram (₹)</label>
                <input 
                  type="number" 
                  value={pricingForm.pricePerKg}
                  onChange={(e) => setPricingForm(prev => ({ ...prev, pricePerKg: parseFloat(e.target.value) || 0 }))}
                  min="0.001" 
                  step="0.001" 
                  className="form-input" 
                  required 
                />
              </div>

              <div className="form-group">
                <label className="form-label">Minimum Charge Guarantee (₹)</label>
                <input 
                  type="number" 
                  value={pricingForm.minimumCharge}
                  onChange={(e) => setPricingForm(prev => ({ ...prev, minimumCharge: parseFloat(e.target.value) || 0 }))}
                  min="100" 
                  step="10" 
                  className="form-input" 
                  required 
                />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '16px' }} disabled={pricingSubmitting}>
                {pricingSubmitting ? 'Updating Pricing Rule...' : 'Save & Publish Rates'}
              </button>
            </form>
          </div>

          {/* Pricing Info Card */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '12px' }}>How Pricing Quote is Calculated</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '16px', lineHeight: '1.6' }}>
              When a farmer creates a booking request, the backend matching and billing engine calculates quotes using the formula:
            </p>
            <div style={{ fontFamily: 'monospace', padding: '16px', backgroundColor: 'var(--bg-main)', borderRadius: '8px', color: 'var(--primary)', marginBottom: '20px', border: '1px solid var(--border-color)', fontSize: '0.9rem' }}>
              Price = (DistanceKm * BaseRatePerKm) + (WeightKg * PricePerKg)
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: '1.6' }}>
              If the computed price is less than the vehicle's configured <strong>Minimum Charge</strong>, the minimum charge is applied as the default.
            </p>
          </div>
        </div>
      )}

    </div>
  );
};

export default AdminDashboard;
