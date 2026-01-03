-- ========================================
-- SCHOOL MANAGEMENT SYSTEM - COMPLETE DATABASE SETUP
-- Version: 2.0
-- Date: 2026-01-03
-- Description: Complete database setup with tables, constraints, and test data
-- ========================================

-- ========================================
-- 1. CREATE DATABASE
-- ========================================
CREATE DATABASE IF NOT EXISTS schoolmanagement
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE schoolmanagement;

-- ========================================
-- 2. CREATE CORE TABLES
-- ========================================

-- 2.1 USERS TABLE (Authentication & Authorization)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL COMMENT 'ADMIN, PRINCIPAL, TEACHER, STUDENT, PARENT, ACCOUNTANT, LIBRARIAN',
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.2 STAFF TABLE (Teachers, Admin, etc.)
CREATE TABLE IF NOT EXISTS staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    staff_id VARCHAR(20) UNIQUE,
    position VARCHAR(100),
    department VARCHAR(100),
    qualification VARCHAR(200),
    phone_number VARCHAR(20),
    date_of_birth DATE,
    hire_date DATE,
    salary DECIMAL(12,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3 STUDENTS TABLE (Basic student info)
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    student_id VARCHAR(20) UNIQUE,
    grade_level_id BIGINT NULL,
    class_id BIGINT NULL,
    academic_year VARCHAR(20) NULL,
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    phone_number VARCHAR(20),
    parent_name VARCHAR(100),
    parent_phone VARCHAR(20),
    parent_email VARCHAR(100),
    enrollment_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 3. VIETNAMESE EDUCATION SYSTEM TABLES
-- ========================================

-- 3.1 GRADE LEVELS TABLE (Khối Lớp)
CREATE TABLE IF NOT EXISTS grade_levels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_number INT NOT NULL COMMENT 'Số khối: 6, 7, 8, 9, 10, 11, 12',
    level_name VARCHAR(50) NOT NULL COMMENT 'Tên khối: Khối 6, Khối 7, etc.',
    school_type VARCHAR(20) NOT NULL COMMENT 'THCS hoặc THPT',
    academic_year VARCHAR(20) NOT NULL COMMENT 'Năm học: 2024-2025',
    head_teacher_id BIGINT NULL,
    description TEXT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_grade_level (level_number, academic_year),
    INDEX idx_school_type (school_type),
    INDEX idx_academic_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.2 CLASSES TABLE (Lớp Học)
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_level_id BIGINT NOT NULL,
    class_name VARCHAR(50) NOT NULL COMMENT 'Tên lớp: 6A, 6B, 10A1',
    full_name VARCHAR(100) NULL,
    homeroom_teacher_id BIGINT NULL,
    academic_year VARCHAR(20) NOT NULL,
    max_students INT DEFAULT 40,
    current_students INT DEFAULT 0,
    room_number VARCHAR(20) NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id) ON DELETE RESTRICT,
    FOREIGN KEY (homeroom_teacher_id) REFERENCES staff(id) ON DELETE SET NULL,
    UNIQUE KEY uk_class (class_name, academic_year),
    INDEX idx_grade_level (grade_level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.3 SUBJECTS TABLE (Môn Học)
CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    subject_name_en VARCHAR(100) NULL,
    school_type VARCHAR(20) NOT NULL COMMENT 'THCS, THPT, or BOTH',
    category VARCHAR(50) NULL,
    total_periods_per_week INT NULL,
    coefficient DECIMAL(3,1) DEFAULT 1.0,
    is_required BOOLEAN DEFAULT TRUE,
    description TEXT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_school_type (school_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.4 CLASS SUBJECT ASSIGNMENTS (Phân Công Giảng Dạy)
CREATE TABLE IF NOT EXISTS class_subject_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    periods_per_week INT DEFAULT 0,
    start_date DATE NULL,
    end_date DATE NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_assignment (class_id, subject_id, semester, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.5 TIMETABLES (Thời Khóa Biểu)
CREATE TABLE IF NOT EXISTS timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    day_of_week INT NOT NULL COMMENT '2-7 (Monday-Saturday)',
    period_number INT NOT NULL COMMENT '1-10',
    room_number VARCHAR(20) NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_timetable (class_id, day_of_week, period_number, semester, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.6 ACADEMIC YEARS (Năm Học)
CREATE TABLE IF NOT EXISTS academic_years (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    year_name VARCHAR(20) NOT NULL UNIQUE COMMENT '2024-2025',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_current BOOLEAN DEFAULT FALSE,
    semester1_start DATE,
    semester1_end DATE,
    semester2_start DATE,
    semester2_end DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.7 STUDENT VN (Vietnam Standard Student Management)
CREATE TABLE IF NOT EXISTS students_vn (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    student_code VARCHAR(20) UNIQUE NOT NULL,
    student_id VARCHAR(20),
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10),
    place_of_birth VARCHAR(100),
    id_number VARCHAR(20),
    id_issue_date DATE,
    id_issue_place VARCHAR(100),
    province VARCHAR(100),
    district VARCHAR(100),
    ward VARCHAR(100),
    detailed_address TEXT,
    phone_number VARCHAR(20),
    ethnicity VARCHAR(50),
    religion VARCHAR(50),
    priority_object VARCHAR(100),
    grade_level_id BIGINT,
    school_class_id BIGINT,
    academic_year VARCHAR(20),
    admission_year INT,
    expected_graduation_year INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    -- Parent info
    father_name VARCHAR(100),
    father_year_of_birth INT,
    father_occupation VARCHAR(100),
    father_workplace VARCHAR(200),
    father_phone VARCHAR(20),
    father_email VARCHAR(100),
    mother_name VARCHAR(100),
    mother_year_of_birth INT,
    mother_occupation VARCHAR(100),
    mother_workplace VARCHAR(200),
    mother_phone VARCHAR(20),
    mother_email VARCHAR(100),
    guardian_name VARCHAR(100),
    guardian_relationship VARCHAR(50),
    guardian_phone VARCHAR(20),
    guardian_address TEXT,
    -- Academic history
    previous_school VARCHAR(200),
    previous_school_address TEXT,
    previous_school_from DATE,
    previous_school_to DATE,
    transfer_reason TEXT,
    previous_academic_rank VARCHAR(50),
    previous_conduct_rank VARCHAR(50),
    awards TEXT,
    -- Health
    height INT,
    weight INT,
    blood_type VARCHAR(10),
    diseases TEXT,
    allergies TEXT,
    notes TEXT,
    -- Documents
    photo_url VARCHAR(500),
    birth_certificate_url VARCHAR(500),
    household_book_url VARCHAR(500),
    other_documents_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id) ON DELETE SET NULL,
    FOREIGN KEY (school_class_id) REFERENCES classes(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.8 EXAMS
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_name VARCHAR(200) NOT NULL,
    exam_type VARCHAR(50),
    subject_id BIGINT NOT NULL,
    class_id BIGINT,
    grade_level_id BIGINT,
    exam_date DATE,
    duration INT COMMENT 'Minutes',
    total_marks DECIMAL(5,2) DEFAULT 100.00,
    academic_year VARCHAR(20),
    semester INT,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3.9 EXAM RESULTS
CREATE TABLE IF NOT EXISTS exam_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    marks_obtained DECIMAL(5,2),
    grade VARCHAR(5),
    remarks TEXT,
    is_absent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY uk_exam_student (exam_id, student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign keys to students table
ALTER TABLE students
ADD CONSTRAINT fk_students_grade_level FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE SET NULL;

CREATE INDEX idx_students_grade_level ON students(grade_level_id);
CREATE INDEX idx_students_class ON students(class_id);
CREATE INDEX idx_students_academic_year ON students(academic_year);

-- ========================================
-- 4. INSERT TEST DATA
-- ========================================

-- 4.1 Insert Admin User
INSERT INTO users (username, password, email, role, first_name, last_name) VALUES
('admin', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'admin@school.com', 'ADMIN', 'System', 'Administrator'),
('principal', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'principal@school.com', 'PRINCIPAL', 'Principal', 'User'),
('teacher1', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher1@school.com', 'TEACHER', 'John', 'Doe'),
('student1', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student1@school.com', 'STUDENT', 'Jane', 'Smith')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.2 Insert Grade Levels
INSERT INTO grade_levels (level_number, level_name, school_type, academic_year, status) VALUES
(6, 'Grade 6', 'THCS', '2024-2025', 'ACTIVE'),
(7, 'Grade 7', 'THCS', '2024-2025', 'ACTIVE'),
(8, 'Grade 8', 'THCS', '2024-2025', 'ACTIVE'),
(9, 'Grade 9', 'THCS', '2024-2025', 'ACTIVE'),
(10, 'Grade 10', 'THPT', '2024-2025', 'ACTIVE'),
(11, 'Grade 11', 'THPT', '2024-2025', 'ACTIVE'),
(12, 'Grade 12', 'THPT', '2024-2025', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.3 Insert Subjects
INSERT INTO subjects (subject_code, subject_name, subject_name_en, school_type, category, total_periods_per_week, coefficient, is_required) VALUES
('MATH', 'Toán học', 'Mathematics', 'BOTH', 'Science', 5, 2.0, TRUE),
('LIT', 'Ngữ văn', 'Literature', 'BOTH', 'Language', 5, 2.0, TRUE),
('ENG', 'Tiếng Anh', 'English', 'BOTH', 'Language', 3, 1.0, TRUE),
('PHY', 'Vật lý', 'Physics', 'BOTH', 'Science', 3, 1.0, TRUE),
('CHEM', 'Hóa học', 'Chemistry', 'BOTH', 'Science', 2, 1.0, TRUE),
('BIO', 'Sinh học', 'Biology', 'BOTH', 'Science', 2, 1.0, TRUE),
('HIST', 'Lịch sử', 'History', 'BOTH', 'Social', 2, 1.0, TRUE),
('GEO', 'Địa lý', 'Geography', 'BOTH', 'Social', 2, 1.0, TRUE),
('CIVIC', 'Giáo dục công dân', 'Civic Education', 'BOTH', 'Social', 1, 1.0, TRUE),
('PE', 'Thể dục', 'Physical Education', 'BOTH', 'Other', 2, 1.0, TRUE),
('IT', 'Tin học', 'Computer Science', 'BOTH', 'Technology', 1, 1.0, TRUE),
('MUSIC', 'Âm nhạc', 'Music', 'BOTH', 'Arts', 1, 1.0, FALSE),
('ART', 'Mỹ thuật', 'Arts', 'BOTH', 'Arts', 1, 1.0, FALSE),
('TECH', 'Công nghệ', 'Technology', 'BOTH', 'Technology', 2, 1.0, TRUE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.4 Insert Classes (Sample)
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, room_number, status) VALUES
(1, '6A', 'Class 6A', '2024-2025', 40, 'A101', 'ACTIVE'),
(1, '6B', 'Class 6B', '2024-2025', 40, 'A102', 'ACTIVE'),
(2, '7A', 'Class 7A', '2024-2025', 40, 'A201', 'ACTIVE'),
(2, '7B', 'Class 7B', '2024-2025', 40, 'A202', 'ACTIVE'),
(3, '8A', 'Class 8A', '2024-2025', 40, 'A301', 'ACTIVE'),
(3, '8B', 'Class 8B', '2024-2025', 40, 'A302', 'ACTIVE'),
(4, '9A', 'Class 9A', '2024-2025', 40, 'A401', 'ACTIVE'),
(4, '9B', 'Class 9B', '2024-2025', 40, 'A402', 'ACTIVE'),
(5, '10A1', 'Class 10A1', '2024-2025', 40, 'B101', 'ACTIVE'),
(5, '10A2', 'Class 10A2', '2024-2025', 40, 'B102', 'ACTIVE'),
(6, '11A1', 'Class 11A1', '2024-2025', 40, 'B201', 'ACTIVE'),
(6, '11A2', 'Class 11A2', '2024-2025', 40, 'B202', 'ACTIVE'),
(7, '12A1', 'Class 12A1', '2024-2025', 40, 'B301', 'ACTIVE'),
(7, '12A2', 'Class 12A2', '2024-2025', 40, 'B302', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.5 Insert Academic Year
INSERT INTO academic_years (year_name, start_date, end_date, is_current, semester1_start, semester1_end, semester2_start, semester2_end) VALUES
('2024-2025', '2024-09-01', '2025-05-31', TRUE, '2024-09-01', '2024-12-31', '2025-01-07', '2025-05-31')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.6 Insert More Teachers (10 teachers total)
INSERT INTO users (username, password, email, role, first_name, last_name) VALUES
('teacher_math', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.math@school.com', 'TEACHER', 'Nguyen', 'Van A'),
('teacher_lit', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.lit@school.com', 'TEACHER', 'Tran', 'Thi B'),
('teacher_eng', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.eng@school.com', 'TEACHER', 'Le', 'Van C'),
('teacher_phy', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.phy@school.com', 'TEACHER', 'Pham', 'Thi D'),
('teacher_chem', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.chem@school.com', 'TEACHER', 'Hoang', 'Van E'),
('teacher_bio', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.bio@school.com', 'TEACHER', 'Vu', 'Thi F'),
('teacher_hist', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.hist@school.com', 'TEACHER', 'Do', 'Van G'),
('teacher_geo', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.geo@school.com', 'TEACHER', 'Bui', 'Thi H'),
('teacher_it', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'teacher.it@school.com', 'TEACHER', 'Dang', 'Van I')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.7 Insert Staff Records for Teachers
INSERT INTO staff (user_id, staff_id, position, department, qualification, phone_number, date_of_birth, hire_date, status) VALUES
(3, 'TCH001', 'Math Teacher', 'Science', 'Master in Mathematics', '0901234567', '1985-05-15', '2015-09-01', 'ACTIVE'),
(5, 'TCH002', 'Math Teacher', 'Science', 'Bachelor in Mathematics', '0901234568', '1987-03-20', '2016-09-01', 'ACTIVE'),
(6, 'TCH003', 'Literature Teacher', 'Language', 'Master in Literature', '0901234569', '1984-07-10', '2014-09-01', 'ACTIVE'),
(7, 'TCH004', 'English Teacher', 'Language', 'Bachelor in English', '0901234570', '1990-11-25', '2017-09-01', 'ACTIVE'),
(8, 'TCH005', 'Physics Teacher', 'Science', 'Master in Physics', '0901234571', '1983-02-14', '2013-09-01', 'ACTIVE'),
(9, 'TCH006', 'Chemistry Teacher', 'Science', 'Bachelor in Chemistry', '0901234572', '1988-09-30', '2016-09-01', 'ACTIVE'),
(10, 'TCH007', 'Biology Teacher', 'Science', 'Master in Biology', '0901234573', '1986-04-18', '2015-09-01', 'ACTIVE'),
(11, 'TCH008', 'History Teacher', 'Social', 'Bachelor in History', '0901234574', '1989-12-05', '2017-09-01', 'ACTIVE'),
(12, 'TCH009', 'Geography Teacher', 'Social', 'Bachelor in Geography', '0901234575', '1991-06-22', '2018-09-01', 'ACTIVE'),
(13, 'TCH010', 'IT Teacher', 'Technology', 'Master in Computer Science', '0901234576', '1992-08-08', '2019-09-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.8 Insert Sample Students (20 students)
INSERT INTO users (username, password, email, role, first_name, last_name) VALUES
('student001', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student001@school.com', 'STUDENT', 'Nguyen', 'Minh Anh'),
('student002', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student002@school.com', 'STUDENT', 'Tran', 'Hoang Bao'),
('student003', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student003@school.com', 'STUDENT', 'Le', 'Thi Cam'),
('student004', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student004@school.com', 'STUDENT', 'Pham', 'Van Dung'),
('student005', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student005@school.com', 'STUDENT', 'Hoang', 'Thi Em'),
('student006', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student006@school.com', 'STUDENT', 'Vu', 'Van Phong'),
('student007', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student007@school.com', 'STUDENT', 'Do', 'Thi Giang'),
('student008', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student008@school.com', 'STUDENT', 'Bui', 'Van Hai'),
('student009', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student009@school.com', 'STUDENT', 'Dang', 'Thi Hoa'),
('student010', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student010@school.com', 'STUDENT', 'Ngo', 'Van Khai'),
('student011', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student011@school.com', 'STUDENT', 'Ly', 'Thi Lan'),
('student012', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student012@school.com', 'STUDENT', 'Vo', 'Van Minh'),
('student013', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student013@school.com', 'STUDENT', 'Trinh', 'Thi Nga'),
('student014', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student014@school.com', 'STUDENT', 'Duong', 'Van Phuc'),
('student015', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student015@school.com', 'STUDENT', 'Mai', 'Thi Quynh'),
('student016', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student016@school.com', 'STUDENT', 'Ha', 'Van Son'),
('student017', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student017@school.com', 'STUDENT', 'Cao', 'Thi Thao'),
('student018', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student018@school.com', 'STUDENT', 'Ta', 'Van Uyen'),
('student019', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student019@school.com', 'STUDENT', 'Tong', 'Thi Van'),
('student020', '$2a$10$xZGKt5eOKBvKRp5WqJq8qOGj8YvXRVY6FqQGqPHe3WpNKLx.tQ8WK', 'student020@school.com', 'STUDENT', 'Lam', 'Van Xuan')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.9 Insert Student Records
INSERT INTO students (user_id, student_id, grade_level_id, class_id, academic_year, date_of_birth, gender, address, phone_number, parent_name, parent_phone, enrollment_date, status) VALUES
(14, 'ST2024001', 1, 1, '2024-2025', '2012-03-15', 'Female', '123 Le Loi, District 1, HCMC', '0987654321', 'Nguyen Van Father', '0912345671', '2024-09-01', 'ACTIVE'),
(15, 'ST2024002', 1, 1, '2024-2025', '2012-05-20', 'Male', '456 Tran Hung Dao, District 1, HCMC', '0987654322', 'Tran Van Father', '0912345672', '2024-09-01', 'ACTIVE'),
(16, 'ST2024003', 1, 2, '2024-2025', '2012-07-10', 'Female', '789 Nguyen Trai, District 5, HCMC', '0987654323', 'Le Van Father', '0912345673', '2024-09-01', 'ACTIVE'),
(17, 'ST2024004', 1, 2, '2024-2025', '2012-09-25', 'Male', '321 Hai Ba Trung, District 3, HCMC', '0987654324', 'Pham Van Father', '0912345674', '2024-09-01', 'ACTIVE'),
(18, 'ST2024005', 2, 3, '2024-2025', '2011-02-14', 'Female', '654 Vo Van Tan, District 3, HCMC', '0987654325', 'Hoang Van Father', '0912345675', '2024-09-01', 'ACTIVE'),
(19, 'ST2024006', 2, 3, '2024-2025', '2011-04-30', 'Male', '987 Cach Mang, District 10, HCMC', '0987654326', 'Vu Van Father', '0912345676', '2024-09-01', 'ACTIVE'),
(20, 'ST2024007', 2, 4, '2024-2025', '2011-06-18', 'Female', '147 Dien Bien Phu, Binh Thanh, HCMC', '0987654327', 'Do Van Father', '0912345677', '2024-09-01', 'ACTIVE'),
(21, 'ST2024008', 2, 4, '2024-2025', '2011-08-05', 'Male', '258 Le Van Sỹ, District 3, HCMC', '0987654328', 'Bui Van Father', '0912345678', '2024-09-01', 'ACTIVE'),
(22, 'ST2024009', 3, 5, '2024-2025', '2010-01-22', 'Female', '369 Ba Thang Hai, District 10, HCMC', '0987654329', 'Dang Van Father', '0912345679', '2024-09-01', 'ACTIVE'),
(23, 'ST2024010', 3, 5, '2024-2025', '2010-03-08', 'Male', '741 Ly Thuong Kiet, District 10, HCMC', '0987654330', 'Ngo Van Father', '0912345680', '2024-09-01', 'ACTIVE'),
(24, 'ST2024011', 3, 6, '2024-2025', '2010-05-12', 'Female', '852 Nguyen Chi Thanh, District 5, HCMC', '0987654331', 'Ly Van Father', '0912345681', '2024-09-01', 'ACTIVE'),
(25, 'ST2024012', 3, 6, '2024-2025', '2010-07-28', 'Male', '963 Tran Quang Khai, District 1, HCMC', '0987654332', 'Vo Van Father', '0912345682', '2024-09-01', 'ACTIVE'),
(26, 'ST2024013', 4, 7, '2024-2025', '2009-11-15', 'Female', '159 Nguyen Dinh Chieu, District 3, HCMC', '0987654333', 'Trinh Van Father', '0912345683', '2024-09-01', 'ACTIVE'),
(27, 'ST2024014', 4, 7, '2024-2025', '2009-12-30', 'Male', '357 Pasteur, District 3, HCMC', '0987654334', 'Duong Van Father', '0912345684', '2024-09-01', 'ACTIVE'),
(28, 'ST2024015', 5, 9, '2024-2025', '2008-04-05', 'Female', '753 Cong Hoa, Tan Binh, HCMC', '0987654335', 'Mai Van Father', '0912345685', '2024-09-01', 'ACTIVE'),
(29, 'ST2024016', 5, 9, '2024-2025', '2008-06-17', 'Male', '951 Hoang Van Thu, Tan Binh, HCMC', '0987654336', 'Ha Van Father', '0912345686', '2024-09-01', 'ACTIVE'),
(30, 'ST2024017', 6, 11, '2024-2025', '2007-08-22', 'Female', '246 Truong Chinh, Tan Binh, HCMC', '0987654337', 'Cao Van Father', '0912345687', '2024-09-01', 'ACTIVE'),
(31, 'ST2024018', 6, 11, '2024-2025', '2007-10-09', 'Male', '468 Lac Long Quan, Tan Binh, HCMC', '0987654338', 'Ta Van Father', '0912345688', '2024-09-01', 'ACTIVE'),
(32, 'ST2024019', 7, 13, '2024-2025', '2006-01-14', 'Female', '810 Phan Xich Long, Phu Nhuan, HCMC', '0987654339', 'Tong Van Father', '0912345689', '2024-09-01', 'ACTIVE'),
(33, 'ST2024020', 7, 13, '2024-2025', '2006-12-25', 'Male', '135 Nguyen Van Troi, Phu Nhuan, HCMC', '0987654340', 'Lam Van Father', '0912345690', '2024-09-01', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.10 Insert Class Subject Assignments (Sample for Class 6A)
INSERT INTO class_subject_assignments (class_id, subject_id, teacher_id, academic_year, semester, periods_per_week, start_date, end_date, status) VALUES
(1, 1, 1, '2024-2025', 1, 5, '2024-09-01', '2024-12-31', 'ACTIVE'),  -- Math in 6A
(1, 2, 3, '2024-2025', 1, 5, '2024-09-01', '2024-12-31', 'ACTIVE'),  -- Literature in 6A
(1, 3, 4, '2024-2025', 1, 3, '2024-09-01', '2024-12-31', 'ACTIVE'),  -- English in 6A
(1, 4, 5, '2024-2025', 1, 3, '2024-09-01', '2024-12-31', 'ACTIVE'),  -- Physics in 6A
(1, 5, 6, '2024-2025', 1, 2, '2024-09-01', '2024-12-31', 'ACTIVE'),  -- Chemistry in 6A
(1, 6, 7, '2024-2025', 1, 2, '2024-09-01', '2024-12-31', 'ACTIVE'),  -- Biology in 6A
(1, 7, 8, '2024-2025', 1, 2, '2024-09-01', '2024-12-31', 'ACTIVE'),  -- History in 6A
(1, 8, 9, '2024-2025', 1, 2, '2024-09-01', '2024-12-31', 'ACTIVE')   -- Geography in 6A
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.11 Insert Sample Timetable for Class 6A (Monday)
INSERT INTO timetables (class_id, subject_id, teacher_id, day_of_week, period_number, room_number, academic_year, semester, start_time, end_time) VALUES
(1, 1, 1, 2, 1, 'A101', '2024-2025', 1, '07:30:00', '08:15:00'),  -- Math Period 1
(1, 1, 1, 2, 2, 'A101', '2024-2025', 1, '08:15:00', '09:00:00'),  -- Math Period 2
(1, 2, 3, 2, 3, 'A101', '2024-2025', 1, '09:15:00', '10:00:00'),  -- Literature Period 3
(1, 3, 4, 2, 4, 'A101', '2024-2025', 1, '10:00:00', '10:45:00'),  -- English Period 4
(1, 4, 5, 2, 5, 'A101', '2024-2025', 1, '10:45:00', '11:30:00')   -- Physics Period 5
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.12 Insert Sample Exams
INSERT INTO exams (exam_name, exam_type, subject_id, class_id, exam_date, duration, total_marks, academic_year, semester, status) VALUES
('Math Midterm Exam', 'MIDTERM', 1, 1, '2024-11-15', 90, 100.00, '2024-2025', 1, 'COMPLETED'),
('Literature Midterm Exam', 'MIDTERM', 2, 1, '2024-11-16', 90, 100.00, '2024-2025', 1, 'COMPLETED'),
('English Midterm Exam', 'MIDTERM', 3, 1, '2024-11-17', 60, 100.00, '2024-2025', 1, 'COMPLETED'),
('Math Final Exam', 'FINAL', 1, 1, '2024-12-20', 120, 100.00, '2024-2025', 1, 'SCHEDULED'),
('Literature Final Exam', 'FINAL', 2, 1, '2024-12-21', 120, 100.00, '2024-2025', 1, 'SCHEDULED')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.13 Insert Sample Exam Results (for completed exams)
INSERT INTO exam_results (exam_id, student_id, marks_obtained, grade, remarks, is_absent) VALUES
(1, 1, 85.50, 'A', 'Excellent work', FALSE),
(1, 2, 78.00, 'B', 'Good effort', FALSE),
(2, 1, 92.00, 'A', 'Outstanding', FALSE),
(2, 2, 76.50, 'B', 'Well done', FALSE),
(3, 1, 88.00, 'A', 'Very good', FALSE),
(3, 2, 82.00, 'B', 'Good progress', FALSE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- 4.14 Update class current_students count
UPDATE classes SET current_students = 2 WHERE id = 1;
UPDATE classes SET current_students = 2 WHERE id = 2;
UPDATE classes SET current_students = 2 WHERE id = 3;
UPDATE classes SET current_students = 2 WHERE id = 4;
UPDATE classes SET current_students = 2 WHERE id = 5;
UPDATE classes SET current_students = 2 WHERE id = 6;
UPDATE classes SET current_students = 2 WHERE id = 7;
UPDATE classes SET current_students = 2 WHERE id = 9;
UPDATE classes SET current_students = 2 WHERE id = 11;
UPDATE classes SET current_students = 2 WHERE id = 13;

-- ========================================
-- 5. VERIFICATION
-- ========================================
SELECT '==========================================' AS '';
SELECT 'DATABASE SETUP COMPLETE!' AS Status;
SELECT '==========================================' AS '';
SELECT '📊 DATABASE STATISTICS:' AS '';
SELECT '==========================================' AS '';
SELECT COUNT(*) AS 'Total Users' FROM users;
SELECT COUNT(*) AS 'Total Staff' FROM staff;
SELECT COUNT(*) AS 'Total Students' FROM students;
SELECT COUNT(*) AS 'Total Grade Levels' FROM grade_levels;
SELECT COUNT(*) AS 'Total Classes' FROM classes;
SELECT COUNT(*) AS 'Total Subjects' FROM subjects;
SELECT COUNT(*) AS 'Total Academic Years' FROM academic_years;
SELECT COUNT(*) AS 'Total Assignments' FROM class_subject_assignments;
SELECT COUNT(*) AS 'Total Timetable Entries' FROM timetables;
SELECT COUNT(*) AS 'Total Exams' FROM exams;
SELECT COUNT(*) AS 'Total Exam Results' FROM exam_results;
SELECT '==========================================' AS '';
SELECT '👥 TEST ACCOUNTS:' AS '';
SELECT '==========================================' AS '';
SELECT 'Admin: admin / password' AS 'Login Credentials';
SELECT 'Principal: principal / password' AS '';
SELECT 'Teacher: teacher1 / password' AS '';
SELECT 'Student: student001 / password' AS '';
SELECT '==========================================' AS '';
SELECT '✅ READY TO TEST ALL FEATURES!' AS '';
SELECT '==========================================' AS '';

