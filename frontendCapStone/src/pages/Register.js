import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { Card, TextField, Button, Typography, Alert, MenuItem } from '@mui/material';

const Register = () => {
  const [form, setForm] = useState({ username: '', password: '', email: '', role: 'EMPLOYEE' });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    try {
      // PATCH: Send payload as expected by backend
      const payload = {
        name: form.username,
        email: form.email,
        password: form.password,
        roles: [form.role]
      };
      await api.register(payload);
      navigate('/login');
    } catch (err) {
      setError('Registration failed');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', marginTop: 70 }}>
      <Card sx={{ width: 340, p: 3 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>Register</Typography>
        <form onSubmit={handleSubmit}>
          <TextField name="username" label="Username" fullWidth value={form.username} onChange={handleChange} sx={{ mb: 2 }} />
          <TextField name="email" label="Email" fullWidth value={form.email} onChange={handleChange} sx={{ mb: 2 }} />
          <TextField name="password" label="Password" type="password" fullWidth value={form.password} onChange={handleChange} sx={{ mb: 2 }} />
          <TextField select name="role" label="Role" fullWidth value={form.role} onChange={handleChange} sx={{ mb: 2 }}>
            <MenuItem value="EMPLOYEE">Employee</MenuItem>
            <MenuItem value="MANAGER">Manager</MenuItem>
          </TextField>
          <Button type="submit" variant="contained" color="primary" fullWidth>Register</Button>
          {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
        </form>
      </Card>
    </div>
  );
};
export default Register;
