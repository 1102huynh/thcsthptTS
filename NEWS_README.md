# 📰 School News Management System

> A comprehensive news management system for schools with public display and admin dashboard

## 🌟 Features

### For Public Users
- ✅ View published school news and announcements
- ✅ Browse news with pagination (3 per page)
- ✅ See news organized by categories
- ✅ View publication dates and view counts
- ✅ Responsive design for all devices

### For Administrators
- ✅ Create, edit, and delete news articles
- ✅ Publish draft news or archive old news
- ✅ Set featured news for prominent display
- ✅ Organize news by category
- ✅ Track view statistics
- ✅ Role-based access control (Admin & Principal only)

## 🚀 Quick Start

### 1. Database Setup (1 minute)
```bash
psql -U postgres -d your_database -f backend/NEWS_SETUP.sql
```

### 2. Start Backend (2 minutes)
```bash
cd backend
mvnw spring-boot:run
```

### 3. Start Frontend (1 minute)
```bash
cd frontend
npm start
```

### 4. Access the System
- **Homepage:** http://localhost:3000
- **Admin Login:** Username: `admin`, Password: `admin123`
- **News Management:** After login, click "News Management" in sidebar

## 📚 Documentation

- **[Complete Guide](NEWS_MANAGEMENT_COMPLETE_GUIDE.md)** - Full documentation with API details
- **[Quick Start](NEWS_QUICK_START.md)** - 5-minute setup guide
- **[Deployment Checklist](NEWS_DEPLOYMENT_CHECKLIST.md)** - Production deployment guide
- **[Visual Overview](NEWS_VISUAL_OVERVIEW.md)** - Visual diagrams and charts
- **[Implementation Summary](NEWS_IMPLEMENTATION_SUMMARY.md)** - Project summary

## 🗂️ Sample Content Included

The system includes 10 pre-loaded news articles:

1. **📚 Admissions Open for Academic Year 2026-2027** (Featured)
   - Complete admission information
   - Application deadlines and process
   - Scholarship details

2. **🎯 Registration for Extracurricular Classes** (Featured)
   - Cultural activities (Drama, Music, Dance, Art)
   - Sports clubs (Football, Basketball, Badminton)
   - Academic enrichment programs
   - Registration deadlines and discounts

3. **🏆 Annual Sports Day 2025** (Featured)
4. **💻 New Computer Lab Inauguration** (Featured)
5. **🎖️ Excellence Awards Ceremony**
6. **🔬 Science Exhibition 2026**
7. **👨‍👩‍👧‍👦 Parent-Teacher Meeting**
8. **📖 New Library Books**
9. **⛺ Winter Camp 2026** (Draft)

## 🎨 News Categories

- **Event** 🎉 - School events and celebrations
- **Achievement** 🎖️ - Awards and accomplishments
- **Infrastructure** 💻 - New facilities and improvements
- **Admission** 📚 - Admission-related announcements
- **Extracurricular** 🎯 - Clubs and activities
- **Meeting** 👨‍👩‍👧‍👦 - Parent-teacher meetings
- **Library** 📖 - Library updates
- **Other** 📰 - General announcements

## 🔐 Security

- **JWT Authentication** - Secure token-based authentication
- **Role-Based Access Control** - Only Admin and Principal can manage news
- **Input Validation** - Prevents invalid data entry
- **SQL Injection Prevention** - Parameterized queries

## 📡 API Endpoints

### Public Endpoints
```
GET  /api/news                    - Get all published news
GET  /api/news/category/{cat}     - Get news by category
GET  /api/news/featured            - Get featured news
GET  /api/news/recent              - Get 5 most recent news
GET  /api/news/search              - Search news
GET  /api/news/{id}                - Get single news item
```

### Admin Endpoints (Requires Authentication)
```
GET    /api/news/admin/all         - Get all news (including drafts)
POST   /api/news                   - Create news
PUT    /api/news/{id}              - Update news
DELETE /api/news/{id}              - Delete news
PUT    /api/news/{id}/publish      - Publish draft news
PUT    /api/news/{id}/archive      - Archive published news
```

## 💻 Technology Stack

### Backend
- **Spring Boot 3.x** - Application framework
- **Java 17+** - Programming language
- **Spring Security** - Authentication & authorization
- **JPA/Hibernate** - ORM
- **PostgreSQL** - Database

### Frontend
- **React 18** - UI framework
- **React Bootstrap** - UI components
- **Axios** - HTTP client
- **React Router** - Navigation
- **React Icons** - Icons

## 🏗️ File Structure

```
backend/
├── entity/News.java              - JPA entity
├── entity/NewsStatus.java        - Status enum
├── repository/NewsRepository.java - Database operations
├── service/NewsService.java      - Business logic
├── controller/NewsController.java - REST API
├── dto/NewsDTO.java              - Data transfer object
└── NEWS_SETUP.sql                - Database script

frontend/
├── pages/AdminNewsPage.js        - Admin dashboard
├── pages/PrincipalHomePage.js    - Public display (updated)
├── services/newsService.js       - API integration
└── styles/AdminNews.css          - Styling
```

## 🎯 Usage Guide

### Creating News (Admin)
1. Login as Admin or Principal
2. Navigate to "News Management"
3. Click "Create News" button
4. Fill in the form:
   - Title (required)
   - Content (required)
   - Category (required)
   - Icon/Emoji (optional)
   - Status (Draft/Published)
   - Featured (checkbox)
5. Click "Create News"

### Publishing News
- Set status to "Published" when creating, OR
- Create as "Draft" and click the "Publish" button (✓) later

### Editing News
1. Click the "Edit" button (✏️) on any news item
2. Modify the fields
3. Click "Update News"

### Archiving News
- Click the "Archive" button (📦) on published news
- Archived news won't appear on the homepage

### Deleting News
- Click the "Delete" button (🗑️)
- Confirm the deletion
- ⚠️ This action is permanent

## 📊 Database Schema

```sql
news (
  id              BIGSERIAL PRIMARY KEY,
  title           VARCHAR(500) NOT NULL,
  content         TEXT NOT NULL,
  published_date  TIMESTAMP NOT NULL,
  category        VARCHAR(100) NOT NULL,
  image           VARCHAR(500),
  status          VARCHAR(50) NOT NULL,
  featured        BOOLEAN DEFAULT FALSE,
  author_name     VARCHAR(255),
  created_by      BIGINT REFERENCES users(id),
  view_count      INTEGER DEFAULT 0,
  created_at      TIMESTAMP DEFAULT NOW(),
  updated_at      TIMESTAMP DEFAULT NOW()
)
```

## 🔧 Configuration

### Backend Configuration
Edit `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Frontend Configuration
Edit `services/api.js`:
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## 🐛 Troubleshooting

### Issue: News not showing on homepage
**Solution:**
- Verify backend is running on port 8080
- Check if news status is "PUBLISHED"
- Clear browser cache

### Issue: Cannot create news
**Solution:**
- Ensure you're logged in as ADMIN or PRINCIPAL
- Check all required fields are filled
- Verify JWT token is valid

### Issue: Database connection error
**Solution:**
- Check PostgreSQL is running
- Verify database credentials
- Ensure news table exists

## 📞 Support

For help and support:
- Check the documentation files
- Review backend logs: `backend/logs/`
- Inspect browser console for errors
- Contact the development team

## 🎉 What's Next?

Potential future enhancements:
- Image upload functionality
- Rich text editor for content
- Comments system
- Email notifications
- Social media sharing
- Analytics dashboard
- Multi-language support

## 📝 License

This feature is part of the School Management System.

## 🙏 Acknowledgments

Developed for Tay Son Secondary and High School

---

**Version:** 1.0.0  
**Status:** ✅ Production Ready  
**Last Updated:** December 5, 2025

---

## 🚀 Ready to Deploy!

All systems are ready for production use. Follow the [Deployment Checklist](NEWS_DEPLOYMENT_CHECKLIST.md) for a smooth launch.

