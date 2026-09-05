import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, cleanup, screen, fireEvent, waitFor } from '@testing-library/react';
import RichTextEditor from './RichTextEditor';

afterEach(cleanup);

// KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md P4: WYSIWYG editor replacing the
// textarea-HTML editor in NewsManagement/EventManagement. TipTap runs a real
// ProseMirror contentEditable under jsdom here, so this stays a shallow
// smoke test (mount + toolbar wiring), not a full typing simulation.
describe('RichTextEditor', () => {
  it('renders the initial HTML and a formatting toolbar', async () => {
    render(<RichTextEditor value="<p>Nội dung ban đầu</p>" onChange={() => {}} />);

    await waitFor(() => expect(screen.getByText('Nội dung ban đầu')).toBeInTheDocument());
    expect(screen.getByRole('button', { name: 'Đậm' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Chèn liên kết' })).toBeInTheDocument();
    // No onUploadImage prop -> no image button offered.
    expect(screen.queryByRole('button', { name: 'Chèn ảnh' })).not.toBeInTheDocument();
  });

  it('offers an image button only when onUploadImage is provided, and calls it on file pick', async () => {
    const onUploadImage = vi.fn().mockResolvedValue('https://x/img.jpg');
    render(<RichTextEditor value="" onChange={() => {}} onUploadImage={onUploadImage} />);

    const imageButton = await screen.findByRole('button', { name: 'Chèn ảnh' });
    expect(imageButton).toBeInTheDocument();
    // The button opens a hidden <input type=file>; simulate picking a file directly.
    const file = new File(['x'], 'photo.png', { type: 'image/png' });
    const input = document.querySelector('input[type="file"]');
    fireEvent.change(input, { target: { files: [file] } });

    await waitFor(() => expect(onUploadImage).toHaveBeenCalledWith(file));
  });

  it('toggling a mark button does not throw with no selection', async () => {
    render(<RichTextEditor value="<p>abc</p>" onChange={() => {}} />);
    await waitFor(() => expect(screen.getByText('abc')).toBeInTheDocument());

    const bold = screen.getByRole('button', { name: 'Đậm' });
    expect(() => fireEvent.click(bold)).not.toThrow();
  });
});
