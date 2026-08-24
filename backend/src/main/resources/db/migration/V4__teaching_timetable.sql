-- =====================================================
-- Phase 3.2: Teaching assignments (phân công giảng dạy) + Timetable
-- (thời khoá biểu)
-- =====================================================
-- No backfill needed — nothing in the pre-3.2 schema represented
-- teacher-subject-class assignments or weekly schedule slots.
--
-- Note: teacher/room/class double-booking (same semester + day_of_week +
-- period) can't be expressed as a plain UNIQUE constraint here since the
-- teacher/room/class all live behind teaching_assignment_id, not as direct
-- columns on timetable_slots — enforced instead in TimetableService via
-- TimetableSlotRepository.existsTeacherConflict/existsRoomConflict/
-- existsClassConflict before every insert/update.
-- =====================================================

CREATE TABLE `teaching_assignments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `school_class_id` bigint NOT NULL,
  `subject_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  `semester_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teaching_assignment_class_subject_semester` (`school_class_id`, `subject_id`, `semester_id`),
  KEY `fk_ta_subject` (`subject_id`),
  KEY `fk_ta_teacher` (`teacher_id`),
  KEY `fk_ta_semester` (`semester_id`),
  CONSTRAINT `fk_ta_school_class` FOREIGN KEY (`school_class_id`) REFERENCES `classes` (`id`),
  CONSTRAINT `fk_ta_subject` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`),
  CONSTRAINT `fk_ta_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `staff` (`id`),
  CONSTRAINT `fk_ta_semester` FOREIGN KEY (`semester_id`) REFERENCES `semesters` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `timetable_slots` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `teaching_assignment_id` bigint NOT NULL,
  `day_of_week` int NOT NULL,
  `period` int NOT NULL,
  `room` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_ts_teaching_assignment` (`teaching_assignment_id`),
  CONSTRAINT `fk_ts_teaching_assignment` FOREIGN KEY (`teaching_assignment_id`) REFERENCES `teaching_assignments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
