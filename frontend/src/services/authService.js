import api from './api';
import jwtDecode from 'jwt-decode';

const AUTH_ENDPOINT = '/v1/auth';

export const authService = {
  // Login user
  login: async (username, password) => {
    try {
      const loginUrl = `${AUTH_ENDPOINT}/login`;
      console.log('API Base URL:', api.defaults.baseURL);
      console.log('Login endpoint:', loginUrl);
      console.log('Full URL:', api.defaults.baseURL + loginUrl);

      const response = await api.post(loginUrl, {
        username,
        password,
      });

      console.log('Login response:', response.data);

      if (response.data.accessToken) {
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        localStorage.setItem('user', JSON.stringify(response.data));
      }

      return response.data;
    } catch (error) {
      console.error('Login API error:', error);
      console.error('Error response:', error.response);
      throw error.response?.data || error.message;
    }
  },

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

  // Check user permission
  hasPermission: (permission) => {
    const user = authService.getCurrentUser();
    return user?.permissions?.includes(permission) || false;
  },
};

export const getCurrentUser = () => authService.getCurrentUser();
export const isAuthenticated = () => authService.isAuthenticated();

export default authService;

