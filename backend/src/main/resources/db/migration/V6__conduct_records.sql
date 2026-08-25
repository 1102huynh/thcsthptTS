-- =====================================================
-- Phase 3.4: Hanh kiem / Ren luyen (conduct records)
-- =====================================================
-- One row per (student, semester) - a student's conduct is re-evaluated,
-- not accumulated, each semester. No backfill: nothing pre-3.4 represented
-- this data.
-- =====================================================

CREATE TABLE `conduct_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `semester_id` bigint NOT NULL,
  `rating` enum('TOT','KHA','TRUNG_BINH','YEU') NOT NULL,
  `remarks` text,
  `evaluated_by_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conduct_student_semester` (`student_id`, `semester_id`),
  KEY `fk_conduct_semester` (`semester_id`),
  KEY `fk_conduct_evaluated_by` (`evaluated_by_id`),
  CONSTRAINT `fk_conduct_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `fk_conduct_semester` FOREIGN KEY (`semester_id`) REFERENCES `semesters` (`id`),
  CONSTRAINT `fk_conduct_evaluated_by` FOREIGN KEY (`evaluated_by_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
