import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios from 'axios';

// Regression test for a bug found live while manually clicking through the
// app (Profile page > "Đổi mật khẩu" with a wrong current password): the
// response interceptor's `requestNewToken().then(success).catch(failure)`
// chain also caught a rejection thrown *inside* the success callback (the
// retried original request 401-ing again for its own, token-unrelated
// reason - a wrong current password has nothing to do with the access
// token) and treated it as "refresh failed", logging the user out and
// hard-redirecting to /login instead of letting the caller's own error
// handling show a toast. Fixed by switching to the two-argument
// `.then(onFulfilled, onRejected)` form, which does NOT catch a rejection
// from onFulfilled's own body - see api.js's comment at the fix site.
//
// axios is mocked because this needs to control what `axios.create()`
// returns (api.js calls `api(originalRequest)` on it directly, as axios
// instances are callable) and what the bare `axios.post` used by
// requestNewToken() resolves/rejects with, without a real HTTP layer.
const interceptors = { request: [], response: [] };
let apiInstance;

vi.mock('axios', () => {
  const create = vi.fn(() => {
    const instance = vi.fn();
    instance.interceptors = {
      request: { use: (onFulfilled, onRejected) => interceptors.request.push({ onFulfilled, onRejected }) },
      response: { use: (onFulfilled, onRejected) => interceptors.response.push({ onFulfilled, onRejected }) },
    };
    apiInstance = instance;
    return instance;
  });
  return { default: { create, post: vi.fn() } };
});

function makeUnauthorizedError({ url = '/v1/users/me/change-password', retried = false } = {}) {
  return {
    config: { url, headers: { Authorization: 'Bearer old-access-token' }, _retry: retried },
    response: { status: 401 },
  };
}

describe('api.js response interceptor - refresh-then-retry', () => {
  // api.js reads/writes window.location.href directly - jsdom logs a
  // harmless "Not implemented: navigation" warning on that assignment but
  // doesn't throw, so window.location is stubbed here to observe the
  // target instead of suppress an error. Restored in afterEach: an
  // unrestored `window.location` override leaks across test files that
  // share a worker/jsdom instance (confirmed live - it broke an unrelated
  // SelfServicePortal.test.jsx run until this restore was added).
  const originalLocationDescriptor = Object.getOwnPropertyDescriptor(window, 'location');

  beforeEach(async () => {
    vi.resetModules();
    interceptors.request.length = 0;
    interceptors.response.length = 0;
    axios.post.mockReset();
    localStorage.clear();
    localStorage.setItem('accessToken', 'old-access-token');
    localStorage.setItem('refreshToken', 'valid-refresh-token');
    delete window.__navigatedTo;
    Object.defineProperty(window, 'location', {
      value: { ...window.location, pathname: '/profile', set href(v) { window.__navigatedTo = v; } },
      writable: true,
      configurable: true,
    });
    await import('./api');
  });

  afterEach(() => {
    Object.defineProperty(window, 'location', originalLocationDescriptor);
  });

  it('does NOT clear the session or redirect when the refreshed retry itself 401s again (e.g. wrong current password)', async () => {
    axios.post.mockResolvedValue({ data: { accessToken: 'new-access-token', refreshToken: 'new-refresh-token' } });
    // The retried request (api(originalRequest)) rejects on its own terms -
    // this is what a still-wrong current password looks like after retry.
    const retryError = makeUnauthorizedError({ retried: true });
    apiInstance.mockRejectedValue(retryError);

    const [responseInterceptor] = interceptors.response;
    await expect(responseInterceptor.onRejected(makeUnauthorizedError())).rejects.toBe(retryError);

    expect(window.__navigatedTo).toBeUndefined();
    expect(localStorage.getItem('accessToken')).toBe('new-access-token'); // refresh itself did succeed and rotate the token
  });

  it('DOES clear the session and redirect when the refresh call itself fails', async () => {
    axios.post.mockRejectedValue(new Error('refresh token expired'));

    const [responseInterceptor] = interceptors.response;
    await expect(responseInterceptor.onRejected(makeUnauthorizedError())).rejects.toThrow('refresh token expired');

    expect(window.__navigatedTo).toBe('/login');
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('refreshToken')).toBeNull();
  });
});
