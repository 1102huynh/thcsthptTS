import React from 'react';
import { z } from 'zod';
import { format } from 'date-fns';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { libraryService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField, TextareaField, DateField } from '@/components/shared/FormFields';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import { BOOK_CATEGORY_LABELS, BOOK_STATUS_LABELS, toOptions } from '@/lib/enumLabels';
import { parseLocalDate } from '@/lib/dates';

const CATEGORY_OPTIONS = toOptions(BOOK_CATEGORY_LABELS);
const STATUS_OPTIONS = toOptions(BOOK_STATUS_LABELS);

const schema = z.object({
  isbn: z.string().min(1, 'Vui lòng nhập ISBN'),
  title: z.string().min(1, 'Vui lòng nhập tên sách'),
  author: z.string().min(1, 'Vui lòng nhập tác giả'),
  publisher: z.string().optional(),
  publicationYear: z.string().optional(),
  edition: z.string().optional(),
  category: z.string().min(1, 'Vui lòng chọn thể loại'),
  totalCopies: z.string().min(1, 'Vui lòng nhập tổng số bản'),
  availableCopies: z.string().optional(),
  description: z.string().optional(),
  locationRack: z.string().optional(),
  callNumber: z.string().optional(),
  status: z.string().min(1, 'Vui lòng chọn tình trạng'),
  price: z.string().optional(),
  acquisitionDate: z.date().optional(),
});

function toBookPayload(values, isEdit) {
  const totalCopies = Number(values.totalCopies);
  // On create, a fresh book's copies are all available by default - the
  // librarian only needs to correct availableCopies later (Edit) once some
  // are actually out. On edit, respect whatever they typed.
  const availableCopies =
    values.availableCopies !== '' && values.availableCopies != null
      ? Number(values.availableCopies)
      : isEdit
        ? undefined
        : totalCopies;

  return {
    isbn: values.isbn,
    title: values.title,
    author: values.author,
    publisher: values.publisher || null,
    publicationYear: values.publicationYear ? Number(values.publicationYear) : null,
    edition: values.edition || null,
    category: values.category,
    totalCopies,
    availableCopies,
    description: values.description || null,
    locationRack: values.locationRack || null,
    callNumber: values.callNumber || null,
    status: values.status,
    price: values.price ? Number(values.price) : null,
    // LibraryBook.acquisitionDate is a LocalDateTime, not LocalDate -
    // Jackson's default deserializer needs a full ISO datetime string.
    acquisitionDate: values.acquisitionDate ? format(values.acquisitionDate, "yyyy-MM-dd'T'00:00:00") : null,
  };
}

function BookFieldsFragment({ control }) {
  return (
    <>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="title" label="Tên sách" />
        <TextField control={control} name="author" label="Tác giả" />
      </div>
      <div className="grid grid-cols-2 gap-4">
        <TextField control={control} name="isbn" label="ISBN" />
        <SelectField control={control} name="category" label="Thể loại" options={CATEGORY_OPTIONS} />
      </div>
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="publisher" label="Nhà xuất bản" />
        <TextField control={control} name="publicationYear" label="Năm XB" type="number" />
        <TextField control={control} name="edition" label="Phiên bản" />
      </div>
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="totalCopies" label="Tổng số bản" type="number" min="0" />
        <TextField control={control} name="availableCopies" label="Số bản còn lại" type="number" min="0" />
        <SelectField control={control} name="status" label="Tình trạng" options={STATUS_OPTIONS} />
      </div>
      <div className="grid grid-cols-3 gap-4">
        <TextField control={control} name="locationRack" label="Vị trí kệ" />
        <TextField control={control} name="callNumber" label="Số ký hiệu" />
        <TextField control={control} name="price" label="Giá (VNĐ)" type="number" min="0" />
      </div>
      <DateField control={control} name="acquisitionDate" label="Ngày nhập sách" />
      <TextareaField control={control} name="description" label="Mô tả" />
    </>
  );
}

/** open: boolean, onOpenChange: fn, book: null (create mode) | LibraryBookDTO (edit mode) */
function BookFormDialog({ open, onOpenChange, book }) {
  const isEdit = Boolean(book);
  const queryClient = useQueryClient();
  const form = useAppForm({
    schema,
    defaultValues: {
      isbn: book?.isbn ?? '',
      title: book?.title ?? '',
      author: book?.author ?? '',
      publisher: book?.publisher ?? '',
      publicationYear: book?.publicationYear != null ? String(book.publicationYear) : '',
      edition: book?.edition ?? '',
      category: book?.category ?? '',
      totalCopies: book?.totalCopies != null ? String(book.totalCopies) : '',
      availableCopies: book?.availableCopies != null ? String(book.availableCopies) : '',
      description: book?.description ?? '',
      locationRack: book?.locationRack ?? '',
      callNumber: book?.callNumber ?? '',
      status: book?.status ?? 'AVAILABLE',
      price: book?.price != null ? String(book.price) : '',
      acquisitionDate: parseLocalDate(book?.acquisitionDate?.slice(0, 10)),
    },
  });

  const mutation = useMutation({
    mutationFn: (values) => {
      const payload = toBookPayload(values, isEdit);
      return isEdit ? libraryService.updateBook(book.id, payload) : libraryService.createBook(payload);
    },
    onSuccess: () => {
      toast.success(isEdit ? 'Đã cập nhật thông tin sách' : 'Đã thêm sách mới');
      queryClient.invalidateQueries({ queryKey: ['books'] });
      onOpenChange(false);
    },
    onError: (err) => {
      toast.error(err?.response?.data?.message || err?.message || 'Không thể lưu thông tin sách');
    },
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[85vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Sửa thông tin sách' : 'Thêm sách mới'}</DialogTitle>
          <DialogDescription>
            {isEdit ? `Cập nhật thông tin "${book.title}"` : 'Thêm một đầu sách mới vào thư viện'}
          </DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="book-form">
          <BookFieldsFragment control={form.control} />
          <DialogFooter className="pt-2">
            <Button type="submit" form="book-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm sách'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default BookFormDialog;
