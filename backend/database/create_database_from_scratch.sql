-- ============================================================================
-- school_management — full database bootstrap (schema only, no seed data)
-- ============================================================================
-- Mục đích: dựng lại toàn bộ database từ số 0 trên một máy MySQL 8.0 mới toanh
-- (ví dụ sau khi cài lại laptop), KHÔNG cần build/chạy app Spring Boot trước.
-- File này là bản gộp thủ công, đúng thứ tự, của toàn bộ migration hiện có ở
-- backend/src/main/resources/db/migration/V1..V9 — tại thời điểm tạo file
-- (2026-08-30) đó là migration mới nhất đã merge vào main. Nếu sau này có
-- thêm V10, V11... thì file này CẦN được cập nhật thêm (hoặc đơn giản hơn:
-- xem mục "Cách 2" bên dưới).
--
-- CÁCH DÙNG (Cách 1 — dùng file này, nhanh, không cần Java/Maven):
--   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < create_database_from_scratch.sql
-- hoặc mở file này bằng MySQL Workbench / DataGrip rồi Execute toàn bộ.
--
-- CÁCH 2 (khuyên dùng nếu đã cài lại được JDK 17 + Maven rồi):
--   1. CREATE DATABASE school_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--   2. Copy backend/.env.example -> backend/.env, điền DB_USERNAME/DB_PASSWORD
--      thật (hoặc dùng root/root cho local).
--   3. Chạy app (`mvn spring-boot:run` hoặc IDE) — Flyway tự động áp toàn bộ
--      V1..V9 theo đúng thứ tự, không cần file này.
-- Cách 2 luôn là "nguồn sự thật" (source of truth) vì nó chạy đúng những gì
-- Flyway thực sự chạy ở production/dev; Cách 1 (file này) chỉ là bản sao chép
-- lại cho tiện, có rủi ro lệch nếu quên cập nhật khi thêm migration mới.
--
-- Sau khi chạy file này xong bằng CÁCH 1, database đã ở đúng trạng thái tương
-- đương "đã áp Flyway V1..V9" (bảng flyway_schema_history được ghi sẵn ở cuối
-- file, đúng version/checksum thật) — khởi động app bình thường sau đó,
-- Flyway sẽ thấy schema đã up-to-date và KHÔNG chạy lại migration nào.
--
-- Toàn bộ dữ liệu mẫu (tài khoản admin, lớp học, học sinh test...) KHÔNG nằm
-- trong file này — xem backend/TEST_DATA_CORRECTED.sql nếu cần seed dữ liệu
-- test sau khi tạo schema xong.
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 1) Database
-- ----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `school_management`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2) User riêng cho app (tuỳ chọn — bỏ qua nếu bạn định dùng thẳng root/root
-- như .env local hiện tại của repo). Đổi mật khẩu bên dưới rồi cập nhật
-- DB_USERNAME/DB_PASSWORD trong backend/.env cho khớp.
-- ----------------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'school_app'@'localhost' IDENTIFIED BY 'CHANGE_ME';
GRANT ALL PRIVILEGES ON `school_management`.* TO 'school_app'@'localhost';
FLUSH PRIVILEGES;

USE `school_management`;


-- ============================================================================
-- V1__baseline.sql
-- Baseline schema — generated from JPA entities via Hibernate ddl-auto=update
-- on an empty MySQL 8.0 database, then dumped with `mysqldump --no-data`.
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `attendance` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attendance_date` date NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `remarks` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ABSENT','LATE','LEAVE_APPROVED','LEAVE_PENDING','PRESENT','SICK_LEAVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `marked_by` bigint DEFAULT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKpkwi8vo8nh7iu7kte6w8nyeeg` (`marked_by`),
  KEY `FK7121lveuhtmu9wa6m90ayd5yg` (`student_id`),
  CONSTRAINT `FK7121lveuhtmu9wa6m90ayd5yg` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FKpkwi8vo8nh7iu7kte6w8nyeeg` FOREIGN KEY (`marked_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `book_transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `borrow_date` date DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `due_date` date DEFAULT NULL,
  `fine_amount` double DEFAULT NULL,
  `fine_paid` bit(1) NOT NULL,
  `notes` text COLLATE utf8mb4_unicode_ci,
  `return_date` date DEFAULT NULL,
  `transaction_type` enum('BORROW','RENEW','RESERVE','RETURN') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `book_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlkrhfj9v11o3a8tx7ak0d8sfs` (`book_id`),
  KEY `FKg7g0fd98s6stuwtv1ow8pnofy` (`user_id`),
  CONSTRAINT `FKg7g0fd98s6stuwtv1ow8pnofy` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKlkrhfj9v11o3a8tx7ak0d8sfs` FOREIGN KEY (`book_id`) REFERENCES `library_books` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `classes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `capacity` int DEFAULT NULL,
  `class_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `room_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `section` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `class_teacher_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_mgg5753yel6celk0t48duc5jx` (`class_name`),
  KEY `FK48qb2e2d0jesu7qnv1ouovn4x` (`class_teacher_id`),
  CONSTRAINT `FK48qb2e2d0jesu7qnv1ouovn4x` FOREIGN KEY (`class_teacher_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `fees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `amount` double NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `due_date` date DEFAULT NULL,
  `fee_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `paid_amount` double DEFAULT NULL,
  `paid_date` date DEFAULT NULL,
  `payment_method` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remaining_amount` double DEFAULT NULL,
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `status` enum('CANCELLED','EXEMPTED','OVERDUE','PAID','PARTIAL_PAID','PENDING') COLLATE utf8mb4_unicode_ci NOT NULL,
  `transaction_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `student_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKh56p3es1h1lt6ge4cl3by4oko` (`student_id`),
  CONSTRAINT `FKh56p3es1h1lt6ge4cl3by4oko` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `grades` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `academic_year` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `exam_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `grade` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `marks_obtained` double NOT NULL,
  `percentage` double DEFAULT NULL,
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `subject` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_marks` double NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `student_id` bigint NOT NULL,
  `teacher_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK13a16545m7vvrcspc999r15s9` (`student_id`),
  KEY `FK8llmstgemr2n61cvc0ukb4qh5` (`teacher_id`),
  CONSTRAINT `FK13a16545m7vvrcspc999r15s9` FOREIGN KEY (`student_id`) REFERENCES `students` (`id`),
  CONSTRAINT `FK8llmstgemr2n61cvc0ukb4qh5` FOREIGN KEY (`teacher_id`) REFERENCES `staff` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `library_books` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `acquisition_date` datetime(6) DEFAULT NULL,
  `author` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `available_copies` int NOT NULL,
  `call_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` enum('ACADEMIC','ARTS','BIOGRAPHY','FICTION','HISTORY','LANGUAGE','LITERATURE','MATHEMATICS','NON_FICTION','OTHER','REFERENCE','SCIENCE','SPORTS') COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `edition` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `isbn` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `location_rack` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` double DEFAULT NULL,
  `publication_year` int DEFAULT NULL,
  `publisher` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ARCHIVED','AVAILABLE','BORROWED','DAMAGED','LOST','RESERVED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_copies` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ed9rs17ag7secg821h9vdmauw` (`isbn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `staff` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `date_of_joining` date DEFAULT NULL,
  `department` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_contact_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_contact_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `employee_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `position` enum('ACCOUNTANT','ADMINISTRATOR','COUNSELOR','LIBRARIAN','MAINTENANCE','NURSE','PRINCIPAL','TEACHER','VICE_PRINCIPAL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `postal_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `qualification` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `salary` double DEFAULT NULL,
  `state` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE','ON_LEAVE','RETIRED','TERMINATED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `subject_specialization` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_gjxemtljj44lom4507vslmg5w` (`employee_id`),
  KEY `FKdlvw23ak3u9v9bomm8g12rtc0` (`user_id`),
  CONSTRAINT `FKdlvw23ak3u9v9bomm8g12rtc0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `students` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `admission_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `blood_group` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `class_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `date_of_admission` date DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `emergency_contact_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_contact_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emergency_contact_relation` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `father_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `father_occupation` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `father_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mother_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mother_occupation` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mother_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `postal_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `roll_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `section` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `state` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','EXPELLED','GRADUATED','INACTIVE','TRANSFERRED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_4ijilwehsq4n3vhrdlq722lnc` (`admission_number`),
  UNIQUE KEY `UK_kmd86jf46110c60b412tjt2bg` (`roll_number`),
  KEY `FKdt1cjx5ve5bdabmuuf3ibrwaq` (`user_id`),
  CONSTRAINT `FKdt1cjx5ve5bdabmuuf3ibrwaq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `user_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `granted_at` datetime(6) NOT NULL,
  `granted_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `permission` enum('ASSIGN_TEACHER','BORROW_BOOK','CREATE_ATTENDANCE','CREATE_BOOK','CREATE_CLASS','CREATE_FEE','CREATE_GRADE','CREATE_STAFF','CREATE_STUDENT','DELETE_ATTENDANCE','DELETE_BOOK','DELETE_CLASS','DELETE_FEE','DELETE_GRADE','DELETE_STAFF','DELETE_STUDENT','GENERATE_REPORT','MANAGE_ROLES','MANAGE_USERS','PROCESS_PAYMENT','READ_ATTENDANCE','READ_BOOK','READ_CLASS','READ_FEE','READ_GRADE','READ_STAFF','READ_STUDENT','RETURN_BOOK','SYSTEM_CONFIG','UPDATE_ATTENDANCE','UPDATE_BOOK','UPDATE_CLASS','UPDATE_FEE','UPDATE_GRADE','UPDATE_STAFF','UPDATE_STUDENT','VIEW_LOGS','VIEW_REPORT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkowxl8b2bngrxd1gafh13005u` (`user_id`),
  CONSTRAINT `FKkowxl8b2bngrxd1gafh13005u` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `enabled` bit(1) NOT NULL,
  `first_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `last_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ACCOUNTANT','ADMIN','LIBRARIAN','PARENT','PRINCIPAL','STUDENT','TEACHER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;


-- ============================================================================
-- V2__fix_classes_unique_constraint.sql
-- V1 baseline generated a UNIQUE key on class_name ALONE — replaced with the
-- real invariant: class name + section + academic year together.
-- ============================================================================

ALTER TABLE classes DROP INDEX UK_mgg5753yel6celk0t48duc5jx;
ALTER TABLE classes MODIFY COLUMN academic_year VARCHAR(255) NOT NULL;
ALTER TABLE classes ADD CONSTRAINT uk_classes_name_section_year UNIQUE (class_name, section, academic_year);


-- ============================================================================
-- V3__academic_structure.sql
-- Phase 3.1: Academic Year / Semester / Subject, plus FK columns + backfill
-- on classes/students. Backfill statements are safe no-ops on an empty DB.
-- ============================================================================

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

ALTER TABLE `classes`
  ADD COLUMN `academic_year_id` bigint DEFAULT NULL AFTER `academic_year`,
  ADD COLUMN `grade_level` int DEFAULT NULL AFTER `academic_year_id`,
  ADD CONSTRAINT `fk_classes_academic_year` FOREIGN KEY (`academic_year_id`) REFERENCES `academic_years` (`id`);

ALTER TABLE `students`
  ADD COLUMN `current_class_id` bigint DEFAULT NULL AFTER `section`,
  ADD CONSTRAINT `fk_students_current_class` FOREIGN KEY (`current_class_id`) REFERENCES `classes` (`id`);

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

INSERT INTO semesters (academic_year_id, name, start_date, end_date, created_at, updated_at)
SELECT id, 'HK1', start_date, end_date, NOW(6), NOW(6)
FROM academic_years;

INSERT IGNORE INTO subjects (code, name, category, created_at, updated_at)
SELECT DISTINCT
  UPPER(LEFT(REPLACE(TRIM(subject), ' ', '_'), 50)),
  subject,
  'BAT_BUOC',
  NOW(6),
  NOW(6)
FROM grades
WHERE subject IS NOT NULL AND subject != '';

UPDATE classes c
JOIN academic_years ay ON ay.name = c.academic_year
SET c.academic_year_id = ay.id;

UPDATE classes
SET grade_level = CAST(class_name AS UNSIGNED)
WHERE class_name REGEXP '^[0-9]+$'
  AND CAST(class_name AS UNSIGNED) BETWEEN 6 AND 12;

UPDATE students s
JOIN classes c ON c.class_name = s.class_name AND c.section = s.section
SET s.current_class_id = c.id
WHERE s.class_name IS NOT NULL AND s.section IS NOT NULL;


-- ============================================================================
-- V4__teaching_timetable.sql
-- Phase 3.2: Teaching assignments (phan cong giang day) + Timetable (thoi
-- khoa bieu).
-- ============================================================================

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


-- ============================================================================
-- V5__grading_tt22.sql
-- Phase 3.3: Grade records (Thong tu 22/2021, TT58-compatible).
-- ============================================================================

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


-- ============================================================================
-- V6__conduct_records.sql
-- Phase 3.4: Hanh kiem / Ren luyen (conduct records).
-- ============================================================================

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


-- ============================================================================
-- V7__promotion_records.sql
-- Phase 3.5: Xet len lop / O lai / Tot nghiep (promotion records).
-- ============================================================================

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


-- ============================================================================
-- V8__parents_notifications.sql
-- Phase 3.6: Phu huynh - Hoc sinh & So lien lac dien tu.
-- ============================================================================

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


-- ============================================================================
-- V9__admission_applications.sql
-- Phase 3.7: Tuyen sinh dau cap (admission applications).
-- ============================================================================

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


-- ============================================================================
-- Flyway bookkeeping
-- ============================================================================
-- Ghi sẵn bảng flyway_schema_history đúng như Flyway tự tạo, với version/
-- checksum thật (lấy từ một DB đã chạy Flyway thật trên đúng bộ migration
-- V1..V9 ở trên) — để lần khởi động app đầu tiên sau khi restore, Flyway
-- validate thấy khớp và KHÔNG chạy lại (hoặc báo lỗi "already exists") bất
-- kỳ migration nào trong số này.
-- ============================================================================

CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO flyway_schema_history
  (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES
  (1, '1', 'baseline',                     'SQL', 'V1__baseline.sql',                      874279696, 'root', NOW(), 0, 1),
  (2, '2', 'fix classes unique constraint','SQL', 'V2__fix_classes_unique_constraint.sql', -1668612134, 'root', NOW(), 0, 1),
  (3, '3', 'academic structure',           'SQL', 'V3__academic_structure.sql',            1827882846, 'root', NOW(), 0, 1),
  (4, '4', 'teaching timetable',           'SQL', 'V4__teaching_timetable.sql',            -915706529, 'root', NOW(), 0, 1),
  (5, '5', 'grading tt22',                 'SQL', 'V5__grading_tt22.sql',                  -676968693, 'root', NOW(), 0, 1),
  (6, '6', 'conduct records',              'SQL', 'V6__conduct_records.sql',               830754629, 'root', NOW(), 0, 1),
  (7, '7', 'promotion records',            'SQL', 'V7__promotion_records.sql',             -1249017958, 'root', NOW(), 0, 1),
  (8, '8', 'parents notifications',        'SQL', 'V8__parents_notifications.sql',         -159158942, 'root', NOW(), 0, 1),
  (9, '9', 'admission applications',       'SQL', 'V9__admission_applications.sql',        -1040324601, 'root', NOW(), 0, 1);

-- ============================================================================
-- XONG. Database "school_management" đã đầy đủ schema (chưa có dữ liệu).
-- Bước tiếp theo:
--   1. Copy backend/.env.example -> backend/.env, điền DB_HOST/DB_PORT/
--      DB_NAME=school_management, DB_USERNAME/DB_PASSWORD (root/root hoặc
--      school_app/CHANGE_ME ở trên), và JWT_SECRET (bắt buộc, không có mặc
--      định — sinh bằng `openssl rand -base64 64`).
--   2. (Tuỳ chọn) chạy backend/TEST_DATA_CORRECTED.sql để có dữ liệu mẫu.
--   3. cd backend && mvn spring-boot:run
-- ============================================================================
