import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import LeaveRequests from './pages/LeaveRequests';
import AdminDashboard from './pages/AdminDashboard';
import ManagerDashboard from './pages/ManagerDashboard';
import Notifications from './pages/Notifications';
import AuditTrail from './pages/AuditTrail';
import { AuthProvider, useAuth } from './context/AuthContext';

function PrivateRoute({ children, roles=[] }) {
  const { user } = useAuth();
  if (!user) {
    return <Navigate to="/login" />;
  }
  if (roles.length && !roles.includes(user.role)) {
    return <Navigate to="/" />;
  }
  return children;
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
          <Route path="/leave-requests" element={<PrivateRoute><LeaveRequests /></PrivateRoute>} />
          <Route path="/notifications" element={<PrivateRoute><Notifications /></PrivateRoute>} />
          <Route path="/manager" element={<PrivateRoute roles={['MANAGER']}><ManagerDashboard /></PrivateRoute>} />
          <Route path="/admin" element={<PrivateRoute roles={['ADMIN']}><AdminDashboard /></PrivateRoute>} />
          <Route path="/audit" element={<PrivateRoute roles={['ADMIN']}><AuditTrail /></PrivateRoute>} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
