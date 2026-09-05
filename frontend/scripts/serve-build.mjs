#!/usr/bin/env node
// Local stand-in for the production static host's routing (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md
// P4 - prerender). `vite preview` always falls back to the root build/index.html
// for any path it doesn't recognize as a real file (its SPA `single` mode),
// which means it never serves the per-route snapshots scripts/prerender.mjs
// writes to build/<route>/index.html - so it can't be used to verify them
// locally, and it does NOT reflect how conventional static hosts behave.
//
// This server instead resolves each request the way Nginx's
//   try_files $uri $uri/index.html /index.html;
// (or Apache/Netlify/Vercel's default static routing) does: an exact file,
// then that path's own index.html (the prerendered snapshot, if one was
// written), then the SPA shell as the final fallback for client-routed
// paths that have no snapshot (/login, /apply, /tin-tuc/<a-slug-newer-than-
// the-last-build>, ...). Whoever deploys this for real needs the same
// three-step rule in their web server config - see the plan doc's "Việc
// cấu hình khi lên thật" section.
import fs from 'node:fs/promises';
import http from 'node:http';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const BUILD_DIR = path.join(path.resolve(__dirname, '..'), 'build');
const PORT = Number(process.env.PORT) || 4173;

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.xml': 'application/xml; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.webp': 'image/webp',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.txt': 'text/plain; charset=utf-8',
};

async function readIfFile(p) {
  try {
    const stat = await fs.stat(p);
    return stat.isFile() ? p : null;
  } catch {
    return null;
  }
}

const server = http.createServer(async (req, res) => {
  const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
  const safe = path.normalize(pathname).replace(/^(\.\.[/\\])+/, ''); // no path traversal
  const candidates = [
    path.join(BUILD_DIR, safe),
    path.join(BUILD_DIR, safe, 'index.html'),
    path.join(BUILD_DIR, 'index.html'),
  ];
  for (const candidate of candidates) {
    const file = await readIfFile(candidate);
    if (file) {
      res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] || 'application/octet-stream' });
      res.end(await fs.readFile(file));
      return;
    }
  }
  res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
  res.end('Not found');
});

server.listen(PORT, () => {
  console.log(`Serving ${BUILD_DIR} at http://localhost:${PORT} (try_files-style routing)`);
});
