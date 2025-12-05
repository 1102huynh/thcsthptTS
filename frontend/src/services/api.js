import axios from 'axios';

// Backend no longer uses context-path, so base URL is just localhost:8080
const API_BASE_URL = 'http://localhost:8080';

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

    console.log('API Request:', {
      method: config.method,
      url: config.url,
      hasToken: !!token,
      tokenLength: token ? token.length : 0,
      tokenPrefix: token ? token.substring(0, 20) + '...' : 'NO TOKEN',
    });

    if (token) {
      // Ensure token has Bearer prefix
      const bearerToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      config.headers.Authorization = bearerToken;
      console.log('Authorization header set:', bearerToken.substring(0, 30) + '...');
    } else {
      console.warn('NO TOKEN FOUND IN LOCALSTORAGE - Request will be unauthorized!');
      console.log('Available localStorage keys:', Object.keys(localStorage));
    }

    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Handle responses
api.interceptors.response.use(
  (response) => {
    console.log('API Response Success:', {
      status: response.status,
      statusText: response.statusText,
      url: response.config.url,
      dataLength: JSON.stringify(response.data).length,
    });
    return response;
  },
  (error) => {
    // Log full error for debugging
    console.error('API Error Response:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      method: error.config?.method,
      authHeader: error.config?.headers?.Authorization ? 'Present' : 'MISSING',
      data: error.response?.data,
      message: error.message,
      errorString: String(error),
    });

    // If 401 and no auth header was sent, the problem is token not being sent
    if (error.response?.status === 401) {
      const hadAuthHeader = error.config?.headers?.Authorization ? 'Yes' : 'No';
      console.error(`⚠️  401 Unauthorized - Auth header was ${hadAuthHeader} sent`);
      console.error('Authorization header value:', error.config?.headers?.Authorization || 'NONE');

      // Check if this is a public endpoint that shouldn't require auth
      const publicEndpoints = ['/news', '/news/category', '/news/featured', '/news/recent', '/news/search'];
      const isPublicEndpoint = publicEndpoints.some(endpoint => error.config?.url?.includes(endpoint));

      if (isPublicEndpoint) {
        console.error('⚠️  Public news endpoint returned 401 - Backend security config may be incorrect');
        console.error('This endpoint should be public but backend is requiring authentication');
        return Promise.reject(new Error('News endpoint requires authentication (backend config issue)'));
      }

      // Only redirect if this is not a public endpoint
      console.error('Clearing credentials and redirecting to login');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/';
      return Promise.reject(new Error('Session expired. Please log in again.'));
    }

    // Handle 403 Forbidden
    if (error.response?.status === 403) {
      return Promise.reject(new Error('You do not have permission to access this resource.'));
    }

    // Handle HTML error responses (error page)
    if (error.response?.data && typeof error.response.data === 'string' && error.response.data.includes('<html')) {
      console.error('Received HTML error page instead of JSON');
      const statusCode = error.response?.status || 'Unknown';
      return Promise.reject(new Error(`Server error (${statusCode}). Please try again.`));
    }

    // Ensure error has a meaningful message
    if (error.response?.data?.message) {
      error.message = error.response.data.message;
    } else if (error.response?.data?.error) {
      error.message = error.response.data.error;
    } else if (error.response?.statusText) {
      error.message = `Error ${error.response.status}: ${error.response.statusText}`;
    } else if (error.message === 'Network Error' || !error.message) {
      error.message = 'Unable to connect to server. Please check your connection and try again.';
    }

    return Promise.reject(error);
  }
);

export default api;

