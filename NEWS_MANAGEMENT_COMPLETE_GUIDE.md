# School News Management System - Complete Implementation Guide

## 📋 Table of Contents
1. [Overview](#overview)
2. [Database Structure](#database-structure)
3. [Backend Implementation](#backend-implementation)
4. [Frontend Implementation](#frontend-implementation)
5. [API Documentation](#api-documentation)
6. [Installation & Setup](#installation--setup)
7. [Usage Guide](#usage-guide)
8. [Sample News Content](#sample-news-content)

---

## 🎯 Overview

The School News Management System is a comprehensive feature that allows administrators and principals to manage school news, announcements, and updates. The system includes:

- **Public News Display**: Shows published news on the Principal's Home Page
- **Admin Dashboard**: Full CRUD operations for news management
- **Category System**: Organize news by type (Events, Admission, Extracurricular, etc.)
- **Status Management**: Draft, Published, and Archived states
- **Featured News**: Highlight important announcements
- **View Tracking**: Monitor news engagement

---

## 🗄️ Database Structure

### News Table Schema

```sql
CREATE TABLE news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    published_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category VARCHAR(100) NOT NULL,
    image VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    author_name VARCHAR(255),
    created_by BIGINT NOT NULL,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_news_created_by FOREIGN KEY (created_by) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_news_status ON news(status);
CREATE INDEX idx_news_category ON news(category);
CREATE INDEX idx_news_published_date ON news(published_date);
CREATE INDEX idx_news_featured ON news(featured);
CREATE INDEX idx_news_created_by ON news(created_by);
```

### Field Descriptions

| Field | Type | Description |
|-------|------|-------------|
| id | BIGSERIAL | Primary key, auto-increment |
| title | VARCHAR(500) | News headline (required) |
| content | TEXT | Full news content (required) |
| published_date | TIMESTAMP | Date when news is/was published |
| category | VARCHAR(100) | News category (Event, Admission, etc.) |
| image | VARCHAR(500) | Emoji or icon for visual representation |
| status | VARCHAR(50) | DRAFT, PUBLISHED, or ARCHIVED |
| featured | BOOLEAN | Whether to feature prominently |
| author_name | VARCHAR(255) | Display name of author |
| created_by | BIGINT | Foreign key to users table |
| view_count | INTEGER | Number of views |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

---

## 🔧 Backend Implementation

### File Structure

```
backend/src/main/java/com/schoolmanagement/
├── entity/
│   ├── News.java
│   └── NewsStatus.java
├── repository/
│   └── NewsRepository.java
├── service/
│   └── NewsService.java
├── controller/
│   └── NewsController.java
└── dto/
    └── NewsDTO.java
```

### Key Components

#### 1. News Entity (`News.java`)
- JPA entity with all fields
- Automatic timestamp management with @PrePersist and @PreUpdate
- ManyToOne relationship with User entity
- Enum for status management

#### 2. News Repository (`NewsRepository.java`)
- Extends JpaRepository
- Custom queries for filtering by status, category, featured
- Search functionality
- Pagination support

#### 3. News Service (`NewsService.java`)
- Business logic layer
- CRUD operations
- Status management (publish, archive)
- View count tracking
- DTO conversion

#### 4. News Controller (`NewsController.java`)
- RESTful API endpoints
- Public endpoints (no auth required for viewing)
- Admin-only endpoints (requires ADMIN or PRINCIPAL role)
- Pagination and filtering support

---

## 💻 Frontend Implementation

### File Structure

```
frontend/src/
├── pages/
│   ├── PrincipalHomePage.js (Updated)
│   └── AdminNewsPage.js (New)
├── services/
│   └── newsService.js (New)
└── styles/
    └── AdminNews.css (New)
```

### Key Components

#### 1. PrincipalHomePage.js (Updated)
- Fetches and displays published news
- Shows top 3 recent news items
- Pagination support
- Loading and error states
- Fallback to static data if API fails

#### 2. AdminNewsPage.js (New)
- Full news management dashboard
- Create, edit, delete operations
- Publish and archive functionality
- Table view with sorting
- Modal form for editing
- Real-time status updates

#### 3. newsService.js (New)
- API integration layer
- All CRUD operations
- Public and admin endpoints
- Error handling

---

## 📡 API Documentation

### Public Endpoints (No Authentication Required)

#### Get Published News
```http
GET /api/news?page=0&size=10
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Annual Sports Day 2025",
      "content": "Join us for the exciting...",
      "publishedDate": "2025-11-16T09:00:00",
      "category": "Event",
      "image": "🏆",
      "status": "PUBLISHED",
      "featured": true,
      "authorName": "Principal School",
      "viewCount": 145
    }
  ],
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "number": 0
}
```

#### Get News by Category
```http
GET /api/news/category/{category}?page=0&size=10
```

#### Get Featured News
```http
GET /api/news/featured?page=0&size=5
```

#### Get Recent News
```http
GET /api/news/recent
```
Returns top 5 most recent published news.

#### Search News
```http
GET /api/news/search?keyword=admission&page=0&size=10
```

#### Get Single News
```http
GET /api/news/{id}
```
Automatically increments view count.

---

### Admin Endpoints (Authentication Required)

**Required Role:** ADMIN or PRINCIPAL

#### Get All News (Including Drafts)
```http
GET /api/news/admin/all?page=0&size=10
Authorization: Bearer {token}
```

#### Create News
```http
POST /api/news
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "New Admission Open",
  "content": "We are pleased to announce...",
  "category": "Admission",
  "image": "📚",
  "status": "DRAFT",
  "featured": false,
  "authorName": "Admin User"
}
```

#### Update News
```http
PUT /api/news/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "Updated content...",
  "category": "Event",
  "status": "PUBLISHED",
  "featured": true
}
```

#### Delete News
```http
DELETE /api/news/{id}
Authorization: Bearer {token}
```

#### Publish News
```http
PUT /api/news/{id}/publish
Authorization: Bearer {token}
```

#### Archive News
```http
PUT /api/news/{id}/archive
Authorization: Bearer {token}
```

---

## 🚀 Installation & Setup

### Step 1: Database Setup

Run the SQL script to create the table and insert sample data:

```bash
psql -U your_username -d your_database -f backend/NEWS_SETUP.sql
```

Or manually execute the SQL from `backend/NEWS_SETUP.sql`

### Step 2: Backend Setup

1. **Verify Backend Files**
   - Ensure all Java files are in place
   - Check that imports are correct

2. **Rebuild Backend**
   ```bash
   cd backend
   mvnw clean install
   ```

3. **Start Backend**
   ```bash
   mvnw spring-boot:run
   ```

   Backend should start on http://localhost:8080

### Step 3: Frontend Setup

1. **Install Dependencies** (if not already installed)
   ```bash
   cd frontend
   npm install
   ```

2. **Start Frontend**
   ```bash
   npm start
   ```

   Frontend should start on http://localhost:3000

### Step 4: Verify Installation

1. **Test Public Access**
   - Visit http://localhost:3000
   - Check if news appears on the homepage
   - Verify pagination works

2. **Test Admin Access**
   - Login as admin or principal
   - Navigate to News Management
   - Try creating, editing, and publishing news

---

## 📖 Usage Guide

### For Administrators/Principals

#### Creating News

1. **Login** as Admin or Principal
2. **Navigate** to "News Management" from the sidebar
3. **Click** "Create News" button
4. **Fill in the form:**
   - Title: Catchy headline
   - Content: Full article text
   - Category: Select appropriate category
   - Icon: Choose an emoji
   - Status: Draft or Published
   - Featured: Check if important
   - Author Name: (Optional) Auto-filled
5. **Submit** the form

#### Publishing News

- **From Draft:** Click the green "Publish" button (✓)
- **Direct Publish:** Set status to "PUBLISHED" when creating

#### Editing News

1. **Click** the blue "Edit" button (✏️)
2. **Modify** the fields
3. **Save** changes

#### Archiving News

- Click the yellow "Archive" button (📦) on published news
- Archived news won't appear on the public page

#### Deleting News

- Click the red "Delete" button (🗑️)
- Confirm the deletion
- **Warning:** This action is permanent

### For Public Visitors

#### Viewing News

- Visit the school homepage
- Scroll to "Latest News & Announcements"
- Click pagination to see more news

#### Reading Full Article

- Click on any news card
- View count is automatically tracked

---

## 📰 Sample News Content

### 1. Admissions Open for Academic Year 2026-2027

**Category:** Admission  
**Featured:** Yes  
**Icon:** 📚

**Content:**
```
We are pleased to announce that admissions are now open for the academic year 2026-2027 
for classes 1 to 11. Tay Son Secondary and High School invites applications from bright 
and motivated students who wish to be part of our excellent learning environment.

Key Highlights:
• Classes Available: 1 to 11 (All Streams: Science, Commerce, Arts)
• Admission Test Date: January 15, 2026
• Last Date for Application: January 5, 2026
• Application Mode: Online and Offline both available
• Scholarship opportunities for meritorious students
• Limited seats available - First come, first served basis

For more information, contact: admission@taysonsecondary.edu
```

### 2. Registration for Extracurricular Classes and Clubs

**Category:** Extracurricular  
**Featured:** Yes  
**Icon:** 🎯

**Content:**
```
Registration for extracurricular classes and clubs for the semester January-May 2026 
is now open! Join us to explore your talents, develop new skills, and make lasting friendships.

🎭 Cultural Activities:
• Drama and Theatre Club
• Music and Band
• Dance Academy
• Art and Craft Workshop

⚽ Sports Clubs:
• Football Academy
• Basketball Team
• Badminton Club
• Table Tennis

🧪 Academic Enrichment:
• Science Club
• Mathematics Olympiad Training
• Coding and Programming
• Debate and Public Speaking

Registration Period: December 5, 2025 - December 20, 2025
Early bird discount: 10% off if registered before December 15, 2025

Contact: activities@taysonsecondary.edu
```

### 3. Annual Sports Day 2025

**Category:** Event  
**Featured:** Yes  
**Icon:** 🏆

**Content:**
```
Join us for the exciting Annual Sports Day on December 15, 2025. Various sports 
competitions and cultural programs will be held throughout the day.

Events include:
- Track and Field
- Basketball
- Football
- Relay Races
- Cultural Performances

All parents and guardians are cordially invited. The event starts at 8:00 AM 
at the school ground.
```

---

## 🔒 Security Features

1. **Role-Based Access Control**
   - Only ADMIN and PRINCIPAL can manage news
   - Public can only view published news

2. **Authentication**
   - JWT token required for admin endpoints
   - Token validation on every request

3. **Data Validation**
   - Required fields enforced
   - Input sanitization
   - SQL injection prevention

---

## 🎨 Categories Available

- **Event** - School events and celebrations
- **Achievement** - Awards and accomplishments
- **Infrastructure** - New facilities and improvements
- **Admission** - Admission-related announcements
- **Extracurricular** - Clubs and activities
- **Meeting** - Parent-teacher meetings
- **Library** - Library updates
- **Other** - General announcements

---

## 📊 Features Summary

### For Administrators
✅ Create, edit, delete news  
✅ Publish/archive functionality  
✅ View statistics (view count)  
✅ Manage featured news  
✅ Category organization  
✅ Draft system  

### For Public Users
✅ View published news  
✅ Browse by category  
✅ Search functionality  
✅ Featured news section  
✅ Pagination  
✅ Responsive design  

---

## 🐛 Troubleshooting

### News not showing on homepage
- Check if news status is "PUBLISHED"
- Verify backend is running on port 8080
- Check browser console for errors

### Cannot create news
- Verify you're logged in as ADMIN or PRINCIPAL
- Check JWT token is valid
- Ensure all required fields are filled

### Database errors
- Verify news table exists
- Check foreign key constraint (created_by references users)
- Ensure indexes are created

---

## 📝 Next Steps

Future enhancements could include:

1. **Image Upload** - Upload actual images instead of emojis
2. **Rich Text Editor** - Better content formatting
3. **Comments System** - Allow users to comment
4. **Email Notifications** - Alert subscribers to new news
5. **Social Sharing** - Share news on social media
6. **Analytics Dashboard** - Detailed view statistics
7. **Multi-language Support** - Translate news content

---

## 📞 Support

For issues or questions:
- Check the logs: `backend/logs/application.log`
- Review API responses in browser DevTools
- Contact the development team

---

## ✅ Implementation Checklist

- [x] Database schema created
- [x] Sample data inserted
- [x] Backend entity created
- [x] Backend repository created
- [x] Backend service created
- [x] Backend controller created
- [x] Frontend service created
- [x] Frontend public page updated
- [x] Frontend admin page created
- [x] Routes configured
- [x] Sidebar updated
- [x] Documentation completed

---

**Status:** ✅ COMPLETE  
**Date:** December 5, 2025  
**Version:** 1.0.0

