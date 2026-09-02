import React, { useState } from 'react';
import { z } from 'zod';
import { format } from 'date-fns';
import { toast } from 'sonner';
import { FiBookOpen, FiCheckCircle } from 'react-icons/fi';
import { admissionService } from '../services/dataService';
import { useAppForm, AppForm } from '../components/shared/Form';
import { TextField, DateField } from '../components/shared/FormFields';
import { Button } from '../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card';

// Mirrors SubmitAdmissionRequest's own validation - the backend re-checks
// all of it anyway (phone pattern, dateOfBirth in the past, gradeLevel
// 6-12), but failing the same way client-side means a mistake shows up
// immediately, not after a round-trip.
const schema = z.object({
  applicantName: z.string().min(1, 'Vui lòng nhập họ tên học sinh').max(255),
  dateOfBirth: z.date({ required_error: 'Vui lòng chọn ngày sinh' }).refine((d) => d < new Date(), {
    message: 'Ngày sinh phải là một ngày trong quá khứ',
  }),
  contactPhone: z
    .string()
    .regex(/^(\+84|0)[0-9]{9,10}$/, 'Số điện thoại không hợp lệ, ví dụ 0912345678'),
  desiredGradeLevel: z.coerce
    .number({ invalid_type_error: 'Vui lòng chọn khối' })
    .int()
    .min(6, 'Khối phải từ 6 đến 12')
    .max(12, 'Khối phải từ 6 đến 12'),
  priorSchool: z.string().max(255).optional(),
});

function toPayload(values) {
  return {
    applicantName: values.applicantName.trim(),
    dateOfBirth: format(values.dateOfBirth, 'yyyy-MM-dd'),
    contactPhone: values.contactPhone.trim(),
    desiredGradeLevel: Number(values.desiredGradeLevel),
    priorSchool: values.priorSchool?.trim() || null,
  };
}

/**
 * Public tuyển sinh đầu cấp submission form - no login required, per
 * IMPLEMENTATION_PLAN.md 3.7. Rendered outside AppShell (App.jsx's
 * unauthenticated route branch), same reasoning as LoginPage: its own
 * <main>/<h1> since there's no AppShell wrapper to inherit those from.
 */
function AdmissionApplyPage() {
  const [submitted, setSubmitted] = useState(false);
  const form = useAppForm({
    schema,
    defaultValues: { applicantName: '', dateOfBirth: undefined, contactPhone: '', desiredGradeLevel: '', priorSchool: '' },
  });

  const handleSubmit = async (values) => {
    try {
      await admissionService.submit(toPayload(values));
      setSubmitted(true);
    } catch (err) {
      const message =
        err?.response?.status === 429
          ? 'Bạn đã gửi quá nhiều hồ sơ trong thời gian ngắn. Vui lòng thử lại sau.'
          : err?.response?.data?.message || err?.message || 'Không thể gửi hồ sơ, vui lòng thử lại.';
      toast.error(message);
    }
  };

  const submitting = form.formState.isSubmitting;

  return (
    <main className="flex min-h-screen items-center justify-center bg-gradient-to-br from-primary/90 via-primary to-purple-700 p-4">
      <h1 className="sr-only">Đăng ký tuyển sinh đầu cấp</h1>
      <Card className="w-full max-w-lg shadow-xl">
        <CardHeader className="space-y-1 text-center">
          <div className="mx-auto mb-2 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10">
            <FiBookOpen className="h-6 w-6 text-primary" />
          </div>
          <CardTitle className="text-2xl">Đăng ký tuyển sinh</CardTitle>
          <CardDescription>Điền thông tin học sinh để đăng ký xét tuyển - nhà trường sẽ liên hệ qua số điện thoại</CardDescription>
        </CardHeader>
        <CardContent>
          {submitted ? (
            <div className="flex flex-col items-center gap-3 py-6 text-center">
              <FiCheckCircle className="h-10 w-10 text-green-600 dark:text-green-400" />
              <p className="font-medium">Đã gửi hồ sơ đăng ký thành công!</p>
              <p className="text-sm text-muted-foreground">
                Nhà trường sẽ xem xét và liên hệ với bạn qua số điện thoại đã đăng ký.
              </p>
              <Button variant="outline" onClick={() => { setSubmitted(false); form.reset(); }}>
                Gửi thêm hồ sơ khác
              </Button>
            </div>
          ) : (
            <AppForm form={form} onSubmit={handleSubmit} className="space-y-4" id="admission-apply-form">
              <TextField control={form.control} name="applicantName" label="Họ tên học sinh" placeholder="Nguyễn Văn An" />
              <div className="grid grid-cols-2 gap-4">
                <DateField control={form.control} name="dateOfBirth" label="Ngày sinh" />
                <TextField
                  control={form.control}
                  name="desiredGradeLevel"
                  label="Khối đăng ký"
                  type="number"
                  placeholder="10"
                  description="Khối 6-9 (THCS), 10-12 (THPT)"
                />
              </div>
              <TextField control={form.control} name="contactPhone" label="Số điện thoại liên hệ" placeholder="0912345678" />
              <TextField control={form.control} name="priorSchool" label="Trường cũ (không bắt buộc)" placeholder="THCS Nguyễn Du" />
              <Button type="submit" className="w-full" disabled={submitting}>
                {submitting ? 'Đang gửi...' : 'Gửi hồ sơ đăng ký'}
              </Button>
            </AppForm>
          )}
        </CardContent>
      </Card>
    </main>
  );
}

export default AdmissionApplyPage;
