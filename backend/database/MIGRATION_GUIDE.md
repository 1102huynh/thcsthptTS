# 📚 DATABASE MIGRATION - VIETNAMESE EDUCATION SYSTEM

## 🎯 **MỤC ĐÍCH**

Migration này thêm hỗ trợ cho mô hình giáo dục Việt Nam với:
- **Khối lớp** (Grade Levels): 6, 7, 8, 9, 10, 11, 12
- **Lớp học** (Classes): 6A, 6B, 10A1, etc.
- **Môn học** (Subjects): Toán, Văn, Anh, Lý, Hóa, etc.
- **Phân công giảng dạy** (Subject Assignments)
- **Giáo viên chủ nhiệm** (Homeroom Teachers)

---

## 📋 **CÁC BẢNG MỚI ĐƯỢC TẠO**

1. **grade_levels** - Quản lý khối (6-12)
2. **classes** - Quản lý lớp học (6A, 7B, 10A1...)
3. **subjects** - Quản lý môn học
4. **class_subject_assignments** - Phân công GV dạy môn
5. **teacher_specializations** - Chuyên môn GV
6. **timetables** - Thời khóa biểu

---

## 🚀 **CÁCH CHẠY MIGRATION**

### **Option 1: MySQL Command Line**

```bash
# 1. Mở MySQL command line
mysql -u root -p

# 2. File đã có USE schoolmanagement; nên chỉ cần chạy:
source d:/learn/thcsthptTS/backend/database/MIGRATION_VIETNAMESE_EDUCATION.sql;

# HOẶC nếu muốn chắc chắn:
USE schoolmanagement;
source d:/learn/thcsthptTS/backend/database/MIGRATION_VIETNAMESE_EDUCATION.sql;
```

### **Option 2: MySQL Workbench**

1. Mở MySQL Workbench
2. Connect vào server (không cần chọn database cụ thể)
3. File → Open SQL Script
4. Chọn file: `MIGRATION_VIETNAMESE_EDUCATION.sql`
5. Click Execute (⚡) hoặc Ctrl+Shift+Enter
6. **Lưu ý:** File đã có `USE schoolmanagement;` ở đầu rồi!

### **Option 3: DBeaver / DataGrip**

1. Connect vào MySQL server
2. Right-click database `schoolmanagement` → SQL Editor
3. Copy paste nội dung file `MIGRATION_VIETNAMESE_EDUCATION.sql`
4. Execute

### **Option 4: Command Line (Single Command - RECOMMENDED)**

```bash
# Cách này KHÔNG cần USE database vì đã chỉ định DB trong lệnh
mysql -u root -p schoolmanagement < "d:/learn/thcsthptTS/backend/database/MIGRATION_VIETNAMESE_EDUCATION.sql"
```


---

## ✅ **SAU KHI CHẠY MIGRATION**

### **Kiểm tra tables đã tạo:**

```sql
SHOW TABLES;

-- Kết quả sẽ có thêm:
-- grade_levels
-- classes
-- subjects
-- class_subject_assignments
-- teacher_specializations  
-- timetables
```

### **Kiểm tra data sample:**

```sql
-- Xem khối lớp (7 khối)
SELECT * FROM grade_levels;

-- Xem lớp học (28 lớp)
SELECT * FROM classes;

-- Xem môn học (14 môn)
SELECT * FROM subjects;
```

### **Kiểm tra students table đã update:**

```sql
DESCRIBE students;

-- Sẽ có 3 cột mới:
-- grade_level_id
-- class_id
-- academic_year
```

---

## 📊 **DATA SAU KHI MIGRATION**

### **Grade Levels (7 khối):**
- Khối 6, 7, 8, 9 (THCS)
- Khối 10, 11, 12 (THPT)

### **Classes (28 lớp):**
- Khối 6: 6A, 6B, 6C, 6D
- Khối 7: 7A, 7B, 7C, 7D
- Khối 8: 8A, 8B, 8C, 8D
- Khối 9: 9A, 9B, 9C, 9D
- Khối 10: 10A1, 10A2, 10A3, 10A4
- Khối 11: 11A1, 11A2, 11A3, 11A4
- Khối 12: 12A1, 12A2, 12A3, 12A4

### **Subjects (14 môn):**
- **Tự nhiên:** Toán, Lý, Hóa, Sinh, Tin học
- **Xã hội:** Văn, Sử, Địa, GDCD
- **Ngoại ngữ:** Tiếng Anh
- **Khác:** TD, Âm nhạc, Mỹ thuật, Công nghệ

---

## 🔄 **ROLLBACK (NẾU CẦN)**

Nếu muốn undo migration:

```bash
mysql -u root -p schoolmanagement < "d:/learn/thcsthptTS/backend/database/ROLLBACK_VIETNAMESE_EDUCATION.sql"
```

⚠️ **WARNING:** Rollback sẽ XÓA TẤT CẢ DATA trong các bảng mới!

---

## 🔧 **BƯỚC TIẾP THEO**

Sau khi migration thành công, bạn cần:

### **1. Update Student Data**

Gán students vào lớp và khối:

```sql
-- Example: Gán student vào lớp 6A
UPDATE students 
SET grade_level_id = 1,  -- Khối 6
    class_id = 1,        -- Lớp 6A
    academic_year = '2024-2025'
WHERE rollNumber = 'HS001';
```

### **2. Assign Homeroom Teachers**

Gán GVCN cho lớp:

```sql
-- Example: Cô Lan làm GVCN lớp 6A
UPDATE classes 
SET homeroom_teacher_id = 3  -- ID của cô Lan trong bảng staff
WHERE class_name = '6A';
```

### **3. Create Subject Assignments**

Phân công GV dạy môn:

```sql
-- Example: Thầy Minh dạy Toán lớp 6A
INSERT INTO class_subject_assignments 
(class_id, subject_id, teacher_id, academic_year, semester, periods_per_week) 
VALUES 
(1, 1, 10, '2024-2025', 1, 5);
```

---

## 📝 **QUERIES HỮU ÍCH**

### **Xem tất cả lớp của một khối:**

```sql
SELECT c.class_name, c.room_number, c.current_students, c.max_students,
       CONCAT(u.first_name, ' ', u.last_name) AS homeroom_teacher
FROM classes c
JOIN grade_levels gl ON c.grade_level_id = gl.id
LEFT JOIN staff s ON c.homeroom_teacher_id = s.id
LEFT JOIN users u ON s.user_id = u.id
WHERE gl.level_number = 6;
```

### **Xem học sinh theo lớp:**

```sql
SELECT s.roll_number, 
       CONCAT(u.first_name, ' ', u.last_name) AS student_name,
       c.class_name,
       gl.level_name
FROM students s
JOIN users u ON s.user_id = u.id
LEFT JOIN classes c ON s.class_id = c.id
LEFT JOIN grade_levels gl ON s.grade_level_id = gl.id
WHERE c.class_name = '6A';
```

### **Xem phân công giảng dạy của GV:**

```sql
SELECT c.class_name,
       sub.subject_name,
       CONCAT(u.first_name, ' ', u.last_name) AS teacher_name,
       csa.periods_per_week
FROM class_subject_assignments csa
JOIN classes c ON csa.class_id = c.id
JOIN subjects sub ON csa.subject_id = sub.id
JOIN staff s ON csa.teacher_id = s.id
JOIN users u ON s.user_id = u.id
WHERE csa.teacher_id = 10;  -- Replace with actual teacher ID
```

---

## ⚠️ **LƯU Ý QUAN TRỌNG**

1. **Backup trước khi chạy:**
   ```bash
   mysqldump -u root -p schoolmanagement > backup_before_migration.sql
   ```

2. **Kiểm tra version MySQL:**
   - Migration yêu cầu MySQL 5.7+ hoặc MariaDB 10.2+

3. **Charset:**
   - Tất cả tables dùng `utf8mb4_unicode_ci` để hỗ trợ tiếng Việt

4. **Foreign Keys:**
   - Có ràng buộc khóa ngoại nên không thể xóa grade_level nếu còn classes
   - Không thể xóa class nếu còn students

---

## 📞 **HỖ TRỢ**

Nếu gặp lỗi khi chạy migration, check:

1. **MySQL service đang chạy**
2. **User có quyền CREATE TABLE, ALTER TABLE**
3. **Database 'schoolmanagement' tồn tại**
4. **Các bảng cũ (users, staff, students) đã tồn tại**

---

**File locations:**
- Migration: `backend/database/MIGRATION_VIETNAMESE_EDUCATION.sql`
- Rollback: `backend/database/ROLLBACK_VIETNAMESE_EDUCATION.sql`
- Documentation: `VIETNAMESE_EDUCATION_SYSTEM.md`

**Created:** 2025-12-30  
**Version:** 1.0  
**Status:** ✅ Ready to run
