package com.schoolmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "Login request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(example = "your_username")
    private String username;

    @Schema(example = "your_password")
    private String password;
}

