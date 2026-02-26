import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  // Persist user state: read user from localStorage on mount
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
  });

  // Keep localStorage in sync with user state
  useEffect(() => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }
  }, [user]);

  // Modified login payload to use email instead of username
  const login = async (credentials) => {
    // Expect credentials: { username, password } from form
    const apiCredentials = {
      email: credentials.username, // map 'username' field to 'email' for API
      password: credentials.password
    };
    const response = await api.login(apiCredentials);
    if (response.token) {
      setUser(response);
      localStorage.setItem('user', JSON.stringify(response));
    } else {
      throw new Error('Invalid login response');
    }
    return response;
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
    window.location.href = '/login'; // Ensure logout always redirects
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
