# 🔍 LOGIN TROUBLESHOOTING GUIDE

## 🎯 "Invalid Credentials" Error - Solutions

### Issue
Getting "Invalid credentials. Please try again." when logging in with admin/Test@123

---

## ✅ STEP 1: Verify Backend is Running

### Check if backend is accessible:
```bash
curl http://localhost:8080/api/swagger-ui.html
```

**Expected**: Swagger UI page loads

If not, start backend:
```bash
cd D:\learn\thcsthptTS\backend
java -jar target/school-management-system-1.0.0.jar
```

---

## ✅ STEP 2: Verify Test Data Was Imported

### Check if users exist in database:
```bash
mysql -u root -p school_management -e "SELECT username, email, role FROM users LIMIT 5;"
```

**Expected Output**:
```
+-----------+-------------------+-------+
| username  | email             | role  |
+-----------+-------------------+-------+
| admin     | admin@school.com  | ADMIN |
| principal | principal@...     | ...   |
...
```

If no users shown, import test data:
```bash
mysql -u root -p school_management < D:\learn\thcsthptTS\backend\TEST_DATA_CORRECTED.sql
```

---

## ✅ STEP 3: Test Login via API (Using cURL)

### Test the /login endpoint directly:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123"}'
```

**Expected Response**:
```json
{
  "userId": 1,
  "username": "admin",
  "accessToken": "eyJ...",
  "role": "ADMIN"
}
```

**If Error**: "Invalid username or password"
- Check test data is imported
- Verify password matches (all test users: Test@123)

---

## ✅ STEP 4: Check Test Data File

The passwords in TEST_DATA_CORRECTED.sql are hashed BCrypt.

**Hashed password for "Test@123"**:
```
$2a$10$slYQmyNdGzin7olVaICC2OPST9/PgBkqquzi.Oy5XH..D6kWGwFqm
```

All users should have this same hashed password.

### Verify in database:
```bash
mysql -u root -p school_management -e "SELECT username, password FROM users WHERE username='admin';"
```

Should show the hashed password above.

---

## ✅ STEP 5: Complete Troubleshooting Checklist

- [ ] Backend running on http://localhost:8080
- [ ] Test data imported into MySQL
- [ ] Users table has data: `SELECT COUNT(*) FROM users;`
- [ ] Admin user exists: `SELECT * FROM users WHERE username='admin';`
- [ ] Login endpoint responds: `curl http://localhost:8080/api/v1/auth/login`
- [ ] Can login via cURL with admin/Test@123
- [ ] Frontend running on http://localhost:3000

---

## 🔧 COMMON CAUSES & FIXES

### Issue 1: Backend Not Running
**Solution**: Start backend
```bash
java -jar target/school-management-system-1.0.0.jar
```

### Issue 2: Test Data Not Imported
**Solution**: Import test data
```bash
mysql -u root -p school_management < TEST_DATA_CORRECTED.sql
```

### Issue 3: Wrong Password
**Solution**: All test users use password: **Test@123** (case-sensitive)

### Issue 4: MySQL Not Running
**Solution**: Start MySQL
```bash
# Windows
net start MySQL80

# Or check MySQL is running in services
```

### Issue 5: Database Not Created
**Solution**: Create database
```bash
mysql -u root -p -e "CREATE DATABASE school_management CHARACTER SET utf8mb4;"
```

---

## 🔑 ALL TEST CREDENTIALS

**All passwords**: Test@123 (exactly)

```
admin
principal
teacher1
teacher2
teacher3
librarian
accountant
student1
student2
student3
student4
student5
student6
```

---

## 🚀 COMPLETE RESET PROCESS

If nothing works, do a complete reset:

### 1. Stop all services
```bash
# Stop backend (press Ctrl+C in terminal)
# Stop frontend (press Ctrl+C in terminal)
```

### 2. Reset database
```bash
mysql -u root -p
DROP DATABASE school_management;
CREATE DATABASE school_management CHARACTER SET utf8mb4;
EXIT;
```

### 3. Re-import test data
```bash
mysql -u root -p school_management < TEST_DATA_CORRECTED.sql
```

### 4. Restart backend
```bash
cd D:\learn\thcsthptTS\backend
java -jar target/school-management-system-1.0.0.jar
```

### 5. Restart frontend
```bash
cd D:\learn\thcsthptTS\frontend
npm start
```

### 6. Try login again
```
Username: admin
Password: Test@123
```

---

## ✅ VERIFY EVERYTHING WORKS

After following steps above:

1. ✅ Backend starts without errors
2. ✅ Frontend loads at http://localhost:3000
3. ✅ Login page displays
4. ✅ Can login with admin/Test@123
5. ✅ Dashboard loads
6. ✅ Data displays

---

## 📞 IF STILL NOT WORKING

Check these in order:

1. **Backend logs**: Any errors when starting?
2. **Frontend console**: Open browser DevTools (F12), check Console tab
3. **Network tab**: Check API calls - is /auth/login being called?
4. **MySQL logs**: Any errors?
5. **Test data**: Did import complete without errors?

---

**Next**: Follow the steps above to identify and fix the issue!

