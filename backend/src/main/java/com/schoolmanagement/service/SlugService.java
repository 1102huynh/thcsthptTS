package com.schoolmanagement.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Turns a Vietnamese title into a URL-safe slug ("Lễ khai giảng 2026" ->
 * "le-khai-giang-2026") and de-duplicates it against whatever is already
 * stored, so news/event URLs are stable and human-readable.
 */
@Service
public class SlugService {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\p{Alnum}-]");
    private static final Pattern DASHES = Pattern.compile("-{2,}");
    private static final Pattern EDGE_DASH = Pattern.compile("(^-|-$)");

    public String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String s = input.trim().toLowerCase(Locale.forLanguageTag("vi"));
        // đ/Đ aren't decomposed by NFD, handle explicitly.
        s = s.replace('đ', 'd');
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replace(' ', '-');
        s = NON_LATIN.matcher(s).replaceAll("");
        s = DASHES.matcher(s).replaceAll("-");
        s = EDGE_DASH.matcher(s).replaceAll("");
        return s;
    }

    /**
     * @param taken returns true if the candidate slug is already used by
     *              another row - the caller passes a repository existsBySlug
     *              (optionally excluding the current row for an update).
     */
    public String uniqueSlug(String title, Predicate<String> taken) {
        String base = slugify(title);
        if (base.isEmpty()) {
            base = "bai-viet";
        }
        if (!taken.test(base)) {
            return base;
        }
        for (int i = 2; i < 1000; i++) {
            String candidate = base + "-" + i;
            if (!taken.test(candidate)) {
                return candidate;
            }
        }
        // Astronomically unlikely; fall back to a timestamp suffix.
        return base + "-" + System.currentTimeMillis();
    }
}
