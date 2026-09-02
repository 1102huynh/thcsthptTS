import api from './api';
import jwtDecode from 'jwt-decode';

const AUTH_ENDPOINT = '/v1/auth';

export const authService = {
  // Login user. Deliberately logs nothing: the response body carries the
  // access token + refresh token + user profile, and the old
  // console.log('Login response', ...) / console.error('Error response', ...)
  // calls dumped all of that into the browser console (A2 in
  // KE_HOACH_NANG_CAP_V4.md - the clearest remaining frontend security gap).
  login: async (username, password) => {
    try {
      const response = await api.post(`${AUTH_ENDPOINT}/login`, {
        username,
        password,
      });

      if (response.data.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        localStorage.setItem('user', JSON.stringify(response.data));
      }

      return response.data;
    } catch (error) {
      // Re-throw the server's error payload (GlobalExceptionHandler returns a
      // Vietnamese message) for LoginPage's toast; never log the error object,
      // which includes the failed request's config and headers.
      throw error.response?.data || error.message;
    }
  },

  // Request a password reset email - public, no auth required. Always
  // resolves the same way regardless of whether the email is registered
  // (per ForgotPasswordRequest's own doc comment, to avoid leaking which
  // emails have accounts), so there's no "email not found" branch to
  // handle here.
  forgotPassword: (email) => api.post(`${AUTH_ENDPOINT}/forgot-password`, { email }),

  // Reset a password using the token from the forgot-password email link -
  // public, no auth required. Token is single-use, expires 15 minutes
  // after being issued (see PasswordResetService).
  resetPassword: (token, newPassword) => api.post(`${AUTH_ENDPOINT}/reset-password`, { token, newPassword }),

  // Logout user
  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },

  // Get current user from localStorage
  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;

    try {
      const decoded = jwtDecode(token);
      return decoded.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  },

  // Get current user role
  getUserRole: () => {
    const user = authService.getCurrentUser();
    return user?.role || null;
  },

  // NOTE (A8): the old hasPermission(permission) helper was removed. It read
  // user.permissions, but AuthResponse has never carried a `permissions`
  // field (the backend gates everything by Role, and enum Permission /
  // UserPermission are defined but unused - see KE_HOACH_NANG_CAP_V4.md
  // H.3.2), so it was dead code that always returned false. UI authorization
  // goes through getUserRole() + config/navigation.js instead.
};

export const getCurrentUser = () => authService.getCurrentUser();
export const isAuthenticated = () => authService.isAuthenticated();

export default authService;

