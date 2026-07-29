// TICKET-ADV125 — Jest-DOM matchers for React Testing Library
import '@testing-library/jest-dom/vitest';

// jsdom doesn't implement matchMedia — polyfill it so components that check
// prefers-color-scheme (ThemeContext) don't crash when rendered under test.
if (typeof window !== 'undefined' && !window.matchMedia) {
  window.matchMedia = (query) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  });
}
