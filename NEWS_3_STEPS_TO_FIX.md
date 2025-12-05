# 🚀 3 STEPS TO FIX NEWS LOADING

## Your homepage is correctly PUBLIC ✅
No login needed - this is correct!

The "Failed to load news" error means the backend API isn't responding correctly.

---

## DO THESE 3 STEPS:

### **STEP 1: Create NEWS table**

Connect to your PostgreSQL database and run:

```sql
-- Quick test if table exists
SELECT COUNT(*) FROM news;
```

**If error:** Table doesn't exist. Run the SQL from `backend/NEWS_SETUP.sql`

**Using command line:**
```bash
psql -U avnadmin -h school-clinicbooking.c.aivencloud.com -p 14143 -d defaultdb -f backend/NEWS_SETUP.sql
```

**Or:** Copy/paste SQL from `backend/NEWS_SETUP.sql` into your database GUI (pgAdmin, DBeaver, etc.)

---

### **STEP 2: Restart Backend**

```bash
cd backend
mvnw clean spring-boot:run
```

Wait for: `Started SchoolManagementApplication`

---

### **STEP 3: Test**

**Backend test:**
```bash
curl http://localhost:8080/api/news
```

Should return JSON (not 401)

**Frontend test:**
1. Refresh http://localhost:3000
2. Should see real news
3. No error message

---

## ✅ Success = News displays on homepage for everyone (no login)

That's it! 🎉

