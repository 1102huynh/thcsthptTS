#!/usr/bin/env node
// Post-build step for the public portal's SEO (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md
// §7/P4). react-helmet-async-style <Seo> (components/public/Seo.jsx) only
// works for browsers/crawlers that execute JS - Facebook's and Zalo's link
// unfurlers do NOT run JS at all, so a pure client-rendered SPA never gives
// them a real preview. This script runs after `vite build` and, for every
// public route (static pages + every published news article / event),
// writes a `build/<route>/index.html` that:
//   - is a byte-for-byte copy of the real built shell (same hashed JS/CSS
//     asset tags) so a real browser boots the exact same SPA and React
//     mounts over the snapshot exactly like any other route, and
//   - has the correct <title>/<meta description>/Open Graph/Twitter Card
//     tags plus a plain-HTML content snapshot inside #root, so a JS-less
//     crawler sees a real preview instead of an empty shell.
// Also regenerates build/sitemap.xml with one absolute <url> per published
// article/event (the static frontend/public/sitemap.xml only lists routes).
//
// LIMITATION: this snapshots whatever is PUBLISHED at build time. A newly
// published article only gets its own snapshot after the next `npm run
// build` - until then it's still fully reachable and correctly rendered by
// JS-executing crawlers (Googlebot) via the normal SPA, it just won't have
// an unfurl preview on Facebook/Zalo yet. Re-run `npm run build` (or wire a
// scheduled rebuild) after publishing if that matters immediately.
//
// Never fails the build: if the backend isn't reachable (e.g. a plain local
// `npm run build` with no API running), this logs a warning and the static
// SPA build stands as-is, exactly as before this script existed.

import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const BUILD_DIR = path.join(ROOT, 'build');
const PAGE_SIZE = 50; // matches PublicPortalController.MAX_PAGE_SIZE

const API_BASE_URL = (process.env.PRERENDER_API_BASE_URL || process.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '');
const SITE_URL = (process.env.PRERENDER_SITE_URL || process.env.FRONTEND_SITE_URL || 'http://localhost:3000').replace(/\/$/, '');

// Mirrors components/public/Seo.jsx's constants - keep in sync.
const SITE_NAME = 'Trường THCS & THPT';
const DEFAULT_DESCRIPTION = 'Tin tức, sự kiện và thông tin tuyển sinh của nhà trường.';

// Mirrors services/publicService.js's mediaUrl().
function mediaUrl(idOrUrl) {
  if (!idOrUrl) return null;
  if (typeof idOrUrl === 'string' && /^https?:\/\//.test(idOrUrl)) return idOrUrl;
  const rel = typeof idOrUrl === 'string' ? idOrUrl : `/v1/public/media/${idOrUrl}`;
  return `${API_BASE_URL}${rel.startsWith('/') ? '' : '/'}${rel}`;
}

async function fetchAllPaginated(pathname) {
  const items = [];
  let page = 0;
  for (;;) {
    const res = await fetch(`${API_BASE_URL}${pathname}${pathname.includes('?') ? '&' : '?'}page=${page}&size=${PAGE_SIZE}`);
    if (!res.ok) throw new Error(`${pathname} -> HTTP ${res.status}`);
    const batch = await res.json();
    items.push(...batch);
    const total = Number(res.headers.get('x-total-count') ?? items.length);
    page += 1;
    if (items.length >= total || batch.length === 0) break;
  }
  return items;
}

function escapeHtml(s) {
  return String(s ?? '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function escapeXml(s) {
  return escapeHtml(s);
}

function renderPage(template, { routePath, title, description, image, type = 'website', bodyHtml = '' }) {
  const fullTitle = title ? `${title} — ${SITE_NAME}` : SITE_NAME;
  const desc = description || DEFAULT_DESCRIPTION;
  const url = `${SITE_URL}${routePath}`;

  const metaTags = [
    `<meta name="description" content="${escapeHtml(desc)}" />`,
    `<meta property="og:title" content="${escapeHtml(fullTitle)}" />`,
    `<meta property="og:description" content="${escapeHtml(desc)}" />`,
    `<meta property="og:type" content="${escapeHtml(type)}" />`,
    `<meta property="og:url" content="${escapeHtml(url)}" />`,
    `<meta property="og:site_name" content="${escapeHtml(SITE_NAME)}" />`,
    image ? `<meta property="og:image" content="${escapeHtml(image)}" />` : '',
    `<meta name="twitter:card" content="${image ? 'summary_large_image' : 'summary'}" />`,
    `<meta name="twitter:title" content="${escapeHtml(fullTitle)}" />`,
    `<meta name="twitter:description" content="${escapeHtml(desc)}" />`,
    image ? `<meta name="twitter:image" content="${escapeHtml(image)}" />` : '',
    `<link rel="canonical" href="${escapeHtml(url)}" />`,
  ].filter(Boolean).join('\n    ');

  return template
    .replace(/<title>.*?<\/title>/s, `<title>${escapeHtml(fullTitle)}</title>`)
    .replace(/\s*<meta name="description"[^>]*\/?>/, '')
    .replace('</head>', `    ${metaTags}\n  </head>`)
    .replace('<div id="root"></div>', `<div id="root">${bodyHtml}</div>`);
}

const SNAPSHOT_STYLE = 'max-width:720px;margin:0 auto;padding:24px 16px;font-family:system-ui,sans-serif;line-height:1.6;';

function articleSnapshot({ title, summary, content, coverImageUrl, categoryName, publishedAt }) {
  const cover = mediaUrl(coverImageUrl);
  return `<article style="${SNAPSHOT_STYLE}">
      <h1>${escapeHtml(title)}</h1>
      ${categoryName ? `<p><strong>${escapeHtml(categoryName)}</strong></p>` : ''}
      ${publishedAt ? `<p>${escapeHtml(new Date(publishedAt).toLocaleDateString('vi-VN'))}</p>` : ''}
      ${cover ? `<img src="${escapeHtml(cover)}" alt="${escapeHtml(title)}" style="max-width:100%;height:auto;" />` : ''}
      ${summary ? `<p>${escapeHtml(summary)}</p>` : ''}
      <div>${content || ''}</div>
    </article>`;
}

function eventSnapshot({ title, description, coverImageUrl, location, startAt }) {
  const cover = mediaUrl(coverImageUrl);
  return `<article style="${SNAPSHOT_STYLE}">
      <h1>${escapeHtml(title)}</h1>
      ${startAt ? `<p>${escapeHtml(new Date(startAt).toLocaleString('vi-VN'))}</p>` : ''}
      ${location ? `<p>${escapeHtml(location)}</p>` : ''}
      ${cover ? `<img src="${escapeHtml(cover)}" alt="${escapeHtml(title)}" style="max-width:100%;height:auto;" />` : ''}
      <div>${description || ''}</div>
    </article>`;
}

async function writeRoute(template, routePath, opts) {
  const html = renderPage(template, { routePath, ...opts });
  const dir = routePath === '/' ? BUILD_DIR : path.join(BUILD_DIR, ...routePath.replace(/^\//, '').split('/'));
  await fs.mkdir(dir, { recursive: true });
  await fs.writeFile(path.join(dir, 'index.html'), html, 'utf8');
  console.log(`  prerendered ${routePath}`);
}

function sitemapXml(newsItems, eventItems) {
  const staticPaths = ['/', '/tin-tuc', '/su-kien', '/tuyen-sinh', '/gioi-thieu', '/lien-he'];
  const urls = [
    ...staticPaths.map((p) => ({ loc: `${SITE_URL}${p}` })),
    ...newsItems.map((a) => ({ loc: `${SITE_URL}/tin-tuc/${a.slug}`, lastmod: a.publishedAt })),
    ...eventItems.map((e) => ({ loc: `${SITE_URL}/su-kien/${e.slug}`, lastmod: e.publishedAt })),
  ];
  const body = urls.map(({ loc, lastmod }) =>
    `  <url>\n    <loc>${escapeXml(loc)}</loc>\n${lastmod ? `    <lastmod>${String(lastmod).slice(0, 10)}</lastmod>\n` : ''}  </url>`
  ).join('\n');
  return `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${body}\n</urlset>\n`;
}

async function main() {
  const templatePath = path.join(BUILD_DIR, 'index.html');
  let template;
  try {
    template = await fs.readFile(templatePath, 'utf8');
  } catch {
    console.warn('[prerender] build/index.html not found - run `vite build` first. Skipping.');
    return;
  }

  console.log(`[prerender] API: ${API_BASE_URL}  Site: ${SITE_URL}`);

  try {
    const staticRoutes = [
      { path: '/tin-tuc', title: 'Tin tức', description: 'Tin tức và thông báo mới nhất của nhà trường.' },
      { path: '/su-kien', title: 'Sự kiện', description: 'Lịch sự kiện, hoạt động của nhà trường.' },
      { path: '/tuyen-sinh', title: 'Tuyển sinh', description: 'Thông tin tuyển sinh đầu cấp và nộp hồ sơ trực tuyến.' },
      { path: '/gioi-thieu', title: 'Giới thiệu', description: 'Giới thiệu về Trường THCS & THPT.' },
      { path: '/lien-he', title: 'Liên hệ', description: 'Thông tin liên hệ và gửi tin nhắn cho nhà trường.' },
    ];
    for (const r of staticRoutes) {
      await writeRoute(template, r.path, { title: r.title, description: r.description });
    }
    await writeRoute(template, '/', {
      title: null,
      description: 'Tin tức, sự kiện và thông tin tuyển sinh của Trường THCS & THPT.',
    });

    const news = await fetchAllPaginated('/v1/public/news');
    for (const a of news) {
      await writeRoute(template, `/tin-tuc/${a.slug}`, {
        title: a.title,
        description: a.summary,
        image: mediaUrl(a.coverImageUrl),
        type: 'article',
        bodyHtml: articleSnapshot(a),
      });
    }

    const events = await fetchAllPaginated('/v1/public/events');
    for (const e of events) {
      await writeRoute(template, `/su-kien/${e.slug}`, {
        title: e.title,
        description: e.location ? `${new Date(e.startAt).toLocaleString('vi-VN')} · ${e.location}` : undefined,
        image: mediaUrl(e.coverImageUrl),
        type: 'article',
        bodyHtml: eventSnapshot(e),
      });
    }

    await fs.writeFile(path.join(BUILD_DIR, 'sitemap.xml'), sitemapXml(news, events), 'utf8');

    console.log(`[prerender] done: ${staticRoutes.length + 1} static + ${news.length} news + ${events.length} events; sitemap.xml regenerated`);
  } catch (err) {
    console.warn(`[prerender] skipped (${err.message}). The SPA build itself is unaffected.`);
  }
}

main();
