# 🚀 QUICK TEST DATA IMPORT GUIDE

## ⚡ Fastest Way (30 seconds)

```bash
# 1. Open Command Prompt
# 2. Run this ONE command:

mysql -u root -p school_management < D:\learn\thcsthptTS\backend\TEST_DATA.sql

# 3. When prompted, enter your MySQL password (usually "root")
# 4. Done! Data is imported
```

---

## 📊 What Gets Imported

| Item | Count | Details |
|------|-------|---------|
| **Users** | 13 | Admin, Principal, 3 Teachers, Librarian, Accountant, 6 Students |
| **Staff** | 6 | Employee records |
| **Students** | 6 | Class 10-A (3), Class 10-B (3) |
| **Books** | 8 | Fiction, Academic, Reference |
| **Attendance** | 15 | Multiple records per student |
| **Grades** | 9 | 3 subjects per 3 students |
| **Fees** | 7 | Various types and statuses |
| **Transactions** | 6 | Book borrow/return |

---

## 🔑 Test Logins (All passwords: Test@123)

| User | Password |
|------|----------|
| admin | Test@123 |
| principal | Test@123 |
| teacher1 | Test@123 |
| librarian | Test@123 |
| accountant | Test@123 |
| student1 | Test@123 |

---

## 🧪 Quick Test After Import

```bash
# 1. Start your application
java -jar target/school-management-system-1.0.0.jar

# 2. Open Swagger UI
http://localhost:8080/api/swagger-ui.html

# 3. Login as admin with credentials above

# 4. Try these endpoints:
# - GET /api/v1/staff
# - GET /api/v1/students
# - GET /api/v1/library/books
# - GET /api/v1/grades/student/1
# - GET /api/v1/fees/student/1
```

---

## 📁 File Location

```
D:\learn\thcsthptTS\backend\TEST_DATA.sql
```

---

## ✅ Verification Query

After import, run this to confirm:

```sql
mysql -u root -p school_management -e "
SELECT 
  (SELECT COUNT(*) FROM users) as users,
  (SELECT COUNT(*) FROM staff) as staff,
  (SELECT COUNT(*) FROM students) as students,
  (SELECT COUNT(*) FROM library_books) as books
;"
```

Should show: users=13, staff=6, students=6, books=8

---

## 🎯 Files to Use

1. **TEST_DATA.sql** ← SQL file with all data
2. **IMPORT_TEST_DATA.md** ← Detailed import instructions
3. **TEST_LOGIN_ENDPOINT.md** ← How to test login

---

**Ready to import?** Just run the command above! ✅

