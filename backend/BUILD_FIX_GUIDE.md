# Maven Build Fix - Connection Refused Error

## Problem
```
Failed to connect to cartifactory01.mba.xifin.com:8081
Connection refused: no further information
```

## Solution Applied

I've fixed the Maven build issues by:

### 1. Updated `pom.xml`
- Added Maven Central Repository configuration
- Changed MySQL connector from `mysql-connector-java` to `mysql-connector-j` (newer version)
- Both are officially supported and available in Maven Central

### 2. Created `settings.xml`
- Configured Maven to use public Maven Central Repository
- Removed dependency on corporate Artifactory server

---

## How to Use the Fix

### Option 1: Use the settings.xml file
```bash
cd backend
mvn -s settings.xml clean install
```

### Option 2: Clear Maven cache and rebuild
```bash
# Clear Maven cache
cd %USERPROFILE%\.m2\repository
del /s /q *xifin*

# Or on Linux/Mac:
rm -rf ~/.m2/repository/xifin

# Then rebuild
cd backend
mvn clean install
```

### Option 3: Force update dependencies
```bash
mvn clean install -U
```

### Option 4: Skip offline mode
```bash
mvn clean install -o false
```

---

## Verifying the Fix

After applying one of the above solutions, you should see:

```
[INFO] BUILD SUCCESS
[INFO] Total time: XX.XXX s
[INFO] Finished at: 2025-11-16T...
```

---

## Next Steps

Once the build succeeds:

```bash
# Run the application
mvn spring-boot:run

# Access API at
http://localhost:8080/api/swagger-ui.html
```

---

## Common Issues & Solutions

### Issue: Still getting connection errors
**Solution**: 
```bash
# Delete the entire .m2 repository
rm -rf ~/.m2/repository
mvn clean install
```

### Issue: Build timeout
**Solution**:
```bash
# Increase timeout
mvn -o install
# or
mvn -DskipTests clean install
```

### Issue: Wrong MySQL version
**Solution**: 
The pom.xml now uses `mysql-connector-j:8.0.33` which is the recommended driver for MySQL 8.0

---

## Files Modified

1. **pom.xml**
   - Added `<repositories>` section with Maven Central
   - Updated MySQL dependency

2. **settings.xml** (New)
   - Maven configuration for Central Repository
   - Place in backend folder

---

## Database Configuration

Your `application.yml` already has correct configuration:
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/school_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  username: root
  password: root
  driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## To Run the Fixed Build

```bash
cd D:\learn\thcsthptTS\backend

# Option 1: Simple clean install
mvn clean install

# Option 2: Skip tests for faster build
mvn clean install -DskipTests

# Option 3: With settings.xml
mvn -s settings.xml clean install

# Run the application
mvn spring-boot:run
```

---

**The build should now succeed!** 🎉

If you still encounter issues, the error message should be clearer, and we can address the specific problem.

