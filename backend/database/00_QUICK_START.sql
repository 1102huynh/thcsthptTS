-- ========================================
-- QUICK RUN SCRIPT - For MySQL Command Line or Workbench
-- ========================================

-- OPTION 1: Run directly from MySQL command line
-- Step 1: Login to MySQL
--   mysql -u root -p
-- Step 2: Run this script
--   source d:/learn/thcsthptTS/backend/database/MIGRATION_VIETNAMESE_EDUCATION.sql

-- OPTION 2: Run with database specified in one command
--   mysql -u root -p schoolmanagement < d:/learn/thcsthptTS/backend/database/MIGRATION_VIETNAMESE_EDUCATION.sql

-- OPTION 3: Run from MySQL Workbench
--   File > Open SQL Script > Select MIGRATION_VIETNAMESE_EDUCATION.sql > Execute (Ctrl+Shift+Enter)

-- ========================================
-- VERIFY DATABASE EXISTS
-- ========================================

-- Create database if not exists (optional - usually already exists)
CREATE DATABASE IF NOT EXISTS schoolmanagement 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Use the database
USE schoolmanagement;

-- Show current database
SELECT DATABASE() AS 'Current Database';

-- Check existing tables
SHOW TABLES;

-- If you see: users, staff, students tables, you're good to go!
-- If not, you need to run the main schema first.

-- ========================================
-- READY TO RUN MIGRATION
-- ========================================
-- After verification, run: MIGRATION_VIETNAMESE_EDUCATION.sql
