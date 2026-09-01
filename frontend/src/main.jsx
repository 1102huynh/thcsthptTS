import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClientProvider } from '@tanstack/react-query';
// Bootstrap and App.css (its own legacy layout/component-override CSS) used
// to load here, before index.css, so Bootstrap's scoped component classes
// (.btn, .card, .modal, ...) stayed usable by whatever page hadn't migrated
// off react-bootstrap yet. The last page migrated back in Tuần 4, and Tuần
// 6 Ngày 1 confirmed nothing in src/ references react-bootstrap or a raw
// Bootstrap class anymore, so both are gone now - index.css (Tailwind +
// the design tokens/font) is the only global stylesheet left.
import './index.css';
import App from './App';
import ThemeProvider from './providers/ThemeProvider';
import { queryClient } from './lib/queryClient';
import { Toaster } from './components/ui/sonner';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <App />
        <Toaster richColors closeButton position="top-right" />
      </ThemeProvider>
    </QueryClientProvider>
  </React.StrictMode>
);

