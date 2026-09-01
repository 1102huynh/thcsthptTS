import React from 'react';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { feeService } from '@/services/dataService';
import { useAppForm, AppForm } from '@/components/shared/Form';
import { TextField, SelectField } from '@/components/shared/FormFields';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import { PAYMENT_METHOD_LABELS, toOptions } from '@/lib/enumLabels';

const PAYMENT_METHOD_OPTIONS = toOptions(PAYMENT_METHOD_LABELS);

const schema = z.object({
  amount: z.string().min(1, 'Vui lòng nhập số tiền'),
  paymentMethod: z.string().min(1, 'Vui lòng chọn phương thức'),
});

/** open, onOpenChange, fee: FeeDTO the payment is being recorded against */
function PaymentDialog({ open, onOpenChange, fee }) {
  const queryClient = useQueryClient();
  const form = useAppForm({
    schema,
    // Pre-filled with the full remaining balance - "pay in full" is the
    // common case, editable down for a partial payment.
    defaultValues: { amount: fee ? String(fee.remainingAmount ?? fee.amount) : '', paymentMethod: 'CASH' },
  });

  const mutation = useMutation({
    mutationFn: (values) => feeService.processPayment(fee.id, Number(values.amount), values.paymentMethod),
    onSuccess: () => {
      toast.success('Đã ghi nhận thanh toán');
      queryClient.invalidateQueries({ queryKey: ['fees'] });
      onOpenChange(false);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể ghi nhận thanh toán'),
  });

  if (!fee) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Ghi nhận thanh toán</DialogTitle>
          <DialogDescription>
            {fee.feeType} - {fee.studentName} · Còn nợ{' '}
            {new Intl.NumberFormat('vi-VN').format(fee.remainingAmount ?? fee.amount)} ₫
          </DialogDescription>
        </DialogHeader>
        <AppForm form={form} onSubmit={(v) => mutation.mutate(v)} className="space-y-4" id="payment-form">
          <TextField control={form.control} name="amount" label="Số tiền thanh toán (VNĐ)" type="number" min="0" />
          <SelectField control={form.control} name="paymentMethod" label="Phương thức" options={PAYMENT_METHOD_OPTIONS} />
          <DialogFooter className="pt-2">
            <Button type="submit" form="payment-form" disabled={mutation.isPending}>
              {mutation.isPending ? 'Đang lưu...' : 'Xác nhận thanh toán'}
            </Button>
          </DialogFooter>
        </AppForm>
      </DialogContent>
    </Dialog>
  );
}

export default PaymentDialog;
