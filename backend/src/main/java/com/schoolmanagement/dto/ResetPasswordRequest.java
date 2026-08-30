package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "Public — the token from the reset-password email link.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String token;

    @Schema(example = "N3wStr0ngPassw0rd!", minLength = 8)
    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
