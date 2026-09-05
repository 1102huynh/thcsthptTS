package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ADMIN-only account lock/unlock (PUT /v1/users/{id}/enabled — D6, trang
 * quản trị tài khoản). `enabled=false` blocks login going forward (Spring
 * Security's DaoAuthenticationProvider already rejects a UserDetails whose
 * isEnabled() is false — see User.isEnabled(), backed by this same
 * `enabled` column); it does not revoke a JWT already issued (same
 * stateless-token caveat as password change, see F3 in the roadmap).
 */
@Schema(description = "Khoá/mở tài khoản (ADMIN) — enabled=false chặn đăng nhập từ lần sau.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetUserEnabledRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private Boolean enabled;
}
