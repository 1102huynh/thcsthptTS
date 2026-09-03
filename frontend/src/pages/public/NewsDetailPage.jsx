import React from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { FiArrowLeft } from 'react-icons/fi';
import publicPortalService, { mediaUrl } from '../../services/publicService';
import Seo from '../../components/public/Seo';
import RichHtml from '../../components/public/RichHtml';
import { formatDate } from '../../components/public/NewsCard';

export default function NewsDetailPage() {
  const { slug } = useParams();
  const { data, isLoading, isError } = useQuery({
    queryKey: ['public', 'news', 'detail', slug],
    queryFn: () => publicPortalService.newsDetail(slug),
    retry: false,
  });

  if (isLoading) {
    return <div className="h-96 animate-pulse rounded-xl border bg-muted/50" />;
  }

  if (isError || !data) {
    return (
      <div className="rounded-xl border bg-card p-8 text-center">
        <p className="text-muted-foreground">Không tìm thấy bài viết.</p>
        <Link to="/tin-tuc" className="mt-3 inline-flex items-center gap-1 text-primary hover:underline">
          <FiArrowLeft className="h-4 w-4" /> Về danh sách tin
        </Link>
      </div>
    );
  }

  const cover = mediaUrl(data.coverImageUrl);

  return (
    <article className="mx-auto max-w-3xl">
      <Seo title={data.title} description={data.summary} image={cover} type="article" />

      <Link to="/tin-tuc" className="inline-flex items-center gap-1 text-sm text-primary hover:underline">
        <FiArrowLeft className="h-4 w-4" /> Tin tức
      </Link>

      <h1 className="mt-3 text-3xl font-bold leading-tight">{data.title}</h1>
      <div className="mt-2 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
        {data.categoryName && (
          <span className="rounded bg-primary/10 px-1.5 py-0.5 font-medium text-primary">{data.categoryName}</span>
        )}
        <span>{formatDate(data.publishedAt)}</span>
        {typeof data.viewCount === 'number' && <span>{data.viewCount} lượt xem</span>}
      </div>

      {cover && (
        <img src={cover} alt={data.title} className="mt-6 w-full rounded-xl object-cover" />
      )}

      {data.summary && <p className="mt-6 text-lg text-muted-foreground">{data.summary}</p>}

      <div className="mt-6">
        <RichHtml html={data.content} />
      </div>
    </article>
  );
}
