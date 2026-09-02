import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FiSend, FiMail, FiCheck } from 'react-icons/fi';
import {
  notificationService,
  schoolClassService,
  studentService,
  staffService,
} from '../services/dataService';
import { getCurrentUser } from '../services/authService';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Textarea } from '../components/ui/textarea';
import { Badge } from '../components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import {
  NOTIFICATION_TARGET_TYPE_LABELS,
  NOTIFICATION_CHANNEL_LABELS,
  toOptions,
} from '../lib/enumLabels';

const targetTypeOptions = toOptions(NOTIFICATION_TARGET_TYPE_LABELS);
// SMS/ZALO exist as vocabulary (NotificationChannel) but return 501
// (NotificationChannelUnavailableException) until a vendor/Zalo OA
// decision is made - only offer the two channels that actually work,
// per IMPLEMENTATION_PLAN.md 3.6.
const CHANNEL_OPTIONS = toOptions(NOTIFICATION_CHANNEL_LABELS, ['APP', 'EMAIL']);

const emptyForm = { title: '', content: '', targetType: 'ALL_PARENTS', targetId: '', channel: 'APP' };

function ComposeCard() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState(emptyForm);

  const classesQuery = useQuery({ queryKey: ['classes'], queryFn: () => schoolClassService.getAll().then((r) => r.data), enabled: form.targetType === 'CLASS' });
  const studentsQuery = useQuery({ queryKey: ['students'], queryFn: () => studentService.getAll().then((r) => r.data), enabled: form.targetType === 'STUDENT' });
  const staffQuery = useQuery({ queryKey: ['staff-lookup'], queryFn: () => staffService.getAll().then((r) => r.data), enabled: form.targetType === 'STAFF' });

  const needsTarget = form.targetType !== 'ALL_PARENTS';
  const canSubmit = form.title.trim() && form.content.trim() && (!needsTarget || form.targetId);

  const sendMutation = useMutation({
    mutationFn: () =>
      notificationService.createAndSend({
        title: form.title.trim(),
        content: form.content.trim(),
        targetType: form.targetType,
        targetId: needsTarget ? Number(form.targetId) : null,
        channel: form.channel,
      }),
    onSuccess: (res) => {
      const { recipientCount, deliveredCount, status } = res.data;
      if (status === 'FAILED') {
        toast.error(`Gửi thất bại cho cả ${recipientCount} người nhận`);
      } else if (status === 'PARTIALLY_SENT') {
        toast.warning(`Đã gửi ${deliveredCount}/${recipientCount} người nhận`);
      } else {
        toast.success(`Đã gửi tới ${recipientCount} người nhận`);
      }
      queryClient.invalidateQueries({ queryKey: ['my-notifications'] });
      setForm(emptyForm);
    },
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể gửi thông báo'),
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>Soạn thông báo</CardTitle>
        <CardDescription>Gửi tới phụ huynh hoặc nhân viên - lưu và gửi ngay khi nhấn nút</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-1.5">
          <label htmlFor="notif-title" className="text-sm font-medium">Tiêu đề</label>
          <Input id="notif-title" value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} placeholder="VD: Thông báo nghỉ học" />
        </div>
        <div className="space-y-1.5">
          <label htmlFor="notif-content" className="text-sm font-medium">Nội dung</label>
          <Textarea id="notif-content" value={form.content} onChange={(e) => setForm((f) => ({ ...f, content: e.target.value }))} rows={4} placeholder="Nội dung thông báo..." />
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div className="space-y-1.5">
            <label htmlFor="notif-target-type" className="text-sm font-medium">Gửi đến</label>
            <Select
              value={form.targetType}
              onValueChange={(v) => setForm((f) => ({ ...f, targetType: v, targetId: '' }))}
            >
              <SelectTrigger id="notif-target-type">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {targetTypeOptions.map((o) => (
                  <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          {needsTarget && (
            <div className="space-y-1.5">
              <label htmlFor="notif-target-id" className="text-sm font-medium">
                {form.targetType === 'CLASS' ? 'Lớp' : form.targetType === 'STUDENT' ? 'Học sinh' : 'Nhân viên'}
              </label>
              <Select value={form.targetId} onValueChange={(v) => setForm((f) => ({ ...f, targetId: v }))}>
                <SelectTrigger id="notif-target-id">
                  <SelectValue placeholder="Chọn..." />
                </SelectTrigger>
                <SelectContent>
                  {form.targetType === 'CLASS' &&
                    (classesQuery.data ?? []).map((c) => (
                      <SelectItem key={c.id} value={String(c.id)}>{c.className} - {c.section}</SelectItem>
                    ))}
                  {form.targetType === 'STUDENT' &&
                    (studentsQuery.data ?? []).map((s) => (
                      <SelectItem key={s.id} value={String(s.id)}>
                        {s.rollNumber} - {s.user?.firstName} {s.user?.lastName}
                      </SelectItem>
                    ))}
                  {form.targetType === 'STAFF' &&
                    (staffQuery.data ?? []).map((st) => (
                      <SelectItem key={st.id} value={String(st.id)}>{st.user?.firstName} {st.user?.lastName}</SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>
          )}
          <div className="space-y-1.5">
            <label htmlFor="notif-channel" className="text-sm font-medium">Kênh</label>
            <Select value={form.channel} onValueChange={(v) => setForm((f) => ({ ...f, channel: v }))}>
              <SelectTrigger id="notif-channel">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {CHANNEL_OPTIONS.map((o) => (
                  <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">SMS/Zalo chưa khả dụng - đang chờ quyết định nhà cung cấp</p>
          </div>
        </div>
        <div className="flex justify-end">
          <Button onClick={() => sendMutation.mutate()} disabled={!canSubmit || sendMutation.isPending}>
            <FiSend className="mr-2 h-4 w-4" />
            {sendMutation.isPending ? 'Đang gửi...' : 'Gửi thông báo'}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

function InboxCard() {
  const queryClient = useQueryClient();
  const inboxQuery = useQuery({ queryKey: ['my-notifications'], queryFn: () => notificationService.getMy().then((r) => r.data) });

  const readMutation = useMutation({
    mutationFn: (recipientId) => notificationService.markAsRead(recipientId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['my-notifications'] }),
    onError: (err) => toast.error(err?.response?.data?.message || err?.message || 'Không thể đánh dấu đã đọc'),
  });

  const items = inboxQuery.data ?? [];
  const unreadCount = items.filter((n) => !n.readAt).length;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Hộp thư của tôi{unreadCount > 0 && <Badge className="ml-2">{unreadCount} chưa đọc</Badge>}</CardTitle>
        <CardDescription>Thông báo gửi tới bạn</CardDescription>
      </CardHeader>
      <CardContent>
        {inboxQuery.isLoading ? (
          <p className="py-4 text-sm text-muted-foreground">Đang tải...</p>
        ) : items.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">Chưa có thông báo nào.</p>
        ) : (
          <div className="divide-y rounded-md border">
            {items.map((n) => (
              <div key={n.id} className={`p-3 ${!n.readAt ? 'bg-primary/5' : ''}`}>
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="font-medium">{n.title}</p>
                    <p className="mt-1 whitespace-pre-wrap text-sm text-muted-foreground">{n.content}</p>
                    <div className="mt-2 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                      <Badge variant="secondary">
                        <FiMail className="mr-1 h-3 w-3" /> {NOTIFICATION_CHANNEL_LABELS[n.channel] ?? n.channel}
                      </Badge>
                      <span>Từ {n.createdByName}</span>
                      {n.failureReason && <span className="text-destructive dark:text-red-400">Lỗi gửi: {n.failureReason}</span>}
                    </div>
                  </div>
                  {!n.readAt && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => readMutation.mutate(n.id)}
                      disabled={readMutation.isPending}
                    >
                      <FiCheck className="mr-1 h-4 w-4" /> Đánh dấu đã đọc
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function NotificationCenter() {
  const role = getCurrentUser()?.role;
  // NotificationController.createAndSend is ADMIN/PRINCIPAL/TEACHER only;
  // getMy/markAsRead have no @PreAuthorize (open to any authenticated
  // account, staff or PARENT) - so the inbox always shows, compose only
  // for roles that can actually call the endpoint.
  const canCompose = role === 'ADMIN' || role === 'PRINCIPAL' || role === 'TEACHER';

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-semibold">Sổ liên lạc điện tử</h1>
        <p className="text-sm text-muted-foreground">Thông báo trong ứng dụng và email tới phụ huynh, học sinh, nhân viên</p>
      </div>
      {canCompose && <ComposeCard />}
      <InboxCard />
    </div>
  );
}

export default NotificationCenter;
