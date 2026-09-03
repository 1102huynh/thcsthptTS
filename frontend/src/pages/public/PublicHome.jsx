import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { FiArrowRight, FiCalendar, FiMapPin } from 'react-icons/fi';
import publicPortalService from '../../services/publicService';
import Seo from '../../components/public/Seo';
import NewsCard, { formatDate } from '../../components/public/NewsCard';

function EventRow({ event }) {
  return (
    <Link
      to={`/su-kien/${event.slug}`}
      className="flex flex-col gap-1 rounded-lg border bg-card p-4 transition-colors hover:border-primary/40"
    >
      <span className="font-medium">{event.title}</span>
      <span className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-muted-foreground">
        <span className="inline-flex items-center gap-1">
          <FiCalendar className="h-4 w-4" />
          {new Date(event.startAt).toLocaleString('vi-VN', {
            day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
          })}
        </span>
        {event.location && (
          <span className="inline-flex items-center gap-1">
            <FiMapPin className="h-4 w-4" /> {event.location}
          </span>
        )}
      </span>
    </Link>
  );
}

export default function PublicHome() {
  const { data, isLoading } = useQuery({
    queryKey: ['public', 'home'],
    queryFn: () => publicPortalService.home(),
  });

  const featured = data?.featuredNews ?? [];
  const latest = data?.latestNews ?? [];
  const events = data?.upcomingEvents ?? [];

  return (
    <div className="space-y-12">
      <Seo title={null} description="Tin tức, sự kiện và thông tin tuyển sinh của Trường THCS & THPT." />

      {/* Hero */}
      <section className="rounded-2xl border bg-gradient-to-br from-primary/10 via-background to-background p-8 sm:p-12">
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">Trường THCS &amp; THPT</h1>
        <p className="mt-3 max-w-2xl text-muted-foreground">
          Cổng thông tin chính thức: tin tức, hoạt động, sự kiện và thông tin tuyển sinh đầu cấp.
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link
            to="/tuyen-sinh"
            className="inline-flex items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
          >
            Thông tin tuyển sinh <FiArrowRight className="h-4 w-4" />
          </Link>
          <Link
            to="/tin-tuc"
            className="inline-flex items-center gap-2 rounded-md border px-4 py-2 text-sm font-medium hover:bg-muted"
          >
            Xem tin tức
          </Link>
        </div>
      </section>

      {/* Featured / latest news */}
      <section>
        <div className="mb-4 flex items-end justify-between">
          <h2 className="text-xl font-semibold">Tin nổi bật</h2>
          <Link to="/tin-tuc" className="text-sm text-primary hover:underline">Tất cả tin →</Link>
        </div>
        {isLoading ? (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {[0, 1, 2].map((i) => (
              <div key={i} className="h-64 animate-pulse rounded-xl border bg-muted/50" />
            ))}
          </div>
        ) : (featured.length || latest.length) ? (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {(featured.length ? featured : latest).slice(0, 6).map((a) => (
              <NewsCard key={a.slug} article={a} />
            ))}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">Chưa có tin tức.</p>
        )}
      </section>

      {latest.length > 0 && featured.length > 0 && (
        <section>
          <h2 className="mb-4 text-xl font-semibold">Tin mới nhất</h2>
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {latest.slice(0, 6).map((a) => (
              <NewsCard key={a.slug} article={a} />
            ))}
          </div>
        </section>
      )}

      {/* Upcoming events */}
      <section>
        <div className="mb-4 flex items-end justify-between">
          <h2 className="text-xl font-semibold">Sự kiện sắp tới</h2>
          <Link to="/su-kien" className="text-sm text-primary hover:underline">Tất cả sự kiện →</Link>
        </div>
        {events.length ? (
          <div className="grid gap-4 sm:grid-cols-2">
            {events.map((e) => (
              <EventRow key={e.slug} event={e} />
            ))}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">Chưa có sự kiện sắp tới.</p>
        )}
      </section>
    </div>
  );
}
