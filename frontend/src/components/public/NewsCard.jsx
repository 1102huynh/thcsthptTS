import React from 'react';
import { Link } from 'react-router-dom';
import { mediaUrl } from '../../services/publicService';

function formatDate(value) {
  if (!value) return '';
  try {
    return new Date(value).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch {
    return '';
  }
}

export default function NewsCard({ article }) {
  const cover = mediaUrl(article.coverImageUrl);
  return (
    <Link
      to={`/tin-tuc/${article.slug}`}
      className="group flex flex-col overflow-hidden rounded-xl border bg-card transition-shadow hover:shadow-md"
    >
      <div className="aspect-[16/9] w-full overflow-hidden bg-muted">
        {cover ? (
          <img
            src={cover}
            alt={article.title}
            loading="lazy"
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-muted-foreground">Không có ảnh</div>
        )}
      </div>
      <div className="flex flex-1 flex-col p-4">
        <div className="mb-1 flex items-center gap-2 text-xs text-muted-foreground">
          {article.categoryName && (
            <span className="rounded bg-primary/10 px-1.5 py-0.5 font-medium text-primary">{article.categoryName}</span>
          )}
          <span>{formatDate(article.publishedAt)}</span>
        </div>
        <h3 className="line-clamp-2 font-semibold group-hover:text-primary">{article.title}</h3>
        {article.summary && (
          <p className="mt-1 line-clamp-3 text-sm text-muted-foreground">{article.summary}</p>
        )}
      </div>
    </Link>
  );
}

export { formatDate };
