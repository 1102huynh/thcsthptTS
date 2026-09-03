import React from 'react';
import Seo from '../../components/public/Seo';

// Static introduction. Kept as plain content for v1 - a future iteration
// can back this with a pinned news article or a dedicated CMS "page" type.
export default function AboutPage() {
  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <Seo title="Giới thiệu" description="Giới thiệu về Trường THCS & THPT." />
      <h1 className="text-2xl font-bold">Giới thiệu</h1>

      <div className="space-y-4 leading-relaxed text-foreground/90">
        <p>
          Trường THCS &amp; THPT là cơ sở giáo dục phổ thông đào tạo từ khối 6 đến khối 12, theo
          chương trình của Bộ Giáo dục và Đào tạo.
        </p>
        <p>
          Nội dung mục này đang được nhà trường cập nhật. Vui lòng theo dõi mục{' '}
          <a href="/tin-tuc" className="text-primary underline">Tin tức</a> để biết thông tin mới nhất,
          hoặc liên hệ với nhà trường qua trang{' '}
          <a href="/lien-he" className="text-primary underline">Liên hệ</a>.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-3">
        {[
          { k: 'Khối lớp', v: '6 – 12' },
          { k: 'Năm học', v: 'Tháng 9 – Tháng 5' },
          { k: 'Học kỳ', v: 'HK1 & HK2' },
        ].map((s) => (
          <div key={s.k} className="rounded-xl border bg-card p-4">
            <div className="text-sm text-muted-foreground">{s.k}</div>
            <div className="mt-1 text-lg font-semibold">{s.v}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
