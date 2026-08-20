package com.schoolmanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGeneralException_hidesOriginalExceptionDetailsFromClient() {
        // Simulate an internal error that would otherwise leak implementation
        // details (e.g. a SQL error message, class name, file path) to the client.
        String sensitiveDetail = "Duplicate entry 'admin' for key 'users.UK_username' at com.mysql.cj.jdbc.exceptions.MySQLIntegrityConstraintViolationException";
        RuntimeException ex = new RuntimeException(sensitiveDetail);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/students");
        when(request.getMethod()).thenReturn("POST");

        ResponseEntity<ApiError> response = handler.handleGeneralException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo(500);
        assertThat(body.getMessage())
                .isEqualTo("Đã có lỗi xảy ra, vui lòng thử lại sau.")
                .doesNotContain(sensitiveDetail)
                .doesNotContain("MySQLIntegrityConstraintViolationException")
                .doesNotContain(ex.getClass().getName());
    }
}
