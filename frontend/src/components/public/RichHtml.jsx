import React from 'react';
import { RICH_TEXT_PROSE_CLASS } from './richTextProse';

/**
 * Renders CMS rich-text that was already sanitized server-side by
 * HtmlSanitizerService (OWASP allow-list) - so dangerouslySetInnerHTML is
 * acceptable here specifically. Basic typographic styling via Tailwind
 * (no @tailwindcss/typography dependency) - shared with the editing side,
 * see richTextProse.js.
 */
export default function RichHtml({ html, className = '' }) {
  if (!html) return null;
  return (
    <div
      className={`${RICH_TEXT_PROSE_CLASS} ${className}`}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
}
