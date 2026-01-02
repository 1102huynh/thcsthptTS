# 🔧 DATABASE ERROR FIX GUIDE

## ❌ Error: "Failed to open the referenced table"

**Problem:** The migration script is trying to create tables with foreign keys to tables that don't exist yet (users, students, staff).

**Root Cause:** You're running the SQL script in the wrong database or the database doesn't have the base tables yet.

---

## ✅ SOLUTION (Choose ONE method)

### **METHOD 1: Let Spring Boot Handle It** ⭐ RECOMMENDED

This is the easiest way - just start your Spring Boot application:

```bash
# Navigate to backend
cd D:\learn\thcsthptTS\backend

# Start Spring Boot
./mvnw spring-boot:run

# Or if using Windows batch file
START_BACKEND.bat
```

**What happens:**
- Spring Boot will use `hibernate.ddl-auto: update` to create the tables automatically
- JPA entities will create all tables including the new Parent Portal tables
- No manual SQL needed!

---

### **METHOD 2: Manual SQL Installation**

If you prefer to run SQL manually:

#### Step 1: Check Current Database
```sql
-- In MySQL command line
SHOW DATABASES;
```

You should see `school_management` (with underscore, not `schoolmanagement`).

#### Step 2: Use Correct Database
```sql
USE school_management;
SHOW TABLES;
```

Verify that you have these base tables:
- ✅ `users`
- ✅ `students`
- ✅ `staff`

#### Step 3: Run Manual Installation Script
```bash
# In MySQL command line or Workbench
mysql -u root -p school_management < D:\learn\thcsthptTS\MANUAL_INSTALL_PHASE_9_10.sql
```

**OR** Copy and paste the contents of `MANUAL_INSTALL_PHASE_9_10.sql` into MySQL Workbench.

---

### **METHOD 3: Fix Database Name**

If your database is actually called `schoolmanagement` (no underscore), update the application.yml:

**File:** `backend/src/main/resources/application.yml`

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/schoolmanagement?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

Then restart Spring Boot.

---

## 🔍 Troubleshooting

### If base tables (users, students, staff) don't exist:

**Option A: Let Hibernate create them**
1. Make sure `spring.jpa.hibernate.ddl-auto: update` in application.yml
2. Start Spring Boot backend
3. Hibernate will create all tables from JPA entities

**Option B: Manual creation**
You need to create the base schema first. Do you have an initial schema SQL file?

---

## 📝 Quick Fix Commands

### MySQL Command Line:

```sql
-- 1. Connect to MySQL
mysql -u root -p

-- 2. Select the correct database
USE school_management;

-- 3. Check if base tables exist
SHOW TABLES;

-- 4. If tables exist, run the manual install script
SOURCE D:/learn/thcsthptTS/MANUAL_INSTALL_PHASE_9_10.sql;

-- 5. Verify new tables created
SHOW TABLES LIKE '%parent%';
SHOW TABLES LIKE 'announcements';
```

---

## ✅ Recommended Approach

**For your case, I recommend METHOD 1:**

1. **Don't run SQL manually** - let Spring Boot handle it
2. **Start the backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
3. **Check logs** - you should see:
   ```
   Hibernate: create table parents ...
   Hibernate: create table announcements ...
   ```
4. **Verify in MySQL:**
   ```sql
   USE school_management;
   SHOW TABLES;
   ```

---

## 🎯 Why This Approach?

Since your `application.yml` has:
```yaml
jpa:
  hibernate:
    ddl-auto: update
```

Hibernate will automatically:
- ✅ Create new tables from JPA entities
- ✅ Add new columns to existing tables
- ✅ Create indexes and foreign keys
- ✅ Handle all the database schema management

**You DON'T need Flyway migrations when using `ddl-auto: update`!**

---

## 📊 Files You Have:

1. **V9__parent_portal_analytics.sql** - Flyway migration (optional if using ddl-auto)
2. **MANUAL_INSTALL_PHASE_9_10.sql** - Manual installation script (backup option)

---

## 🚀 Next Steps:

1. ✅ Start Spring Boot backend
2. ✅ Check logs for table creation
3. ✅ Verify tables in MySQL
4. ✅ Start frontend
5. ✅ Test Parent Portal

---

## 💡 Pro Tip:

If you want to use Flyway migrations instead of Hibernate DDL:

1. Change in `application.yml`:
   ```yaml
   jpa:
     hibernate:
       ddl-auto: validate  # Change from 'update' to 'validate'
   ```

2. Add Flyway dependency to `pom.xml`:
   ```xml
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-core</artifactId>
   </dependency>
   <dependency>
       <groupId>org.flywaydb</groupId>
       <artifactId>flyway-mysql</artifactId>
   </dependency>
   ```

3. Restart Spring Boot - Flyway will run migrations automatically

---

## ✅ Summary

**Easiest solution:** Just start Spring Boot and let it create the tables!

```bash
cd backend
./mvnw spring-boot:run
```

That's it! 🎉

