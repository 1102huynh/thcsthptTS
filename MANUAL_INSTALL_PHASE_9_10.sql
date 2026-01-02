-- ============================================================================
-- MANUAL INSTALLATION SCRIPT FOR PHASE 9 & 10
-- Parent Portal and Analytics Tables
-- Database: school_management
-- ============================================================================

-- INSTRUCTIONS:
-- 1. Open MySQL command line or MySQL Workbench
-- 2. Run: USE school_management;
-- 3. Run this entire script

-- ============================================================================
-- STEP 1: Verify we're in the correct database
-- ============================================================================
SELECT DATABASE() AS Current_Database;
SHOW TABLES;  -- Show existing tables

-- ============================================================================
-- STEP 2: Create Parents table
-- ============================================================================
DROP TABLE IF EXISTS parent_student;
DROP TABLE IF EXISTS parent_teacher_messages;
DROP TABLE IF EXISTS parent_meetings;
DROP TABLE IF EXISTS parents;

CREATE TABLE parents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    relation_to_student VARCHAR(50),
    occupation VARCHAR(100),
    office_address VARCHAR(255),
    annual_income VARCHAR(50),
    notification_email_enabled BOOLEAN DEFAULT TRUE,
    notification_sms_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_parent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_parent_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verify table creation
SELECT 'parents table created' AS Status;
DESCRIBE parents;

-- ============================================================================
-- STEP 3: Create Parent-Student relationship table (many-to-many)
-- ============================================================================
CREATE TABLE parent_student (
    parent_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (parent_id, student_id),
    CONSTRAINT fk_parent_student_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_student_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    INDEX idx_parent_student_parent (parent_id),
    INDEX idx_parent_student_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'parent_student table created' AS Status;
DESCRIBE parent_student;

-- ============================================================================
-- STEP 4: Create Parent-Teacher Messages table
-- ============================================================================
CREATE TABLE parent_teacher_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    sender_id BIGINT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_teacher FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_message_parent (parent_id),
    INDEX idx_message_teacher (teacher_id),
    INDEX idx_message_student (student_id),
    INDEX idx_message_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'parent_teacher_messages table created' AS Status;
DESCRIBE parent_teacher_messages;

-- ============================================================================
-- STEP 5: Create Announcements table
-- ============================================================================
DROP TABLE IF EXISTS announcements;

CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    target_audience ENUM('ALL', 'PARENTS', 'STUDENTS', 'TEACHERS', 'STAFF') NOT NULL,
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',
    created_by BIGINT NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_announcement_published (published, published_at DESC),
    INDEX idx_announcement_target (target_audience),
    INDEX idx_announcement_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'announcements table created' AS Status;
DESCRIBE announcements;

-- ============================================================================
-- STEP 6: Create Parent Meetings table
-- ============================================================================
CREATE TABLE parent_meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    student_id BIGINT,
    meeting_date TIMESTAMP NOT NULL,
    purpose VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    status ENUM('SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED') DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_meeting_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE CASCADE,
    CONSTRAINT fk_meeting_teacher FOREIGN KEY (teacher_id) REFERENCES staff(id) ON DELETE CASCADE,
    CONSTRAINT fk_meeting_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    INDEX idx_meeting_parent (parent_id),
    INDEX idx_meeting_teacher (teacher_id),
    INDEX idx_meeting_date (meeting_date),
    INDEX idx_meeting_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'parent_meetings table created' AS Status;
DESCRIBE parent_meetings;

-- ============================================================================
-- STEP 7: Insert Sample Data
-- ============================================================================

-- Sample parent users
INSERT INTO users (username, email, password, first_name, last_name, phone_number, role, enabled, created_at, updated_at)
VALUES
('parent1', 'parent1@school.com', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KFC_Ci', 'Nguyen', 'Van A', '0901234567', 'PARENT', TRUE, NOW(), NOW()),
('parent2', 'parent2@school.com', '$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1KFC_Ci', 'Tran', 'Thi B', '0902345678', 'PARENT', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE username=username;

SELECT 'Sample parent users created' AS Status;

-- Sample parents
INSERT INTO parents (user_id, relation_to_student, occupation, notification_email_enabled, created_at, updated_at)
SELECT id, 'Father', 'Engineer', TRUE, NOW(), NOW() FROM users WHERE username = 'parent1'
ON DUPLICATE KEY UPDATE user_id=user_id;

INSERT INTO parents (user_id, relation_to_student, occupation, notification_email_enabled, created_at, updated_at)
SELECT id, 'Mother', 'Teacher', TRUE, NOW(), NOW() FROM users WHERE username = 'parent2'
ON DUPLICATE KEY UPDATE user_id=user_id;

SELECT 'Sample parents created' AS Status;

-- Sample announcement (only if there's a PRINCIPAL user)
INSERT INTO announcements (title, content, target_audience, priority, created_by, published, published_at, created_at, updated_at)
SELECT
    'Welcome to Parent Portal',
    'Dear Parents, We are pleased to announce the launch of our new Parent Portal. You can now view your child\'s grades, attendance, and communicate with teachers directly.',
    'PARENTS',
    'HIGH',
    id,
    TRUE,
    NOW(),
    NOW(),
    NOW()
FROM users WHERE role = 'PRINCIPAL' LIMIT 1;

SELECT 'Sample announcement created' AS Status;

-- ============================================================================
-- STEP 8: Verify All Tables Created
-- ============================================================================
SELECT 'All Phase 9 & 10 tables created successfully!' AS Status;

SHOW TABLES LIKE '%parent%';
SHOW TABLES LIKE 'announcements';

-- Check counts
SELECT
    (SELECT COUNT(*) FROM parents) AS parents_count,
    (SELECT COUNT(*) FROM announcements) AS announcements_count,
    (SELECT COUNT(*) FROM parent_teacher_messages) AS messages_count,
    (SELECT COUNT(*) FROM parent_meetings) AS meetings_count;

-- ============================================================================
-- DONE! Tables are ready to use.
-- ============================================================================

