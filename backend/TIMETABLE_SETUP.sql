-- =====================================================
-- TIMETABLE TABLE - Creation and Test Data
-- =====================================================
-- Date: November 22, 2025
-- Purpose: Create timetable table and insert test data
-- Database: MySQL (compatible with Aiven Cloud MySQL)
-- Notes: All students in a class can view the timetable
--        Only homeroom teacher can edit the timetable
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
-- Homeroom Teacher: teacher1 (John Smith) - User ID: 3, Staff ID: 2
-- Session: MORNING (07:00-12:00)
-- Classroom: A
-- Academic Year: 2024-2025
-- Subject Teachers:
--   - Mathematics: teacher1 (Staff ID: 2)
--   - English: teacher2 (Staff ID: 3)
--   - Physics: teacher3 (Staff ID: 4)
--   - Chemistry: teacher1 (Staff ID: 2)
--   - Biology: teacher2 (Staff ID: 3)
--   - History: teacher3 (Staff ID: 4)
--   - Geography: teacher1 (Staff ID: 2)
--   - Literature: teacher2 (Staff ID: 3)
--   - Computer Science: teacher3 (Staff ID: 4)
--   - Arts: teacher1 (Staff ID: 2)

INSERT INTO timetables (class_id, day_of_week, session_type, time_slot, start_time, end_time, subject, classroom, subject_teacher_id, academic_year, status, created_by_id, created_at, updated_at) VALUES
-- MONDAY - MORNING (GROUP A)
(1, 'MONDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'MONDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- TUESDAY - MORNING (GROUP A)
(1, 'TUESDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'TUESDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- WEDNESDAY - MORNING (GROUP A)
(1, 'WEDNESDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'WEDNESDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- THURSDAY - MORNING (GROUP A)
(1, 'THURSDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'THURSDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- FRIDAY - MORNING (GROUP A)
(1, 'FRIDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'FRIDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),

-- SATURDAY - MORNING (GROUP A)
(1, 'SATURDAY', 'MORNING', 1, '07:00:00', '07:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 2, '08:00:00', '08:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 3, '09:00:00', '09:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 4, '10:15:00', '11:00:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 2, NOW(), NOW()),
(1, 'SATURDAY', 'MORNING', 5, '11:15:00', '12:00:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 2, NOW(), NOW());

-- =====================================================
-- INSERT TEST DATA FOR CLASS 10B (AFTERNOON - GROUP B)
-- =====================================================
-- Class 10B: ID = 2, 3 students (student4, student5, student6)
-- Homeroom Teacher: teacher2 (Sarah Johnson) - User ID: 4, Staff ID: 3
-- Session: AFTERNOON (13:00-18:00)
-- Classroom: A (same classroom, different time)
-- Academic Year: 2024-2025
-- Subject Teachers: Same as Class 10A

INSERT INTO timetables (class_id, day_of_week, session_type, time_slot, start_time, end_time, subject, classroom, subject_teacher_id, academic_year, status, created_by_id, created_at, updated_at) VALUES
-- MONDAY - AFTERNOON (GROUP B)
(2, 'MONDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'MONDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- TUESDAY - AFTERNOON (GROUP B)
(2, 'TUESDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'TUESDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- WEDNESDAY - AFTERNOON (GROUP B)
(2, 'WEDNESDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'WEDNESDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- THURSDAY - AFTERNOON (GROUP B)
(2, 'THURSDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'THURSDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- FRIDAY - AFTERNOON (GROUP B)
(2, 'FRIDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'English', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Chemistry', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Mathematics', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'Physics', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'FRIDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Computer Science', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),

-- SATURDAY - AFTERNOON (GROUP B)
(2, 'SATURDAY', 'AFTERNOON', 1, '13:00:00', '13:45:00', 'Arts', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 2, '14:00:00', '14:45:00', 'Literature', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 3, '15:00:00', '15:45:00', 'Geography', 'A', 2, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 4, '16:00:00', '16:45:00', 'History', 'A', 4, '2024-2025', 'ACTIVE', 3, NOW(), NOW()),
(2, 'SATURDAY', 'AFTERNOON', 5, '17:00:00', '17:45:00', 'Biology', 'A', 3, '2024-2025', 'ACTIVE', 3, NOW(), NOW());

-- =====================================================
-- VERIFY DATA
-- =====================================================

-- Count total timetable entries
SELECT COUNT(*) as total_timetable_entries FROM timetables;

-- Show Class 10A timetable (MORNING GROUP A)
SELECT
    c.class_name,
    c.section,
    t.day_of_week,
    t.session_type,
    t.time_slot,
    t.start_time,
    t.end_time,
    t.subject,
    t.classroom,
    t.academic_year
FROM timetables t
JOIN classes c ON t.class_id = c.id
WHERE c.id = 1
ORDER BY t.day_of_week, t.time_slot;

-- Show Class 10B timetable (AFTERNOON GROUP B)
SELECT
    c.class_name,
    c.section,
    t.day_of_week,
    t.session_type,
    t.time_slot,
    t.start_time,
    t.end_time,
    t.subject,
    t.classroom,
    t.academic_year
FROM timetables t
JOIN classes c ON t.class_id = c.id
WHERE c.id = 2
ORDER BY t.day_of_week, t.time_slot;

-- Show Monday schedule across both classes
SELECT
    c.class_name,
    c.section,
    t.session_type,
    t.time_slot,
    t.start_time,
    t.end_time,
    t.subject,
    t.classroom
FROM timetables t
JOIN classes c ON t.class_id = c.id
WHERE LOWER(t.day_of_week) = 'MONDAY'
ORDER BY t.session_type, t.time_slot;

-- =====================================================
-- NOTES FOR DEVELOPERS
-- =====================================================
-- 1. STUDENT VIEW ACCESS:
--    - All students in Class 10A can view timetables WHERE class_id = (their_class_id)
--    - Query: SELECT * FROM timetables WHERE class_id = student.class_id
--
-- 2. HOMEROOM TEACHER EDIT ACCESS:
--    - Only the homeroom teacher of Class 10A can edit/create/delete timetables
--    - Check: IF staff.class_teacher_id == schoolClass.classTeacher.id THEN ALLOW_EDIT
--    - Query: SELECT * FROM classes WHERE class_teacher_id = current_user.staff.id
--
-- 3. ACADEMIC YEAR:
--    - Always filter by current academic year (2024-2025)
--    - Add to WHERE clause: AND academic_year = '2024-2025'
--
-- 4. EFFICIENT QUERIES:
--    - Indexes created on: class_id, academic_year, day_of_week, session_type
--    - Use these in WHERE clauses for optimal performance
--
-- 5. AUTHORIZATION RULES:
--    - ADMIN: Can view and edit all timetables
--    - HOMEROOM_TEACHER: Can view and edit only their class timetable
--    - OTHER_TEACHERS: Can view but cannot edit
--    - STUDENTS: Can view only their own class timetable

