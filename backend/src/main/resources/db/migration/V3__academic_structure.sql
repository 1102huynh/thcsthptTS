-- =====================================================
-- Phase 3.1: Academic Year / Semester / Subject
-- =====================================================
-- New tables, plus backfill of the FK columns added to `classes` and
-- `students` (academic_year_id/grade_level, current_class_id) from the
-- free-text data that already exists. The old free-text columns
-- (classes.academic_year, students.class_name/section) are kept —
-- @Deprecated on the entities, not dropped here — so nothing already
-- shipped in Phase 1-2 breaks. See KẾ HOẠCH MIGRATION DỮ LIỆU in
-- IMPLEMENTATION_PLAN.md.
-- =====================================================

CREATE TABLE `academic_years` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `status` enum('ACTIVE','CLOSED') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_academic_years_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `semesters` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year_id` bigint NOT NULL,
  `name` enum('HK1','HK2') NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_semesters_year_name` (`academic_year_id`, `name`),
  CONSTRAINT `fk_semesters_academic_year` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `subjects` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `grade_levels` varchar(255) DEFAULT NULL,
  `category` enum('BAT_BUOC','TU_CHON') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subjects_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- New FK columns on existing tables (old free-text columns untouched)
ALTER TABLE `classes`
  ADD COLUMN `academic_year_id` bigint DEFAULT NULL AFTER `academic_year`,
  ADD COLUMN `grade_level` int DEFAULT NULL AFTER `academic_year_id`,
  ADD CONSTRAINT `fk_classes_academic_year` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`);

ALTER TABLE `students`
  ADD COLUMN `current_class_id` bigint DEFAULT NULL AFTER `section`,
  ADD CONSTRAINT `fk_students_current_class` FOREIGN KEY (`current_class_id`) REFERENCES `classes` (`id`);

-- =====================================================
-- Backfill: AcademicYear, derived from every distinct "YYYY-YYYY" label
-- already used in classes/grades/fees. Vietnamese school year ~ Sep -> May.
-- =====================================================
INSERT INTO academic_years (name, start_date, end_date, status, created_at, updated_at)
SELECT DISTINCT
  y.label,
  STR_TO_DATE(CONCAT(SUBSTRING_INDEX(y.label, '-', 1), '-09-01'), '%Y-%m-%d'),
  STR_TO_DATE(CONCAT(SUBSTRING_INDEX(y.label, '-', -1), '-05-31'), '%Y-%m-%d'),
  'ACTIVE',
  NOW(6),
  NOW(6)
FROM (
  SELECT academic_year AS label FROM classes WHERE academic_year REGEXP '^[0-9]{4}-[0-9]{4}$'
  UNION
  SELECT academic_year FROM grades WHERE academic_year REGEXP '^[0-9]{4}-[0-9]{4}$'
  UNION
  SELECT academic_year FROM fees WHERE academic_year REGEXP '^[0-9]{4}-[0-9]{4}$'
) y;

-- Backfill: one HK1 semester per academic year as a starting point (plan:
-- "mặc định Học kỳ 1, cho phép sửa lại thủ công" — an admin can add HK2 and
-- adjust dates via /v1/semesters afterwards).
INSERT INTO semesters (academic_year_id, name, start_date, end_date, created_at, updated_at)
SELECT id, 'HK1', start_date, end_date, NOW(6), NOW(6)
FROM academic_years;

-- Backfill: Subject, derived from every distinct grades.subject value.
-- code is a best-effort slug (upper-case, spaces -> underscores, truncated to
-- fit); duplicates after truncation are silently skipped (INSERT IGNORE) —
-- review/rename via PUT /v1/subjects/{id} afterwards.
INSERT IGNORE INTO subjects (code, name, category, created_at, updated_at)
SELECT DISTINCT
  UPPER(LEFT(REPLACE(TRIM(subject), ' ', '_'), 50)),
  subject,
  'BAT_BUOC',
  NOW(6),
  NOW(6)
FROM grades
WHERE subject IS NOT NULL AND subject != '';

-- Backfill classes.academic_year_id from the matching academic_years row.
UPDATE classes c
JOIN academic_years ay ON ay.name = c.academic_year
SET c.academic_year_id = ay.id;

-- Backfill classes.grade_level where class_name is a plain 6-12 number
-- (e.g. "10"); non-numeric class names are left NULL for manual review.
UPDATE classes
SET grade_level = CAST(class_name AS UNSIGNED)
WHERE class_name REGEXP '^[0-9]+$'
  AND CAST(class_name AS UNSIGNED) BETWEEN 6 AND 12;

-- Backfill students.current_class_id by matching the old free-text
-- class_name+section to a class. If the same class_name+section exists in
-- more than one academic year, MySQL picks one match arbitrarily — fine for
-- the current single-academic-year dataset; review manually otherwise.
UPDATE students s
JOIN classes c ON c.class_name = s.class_name AND c.section = s.section
SET s.current_class_id = c.id
WHERE s.class_name IS NOT NULL AND s.section IS NOT NULL;
