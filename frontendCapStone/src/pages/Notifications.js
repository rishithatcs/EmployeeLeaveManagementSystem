import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const Notifications = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    api.getNotifications(user?.token).then(setNotifications);
  }, [user]);

  return (
    <div>
      <h2>Notifications</h2>
      <ul>
        {notifications.map(n => (
          <li key={n.id}>{n.message} - {n.date}</li>
        ))}
      </ul>
    </div>
  );
};
export default Notifications;
