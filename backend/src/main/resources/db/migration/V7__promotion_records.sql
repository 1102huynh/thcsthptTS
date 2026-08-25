-- =====================================================
-- Phase 3.5: Xet len lop / O lai / Tot nghiep (promotion records)
-- =====================================================
-- No default rows in promotion_threshold_configs: the exact cutoffs are a
-- regulation/school-policy detail this migration doesn't assume - set them
-- via POST /v1/promotion-thresholds before previewing/confirming decisions.
-- No backfill in promotion_records - nothing pre-3.5 represented this data.
-- =====================================================

CREATE TABLE `promotion_threshold_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applies_from` varchar(255) NOT NULL,
  `min_subject_average` double NOT NULL,
  `min_conduct` enum('TOT','KHA','TRUNG_BINH','YEU') NOT NULL,
  `max_absence_rate` double NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_promotion_threshold_applies_from` (`applies_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `promotion_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `academic_year_id` bigint NOT NULL,
  `lowest_subject_average_snapshot` double DEFAULT NULL,
  `conduct_snapshot` enum('TOT','KHA','TRUNG_BINH','YEU') DEFAULT NULL,
  `attendance_rate_snapshot` double DEFAULT NULL,
  `decision` enum('LEN_LOP','O_LAI','TOT_NGHIEP','RA_TRUONG') NOT NULL,
  `decision_date` date NOT NULL,
  `decided_by_id` bigint NOT NULL,
  `remarks` text,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_promotion_student_year` (`student_id`, `academic_year_id`),
  KEY `fk_promotion_academic_year` (`academic_year_id`),
  KEY `fk_promotion_decided_by` (`decided_by_id`),
  CONSTRAINT `fk_promotion_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `fk_promotion_academic_year` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`),
  CONSTRAINT `fk_promotion_decided_by` FOREIGN KEY (`decided_by_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
