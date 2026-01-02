-- ========================================
-- VIETNAMESE EDUCATION SYSTEM - DATABASE MIGRATION
-- Version: 1.0
-- Date: 2025-12-30
-- Description: Add support for Vietnamese grade levels (Khối), classes (Lớp), and subject assignments
-- ========================================

-- ========================================
-- 0. DATABASE SELECTION & VALIDATION
-- ========================================

-- Select database
USE schoolmanagement;

-- Verify database exists
SELECT DATABASE() AS Current_Database;

-- Check if required tables exist before migration
SELECT 'Checking required tables...' AS Step;
SELECT COUNT(*) AS Required_Tables_Exist
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'schoolmanagement' 
AND TABLE_NAME IN ('users', 'staff', 'students');

-- If count is not 3, migration should not proceed
-- Make sure you have users, staff, and students tables first!

SELECT 'Starting migration for database: schoolmanagement' AS Status;
SELECT '========================================' AS Separator;


-- ========================================
-- 1. CREATE GRADE_LEVELS TABLE (Khối Lớp)
-- ========================================
CREATE TABLE IF NOT EXISTS grade_levels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_number INT NOT NULL COMMENT 'Số khối: 6, 7, 8, 9, 10, 11, 12',
    level_name VARCHAR(50) NOT NULL COMMENT 'Tên khối: Khối 6, Khối 7, etc.',
    school_type VARCHAR(20) NOT NULL COMMENT 'THCS hoặc THPT',
    academic_year VARCHAR(20) NOT NULL COMMENT 'Năm học: 2024-2025',
    head_teacher_id BIGINT NULL COMMENT 'Tổ trưởng khối (optional)',
    description TEXT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_grade_level (level_number, academic_year),
    INDEX idx_school_type (school_type),
    INDEX idx_academic_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng quản lý khối lớp';

-- ========================================
-- 2. CREATE CLASSES TABLE (Lớp Học)
-- ========================================
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_level_id BIGINT NOT NULL COMMENT 'Khối lớp',
    class_name VARCHAR(50) NOT NULL COMMENT 'Tên lớp: 6A, 6B, 10A1',
    full_name VARCHAR(100) NULL COMMENT 'Tên đầy đủ: Lớp 6A, Lớp 10A1',
    homeroom_teacher_id BIGINT NULL COMMENT 'Giáo viên chủ nhiệm (GVCN)',
    academic_year VARCHAR(20) NOT NULL COMMENT 'Năm học: 2024-2025',
    max_students INT DEFAULT 40 COMMENT 'Sĩ số tối đa',
    current_students INT DEFAULT 0 COMMENT 'Sĩ số hiện tại',
    room_number VARCHAR(20) NULL COMMENT 'Phòng học: A101, B205',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id) ON DELETE RESTRICT,
    FOREIGN KEY (homeroom_teacher_id) REFERENCES staff(id) ON DELETE SET NULL,
    UNIQUE KEY uk_class (class_name, academic_year),
    INDEX idx_grade_level (grade_level_id),
    INDEX idx_homeroom_teacher (homeroom_teacher_id),
    INDEX idx_academic_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng quản lý lớp học';

-- ========================================
-- 3. CREATE SUBJECTS TABLE (Môn Học)
-- ========================================
CREATE TABLE IF NOT EXISTS subjects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE COMMENT 'Mã môn: TOAN, VAN, ANH',
    subject_name VARCHAR(100) NOT NULL COMMENT 'Tên môn: Toán học, Ngữ văn',
    subject_name_en VARCHAR(100) NULL COMMENT 'Tên tiếng Anh',
    school_type VARCHAR(20) NOT NULL COMMENT 'THCS, THPT, hoặc BOTH',
    category VARCHAR(50) NULL COMMENT 'Phân loại: Khoa học tự nhiên, Xã hội',
    total_periods_per_week INT NULL COMMENT 'Số tiết mặc định mỗi tuần',
    coefficient DECIMAL(3,1) DEFAULT 1.0 COMMENT 'Hệ số môn học',
    is_required BOOLEAN DEFAULT TRUE COMMENT 'Môn bắt buộc',
    description TEXT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_school_type (school_type),
    INDEX idx_subject_code (subject_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng môn học';

-- ========================================
-- 4. CREATE CLASS_SUBJECT_ASSIGNMENTS TABLE (Phân Công Giảng Dạy)
-- ========================================
CREATE TABLE IF NOT EXISTS class_subject_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL COMMENT 'Lớp học',
    subject_id BIGINT NOT NULL COMMENT 'Môn học',
    teacher_id BIGINT NOT NULL COMMENT 'Giáo viên bộ môn',
    academic_year VARCHAR(20) NOT NULL COMMENT 'Năm học: 2024-2025',
    semester INT NOT NULL COMMENT 'Học kỳ: 1 hoặc 2',
    periods_per_week INT DEFAULT 0 COMMENT 'Số tiết/tuần',
    start_date DATE NULL,
    end_date DATE NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_assignment (class_id, subject_id, semester, academic_year),
    INDEX idx_teacher (teacher_id),
    INDEX idx_academic_year (academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng phân công giảng dạy';

-- ========================================
-- 5. CREATE TEACHER_SPECIALIZATIONS TABLE (Chuyên Môn Giáo Viên)
-- ========================================
CREATE TABLE IF NOT EXISTS teacher_specializations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE COMMENT 'Bộ môn chính',
    certification_level VARCHAR(50) NULL COMMENT 'Trình độ: Giỏi, Khá, Trung bình',
    years_of_experience INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    UNIQUE KEY uk_teacher_subject (teacher_id, subject_id),
    INDEX idx_teacher (teacher_id),
    INDEX idx_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng chuyên môn giáo viên';

-- ========================================
-- 6. CREATE TIMETABLES TABLE (Thời Khóa Biểu)
-- ========================================
CREATE TABLE IF NOT EXISTS timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    day_of_week INT NOT NULL COMMENT 'Thứ 2-7 (2,3,4,5,6,7)',
    period_number INT NOT NULL COMMENT 'Tiết 1-10',
    room_number VARCHAR(20) NULL COMMENT 'Phòng học',
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL COMMENT '1 hoặc 2',
    start_time TIME NULL,
    end_time TIME NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_timetable (class_id, day_of_week, period_number, semester, academic_year),
    INDEX idx_teacher (teacher_id),
    INDEX idx_day_period (day_of_week, period_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng thời khóa biểu';

-- ========================================
-- 7. UPDATE STUDENTS TABLE (Add new columns)
-- ========================================
ALTER TABLE students 
ADD COLUMN IF NOT EXISTS grade_level_id BIGINT NULL COMMENT 'Khối lớp' AFTER id,
ADD COLUMN IF NOT EXISTS class_id BIGINT NULL COMMENT 'Lớp học' AFTER grade_level_id,
ADD COLUMN IF NOT EXISTS academic_year VARCHAR(20) NULL COMMENT 'Năm học' AFTER class_id;

-- Add foreign keys if not exists
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'students' 
    AND CONSTRAINT_NAME = 'fk_students_grade_level');

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE students ADD CONSTRAINT fk_students_grade_level FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id) ON DELETE SET NULL',
    'SELECT "FK already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_SCHEMA = DATABASE() 
    AND TABLE_NAME = 'students' 
    AND CONSTRAINT_NAME = 'fk_students_class');

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE students ADD CONSTRAINT fk_students_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE SET NULL',
    'SELECT "FK already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_students_grade_level ON students(grade_level_id);
CREATE INDEX IF NOT EXISTS idx_students_class ON students(class_id);
CREATE INDEX IF NOT EXISTS idx_students_academic_year ON students(academic_year);

-- ========================================
-- 8. INSERT SAMPLE DATA - GRADE LEVELS (Khối)
-- ========================================
INSERT INTO grade_levels (level_number, level_name, school_type, academic_year, status) VALUES
(6, 'Khối 6', 'THCS', '2024-2025', 'ACTIVE'),
(7, 'Khối 7', 'THCS', '2024-2025', 'ACTIVE'),
(8, 'Khối 8', 'THCS', '2024-2025', 'ACTIVE'),
(9, 'Khối 9', 'THCS', '2024-2025', 'ACTIVE'),
(10, 'Khối 10', 'THPT', '2024-2025', 'ACTIVE'),
(11, 'Khối 11', 'THPT', '2024-2025', 'ACTIVE'),
(12, 'Khối 12', 'THPT', '2024-2025', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 9. INSERT SAMPLE DATA - SUBJECTS (Môn Học)
-- ========================================
INSERT INTO subjects (subject_code, subject_name, subject_name_en, school_type, category, total_periods_per_week, coefficient, is_required) VALUES
('TOAN', 'Toán học', 'Mathematics', 'BOTH', 'Khoa học tự nhiên', 5, 2.0, TRUE),
('VAN', 'Ngữ văn', 'Literature', 'BOTH', 'Xã hội', 5, 2.0, TRUE),
('ANH', 'Tiếng Anh', 'English', 'BOTH', 'Ngoại ngữ', 3, 1.0, TRUE),
('LY', 'Vật lý', 'Physics', 'BOTH', 'Khoa học tự nhiên', 3, 1.0, TRUE),
('HOA', 'Hóa học', 'Chemistry', 'BOTH', 'Khoa học tự nhiên', 2, 1.0, TRUE),
('SINH', 'Sinh học', 'Biology', 'BOTH', 'Khoa học tự nhiên', 2, 1.0, TRUE),
('SU', 'Lịch sử', 'History', 'BOTH', 'Xã hội', 2, 1.0, TRUE),
('DIA', 'Địa lý', 'Geography', 'BOTH', 'Xã hội', 2, 1.0, TRUE),
('GDCD', 'Giáo dục công dân', 'Civic Education', 'BOTH', 'Xã hội', 1, 1.0, TRUE),
('TD', 'Thể dục', 'Physical Education', 'BOTH', 'Khác', 2, 1.0, TRUE),
('TIN', 'Tin học', 'Computer Science', 'BOTH', 'Công nghệ', 1, 1.0, TRUE),
('AM', 'Âm nhạc', 'Music', 'BOTH', 'Nghệ thuật', 1, 1.0, FALSE),
('MT', 'Mỹ thuật', 'Arts', 'BOTH', 'Nghệ thuật', 1, 1.0, FALSE),
('CN', 'Công nghệ', 'Technology', 'BOTH', 'Công nghệ', 2, 1.0, TRUE)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 10. INSERT SAMPLE DATA - CLASSES (Lớp Học)
-- ========================================
-- Khối 6 - THCS
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, current_students, room_number, status) VALUES
(1, '6A', 'Lớp 6A', '2024-2025', 40, 0, 'A101', 'ACTIVE'),
(1, '6B', 'Lớp 6B', '2024-2025', 40, 0, 'A102', 'ACTIVE'),
(1, '6C', 'Lớp 6C', '2024-2025', 40, 0, 'A103', 'ACTIVE'),
(1, '6D', 'Lớp 6D', '2024-2025', 40, 0, 'A104', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Khối 7 - THCS
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, current_students, room_number, status) VALUES
(2, '7A', 'Lớp 7A', '2024-2025', 40, 0, 'A201', 'ACTIVE'),
(2, '7B', 'Lớp 7B', '2024-2025', 40, 0, 'A202', 'ACTIVE'),
(2, '7C', 'Lớp 7C', '2024-2025', 40, 0, 'A203', 'ACTIVE'),
(2, '7D', 'Lớp 7D', '2024-2025', 40, 0, 'A204', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Khối 8 - THCS
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, current_students, room_number, status) VALUES
(3, '8A', 'Lớp 8A', '2024-2025', 40, 0, 'A301', 'ACTIVE'),
(3, '8B', 'Lớp 8B', '2024-2025', 40, 0, 'A302', 'ACTIVE'),
(3, '8C', 'Lớp 8C', '2024-2025', 40, 0, 'A303', 'ACTIVE'),
(3, '8D', 'Lớp 8D', '2024-2025', 40, 0, 'A304', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Khối 9 - THCS
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, current_students, room_number, status) VALUES
(4, '9A', 'Lớp 9A', '2024-2025', 40, 0, 'A401', 'ACTIVE'),
(4, '9B', 'Lớp 9B', '2024-2025', 40, 0, 'A402', 'ACTIVE'),
(4, '9C', 'Lớp 9C', '2024-2025', 40, 0, 'A403', 'ACTIVE'),
(4, '9D', 'Lớp 9D', '2024-2025', 40, 0, 'A404', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Khối 10 - THPT
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, current_students, room_number, status) VALUES
(5, '10A1', 'Lớp 10A1', '2024-2025', 40, 0, 'B101', 'ACTIVE'),
(5, '10A2', 'Lớp 10A2', '2024-2025', 40, 0, 'B102', 'ACTIVE'),
(5, '10A3', 'Lớp 10A3', '2024-2025', 40, 0, 'B103', 'ACTIVE'),
(5, '10A4', 'Lớp 10A4', '2024-2025', 40, 0, 'B104', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Khối 11 - THPT
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, current_students, room_number, status) VALUES
(6, '11A1', 'Lớp 11A1', '2024-2025', 40, 0, 'B201', 'ACTIVE'),
(6, '11A2', 'Lớp 11A2', '2024-2025', 40, 0, 'B202', 'ACTIVE'),
(6, '11A3', 'Lớp 11A3', '2024-2025', 40, 0, 'B203', 'ACTIVE'),
(6, '11A4', 'Lớp 11A4', '2024-2025', 40, 0, 'B204', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- Khối 12 - THPT
INSERT INTO classes (grade_level_id, class_name, full_name, academic_year, max_students, current_students, room_number, status) VALUES
(7, '12A1', 'Lớp 12A1', '2024-2025', 40, 0, 'B301', 'ACTIVE'),
(7, '12A2', 'Lớp 12A2', '2024-2025', 40, 0, 'B302', 'ACTIVE'),
(7, '12A3', 'Lớp 12A3', '2024-2025', 40, 0, 'B303', 'ACTIVE'),
(7, '12A4', 'Lớp 12A4', '2024-2025', 40, 0, 'B304', 'ACTIVE')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- MIGRATION COMPLETE
-- ========================================
SELECT 'Database migration completed successfully!' AS Status;
SELECT COUNT(*) AS 'Total Grade Levels' FROM grade_levels;
SELECT COUNT(*) AS 'Total Classes' FROM classes;
SELECT COUNT(*) AS 'Total Subjects' FROM subjects;
