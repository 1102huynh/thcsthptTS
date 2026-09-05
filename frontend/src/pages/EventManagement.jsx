import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiPlus, FiEdit2, FiTrash2, FiUploadCloud, FiEye, FiEyeOff } from 'react-icons/fi';
import { eventCmsService } from '../services/dataService';
import { mediaUrl } from '../services/publicService';
import { useMediaUpload } from '../hooks/useMediaUpload';
import DataTable from '../components/shared/DataTable';
import RichTextEditor from '../components/shared/RichTextEditor';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Badge } from '../components/ui/badge';
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter,
} from '../components/ui/dialog';
import {
  AlertDialog, AlertDialogContent, AlertDialogHeader, AlertDialogTitle,
  AlertDialogDescription, AlertDialogFooter, AlertDialogCancel, AlertDialogAction,
} from '../components/ui/alert-dialog';

const STATUS_LABELS = { DRAFT: 'Bản nháp', PUBLISHED: 'Đã đăng', ARCHIVED: 'Đã gỡ' };
const STATUS_VARIANT = { DRAFT: 'outline', PUBLISHED: 'default', ARCHIVED: 'secondary' };
const EMPTY = { title: '', description: '', coverImageUrl: '', location: '', startAt: '', endAt: '', isFeatured: false };

function fmt(v) {
  if (!v) return '—';
  try { return new Date(v).toLocaleString('vi-VN'); } catch { return '—'; }
}
// datetime-local wants "yyyy-MM-ddThh:mm"
function toLocalInput(v) {
  if (!v) return '';
  const d = new Date(v);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function EventDialog({ open, onOpenChange, event, onSaved }) {
  const isEdit = Boolean(event?.id);
  const [form, setForm] = useState(EMPTY);
  const [uploading, setUploading] = useState(false);
  const fieldCls = 'mt-1 w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/40';

  React.useEffect(() => {
    if (open) {
      setForm(event
        ? {
            title: event.title ?? '', description: event.description ?? '',
            coverImageUrl: event.coverImageUrl ?? '', location: event.location ?? '',
            startAt: toLocalInput(event.startAt), endAt: toLocalInput(event.endAt),
            isFeatured: Boolean(event.isFeatured),
          }
        : EMPTY);
    }
  }, [open, event]);

  const saveMutation = useMutation({
    mutationFn: (payload) => (isEdit ? eventCmsService.update(event.id, payload) : eventCmsService.create(payload)),
    onSuccess: () => { toast.success(isEdit ? 'Đã lưu sự kiện' : 'Đã tạo sự kiện (bản nháp)'); onSaved(); onOpenChange(false); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Không lưu được sự kiện'),
  });

  const { uploadCover, uploadInlineImage } = useMediaUpload({ setForm, setUploading });

  const submit = (e) => {
    e.preventDefault();
    if (!form.title.trim()) { toast.error('Vui lòng nhập tiêu đề'); return; }
    if (!form.startAt) { toast.error('Vui lòng chọn thời gian bắt đầu'); return; }
    saveMutation.mutate({
      title: form.title.trim(),
      description: form.description || null,
      coverImageUrl: form.coverImageUrl || null,
      location: form.location.trim() || null,
      startAt: form.startAt,
      endAt: form.endAt || null,
      isFeatured: form.isFeatured,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa sự kiện' : 'Tạo sự kiện mới'}</DialogTitle>
          <DialogDescription>Sự kiện mới ở trạng thái bản nháp cho tới khi bấm "Đăng".</DialogDescription>
        </DialogHeader>

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Tiêu đề *</label>
            <Input value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} maxLength={255} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="text-sm font-medium">Bắt đầu *</label>
              <input type="datetime-local" className={fieldCls}
                value={form.startAt} onChange={(e) => setForm((f) => ({ ...f, startAt: e.target.value }))} />
            </div>
            <div>
              <label className="text-sm font-medium">Kết thúc</label>
              <input type="datetime-local" className={fieldCls}
                value={form.endAt} onChange={(e) => setForm((f) => ({ ...f, endAt: e.target.value }))} />
            </div>
          </div>
          <div>
            <label className="text-sm font-medium">Địa điểm</label>
            <Input value={form.location} onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))} maxLength={255} />
          </div>
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.isFeatured} onChange={(e) => setForm((f) => ({ ...f, isFeatured: e.target.checked }))} />
            Ghim làm sự kiện nổi bật
          </label>
          <div>
            <label className="text-sm font-medium">Ảnh bìa</label>
            <div className="mt-1 flex items-center gap-3">
              {form.coverImageUrl && <img src={mediaUrl(form.coverImageUrl)} alt="" className="h-16 w-24 rounded object-cover" />}
              <label className="inline-flex cursor-pointer items-center gap-2 rounded-md border px-3 py-2 text-sm hover:bg-muted">
                <FiUploadCloud className="h-4 w-4" /> {uploading ? 'Đang tải...' : 'Chọn ảnh'}
                <input type="file" accept="image/*" className="hidden" onChange={uploadCover} disabled={uploading} />
              </label>
            </div>
          </div>
          <div>
            <label className="text-sm font-medium">Mô tả</label>
            <div className="mt-1">
              <RichTextEditor
                value={form.description}
                onChange={(html) => setForm((f) => ({ ...f, description: html }))}
                onUploadImage={uploadInlineImage}
                placeholder="Mô tả sự kiện…"
              />
            </div>
            <p className="mt-1 text-xs text-muted-foreground">Nội dung được máy chủ làm sạch khi lưu.</p>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
            <Button type="submit" disabled={saveMutation.isPending}>{saveMutation.isPending ? 'Đang lưu...' : 'Lưu'}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default function EventManagement() {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const listQuery = useQuery({ queryKey: ['cms-events'], queryFn: () => eventCmsService.list({ page: 0, size: 100 }) });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['cms-events'] });

  const publishMutation = useMutation({
    mutationFn: ({ id, publish }) => (publish ? eventCmsService.publish(id) : eventCmsService.unpublish(id)),
    onSuccess: (_d, v) => { toast.success(v.publish ? 'Đã đăng sự kiện' : 'Đã gỡ sự kiện'); invalidate(); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Thao tác thất bại'),
  });
  const deleteMutation = useMutation({
    mutationFn: (id) => eventCmsService.remove(id),
    onSuccess: () => { toast.success('Đã xoá sự kiện'); invalidate(); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Không xoá được'),
    onSettled: () => setDeleting(null),
  });

  const columns = useMemo(() => [
    { accessorKey: 'title', header: 'Tiêu đề', cell: ({ row }) => <span className="font-medium">{row.original.title}</span> },
    { accessorKey: 'startAt', header: 'Bắt đầu', cell: ({ getValue }) => fmt(getValue()) },
    { accessorKey: 'location', header: 'Địa điểm', cell: ({ getValue }) => getValue() || '—' },
    {
      accessorKey: 'status', header: 'Trạng thái',
      cell: ({ getValue }) => <Badge variant={STATUS_VARIANT[getValue()]}>{STATUS_LABELS[getValue()] ?? getValue()}</Badge>,
    },
    {
      id: 'actions', header: '',
      cell: ({ row }) => {
        const ev = row.original;
        return (
          <div className="flex justify-end gap-1">
            <Button variant="ghost" size="icon" aria-label="Sửa" onClick={() => { setEditing(ev); setDialogOpen(true); }}>
              <FiEdit2 className="h-4 w-4" />
            </Button>
            {ev.status === 'PUBLISHED' ? (
              <Button variant="ghost" size="icon" aria-label="Gỡ" onClick={() => publishMutation.mutate({ id: ev.id, publish: false })}>
                <FiEyeOff className="h-4 w-4" />
              </Button>
            ) : (
              <Button variant="ghost" size="icon" aria-label="Đăng" onClick={() => publishMutation.mutate({ id: ev.id, publish: true })}>
                <FiEye className="h-4 w-4" />
              </Button>
            )}
            <Button variant="ghost" size="icon" aria-label="Xoá" className="text-destructive hover:text-destructive"
              onClick={() => setDeleting(ev)}>
              <FiTrash2 className="h-4 w-4" />
            </Button>
          </div>
        );
      },
    },
  ], [publishMutation]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Sự kiện (Cổng công khai)</h1>
          <p className="text-sm text-muted-foreground">Đăng và quản lý sự kiện hiển thị trên trang công khai.</p>
        </div>
        <Button onClick={() => { setEditing(null); setDialogOpen(true); }}>
          <FiPlus className="mr-2 h-4 w-4" /> Sự kiện mới
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={listQuery.data?.items ?? []}
        isLoading={listQuery.isLoading}
        emptyMessage="Chưa có sự kiện nào."
      />

      <EventDialog open={dialogOpen} onOpenChange={setDialogOpen} event={editing} onSaved={invalidate} />

      <AlertDialog open={Boolean(deleting)} onOpenChange={(o) => !o && setDeleting(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xoá sự kiện?</AlertDialogTitle>
            <AlertDialogDescription>Bạn sắp xoá "{deleting?.title}". Hành động này không thể hoàn tác.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteMutation.isPending}
              onClick={() => deleteMutation.mutate(deleting.id)}
            >
              {deleteMutation.isPending ? 'Đang xoá...' : 'Xoá'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
