import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { visualizer } from 'rollup-plugin-visualizer';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    // Emits build/bundle-stats.html after `npm run build` (gitignored,
    // dev-only tooling output - not part of the deployed app). Vite has no
    // CRA-style `--report` flag, so this is the plan's other option
    // (Tuần 6 Ngày 2) for seeing what each chunk is actually made of after
    // the route-level code-splitting below.
    visualizer({
      filename: 'build/bundle-stats.html',
      gzipSize: true,
      brotliSize: true,
    }),
  ],
  resolve: {
    alias: {
      // Matches components.json's aliases - shadcn/ui components import
      // each other via "@/components/ui/..." etc.
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
  },
  build: {
    outDir: 'build',
  },
});
