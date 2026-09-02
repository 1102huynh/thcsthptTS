import React, { useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiUpload, FiDownload, FiTrash2, FiFile } from 'react-icons/fi';
import { documentService } from '@/services/dataService';
import { triggerBlobDownload } from '@/lib/download';
import { getCurrentUser } from '@/services/authService';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/components/ui/dialog';
import {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
} from '@/components/ui/alert-dialog';

const MAX_SIZE_BYTES = 10 * 1024 * 1024; // matches DocumentController's own 10MB limit
const ACCEPTED_EXTENSIONS = ['.pdf', '.jpg', '.jpeg', '.png', '.doc', '.docx', '.xls', '.xlsx'];

function formatFileSize(bytes) {
  if (bytes == null) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 102.4) / 10} KB`;
  return `${Math.round(bytes / (1024 * 102.4)) / 10} MB`;
}

/**
 * Shared upload/list/download/delete panel for DocumentAttachment
 * (IMPLEMENTATION_PLAN.md 3.9) - one dialog reused across every owner type
 * (Student, Staff, AdmissionApplication) rather than 3 near-identical
 * copies, since DocumentController's shape is the same for all of them.
 *
 * open, onOpenChange, ownerType: DocumentOwnerType, ownerId, ownerLabel
 */
function DocumentsDialog({ open, onOpenChange, ownerType, ownerId, ownerLabel }) {
  const queryClient = useQueryClient();
  const role = getCurrentUser()?.role;
  // Matches DocumentController.delete's @PreAuthorize exactly.
  const canDelete = role === 'ADMIN' || role === 'PRINCIPAL';
  const fileInputRef = useRef(null);
  const [deletingDoc, setDeletingDoc] = useState(null);

  const docsQuery = useQuery({
    queryKey: ['documents', ownerType, ownerId],
    queryFn: () => documentService.listByOwner(ownerType, ownerId).then((r) => r.data),
    enabled: open && Boolean(ownerId),
  });

  const uploadMutation = useMutation({
    mutationFn: (file) => documentService.upload(file, ownerType, ownerId),
    onSuccess: () => {
      toast.success('Đã tải lên tệp đính kèm');
      queryClient.invalidateQueries({ queryKey: ['documents', ownerType, ownerId] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể tải lên tệp'),
    onSettled: () => {
      if (fileInputRef.current) fileInputRef.current.value = '';
    },
  });

  const downloadMutation = useMutation({
    mutationFn: (doc) => triggerBlobDownload(documentService.download(doc.id), doc.fileName),
    onError: (err) => toast.error(err.message || 'Không thể tải xuống tệp'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => documentService.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa tệp đính kèm');
      queryClient.invalidateQueries({ queryKey: ['documents', ownerType, ownerId] });
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể xóa tệp'),
    onSettled: () => setDeletingDoc(null),
  });

  const handleFilePicked = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > MAX_SIZE_BYTES) {
      toast.error('Tệp vượt quá 10MB');
      e.target.value = '';
      return;
    }
    uploadMutation.mutate(file);
  };

  const docs = docsQuery.data ?? [];

  return (
    <>
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Tệp đính kèm</DialogTitle>
            <DialogDescription>{ownerLabel}</DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div>
              <input
                ref={fileInputRef}
                type="file"
                accept={ACCEPTED_EXTENSIONS.join(',')}
                onChange={handleFilePicked}
                className="hidden"
                id="document-upload-input"
              />
              <Button
                variant="outline"
                onClick={() => fileInputRef.current?.click()}
                disabled={uploadMutation.isPending}
              >
                <FiUpload className="mr-2 h-4 w-4" />
                {uploadMutation.isPending ? 'Đang tải lên...' : 'Tải lên tệp mới'}
              </Button>
              <p className="mt-1 text-xs text-muted-foreground">
                Tối đa 10MB - PDF, JPG, PNG, DOC, DOCX, XLS, XLSX
              </p>
            </div>

            {docsQuery.isLoading ? (
              <p className="py-4 text-center text-sm text-muted-foreground">Đang tải...</p>
            ) : docs.length === 0 ? (
              <p className="py-6 text-center text-sm text-muted-foreground">Chưa có tệp đính kèm nào.</p>
            ) : (
              <div className="divide-y rounded-md border">
                {docs.map((doc) => (
                  <div key={doc.id} className="flex items-center justify-between gap-3 p-3">
                    <div className="flex min-w-0 items-center gap-2">
                      <FiFile className="h-4 w-4 shrink-0 text-muted-foreground" />
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium" title={doc.fileName}>{doc.fileName}</p>
                        <p className="text-xs text-muted-foreground">
                          {formatFileSize(doc.fileSize)} · {doc.uploadedByName ?? 'Không rõ'}
                        </p>
                      </div>
                    </div>
                    <div className="flex shrink-0 items-center gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => downloadMutation.mutate(doc)}
                        disabled={downloadMutation.isPending}
                        aria-label={`Tải xuống ${doc.fileName}`}
                      >
                        <FiDownload className="h-4 w-4" />
                      </Button>
                      {canDelete && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-destructive hover:text-destructive"
                          onClick={() => setDeletingDoc(doc)}
                          aria-label={`Xóa ${doc.fileName}`}
                        >
                          <FiTrash2 className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      <AlertDialog open={Boolean(deletingDoc)} onOpenChange={(o) => !o && setDeletingDoc(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa tệp đính kèm?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn sắp xóa "{deletingDoc?.fileName}". Hành động này không thể hoàn tác.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteMutation.isPending}
              onClick={() => deleteMutation.mutate(deletingDoc.id)}
            >
              {deleteMutation.isPending ? 'Đang xóa...' : 'Xóa'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}

export default DocumentsDialog;
