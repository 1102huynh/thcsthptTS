-- ========================================
-- ROLLBACK SCRIPT - VIETNAMESE EDUCATION SYSTEM
-- Version: 1.0
-- Date: 2025-12-30
-- Description: Rollback migration if needed
-- WARNING: This will delete all data in new tables!
-- ========================================

-- Select database
USE schoolmanagement;

-- Verify current database
SELECT DATABASE() AS Current_Database;


-- Confirm before running
SELECT 'WARNING: This will delete all Vietnamese education system data!' AS Warning;
SELECT 'Press Ctrl+C to cancel or run to proceed' AS Action;

-- ========================================
-- 1. REMOVE FOREIGN KEYS FROM STUDENTS TABLE
-- ========================================
ALTER TABLE students DROP FOREIGN KEY IF EXISTS fk_students_grade_level;
ALTER TABLE students DROP FOREIGN KEY IF EXISTS fk_students_class;

-- ========================================
-- 2. REMOVE NEW COLUMNS FROM STUDENTS TABLE
-- ========================================
ALTER TABLE students 
DROP COLUMN IF EXISTS grade_level_id,
DROP COLUMN IF EXISTS class_id,
DROP COLUMN IF EXISTS academic_year;

-- ========================================
-- 3. DROP TABLES (In reverse order of dependencies)
-- ========================================
DROP TABLE IF EXISTS timetables;
DROP TABLE IF EXISTS teacher_specializations;
DROP TABLE IF EXISTS class_subject_assignments;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS grade_levels;

-- ========================================
-- ROLLBACK COMPLETE
-- ========================================
SELECT 'Rollback completed. All Vietnamese education system tables removed.' AS Status;
