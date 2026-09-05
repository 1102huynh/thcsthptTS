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

    // Two-argument .then(onFulfilled, onRejected) on purpose - NOT
    // .then(onFulfilled).catch(onRejected). The latter also catches a
    // rejection thrown *inside* onFulfilled, and onFulfilled's own
    // api(originalRequest) retry can legitimately reject on its own terms
    // (e.g. POST /v1/users/me/change-password retried with a fresh token
    // still 401s because the current-password the user typed is simply
    // wrong - nothing to do with the token). With .catch(), that retry
    // failure was being treated as "refresh failed" and logged the user
    // out to /login instead of letting the caller's own error handling
    // (a toast) show it - reproduced live on the Profile page. The
    // two-argument form's second callback only fires for a genuine
    // requestNewToken() rejection; a rejection from the onFulfilled
    // callback's own body propagates to the original caller untouched.
    return requestNewToken().then(
      (newAccessToken) => {
        onRefreshed(newAccessToken);
        isRefreshing = false;
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      },
      (refreshError) => {
        onRefreshFailed(refreshError);
        isRefreshing = false;
        clearSessionAndRedirect();
        return Promise.reject(refreshError);
      }
    );
  }
);

export default api;
