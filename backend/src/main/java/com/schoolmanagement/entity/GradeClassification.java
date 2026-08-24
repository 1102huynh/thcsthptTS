package com.schoolmanagement.entity;

/**
 * Xếp loại học lực — which subset applies depends on which circular (TT22 or
 * TT58) is in effect for a given academicYear/gradeLevel:
 * <ul>
 *   <li>Thông tư 22/2021 (chương trình GDPT 2018): TOT, KHA, DAT, CHUA_DAT</li>
 *   <li>Thông tư 58 (chương trình cũ): GIOI, KHA, TRUNG_BINH, YEU, KEM</li>
 * </ul>
 * The actual score thresholds + môn Toán/Ngữ văn condition that produce a
 * classification are NOT implemented yet — see IMPLEMENTATION_PLAN.md 3.3:
 * "cần người có chuyên môn xác nhận bảng ngưỡng cụ thể trước khi code". This
 * enum only defines the vocabulary so the schema/API shape is ready.
 */
public enum GradeClassification {
    TOT,
    GIOI,
    KHA,
    DAT,
    TRUNG_BINH,
    YEU,
    CHUA_DAT,
    KEM
}
