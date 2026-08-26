-- =====================================================
-- Phase 3.6: Phu huynh - Hoc sinh & So lien lac dien tu
-- =====================================================
-- No backfill - nothing pre-3.6 represented parent-student relationships or
-- notifications.
-- =====================================================

CREATE TABLE `parent_student_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `relationship` enum('CHA','ME','NGUOI_GIAM_HO') NOT NULL,
  `is_primary_contact` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_student` (`parent_id`, `student_id`),
  KEY `fk_psr_student` (`student_id`),
  CONSTRAINT `fk_psr_parent` FOREIGN KEY (`parent_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_psr_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `target_type` enum('CLASS','STUDENT','ALL_PARENTS','STAFF') NOT NULL,
  `target_id` bigint DEFAULT NULL,
  `channel` enum('APP','EMAIL','SMS','ZALO') NOT NULL,
  `created_by_id` bigint NOT NULL,
  `sent_at` datetime(6) DEFAULT NULL,
  `status` enum('SENT','PARTIALLY_SENT','FAILED') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_notification_created_by` (`created_by_id`),
  CONSTRAINT `fk_notification_created_by` FOREIGN KEY (`created_by_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notification_recipients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `notification_id` bigint NOT NULL,
  `recipient_id` bigint NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `failure_reason` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_nr_notification` (`notification_id`),
  KEY `fk_nr_recipient` (`recipient_id`),
  CONSTRAINT `fk_nr_notification` FOREIGN KEY (`notification_id`) REFERENCES `notifications` (`id`),
  CONSTRAINT `fk_nr_recipient` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
