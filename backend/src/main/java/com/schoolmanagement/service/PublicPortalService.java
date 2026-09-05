package com.schoolmanagement.service;

import com.schoolmanagement.dto.PublicHomeDTO;
import com.schoolmanagement.dto.PublicNewsDTO;
import com.schoolmanagement.dto.SchoolEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates the public home page ({@code GET /v1/public/home}) so the
 * landing page is one request: featured news, latest news, upcoming events.
 * Also builds {@code GET /v1/public/sitemap.xml} (P4) from the same
 * published-content services.
 */
@Service
@Transactional(readOnly = true)
public class PublicPortalService {

    // Static, always-public routes (mirrors PublicLayout.jsx's NAV + the
    // detail-less pages) - kept in sync with frontend/src/App.jsx's public
    // route list.
    private static final String[] STATIC_ROUTES =
            {"/", "/tin-tuc", "/su-kien", "/tuyen-sinh", "/gioi-thieu", "/lien-he"};
    private static final int SITEMAP_MAX_ITEMS = 1000;

    private final NewsService newsService;
    private final SchoolEventService eventService;
    private final String siteUrl;

    // Not @AllArgsConstructor: Lombok's generated all-args constructor
    // doesn't copy the @Value annotation onto its parameter, so Spring's
    // constructor injection would try to autowire siteUrl as a `String` bean
    // and fail to start the application context (see PasswordResetService's
    // constructor for the same reasoning).
    public PublicPortalService(NewsService newsService,
                                SchoolEventService eventService,
                                @Value("${app.frontend.site-url:http://localhost:3000}") String siteUrl) {
        this.newsService = newsService;
        this.eventService = eventService;
        this.siteUrl = siteUrl.endsWith("/") ? siteUrl.substring(0, siteUrl.length() - 1) : siteUrl;
    }

    @Cacheable("publicHome")
    public PublicHomeDTO home() {
        return PublicHomeDTO.builder()
                .featuredNews(newsService.featured(4))
                .latestNews(newsService.latest(6))
                .upcomingEvents(eventService.upcoming(4))
                .build();
    }

    /**
     * {@code urlset} XML per the sitemaps.org protocol: the static public
     * pages plus one {@code <url>} per published news article / event, so
     * search engines discover content the static
     * {@code frontend/public/sitemap.xml} (route-only, built once) can't.
     */
    @Cacheable("publicSitemap")
    public String sitemapXml() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String path : STATIC_ROUTES) {
            appendUrl(xml, siteUrl + path, null);
        }
        for (PublicNewsDTO a : newsService.listPublished(null, PageRequest.of(0, SITEMAP_MAX_ITEMS))) {
            appendUrl(xml, siteUrl + "/tin-tuc/" + a.getSlug(), a.getPublishedAt());
        }
        for (SchoolEventDTO e : eventService.listPublished(null, PageRequest.of(0, SITEMAP_MAX_ITEMS))) {
            appendUrl(xml, siteUrl + "/su-kien/" + e.getSlug(), e.getPublishedAt());
        }
        xml.append("</urlset>\n");
        return xml.toString();
    }

    private static void appendUrl(StringBuilder xml, String loc, java.time.LocalDateTime lastMod) {
        xml.append("  <url>\n    <loc>").append(escapeXml(loc)).append("</loc>\n");
        if (lastMod != null) {
            xml.append("    <lastmod>").append(lastMod.toLocalDate()).append("</lastmod>\n");
        }
        xml.append("  </url>\n");
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
