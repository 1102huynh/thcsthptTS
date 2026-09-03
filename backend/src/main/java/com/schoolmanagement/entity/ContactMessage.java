package com.schoolmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A message submitted through the public "Liên hệ" form
 * ({@code POST /v1/public/contact}, rate-limited). ADMIN/PRINCIPAL read
 * these and mark them {@code handled}.
 */
@Entity
@Table(name = "contact_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Email
    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 30)
    @Column(length = 30)
    private String phone;

    @Size(max = 200)
    @Column(length = 200)
    private String subject;

    @NotBlank
    @Size(max = 4000)
    @Column(nullable = false, length = 4000)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private Boolean handled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
