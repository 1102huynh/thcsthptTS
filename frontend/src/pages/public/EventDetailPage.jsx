import React from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { FiArrowLeft, FiCalendar, FiMapPin } from 'react-icons/fi';
import publicPortalService, { mediaUrl } from '../../services/publicService';
import Seo from '../../components/public/Seo';
import RichHtml from '../../components/public/RichHtml';

function fmt(value) {
  if (!value) return '';
  return new Date(value).toLocaleString('vi-VN', {
    weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

export default function EventDetailPage() {
  const { slug } = useParams();
  const { data, isLoading, isError } = useQuery({
    queryKey: ['public', 'events', 'detail', slug],
    queryFn: () => publicPortalService.eventDetail(slug),
    retry: false,
  });

  if (isLoading) {
    return <div className="h-96 animate-pulse rounded-xl border bg-muted/50" />;
  }

  if (isError || !data) {
    return (
      <div className="rounded-xl border bg-card p-8 text-center">
        <p className="text-muted-foreground">Không tìm thấy sự kiện.</p>
        <Link to="/su-kien" className="mt-3 inline-flex items-center gap-1 text-primary hover:underline">
          <FiArrowLeft className="h-4 w-4" /> Về danh sách sự kiện
        </Link>
      </div>
    );
  }

  const cover = mediaUrl(data.coverImageUrl);

  return (
    <article className="mx-auto max-w-3xl">
      <Seo title={data.title} description={data.location ? `${fmt(data.startAt)} · ${data.location}` : fmt(data.startAt)} image={cover} type="article" />

      <Link to="/su-kien" className="inline-flex items-center gap-1 text-sm text-primary hover:underline">
        <FiArrowLeft className="h-4 w-4" /> Sự kiện
      </Link>

      <h1 className="mt-3 text-3xl font-bold leading-tight">{data.title}</h1>

      <div className="mt-3 space-y-1 text-sm text-muted-foreground">
        <div className="inline-flex items-center gap-1.5">
          <FiCalendar className="h-4 w-4" />
          {fmt(data.startAt)}
          {data.endAt && ` – ${fmt(data.endAt)}`}
        </div>
        {data.location && (
          <div className="inline-flex items-center gap-1.5">
            <FiMapPin className="h-4 w-4" /> {data.location}
          </div>
        )}
      </div>

      {cover && <img src={cover} alt={data.title} className="mt-6 w-full rounded-xl object-cover" />}

      <div className="mt-6">
        <RichHtml html={data.description} />
      </div>
    </article>
  );
}
