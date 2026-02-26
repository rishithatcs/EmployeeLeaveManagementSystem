import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Card, Button, Typography, Grid, Alert, TextField } from '@mui/material';

const ManagerDashboard = () => {
  const { user } = useAuth();
  const [pending, setPending] = useState([]);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [commentsMap, setCommentsMap] = useState({});

  const fetchRequests = () => {
    api.pendingRequests(user.token).then(setPending).catch(() => setPending([]));
  };
  useEffect(() => { fetchRequests(); }, [user]);

  const handleDecision = (id, status) => {
    setError(''); setSuccess('');
    const comments = commentsMap[id] || '';
    // DEBUG: Log token and user info so we know what is sent to backend
    console.log('[ManagerDashboard] DECIDE', {id, status, comments});
    console.log('[ManagerDashboard] USER:', user);
    console.log('[ManagerDashboard] TOKEN:', user.token);
    api.decideLeave(id, status === 'APPROVED', comments, user.token)
      .then(() => {
        setSuccess(`Leave ${status.toLowerCase()}ed.`);
        setCommentsMap({...commentsMap, [id]: ''});
        fetchRequests();
      })
      .catch(e => setError(e.message));
  };

  return (
    <div>
      <Typography variant="h5" sx={{ mb: 2 }}>Pending Leave Requests</Typography>
      {success && <Alert severity="success">{success}</Alert>}
      {error && <Alert severity="error">{error}</Alert>}
      <Grid container spacing={2}>
        {pending.map(req => (
          <Grid item xs={12} md={6} key={req.id}>
            <Card sx={{ p: 2, mb: 1 }}>
              <Typography><b>{req.employeeName || req.username}:</b> {req.leaveType} ({req.startDate} → {req.endDate})</Typography>
              <Typography variant="body2">Reason: {req.notes}</Typography>
              <TextField
                size="small"
                sx={{ mt: 1, mr: 1, width: '80%' }}
                label="Manager Comments"
                value={commentsMap[req.id] || ''}
                onChange={e => setCommentsMap({...commentsMap, [req.id]: e.target.value})}
              />
              <Button
                sx={{ mt: 1, mr: 1 }}
                variant="contained"
                color="success"
                onClick={() => handleDecision(req.id, 'APPROVED')}
              >Approve</Button>
              <Button
                sx={{ mt: 1 }}
                variant="contained"
                color="error"
                onClick={() => handleDecision(req.id, 'REJECTED')}
              >Reject</Button>
            </Card>
          </Grid>
        ))}
        {!pending.length && <Typography sx={{ px: 2 }}>No pending requests.</Typography>}
      </Grid>
    </div>
  );
};

export default ManagerDashboard;
