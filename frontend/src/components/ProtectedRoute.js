import React from 'react';
import { Navigate } from 'react-router-dom';
import { getCurrentUser } from '../services/authService';

const ProtectedRoute = ({ children, allowedRoles = null }) => {
  const user = getCurrentUser();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return (
      <div className="d-flex align-items-center justify-content-center vh-100">
        <div className="text-center">
          <h2>Access Denied</h2>
          <p>You don't have permission to access this page.</p>
          <a href="/" className="btn btn-primary">Go Home</a>
        </div>
      </div>
    );
  }

  return children;
};

export default ProtectedRoute;

