package com.schoolmanagement.dto;

import com.schoolmanagement.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Returned by register/login/refresh-token. `accessToken` is sent as `Authorization: Bearer <accessToken>` on subsequent requests.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    @Schema(description = "JWT access token — null on the register response, present on login/refresh-token")
    private String accessToken;

    private String refreshToken;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    @Schema(example = "Bearer")
    private String tokenType;
}

