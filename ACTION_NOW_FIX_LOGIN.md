# 🔧 IMMEDIATE ACTION - FIX INVALID CREDENTIALS

## ⏱️ Takes 5 Minutes

## ✅ DO THIS NOW

### Copy This Command
```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p school_management < D:\learn\thcsthptTS\backend\TEST_DATA_CORRECTED.sql
```

### Steps:
1. Open Command Prompt (Windows+R → cmd → Enter)
2. Navigate: `cd D:\learn\thcsthptTS\backend`
3. Paste the command above
4. Press Enter
5. When asked for password: Press Enter (or enter your MySQL password if you set one)
6. Wait 5 seconds

---

## ✅ Then Do This

### Restart Backend
```bash
java -jar target/school-management-system-1.0.0.jar
```

Wait for: "Started SchoolManagementApplication"

---

## ✅ Finally Do This

### Open Browser & Login
```
URL: http://localhost:3000
Username: admin
Password: Test@123
Click: Login
```

---

## ✅ SHOULD WORK NOW!

You should see the Dashboard! ✅

---

## 🆘 IF IMPORT COMMAND FAILS

### Check MySQL Path
Try this instead:
```bash
mysql -u root -p school_management < TEST_DATA_CORRECTED.sql
```

### If that doesn't work
Find MySQL bin folder:
```bash
# Might be in different location
"C:\MySQL\MySQL Server 8.0\bin\mysql.exe" ...
# Or
"C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe" ...
```

---

## 🎯 THAT'S IT!

Import data → Restart backend → Login

**All fixed!** ✅

