-- =====================================================
-- School Management System - Test Data SQL (PostgreSQL)
-- =====================================================
-- Database: defaultdb (Aiven Cloud)
-- Date: November 17, 2025
-- Purpose: DELETE ALL OLD DATA, then INSERT fresh test data
-- DATABASE: PostgreSQL 12+ (Aiven Cloud)
-- METHOD: DELETE with proper foreign key ordering
-- =====================================================

-- =====================================================
-- STEP 1: DELETE ALL OLD DATA FROM ALL TABLES
-- =====================================================
-- INTENTIONAL: Delete all data without WHERE clause
-- Purpose: Clean database before fresh test data import
-- This is SAFE because:
--   1. This is a TEST DATA script
--   2. We immediately reset sequences
--   3. We immediately insert fresh data
-- Delete in correct order to avoid foreign key constraint violations
-- Delete dependent tables FIRST, then parent tables

-- Delete from tables with foreign keys first (INTENTIONAL - clearing all data)
DELETE FROM user_permissions;
DELETE FROM book_transactions;
DELETE FROM fees;
DELETE FROM grades;
DELETE FROM attendance;
DELETE FROM library_books;
DELETE FROM classes CASCADE;
DELETE FROM students;
DELETE FROM staff;
DELETE FROM users;

-- All data from all tables has been deleted above
-- Sequences will be reset in the next step
-- Fresh data will be inserted in step 3

-- =====================================================
-- STEP 2: RESET ALL ID SEQUENCES
-- =====================================================
-- Reset all sequences to start from 1
-- Using IF EXISTS to avoid errors if sequence doesn't exist

ALTER SEQUENCE IF EXISTS user_permissions_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS book_transactions_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS fees_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS grades_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS attendance_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS library_books_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS classes_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS students_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS staff_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;

-- =====================================================
-- STEP 3: INSERT FRESH NEW DATA
-- =====================================================

-- =====================================================
-- 1. USERS TABLE - Test Users
-- =====================================================

INSERT INTO users (username, email, password, first_name, last_name, phone_number, role, enabled, created_at, updated_at) VALUES
('admin', 'admin@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Admin', 'User', '9876543210', 'ADMIN', true, NOW(), NOW()),
('principal', 'principal@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Principal', 'School', '9876543211', 'PRINCIPAL', true, NOW(), NOW()),
('teacher1', 'teacher1@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'John', 'Smith', '9876543212', 'TEACHER', true, NOW(), NOW()),
('teacher2', 'teacher2@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Sarah', 'Johnson', '9876543213', 'TEACHER', true, NOW(), NOW()),
('teacher3', 'teacher3@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Michael', 'Brown', '9876543214', 'TEACHER', true, NOW(), NOW()),
('librarian', 'librarian@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Emily', 'Davis', '9876543215', 'LIBRARIAN', true, NOW(), NOW()),
('accountant', 'accountant@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Robert', 'Wilson', '9876543216', 'ACCOUNTANT', true, NOW(), NOW()),
('student1', 'student1@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Raj', 'Kumar', '9876543217', 'STUDENT', true, NOW(), NOW()),
('student2', 'student2@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Priya', 'Singh', '9876543218', 'STUDENT', true, NOW(), NOW()),
('student3', 'student3@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Amit', 'Patel', '9876543219', 'STUDENT', true, NOW(), NOW()),
('student4', 'student4@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Anjali', 'Sharma', '9876543220', 'STUDENT', true, NOW(), NOW()),
('student5', 'student5@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Arjun', 'Verma', '9876543221', 'STUDENT', true, NOW(), NOW()),
('student6', 'student6@school.com', '$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm', 'Divya', 'Nair', '9876543222', 'STUDENT', true, NOW(), NOW());

-- =====================================================
-- 2. STAFF TABLE - Employee Information
-- =====================================================

INSERT INTO staff (employee_id, user_id, position, department, date_of_birth, date_of_joining, qualification, subject_specialization, salary, status, address, city, state, postal_code, emergency_contact_name, emergency_contact_phone, created_at, updated_at) VALUES
('EMP001', 2, 'PRINCIPAL', 'Administration', '1970-05-15', '2010-01-01', 'M.A. Education', 'General Administration', 150000.00, 'ACTIVE', '123 Principal Lane', 'Delhi', 'Delhi', '110001', 'Rajesh Singh', '9999999999', NOW(), NOW()),
('EMP002', 3, 'TEACHER', 'English', '1985-08-22', '2015-06-01', 'B.Ed', 'English Literature', 50000.00, 'ACTIVE', '456 Teacher Road', 'Delhi', 'Delhi', '110002', 'Mrs. Smith', '9888888888', NOW(), NOW()),
('EMP003', 4, 'TEACHER', 'Mathematics', '1987-03-18', '2016-07-01', 'B.Ed', 'Mathematics', 50000.00, 'ACTIVE', '789 Math Street', 'Delhi', 'Delhi', '110003', 'Mr. Johnson', '9777777777', NOW(), NOW()),
('EMP004', 5, 'TEACHER', 'Science', '1986-11-30', '2014-08-15', 'B.Sc, B.Ed', 'Physics', 50000.00, 'ACTIVE', '321 Science Avenue', 'Delhi', 'Delhi', '110004', 'Dr. Brown', '9666666666', NOW(), NOW()),
('EMP005', 6, 'LIBRARIAN', 'Library', '1988-07-12', '2018-09-01', 'B.Lib', 'Library Science', 40000.00, 'ACTIVE', '654 Library Plaza', 'Delhi', 'Delhi', '110005', 'Ms. Davis', '9555555555', NOW(), NOW()),
('EMP006', 7, 'ACCOUNTANT', 'Finance', '1989-02-25', '2017-04-01', 'B.Com', 'Accounting', 45000.00, 'ACTIVE', '987 Finance Tower', 'Delhi', 'Delhi', '110006', 'Mr. Wilson', '9444444444', NOW(), NOW());

-- =====================================================
-- 3. STUDENTS TABLE - Student Information
-- =====================================================

INSERT INTO students (roll_number, admission_number, user_id, date_of_birth, gender, blood_group, class_name, section, date_of_admission, status, father_name, father_phone, father_occupation, mother_name, mother_phone, mother_occupation, address, city, state, postal_code, emergency_contact_name, emergency_contact_phone, emergency_contact_relation, created_at, updated_at) VALUES
('10A001', 'ADM001', 8, '2009-05-10', 'Male', 'O+', '10', 'A', '2023-06-01', 'ACTIVE', 'Rajesh Kumar', '9111111111', 'Engineer', 'Sunita Kumar', '9222222222', 'Homemaker', '100 Student Lane', 'Delhi', 'Delhi', '110101', 'Uncle Vikram', '9333333333', 'Uncle', NOW(), NOW()),
('10A002', 'ADM002', 9, '2009-07-15', 'Female', 'AB+', '10', 'A', '2023-06-01', 'ACTIVE', 'Ramesh Singh', '9111111112', 'Doctor', 'Kavya Singh', '9222222223', 'Teacher', '101 Student Lane', 'Delhi', 'Delhi', '110102', 'Aunt Neha', '9333333334', 'Aunt', NOW(), NOW()),
('10A003', 'ADM003', 10, '2009-03-20', 'Male', 'B+', '10', 'A', '2023-06-01', 'ACTIVE', 'Vikram Patel', '9111111113', 'Business', 'Anjali Patel', '9222222224', 'Accountant', '102 Student Lane', 'Delhi', 'Delhi', '110103', 'Grandfather', '9333333335', 'Grandfather', NOW(), NOW()),
('10B001', 'ADM004', 11, '2009-08-12', 'Female', 'A+', '10', 'B', '2023-06-01', 'ACTIVE', 'Suresh Sharma', '9111111114', 'Banker', 'Priya Sharma', '9222222225', 'Nurse', '103 Student Lane', 'Delhi', 'Delhi', '110104', 'Sister Pooja', '9333333336', 'Sister', NOW(), NOW()),
('10B002', 'ADM005', 12, '2009-09-08', 'Male', 'O-', '10', 'B', '2023-06-01', 'ACTIVE', 'Arjun Verma', '9111111115', 'Lawyer', 'Divya Verma', '9222222226', 'Designer', '104 Student Lane', 'Delhi', 'Delhi', '110105', 'Cousin Rohit', '9333333337', 'Cousin', NOW(), NOW()),
('10B003', 'ADM006', 13, '2009-10-25', 'Female', 'B-', '10', 'B', '2023-06-01', 'ACTIVE', 'Ashok Nair', '9111111116', 'Manager', 'Lakshmi Nair', '9222222227', 'Homemaker', '105 Student Lane', 'Delhi', 'Delhi', '110106', 'Neighbor Uncle', '9333333338', 'Guardian', NOW(), NOW());

-- =====================================================
-- 4. CLASSES TABLE - School Classes
-- =====================================================
-- Note: Using ON CONFLICT to handle cases where class_name + section + academic_year combination already exists
-- If the exact same class already exists, skip it (do nothing)

INSERT INTO classes (class_name, section, academic_year, created_at, updated_at) VALUES
('10', 'A', '2024-2025', NOW(), NOW()),
('10', 'B', '2024-2025', NOW(), NOW()),
('9', 'A', '2024-2025', NOW(), NOW()),
('9', 'B', '2024-2025', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =====================================================
-- 5. LIBRARY_BOOKS TABLE - Book Catalog
-- =====================================================

INSERT INTO library_books (isbn, title, author, publisher, publication_year, edition, category, total_copies, available_copies, description, location_rack, call_number, status, price, acquisition_date, created_at, updated_at) VALUES
('978-0-06-112008-4', 'To Kill a Mockingbird', 'Harper Lee', 'J.B. Lippincott', 1960, '1st', 'FICTION', 5, 4, 'Classic American novel about racial injustice', 'A-1', '813.54', 'AVAILABLE', 300.00, '2023-01-15', NOW(), NOW()),
('978-0-7432-7356-5', 'The Great Gatsby', 'F. Scott Fitzgerald', 'Scribner', 1925, '1st', 'FICTION', 5, 3, 'Jazz Age masterpiece', 'A-2', '813.52', 'AVAILABLE', 280.00, '2023-02-10', NOW(), NOW()),
('978-81-203-2356-8', 'Mathematics for Class 10', 'R.D. Sharma', 'Dhanpat Rai Publications', 2022, '1st', 'ACADEMIC', 10, 8, 'Complete Mathematics textbook', 'B-1', '510', 'AVAILABLE', 450.00, '2023-03-20', NOW(), NOW()),
('978-81-7525-737-7', 'Advanced Mathematics', 'S.L. Loney', 'Arihant Publications', 2021, '1st', 'ACADEMIC', 5, 2, 'Advanced math problems and solutions', 'B-2', '515', 'AVAILABLE', 550.00, '2023-04-05', NOW(), NOW()),
('978-81-7525-500-7', 'Physics Textbook Class 10', 'H.C. Verma', 'Bharati Bhawan', 2022, '1st', 'ACADEMIC', 8, 6, 'Complete Physics concepts', 'C-1', '530', 'AVAILABLE', 520.00, '2023-05-12', NOW(), NOW()),
('978-81-318-0059-2', 'Chemistry Class 10', 'N.C.E.R.T', 'NCERT Publications', 2022, '1st', 'ACADEMIC', 8, 5, 'State board Chemistry curriculum', 'C-2', '540', 'AVAILABLE', 400.00, '2023-06-08', NOW(), NOW()),
('978-81-7525-700-1', 'Indian History', 'Ramesh Chandra Majumdar', 'Bharati Bhawan', 2021, '1st', 'ACADEMIC', 5, 3, 'Comprehensive Indian history', 'D-1', '954', 'AVAILABLE', 480.00, '2023-07-14', NOW(), NOW()),
('978-0-7432-7357-2', 'English Grammar Guide', 'Raymond Murphy', 'Cambridge University Press', 2019, '3rd', 'REFERENCE', 6, 4, 'Complete English grammar reference', 'E-1', '425', 'AVAILABLE', 350.00, '2023-08-22', NOW(), NOW());

-- =====================================================
-- 6. ATTENDANCE TABLE - Student Attendance
-- =====================================================

INSERT INTO attendance (student_id, attendance_date, status, remarks, marked_by, created_at, updated_at) VALUES
(1, '2025-11-01', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(1, '2025-11-02', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(1, '2025-11-03', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(1, '2025-11-04', 'ABSENT', 'Medical reason', 3, NOW(), NOW()),
(1, '2025-11-05', 'LATE', 'Came late 30 mins', 3, NOW(), NOW()),
(2, '2025-11-01', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(2, '2025-11-02', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(2, '2025-11-03', 'ABSENT', 'Medical reason', 3, NOW(), NOW()),
(2, '2025-11-04', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(2, '2025-11-05', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(3, '2025-11-01', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(3, '2025-11-02', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(3, '2025-11-03', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(3, '2025-11-04', 'PRESENT', 'Regular attendance', 3, NOW(), NOW()),
(3, '2025-11-05', 'SICK_LEAVE', 'Medical leave', 3, NOW(), NOW());

-- =====================================================
-- 7. GRADES TABLE - Student Grades
-- =====================================================

INSERT INTO grades (student_id, subject, exam_type, marks_obtained, total_marks, percentage, grade, teacher_id, academic_year, remarks, created_at, updated_at) VALUES
(1, 'English', 'Midterm', 85, 100, 85.0, 'A', 3, '2024-2025', 'Good performance', NOW(), NOW()),
(1, 'Mathematics', 'Midterm', 92, 100, 92.0, 'A+', 4, '2024-2025', 'Excellent in calculations', NOW(), NOW()),
(1, 'Science', 'Midterm', 78, 100, 78.0, 'B+', 5, '2024-2025', 'Need improvement in practicals', NOW(), NOW()),
(2, 'English', 'Midterm', 88, 100, 88.0, 'A', 3, '2024-2025', 'Excellent in language', NOW(), NOW()),
(2, 'Mathematics', 'Midterm', 95, 100, 95.0, 'A+', 4, '2024-2025', 'Outstanding performer', NOW(), NOW()),
(2, 'Science', 'Midterm', 82, 100, 82.0, 'A', 5, '2024-2025', 'Good in theory and practicals', NOW(), NOW()),
(3, 'English', 'Midterm', 75, 100, 75.0, 'B+', 3, '2024-2025', 'Average performance', NOW(), NOW()),
(3, 'Mathematics', 'Midterm', 88, 100, 88.0, 'A', 4, '2024-2025', 'Strong in problem solving', NOW(), NOW()),
(3, 'Science', 'Midterm', 80, 100, 80.0, 'A', 5, '2024-2025', 'Good understanding of concepts', NOW(), NOW());

-- =====================================================
-- 8. FEES TABLE - Student Fees
-- =====================================================

INSERT INTO fees (student_id, academic_year, fee_type, amount, due_date, paid_date, status, paid_amount, remaining_amount, payment_method, transaction_id, remarks, created_at, updated_at) VALUES
(1, '2024-2025', 'Tuition', 50000, '2025-06-30', NULL, 'PENDING', 0, 50000, NULL, NULL, 'Tuition fee for academic year', NOW(), NOW()),
(1, '2024-2025', 'Library', 1000, '2025-07-31', '2025-07-25', 'PAID', 1000, 0, 'ONLINE', 'TXN001', 'Library fee paid', NOW(), NOW()),
(2, '2024-2025', 'Tuition', 50000, '2025-06-30', '2025-06-25', 'PAID', 50000, 0, 'CHEQUE', 'CHQ001', 'Paid via cheque', NOW(), NOW()),
(2, '2024-2025', 'Library', 1000, '2025-07-31', '2025-07-20', 'PAID', 1000, 0, 'ONLINE', 'TXN002', 'Library fee paid', NOW(), NOW()),
(2, '2024-2025', 'Sports', 5000, '2025-08-31', '2025-08-28', 'PAID', 5000, 0, 'ONLINE', 'TXN003', 'Sports fee paid', NOW(), NOW()),
(3, '2024-2025', 'Tuition', 50000, '2025-06-30', '2025-06-15', 'PAID', 30000, 20000, 'ONLINE', 'TXN004', 'Partial payment done', NOW(), NOW()),
(3, '2024-2025', 'Library', 1000, '2025-07-31', NULL, 'PENDING', 0, 1000, NULL, NULL, 'Library fee pending', NOW(), NOW());

-- =====================================================
-- 9. BOOK_TRANSACTIONS TABLE - Library Transactions
-- =====================================================

INSERT INTO book_transactions (book_id, user_id, transaction_type, borrow_date, due_date, return_date, fine_amount, fine_paid, created_at, updated_at) VALUES
(1, 8, 'BORROW', '2025-11-01', '2025-11-15', NULL, 0, false, NOW(), NOW()),
(3, 8, 'BORROW', '2025-11-02', '2025-11-16', '2025-11-14', 0, false, NOW(), NOW()),
(2, 9, 'BORROW', '2025-11-03', '2025-11-17', NULL, 0, false, NOW(), NOW()),
(5, 9, 'BORROW', '2025-10-20', '2025-11-03', '2025-11-08', 50, true, NOW(), NOW()),
(4, 10, 'BORROW', '2025-11-04', '2025-11-18', NULL, 0, false, NOW(), NOW()),
(6, 10, 'BORROW', '2025-11-05', '2025-11-19', NULL, 0, false, NOW(), NOW());

-- =====================================================
-- 10. USER_PERMISSIONS TABLE - Access Control
-- =====================================================
-- Using actual Permission enum values from the system
-- Admin user (id=1) gets all permissions
-- Principal (id=2) gets read/update permissions
-- Teachers (id=3,4) get specific permissions
-- Librarian (id=6) gets book management permissions
-- Accountant (id=7) gets fee management permissions
-- Students (id=8,9,10) get read permissions only

INSERT INTO user_permissions (user_id, permission, granted_at, granted_by) VALUES
-- Admin - Full access to all resources
(1, 'CREATE_STAFF', NOW(), NULL),
(1, 'READ_STAFF', NOW(), NULL),
(1, 'UPDATE_STAFF', NOW(), NULL),
(1, 'DELETE_STAFF', NOW(), NULL),
(1, 'CREATE_STUDENT', NOW(), NULL),
(1, 'READ_STUDENT', NOW(), NULL),
(1, 'UPDATE_STUDENT', NOW(), NULL),
(1, 'DELETE_STUDENT', NOW(), NULL),
(1, 'CREATE_BOOK', NOW(), NULL),
(1, 'READ_BOOK', NOW(), NULL),
(1, 'UPDATE_BOOK', NOW(), NULL),
(1, 'DELETE_BOOK', NOW(), NULL),
(1, 'CREATE_CLASS', NOW(), NULL),
(1, 'READ_CLASS', NOW(), NULL),
(1, 'UPDATE_CLASS', NOW(), NULL),
(1, 'DELETE_CLASS', NOW(), NULL),
(1, 'CREATE_GRADE', NOW(), NULL),
(1, 'READ_GRADE', NOW(), NULL),
(1, 'UPDATE_GRADE', NOW(), NULL),
(1, 'DELETE_GRADE', NOW(), NULL),
(1, 'CREATE_ATTENDANCE', NOW(), NULL),
(1, 'READ_ATTENDANCE', NOW(), NULL),
(1, 'UPDATE_ATTENDANCE', NOW(), NULL),
(1, 'DELETE_ATTENDANCE', NOW(), NULL),
(1, 'CREATE_FEE', NOW(), NULL),
(1, 'READ_FEE', NOW(), NULL),
(1, 'UPDATE_FEE', NOW(), NULL),
(1, 'DELETE_FEE', NOW(), NULL),
(1, 'PROCESS_PAYMENT', NOW(), NULL),
(1, 'GENERATE_REPORT', NOW(), NULL),

-- Principal (id=2) - Read and update access
(2, 'READ_STAFF', NOW(), NULL),
(2, 'UPDATE_STAFF', NOW(), NULL),
(2, 'READ_STUDENT', NOW(), NULL),
(2, 'UPDATE_STUDENT', NOW(), NULL),
(2, 'READ_BOOK', NOW(), NULL),
(2, 'READ_CLASS', NOW(), NULL),
(2, 'UPDATE_CLASS', NOW(), NULL),
(2, 'READ_GRADE', NOW(), NULL),
(2, 'READ_ATTENDANCE', NOW(), NULL),
(2, 'READ_FEE', NOW(), NULL),
(2, 'GENERATE_REPORT', NOW(), NULL),

-- Teachers (id=3,4,5) - Grade, Attendance, and Class permissions
(3, 'CREATE_GRADE', NOW(), NULL),
(3, 'READ_GRADE', NOW(), NULL),
(3, 'UPDATE_GRADE', NOW(), NULL),
(3, 'CREATE_ATTENDANCE', NOW(), NULL),
(3, 'READ_ATTENDANCE', NOW(), NULL),
(3, 'UPDATE_ATTENDANCE', NOW(), NULL),
(3, 'READ_CLASS', NOW(), NULL),
(3, 'READ_STUDENT', NOW(), NULL),
(3, 'GENERATE_REPORT', NOW(), NULL),

(4, 'CREATE_GRADE', NOW(), NULL),
(4, 'READ_GRADE', NOW(), NULL),
(4, 'UPDATE_GRADE', NOW(), NULL),
(4, 'CREATE_ATTENDANCE', NOW(), NULL),
(4, 'READ_ATTENDANCE', NOW(), NULL),
(4, 'UPDATE_ATTENDANCE', NOW(), NULL),
(4, 'READ_CLASS', NOW(), NULL),
(4, 'READ_STUDENT', NOW(), NULL),
(4, 'GENERATE_REPORT', NOW(), NULL),

(5, 'CREATE_GRADE', NOW(), NULL),
(5, 'READ_GRADE', NOW(), NULL),
(5, 'UPDATE_GRADE', NOW(), NULL),
(5, 'CREATE_ATTENDANCE', NOW(), NULL),
(5, 'READ_ATTENDANCE', NOW(), NULL),
(5, 'UPDATE_ATTENDANCE', NOW(), NULL),
(5, 'READ_CLASS', NOW(), NULL),
(5, 'READ_STUDENT', NOW(), NULL),
(5, 'GENERATE_REPORT', NOW(), NULL),

-- Librarian (id=6) - Book management permissions
(6, 'CREATE_BOOK', NOW(), NULL),
(6, 'READ_BOOK', NOW(), NULL),
(6, 'UPDATE_BOOK', NOW(), NULL),
(6, 'BORROW_BOOK', NOW(), NULL),
(6, 'RETURN_BOOK', NOW(), NULL),

-- Accountant (id=7) - Fee and payment permissions
(7, 'READ_FEE', NOW(), NULL),
(7, 'UPDATE_FEE', NOW(), NULL),
(7, 'CREATE_FEE', NOW(), NULL),
(7, 'PROCESS_PAYMENT', NOW(), NULL),

-- Students (id=8,9,10) - Read-only access
(8, 'READ_STUDENT', NOW(), NULL),
(8, 'READ_GRADE', NOW(), NULL),
(8, 'READ_BOOK', NOW(), NULL),
(8, 'READ_FEE', NOW(), NULL),

(9, 'READ_STUDENT', NOW(), NULL),
(9, 'READ_GRADE', NOW(), NULL),
(9, 'READ_BOOK', NOW(), NULL),
(9, 'READ_FEE', NOW(), NULL),

(10, 'READ_STUDENT', NOW(), NULL),
(10, 'READ_GRADE', NOW(), NULL),
(10, 'READ_BOOK', NOW(), NULL),
(10, 'READ_FEE', NOW(), NULL);

-- =====================================================
-- DATA IMPORT COMPLETE - All data deleted and reinserted
-- =====================================================
-- Total: 13 users, 6 staff, 6 students, 4 classes, 8 books
-- Attendance: 15 records, Grades: 9 records, Fees: 7 records
-- Book Transactions: 6 records, Permissions: 21+ records
-- All passwords: Test@123
-- Database: PostgreSQL (Aiven Cloud)
-- Status: All old data deleted, new data inserted
-- =====================================================

