import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiPlus, FiEdit2, FiTrash2, FiUploadCloud, FiEye, FiEyeOff } from 'react-icons/fi';
import { newsCmsService } from '../services/dataService';
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
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '../components/ui/select';

const STATUS_LABELS = { DRAFT: 'Bản nháp', PUBLISHED: 'Đã đăng', ARCHIVED: 'Đã gỡ' };
const STATUS_VARIANT = { DRAFT: 'outline', PUBLISHED: 'default', ARCHIVED: 'secondary' };
const EMPTY = { title: '', summary: '', content: '', coverImageUrl: '', categoryId: '', isFeatured: false };

function formatDate(v) {
  if (!v) return '—';
  try { return new Date(v).toLocaleDateString('vi-VN'); } catch { return '—'; }
}

function ArticleDialog({ open, onOpenChange, article, categories, onSaved }) {
  const isEdit = Boolean(article?.id);
  const [form, setForm] = useState(EMPTY);
  const [uploading, setUploading] = useState(false);
  const fieldCls = 'mt-1 w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/40';

  React.useEffect(() => {
    if (open) {
      setForm(article
        ? {
            title: article.title ?? '', summary: article.summary ?? '', content: article.content ?? '',
            coverImageUrl: article.coverImageUrl ?? '',
            categoryId: article.categoryId ? String(article.categoryId) : '',
            isFeatured: Boolean(article.isFeatured),
          }
        : EMPTY);
    }
  }, [open, article]);

  const saveMutation = useMutation({
    mutationFn: (payload) => (isEdit ? newsCmsService.update(article.id, payload) : newsCmsService.create(payload)),
    onSuccess: () => { toast.success(isEdit ? 'Đã lưu bài tin' : 'Đã tạo bài tin (bản nháp)'); onSaved(); onOpenChange(false); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Không lưu được bài tin'),
  });

  const { uploadCover, uploadInlineImage } = useMediaUpload({ setForm, setUploading });

  const submit = (e) => {
    e.preventDefault();
    if (!form.title.trim()) { toast.error('Vui lòng nhập tiêu đề'); return; }
    saveMutation.mutate({
      title: form.title.trim(),
      summary: form.summary.trim() || null,
      content: form.content || null,
      coverImageUrl: form.coverImageUrl || null,
      categoryId: form.categoryId ? Number(form.categoryId) : null,
      isFeatured: form.isFeatured,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa bài tin' : 'Tạo bài tin mới'}</DialogTitle>
          <DialogDescription>Bài mới ở trạng thái bản nháp, chưa hiển thị công khai cho tới khi bấm "Đăng".</DialogDescription>
        </DialogHeader>

        <form onSubmit={submit} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Tiêu đề *</label>
            <Input value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} maxLength={255} />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="text-sm font-medium">Chuyên mục</label>
              <Select value={form.categoryId} onValueChange={(v) => setForm((f) => ({ ...f, categoryId: v }))}>
                <SelectTrigger className="mt-1"><SelectValue placeholder="— Không phân loại —" /></SelectTrigger>
                <SelectContent>
                  {categories.map((c) => <SelectItem key={c.id} value={String(c.id)}>{c.name}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <label className="mt-6 flex items-center gap-2 text-sm">
              <input type="checkbox" checked={form.isFeatured} onChange={(e) => setForm((f) => ({ ...f, isFeatured: e.target.checked }))} />
              Ghim làm tin nổi bật
            </label>
          </div>
          <div>
            <label className="text-sm font-medium">Tóm tắt</label>
            <textarea className={fieldCls} rows={2} maxLength={500}
              value={form.summary} onChange={(e) => setForm((f) => ({ ...f, summary: e.target.value }))} />
          </div>
          <div>
            <label className="text-sm font-medium">Ảnh bìa</label>
            <div className="mt-1 flex items-center gap-3">
              {form.coverImageUrl && <img src={mediaUrl(form.coverImageUrl)} alt="" className="h-16 w-24 rounded object-cover" />}
              <label className="inline-flex cursor-pointer items-center gap-2 rounded-md border px-3 py-2 text-sm hover:bg-muted">
                <FiUploadCloud className="h-4 w-4" /> {uploading ? 'Đang tải...' : 'Chọn ảnh'}
                <input type="file" accept="image/*" className="hidden" onChange={uploadCover} disabled={uploading} />
              </label>
              {form.coverImageUrl && (
                <button type="button" className="text-xs text-destructive" onClick={() => setForm((f) => ({ ...f, coverImageUrl: '' }))}>
                  Bỏ ảnh
                </button>
              )}
            </div>
          </div>
          <div>
            <label className="text-sm font-medium">Nội dung</label>
            <div className="mt-1">
              <RichTextEditor
                value={form.content}
                onChange={(html) => setForm((f) => ({ ...f, content: html }))}
                onUploadImage={uploadInlineImage}
                placeholder="Nội dung bài viết…"
              />
            </div>
            <p className="mt-1 text-xs text-muted-foreground">
              Nội dung sẽ được máy chủ làm sạch (bỏ script, thẻ nguy hiểm) khi lưu.
            </p>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
            <Button type="submit" disabled={saveMutation.isPending}>
              {saveMutation.isPending ? 'Đang lưu...' : 'Lưu'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default function NewsManagement() {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleting, setDeleting] = useState(null);
  const [newCategory, setNewCategory] = useState('');

  const listQuery = useQuery({ queryKey: ['cms-news'], queryFn: () => newsCmsService.list({ page: 0, size: 100 }) });
  const catQuery = useQuery({ queryKey: ['cms-news-categories'], queryFn: () => newsCmsService.categories() });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['cms-news'] });

  const publishMutation = useMutation({
    mutationFn: ({ id, publish }) => (publish ? newsCmsService.publish(id) : newsCmsService.unpublish(id)),
    onSuccess: (_d, v) => { toast.success(v.publish ? 'Đã đăng bài' : 'Đã gỡ bài'); invalidate(); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Thao tác thất bại'),
  });
  const deleteMutation = useMutation({
    mutationFn: (id) => newsCmsService.remove(id),
    onSuccess: () => { toast.success('Đã xoá bài tin'); invalidate(); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Không xoá được'),
    onSettled: () => setDeleting(null),
  });
  const addCategoryMutation = useMutation({
    mutationFn: (name) => newsCmsService.createCategory({ name }),
    onSuccess: () => { toast.success('Đã thêm chuyên mục'); setNewCategory(''); queryClient.invalidateQueries({ queryKey: ['cms-news-categories'] }); },
    onError: (e) => toast.error(e?.response?.data?.message || 'Không thêm được chuyên mục'),
  });

  const columns = useMemo(() => [
    { accessorKey: 'title', header: 'Tiêu đề', cell: ({ row }) => <span className="font-medium">{row.original.title}</span> },
    { accessorKey: 'categoryName', header: 'Chuyên mục', cell: ({ getValue }) => getValue() || '—' },
    {
      accessorKey: 'status', header: 'Trạng thái',
      cell: ({ getValue }) => <Badge variant={STATUS_VARIANT[getValue()]}>{STATUS_LABELS[getValue()] ?? getValue()}</Badge>,
    },
    { accessorKey: 'publishedAt', header: 'Ngày đăng', cell: ({ getValue }) => formatDate(getValue()) },
    { accessorKey: 'viewCount', header: 'Lượt xem', cell: ({ getValue }) => getValue() ?? 0 },
    {
      id: 'actions', header: '',
      cell: ({ row }) => {
        const a = row.original;
        return (
          <div className="flex justify-end gap-1">
            <Button variant="ghost" size="icon" aria-label="Sửa" onClick={() => { setEditing(a); setDialogOpen(true); }}>
              <FiEdit2 className="h-4 w-4" />
            </Button>
            {a.status === 'PUBLISHED' ? (
              <Button variant="ghost" size="icon" aria-label="Gỡ bài" onClick={() => publishMutation.mutate({ id: a.id, publish: false })}>
                <FiEyeOff className="h-4 w-4" />
              </Button>
            ) : (
              <Button variant="ghost" size="icon" aria-label="Đăng bài" onClick={() => publishMutation.mutate({ id: a.id, publish: true })}>
                <FiEye className="h-4 w-4" />
              </Button>
            )}
            <Button variant="ghost" size="icon" aria-label="Xoá" className="text-destructive hover:text-destructive"
              onClick={() => setDeleting(a)}>
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
          <h1 className="text-2xl font-semibold">Tin tức (Cổng công khai)</h1>
          <p className="text-sm text-muted-foreground">Đăng và quản lý bài tin hiển thị trên trang công khai.</p>
        </div>
        <Button onClick={() => { setEditing(null); setDialogOpen(true); }}>
          <FiPlus className="mr-2 h-4 w-4" /> Bài tin mới
        </Button>
      </div>

      <div className="flex flex-wrap items-center gap-2 rounded-lg border bg-card p-3 text-sm">
        <span className="font-medium">Chuyên mục:</span>
        {(catQuery.data ?? []).map((c) => (
          <span key={c.id} className="rounded-full bg-muted px-2 py-0.5">{c.name}</span>
        ))}
        <Input className="h-8 w-40" placeholder="Thêm chuyên mục..." value={newCategory}
          onChange={(e) => setNewCategory(e.target.value)} />
        <Button size="sm" variant="outline" disabled={!newCategory.trim() || addCategoryMutation.isPending}
          onClick={() => addCategoryMutation.mutate(newCategory.trim())}>
          Thêm
        </Button>
      </div>

      <DataTable
        columns={columns}
        data={listQuery.data?.items ?? []}
        isLoading={listQuery.isLoading}
        emptyMessage="Chưa có bài tin nào."
      />

      <ArticleDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        article={editing}
        categories={catQuery.data ?? []}
        onSaved={invalidate}
      />

      <AlertDialog open={Boolean(deleting)} onOpenChange={(o) => !o && setDeleting(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xoá bài tin?</AlertDialogTitle>
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
