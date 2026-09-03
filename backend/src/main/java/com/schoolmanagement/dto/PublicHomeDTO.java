package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Everything the public home page needs in one request (keeps the landing
 * page snappy - KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md §4).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicHomeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<PublicNewsDTO> featuredNews;
    private List<PublicNewsDTO> latestNews;
    private List<SchoolEventDTO> upcomingEvents;
}
