import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';

// RTL's own auto-cleanup only self-registers when it detects a *global*
// afterEach - vitest.config.js doesn't set test.globals (kept explicit
// `import { describe, it, expect } from 'vitest'` in every test file
// instead), so without this each test's rendered tree stayed mounted into
// the next one, and later tests in a file started failing with "multiple
// elements found" for markup that actually came from an earlier test.
afterEach(() => {
  cleanup();
});

// jsdom doesn't implement these, but the Radix primitives used throughout
// (Popover for DatePicker, Select for DataTable's page-size control, ...)
// call them during layout/positioning and pointer-capture handling - every
// component under test here goes through one of those, so without these
// polyfills even a plain render can throw. Standard workaround for
// testing Radix under jsdom, not specific to any one component.
if (!window.ResizeObserver) {
  window.ResizeObserver = class ResizeObserver {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
}

if (!window.matchMedia) {
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

if (!Element.prototype.hasPointerCapture) {
  Element.prototype.hasPointerCapture = () => false;
}
if (!Element.prototype.setPointerCapture) {
  Element.prototype.setPointerCapture = () => {};
}
if (!Element.prototype.releasePointerCapture) {
  Element.prototype.releasePointerCapture = () => {};
}
if (!Element.prototype.scrollIntoView) {
  Element.prototype.scrollIntoView = () => {};
}
