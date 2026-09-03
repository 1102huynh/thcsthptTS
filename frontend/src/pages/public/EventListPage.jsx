import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { FiCalendar, FiMapPin } from 'react-icons/fi';
import publicPortalService, { mediaUrl } from '../../services/publicService';
import Seo from '../../components/public/Seo';

const PAGE_SIZE = 12;

function EventCard({ event }) {
  const cover = mediaUrl(event.coverImageUrl);
  return (
    <Link
      to={`/su-kien/${event.slug}`}
      className="group flex gap-4 rounded-xl border bg-card p-4 transition-colors hover:border-primary/40"
    >
      {cover && (
        <img src={cover} alt={event.title} loading="lazy" className="h-24 w-32 shrink-0 rounded-lg object-cover" />
      )}
      <div className="min-w-0">
        <h3 className="font-semibold group-hover:text-primary">{event.title}</h3>
        <div className="mt-1 space-y-1 text-sm text-muted-foreground">
          <div className="inline-flex items-center gap-1">
            <FiCalendar className="h-4 w-4" />
            {new Date(event.startAt).toLocaleString('vi-VN', {
              day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
            })}
          </div>
          {event.location && (
            <div className="inline-flex items-center gap-1">
              <FiMapPin className="h-4 w-4" /> {event.location}
            </div>
          )}
        </div>
      </div>
    </Link>
  );
}

export default function EventListPage() {
  const [when, setWhen] = useState('upcoming');
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: ['public', 'events', when, page],
    queryFn: () => publicPortalService.events({ when, page, size: PAGE_SIZE }),
    placeholderData: keepPreviousData,
  });

  const items = data?.items ?? [];
  const total = data?.total ?? 0;
  const pageCount = Math.max(1, Math.ceil(total / PAGE_SIZE));

  const tab = (value, label) => (
    <button
      type="button"
      onClick={() => { setWhen(value); setPage(0); }}
      className={
        'rounded-full border px-3 py-1 text-sm ' +
        (when === value ? 'border-primary bg-primary/10 text-primary' : 'hover:bg-muted')
      }
    >
      {label}
    </button>
  );

  return (
    <div className="space-y-6">
      <Seo title="Sự kiện" description="Lịch sự kiện, hoạt động của nhà trường." />
      <h1 className="text-2xl font-bold">Sự kiện</h1>

      <div className="flex gap-2">
        {tab('upcoming', 'Sắp tới')}
        {tab('past', 'Đã qua')}
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="h-28 animate-pulse rounded-xl border bg-muted/50" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <p className="text-sm text-muted-foreground">
          {when === 'upcoming' ? 'Chưa có sự kiện sắp tới.' : 'Chưa có sự kiện đã qua.'}
        </p>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {items.map((e) => (
            <EventCard key={e.slug} event={e} />
          ))}
        </div>
      )}

      {pageCount > 1 && (
        <div className="flex items-center justify-center gap-3 pt-4">
          <button
            type="button"
            className="rounded-md border px-3 py-1.5 text-sm disabled:opacity-50"
            disabled={page === 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Trang trước
          </button>
          <span className="text-sm text-muted-foreground">Trang {page + 1} / {pageCount}</span>
          <button
            type="button"
            className="rounded-md border px-3 py-1.5 text-sm disabled:opacity-50"
            disabled={page + 1 >= pageCount}
            onClick={() => setPage((p) => p + 1)}
          >
            Trang sau
          </button>
        </div>
      )}
    </div>
  );
}
