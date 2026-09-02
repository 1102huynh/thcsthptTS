import React, { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiEdit2, FiTrash2, FiPlus, FiBookOpen, FiCornerUpLeft, FiUserPlus } from 'react-icons/fi';
import { libraryService } from '../services/dataService';
import DataTable from '../components/shared/DataTable';
import BookFormDialog from './library/BookFormDialog';
import { Button } from '../components/ui/button';
import { Badge } from '../components/ui/badge';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../components/ui/dialog';
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from '../components/ui/alert-dialog';
import { BOOK_CATEGORY_LABELS, BOOK_STATUS_LABELS } from '../lib/enumLabels';

const PAGE_SIZE_OPTIONS = [10, 20, 50];

/**
 * Behaves differently by role, matching what the backend actually allows
 * (LibraryController): ADMIN/LIBRARIAN manage the catalog (add/edit/delete
 * books) but can't borrow/return - that's TEACHER/STUDENT only, and always
 * for themselves (the backend reads the borrower from the JWT, there's no
 * "librarian lends to student X" endpoint). Both land on this same page
 * (see navigation.js), so the column set and toolbar just adapt to which
 * one is signed in rather than being two separate pages.
 */
function LibraryManagement({ user }) {
  const queryClient = useQueryClient();
  const canManage = user?.role === 'ADMIN' || user?.role === 'LIBRARIAN';
  const canBorrow = user?.role === 'TEACHER' || user?.role === 'STUDENT';

  const [search, setSearch] = useState('');
  const [pageIndex, setPageIndex] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [deletingBook, setDeletingBook] = useState(null);
  // "Mượn/trả hộ" (H.2.3) - the book whose desk-lend dialog is open, plus
  // the typed student id. LIBRARIAN can't browse the student directory, so
  // this is a plain "mã học sinh" field rather than a picker.
  const [lendingBook, setLendingBook] = useState(null);
  const [lendStudentId, setLendStudentId] = useState('');

  const booksQuery = useQuery({
    queryKey: ['books'],
    queryFn: () => libraryService.getBooks().then((r) => r.data),
  });

  const myTransactionsQuery = useQuery({
    queryKey: ['my-library-transactions'],
    queryFn: () => libraryService.getMyTransactions().then((r) => r.data),
    enabled: canBorrow,
  });

  const borrowedBookIds = useMemo(() => {
    const active = (myTransactionsQuery.data ?? []).filter(
      (t) => t.transactionType === 'BORROW' && !t.returnDate
    );
    return new Set(active.map((t) => t.bookId));
  }, [myTransactionsQuery.data]);

  const invalidateLibrary = () => {
    queryClient.invalidateQueries({ queryKey: ['books'] });
    queryClient.invalidateQueries({ queryKey: ['my-library-transactions'] });
  };

  const borrowMutation = useMutation({
    mutationFn: (bookId) => libraryService.borrowBook(bookId),
    onSuccess: () => {
      toast.success('Đã mượn sách');
      invalidateLibrary();
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể mượn sách'),
  });

  const returnMutation = useMutation({
    mutationFn: (bookId) => libraryService.returnBook(bookId),
    onSuccess: () => {
      toast.success('Đã trả sách');
      invalidateLibrary();
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể trả sách'),
  });

  const lendMutation = useMutation({
    mutationFn: ({ bookId, studentId }) => libraryService.lendToStudent(bookId, studentId),
    onSuccess: () => {
      toast.success('Đã ghi mượn cho học sinh');
      setLendingBook(null);
      setLendStudentId('');
      queryClient.invalidateQueries({ queryKey: ['books'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể ghi mượn'),
  });

  const returnForMutation = useMutation({
    mutationFn: ({ bookId, studentId }) => libraryService.returnForStudent(bookId, studentId),
    onSuccess: () => {
      toast.success('Đã ghi trả cho học sinh');
      setLendingBook(null);
      setLendStudentId('');
      queryClient.invalidateQueries({ queryKey: ['books'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể ghi trả'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => libraryService.deleteBook(id),
    onSuccess: () => {
      toast.success('Đã xóa sách');
      queryClient.invalidateQueries({ queryKey: ['books'] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa sách'),
    onSettled: () => setDeletingBook(null),
  });

  const filtered = useMemo(() => {
    const rows = booksQuery.data ?? [];
    if (!search.trim()) return rows;
    const q = search.trim().toLowerCase();
    return rows.filter(
      (b) =>
        b.title?.toLowerCase().includes(q) ||
        b.author?.toLowerCase().includes(q) ||
        b.isbn?.toLowerCase().includes(q)
    );
  }, [booksQuery.data, search]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pageRows = filtered.slice(pageIndex * pageSize, (pageIndex + 1) * pageSize);

  const openCreate = () => {
    setEditingBook(null);
    setDialogOpen(true);
  };
  const openEdit = (book) => {
    setEditingBook(book);
    setDialogOpen(true);
  };

  const columns = useMemo(() => {
    const base = [
      { accessorKey: 'title', header: 'Tên sách' },
      { accessorKey: 'author', header: 'Tác giả' },
      {
        accessorKey: 'category',
        header: 'Thể loại',
        cell: ({ getValue }) => BOOK_CATEGORY_LABELS[getValue()] ?? getValue(),
      },
      {
        id: 'copies',
        header: 'Còn lại / Tổng',
        accessorFn: (row) => `${row.availableCopies ?? 0} / ${row.totalCopies ?? 0}`,
      },
      {
        accessorKey: 'status',
        header: 'Tình trạng',
        cell: ({ getValue }) => {
          const status = getValue();
          return (
            <Badge variant={status === 'AVAILABLE' ? 'default' : 'secondary'}>
              {BOOK_STATUS_LABELS[status] ?? status}
            </Badge>
          );
        },
      },
    ];

    if (canManage) {
      base.push({
        id: 'actions',
        header: '',
        cell: ({ row }) => (
          <div className="flex justify-end gap-2">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => { setLendingBook(row.original); setLendStudentId(''); }}
              aria-label="Ghi mượn/trả hộ"
              title="Ghi mượn/trả hộ học sinh"
            >
              <FiUserPlus className="h-4 w-4" />
            </Button>
            <Button variant="ghost" size="icon" onClick={() => openEdit(row.original)} aria-label="Sửa">
              <FiEdit2 className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              className="text-destructive hover:text-destructive"
              onClick={() => setDeletingBook(row.original)}
              aria-label="Xóa"
            >
              <FiTrash2 className="h-4 w-4" />
            </Button>
          </div>
        ),
      });
    } else if (canBorrow) {
      base.push({
        id: 'actions',
        header: '',
        cell: ({ row }) => {
          const book = row.original;
          const isBorrowedByMe = borrowedBookIds.has(book.id);
          if (isBorrowedByMe) {
            return (
              <Button
                variant="outline"
                size="sm"
                onClick={() => returnMutation.mutate(book.id)}
                disabled={returnMutation.isPending}
              >
                <FiCornerUpLeft className="mr-2 h-4 w-4" /> Trả sách
              </Button>
            );
          }
          return (
            <Button
              variant="outline"
              size="sm"
              onClick={() => borrowMutation.mutate(book.id)}
              disabled={borrowMutation.isPending || (book.availableCopies ?? 0) <= 0}
            >
              <FiBookOpen className="mr-2 h-4 w-4" /> Mượn sách
            </Button>
          );
        },
      });
    }

    return base;
  }, [canManage, canBorrow, borrowedBookIds, borrowMutation, returnMutation]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Thư viện</h1>
          <p className="text-sm text-muted-foreground">
            {canManage ? 'Quản lý danh mục sách của trường' : 'Tra cứu và mượn/trả sách'}
          </p>
        </div>
        {canManage && (
          <Button onClick={openCreate}>
            <FiPlus className="mr-2 h-4 w-4" /> Thêm sách
          </Button>
        )}
      </div>

      {booksQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive dark:text-red-400">
          Không tải được danh sách sách.
        </div>
      )}

      <DataTable
        columns={columns}
        data={pageRows}
        isLoading={booksQuery.isLoading}
        emptyMessage="Không tìm thấy sách nào."
        pageIndex={pageIndex}
        pageCount={pageCount}
        pageSize={pageSize}
        totalCount={filtered.length}
        onPageChange={setPageIndex}
        onPageSizeChange={(s) => {
          setPageSize(s);
          setPageIndex(0);
        }}
        pageSizeOptions={PAGE_SIZE_OPTIONS}
        searchValue={search}
        onSearchChange={(v) => {
          setSearch(v);
          setPageIndex(0);
        }}
        searchPlaceholder="Tìm theo tên sách, tác giả, ISBN..."
      />

      {canManage && (
        <>
          {/* key forces a remount (and so a fresh useAppForm defaultValues
              read) whenever the target record changes - react-hook-form
              only applies defaultValues once per component instance, so
              without this, editing record B right after record A (or right
              after a Create) would keep showing A's values. Caught live via
              Playwright: create -> edit showed the just-typed create values
              instead of the book's real ones. */}
          <BookFormDialog
            key={editingBook?.id ?? 'create'}
            open={dialogOpen}
            onOpenChange={setDialogOpen}
            book={editingBook}
          />

          <AlertDialog open={Boolean(deletingBook)} onOpenChange={(open) => !open && setDeletingBook(null)}>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Xóa sách?</AlertDialogTitle>
                <AlertDialogDescription>
                  Bạn sắp xóa "{deletingBook?.title}". Hành động này không thể hoàn tác.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Hủy</AlertDialogCancel>
                <AlertDialogAction
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                  disabled={deleteMutation.isPending}
                  onClick={() => deleteMutation.mutate(deletingBook.id)}
                >
                  {deleteMutation.isPending ? 'Đang xóa...' : 'Xóa'}
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>

          <Dialog
            open={Boolean(lendingBook)}
            onOpenChange={(open) => { if (!open) { setLendingBook(null); setLendStudentId(''); } }}
          >
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Ghi mượn / trả hộ</DialogTitle>
                <DialogDescription>
                  "{lendingBook?.title}" — nhập mã học sinh (ID) để ghi mượn hoặc ghi trả thay cho học sinh tại quầy.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-2">
                <Label htmlFor="lend-student-id">Mã học sinh (ID)</Label>
                <Input
                  id="lend-student-id"
                  type="number"
                  min="1"
                  value={lendStudentId}
                  onChange={(e) => setLendStudentId(e.target.value)}
                  placeholder="Ví dụ: 42"
                />
              </div>
              <DialogFooter className="gap-2 sm:gap-2">
                <Button
                  variant="outline"
                  disabled={!lendStudentId || returnForMutation.isPending}
                  onClick={() => returnForMutation.mutate({ bookId: lendingBook.id, studentId: Number(lendStudentId) })}
                >
                  <FiCornerUpLeft className="mr-2 h-4 w-4" /> Ghi trả
                </Button>
                <Button
                  disabled={!lendStudentId || lendMutation.isPending || (lendingBook?.availableCopies ?? 0) <= 0}
                  onClick={() => lendMutation.mutate({ bookId: lendingBook.id, studentId: Number(lendStudentId) })}
                >
                  <FiBookOpen className="mr-2 h-4 w-4" /> Ghi mượn
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </>
      )}
    </div>
  );
}

export default LibraryManagement;
