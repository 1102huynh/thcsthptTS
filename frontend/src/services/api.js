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

// --- Automatic access-token refresh (A4 in KE_HOACH_NANG_CAP_V4.md) --------
//
// Before this, a 401 on any authenticated request just wiped the token and
// hard-redirected to the login page, even though the backend has issued a
// refresh token all along (stored in localStorage but never used). Now the
// first 401 triggers a single POST /v1/auth/refresh-token; every other
// request that 401s while that call is in flight waits for it instead of
// firing its own, then all of them retry once with the new access token.
// Only if the refresh itself fails do we fall back to the old
// clear-and-redirect behavior.

function clearSessionAndRedirect() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  // Session expired mid-use -> back to the login page. ("/" is the public
  // news portal now, not the login form - see App.jsx routing.) Guard
  // against a redirect loop when we're already there.
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

let isRefreshing = false;
// Requests that hit a 401 while a refresh is in flight park their resolve/
// reject here; drained by onRefreshed / onRefreshFailed once it settles.
let pendingQueue = [];

function onRefreshed(newAccessToken) {
  pendingQueue.forEach(({ resolve }) => resolve(newAccessToken));
  pendingQueue = [];
}

function onRefreshFailed(error) {
  pendingQueue.forEach(({ reject }) => reject(error));
  pendingQueue = [];
}

async function requestNewToken() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) {
    throw new Error('No refresh token');
  }
  // A bare axios call, not the `api` instance - it must not pick up the
  // (expired) access token from the request interceptor, and a 401 on it
  // must not re-enter this response interceptor.
  const { data } = await axios.post(
    `${API_BASE_URL}/v1/auth/refresh-token`,
    {},
    { headers: { Authorization: `Bearer ${refreshToken}` } }
  );
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  // The refresh response is a full AuthResponse (same shape as login), so
  // keep the cached user record in step with it.
  localStorage.setItem('user', JSON.stringify(data));
  return data.accessToken;
}

// Handle responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    // Only treat a 401 as "session expired" when the failed request actually
    // carried a token (an authenticated call rejected mid-session). A 401
    // from an anonymous request - most notably POST /v1/auth/login on a
    // wrong password - just means "invalid credentials"; redirecting on that
    // too hard-reloaded the page and wiped the login form before LoginPage's
    // own toast could render (found live via Playwright).
    const hadToken = Boolean(originalRequest?.headers?.Authorization);
    const isAuthCall = originalRequest?.url?.includes('/v1/auth/');

    if (status !== 401 || !hadToken || isAuthCall || originalRequest?._retry) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    if (isRefreshing) {
      // Park until the in-flight refresh settles, then retry this request.
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject });
      })
        .then((newAccessToken) => {
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return api(originalRequest);
        })
        .catch((queueError) => Promise.reject(queueError));
    }

    isRefreshing = true;

    return requestNewToken()
      .then((newAccessToken) => {
        onRefreshed(newAccessToken);
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      })
      .catch((refreshError) => {
        onRefreshFailed(refreshError);
        clearSessionAndRedirect();
        return Promise.reject(refreshError);
      })
      .finally(() => {
        isRefreshing = false;
      });
  }
);

export default api;
