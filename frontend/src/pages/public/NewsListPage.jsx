import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery, keepPreviousData } from '@tanstack/react-query';
import publicPortalService from '../../services/publicService';
import Seo from '../../components/public/Seo';
import NewsCard from '../../components/public/NewsCard';

const PAGE_SIZE = 12;

export default function NewsListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const category = searchParams.get('category') || '';
  const [page, setPage] = useState(0);

  const categoriesQuery = useQuery({
    queryKey: ['public', 'news-categories'],
    queryFn: () => publicPortalService.newsCategories(),
    staleTime: 10 * 60 * 1000,
  });

  const newsQuery = useQuery({
    queryKey: ['public', 'news', category, page],
    queryFn: () => publicPortalService.news({ category, page, size: PAGE_SIZE }),
    placeholderData: keepPreviousData,
  });

  const items = newsQuery.data?.items ?? [];
  const total = newsQuery.data?.total ?? 0;
  const pageCount = Math.max(1, Math.ceil(total / PAGE_SIZE));

  const selectCategory = (slug) => {
    setPage(0);
    setSearchParams(slug ? { category: slug } : {});
  };

  return (
    <div className="space-y-6">
      <Seo title="Tin tức" description="Tin tức và thông báo mới nhất của nhà trường." />
      <h1 className="text-2xl font-bold">Tin tức</h1>

      <div className="flex flex-wrap gap-2">
        <button
          type="button"
          onClick={() => selectCategory('')}
          className={
            'rounded-full border px-3 py-1 text-sm ' +
            (!category ? 'border-primary bg-primary/10 text-primary' : 'hover:bg-muted')
          }
        >
          Tất cả
        </button>
        {(categoriesQuery.data ?? []).map((c) => (
          <button
            key={c.slug}
            type="button"
            onClick={() => selectCategory(c.slug)}
            className={
              'rounded-full border px-3 py-1 text-sm ' +
              (category === c.slug ? 'border-primary bg-primary/10 text-primary' : 'hover:bg-muted')
            }
          >
            {c.name}
          </button>
        ))}
      </div>

      {newsQuery.isLoading ? (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-64 animate-pulse rounded-xl border bg-muted/50" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <p className="text-sm text-muted-foreground">Chưa có tin nào trong mục này.</p>
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {items.map((a) => (
            <NewsCard key={a.slug} article={a} />
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
