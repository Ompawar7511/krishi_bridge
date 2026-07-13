import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const CITY_PRESETS = [
  { name: 'Pune', lat: 18.5204, lng: 73.8567 },
  { name: 'Mumbai', lat: 19.0760, lng: 72.8777 },
  { name: 'Nashik', lat: 19.9975, lng: 73.7898 },
  { name: 'Nagpur', lat: 21.1458, lng: 79.0882 },
  { name: 'Kolhapur', lat: 16.7050, lng: 74.2433 }
];

const FarmerDashboard = () => {
  const { user, showToast } = useAuth();
  
  // Tabs
  const [activeTab, setActiveTab] = useState('bookings'); // 'bookings' or 'create'

  // Booking Create State
  const [bookingForm, setBookingForm] = useState({
    cropType: 'Wheat',
    weightTons: 5,
    pickupCity: 'Pune',
    pickupLat: 18.5204,
    pickupLng: 73.8567,
    destCity: 'Mumbai',
    destLat: 19.0760,
    destLng: 72.8777,
    vehicleType: 'TRUCK'
  });
  
  const [estimatedPrice, setEstimatedPrice] = useState(null);
  const [pricingLoading, setPricingLoading] = useState(false);
  const [bookingHistory, setBookingHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(true);

  // Notifications State
  const [notifications, setNotifications] = useState([]);
  const [notificationsLoading, setNotificationsLoading] = useState(false);

  // Match / Dispute Overlay state
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [matches, setMatches] = useState([]);
  const [matchesLoading, setMatchesLoading] = useState(false);
  
  // Modals / Sub-forms
  const [showMatchPanel, setShowMatchPanel] = useState(false);
  const [showRatingPanel, setShowRatingPanel] = useState(false);
  const [showDisputePanel, setShowDisputePanel] = useState(false);

  // Rating state
  const [ratingData, setRatingData] = useState({
    rating: 5,
    review: ''
  });

  // Dispute state
  const [disputeData, setDisputeData] = useState({
    reason: 'DELAYED_DELIVERY',
    description: ''
  });

  // Calculate distance between two coordinates using Haversine
  const calculateDistance = (lat1, lon1, lat2, lon2) => {
    const R = 6371; // Radius of earth in km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = 
      Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * 
      Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return parseFloat((R * c).toFixed(1));
  };

  const calculatedDistance = calculateDistance(
    bookingForm.pickupLat,
    bookingForm.pickupLng,
    bookingForm.destLat,
    bookingForm.destLng
  );

  // Fetch Booking History
  const fetchHistory = async () => {
    setHistoryLoading(true);
    try {
      const response = await fetch('/api/v1/booking/history');
      const json = await response.json();
      if (response.ok && json.success) {
        setBookingHistory(json.data);
      } else {
        showToast(json.message || 'Failed to load booking history', 'error');
      }
    } catch (err) {
      showToast('Error loading booking history', 'error');
    } finally {
      setHistoryLoading(false);
    }
  };

  const fetchNotifications = async () => {
    setNotificationsLoading(true);
    try {
      const response = await fetch('/api/v1/notifications');
      const json = await response.json();
      if (response.ok && json.success) {
        setNotifications(json.data);
      } else {
        showToast(json.message || 'Failed to load notifications', 'error');
      }
    } catch (err) {
      showToast('Error loading notifications', 'error');
    } finally {
      setNotificationsLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      const response = await fetch(`/api/v1/notifications/read/${id}`, {
        method: 'PUT'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        fetchNotifications();
      } else {
        showToast(json.message || 'Failed to mark notification as read', 'error');
      }
    } catch (err) {
      showToast('Error updating notification', 'error');
    }
  };

  // Initial load of notifications
  useEffect(() => {
    fetchNotifications();
    
    // Poll notifications every 10 seconds to keep dashboard updated
    const interval = setInterval(fetchNotifications, 10000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (activeTab === 'bookings') {
      fetchHistory();
    }
    fetchNotifications();
  }, [activeTab]);

  // Fetch Quote Price dynamically
  useEffect(() => {
    const getPricingQuote = async () => {
      setPricingLoading(true);
      try {
        const response = await fetch('/api/v1/pricing/calculate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            vehicleType: bookingForm.vehicleType,
            distanceKm: calculatedDistance,
            weightKg: bookingForm.weightTons * 1000
          })
        });
        const json = await response.json();
        if (response.ok && json.success) {
          setEstimatedPrice(json.data.estimatedPrice);
        } else {
          setEstimatedPrice(null);
        }
      } catch (err) {
        setEstimatedPrice(null);
      } finally {
        setPricingLoading(false);
      }
    };

    const timer = setTimeout(() => {
      if (calculatedDistance > 0) {
        getPricingQuote();
      }
    }, 600); // Debounce pricing calls

    return () => clearTimeout(timer);
  }, [bookingForm.vehicleType, calculatedDistance, bookingForm.weightTons]);

  const handleCityChange = (type, cityName) => {
    const preset = CITY_PRESETS.find(c => c.name === cityName);
    if (!preset) return;
    
    if (type === 'pickup') {
      setBookingForm(prev => ({
        ...prev,
        pickupCity: cityName,
        pickupLat: preset.lat,
        pickupLng: preset.lng
      }));
    } else {
      setBookingForm(prev => ({
        ...prev,
        destCity: cityName,
        destLat: preset.lat,
        destLng: preset.lng
      }));
    }
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setBookingForm(prev => ({
      ...prev,
      [name]: name === 'weightTons' || name.endsWith('Lat') || name.endsWith('Lng') ? parseFloat(value) || 0 : value
    }));
  };

  const handleCreateBooking = async (e) => {
    e.preventDefault();
    if (estimatedPrice === null) {
      showToast('Wait for the quote calculation before booking.', 'error');
      return;
    }

    try {
      const payload = {
        cropType: bookingForm.cropType,
        weightTons: bookingForm.weightTons,
        pickupLatitude: bookingForm.pickupLat,
        pickupLongitude: bookingForm.pickupLng,
        destinationLatitude: bookingForm.destLat,
        destinationLongitude: bookingForm.destLng,
        estimatedDistanceKm: calculatedDistance,
        vehicleType: bookingForm.vehicleType,
        idempotencyKey: 'idem-' + Date.now() + '-' + Math.random().toString(36).substring(2, 7)
      };

      const response = await fetch('/api/v1/booking/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Booking request submitted successfully!', 'success');
        setActiveTab('bookings'); // Switch back to history
      } else {
        showToast(json.message || 'Failed to submit booking', 'error');
      }
    } catch (err) {
      showToast('Server error submitting booking.', 'error');
    }
  };

  const handleCancelBooking = async (bookingId) => {
    if (!confirm('Are you sure you want to cancel this booking request?')) return;
    try {
      const response = await fetch(`/api/v1/booking/cancel/${bookingId}`, {
        method: 'DELETE'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Booking request cancelled.', 'success');
        fetchHistory();
      } else {
        showToast(json.message || 'Failed to cancel booking', 'error');
      }
    } catch (err) {
      showToast('Error cancelling booking', 'error');
    }
  };

  const handleOpenMatches = async (booking) => {
    setSelectedBooking(booking);
    setMatchesLoading(true);
    setShowMatchPanel(true);
    try {
      const response = await fetch(`/api/v1/matching/find/${booking.id}`);
      const json = await response.json();
      if (response.ok && json.success) {
        setMatches(json.data);
        if (json.data.length === 0) {
          showToast('No matching transporters found nearby. Try updating schedule/capacity.', 'warning');
        }
      } else {
        showToast(json.message || 'Failed to look up matches', 'error');
      }
    } catch (err) {
      showToast('Error fetching matches', 'error');
    } finally {
      setMatchesLoading(false);
    }
  };

  const handleAcceptTransporter = async (match) => {
    try {
      const response = await fetch(`/api/v1/matching/accept/${selectedBooking.id}/transporter/${match.transporterId}/vehicle/${match.vehicleId}`, {
        method: 'POST'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast(`Assigned transporter ${match.transporterName} to booking!`, 'success');
        setShowMatchPanel(false);
        fetchHistory();
      } else {
        showToast(json.message || 'Assignment failed.', 'error');
      }
    } catch (err) {
      showToast('Error assigning transporter', 'error');
    }
  };

  const handleRejectTransporter = async (match) => {
    try {
      const response = await fetch(`/api/v1/matching/reject/${selectedBooking.id}/transporter/${match.transporterId}`, {
        method: 'POST'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Transporter match rejected.', 'info');
        // Refresh matches
        handleOpenMatches(selectedBooking);
      } else {
        showToast(json.message || 'Rejection failed.', 'error');
      }
    } catch (err) {
      showToast('Error rejecting transporter', 'error');
    }
  };

  const handleOpenRating = (booking) => {
    setSelectedBooking(booking);
    setRatingData({ rating: 5, review: '' });
    setShowRatingPanel(true);
  };

  const handleSubmitRating = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('/api/v1/rating/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          bookingId: selectedBooking.id,
          rating: ratingData.rating,
          review: ratingData.review
        })
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Review submitted successfully. Job Completed!', 'success');
        setShowRatingPanel(false);
        fetchHistory();
      } else {
        showToast(json.message || 'Failed to submit rating', 'error');
      }
    } catch (err) {
      showToast('Error submitting review', 'error');
    }
  };

  const handleOpenDispute = (booking) => {
    setSelectedBooking(booking);
    setDisputeData({ reason: 'DELAYED_DELIVERY', description: '' });
    setShowDisputePanel(true);
  };

  const handleSubmitDispute = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('/api/v1/disputes/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          bookingId: selectedBooking.id,
          reason: disputeData.reason,
          description: disputeData.description
        })
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Dispute ticket raised. Marketplace admins will investigate.', 'warning');
        setShowDisputePanel(false);
        fetchHistory();
      } else {
        showToast(json.message || 'Failed to raise dispute', 'error');
      }
    } catch (err) {
      showToast('Error filing dispute', 'error');
    }
  };

  return (
    <div className="app-container" style={{ padding: '40px 0' }}>
      
      {/* Welcome & Navigation tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h1 style={{ fontSize: '2.5rem', fontFamily: 'var(--font-heading)' }}>Farmer Portal</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Welcome back. Manage your crop logistics and hire verified trucks.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button 
            className={`btn ${activeTab === 'bookings' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('bookings')}
          >
            My Bookings
          </button>
          <button 
            className={`btn ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('create')}
          >
            🌾 Request Transport
          </button>
          <button 
            className={`btn ${activeTab === 'notifications' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('notifications')}
            style={{ position: 'relative' }}
          >
            🔔 Notifications
            {notifications.filter(n => !n.read).length > 0 && (
              <span style={{
                position: 'absolute',
                top: '-8px',
                right: '-8px',
                backgroundColor: 'var(--danger)',
                color: 'white',
                borderRadius: '50%',
                padding: '4px 8px',
                fontSize: '0.75rem',
                fontWeight: 'bold',
                lineHeight: 1
              }}>
                {notifications.filter(n => !n.read).length}
              </span>
            )}
          </button>
        </div>
      </div>

      {/* CREATE TAB */}
      {activeTab === 'create' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }}>
          {/* Form Card */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Book Transit Cargo</h2>
            
            <form onSubmit={handleCreateBooking}>
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Crop Type</label>
                  <select name="cropType" value={bookingForm.cropType} onChange={handleFormChange} className="form-select">
                    <option value="Wheat">Wheat</option>
                    <option value="Rice">Rice</option>
                    <option value="Sugarcane">Sugarcane</option>
                    <option value="Cotton">Cotton</option>
                    <option value="Maize">Maize</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Weight (Tons)</label>
                  <input 
                    type="number" 
                    name="weightTons" 
                    value={bookingForm.weightTons} 
                    onChange={handleFormChange} 
                    min="0.1" 
                    step="0.1" 
                    className="form-input" 
                    required 
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Pickup City Preset</label>
                  <select value={bookingForm.pickupCity} onChange={(e) => handleCityChange('pickup', e.target.value)} className="form-select">
                    {CITY_PRESETS.map(c => <option key={c.name} value={c.name}>{c.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Destination City Preset</label>
                  <select value={bookingForm.destCity} onChange={(e) => handleCityChange('destination', e.target.value)} className="form-select">
                    {CITY_PRESETS.map(c => <option key={c.name} value={c.name}>{c.name}</option>)}
                  </select>
                </div>
              </div>

              {/* Coordinates modification panel */}
              <div className="form-row" style={{ fontSize: '0.8rem', opacity: 0.8 }}>
                <div className="form-group">
                  <label className="form-label">Pickup Coordinates</label>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <input type="number" name="pickupLat" value={bookingForm.pickupLat} onChange={handleFormChange} step="0.0001" className="form-input" style={{ padding: '6px' }} />
                    <input type="number" name="pickupLng" value={bookingForm.pickupLng} onChange={handleFormChange} step="0.0001" className="form-input" style={{ padding: '6px' }} />
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">Destination Coordinates</label>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <input type="number" name="destLat" value={bookingForm.destLat} onChange={handleFormChange} step="0.0001" className="form-input" style={{ padding: '6px' }} />
                    <input type="number" name="destLng" value={bookingForm.destLng} onChange={handleFormChange} step="0.0001" className="form-input" style={{ padding: '6px' }} />
                  </div>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Vehicle Type Required</label>
                <select name="vehicleType" value={bookingForm.vehicleType} onChange={handleFormChange} className="form-select">
                  <option value="TRUCK">High-Capacity Truck (Flatbed/Box)</option>
                  <option value="MINI_TRUCK">Mini Truck (Tempo/Bolero)</option>
                  <option value="TRACTOR">Tractor Trolley (Short-haul)</option>
                </select>
              </div>

              <div style={{ margin: '24px 0', padding: '16px', background: 'rgba(255,255,255,0.02)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Calculated Distance:</span>
                  <span><strong>{calculatedDistance} km</strong></span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-secondary)' }}>Estimated Fare:</span>
                  <span>
                    {pricingLoading ? 'Calculating...' : estimatedPrice ? (
                      <strong style={{ color: 'var(--primary)', fontSize: '1.2rem' }}>₹{estimatedPrice}</strong>
                    ) : 'N/A'}
                  </span>
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={pricingLoading || estimatedPrice === null}>
                Submit & Find Transporters
              </button>
            </form>
          </div>

          {/* Interactive Map card */}
          <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '8px', fontFamily: 'var(--font-heading)' }}>Interactive Cargo Route</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '16px' }}>Showing simulated GPS coordinates and transit path</p>
            
            <div className="simulated-map" style={{ flex: 1 }}>
              <div className="map-grid"></div>
              {/* Pickup Marker (Mapped to relative percentage based on Pune/Mumbai coordinates for visual mockup) */}
              <div className="map-marker" style={{ top: '60%', left: '30%' }}>
                <div className="map-marker-dot marker-pickup"></div>
                <span className="map-marker-label">Pickup: {bookingForm.pickupCity} ({bookingForm.pickupLat.toFixed(2)}, {bookingForm.pickupLng.toFixed(2)})</span>
              </div>
              {/* Destination Marker */}
              <div className="map-marker" style={{ top: '35%', left: '70%' }}>
                <div className="map-marker-dot marker-destination"></div>
                <span className="map-marker-label">Dest: {bookingForm.destCity} ({bookingForm.destLat.toFixed(2)}, {bookingForm.destLng.toFixed(2)})</span>
              </div>
              {/* Route path line */}
              <svg style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', pointerEvents: 'none' }}>
                <line x1="30%" y1="60%" x2="70%" y2="35%" stroke="var(--primary)" strokeWidth="3" strokeDasharray="6,6" style={{ opacity: 0.7 }} />
              </svg>
            </div>
            
            <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '16px' }}>
              ℹ️ Presets auto-calculate precise distance. Transporters will execute matching within 20km of the pickup coordinates.
            </div>
          </div>
        </div>
      )}

      {/* BOOKINGS HISTORY TAB */}
      {activeTab === 'bookings' && (
        <div className="card">
          <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>My Delivery Requests</h2>
          
          {historyLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading history...</div>
          ) : bookingHistory.length === 0 ? (
            <div style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)' }}>
              <p style={{ fontSize: '1.1rem', marginBottom: '16px' }}>No booking requests found.</p>
              <button className="btn btn-primary" onClick={() => setActiveTab('create')}>Create First Request</button>
            </div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                    <th style={{ padding: '16px' }}>ID / Created</th>
                    <th style={{ padding: '16px' }}>Crop & Weight</th>
                    <th style={{ padding: '16px' }}>Route Details</th>
                    <th style={{ padding: '16px' }}>Fare Quote</th>
                    <th style={{ padding: '16px' }}>Status</th>
                    <th style={{ padding: '16px' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {bookingHistory.map(b => (
                    <tr key={b.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.95rem' }}>
                      <td style={{ padding: '16px' }}>
                        <div><strong>#{b.id}</strong></div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{new Date(b.createdAt).toLocaleDateString()}</div>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <div><strong>{b.cropType}</strong></div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{b.weightTons} tons</div>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <div>{b.estimatedDistanceKm} km</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>GPS: ({b.pickupLatitude.toFixed(2)}, {b.pickupLongitude.toFixed(2)}) → ({b.destinationLatitude.toFixed(2)}, {b.destinationLongitude.toFixed(2)})</div>
                      </td>
                      <td style={{ padding: '16px', fontWeight: 600 }}>₹{b.estimatedPrice}</td>
                      <td style={{ padding: '16px' }}>
                        <span className={`badge badge-${
                          b.status === 'CREATED' ? 'info' :
                          b.status === 'WAITING_TRANSPORTER' ? 'warning' :
                          b.status === 'TRANSPORTER_ACCEPTED' ? 'success' :
                          b.status === 'COMPLETED' ? 'success' : 'danger'
                        }`}>
                          {b.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                          {(b.status === 'CREATED' || b.status === 'WAITING_TRANSPORTER') && (
                            <button className="btn btn-secondary btn-sm" onClick={() => handleOpenMatches(b)}>
                              🔍 Find Match
                            </button>
                          )}
                          
                          {b.status === 'TRANSPORTER_ACCEPTED' && (
                            <>
                              <button className="btn btn-primary btn-sm" onClick={() => handleOpenRating(b)}>
                                ✓ Complete Transit
                              </button>
                              <button className="btn btn-danger btn-sm" onClick={() => handleOpenDispute(b)}>
                                ⚠️ Dispute
                              </button>
                            </>
                          )}

                          {b.status !== 'COMPLETED' && b.status !== 'CANCELLED' && (
                            <button 
                              className="btn btn-sm" 
                              style={{ backgroundColor: 'transparent', border: '1px solid var(--danger)', color: 'var(--danger)' }}
                              onClick={() => handleCancelBooking(b.id)}
                            >
                              Cancel
                            </button>
                          )}

                          {b.status === 'COMPLETED' && (
                            <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 'bold' }}>Job Complete</span>
                          )}
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

      {/* NOTIFICATIONS TAB */}
      {activeTab === 'notifications' && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
            <h2 style={{ fontSize: '1.5rem', fontFamily: 'var(--font-heading)' }}>Notification Inbox</h2>
            {notifications.filter(n => !n.read).length > 0 && (
              <button 
                className="btn btn-secondary btn-sm" 
                onClick={async () => {
                  for (const n of notifications) {
                    if (!n.read) {
                      await handleMarkAsRead(n.id);
                    }
                  }
                }}
              >
                ✓ Mark all as read
              </button>
            )}
          </div>

          {notificationsLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading notifications...</div>
          ) : notifications.length === 0 ? (
            <div style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)' }}>
              <p style={{ fontSize: '1.1rem', color: 'var(--text-secondary)' }}>No notifications yet.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {notifications.map(n => (
                <div 
                  key={n.id} 
                  className="card" 
                  style={{ 
                    padding: '16px 20px', 
                    backgroundColor: n.read ? 'var(--bg-surface)' : 'rgba(16, 185, 129, 0.05)', 
                    borderLeft: n.read ? '4px solid var(--border-color)' : '4px solid var(--primary)',
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    transition: 'all 0.2s ease'
                  }}
                >
                  <div style={{ flex: 1, paddingRight: '16px' }}>
                    <p style={{ margin: 0, fontSize: '0.975rem', color: n.read ? 'var(--text-secondary)' : 'var(--text-main)', fontWeight: n.read ? 'normal' : '500' }}>
                      {n.message}
                    </p>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px', display: 'inline-block' }}>
                      {new Date(n.createdAt).toLocaleString()}
                    </span>
                  </div>
                  {!n.read && (
                    <button 
                      className="btn btn-secondary btn-sm" 
                      onClick={() => handleMarkAsRead(n.id)}
                      style={{ fontSize: '0.8rem', padding: '6px 12px' }}
                    >
                      Mark Read
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* MATCH MODAL OVERLAY */}
      {showMatchPanel && selectedBooking && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="card" style={{ width: '90%', maxWidth: '650px', maxHeight: '85vh', display: 'flex', flexDirection: 'column' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h2 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.4rem' }}>Matches for Request #{selectedBooking.id}</h2>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowMatchPanel(false)}>Close</button>
            </div>
            
            <div style={{ flex: 1, overflowY: 'auto', paddingRight: '4px' }}>
              {matchesLoading ? (
                <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-secondary)' }}>Searching nearby transporters...</div>
              ) : matches.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '40px' }}>
                  <p style={{ color: 'var(--text-secondary)', marginBottom: '16px' }}>No nearby verified transporters found matching coordinates and weight criteria.</p>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Note: Ensure a transporter has registered a vehicle near ({selectedBooking.pickupLatitude}, {selectedBooking.pickupLongitude}) and that their documents are approved by admin.</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {matches.map(m => (
                    <div key={m.vehicleId} className="card" style={{ padding: '16px', backgroundColor: 'var(--bg-surface-hover)', border: '1px solid rgba(255,255,255,0.05)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <h4 style={{ fontSize: '1.1rem', marginBottom: '4px' }}>{m.transporterName}</h4>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                          🚛 Vehicle: <strong>{m.vehicleNumber}</strong> ({m.vehicleType})
                        </div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                          Capacity: {m.capacityTons} tons | Dist to Pickup: <strong>{m.distanceKm.toFixed(1)} km</strong>
                        </div>
                      </div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                        <button className="btn btn-primary btn-sm" onClick={() => handleAcceptTransporter(m)}>
                          Assign Truck
                        </button>
                        <button className="btn btn-secondary btn-sm" onClick={() => handleRejectTransporter(m)}>
                          Reject Match
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* DISPUTE MODAL */}
      {showDisputePanel && selectedBooking && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px' }}>
            <h2 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.4rem', marginBottom: '16px' }}>Raise Dispute - Job #{selectedBooking.id}</h2>
            <form onSubmit={handleSubmitDispute}>
              <div className="form-group">
                <label className="form-label">Dispute Reason</label>
                <select 
                  className="form-select"
                  value={disputeData.reason}
                  onChange={(e) => setDisputeData(prev => ({ ...prev, reason: e.target.value }))}
                >
                  <option value="DELAYED_DELIVERY">Delayed Delivery</option>
                  <option value="DAMAGED_CARGO">Damaged Cargo</option>
                  <option value="TRANSPORTER_MISCONDUCT">Transporter Misconduct</option>
                  <option value="OVERCHARGING_FRAUD">Pricing Anomaly / Fraud</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Detailed Description</label>
                <textarea 
                  rows="4"
                  className="form-textarea"
                  placeholder="Describe the issue in detail. Admins will review the audit logs."
                  value={disputeData.description}
                  onChange={(e) => setDisputeData(prev => ({ ...prev, description: e.target.value }))}
                  required
                />
              </div>
              <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
                <button type="submit" className="btn btn-danger" style={{ flex: 1 }}>Submit Dispute Ticket</button>
                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowDisputePanel(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* RATING / COMPLETION MODAL */}
      {showRatingPanel && selectedBooking && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.8)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
          <div className="card" style={{ width: '90%', maxWidth: '500px' }}>
            <h2 style={{ fontFamily: 'var(--font-heading)', fontSize: '1.4rem', marginBottom: '16px' }}>Complete Transit & Review</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '20px' }}>
              Confirming completion will release payment. Please rate the transporter's service.
            </p>
            <form onSubmit={handleSubmitRating}>
              <div className="form-group">
                <label className="form-label">Service Rating (1 to 5 Stars)</label>
                <select 
                  className="form-select"
                  value={ratingData.rating}
                  onChange={(e) => setRatingData(prev => ({ ...prev, rating: parseInt(e.target.value) }))}
                >
                  <option value="5">⭐⭐⭐⭐⭐ Excellent (5/5)</option>
                  <option value="4">⭐⭐⭐⭐ Very Good (4/5)</option>
                  <option value="3">⭐⭐⭐ Satisfactory (3/5)</option>
                  <option value="2">⭐⭐ Fair (2/5)</option>
                  <option value="1">⭐ Poor (1/5)</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Written Feedback</label>
                <textarea 
                  rows="3"
                  className="form-textarea"
                  placeholder="Optional review feedback..."
                  value={ratingData.review}
                  onChange={(e) => setRatingData(prev => ({ ...prev, review: e.target.value }))}
                />
              </div>
              <div style={{ display: 'flex', gap: '12px', marginTop: '20px' }}>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>Confirm Delivery & Rate</button>
                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowRatingPanel(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};

export default FarmerDashboard;
