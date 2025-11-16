# 📊 How to Import Test Data into Your Database

## 🎯 Quick Start - 3 Steps

### Step 1: Ensure Database Exists
```bash
mysql -u root -p
CREATE DATABASE school_management CHARACTER SET utf8mb4;
EXIT;
```

### Step 2: Import the SQL File
```bash
mysql -u root -p school_management < D:\learn\thcsthptTS\backend\TEST_DATA.sql
```

### Step 3: Verify Data Was Imported
```bash
mysql -u root -p
USE school_management;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_students FROM students;
SELECT COUNT(*) as total_books FROM library_books;
EXIT;
```

---

## 📋 Alternative Methods

### Method 1: Using MySQL Workbench
1. Open MySQL Workbench
2. Connect to your MySQL server
3. Go to **File** → **Open SQL Script**
4. Select `TEST_DATA.sql`
5. Click **Execute**
6. View results in Output panel

### Method 2: Using phpMyAdmin
1. Open phpMyAdmin (http://localhost/phpmyadmin)
2. Select `school_management` database
3. Go to **Import** tab
4. Click **Choose File**
5. Select `TEST_DATA.sql`
6. Click **Import**

### Method 3: Using MySQL Command Line (Step by Step)
```bash
# Connect to MySQL
mysql -u root -p

# Create database
CREATE DATABASE school_management CHARACTER SET utf8mb4;

# Select database
USE school_management;

# Source the file
SOURCE D:\learn\thcsthptTS\backend\TEST_DATA.sql;

# Verify
SELECT * FROM users LIMIT 5;

# Exit
EXIT;
```

---

## ✅ What Gets Imported

### Users (13 total)
- ✅ 1 Admin
- ✅ 1 Principal
- ✅ 3 Teachers
- ✅ 1 Librarian
- ✅ 1 Accountant
- ✅ 6 Students

### Staff (6 total)
- ✅ 1 Principal
- ✅ 3 Teachers
- ✅ 1 Librarian
- ✅ 1 Accountant

### Students (6 total)
- ✅ Class 10-A: 3 students
- ✅ Class 10-B: 3 students

### Classes (4 total)
- ✅ Class 10-A
- ✅ Class 10-B
- ✅ Class 9-A
- ✅ Class 9-B

### Books (8 total)
- ✅ 2 Fiction books
- ✅ 4 Academic books
- ✅ 2 Reference books

### Other Data
- ✅ 15 Attendance records
- ✅ 9 Grade records
- ✅ 7 Fee records
- ✅ 6 Book transactions
- ✅ Multiple permissions

---

## 🔑 Test Login Credentials

**All passwords**: `Test@123`

| Username | Role | Email |
|----------|------|-------|
| admin | ADMIN | admin@school.com |
| principal | PRINCIPAL | principal@school.com |
| teacher1 | TEACHER | teacher1@school.com |
| teacher2 | TEACHER | teacher2@school.com |
| teacher3 | TEACHER | teacher3@school.com |
| librarian | LIBRARIAN | librarian@school.com |
| accountant | ACCOUNTANT | accountant@school.com |
| student1 | STUDENT | student1@school.com |
| student2 | STUDENT | student2@school.com |
| student3 | STUDENT | student3@school.com |
| student4 | STUDENT | student4@school.com |
| student5 | STUDENT | student5@school.com |
| student6 | STUDENT | student6@school.com |

---

## 🧪 Test All Endpoints

### Example: Login as Admin
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Test@123"
  }'
```

### Example: Get All Staff
```bash
curl -X GET http://localhost:8080/api/v1/staff \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Example: Get Students by Class
```bash
curl -X GET "http://localhost:8080/api/v1/students/class/10?section=A" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Example: Get Attendance for Student
```bash
curl -X GET http://localhost:8080/api/v1/attendance/student/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Example: Get Student Grades
```bash
curl -X GET http://localhost:8080/api/v1/grades/student/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## 📊 Database Structure After Import

```
school_management/
├── users (13 records)
├── staff (6 records)
├── students (6 records)
├── classes (4 records)
├── attendance (15 records)
├── grades (9 records)
├── fees (7 records)
├── library_books (8 records)
├── book_transactions (6 records)
└── user_permissions (40+ records)
```

---

## 🔍 Verify Data with Queries

```sql
-- Check users
SELECT * FROM users;

-- Check staff
SELECT * FROM staff;

-- Check students
SELECT * FROM students;

-- Check attendance
SELECT * FROM attendance;

-- Check grades
SELECT * FROM grades;

-- Check fees
SELECT * FROM fees;

-- Check library books
SELECT * FROM library_books;

-- Check book transactions
SELECT * FROM book_transactions;

-- Count records
SELECT 'Users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'Staff', COUNT(*) FROM staff
UNION ALL
SELECT 'Students', COUNT(*) FROM students
UNION ALL
SELECT 'Attendance', COUNT(*) FROM attendance
UNION ALL
SELECT 'Grades', COUNT(*) FROM grades
UNION ALL
SELECT 'Fees', COUNT(*) FROM fees
UNION ALL
SELECT 'Books', COUNT(*) FROM library_books
UNION ALL
SELECT 'Transactions', COUNT(*) FROM book_transactions;
```

---

## ⚠️ Troubleshooting

### Error: "Access denied for user"
**Solution**: Check MySQL username and password are correct
```bash
mysql -u root -p  # Enter your password
```

### Error: "Database doesn't exist"
**Solution**: Create the database first
```sql
CREATE DATABASE school_management CHARACTER SET utf8mb4;
```

### Error: "No such file or directory"
**Solution**: Use absolute path to the file
```bash
mysql -u root -p school_management < D:\learn\thcsthptTS\backend\TEST_DATA.sql
```

### Data not appearing in application
**Solution**: Restart the application so it reloads data from database
```bash
mvn -s settings.xml spring-boot:run
```

---

## 🎯 Next Steps After Import

1. ✅ Start your application
2. ✅ Login with test credentials
3. ✅ Test all endpoints using Swagger UI
4. ✅ View data in your API responses
5. ✅ Create more test data as needed

---

## 📝 Example Test Scenarios

### Scenario 1: Teacher Managing Attendance
1. Login as `teacher1` / `Test@123`
2. GET `/api/v1/attendance/student/1` - View attendance
3. POST `/api/v1/attendance` - Mark attendance
4. GET `/api/v1/attendance/student/1/percentage` - Calculate percentage

### Scenario 2: Accountant Managing Fees
1. Login as `accountant` / `Test@123`
2. GET `/api/v1/fees/student/1` - View student fees
3. POST `/api/v1/fees/1/payment` - Process payment
4. GET `/api/v1/fees/student/1/total-dues` - Calculate total dues

### Scenario 3: Librarian Managing Books
1. Login as `librarian` / `Test@123`
2. GET `/api/v1/library/books` - View all books
3. POST `/api/v1/library/books/1/borrow` - Borrow a book
4. POST `/api/v1/library/books/1/return` - Return a book

### Scenario 4: Student Viewing Records
1. Login as `student1` / `Test@123`
2. GET `/api/v1/students/1` - View own record
3. GET `/api/v1/grades/student/1` - View own grades
4. GET `/api/v1/attendance/student/1` - View own attendance

---

**File Location**: `D:\learn\thcsthptTS\backend\TEST_DATA.sql`

**Ready to use**: ✅ YES

