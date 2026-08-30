-- =====================================================
-- Phase 3.9: Hạ tầng dùng chung (document attachments,
-- audit log, forgot/reset password).
-- =====================================================
-- No backfill in any of these three - nothing pre-3.9 represented file
-- attachments, an audit trail, or password-reset tokens.
-- =====================================================

CREATE TABLE `document_attachments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `owner_type` enum('STUDENT','STAFF','ADMISSION_APPLICATION') NOT NULL,
  `owner_id` bigint NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `file_type` varchar(100) NOT NULL,
  `file_size` bigint NOT NULL,
  `uploaded_by_id` bigint NOT NULL,
  `uploaded_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_document_attachments_stored_file_name` (`stored_file_name`),
  KEY `idx_document_attachments_owner` (`owner_type`, `owner_id`),
  KEY `fk_document_attachments_uploaded_by` (`uploaded_by_id`),
  CONSTRAINT `fk_document_attachments_uploaded_by` FOREIGN KEY (`uploaded_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `actor_id` bigint NOT NULL,
  `action` varchar(50) NOT NULL,
  `entity_type` varchar(100) NOT NULL,
  `entity_id` bigint DEFAULT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `detail_json` text,
  PRIMARY KEY (`id`),
  KEY `idx_audit_logs_entity` (`entity_type`, `entity_id`),
  KEY `idx_audit_logs_actor` (`actor_id`),
  KEY `idx_audit_logs_occurred_at` (`occurred_at`),
  CONSTRAINT `fk_audit_logs_actor` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `password_reset_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  -- SHA-256 hash of the token, not the raw token - a DB leak alone must not
  -- be enough to reset anyone's password. The raw token only ever exists in
  -- the outgoing email and the incoming reset request.
  `token_hash` varchar(64) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_password_reset_tokens_token_hash` (`token_hash`),
  KEY `fk_password_reset_tokens_user` (`user_id`),
  CONSTRAINT `fk_password_reset_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
