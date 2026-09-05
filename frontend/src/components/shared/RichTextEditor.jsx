import React, { useEffect } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import { Placeholder } from '@tiptap/extensions';
import {
  MdFormatBold, MdFormatItalic, MdFormatUnderlined, MdFormatQuote,
  MdFormatListBulleted, MdFormatListNumbered, MdHorizontalRule,
  MdLink, MdLinkOff, MdImage, MdUndo, MdRedo,
} from 'react-icons/md';
import { RICH_TEXT_PROSE_CLASS } from '../public/richTextProse';

/**
 * WYSIWYG editor for CMS rich-text fields (NewsManagement/EventManagement -
 * "content"/"description"), replacing the plain textarea-HTML editor
 * (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md P4). Only offers formatting the
 * backend's HtmlSanitizerService allow-list actually keeps (bold/italic/
 * underline, h2/h3, lists, blockquote, links, images, hr) - no code block,
 * no text color/font, since HtmlSanitizerService would silently strip those
 * and a control that visibly "works" but loses its effect on save would be
 * a worse experience than not offering it. The server still re-sanitizes on
 * every save regardless - this is only about not confusing the editor UI.
 *
 * Emits plain HTML via onChange, same shape the old textarea produced, so
 * callers (ArticleDialog/EventDialog) don't need to change their submit
 * payload at all.
 */
export default function RichTextEditor({ value, onChange, onUploadImage, placeholder, disabled = false }) {
  const [uploading, setUploading] = React.useState(false);
  const fileInputRef = React.useRef(null);

  const editor = useEditor({
    editable: !disabled,
    extensions: [
      StarterKit.configure({
        heading: { levels: [2, 3] },
        code: false,
        codeBlock: false,
        link: {
          openOnClick: false,
          autolink: true,
          HTMLAttributes: { rel: 'noopener noreferrer nofollow' },
        },
      }),
      Image,
      Placeholder.configure({ placeholder: placeholder || '' }),
    ],
    content: value || '',
    onUpdate: ({ editor: e }) => onChange(e.isEmpty ? '' : e.getHTML()),
    editorProps: {
      attributes: {
        class: `${RICH_TEXT_PROSE_CLASS} min-h-[10rem] rounded-b-md border border-t-0 bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/40`,
      },
    },
  });

  // Re-sync when the caller swaps to a different record (dialog reopened
  // with another article/event) - never while the user is actively typing,
  // or every keystroke's onUpdate -> value prop echo would fight the cursor.
  //
  // `editor.isDestroyed` guard is required, not defensive fluff: React 18
  // StrictMode double-invokes effects in dev (mount -> cleanup -> mount),
  // and useEditor()'s cleanup calls editor.destroy() - a destroyed editor's
  // schema is null, so a stale closure calling editor.getHTML() here throws
  // "Cannot read properties of null (reading 'cached')" from ProseMirror's
  // DOMSerializer, uncaught (no error boundary in the tree - App.jsx),
  // blanking the whole app every time NewsManagement/EventManagement's
  // "new article/event" dialog opened. Reproduced live, not theoretical.
  useEffect(() => {
    if (!editor || editor.isDestroyed || editor.isFocused) return;
    if ((value || '') !== editor.getHTML()) {
      editor.commands.setContent(value || '', { emitUpdate: false });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value, editor]);

  useEffect(() => {
    editor?.setEditable(!disabled);
  }, [disabled, editor]);

  useEffect(() => () => editor?.destroy(), [editor]);

  if (!editor) return null;

  const setLink = () => {
    const prev = editor.getAttributes('link').href || '';
    // eslint-disable-next-line no-alert
    const url = window.prompt('Nhập URL liên kết (để trống để bỏ liên kết):', prev);
    if (url === null) return;
    if (!url.trim()) {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
      return;
    }
    editor.chain().focus().extendMarkRange('link').setLink({ href: url.trim() }).run();
  };

  const pickImage = () => fileInputRef.current?.click();

  const onImageSelected = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file || !onUploadImage) return;
    setUploading(true);
    try {
      const url = await onUploadImage(file);
      if (url) editor.chain().focus().setImage({ src: url }).run();
    } finally {
      setUploading(false);
    }
  };

  const buttons = [
    { label: 'Đậm', Icon: MdFormatBold, active: editor.isActive('bold'), onClick: () => editor.chain().focus().toggleBold().run() },
    { label: 'Nghiêng', Icon: MdFormatItalic, active: editor.isActive('italic'), onClick: () => editor.chain().focus().toggleItalic().run() },
    { label: 'Gạch chân', Icon: MdFormatUnderlined, active: editor.isActive('underline'), onClick: () => editor.chain().focus().toggleUnderline().run() },
    { type: 'sep' },
    { label: 'Tiêu đề vừa', text: 'H2', active: editor.isActive('heading', { level: 2 }), onClick: () => editor.chain().focus().toggleHeading({ level: 2 }).run() },
    { label: 'Tiêu đề nhỏ', text: 'H3', active: editor.isActive('heading', { level: 3 }), onClick: () => editor.chain().focus().toggleHeading({ level: 3 }).run() },
    { label: 'Đoạn văn thường', text: 'P', active: editor.isActive('paragraph'), onClick: () => editor.chain().focus().setParagraph().run() },
    { type: 'sep' },
    { label: 'Danh sách chấm', Icon: MdFormatListBulleted, active: editor.isActive('bulletList'), onClick: () => editor.chain().focus().toggleBulletList().run() },
    { label: 'Danh sách số', Icon: MdFormatListNumbered, active: editor.isActive('orderedList'), onClick: () => editor.chain().focus().toggleOrderedList().run() },
    { label: 'Trích dẫn', Icon: MdFormatQuote, active: editor.isActive('blockquote'), onClick: () => editor.chain().focus().toggleBlockquote().run() },
    { label: 'Đường kẻ ngang', Icon: MdHorizontalRule, onClick: () => editor.chain().focus().setHorizontalRule().run() },
    { type: 'sep' },
    { label: 'Chèn liên kết', Icon: MdLink, active: editor.isActive('link'), onClick: setLink },
    { label: 'Bỏ liên kết', Icon: MdLinkOff, disabled: !editor.isActive('link'), onClick: () => editor.chain().focus().unsetLink().run() },
    ...(onUploadImage ? [{ label: 'Chèn ảnh', Icon: MdImage, disabled: uploading, onClick: pickImage }] : []),
    { type: 'sep' },
    { label: 'Hoàn tác', Icon: MdUndo, disabled: !editor.can().undo(), onClick: () => editor.chain().focus().undo().run() },
    { label: 'Làm lại', Icon: MdRedo, disabled: !editor.can().redo(), onClick: () => editor.chain().focus().redo().run() },
  ];

  return (
    <div>
      <div className="flex flex-wrap items-center gap-0.5 rounded-t-md border bg-muted/40 p-1">
        {buttons.map((b, i) => {
          if (b.type === 'sep') return <span key={i} className="mx-1 h-5 w-px bg-border" />;
          const { Icon, text, label, active, disabled: btnDisabled, onClick } = b;
          return (
            <button
              key={label}
              type="button"
              title={label}
              aria-label={label}
              aria-pressed={Boolean(active)}
              disabled={btnDisabled || disabled}
              onMouseDown={(e) => e.preventDefault()} // keep editor selection/focus while clicking
              onClick={onClick}
              className={
                'inline-flex h-7 min-w-7 items-center justify-center rounded px-1 text-xs font-medium hover:bg-muted disabled:pointer-events-none disabled:opacity-40 ' +
                (active ? 'bg-primary/15 text-primary' : 'text-foreground/80')
              }
            >
              {Icon ? <Icon className="h-4 w-4" /> : text}
            </button>
          );
        })}
        {uploading && <span className="ml-1 text-xs text-muted-foreground">Đang tải ảnh...</span>}
      </div>
      <EditorContent editor={editor} />
      {onUploadImage && (
        <input ref={fileInputRef} type="file" accept="image/*" className="hidden" onChange={onImageSelected} />
      )}
    </div>
  );
}
