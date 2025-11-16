# 🔧 QUICK LOGIN DIAGNOSTIC - RUN THIS FIRST

## ⚡ Quick Test (30 seconds)

### Test 1: Is Backend Running?
```bash
curl http://localhost:8080/api/v1/auth/login -X OPTIONS
```
**Expected**: Response (200 or no error)
**If Error**: Backend not running

---

### Test 2: Try Login via cURL
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Test@123"}'
```

**If Success**: Returns tokens and user data ✅
**If Error "Invalid credentials"**: Test data not imported ❌

---

### Test 3: Check Database
```bash
mysql -u root -p school_management -e "SELECT COUNT(*) as user_count FROM users;"
```

**If Shows Number > 0**: Data exists ✅
**If Shows 0**: Need to import test data ❌

---

## 🔧 IF ALL TESTS FAIL

### Solution 1: Ensure Test Data Imported
```bash
mysql -u root -p school_management < D:\learn\thcsthptTS\backend\TEST_DATA_CORRECTED.sql
```

### Solution 2: Verify Import Success
```bash
mysql -u root -p school_management -e "
SELECT username, role FROM users LIMIT 5;
SELECT COUNT(*) as total_users FROM users;
"
```

### Solution 3: Restart Everything
```bash
# Terminal 1
cd D:\learn\thcsthptTS\backend
java -jar target/school-management-system-1.0.0.jar

# Terminal 2
cd D:\learn\thcsthptTS\frontend
npm start
```

### Solution 4: Try Login Again
- Open: http://localhost:3000
- Username: admin
- Password: Test@123

---

## ✅ If Everything Works

You should see:
1. Login page loads
2. Can enter credentials
3. Click login
4. Dashboard appears with statistics

---

**Follow these steps to fix the login issue!**

