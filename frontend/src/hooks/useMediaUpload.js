import { toast } from 'sonner';
import { mediaCmsService } from '../services/dataService';
import { mediaUrl } from '../services/publicService';

/**
 * Shared upload logic for CMS forms (news, events, ...) that have a cover
 * image field plus a rich-text body where images can be inlined.
 *
 * - `uploadCover` stores the raw asset URL into `coverField` of the form;
 *   it is resolved through mediaUrl() at display time instead.
 * - `uploadInlineImage` is handed to RichTextEditor's image button: inline
 *   images are rendered as-is by RichHtml (no mediaUrl() pass at display
 *   time), so the <img> src stashed into the editor must already be the
 *   absolute URL.
 */
export function useMediaUpload({ setForm, setUploading, coverField = 'coverImageUrl' }) {
  const uploadCover = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const asset = await mediaCmsService.upload(file);
      setForm((f) => ({ ...f, [coverField]: asset.url }));
      toast.success('Đã tải ảnh bìa');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không tải được ảnh');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const uploadInlineImage = async (file) => {
    try {
      const asset = await mediaCmsService.upload(file);
      return mediaUrl(asset.url);
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Không tải được ảnh');
      return null;
    }
  };

  return { uploadCover, uploadInlineImage };
}
