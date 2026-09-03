import React from 'react';
import { describe, it, expect, afterEach } from 'vitest';
import { render, cleanup } from '@testing-library/react';
import Seo from './Seo';

afterEach(cleanup);

const meta = (sel) => document.head.querySelector(sel)?.getAttribute('content');

describe('Seo', () => {
  it('sets the document title and OG/description meta while mounted', () => {
    const { unmount } = render(
      <Seo title="Bài viết A" description="Mô tả bài A" image="https://x/img.jpg" type="article" />
    );

    expect(document.title).toBe('Bài viết A — Trường THCS & THPT');
    expect(meta('meta[name="description"]')).toBe('Mô tả bài A');
    expect(meta('meta[property="og:title"]')).toBe('Bài viết A — Trường THCS & THPT');
    expect(meta('meta[property="og:type"]')).toBe('article');
    expect(meta('meta[property="og:image"]')).toBe('https://x/img.jpg');
    expect(meta('meta[name="twitter:card"]')).toBe('summary_large_image');

    unmount();
    // reverts on unmount
    expect(document.title).not.toContain('Bài viết A');
  });

  it('falls back to the site name and default description with no props', () => {
    render(<Seo />);
    expect(document.title).toBe('Trường THCS & THPT');
    expect(meta('meta[name="description"]')).toContain('tuyển sinh');
  });
});
