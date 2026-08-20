-- =====================================================
-- Baseline schema — generated from JPA entities via
-- Hibernate ddl-auto=update on an empty MySQL 8.0 database,
-- then dumped with `mysqldump --no-data`.
-- Do not hand-edit table shapes without updating the matching
-- @Entity class — ddl-auto is `validate` from here on.
-- =====================================================

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
