import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Card, TextField, Button, Typography, Alert } from '@mui/material';

const Login = () => {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    try {
      await login(form);
      navigate('/');
    } catch (err) {
      setError('Invalid credentials');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', marginTop: 70 }}>
      <Card sx={{ width: 340, p: 3 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>Login</Typography>
        <form onSubmit={handleSubmit}>
          {/* Updated label from Username to Email for clarity */}
          <TextField name="username" label="Email" fullWidth value={form.username} onChange={handleChange} sx={{ mb: 2 }} />
          <TextField name="password" label="Password" type="password" fullWidth value={form.password} onChange={handleChange} sx={{ mb: 2 }} />
          <Button type="submit" variant="contained" color="primary" fullWidth>Login</Button>
          {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
        </form>
        <Typography variant="body2" align="center" sx={{ mt: 2 }}>
          Don't have an account? <Link to="/register">Register here</Link>
        </Typography>
      </Card>
    </div>
  );
};
export default Login;
