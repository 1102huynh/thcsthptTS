# ✅ IMPORT TEST DATA NOW - FINAL STEP

## 🎯 CURRENT SITUATION

✅ 403 Error Fixed - Swagger UI accessible
✅ Backend running properly
❌ Login fails: "Invalid username or password"

**Reason**: Test data (admin user) NOT in database yet!

---

## 🚀 SOLUTION: Import Test Data (2 Methods)

### Method 1: Using Command Prompt (Recommended)

1. **Open Command Prompt**
   - Press: `Windows Key + R`
   - Type: `cmd`
   - Press: `Enter`

2. **Navigate to Backend Folder**
   ```bash
   cd D:\learn\thcsthptTS\backend
   ```

3. **Import Test Data**
   ```bash
   "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management < TEST_DATA_CORRECTED.sql
   ```

4. **When prompted for password**:
   - If you set a MySQL password during installation, enter it
   - If no password (default), just press `Enter`

5. **Wait for import to complete** (5-10 seconds)

---

### Method 2: Using MySQL Workbench (Visual)

1. **Open MySQL Workbench**

2. **Connect to your local MySQL server**

3. **Open SQL File**
   - Click: `File` → `Open SQL Script`
   - Navigate to: `D:\learn\thcsthptTS\backend\TEST_DATA_CORRECTED.sql`
   - Click: `Open`

4. **Execute Script**
   - Click the lightning bolt icon ⚡ or press `Ctrl+Shift+Enter`

5. **Verify Success**
   - Should see "X rows affected" messages
   - No errors

---

## ✅ VERIFY DATA IMPORTED

After importing, verify the admin user exists:

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management -e "SELECT username, email, role FROM users WHERE username='admin';"
```

**Expected output**:
```
+----------+-------------------+-------+
| username | email             | role  |
+----------+-------------------+-------+
| admin    | admin@school.com  | ADMIN |
+----------+-------------------+-------+
```

---

## 🔄 AFTER IMPORTING DATA

### Important: Restart Backend!

The backend needs to be restarted to pick up the new database data:

1. **Stop current backend**:
   - Find the terminal/window running backend
   - Press: `Ctrl+C`

2. **Restart backend**:
   ```bash
   cd D:\learn\thcsthptTS\backend
   java -jar target/school-management-system-1.0.0.jar
   ```

3. **Wait for**: "Started SchoolManagementApplication" message

---

## 🧪 TEST LOGIN AGAIN

After restart:

1. **Open Swagger UI**:
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **Find POST /api/v1/auth/login endpoint**

3. **Click "Try it out"**

4. **Enter credentials**:
   ```json
   {
     "username": "admin",
     "password": "Test@123"
   }
   ```

5. **Click "Execute"**

6. **Expected Response (200 OK)**:
   ```json
   {
     "userId": 1,
     "username": "admin",
     "email": "admin@school.com",
     "firstName": "Admin",
     "lastName": "User",
     "role": "ADMIN",
     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "tokenType": "Bearer"
   }
   ```

---

## 📊 WHAT GETS IMPORTED

The test data includes:
- ✅ 13 Users (admin, principal, teachers, students, etc.)
- ✅ 6 Staff members
- ✅ 6 Students
- ✅ 4 Classes
- ✅ 8 Library books
- ✅ 15 Attendance records
- ✅ 9 Grade records
- ✅ 7 Fee records
- ✅ 6 Book transactions
- ✅ 40+ Permissions

**All users have password**: `Test@123`

---

## 🔑 ALL TEST CREDENTIALS

After importing, you can login with:

| Username | Password | Role |
|----------|----------|------|
| admin | Test@123 | ADMIN |
| principal | Test@123 | PRINCIPAL |
| teacher1 | Test@123 | TEACHER |
| teacher2 | Test@123 | TEACHER |
| teacher3 | Test@123 | TEACHER |
| librarian | Test@123 | LIBRARIAN |
| accountant | Test@123 | ACCOUNTANT |
| student1 | Test@123 | STUDENT |
| student2 | Test@123 | STUDENT |
| student3 | Test@123 | STUDENT |
| student4 | Test@123 | STUDENT |
| student5 | Test@123 | STUDENT |
| student6 | Test@123 | STUDENT |

---

## 🎯 COMPLETE WORKFLOW

```
1. Import test data ✅ (Do this now)
   ↓
2. Restart backend ✅
   ↓
3. Test login in Swagger UI ✅
   ↓
4. Login to frontend ✅
   ↓
5. Access dashboard ✅
   ↓
6. Full system working! 🎉
```

---

## 📞 IF IMPORT FAILS

### Error: "mysql is not recognized"
**Solution**: Use full path to mysql.exe:
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management < TEST_DATA_CORRECTED.sql
```

### Error: "Access denied"
**Solution**: Check MySQL password, or use MySQL Workbench method

### Error: "Database doesn't exist"
**Solution**: Create it first:
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE DATABASE school_management;"
```

---

## ✅ STATUS SUMMARY

- [x] Backend built successfully
- [x] 403 error fixed
- [x] Swagger UI accessible
- [x] Backend running
- [ ] **Test data imported** ← DO THIS NOW
- [ ] Backend restarted
- [ ] Login working
- [ ] System ready

---

**NEXT ACTION**: Import the test data using one of the methods above!

