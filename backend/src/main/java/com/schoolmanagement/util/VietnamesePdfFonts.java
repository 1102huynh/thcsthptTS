package com.schoolmanagement.util;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads a Unicode TrueType font (DejaVu Sans, Bitstream Vera license — see
 * resources/fonts/DejaVuSans-LICENSE.txt) once and exposes ready-to-use
 * {@link Font} instances for every PDF report (3.8).
 *
 * <p>OpenPDF's built-in fonts (Helvetica etc.) only cover Latin-1/CP1252 —
 * they render Vietnamese diacritics (ệ, ố, ủ, ẫ...) as blank boxes or wrong
 * glyphs. A TrueType font must be embedded with Identity-H encoding instead,
 * which is what {@link BaseFont#createFont(String, String, boolean)} with
 * {@code IDENTITY_H} + {@code embedded=true} below does.
 */
@Component
public class VietnamesePdfFonts {

    private final BaseFont baseRegular;
    private final BaseFont baseBold;

    public VietnamesePdfFonts() {
        this.baseRegular = loadBaseFont("/fonts/DejaVuSans.ttf");
        this.baseBold = loadBaseFont("/fonts/DejaVuSans-Bold.ttf");
    }

    private BaseFont loadBaseFont(String classpathLocation) {
        try (InputStream in = new ClassPathResource(classpathLocation).getInputStream()) {
            byte[] fontBytes = in.readAllBytes();
            // BaseFont.createFont needs a resource *name* even when the bytes are
            // supplied directly — it only uses it to pick the font-program parser
            // (by file extension), never to re-read from disk.
            return BaseFont.createFont(classpathLocation, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load bundled Vietnamese PDF font: " + classpathLocation, ex);
        }
    }

    public Font regular(float size) {
        return new Font(baseRegular, size, Font.NORMAL);
    }

    public Font bold(float size) {
        return new Font(baseBold, size, Font.BOLD);
    }

    public Font italic(float size) {
        return new Font(baseRegular, size, Font.ITALIC);
    }
}
