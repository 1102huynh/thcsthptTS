package com.schoolmanagement.util;

/**
 * Shared helper for the "resolve the config whose appliesFrom year is the
 * latest one still &lt;= this academic year" pattern used by both
 * GradeRecordService (hệ số điểm, 3.3) and PromotionService (ngưỡng xét lên
 * lớp, 3.5) — both parse a leading "-"-delimited year out of an academic
 * year label like "2025-2026" to compare configs scoped by the year they
 * start applying from.
 */
public final class AcademicYearMatcher {

    private AcademicYearMatcher() {
    }

    /**
     * @throws IllegalArgumentException (mapped to 400) if academicYearLabel doesn't
     *         start with a parseable year — e.g. an academic year name or a
     *         config's appliesFrom that isn't in "YYYY-..." form.
     */
    public static int extractStartYear(String academicYearLabel) {
        try {
            return Integer.parseInt(academicYearLabel.trim().split("-")[0].trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot parse a starting year out of: " + academicYearLabel);
        }
    }
}
