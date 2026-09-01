import React from 'react';
import ReactDOM from 'react-dom/client';
import { QueryClientProvider } from '@tanstack/react-query';
// Bootstrap (and App's own legacy layout CSS) load first, index.css
// (Tailwind + the new design tokens/font) loads last - so as pages migrate
// off Bootstrap, the new global styles win any tag-level conflict (body,
// headings, ...) while Bootstrap's own scoped component classes (.btn,
// .card, .modal, ...) stay fully usable by whatever hasn't been migrated
// yet. Moved here from App.jsx specifically to guarantee this load order -
// see index.css's own comment on the Be Vietnam Pro font for what this fixed.
import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';
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

