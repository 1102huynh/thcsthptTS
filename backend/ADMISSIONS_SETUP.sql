-- Admissions Table Setup Script
-- Run this script to create the admissions table and insert sample data

-- Create admissions table
CREATE TABLE IF NOT EXISTS admissions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    grade VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    requirements TEXT NOT NULL,
    eligibility VARCHAR(500) NOT NULL,
    exam_date DATE NOT NULL,
    deadline DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    seats VARCHAR(100) NOT NULL,
    class_structure VARCHAR(200) NOT NULL,
    students_per_class VARCHAR(50) NOT NULL,
    contact VARCHAR(100) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on status for faster queries
CREATE INDEX IF NOT EXISTS idx_admissions_status ON admissions(status);
CREATE INDEX IF NOT EXISTS idx_admissions_academic_year ON admissions(academic_year);
CREATE INDEX IF NOT EXISTS idx_admissions_display_order ON admissions(display_order);

-- Insert sample admission data
INSERT INTO admissions (title, grade, description, requirements, eligibility, exam_date, deadline, status, seats, class_structure, students_per_class, contact, academic_year, display_order) VALUES
(
    'Transfer Student Admission - Level 1 to Level 2',
    'Level 2 (Secondary School - Cấp 2)',
    'Tay Son Secondary and High School announces transfer student admissions from Level 1 to Level 2 (Secondary School) for the academic year 2026-2027. We are accepting transfer students who have completed Level 1 education at other schools.',
    '["Level 1 Completion Certificate (Grade 5 Certificate)", "Academic Transcript (All years of Level 1)", "Birth Certificate (Original + 2 Copies)", "Transfer Certificate from Previous School", "Health Certificate from Certified Hospital", "Residence Certificate (Household Registration)", "Recent Passport-size Photos (6 copies - 3x4cm)", "Student Profile Folder"]',
    'Transfer students who have completed Level 1 (Primary School - Grades 1-5) from other schools',
    '2026-03-15',
    '2026-03-05',
    'OPEN',
    '5 classes × 40 students = 200 seats total',
    '5 classes (Class 6A, 6B, 6C, 6D, 6E)',
    '40 students per class',
    'admission@taysonsecondary.edu',
    '2026-2027',
    1
),
(
    'Transfer Student Admission - Level 2 to Level 3',
    'Level 3 (High School - Cấp 3)',
    'Tay Son Secondary and High School announces transfer student admissions from Level 2 to Level 3 (High School) for the academic year 2026-2027. We are accepting transfer students who have completed Level 2 education (Grade 9) at other secondary schools.',
    '["Level 2 Completion Certificate (Grade 9 Certificate)", "Academic Transcript (All years of Level 2 - Grades 6-9)", "Birth Certificate (Original + 2 Copies)", "Transfer Certificate from Previous School", "Good Conduct Certificate from Previous School", "Health Certificate from District Hospital or Higher", "Residence Certificate (Household Registration)", "Recent Passport-size Photos (8 copies - 3x4cm)", "Student Profile Folder with Achievements (if any)"]',
    'Transfer students who have completed Level 2 (Secondary School - Grades 6-9) from other schools',
    '2026-04-20',
    '2026-04-10',
    'OPEN',
    '4 classes × 40 students = 160 seats total',
    '4 classes (Class 10A, 10B, 10C, 10D)',
    '40 students per class',
    'admission@taysonsecondary.edu',
    '2026-2027',
    2
);

-- Verify the data was inserted
SELECT COUNT(*) as total_admissions FROM admissions;
SELECT * FROM admissions ORDER BY display_order;

