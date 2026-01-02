# 🔍 TROUBLESHOOTING - "Fail to load" Error

## **POSSIBLE CAUSES:**

### **1. Backend Not Running** 🔴
**Check:**
```bash
# Is backend running?
curl http://localhost:8080/api/grade-levels
```

**If not running:**
```bash
cd backend
mvn spring-boot:run
```

---

### **2. Controllers Not Loaded** 🟡
**We created 2 new controllers:**
- SubjectController.java
- ClassSubjectAssignmentController.java

**Backend MUST be restarted** to load them!

```bash
# Stop backend (Ctrl+C)
# Then restart:
mvn spring-boot:run
```

---

### **3. Database Connection** 🟡
**Check if MySQL is running:**
```bash
# Windows:
services.msc
# Look for MySQL service

# Or test connection:
mysql -u root -p
```

---

### **4. CORS Issues** 🟢
**Already configured in SecurityConfig** - should be OK

---

## **QUICK FIX STEPS:**

### **Step 1: Restart Backend** ⭐ MOST LIKELY FIX
```bash
cd d:\learn\thcsthptTS\backend
mvn spring-boot:run
```

**Wait for:**
```
Started SchoolManagementApplication in X.XXX seconds
```

### **Step 2: Check Browser Console**
Open DevTools (F12) → Console tab

**Look for:**
- Network errors?
- 404 errors? (endpoint not found)
- 500 errors? (server error)
- CORS errors?

### **Step 3: Check Network Tab**
DevTools → Network tab

**Check failed requests:**
- Status code?
- Response body?
- Request headers (has Authorization)?

### **Step 4: Test Endpoints Manually**
```bash
# Test if endpoints exist:
curl http://localhost:8080/api/classes
curl http://localhost:8080/api/subjects
curl http://localhost:8080/api/assignments
```

---

## **EXPECTED BEHAVIOR:**

### **When Backend is Running:**
```
✅ http://localhost:8080/api/classes → 200 OK (or 401 if no token)
✅ http://localhost:8080/api/subjects → 200 OK (or 401 if no token)
✅ http://localhost:8080/api/assignments → 200 OK (or 401 if no token)
```

### **401 is OK!**
401 means endpoint exists but needs authentication.
That's expected for direct curl without token.

### **404 is BAD!**
404 means endpoint doesn't exist.
→ Backend not restarted after creating controllers!

---

## **DEBUGGING CHECKLIST:**

- [ ] Backend is running on port 8080
- [ ] MySQL is running
- [ ] Database `school_management` exists
- [ ] Migration was run successfully
- [ ] Backend was restarted after creating controllers
- [ ] Frontend can reach backend (no CORS errors)
- [ ] User is logged in (JWT token in localStorage)

---

## **COMMON ERRORS & SOLUTIONS:**

### **Error: "Failed to load data: Network Error"**
**Solution:** Backend not running → Start it!

### **Error: "Failed to load data: Request failed with status code 404"**
**Solution:** Endpoint doesn't exist → Restart backend!

### **Error: "Failed to load data: Request failed with status code 401"**
**Solution:** Not authenticated → Login again!

### **Error: "Failed to load data: Request failed with status code 500"**
**Solution:** Server error → Check backend console logs!

---

## **NEXT STEPS:**

1. **Open backend terminal**
2. **Check if it's running**
3. **If not, start it:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. **Wait for "Started" message**
5. **Refresh frontend pages**

---

**Most likely issue:** Backend needs restart after creating new controllers!

**Quick fix:** `mvn spring-boot:run` in backend folder! 🚀
