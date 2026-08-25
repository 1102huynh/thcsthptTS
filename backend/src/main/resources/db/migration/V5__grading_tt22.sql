-- =====================================================
-- Phase 3.3: Grade records (Thông tư 22/2021, TT58-compatible)
-- =====================================================
-- No backfill from the old `grades` table — that table's percentage/
-- marksObtained-out-of-totalMarks model has no reliable automatic mapping
-- to thang-điểm-10 component scores (MIENG/MUOI_LAM_PHUT/MOT_TIET/GIUA_KY/
-- CUOI_KY); the old Grade entity/table/`/v1/grades` endpoints are left
-- exactly as they were (Phase 1-2 compatibility) rather than replaced —
-- see the GradeRecord entity Javadoc.
--
-- No default rows in grade_component_configs either: which hệ số applies
-- to which component type is a regulation detail this migration doesn't
-- assume — set it via POST /v1/grade-config before entering grades.
-- =====================================================

CREATE TABLE `grade_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  `semester_id` bigint NOT NULL,
  `component_type` enum('MIENG','MUOI_LAM_PHUT','MOT_TIET','GIUA_KY','CUOI_KY') NOT NULL,
  `score` double NOT NULL,
  `teacher_id` bigint NOT NULL,
  `remarks` text,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_gr_student` (`student_id`),
  KEY `fk_gr_subject` (`subject_id`),
  KEY `fk_gr_semester` (`semester_id`),
  KEY `fk_gr_teacher` (`teacher_id`),
  CONSTRAINT `fk_gr_student` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `fk_gr_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `fk_gr_semester` FOREIGN KEY (`semester_id`) REFERENCES `semesters` (`id`),
  CONSTRAINT `fk_gr_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `grade_component_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `component_type` enum('MIENG','MUOI_LAM_PHUT','MOT_TIET','GIUA_KY','CUOI_KY') NOT NULL,
  `weight` int NOT NULL,
  `applies_from` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_grade_component_config_type_applies_from` (`component_type`, `applies_from`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
