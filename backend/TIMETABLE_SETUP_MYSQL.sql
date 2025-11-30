-- =====================================================
-- TIMETABLE TABLE - Creation and Test Data (MySQL)
-- =====================================================
-- Date: November 22, 2025
-- Database: MySQL (Aiven Cloud)
-- Purpose: Create timetable table and insert test data
-- =====================================================

-- =====================================================
-- DROP TABLE IF EXISTS (for fresh setup)
-- =====================================================
DROP TABLE IF EXISTS timetables;

-- =====================================================
-- CREATE TIMETABLE TABLE (MySQL Compatible)
-- =====================================================
CREATE TABLE timetables (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    session_type VARCHAR(20) NOT NULL,
    time_slot INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    subject VARCHAR(100) NOT NULL,
    classroom VARCHAR(10) NOT NULL,
    academic_year VARCHAR(10) NOT NULL,
    subject_teacher_id BIGINT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_teacher_id) REFERENCES staff(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_timetable_slot (class_id, day_of_week, time_slot, session_type),
    INDEX idx_timetable_class_id (class_id),
    INDEX idx_timetable_academic_year (academic_year),
    INDEX idx_timetable_day_session (day_of_week, session_type),
    INDEX idx_timetable_subject_teacher (subject_teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- INSERT TEST DATA FOR CLASS 10A (MORNING - GROUP A)
-- =====================================================
-- Class 10A: ID = 1, 3 students (student1, student2, student3)
-- Homeroom Teacher: teacher1 (John Smith) - Staff ID: 2
-- Session: MORNING (07:00-12:00)
-- Subject Teachers: teacher1=2, teacher2=3, teacher3=4

INSERT INTO timetables (class_id, day_of_week, session_type, time_slot, start_time, end_time, subject, classroom, subject_teacher_id, academic_year, status, created_by_id, created_at, updated_at) VALUES
-- MONDAY - MORNING
(1, 'MONDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- TUESDAY - MORNING
(1, 'TUESDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- WEDNESDAY - MORNING
(1, 'WEDNESDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- THURSDAY - MORNING
(1, 'THURSDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- FRIDAY - MORNING
(1, 'FRIDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- SATURDAY - MORNING
(1, 'SATURDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW());

-- =====================================================
-- INSERT TEST DATA FOR CLASS 10B (AFTERNOON - GROUP B)
-- =====================================================
-- Class 10B: ID = 2, 3 students (student4, student5, student6)
-- Homeroom Teacher: teacher2 (Sarah Johnson) - Staff ID: 3
-- Session: AFTERNOON (13:00-18:00)

INSERT INTO timetables (class_id, day_of_week, session_type, time_slot, start_time, end_time, subject, classroom, subject_teacher_id, academic_year, status, created_by_id, created_at, updated_at) VALUES
-- MONDAY - AFTERNOON
(2, 'MONDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- TUESDAY - AFTERNOON
(2, 'TUESDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- WEDNESDAY - AFTERNOON
(2, 'WEDNESDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- THURSDAY - AFTERNOON
(2, 'THURSDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- FRIDAY - AFTERNOON
(2, 'FRIDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- SATURDAY - AFTERNOON
(2, 'SATURDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW());

-- =====================================================
-- VERIFY DATA
-- =====================================================
SELECT COUNT(*) as total_timetable_entries FROM timetables;

SELECT
    c.class_name,
    c.section,
    t.day_of_week,
    t.session_type,
    t.time_slot,
    t.start_time,
    t.end_time,
    t.subject,
    CONCAT(u.first_name, ' ', u.last_name) as teacher_name,
    t.classroom,
    t.academic_year
FROM timetables t
JOIN classes c ON t.class_id = c.id
LEFT JOIN staff s ON t.subject_teacher_id = s.id
LEFT JOIN users u ON s.user_id = u.id
WHERE c.id = 1
ORDER BY
    FIELD(t.day_of_week, 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'),
    t.time_slot
LIMIT 10;

