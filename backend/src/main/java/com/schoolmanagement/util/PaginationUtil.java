package com.schoolmanagement.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Shared helper for list endpoints that accept optional {@code page}/{@code size}
 * query params without breaking existing callers: when either is missing, the
 * endpoint keeps returning the full list (see PaginationUtil.toPageable == null).
 */
public final class PaginationUtil {

    private PaginationUtil() {
    }

    /**
     * @return a {@link Pageable} when both page and size are supplied, otherwise
     *         null (caller should fall back to returning the unpaginated list).
     * @throws IllegalArgumentException if page/size are supplied but invalid
     *         (mapped to a 400 by GlobalExceptionHandler)
     */
    public static Pageable toPageable(Integer page, Integer size) {
        if (page == null || size == null) {
            return null;
        }
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
        return PageRequest.of(page, size);
    }
}
