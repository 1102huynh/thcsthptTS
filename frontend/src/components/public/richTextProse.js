// Shared Tailwind-arbitrary-variant styling for CMS rich-text HTML - used by
// both the read side (RichHtml, the public portal) and the write side
// (shared/RichTextEditor, the CMS dialogs) so what an editor sees while
// typing matches what a visitor sees after publish. No @tailwindcss/
// typography dependency (project convention, see RichHtml's own note).
export const RICH_TEXT_PROSE_CLASS =
  'max-w-none space-y-4 leading-relaxed ' +
  '[&_h1]:text-2xl [&_h1]:font-bold [&_h2]:text-xl [&_h2]:font-semibold ' +
  '[&_h3]:text-lg [&_h3]:font-semibold [&_h2]:mt-6 [&_h3]:mt-4 ' +
  '[&_p]:my-3 [&_ul]:list-disc [&_ul]:pl-6 [&_ol]:list-decimal [&_ol]:pl-6 ' +
  '[&_li]:my-1 [&_a]:text-primary [&_a]:underline [&_img]:rounded-lg [&_img]:my-4 ' +
  '[&_table]:w-full [&_table]:border-collapse [&_td]:border [&_td]:border-border [&_td]:p-2 ' +
  '[&_th]:border [&_th]:border-border [&_th]:p-2 [&_th]:bg-muted [&_blockquote]:border-l-4 ' +
  '[&_blockquote]:border-border [&_blockquote]:pl-4 [&_blockquote]:text-muted-foreground ' +
  '[&_hr]:my-6 [&_hr]:border-border';
