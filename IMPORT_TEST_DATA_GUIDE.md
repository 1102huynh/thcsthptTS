# 🔧 FIX: "Invalid Credentials" - Complete Data Import Guide

## 🎯 THE ISSUE

Getting "Invalid credentials" when logging in with admin/Test@123

**Root Cause**: Test data has NOT been imported to the database yet

---

## ✅ SOLUTION: Import Test Data

### Method 1: Using MySQL Command Line (Recommended)

#### Step 1: Open Command Prompt
```
Press: Windows Key + R
Type: cmd
Press: Enter
```

#### Step 2: Navigate to Backend Folder
```bash
cd D:\learn\thcsthptTS\backend
```

#### Step 3: Import Test Data
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management < TEST_DATA_CORRECTED.sql
```

**When prompted**: Enter your MySQL password (usually blank, just press Enter)

**Expected Output**:
```
[SUCCESS] No errors
Or
Query OK messages
```

#### Step 4: Verify Data Imported
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management -e "SELECT COUNT(*) as user_count FROM users;"
```

**Should show**: `user_count: 13`

---

### Method 2: Using MySQL Workbench (Visual)

#### Step 1: Open MySQL Workbench
- Launch MySQL Workbench from Start menu
- Connect to your local MySQL server

#### Step 2: Create/Select Database
```sql
CREATE DATABASE IF NOT EXISTS school_management;
USE school_management;
```

#### Step 3: Import SQL File
- Click: **File** → **Open SQL Script**
- Select: `D:\learn\thcsthptTS\backend\TEST_DATA_CORRECTED.sql`
- Click: **Execute** or press **Ctrl+Shift+Enter**

#### Step 4: Verify
```sql
SELECT COUNT(*) FROM users;
SELECT * FROM users LIMIT 5;
```

**Should show**: 13 users with admin user having email admin@school.com

---

### Method 3: Direct in PhpMyAdmin (If Available)

#### Step 1: Open phpMyAdmin
- URL: `http://localhost/phpmyadmin`
- Login with MySQL credentials

#### Step 2: Select/Create Database
- Click: **New** (to create school_management)
- Or select existing school_management

#### Step 3: Import File
- Click: **Import** tab
- Click: **Choose File**
- Select: `D:\learn\thcsthptTS\backend\TEST_DATA_CORRECTED.sql`
- Click: **Import** button

#### Step 4: Verify
- Click: **school_management** → **users**
- Should show 13 users

---

## 🔍 VERIFY DATA WAS IMPORTED

After importing, verify the data:

```bash
# Method 1: Using Command Line
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management -e "SELECT username, email, role FROM users LIMIT 5;"

# Should show:
# +----------+-------------------+-------+
# | username | email             | role  |
# +----------+-------------------+-------+
# | admin    | admin@school.com  | ADMIN |
# ...
```

---

## 🚀 AFTER IMPORTING DATA

### Step 1: Ensure Backend is Running
```bash
# Make sure backend is running
http://localhost:8080
```

### Step 2: Restart Backend (Important!)
```bash
# Stop backend (Ctrl+C if running)

# Restart it
cd D:\learn\thcsthptTS\backend
java -jar target/school-management-system-1.0.0.jar
```

### Step 3: Try Login Again
- Open: http://localhost:3000
- Username: `admin`
- Password: `Test@123`
- Click: Login

**Should work now!** ✅

---

## ✅ TEST ALL CREDENTIALS

After successful data import, try these logins:

```
Username: admin           Password: Test@123  ✅
Username: principal       Password: Test@123  ✅
Username: teacher1        Password: Test@123  ✅
Username: student1        Password: Test@123  ✅
Username: librarian       Password: Test@123  ✅
Username: accountant      Password: Test@123  ✅
```

---

## 🐛 IF IMPORT FAILS

### Check MySQL is Running
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SELECT 1;"
```

Should show: `1` (meaning MySQL is running)

### Check Database Exists
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SHOW DATABASES;" | findstr school_management
```

Should show: `school_management`

### If Database Missing, Create It
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE DATABASE school_management CHARACTER SET utf8mb4;"
```

Then import again:
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management < TEST_DATA_CORRECTED.sql
```

---

## 📊 WHAT GETS IMPORTED

The SQL file imports:

- ✅ 13 users (admin, principal, teachers, students, etc.)
- ✅ 6 staff members
- ✅ 6 students in different classes
- ✅ 4 classes
- ✅ 8 library books
- ✅ 15 attendance records
- ✅ 9 grade records
- ✅ 7 fee records
- ✅ 40+ permissions

---

## 🎯 COMPLETE WORKFLOW

1. **Import test data** (this section)
2. **Restart backend**
3. **Try login with admin/Test@123**
4. **Dashboard loads** ✅

---

## 🎊 SUCCESS CHECKLIST

- [ ] MySQL is running
- [ ] school_management database exists
- [ ] Test data imported (13 users)
- [ ] Backend restarted
- [ ] Can login with admin/Test@123
- [ ] Dashboard displays
- [ ] No errors in console

---

**Follow the steps above to import test data and fix the login issue!**

