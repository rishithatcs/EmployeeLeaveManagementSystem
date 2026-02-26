import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Button, Card, TextField, MenuItem, Typography, Grid, Modal, Alert } from '@mui/material';

const leaveTypes = [
  { label: 'PAID', value: 'PAID' },
  { label: 'SICK', value: 'SICK' },
  { label: 'UNPAID', value: 'UNPAID' },
  { label: 'VACATION', value: 'VACATION' },
];

const LeaveRequests = () => {
  const { user } = useAuth();
  const [leaves, setLeaves] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  // FIX: Fields now match backend API
  const [form, setForm] = useState({ leaveType: '', startDate: '', endDate: '', notes: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Helper to safely render leaveType
  const renderLeaveType = lt => {
    if (lt && typeof lt === 'object') {
      return lt.name || lt.type || JSON.stringify(lt);
    }
    return lt;
  };

  // Get token robustly: from context or localStorage
  const getToken = () => (user && user.token) ? user.token : localStorage.getItem('token');

  const fetchLeaves = () => {
    api.leaveHistory(getToken()).then(setLeaves).catch(() => setLeaves([]));
  };

  useEffect(() => {
    fetchLeaves();
    // eslint-disable-next-line
  }, [user]);

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    if (!form.leaveType || !form.startDate || !form.endDate || !form.notes) {
      setError('All fields required');
      return;
    }
    setError(''); setSuccess('');
    const token = getToken();
    if (!token) {
      setError("Session expired or not logged in. Please log in again.");
      return;
    }
    try {
      await api.createLeaveRequest(form, token);
      setSuccess('Leave request submitted!');
      setModalOpen(false);
      setForm({ leaveType: '', startDate: '', endDate: '', notes: '' });
      fetchLeaves();
    } catch (err) {
      if (err.message.toLowerCase().includes('403')) {
        setError("Forbidden: You are not authorized. Please log in as an employee.");
      } else if (err.message.toLowerCase().includes('401')) {
        setError("Unauthorized: Your session is invalid or expired.");
      } else {
        setError(err.message);
      }
    }
  };

  return (
    <div>
      <Typography variant="h5" sx={{ mb: 2 }}>Your Leave Requests</Typography>
      <Button variant="contained" color="primary" onClick={() => setModalOpen(true)} sx={{ mb: 2 }}>Apply for Leave</Button>
      <Grid container spacing={2}>
        {leaves.length ? leaves.map(lr => (
          <Grid item xs={12} md={6} key={lr.id}>
            <Card sx={{ p: 2, mb: 1, background: lr.status === 'APPROVED' ? '#e7ffe7' : lr.status === 'REJECTED' ? '#ffe7e7' : '#fff' }}>
              <Typography><b>{renderLeaveType(lr.leaveType)}</b> | {lr.status}</Typography>
              {/* Fields now match backend */}
              <Typography>{lr.startDate} to {lr.endDate}</Typography>
              <Typography variant="body2"><i>{lr.notes}</i></Typography>
            </Card>
          </Grid>
        )) : <Typography sx={{ px: 2 }}>No leaves found.</Typography>}
      </Grid>
      <Modal open={modalOpen} onClose={() => setModalOpen(false)}>
        <Card sx={{ width: 400, margin: '10% auto', p: 3, outline: 'none' }}>
          <Typography variant="h6" sx={{ mb: 2 }}>Apply for Leave</Typography>
          <TextField select fullWidth label="Leave Type" name="leaveType" value={form.leaveType} onChange={handleChange} sx={{ mb: 2 }}>
            {leaveTypes.map(lt => <MenuItem key={lt.value} value={lt.value}>{lt.label}</MenuItem>)}
          </TextField>
          {/* Field names now match backend API */}
          <TextField fullWidth type="date" label="From" name="startDate" value={form.startDate} onChange={handleChange} sx={{ mb: 2 }} InputLabelProps={{ shrink: true }} />
          <TextField fullWidth type="date" label="To" name="endDate" value={form.endDate} onChange={handleChange} sx={{ mb: 2 }} InputLabelProps={{ shrink: true }} />
          <TextField multiline fullWidth label="Reason" name="notes" value={form.notes} onChange={handleChange} sx={{ mb: 2 }} />
          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
          {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
          <Button variant="contained" onClick={handleSubmit}>Submit</Button>
          <Button variant="outlined" onClick={() => setModalOpen(false)} sx={{ ml: 2 }}>Cancel</Button>
        </Card>
      </Modal>
    </div>
  );
};

export default LeaveRequests;
