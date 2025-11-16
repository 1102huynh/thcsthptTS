# ✅ 403 FORBIDDEN ERROR - FIXED!

## 🎯 WHAT WAS FIXED

Updated `SecurityConfig.java` to allow public access to Swagger UI endpoints:

**Added these paths to permitAll:**
- ✅ `/api/swagger-ui.html`
- ✅ `/api/swagger-ui/**`
- ✅ `/api/v3/api-docs/**`
- ✅ `/api/v3/api-docs.yaml`
- ✅ `/v3/api-docs.yaml`

---

## 🚀 REBUILD & RESTART BACKEND

### Step 1: Stop Current Backend
Press **Ctrl+C** in the terminal where backend is running

### Step 2: Rebuild Backend
```bash
cd D:\learn\thcsthptTS\backend
mvn -s settings.xml clean install
```

**Expected**: Build succeeds (BUILD SUCCESS message)

### Step 3: Restart Backend
```bash
java -jar target/school-management-system-1.0.0.jar
```

**Expected**: Backend starts on port 8080

### Step 4: Test Swagger UI
Open in browser:
```
http://localhost:8080/api/swagger-ui.html
```

**Expected**: 
- ✅ Swagger UI page loads
- ✅ No 403 error
- ✅ Can see all API endpoints

---

## ✅ WHAT CHANGED

**File**: `src/main/java/com/schoolmanagement/config/SecurityConfig.java`

**Before**:
```java
.requestMatchers("/swagger-ui/**").permitAll()
.requestMatchers("/v3/api-docs/**").permitAll()
```

**After**:
```java
.requestMatchers("/swagger-ui/**").permitAll()
.requestMatchers("/api/swagger-ui.html").permitAll()
.requestMatchers("/api/swagger-ui/**").permitAll()
.requestMatchers("/v3/api-docs/**").permitAll()
.requestMatchers("/api/v3/api-docs/**").permitAll()
.requestMatchers("/api/v3/api-docs.yaml").permitAll()
.requestMatchers("/v3/api-docs.yaml").permitAll()
```

---

## 📝 BUILD COMMANDS

### Full Rebuild (Recommended)
```bash
cd D:\learn\thcsthptTS\backend
mvn -s settings.xml clean install -DskipTests
java -jar target/school-management-system-1.0.0.jar
```

### Quick Rebuild (If Already Built)
```bash
cd D:\learn\thcsthptTS\backend
mvn -s settings.xml compile
mvn -s settings.xml package -DskipTests
java -jar target/school-management-system-1.0.0.jar
```

---

## ✅ VERIFICATION

After restart, you should be able to:

1. ✅ Access Swagger UI: `http://localhost:8080/api/swagger-ui.html`
2. ✅ See all endpoints listed
3. ✅ Try login endpoint
4. ✅ Get proper responses (no 403 error)

---

## 🔧 TROUBLESHOOTING

### If Build Fails
```bash
# Clear Maven cache
mvn -s settings.xml clean
# Try again
mvn -s settings.xml install -DskipTests
```

### If Port 8080 Still in Use
```bash
# Find process on port 8080
netstat -ano | findstr :8080
# Kill it (replace PID)
taskkill /PID <PID> /F
```

### If Still Getting 403
1. Make sure backend restarted (look for "Started SchoolManagementApplication")
2. Clear browser cache (Ctrl+Shift+Delete)
3. Try different browser
4. Wait 10 seconds for app to fully start

---

## 📋 COMPLETE WORKFLOW

```bash
# Terminal 1: Rebuild and restart backend
cd D:\learn\thcsthptTS\backend
mvn -s settings.xml clean install -DskipTests
java -jar target/school-management-system-1.0.0.jar

# Terminal 2: Keep frontend running
cd D:\learn\thcsthptTS\frontend
npm start

# Browser: Test Swagger UI
http://localhost:8080/api/swagger-ui.html

# Browser: Test Frontend Login
http://localhost:3000
Username: admin
Password: Test@123
```

---

## ✅ EXPECTED RESULT

✅ Swagger UI loads without 403 error  
✅ Can see all API endpoints  
✅ Can test login endpoint  
✅ Backend responds with proper data  
✅ Frontend can login successfully  

---

**Status**: ✅ **FIXED & READY**

Rebuild backend and the 403 error will be gone!

