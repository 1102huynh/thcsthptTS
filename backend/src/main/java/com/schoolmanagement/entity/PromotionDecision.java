package com.schoolmanagement.entity;

/**
 * Quyết định xét lên lớp/ở lại/tốt nghiệp cuối năm học, per
 * IMPLEMENTATION_PLAN.md 3.5. Always a human (Hội đồng/ADMIN/PRINCIPAL)
 * decision recorded via POST /v1/promotions/confirm — the system only ever
 * offers LEN_LOP/O_LAI/TOT_NGHIEP as a *suggestion* (see PromotionService);
 * RA_TRUONG (rời trường — chuyển trường, thôi học...) is never auto-suggested.
 */
public enum PromotionDecision {
    LEN_LOP,
    O_LAI,
    TOT_NGHIEP,
    RA_TRUONG
}
