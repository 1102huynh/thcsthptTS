-- =====================================================
-- Phase 3.7: Tuyen sinh dau cap (admission applications)
-- =====================================================
-- No backfill - nothing pre-3.7 represented this data.
-- =====================================================

CREATE TABLE `admission_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_name` varchar(255) NOT NULL,
  `date_of_birth` date NOT NULL,
  `contact_phone` varchar(20) NOT NULL,
  `desired_grade_level` int NOT NULL,
  `prior_school` varchar(255) DEFAULT NULL,
  `status` enum('PENDING','REVIEWING','APPROVED','REJECTED') NOT NULL,
  `submitted_at` datetime(6) NOT NULL,
  `reviewed_by_id` bigint DEFAULT NULL,
  `note` text,
  `created_student_id` bigint DEFAULT NULL,
  `version` bigint NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_admission_reviewed_by` (`reviewed_by_id`),
  UNIQUE KEY `uk_admission_created_student` (`created_student_id`),
  CONSTRAINT `fk_admission_reviewed_by` FOREIGN KEY (`reviewed_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_admission_created_student` FOREIGN KEY (`created_student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
