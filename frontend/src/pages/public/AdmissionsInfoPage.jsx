import React from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { FiArrowRight } from 'react-icons/fi';
import publicPortalService from '../../services/publicService';
import Seo from '../../components/public/Seo';
import NewsCard from '../../components/public/NewsCard';

// The dedicated admissions landing: a strong CTA to the existing public
// application form (/apply -> POST /v1/admissions), plus every news article
// filed under the "tuyen-sinh" category.
export default function AdmissionsInfoPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['public', 'news', 'tuyen-sinh', 0],
    queryFn: () => publicPortalService.news({ category: 'tuyen-sinh', page: 0, size: 12 }),
  });

  const items = data?.items ?? [];

  return (
    <div className="space-y-8">
      <Seo title="Tuyển sinh" description="Thông tin tuyển sinh đầu cấp và nộp hồ sơ trực tuyến." />

      <section className="rounded-2xl border bg-gradient-to-br from-primary/10 via-background to-background p-8">
        <h1 className="text-3xl font-bold">Tuyển sinh đầu cấp</h1>
        <p className="mt-3 max-w-2xl text-muted-foreground">
          Phụ huynh nộp hồ sơ trực tuyến. Sau khi nhà trường xét duyệt, tài khoản học sinh sẽ được tạo
          và thông báo tới phụ huynh.
        </p>
        <Link
          to="/apply"
          className="mt-6 inline-flex items-center gap-2 rounded-md bg-primary px-5 py-2.5 text-sm font-medium text-primary-foreground hover:bg-primary/90"
        >
          Nộp hồ sơ trực tuyến <FiArrowRight className="h-4 w-4" />
        </Link>
      </section>

      <section>
        <h2 className="mb-4 text-xl font-semibold">Thông báo tuyển sinh</h2>
        {isLoading ? (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {[0, 1, 2].map((i) => (
              <div key={i} className="h-64 animate-pulse rounded-xl border bg-muted/50" />
            ))}
          </div>
        ) : items.length === 0 ? (
          <p className="text-sm text-muted-foreground">Chưa có thông báo tuyển sinh.</p>
        ) : (
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {items.map((a) => (
              <NewsCard key={a.slug} article={a} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
