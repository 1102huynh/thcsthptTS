import axios from 'axios';

// Bare axios instance for the public portal - deliberately NOT the shared
// `api` instance: these endpoints need no auth, and they must never trip the
// token-refresh / redirect-to-login interceptor if a stale token happens to
// be in localStorage.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const publicApi = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

export const publicPortalService = {
  home: () => publicApi.get('/v1/public/home').then((r) => r.data),

  news: ({ category, page = 0, size = 12 } = {}) =>
    publicApi
      .get('/v1/public/news', { params: { category: category || undefined, page, size } })
      .then((r) => ({ items: r.data, total: Number(r.headers['x-total-count'] ?? r.data.length) })),
  newsDetail: (slug) => publicApi.get(`/v1/public/news/${slug}`).then((r) => r.data),
  newsCategories: () => publicApi.get('/v1/public/news/categories').then((r) => r.data),

  events: ({ when, page = 0, size = 12 } = {}) =>
    publicApi
      .get('/v1/public/events', { params: { when: when || undefined, page, size } })
      .then((r) => ({ items: r.data, total: Number(r.headers['x-total-count'] ?? r.data.length) })),
  eventDetail: (slug) => publicApi.get(`/v1/public/events/${slug}`).then((r) => r.data),

  submitContact: (payload) => publicApi.post('/v1/public/contact', payload).then((r) => r.data),
};

/** Absolute URL for a media asset id (for <img src> and OG image tags). */
export function mediaUrl(idOrUrl) {
  if (!idOrUrl) return null;
  if (typeof idOrUrl === 'string' && /^https?:\/\//.test(idOrUrl)) return idOrUrl;
  const rel = typeof idOrUrl === 'string' ? idOrUrl : `/v1/public/media/${idOrUrl}`;
  return `${API_BASE_URL}${rel.startsWith('/') ? '' : '/'}${rel}`;
}

export default publicPortalService;
