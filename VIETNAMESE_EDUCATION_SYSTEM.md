# 🏫 VIETNAMESE EDUCATION SYSTEM - DATABASE DESIGN

## 📊 **DATABASE SCHEMA FOR VIETNAMESE SCHOOL**

### **1. GRADE_LEVEL (Khối)**
```sql
CREATE TABLE grade_levels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    level_number INT NOT NULL,              -- 6, 7, 8, 9, 10, 11, 12
    level_name VARCHAR(50),                 -- "Khối 6", "Khối 7", etc.
    school_type VARCHAR(20),                -- "THCS" or "THPT"
    academic_year VARCHAR(20),              -- "2024-2025"
    head_teacher_id BIGINT,                 -- Tổ trưởng khối (optional)
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(level_number, academic_year)
);

-- Example data:
INSERT INTO grade_levels VALUES 
(1, 6, 'Khối 6', 'THCS', '2024-2025', NULL),
(2, 7, 'Khối 7', 'THCS', '2024-2025', NULL),
(3, 8, 'Khối 8', 'THCS', '2024-2025', NULL),
(4, 9, 'Khối 9', 'THCS', '2024-2025', NULL),
(5, 10, 'Khối 10', 'THPT', '2024-2025', NULL),
(6, 11, 'Khối 11', 'THPT', '2024-2025', NULL),
(7, 12, 'Khối 12', 'THPT', '2024-2025', NULL);
```

### **2. CLASS (Lớp học)**
```sql
CREATE TABLE classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    grade_level_id BIGINT NOT NULL,         -- Link to grade_level
    class_name VARCHAR(50) NOT NULL,        -- "6A", "6B", "10A1", etc.
    full_name VARCHAR(100),                 -- "Lớp 6A", "Lớp 10A1"
    homeroom_teacher_id BIGINT,             -- GVCN (Giáo viên chủ nhiệm)
    academic_year VARCHAR(20),              -- "2024-2025"
    max_students INT DEFAULT 40,            -- Sĩ số tối đa
    current_students INT DEFAULT 0,         -- Sĩ số hiện tại
    room_number VARCHAR(20),                -- Phòng học: "A101", "B205"
    status VARCHAR(20) DEFAULT 'ACTIVE',    -- ACTIVE, INACTIVE
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id),
    FOREIGN KEY (homeroom_teacher_id) REFERENCES staff(id),
    UNIQUE(class_name, academic_year)
);

-- Example data:
INSERT INTO classes VALUES
(1, 1, '6A', 'Lớp 6A', 3, '2024-2025', 40, 35, 'A101', 'ACTIVE'),
(2, 1, '6B', 'Lớp 6B', 4, '2024-2025', 40, 38, 'A102', 'ACTIVE'),
(3, 5, '10A1', 'Lớp 10A1', 5, '2024-2025', 40, 36, 'B201', 'ACTIVE');
```

### **3. SUBJECT (Môn học)**
```sql
CREATE TABLE subjects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subject_code VARCHAR(20) NOT NULL,      -- "TOAN", "VAN", "ANH"
    subject_name VARCHAR(100) NOT NULL,     -- "Toán học", "Ngữ văn"
    subject_name_en VARCHAR(100),           -- "Mathematics", "Literature"
    school_type VARCHAR(20),                -- "THCS", "THPT", or "BOTH"
    category VARCHAR(50),                   -- "Khoa học tự nhiên", "Xã hội"
    total_periods_per_week INT,             -- Số tiết/tuần
    coefficient DECIMAL(3,1) DEFAULT 1.0,   -- Hệ số môn học
    is_required BOOLEAN DEFAULT TRUE,       -- Môn bắt buộc
    created_at TIMESTAMP,
    UNIQUE(subject_code)
);

-- Example data:
INSERT INTO subjects VALUES
(1, 'TOAN', 'Toán học', 'Mathematics', 'BOTH', 'Khoa học tự nhiên', 5, 2.0, TRUE),
(2, 'VAN', 'Ngữ văn', 'Literature', 'BOTH', 'Xã hội', 5, 2.0, TRUE),
(3, 'ANH', 'Tiếng Anh', 'English', 'BOTH', 'Ngoại ngữ', 3, 1.0, TRUE),
(4, 'LY', 'Vật lý', 'Physics', 'BOTH', 'Khoa học tự nhiên', 3, 1.0, TRUE),
(5, 'HOA', 'Hóa học', 'Chemistry', 'BOTH', 'Khoa học tự nhiên', 2, 1.0, TRUE),
(6, 'SINH', 'Sinh học', 'Biology', 'BOTH', 'Khoa học tự nhiên', 2, 1.0, TRUE),
(7, 'SU', 'Lịch sử', 'History', 'BOTH', 'Xã hội', 2, 1.0, TRUE),
(8, 'DIA', 'Địa lý', 'Geography', 'BOTH', 'Xã hội', 2, 1.0, TRUE),
(9, 'GDCD', 'Giáo dục công dân', 'Civic Education', 'BOTH', 'Xã hội', 1, 1.0, TRUE),
(10, 'TD', 'Thể dục', 'Physical Education', 'BOTH', 'Khác', 2, 1.0, TRUE),
(11, 'TIN', 'Tin học', 'Computer Science', 'BOTH', 'Khoa học tự nhiên', 1, 1.0, TRUE);
```

### **4. CLASS_SUBJECT_ASSIGNMENT (Phân công giảng dạy)**
```sql
CREATE TABLE class_subject_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,               -- Lớp học
    subject_id BIGINT NOT NULL,             -- Môn học
    teacher_id BIGINT NOT NULL,             -- Giáo viên bộ môn
    academic_year VARCHAR(20),              -- "2024-2025"
    semester INT,                           -- 1 or 2 (HK1, HK2)
    periods_per_week INT,                   -- Số tiết/tuần
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (teacher_id) REFERENCES staff(id),
    UNIQUE(class_id, subject_id, semester, academic_year)
);

-- Example: Lớp 6A có nhiều giáo viên dạy các môn khác nhau
INSERT INTO class_subject_assignments VALUES
(1, 1, 1, 10, '2024-2025', 1, 5, '2024-09-01', '2025-01-31', 'ACTIVE'), -- GV Toán
(2, 1, 2, 11, '2024-2025', 1, 5, '2024-09-01', '2025-01-31', 'ACTIVE'), -- GV Văn
(3, 1, 3, 12, '2024-2025', 1, 3, '2024-09-01', '2025-01-31', 'ACTIVE'); -- GV Anh
```

### **5. TEACHER_SPECIALIZATION (Chuyên môn giáo viên)**
```sql
CREATE TABLE teacher_specializations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,       -- Bộ môn chính
    certification_level VARCHAR(50),        -- "Giỏi", "Khá", "Trung bình"
    years_of_experience INT,
    created_at TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES staff(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    UNIQUE(teacher_id, subject_id)
);
```

### **6. UPDATE STUDENT TABLE**
```sql
ALTER TABLE students 
ADD COLUMN grade_level_id BIGINT,
ADD COLUMN class_id BIGINT,
ADD COLUMN academic_year VARCHAR(20),
ADD FOREIGN KEY (grade_level_id) REFERENCES grade_levels(id),
ADD FOREIGN KEY (class_id) REFERENCES classes(id);

-- Remove old className, section columns
-- ALTER TABLE students DROP COLUMN className, DROP COLUMN section;
```

### **7. TIMETABLE (Thời khóa biểu)**
```sql
CREATE TABLE timetables (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    day_of_week INT,                        -- 2-7 (Thứ 2 - Thứ 7)
    period_number INT,                      -- 1-10 (Tiết 1-10)
    room_number VARCHAR(20),
    academic_year VARCHAR(20),
    semester INT,
    start_time TIME,
    end_time TIME,
    FOREIGN KEY (class_id) REFERENCES classes(id),
    FOREIGN KEY (subject_id) REFERENCES subjects(id),
    FOREIGN KEY (teacher_id) REFERENCES staff(id)
);
```

---

## 🎯 **MANAGEMENT HIERARCHY**

```
SCHOOL (Nhà trường)
├── THCS (Cấp 2)
│   ├── Khối 6
│   │   ├── Lớp 6A (GVCN: Cô Lan)
│   │   │   ├── Toán: Thầy Minh
│   │   │   ├── Văn: Cô Hương
│   │   │   └── Anh: Cô Mai
│   │   ├── Lớp 6B (GVCN: Thầy Nam)
│   │   └── Lớp 6C
│   ├── Khối 7
│   ├── Khối 8
│   └── Khối 9
└── THPT (Cấp 3)
    ├── Khối 10
    ├── Khối 11
    └── Khối 12
```

---

## 📋 **FEATURES CẦN THÊM**

### **1. Class Management (Quản lý lớp học)**
- Tạo/sửa/xóa lớp
- Gán GVCN cho lớp
- Gán giáo viên bộ môn
- Quản lý sĩ số lớp
- Chuyển lớp cho học sinh

### **2. Subject Assignment (Phân công giảng dạy)**
- Phân công GV dạy môn cho lớp
- Quản lý thời khóa biểu
- Theo dõi số tiết dạy của GV

### **3. Grade Level Management (Quản lý khối)**
- Tạo khối lớp theo năm học
- Thống kê theo khối
- Báo cáo khối

### **4. Homeroom Teacher Dashboard (Bảng điều khiển GVCN)**
- Xem danh sách học sinh lớp
- Điểm danh học sinh
- Nhận xét học sinh
- Liên hệ phụ huynh

---

## 🚀 **ĐỀ XUẤT TRIỂN KHAI**

### **PHASE 1: Database Migration** (Ưu tiên cao)
1. Tạo bảng grade_levels
2. Tạo bảng classes  
3. Tạo bảng subjects
4. Tạo bảng class_subject_assignments
5. Update bảng students

### **PHASE 2: Backend APIs**
1. Class CRUD APIs
2. Subject CRUD APIs
3. Assignment APIs
4. Grade Level APIs

### **PHASE 3: Frontend Pages**
1. Class Management page
2. Subject Management page
3. Teacher Assignment page
4. Grade Level Overview page

---

Bạn muốn tôi:
**A.** Tạo migration scripts cho database
**B.** Tạo backend entities & services
**C.** Tạo frontend pages mới
**D.** Tất cả (full implementation)

Cho tôi biết bạn muốn bắt đầu từ đâu! 🚀
