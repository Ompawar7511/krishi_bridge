import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

const LOCATION_PRESETS = [
  { city: 'Pune', lat: 18.5204, lng: 73.8567 },
  { city: 'Mumbai', lat: 19.0760, lng: 72.8777 },
  { city: 'Nashik', lat: 19.9975, lng: 73.7898 },
  { city: 'Nagpur', lat: 21.1458, lng: 79.0882 },
  { city: 'Kolhapur', lat: 16.7050, lng: 74.2433 }
];

const TransporterDashboard = () => {
  const { user, showToast } = useAuth();
  
  // Dashboard Tabs
  const [activeTab, setActiveTab] = useState('fleet'); // 'fleet', 'schedule', 'compliance'

  // Fleet State
  const [fleet, setFleet] = useState([]);
  const [fleetLoading, setFleetLoading] = useState(true);
  const [vehicleForm, setVehicleForm] = useState({
    vehicleNumber: '',
    vehicleType: 'TRUCK',
    capacityTons: 10,
    cityPreset: 'Pune',
    latitude: 18.5204,
    longitude: 73.8567
  });

  // Schedule State
  const [selectedVehicle, setSelectedVehicle] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [scheduleLoading, setScheduleLoading] = useState(false);
  const [scheduleForm, setScheduleForm] = useState({
    startTime: '',
    endTime: '',
    status: 'AVAILABLE'
  });

  // Compliance State
  const [documents, setDocuments] = useState([]);
  const [docLoading, setDocLoading] = useState(true);
  const [uploadData, setUploadData] = useState({
    documentType: 'DRIVING_LICENSE',
    file: null
  });
  const [uploading, setUploading] = useState(false);

  // Trips State
  const [trips, setTrips] = useState([]);
  const [tripsLoading, setTripsLoading] = useState(false);

  // Fetch Fleet
  const fetchFleet = async () => {
    setFleetLoading(true);
    try {
      const response = await fetch('/api/v1/vehicle/my-fleet');
      const json = await response.json();
      if (response.ok && json.success) {
        setFleet(json.data);
        if (json.data.length > 0 && !selectedVehicle) {
          setSelectedVehicle(json.data[0]);
        }
      } else {
        showToast(json.message || 'Failed to load fleet', 'error');
      }
    } catch (err) {
      showToast('Error loading fleet', 'error');
    } finally {
      setFleetLoading(false);
    }
  };

  // Fetch Schedules for selected vehicle
  const fetchSchedules = async (vehicle) => {
    if (!vehicle) return;
    setScheduleLoading(true);
    try {
      const response = await fetch(`/api/v1/vehicle/${vehicle.id}/schedules`);
      const json = await response.json();
      if (response.ok && json.success) {
        setSchedules(json.data);
      } else {
        showToast(json.message || 'Failed to load vehicle schedules', 'error');
      }
    } catch (err) {
      showToast('Error loading schedules', 'error');
    } finally {
      setScheduleLoading(false);
    }
  };

  // Fetch Compliance Documents Status
  const fetchDocuments = async () => {
    setDocLoading(true);
    try {
      const response = await fetch('/api/v1/transporter/documents/status');
      const json = await response.json();
      if (response.ok && json.success) {
        setDocuments(json.data);
      } else {
        showToast(json.message || 'Failed to load documents status', 'error');
      }
    } catch (err) {
      showToast('Error loading documents status', 'error');
    } finally {
      setDocLoading(false);
    }
  };

  // Trigger loading based on tabs
  useEffect(() => {
    if (activeTab === 'fleet') {
      fetchFleet();
    } else if (activeTab === 'schedule') {
      fetchFleet();
    } else if (activeTab === 'compliance') {
      fetchDocuments();
    } else if (activeTab === 'trips') {
      fetchTrips();
    }
  }, [activeTab]);

  useEffect(() => {
    if (selectedVehicle) {
      fetchSchedules(selectedVehicle);
    }
  }, [selectedVehicle]);

  // Vehicle form handlers
  const handleLocationPreset = (cityName) => {
    const preset = LOCATION_PRESETS.find(l => l.city === cityName);
    if (!preset) return;
    setVehicleForm(prev => ({
      ...prev,
      cityPreset: cityName,
      latitude: preset.lat,
      longitude: preset.lng
    }));
  };

  const handleVehicleSubmit = async (e) => {
    e.preventDefault();
    if (!vehicleForm.vehicleNumber.trim()) {
      showToast('Please enter vehicle plate number.', 'error');
      return;
    }
    
    try {
      const response = await fetch('/api/v1/vehicle/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          vehicleNumber: vehicleForm.vehicleNumber.toUpperCase(),
          vehicleType: vehicleForm.vehicleType,
          capacityTons: vehicleForm.capacityTons,
          latitude: vehicleForm.latitude,
          longitude: vehicleForm.longitude,
          currentCity: vehicleForm.cityPreset
        })
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Vehicle added to fleet successfully!', 'success');
        setVehicleForm({
          vehicleNumber: '',
          vehicleType: 'TRUCK',
          capacityTons: 10,
          cityPreset: 'Pune',
          latitude: 18.5204,
          longitude: 73.8567
        });
        fetchFleet();
      } else {
        showToast(json.message || 'Failed to add vehicle', 'error');
      }
    } catch (err) {
      showToast('Error communicating with backend', 'error');
    }
  };

  const handleDeleteVehicle = async (id) => {
    if (!confirm('Are you sure you want to delete this vehicle from your fleet?')) return;
    try {
      const response = await fetch(`/api/v1/vehicle/delete/${id}`, {
        method: 'DELETE'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Vehicle removed from fleet.', 'success');
        if (selectedVehicle && selectedVehicle.id === id) {
          setSelectedVehicle(null);
          setSchedules([]);
        }
        fetchFleet();
      } else {
        showToast(json.message || 'Failed to delete vehicle', 'error');
      }
    } catch (err) {
      showToast('Error deleting vehicle', 'error');
    }
  };

  // Schedule form handlers
  const handleScheduleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedVehicle) {
      showToast('Select a vehicle first.', 'error');
      return;
    }
    if (!scheduleForm.startTime || !scheduleForm.endTime) {
      showToast('Select start and end times.', 'error');
      return;
    }

    try {
      // Formats HTML datetime-local (yyyy-MM-ddTHH:mm) to ISO with seconds
      const startIso = scheduleForm.startTime + ':00';
      const endIso = scheduleForm.endTime + ':00';

      const response = await fetch('/api/v1/vehicle/schedule/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          vehicleId: selectedVehicle.id,
          startTime: startIso,
          endTime: endIso,
          status: scheduleForm.status
        })
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Schedule availability block created!', 'success');
        setScheduleForm({ startTime: '', endTime: '', status: 'AVAILABLE' });
        fetchSchedules(selectedVehicle);
      } else {
        showToast(json.message || 'Schedule block overlap or invalid request.', 'error');
      }
    } catch (err) {
      showToast('Error saving schedule block', 'error');
    }
  };

  // Compliance upload handlers
  const handleFileChange = (e) => {
    setUploadData(prev => ({ ...prev, file: e.target.files[0] }));
  };

  const handleDocumentSubmit = async (e) => {
    e.preventDefault();
    if (!uploadData.file) {
      showToast('Please select a file to upload.', 'error');
      return;
    }

    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('documentType', uploadData.documentType);
      formData.append('file', uploadData.file);

      const response = await fetch('/api/v1/transporter/documents/upload', {
        method: 'POST',
        body: formData
      });
      const json = await response.json();
      setUploading(false);
      if (response.ok && json.success) {
        showToast(`${uploadData.documentType.replace('_', ' ')} uploaded successfully.`, 'success');
        fetchDocuments();
      } else {
        showToast(json.message || 'Upload failed.', 'error');
      }
    } catch (err) {
      setUploading(false);
      showToast('Server upload error', 'error');
    }
  };

  const fetchTrips = async () => {
    setTripsLoading(true);
    try {
      const response = await fetch('/api/v1/transporter/booking/history');
      const json = await response.json();
      if (response.ok && json.success) {
        setTrips(json.data);
      } else {
        showToast(json.message || 'Failed to load trips', 'error');
      }
    } catch (err) {
      showToast('Error loading trips', 'error');
    } finally {
      setTripsLoading(false);
    }
  };

  const handleCompleteTrip = async (bookingId) => {
    if (!confirm('Are you sure you want to mark this trip as completed? This will notify the farmer.')) return;
    try {
      const response = await fetch(`/api/v1/transporter/booking/complete/${bookingId}`, {
        method: 'POST'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Trip completed successfully and farmer notified!', 'success');
        fetchTrips();
      } else {
        showToast(json.message || 'Failed to complete trip', 'error');
      }
    } catch (err) {
      showToast('Error completing trip', 'error');
    }
  };

  const handleCancelTrip = async (bookingId) => {
    if (!confirm('Are you sure you want to cancel and remove this booked trip? The farmer will be notified.')) return;
    try {
      const response = await fetch(`/api/v1/transporter/booking/cancel/${bookingId}`, {
        method: 'POST'
      });
      const json = await response.json();
      if (response.ok && json.success) {
        showToast('Trip cancelled and removed successfully!', 'success');
        fetchTrips();
      } else {
        showToast(json.message || 'Failed to cancel trip', 'error');
      }
    } catch (err) {
      showToast('Error cancelling trip', 'error');
    }
  };

  return (
    <div className="app-container" style={{ padding: '40px 0' }}>
      
      {/* Welcome & Navigation tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <div>
          <h1 style={{ fontSize: '2.5rem', fontFamily: 'var(--font-heading)' }}>Transporter Fleet</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Welcome back. Manage your cargo trucks and availability schedules.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button 
            className={`btn ${activeTab === 'fleet' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('fleet')}
          >
            My Fleet
          </button>
          <button 
            className={`btn ${activeTab === 'schedule' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('schedule')}
          >
            📅 Availability schedules
          </button>
          <button 
            className={`btn ${activeTab === 'compliance' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('compliance')}
          >
            🛡️ Compliance Docs
          </button>
          <button 
            className={`btn ${activeTab === 'trips' ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setActiveTab('trips')}
          >
            🚛 Trips & Jobs
          </button>
        </div>
      </div>

      {/* FLEET MANAGEMENT TAB */}
      {activeTab === 'fleet' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '32px' }}>
          {/* List Fleet */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Registered Cargo Fleet</h2>
            
            {fleetLoading ? (
              <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading fleet list...</div>
            ) : fleet.length === 0 ? (
              <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                You have no vehicles registered. Complete the form to add your first cargo vehicle.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {fleet.map(v => (
                  <div key={v.id} className="card" style={{ padding: '16px', backgroundColor: 'var(--bg-surface-hover)', border: '1px solid rgba(255,255,255,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontSize: '1.3rem' }}>🚛</span>
                        <strong style={{ fontSize: '1.1rem', letterSpacing: '0.05em' }}>{v.vehicleNumber}</strong>
                        <span className="badge badge-info">{v.vehicleType}</span>
                      </div>
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '8px' }}>
                        Capacity: {v.capacityTons} tons | Located: <strong>{v.currentCity}</strong> ({v.latitude.toFixed(2)}, {v.longitude.toFixed(2)})
                      </div>
                    </div>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDeleteVehicle(v.id)}>
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Add Vehicle */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Register New Vehicle</h2>
            
            <form onSubmit={handleVehicleSubmit}>
              <div className="form-group">
                <label className="form-label">Vehicle Plate Number</label>
                <input 
                  type="text" 
                  value={vehicleForm.vehicleNumber}
                  onChange={(e) => setVehicleForm(prev => ({ ...prev, vehicleNumber: e.target.value }))}
                  placeholder="e.g. MH 12 AB 1234" 
                  className="form-input" 
                  required 
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Vehicle Category</label>
                  <select 
                    value={vehicleForm.vehicleType}
                    onChange={(e) => setVehicleForm(prev => ({ ...prev, vehicleType: e.target.value }))}
                    className="form-select"
                  >
                    <option value="TRUCK">Heavy Duty Truck</option>
                    <option value="MINI_TRUCK">Mini Truck / Bolero Pick-up</option>
                    <option value="TRACTOR">Tractor Trolley</option>
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Max Capacity (Tons)</label>
                  <input 
                    type="number" 
                    value={vehicleForm.capacityTons}
                    onChange={(e) => setVehicleForm(prev => ({ ...prev, capacityTons: parseFloat(e.target.value) || 0 }))}
                    min="0.5" 
                    step="0.5" 
                    className="form-input" 
                    required 
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Base Location City (Preset)</label>
                <select 
                  value={vehicleForm.cityPreset}
                  onChange={(e) => handleLocationPreset(e.target.value)}
                  className="form-select"
                >
                  {LOCATION_PRESETS.map(l => <option key={l.city} value={l.city}>{l.city}</option>)}
                </select>
              </div>

              <div className="form-row" style={{ fontSize: '0.8rem', opacity: 0.8 }}>
                <div className="form-group">
                  <label className="form-label">Latitude</label>
                  <input 
                    type="number" 
                    value={vehicleForm.latitude}
                    onChange={(e) => setVehicleForm(prev => ({ ...prev, latitude: parseFloat(e.target.value) || 0 }))}
                    step="0.0001" 
                    className="form-input" 
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Longitude</label>
                  <input 
                    type="number" 
                    value={vehicleForm.longitude}
                    onChange={(e) => setVehicleForm(prev => ({ ...prev, longitude: parseFloat(e.target.value) || 0 }))}
                    step="0.0001" 
                    className="form-input" 
                  />
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '16px' }}>
                Add to My Fleet
              </button>
            </form>
          </div>
        </div>
      )}

      {/* AVAILABILITY SCHEDULES TAB */}
      {activeTab === 'schedule' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '32px' }}>
          {/* Selector & Availability Form */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Declare Availability</h2>
            
            <div className="form-group">
              <label className="form-label">Select Fleet Vehicle</label>
              {fleet.length === 0 ? (
                <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>Add a vehicle in Fleet Tab first.</div>
              ) : (
                <select 
                  className="form-select" 
                  value={selectedVehicle ? selectedVehicle.id : ''} 
                  onChange={(e) => setSelectedVehicle(fleet.find(v => v.id === parseInt(e.target.value)))}
                >
                  {fleet.map(v => <option key={v.id} value={v.id}>{v.vehicleNumber} ({v.vehicleType})</option>)}
                </select>
              )}
            </div>

            {selectedVehicle && (
              <form onSubmit={handleScheduleSubmit} style={{ marginTop: '20px', borderTop: '1px solid var(--border-color)', paddingTop: '20px' }}>
                <div className="form-group">
                  <label className="form-label">Available From (Start Date/Time)</label>
                  <input 
                    type="datetime-local" 
                    value={scheduleForm.startTime}
                    onChange={(e) => setScheduleForm(prev => ({ ...prev, startTime: e.target.value }))}
                    className="form-input" 
                    required 
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Available To (End Date/Time)</label>
                  <input 
                    type="datetime-local" 
                    value={scheduleForm.endTime}
                    onChange={(e) => setScheduleForm(prev => ({ ...prev, endTime: e.target.value }))}
                    className="form-input" 
                    required 
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Schedule Block Status</label>
                  <select 
                    value={scheduleForm.status}
                    onChange={(e) => setScheduleForm(prev => ({ ...prev, status: e.target.value }))}
                    className="form-select"
                  >
                    <option value="AVAILABLE">AVAILABLE (Open to match requests)</option>
                    <option value="MAINTENANCE">MAINTENANCE (Repair/Breakdown)</option>
                    <option value="BLOCKED">BLOCKED (Not working)</option>
                  </select>
                </div>
                
                <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '16px' }}>
                  Reserve Availability Slot
                </button>
              </form>
            )}
          </div>

          {/* List Availability Schedules */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '8px', fontFamily: 'var(--font-heading)' }}>
              Schedule Grid {selectedVehicle ? `- ${selectedVehicle.vehicleNumber}` : ''}
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '24px' }}>Timeline of reservations, matches, and transit slots</p>

            {scheduleLoading ? (
              <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading schedule data...</div>
            ) : !selectedVehicle ? (
              <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Select a vehicle to display schedule logs.</div>
            ) : schedules.length === 0 ? (
              <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>No availability block reserved for this vehicle.</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {schedules.map(s => (
                  <div key={s.id} className="card" style={{ padding: '14px 18px', backgroundColor: 'var(--bg-surface-hover)', border: '1px solid rgba(255,255,255,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                        🕒 <strong>{new Date(s.startTime).toLocaleString()}</strong>
                      </div>
                      <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                        ➡️ <strong>{new Date(s.endTime).toLocaleString()}</strong>
                      </div>
                    </div>
                    <div>
                      <span className={`badge badge-${
                        s.status === 'AVAILABLE' ? 'success' :
                        s.status === 'BOOKED' ? 'info' : 'danger'
                      }`}>
                        {s.status}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* COMPLIANCE DOCUMENT CENTER */}
      {activeTab === 'compliance' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '32px' }}>
          
          {/* Upload Status Card */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '8px', fontFamily: 'var(--font-heading)' }}>Verification Status</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '24px' }}>Admins review uploads. All documents must be APPROVED to receive booking match requests.</p>
            
            {docLoading ? (
              <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading documents database...</div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                {['DRIVING_LICENSE', 'VEHICLE_RC', 'INSURANCE', 'VEHICLE_PERMIT', 'ID_PROOF'].map(docType => {
                  const uploaded = documents.find(d => d.documentType === docType);
                  return (
                    <div key={docType} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px', borderBottom: '1px solid var(--border-color)' }}>
                      <div>
                        <strong>{docType.replace('_', ' ')}</strong>
                        {uploaded && (
                          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                            Uploaded: {new Date(uploaded.uploadedAt).toLocaleDateString()}
                          </div>
                        )}
                      </div>
                      <div>
                        {uploaded ? (
                          <span className={`badge badge-${
                            uploaded.verificationStatus === 'APPROVED' ? 'success' :
                            uploaded.verificationStatus === 'PENDING' ? 'warning' : 'danger'
                          }`}>
                            {uploaded.verificationStatus}
                          </span>
                        ) : (
                          <span className="badge badge-warning" style={{ backgroundColor: 'rgba(245,158,11,0.05)', color: 'var(--text-muted)' }}>MISSING</span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Upload Form */}
          <div className="card">
            <h2 style={{ fontSize: '1.5rem', marginBottom: '24px', fontFamily: 'var(--font-heading)' }}>Upload PDF/Image Document</h2>
            <form onSubmit={handleDocumentSubmit}>
              <div className="form-group">
                <label className="form-label">Document Category</label>
                <select 
                  className="form-select"
                  value={uploadData.documentType}
                  onChange={(e) => setUploadData(prev => ({ ...prev, documentType: e.target.value }))}
                >
                  <option value="DRIVING_LICENSE">Driving License</option>
                  <option value="VEHICLE_RC">Vehicle RC Book (Registration)</option>
                  <option value="INSURANCE">Commercial Fleet Insurance</option>
                  <option value="VEHICLE_PERMIT">National/State Transit Permit</option>
                  <option value="ID_PROOF">Owner Identity Proof (Aadhar/PAN)</option>
                </select>
              </div>

              <div className="form-group" style={{ margin: '24px 0' }}>
                <label className="form-label">Select File (PDF, PNG, JPG)</label>
                <input 
                  type="file" 
                  onChange={handleFileChange}
                  accept=".pdf,image/*"
                  className="form-input"
                  style={{ padding: '8px' }}
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={uploading}>
                {uploading ? 'Uploading doc...' : 'Upload & Submit for Audit'}
              </button>
            </form>
          </div>

        </div>
      )}

      {/* MY TRIPS TAB */}
      {activeTab === 'trips' && (
        <div className="card">
          <h2 style={{ fontSize: '1.5rem', marginBottom: '8px', fontFamily: 'var(--font-heading)' }}>Assigned Cargo Trips & Jobs</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '24px' }}>Accept matches on the farmer platform to see jobs here. Mark trips as completed once delivered.</p>

          {tripsLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading trips data...</div>
          ) : trips.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>No trips or delivery jobs assigned to you yet.</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {trips.map(t => (
                <div key={t.id} className="card" style={{ padding: '20px', backgroundColor: 'var(--bg-surface-hover)', border: '1px solid rgba(255,255,255,0.04)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <span style={{ fontSize: '1.2rem' }}>📦</span>
                      <strong style={{ fontSize: '1.1rem' }}>Job #{t.id} - {t.cropType}</strong>
                      <span className={`badge badge-${t.status === 'TRANSPORTER_ACCEPTED' ? 'info' : t.status === 'COMPLETED' ? 'success' : 'danger'}`}>
                        {t.status.replace('_', ' ')}
                      </span>
                    </div>
                    <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '8px' }}>
                      Weight: <strong>{t.weightTons} tons</strong> | Distance: <strong>{t.estimatedDistanceKm} km</strong> | Earnings: <strong style={{ color: 'var(--success)' }}>₹{t.estimatedPrice}</strong>
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '6px' }}>
                      GPS Route: ({t.pickupLatitude.toFixed(4)}, {t.pickupLongitude.toFixed(4)}) → ({t.destinationLatitude.toFixed(4)}, {t.destinationLongitude.toFixed(4)})
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    {t.status === 'TRANSPORTER_ACCEPTED' && (
                      <>
                        <button 
                          className="btn btn-primary" 
                          onClick={() => handleCompleteTrip(t.id)}
                        >
                          ✓ Complete Trip
                        </button>
                        <button 
                          className="btn btn-danger" 
                          onClick={() => handleCancelTrip(t.id)}
                          style={{ backgroundColor: 'var(--danger)', color: 'white' }}
                        >
                          ❌ Remove Trip
                        </button>
                      </>
                    )}
                    {t.status === 'COMPLETED' && (
                      <span style={{ color: 'var(--text-muted)', fontWeight: 'bold', fontSize: '0.9rem' }}>🎉 Delivered & Completed</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

    </div>
  );
};

export default TransporterDashboard;
