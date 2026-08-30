package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "Public — no login required. Always responds the same way regardless of whether the email matches an account, to avoid leaking which emails are registered.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(example = "student1@school.com")
    @NotBlank
    @Email
    private String email;
}
