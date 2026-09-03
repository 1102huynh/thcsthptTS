package com.schoolmanagement.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

/**
 * Cleans CMS-authored rich-text HTML (news {@code content}, event
 * {@code description}) down to a safe allow-list before it is stored, so the
 * public portal can render it directly. Public XSS via authored content is
 * the portal module's top risk (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md §8).
 *
 * <p>Allows the formatting a school notice actually needs - headings,
 * paragraphs, lists, bold/italic, links, tables, images - and drops
 * everything else: {@code <script>}, event handlers, {@code javascript:}
 * URLs, {@code <style>}, {@code <iframe>}, etc.
 */
@Service
public class HtmlSanitizerService {

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)          // <a href> - forces rel=nofollow, http(s)/mailto only
            .and(Sanitizers.TABLES)
            .and(Sanitizers.IMAGES)        // <img src> - http(s) only
            .and(new HtmlPolicyBuilder()
                    .allowElements("h1", "h2", "h3", "h4", "figure", "figcaption", "hr")
                    .allowAttributes("class").onElements("p", "span", "div", "figure", "figcaption", "table")
                    .toFactory());

    public String sanitize(String rawHtml) {
        if (rawHtml == null) {
            return null;
        }
        return POLICY.sanitize(rawHtml);
    }
}
