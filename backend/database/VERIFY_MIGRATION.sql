-- ========================================
-- VERIFICATION SCRIPT - Check Migration Success
-- Run this to verify database is set up correctly
-- ========================================

-- Select database
USE schoolmanagement;

-- Verify current database
SELECT DATABASE() AS Current_Database;

-- 1. Check all tables exist
SELECT 'Checking tables...' AS Step;
SELECT TABLE_NAME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'schoolmanagement' 
AND TABLE_NAME IN ('grade_levels', 'classes', 'subjects', 'class_subject_assignments', 'teacher_specializations', 'timetables')
ORDER BY TABLE_NAME;

-- 2. Check grade levels (should be 7)
SELECT 'Checking grade levels...' AS Step;
SELECT COUNT(*) AS Total_Grade_Levels FROM grade_levels;
SELECT * FROM grade_levels ORDER BY level_number;

-- 3. Check classes (should be 28)
SELECT 'Checking classes...' AS Step;
SELECT COUNT(*) AS Total_Classes FROM classes;
SELECT gl.level_name, COUNT(*) AS Class_Count
FROM classes c
JOIN grade_levels gl ON c.grade_level_id = gl.id
GROUP BY gl.level_name
ORDER BY gl.level_number;

-- 4. Check subjects (should be 14)
SELECT 'Checking subjects...' AS Step;
SELECT COUNT(*) AS Total_Subjects FROM subjects;
SELECT subject_code, subject_name, school_type, coefficient 
FROM subjects 
ORDER BY subject_code;

-- 5. Check students table updated
SELECT 'Checking students table structure...' AS Step;
DESCRIBE students;

-- 6. Show sample data
SELECT 'Sample Classes:' AS Info;
SELECT c.class_name, c.full_name, c.room_number, gl.level_name
FROM classes c
JOIN grade_levels gl ON c.grade_level_id = gl.id
LIMIT 10;

SELECT 'Migration verification complete!' AS Status;
