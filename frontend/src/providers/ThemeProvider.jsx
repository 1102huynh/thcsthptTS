import React, { createContext, useContext, useEffect, useState } from 'react';

const STORAGE_KEY = 'theme';
const ThemeContext = createContext(undefined);

function systemPrefersDark() {
  return window.matchMedia('(prefers-color-scheme: dark)').matches;
}

function readStoredTheme() {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored === 'light' || stored === 'dark') return stored;
  } catch {
    // localStorage unavailable (private mode, blocked) - fall through to system.
  }
  return systemPrefersDark() ? 'dark' : 'light';
}

/**
 * Tailwind's `class` dark-mode strategy (tailwind.config.js: darkMode:
 * 'class') needs a `dark` class on <html>; this provider owns that class
 * plus persistence, and exposes `theme`/`toggleTheme` to any component.
 *
 * index.html has a matching inline script that applies the same class
 * before React even mounts (reading the same localStorage key) - otherwise
 * a dark-mode user would see a flash of the light theme on every load while
 * this component's first effect runs.
 */
function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(readStoredTheme);

  useEffect(() => {
    const root = document.documentElement;
    root.classList.toggle('dark', theme === 'dark');
    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch {
      // ignore - theme still applies for this page load, just won't persist
    }
  }, [theme]);

  useEffect(() => {
    // Only follow the OS setting live if the user never explicitly chose a
    // theme on this device (nothing in localStorage yet).
    let hasStoredChoice = false;
    try {
      hasStoredChoice = localStorage.getItem(STORAGE_KEY) != null;
    } catch {
      hasStoredChoice = false;
    }
    if (hasStoredChoice) return undefined;

    const mql = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = (e) => setTheme(e.matches ? 'dark' : 'light');
    mql.addEventListener('change', handleChange);
    return () => mql.removeEventListener('change', handleChange);
  }, []);

  const toggleTheme = () => setTheme((t) => (t === 'dark' ? 'light' : 'dark'));

  return (
    <ThemeContext.Provider value={{ theme, setTheme, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be used within a ThemeProvider');
  return ctx;
}

export default ThemeProvider;
