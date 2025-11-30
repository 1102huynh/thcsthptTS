# ✅ TIMETABLE - MySQL Compatibility Fix

**Date**: November 22, 2025
**Issue**: PostgreSQL syntax errors in MySQL database
**Status**: ✅ FIXED

---

## 🔧 Issue Found

**Error**: `Referencing column 'class_id' and referenced column 'id' in foreign key constraint are incompatible.`

**Cause**: The SQL script was using PostgreSQL syntax (`SERIAL`, `CASCADE`, etc.) on a MySQL database.

---

## 🛠️ What Was Wrong

### PostgreSQL Syntax (NOT compatible with MySQL)
```sql
-- Wrong for MySQL:
id SERIAL PRIMARY KEY
UNIQUE (class_id, day_of_week, ...)
DROP TABLE ... CASCADE
```

### MySQL Syntax (CORRECT)
```sql
-- Correct for MySQL:
id BIGINT AUTO_INCREMENT PRIMARY KEY
UNIQUE KEY unique_name (class_id, day_of_week, ...)
DROP TABLE ... (without CASCADE)
```

---

## ✅ What's Fixed

### 1. Primary Key Definition
```sql
-- PostgreSQL (WRONG)
id SERIAL PRIMARY KEY

-- MySQL (CORRECT)
id BIGINT AUTO_INCREMENT PRIMARY KEY
```

### 2. Column Types
```sql
-- PostgreSQL (WRONG)
class_id INTEGER
subject_teacher_id INTEGER
created_by_id INTEGER

-- MySQL (CORRECT) - Must match other tables
class_id BIGINT
subject_teacher_id BIGINT
created_by_id BIGINT
```

### 3. Unique Constraints
```sql
-- PostgreSQL (WRONG)
UNIQUE (class_id, day_of_week, time_slot, session_type)

-- MySQL (CORRECT) - Named constraint
UNIQUE KEY unique_timetable_slot (class_id, day_of_week, time_slot, session_type)
```

### 4. Updated Timestamp
```sql
-- PostgreSQL (WRONG)
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

-- MySQL (CORRECT)
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

### 5. Indexes Definition
```sql
-- PostgreSQL (WRONG)
CREATE INDEX idx_timetable_class_id ON timetables(class_id);

-- MySQL (CORRECT) - Create inside table definition
INDEX idx_timetable_class_id (class_id),
```

### 6. Engine & Charset
```sql
-- MySQL (CORRECT) - Added
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 📂 Files Updated

### TIMETABLE_SETUP.sql (Original - PostgreSQL)
- Location: `backend/TIMETABLE_SETUP.sql`
- Purpose: For PostgreSQL databases
- Status: Updated to MySQL syntax

### TIMETABLE_SETUP_MYSQL.sql (NEW - MySQL)
- Location: `backend/TIMETABLE_SETUP_MYSQL.sql`
- Purpose: For MySQL databases (Aiven Cloud)
- Status: ✅ Ready to use

---

## 🚀 How to Fix Your Database

### Option 1: Copy-Paste into Database Console

1. **Open Database Console** in IntelliJ
2. **Run this command**:
```sql
DROP TABLE IF EXISTS timetables;
```

3. **Copy entire content** from `TIMETABLE_SETUP_MYSQL.sql`
4. **Paste and execute** in the console

### Option 2: Run SQL File

```bash
mysql -u username -p school_management < TIMETABLE_SETUP_MYSQL.sql
```

---

## ✅ Verification

After running the SQL, verify with:

```sql
-- Check table created
SHOW TABLES LIKE 'timetables';

-- Check table structure
DESC timetables;

-- Count test data
SELECT COUNT(*) FROM timetables;
-- Expected: 60 rows

-- Verify foreign keys work
SELECT 
    COUNT(*) as total,
    SUM(CASE WHEN subject_teacher_id IS NOT NULL THEN 1 ELSE 0 END) as with_teacher
FROM timetables;
-- Expected: total=60, with_teacher=60
```

---

## 🔍 Key Differences: PostgreSQL vs MySQL

| Feature | PostgreSQL | MySQL |
|---------|------------|-------|
| Auto ID | `SERIAL` | `BIGINT AUTO_INCREMENT` |
| Constraint Name | Optional | Required for some |
| Update Timestamp | `DEFAULT NOW()` | `ON UPDATE CURRENT_TIMESTAMP` |
| Drop Cascade | `DROP ... CASCADE` | `DROP ... (without CASCADE)` |
| Indexes | Separate statements | Inside table definition |
| Engine | Not specified | `InnoDB` |
| Charset | Default | Specify explicitly |

---

## 📋 Complete MySQL Script

Use: `TIMETABLE_SETUP_MYSQL.sql`

**Contains**:
- ✅ MySQL-compatible CREATE TABLE
- ✅ All 60 test data rows (30 for Class 10A, 30 for Class 10B)
- ✅ Subject teacher assignments
- ✅ Proper foreign keys with BIGINT types
- ✅ Verification queries

---

## 🎯 Test After Setup

```sql
-- Should return Class 10A with teachers
SELECT 
    c.class_name,
    t.day_of_week,
    t.subject,
    CONCAT(u.first_name, ' ', u.last_name) as teacher
FROM timetables t
JOIN classes c ON t.class_id = c.id
LEFT JOIN staff s ON t.subject_teacher_id = s.id
LEFT JOIN users u ON s.user_id = u.id
WHERE c.id = 1 AND t.day_of_week = 'MONDAY'
LIMIT 5;
```

Expected output:
```
class_name | day_of_week | subject    | teacher
10A        | MONDAY      | Mathematics| John Smith
10A        | MONDAY      | English    | Sarah Johnson
10A        | MONDAY      | Physics    | Michael Brown
...
```

---

## ✅ Status

**✅ FIXED** - MySQL compatible SQL script created and ready to use!

All data types match, foreign keys are compatible, and the script follows MySQL best practices.

---

## 📞 If You Get Errors

### Error: Unknown table 'school_management.timetables'
**Solution**: This is normal on first run (table doesn't exist yet). Continue.

### Error: Column count doesn't match
**Solution**: Check that your table definitions match. All foreign key columns should be BIGINT.

### Error: Foreign key constraint fails
**Solution**: Verify that:
1. classes table exists
2. staff table exists
3. users table exists
4. Column types match (all BIGINT)

### Error: Duplicate entry
**Solution**: Run `DROP TABLE IF EXISTS timetables;` first to clean up.

---

**Next Step**: Copy content from `TIMETABLE_SETUP_MYSQL.sql` and run in your database console!

