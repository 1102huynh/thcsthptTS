import { useEffect } from 'react';

// Dependency-free <title>/<meta> manager for the public portal. Sets the
// document title plus description + Open Graph + Twitter Card tags so a
// link shared to Facebook/Zalo has a real preview (the "bắt buộc (rẻ)"
// SEO tier in KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md §7). Full prerendering
// for JS-less crawlers is a later phase (P4).
//
// Note: a pure-CSR SPA still can't feed crawlers that don't run JS - this
// covers browsers, link unfurlers that DO execute JS, and human-visible
// tab titles. Reverts to the site default on unmount.

const SITE_NAME = 'Trường THCS & THPT';
const DEFAULT_DESCRIPTION = 'Tin tức, sự kiện và thông tin tuyển sinh của nhà trường.';

function setMeta(attr, key, content) {
  if (content == null) return null;
  let el = document.head.querySelector(`meta[${attr}="${key}"]`);
  const created = !el;
  if (!el) {
    el = document.createElement('meta');
    el.setAttribute(attr, key);
    document.head.appendChild(el);
  }
  const prev = el.getAttribute('content');
  el.setAttribute('content', content);
  return { el, created, prev };
}

export default function Seo({ title, description, image, type = 'website' }) {
  useEffect(() => {
    const fullTitle = title ? `${title} — ${SITE_NAME}` : SITE_NAME;
    const desc = description || DEFAULT_DESCRIPTION;
    const url = typeof window !== 'undefined' ? window.location.href : '';

    const prevTitle = document.title;
    document.title = fullTitle;

    const changes = [
      setMeta('name', 'description', desc),
      setMeta('property', 'og:title', fullTitle),
      setMeta('property', 'og:description', desc),
      setMeta('property', 'og:type', type),
      setMeta('property', 'og:url', url),
      setMeta('property', 'og:site_name', SITE_NAME),
      setMeta('property', 'og:image', image || null),
      setMeta('name', 'twitter:card', image ? 'summary_large_image' : 'summary'),
      setMeta('name', 'twitter:title', fullTitle),
      setMeta('name', 'twitter:description', desc),
      setMeta('name', 'twitter:image', image || null),
    ].filter(Boolean);

    return () => {
      document.title = prevTitle;
      for (const { el, created, prev } of changes) {
        if (created) el.remove();
        else if (prev != null) el.setAttribute('content', prev);
      }
    };
  }, [title, description, image, type]);

  return null;
}
