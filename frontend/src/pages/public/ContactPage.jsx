import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiMail, FiMapPin, FiPhone } from 'react-icons/fi';
import publicPortalService from '../../services/publicService';
import Seo from '../../components/public/Seo';

const EMPTY = { fullName: '', email: '', phone: '', subject: '', message: '' };

export default function ContactPage() {
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});

  const mutation = useMutation({
    mutationFn: (payload) => publicPortalService.submitContact(payload),
    onSuccess: () => {
      toast.success('Đã gửi liên hệ. Nhà trường sẽ phản hồi sớm.');
      setForm(EMPTY);
      setErrors({});
    },
    onError: (err) => {
      const status = err?.response?.status;
      if (status === 429) {
        toast.error('Bạn đã gửi quá nhiều lần, vui lòng thử lại sau.');
      } else {
        toast.error(err?.response?.data?.message || 'Không gửi được liên hệ, vui lòng thử lại.');
      }
    },
  });

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const submit = (e) => {
    e.preventDefault();
    const next = {};
    if (!form.fullName.trim()) next.fullName = 'Vui lòng nhập họ tên';
    if (!form.message.trim()) next.message = 'Vui lòng nhập nội dung';
    if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) next.email = 'Email không hợp lệ';
    setErrors(next);
    if (Object.keys(next).length === 0) {
      mutation.mutate({
        fullName: form.fullName.trim(),
        email: form.email.trim() || undefined,
        phone: form.phone.trim() || undefined,
        subject: form.subject.trim() || undefined,
        message: form.message.trim(),
      });
    }
  };

  const field =
    'mt-1 w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary/40';

  return (
    <div className="grid gap-10 md:grid-cols-[1fr_1.2fr]">
      <Seo title="Liên hệ" description="Thông tin liên hệ và gửi tin nhắn cho nhà trường." />

      <div className="space-y-4">
        <h1 className="text-2xl font-bold">Liên hệ</h1>
        <p className="text-muted-foreground">
          Gửi câu hỏi hoặc góp ý tới nhà trường. Với việc nộp hồ sơ tuyển sinh, vui lòng dùng{' '}
          <a href="/tuyen-sinh" className="text-primary underline">trang Tuyển sinh</a>.
        </p>
        <ul className="space-y-3 text-sm">
          <li className="flex items-center gap-2"><FiMapPin className="h-4 w-4 text-primary" /> Địa chỉ: (đang cập nhật)</li>
          <li className="flex items-center gap-2"><FiPhone className="h-4 w-4 text-primary" /> Điện thoại: (đang cập nhật)</li>
          <li className="flex items-center gap-2"><FiMail className="h-4 w-4 text-primary" /> Email: (đang cập nhật)</li>
        </ul>
      </div>

      <form onSubmit={submit} className="space-y-4 rounded-xl border bg-card p-6">
        <div>
          <label className="text-sm font-medium" htmlFor="c-name">Họ và tên *</label>
          <input id="c-name" className={field} value={form.fullName} onChange={set('fullName')} maxLength={150} />
          {errors.fullName && <p className="mt-1 text-xs text-destructive">{errors.fullName}</p>}
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="text-sm font-medium" htmlFor="c-email">Email</label>
            <input id="c-email" type="email" className={field} value={form.email} onChange={set('email')} maxLength={150} />
            {errors.email && <p className="mt-1 text-xs text-destructive">{errors.email}</p>}
          </div>
          <div>
            <label className="text-sm font-medium" htmlFor="c-phone">Điện thoại</label>
            <input id="c-phone" className={field} value={form.phone} onChange={set('phone')} maxLength={30} />
          </div>
        </div>
        <div>
          <label className="text-sm font-medium" htmlFor="c-subject">Tiêu đề</label>
          <input id="c-subject" className={field} value={form.subject} onChange={set('subject')} maxLength={200} />
        </div>
        <div>
          <label className="text-sm font-medium" htmlFor="c-message">Nội dung *</label>
          <textarea id="c-message" rows={5} className={field} value={form.message} onChange={set('message')} maxLength={4000} />
          {errors.message && <p className="mt-1 text-xs text-destructive">{errors.message}</p>}
        </div>
        <button
          type="submit"
          disabled={mutation.isPending}
          className="w-full rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-60"
        >
          {mutation.isPending ? 'Đang gửi...' : 'Gửi liên hệ'}
        </button>
      </form>
    </div>
  );
}
