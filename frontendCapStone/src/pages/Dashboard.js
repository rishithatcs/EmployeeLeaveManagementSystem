import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';
import { Card, Typography, Button, Grid, Alert, TextField } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [balance, setBalance] = useState(null);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState('');
  const [pending, setPending] = useState([]); // Pending requests (for MANAGER)
  const [success, setSuccess] = useState('');
  const [commentsMap, setCommentsMap] = useState({});
  const navigate = useNavigate();

  // Styled root wrapper for central layout and colors
  const wrapperStyle = {
    minHeight: '100vh',
    background: 'linear-gradient(135deg, #f8fafc 65%, #e3f0ff 100%)',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'flex-start',
    padding: '40px 0 60px 0'
  };

  // Fetch balance/history/pending per role
  useEffect(() => {
    setError('');
    setSuccess('');
    if (user?.token) {
      const role = user.role ? user.role.toUpperCase() : '';
      if (role === "EMPLOYEE") {
        api.leaveBalance(user.token).then(setBalance).catch(e => setError(e.message));
        api.leaveHistory(user.token).then(setHistory).catch(e => setError(e.message));
      } else if (role === "MANAGER") {
        api.teamLeaves(user.token).then(setBalance).catch(e => setError(e.message));
        api.teamHistory(user.token).then(setHistory).catch(e => setError(e.message));
        // Also fetch pending requests for managers
        api.pendingRequests(user.token).then(setPending).catch(() => setPending([]));
      } else {
        setError('Unsupported role.');
      }
    }
  }, [user]);

  // Approve/Reject Handlers - for manager only
  const handleDecision = (id, status) => {
    setError(''); setSuccess('');
    const approved = status === 'APPROVED';
    const comments = commentsMap[id] || (approved ? 'Approved by manager' : 'Rejected by manager');
    api.decideLeave(id, approved, comments, user.token)
      .then(() => {
        setSuccess(`Leave ${status.toLowerCase()}ed.`);
        // Refresh pending requests
        setCommentsMap({ ...commentsMap, [id]: '' });
        api.pendingRequests(user.token).then(setPending).catch(() => setPending([]));
      })
      .catch(e => setError(e.message));
  };

  // Helper to safely render leaveType
  const renderLeaveType = lt => {
    if (lt && typeof lt === 'object') {
      return lt.name || lt.type || JSON.stringify(lt);
    }
    return lt;
  };

  return (
    <div style={wrapperStyle}>
      <Card sx={{ px: 5, py: 3, mb: 3, minWidth: '40%', boxShadow: 3,
        background: '#fff', borderRadius: '18px', display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
        <Typography variant="h4" sx={{ mb: 1, color: '#276678', fontWeight: 600}}>
          Welcome, <span style={{color:'#347474'}}>{user?.username}</span></Typography>
        <Typography variant="subtitle2" sx={{ mb: 2, color: '#496376' }}>Role: {user?.role}</Typography>
        <Button
          variant="outlined"
          color="secondary"
          onClick={() => {
            logout();
            navigate('/login');
          }}
          style={{ marginBottom: '20px', fontWeight: 500, borderColor:'#b7bec7', color:'#4b515d' }}
        >Logout</Button>
        {error && <Alert severity="error" sx={{ width: '100%', mb: 1, bgcolor: '#ffe5e5', color:'#b33131', fontWeight: 500}}>{error}</Alert>}
        {success && <Alert severity="success" sx={{ width: '100%', mb: 1 }}>{success}</Alert>}
        <Grid container spacing={2} justifyContent="center" alignItems="flex-start">
          <Grid item xs={12} md={6}>
            <Card sx={{ p: 2, mb: 2, boxShadow:1, background:'#f6fbff', borderRadius: '12px' }}>
              <Typography variant="h6" sx={{ color: '#205473', mb: 1 }}>Leave Balance</Typography>
              {balance ? (
                Array.isArray(balance) ? (
                  <ul>
                    {balance.map(bal => (
                      <li key={bal.id}><b>{renderLeaveType(bal.leaveType)}</b>: {bal.balance}</li>
                    ))}
                  </ul>
                ) : (
                  <ul>
                    {Object.entries(balance || {}).map(([type, count]) => (
                      <li key={type}><b>{type}</b>: {count}</li>
                    ))}
                  </ul>
                )
              ) : (
                <Typography>Loading...</Typography>
              )}
            </Card>
            {/* Only show APPLY FOR LEAVE for EMPLOYEE */}
            {user?.role === 'EMPLOYEE' && (
              <Button variant="contained" color="primary" sx={{ bgcolor:'#57acef'}} onClick={() => navigate('/leave-requests')}>Apply for Leave</Button>
            )}
          </Grid>
          <Grid item xs={12} md={6}>
            <Card sx={{ p: 2, background: '#f6fbff', borderRadius: '12px' }}>
              <Typography variant="h6" sx={{ color: '#205473', mb: 1 }}>Leave History</Typography>
              {history && history.length > 0 ? (
                <ul>
                  {history.map(lh => (
                    <li key={lh.id} style={{ marginBottom: 8 }}>
                      <b>{renderLeaveType(lh.leaveType)}</b> | <b>{lh.status}</b> | {lh.fromDate || lh.startDate} to {lh.toDate || lh.endDate}
                    </li>
                  ))}
                </ul>
              ) : (
                <Typography>No history found.</Typography>
              )}
            </Card>
          </Grid>
        </Grid>
        {/* MANAGER: Pending Requests section */}
        {user?.role === 'MANAGER' && (
          <div style={{ width: '100%', marginTop: 32 }}>
            <Typography variant="h5" sx={{ mb: 2, color:'#2980b9', textAlign:'center'}}>Pending Leave Requests</Typography>
            <Grid container spacing={2} justifyContent="center">
              {pending.map(req => (
                <Grid item xs={12} md={10} key={req.id}>
                  <Card sx={{ p: 2, mb: 1, boxShadow:2, display:'flex', flexDirection:'column', background:'#effaff', borderRadius:'14px', alignItems:'stretch' }}>
                    <Typography sx={{color:'#345678'}}><b>{req.employeeName || req.username}:</b> {req.leaveType} ({req.startDate} → {req.endDate})</Typography>
                    <Typography variant="body2" sx={{mb:0.5}}>Reason: {req.notes}</Typography>
                    <TextField
                      size="small"
                      sx={{ mt: 1, mb:1, width: '80%' }}
                      label="Manager Comments"
                      value={commentsMap[req.id] || ''}
                      onChange={e => setCommentsMap({...commentsMap, [req.id]: e.target.value})}
                    />
                    <div style={{ display: 'flex', gap: 8 }}>
                      <Button
                        sx={{ mt: 1 }}
                        variant="contained"
                        style={{backgroundColor:'#47b47d', minWidth:84}}
                        onClick={() => handleDecision(req.id, 'APPROVED')}
                      >Approve</Button>
                      <Button
                        sx={{ mt: 1 }}
                        variant="contained"
                        style={{backgroundColor:'#dd5256', minWidth:84}}
                        onClick={() => handleDecision(req.id, 'REJECTED')}
                      >Reject</Button>
                    </div>
                  </Card>
                </Grid>
              ))}
              {!pending.length && <Typography sx={{ px: 2 }}>No pending requests.</Typography>}
            </Grid>
          </div>
        )}
      </Card>
    </div>
  );
};
export default Dashboard;
