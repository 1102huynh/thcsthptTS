import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only treat a 401 as "session expired, force back to login" when the
    // failed request actually carried a token (an authenticated call got
    // rejected mid-session). A 401 from an anonymous request - most
    // notably POST /v1/auth/login itself on a wrong password - just means
    // "invalid credentials", not an expired session; redirecting on that
    // too (the previous behavior) hard-reloaded the page on every failed
    // login attempt, wiping the form before the caller's own error
    // handling (LoginPage's toast) ever got a chance to render. Found live
    // via Playwright, not by inspection - the toast simply never appeared.
    const hadToken = Boolean(error.config?.headers?.Authorization);
    if (error.response?.status === 401 && hadToken) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('user');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default api;

