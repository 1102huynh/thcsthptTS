package com.schoolmanagement.service;

import com.schoolmanagement.repository.NewsArticleRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Batches public news-detail view counts in memory instead of running one
 * {@code UPDATE} per page view (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md §8/P4:
 * "viewCount tăng bất đồng bộ/gộp để không khoá ghi mỗi lượt xem" - a popular
 * article read by many people at once would otherwise serialize writers on
 * that single row).
 *
 * <p>{@link #recordView(Long)} runs on the public request thread and never
 * touches the database - it only bumps an in-memory {@link LongAdder} (built
 * for exactly this high-contention-single-counter case). {@link
 * #flushToDatabase()} applies the accumulated deltas in one UPDATE per
 * article; it runs on a schedule ({@code app.news.view-count-flush-ms},
 * default 30s) and is also called directly (same thread, same transaction)
 * from integration tests that need to observe the count immediately.
 */
@Service
@AllArgsConstructor
public class NewsViewCountAggregator {

    private static final Logger log = LoggerFactory.getLogger(NewsViewCountAggregator.class);

    private final NewsArticleRepository articleRepository;
    private final Map<Long, LongAdder> pending = new ConcurrentHashMap<>();

    public void recordView(Long articleId) {
        pending.computeIfAbsent(articleId, id -> new LongAdder()).increment();
    }

    @Scheduled(fixedDelayString = "${app.news.view-count-flush-ms:30000}")
    @Transactional
    public void flushToDatabase() {
        if (pending.isEmpty()) {
            return;
        }
        // Remove-then-sum per key so a view recorded while a flush is running
        // lands in the *next* flush instead of racing (and possibly being
        // lost against) this one.
        for (Long articleId : List.copyOf(pending.keySet())) {
            LongAdder adder = pending.remove(articleId);
            long delta = adder == null ? 0 : adder.sum();
            if (delta > 0) {
                articleRepository.incrementViewCountBy(articleId, delta);
            }
        }
        log.debug("Flushed batched news view counts");
    }
}
