// TICKET-ADV124 — ThemeProvider: context flips data-theme; CSS owns colours.
import React, { createContext, useContext, useEffect, useState } from 'react';

const STORAGE_KEY = 'reconx-theme';

const ThemeContext = createContext({ theme: 'light', toggle: () => {} });

function initialTheme() {
  if (typeof window === 'undefined') {
    return 'light';
  }

  const storage = typeof window.localStorage === 'object' ? window.localStorage : undefined;
  const stored = storage && typeof storage.getItem === 'function'
    ? storage.getItem(STORAGE_KEY)
    : null;
  if (stored) return stored;

  const prefersDark = typeof window.matchMedia === 'function'
    ? window.matchMedia('(prefers-color-scheme: dark)').matches
    : false;

  return prefersDark ? 'dark' : 'light';
}

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(initialTheme);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    const storage = typeof window.localStorage === 'object' ? window.localStorage : undefined;
    if (storage && typeof storage.setItem === 'function') {
      storage.setItem(STORAGE_KEY, theme);
    }
  }, [theme]);

  const toggle = () => setTheme((t) => (t === 'light' ? 'dark' : 'light'));

  return (
    <ThemeContext.Provider value={{ theme, toggle }}>
      {children}
    </ThemeContext.Provider>
  );
}

export const useTheme = () => useContext(ThemeContext);
