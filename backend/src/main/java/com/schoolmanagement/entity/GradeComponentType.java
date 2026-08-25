package com.schoolmanagement.entity;

/**
 * Loại điểm theo Thông tư 22/2021/TT-BGDĐT (và tương thích TT58):
 * điểm miệng, 15 phút, 1 tiết, giữa kỳ, cuối kỳ — mỗi loại có hệ số riêng,
 * xem {@link GradeComponentConfig}.
 */
public enum GradeComponentType {
    MIENG,
    MUOI_LAM_PHUT,
    MOT_TIET,
    GIUA_KY,
    CUOI_KY
}
