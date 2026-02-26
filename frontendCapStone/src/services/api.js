// API service for backendCapStone integration
const API_BASE = 'http://localhost:8080/api';

function request(endpoint, method = 'GET', body, token) {
  return fetch(API_BASE + endpoint, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    ...(body ? { body: JSON.stringify(body) } : {})
  }).then(async res => {
    // If status is 204 (No Content), don't attempt to parse .json()
    if (res.status === 204) return {};
    let resp;
    try {
      resp = await res.json();
    } catch (e) {
      // If empty body or cannot parse JSON, return error for non-ok, or empty object for ok
      if (!res.ok) throw new Error('Unexpected server response.');
      resp = {};
    }
    if (!res.ok) throw new Error((resp && resp.message) || 'Error');
    return resp;
  });
}

const api = {
  // Auth
  login: (credentials) => request('/auth/login', 'POST', credentials),
  register: (data) => request('/auth/signup', 'POST', data),

  // Employee endpoints
  leaveBalance: (token) => request('/employee/leave-balance', 'GET', null, token),
  leaveHistory: (token) => request('/employee/leave-history', 'GET', null, token),
  createLeaveRequest: (data, token) => request('/employee/leave-request', 'POST', data, token),
  editLeaveRequest: (id, data, token) => request(`/employee/leave-request/${id}`, 'PUT', data, token),
  cancelLeaveRequest: (id, token) => request(`/employee/leave-request/${id}`, 'DELETE', null, token),

  // Manager endpoints
  pendingRequests: (token) => request('/manager/pending-requests', 'GET', null, token),
  // FIX: Send { approved, comments } for decideLeave matching backend DTO
  decideLeave: (id, approved, comments, token) => request(`/manager/decide-leave/${id}`, 'POST', { approved, comments }, token),
  teamLeaves: (token) => request('/manager/team-leaves', 'GET', null, token),
  teamHistory: (token) => request('/manager/team-history', 'GET', null, token),
};

export default api;
