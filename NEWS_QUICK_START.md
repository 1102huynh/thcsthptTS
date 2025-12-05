# 🚀 Quick Start Guide - School News Management

## ⚡ Quick Setup (5 Minutes)

### Step 1: Run Database Script (1 minute)

Open your PostgreSQL client and run:

```bash
# Option 1: Using psql command line
psql -U postgres -d your_database -f backend/NEWS_SETUP.sql

# Option 2: Copy and paste the SQL from backend/NEWS_SETUP.sql
```

✅ This creates the news table and inserts 10 sample news articles including:
- Admissions announcement
- Extracurricular registration
- Sports day event
- And more...

### Step 2: Rebuild Backend (2 minutes)

```bash
cd backend
mvnw clean install
mvnw spring-boot:run
```

Wait for: `Started SchoolManagementApplication in X seconds`

### Step 3: Start Frontend (1 minute)

```bash
cd frontend
npm start
```

Browser opens automatically at http://localhost:3000

### Step 4: Test It! (1 minute)

#### Test Public View
1. Visit http://localhost:3000
2. See news on homepage
3. Click pagination to browse

#### Test Admin View
1. Click "Login" button
2. Login credentials:
   ```
   Username: admin
   Password: admin123
   ```
   OR
   ```
   Username: principal
   Password: admin123
   ```
3. Click "News Management" in sidebar
4. Try creating a new news article!

---

## 🎯 Key Features You Can Use Right Now

### 👀 Public Features (No Login)
✅ View all published news on homepage  
✅ Browse news with pagination  
✅ See featured news highlighted  
✅ Automatic view counting  

### 🔐 Admin Features (Login Required)
✅ Create new news articles  
✅ Edit existing news  
✅ Publish draft news  
✅ Archive old news  
✅ Delete news  
✅ Set featured news  
✅ View statistics  

---

## 📝 Quick Test: Create Your First News

1. **Login** as admin
2. **Navigate** to "News Management"
3. **Click** "Create News"
4. **Fill form:**
   ```
   Title: Winter Camp 2026
   Content: Exciting winter camp from January 20-25, 2026
   Category: Event
   Icon: ⛺
   Status: Published
   Featured: ✓
   ```
5. **Submit** and see it appear on homepage!

---

## 🎨 Available Categories

- Event (🎉)
- Achievement (🎖️)
- Infrastructure (💻)
- Admission (📚)
- Extracurricular (🎯)
- Meeting (👨‍👩‍👧‍👦)
- Library (📖)
- Other (📰)

---

## 🔧 API Endpoints You Can Test

### Public (No Auth)
```bash
# Get all published news
curl http://localhost:8080/api/news

# Get recent news
curl http://localhost:8080/api/news/recent

# Get featured news
curl http://localhost:8080/api/news/featured

# Search news
curl "http://localhost:8080/api/news/search?keyword=admission"
```

### Admin (With Auth)
First, login to get token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Then use the token:
```bash
# Get all news (including drafts)
curl http://localhost:8080/api/news/admin/all \
  -H "Authorization: Bearer YOUR_TOKEN"

# Create news
curl -X POST http://localhost:8080/api/news \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test News",
    "content": "This is a test",
    "category": "Event",
    "status": "PUBLISHED"
  }'
```

---

## 🐛 Common Issues & Solutions

### Issue: News not showing on homepage
**Solution:**
- Check if backend is running on port 8080
- Verify news status is "PUBLISHED" in database
- Clear browser cache

### Issue: Cannot create news
**Solution:**
- Verify you're logged in as ADMIN or PRINCIPAL
- Check browser console for errors
- Ensure all required fields are filled

### Issue: Database connection error
**Solution:**
- Check database is running
- Verify credentials in application.properties
- Ensure news table exists

---

## 📊 Sample News Already Included

The NEWS_SETUP.sql script includes these sample articles:

1. ✅ **Admissions Open 2026-2027** (Featured)
2. ✅ **Extracurricular Registration** (Featured)
3. ✅ **Annual Sports Day 2025** (Featured)
4. ✅ **New Computer Lab**
5. ✅ **Excellence Awards Ceremony**
6. ✅ **Science Exhibition**
7. ✅ **Parent-Teacher Meeting**
8. ✅ **New Library Books**
9. ⏸️ **Winter Camp** (Draft)

---

## 🎓 What You've Gained

### Database
- ✅ News table with proper relationships
- ✅ Indexes for performance
- ✅ Sample data ready to use

### Backend
- ✅ Full REST API
- ✅ CRUD operations
- ✅ Role-based security
- ✅ Pagination & filtering

### Frontend
- ✅ Public news display
- ✅ Admin dashboard
- ✅ Responsive design
- ✅ Real-time updates

---

## 📞 Need Help?

1. Check full documentation: `NEWS_MANAGEMENT_COMPLETE_GUIDE.md`
2. Review backend logs: `backend/logs/`
3. Check browser console for frontend errors
4. Verify database connection

---

## ✅ Success Indicators

You've successfully set up the system when:

✅ Homepage shows news articles  
✅ Pagination works  
✅ Admin can login  
✅ News Management page loads  
✅ Can create/edit/delete news  
✅ News appears on homepage immediately after publishing  

---

## 🎉 Next Steps

1. **Customize content:** Edit sample news to match your school
2. **Add more news:** Create announcements for upcoming events
3. **Test features:** Try all CRUD operations
4. **Share with team:** Show them the admin dashboard
5. **Go live:** Start using it for real announcements!

---

**Setup Time:** ~5 minutes  
**Difficulty:** ⭐⭐ (Easy)  
**Status:** Ready to Use! 🚀

---

**Last Updated:** December 5, 2025

