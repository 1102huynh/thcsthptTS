# ⚡ ACTION REQUIRED: Fix "Securing GET /error" Infinite Loop

## 🎯 Problem Summary

**Error:** `Securing GET /error?page=0&size=3` being called repeatedly

**Cause:** Backend is throwing an exception when querying the `news` table, most likely because **the table doesn't exist yet**.

---

## ✅ All Code Fixes Have Been Applied

The following files have been updated:
- ✅ `backend/.../SecurityConfig.java` - Security configuration updated
- ✅ `frontend/.../api.js` - 401 handling improved  
- ✅ `frontend/.../PrincipalHomePage.js` - Infinite loop fixed

**No more code changes needed!**

---

## 🚨 YOU MUST DO THIS NOW:

### **STEP 1: Create the NEWS table in your database**

The `news` table doesn't exist yet. You need to run the SQL script:

#### Option A: Using psql command line
```bash
psql -U postgres -d your_database_name -f backend/NEWS_SETUP.sql
```

#### Option B: Using pgAdmin or database GUI
1. Open `backend/NEWS_SETUP.sql` in a text editor
2. Copy all the SQL
3. Open your database client (pgAdmin, DBeaver, etc.)
4. Paste and execute the SQL

#### Option C: Manual check first
```sql
-- Check if table exists
SELECT * FROM news LIMIT 1;
```

If you get "table doesn't exist", then run the NEWS_SETUP.sql script.

---

### **STEP 2: Restart the backend**

```bash
cd backend
mvnw clean package
mvnw spring-boot:run
```

Or if using batch file:
```bash
START_BACKEND.bat
```

**Wait for:** "Started SchoolManagementApplication..."

---

### **STEP 3: Test**

```bash
# Test backend API
curl http://localhost:8080/api/news
```

**Should return:** JSON with news data  
**Should NOT return:** 401 or 500 error

Then refresh your browser at http://localhost:3000

---

## 🔍 How to Verify It's Fixed

### Browser Console Should Show:
```
PrincipalHomePage mounted, loading initial data...
Fetching news for page: 0
News loaded successfully: 3 items
```

### Browser Should Display:
- ✅ 3 news items on the homepage
- ✅ No error messages
- ✅ No infinite loop
- ✅ Pagination buttons visible

### Browser Network Tab Should Show:
- ✅ One request to `/api/news?page=0&size=3`
- ✅ Status: `200 OK`
- ✅ Response: JSON data (not HTML)

---

## 🎯 Why This Happens

1. Frontend calls `/api/news`
2. Backend tries to query the `news` table
3. **Table doesn't exist** → Exception thrown
4. Spring Boot redirects to `/error` page
5. Frontend receives error, retries
6. Loop continues

**Solution:** Create the table!

---

## ✅ Summary

**Problem:** News table doesn't exist in database  
**Solution:** Run NEWS_SETUP.sql  
**Then:** Restart backend  
**Result:** News will load correctly  

---

**YOUR NEXT ACTION:** Run the SQL script to create the news table, then restart the backend!

That's it! 🚀

