# 📅 TIMETABLE - Database Implementation & API

**Date**: November 22, 2025
**Status**: ✅ Complete with Database & API

---

## 🎯 Overview

Implemented complete timetable system with:
- ✅ Database table creation
- ✅ Test data for 2 classes
- ✅ Service layer with business logic
- ✅ REST API endpoints
- ✅ Authorization/Access control

---

## 📊 Database Schema

### Table: `timetables`

```sql
CREATE TABLE timetables (
    id SERIAL PRIMARY KEY,
    class_id INTEGER NOT NULL (FK: classes),
    day_of_week VARCHAR(20) NOT NULL,  -- MONDAY, TUESDAY, etc.
    session_type VARCHAR(20) NOT NULL, -- MORNING or AFTERNOON
    time_slot INTEGER NOT NULL,         -- 1-5 (lesson order)
    start_time TIME NOT NULL,           -- HH:MM:SS
    end_time TIME NOT NULL,             -- HH:MM:SS
    subject VARCHAR(100) NOT NULL,
    classroom VARCHAR(10) NOT NULL,     -- Usually "A"
    academic_year VARCHAR(10) NOT NULL, -- 2024-2025
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_by_id INTEGER (FK: users),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (class_id, day_of_week, time_slot, session_type),
    FOREIGN KEY (class_id) REFERENCES classes(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id)
);
```

### Indexes
- `idx_timetable_class_id` - Fast lookup by class
- `idx_timetable_academic_year` - Filter by academic year
- `idx_timetable_day_session` - Quick session filtering

---

## 📋 Test Data

### Class 10A - Morning (Group A)
- **Class ID**: 1
- **Students**: student1 (ID: 8), student2 (ID: 9), student3 (ID: 10)
- **Homeroom Teacher**: teacher1 (John Smith, User ID: 3)
- **Session**: MORNING (07:00-12:00)
- **Classroom**: A
- **Academic Year**: 2024-2025
- **Lessons Per Day**: 5 (45 minutes each, 15-minute breaks)
- **Days**: Monday - Saturday

### Class 10B - Afternoon (Group B)
- **Class ID**: 2
- **Students**: student4 (ID: 11), student5 (ID: 12), student6 (ID: 13)
- **Homeroom Teacher**: teacher2 (Sarah Johnson, User ID: 4)
- **Session**: AFTERNOON (13:00-18:00)
- **Classroom**: A (same classroom, different time slot)
- **Academic Year**: 2024-2025
- **Lessons Per Day**: 5 (45 minutes each, 15-minute breaks)
- **Days**: Monday - Saturday

### Sample Timetable Entry
```
Class 10A - Monday - Morning - Lesson 1
- Day: MONDAY
- Session: MORNING
- Time Slot: 1
- Start Time: 07:00:00
- End Time: 07:45:00
- Subject: Mathematics
- Classroom: A
- Academic Year: 2024-2025
- Created By: teacher1 (User ID: 3)
```

---

## 🏗️ Backend Architecture

### File Structure
```
backend/src/main/java/com/schoolmanagement/
├── entity/
│   └── Timetable.java           (JPA Entity)
├── dto/
│   └── TimetableDTO.java        (Data Transfer Object)
├── repository/
│   └── TimetableRepository.java (Data Access Layer)
├── service/
│   └── TimetableService.java    (Business Logic)
└── controller/
    └── TimetableController.java (REST API)
```

### Entity: Timetable.java
- JPA entity mapped to `timetables` table
- Relationships:
  - ManyToOne with `SchoolClass`
  - ManyToOne with `User` (created_by)
- Enums: `DayOfWeek` (from Java time API)

### Service: TimetableService.java
Key methods:
- `getTimetableByClass(classId, academicYear)` - View timetable
- `getTimetableByDay(classId, dayOfWeek, academicYear)` - View by day
- `getTimetableBySession(classId, dayOfWeek, sessionType, academicYear)` - View by session
- `createTimetable(classId, dto, user)` - Create (homeroom teacher only)
- `updateTimetable(timetableId, dto, user)` - Update (homeroom teacher only)
- `deleteTimetable(timetableId, user)` - Delete (homeroom teacher only)

### Authorization Logic
```java
// Check if user is homeroom teacher of the class
if (schoolClass.getClassTeacher().getUser().getId().equals(currentUser.getId())) {
    // Allow EDIT
} else {
    // DENY - only allow VIEW
}
```

---

## 🔌 REST API Endpoints

### 1. Get Timetable for a Class
```
GET /api/v1/timetables/class/{classId}?academicYear=2024-2025

Response: List<TimetableDTO>
Status: 200 OK
Access: All students of class, teachers, admins
```

### 2. Get Timetable for a Specific Day
```
GET /api/v1/timetables/class/{classId}/day/{dayOfWeek}?academicYear=2024-2025

Parameters:
  - dayOfWeek: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY

Response: List<TimetableDTO>
Status: 200 OK
Access: All students of class, teachers, admins
```

### 3. Get Timetable for a Specific Session
```
GET /api/v1/timetables/class/{classId}/day/{dayOfWeek}/session/{sessionType}?academicYear=2024-2025

Parameters:
  - dayOfWeek: MONDAY, TUESDAY, etc.
  - sessionType: MORNING, AFTERNOON

Response: List<TimetableDTO>
Status: 200 OK
Access: All students of class, teachers, admins
```

### 4. Create Timetable Entry
```
POST /api/v1/timetables/class/{classId}

Request Body:
{
  "dayOfWeek": "MONDAY",
  "sessionType": "MORNING",
  "timeSlot": 1,
  "startTime": "07:00:00",
  "endTime": "07:45:00",
  "subject": "Mathematics",
  "classroom": "A",
  "academicYear": "2024-2025"
}

Response: TimetableDTO
Status: 201 CREATED
Access: Homeroom teacher of the class ONLY
Error: 403 FORBIDDEN if not homeroom teacher
```

### 5. Update Timetable Entry
```
PUT /api/v1/timetables/{timetableId}

Request Body:
{
  "subject": "English",
  "startTime": "07:00:00",
  "endTime": "07:45:00",
  "classroom": "A",
  "status": "ACTIVE"
}

Response: TimetableDTO
Status: 200 OK
Access: Homeroom teacher of the class ONLY
Error: 403 FORBIDDEN if not homeroom teacher
```

### 6. Delete Timetable Entry
```
DELETE /api/v1/timetables/{timetableId}

Response: { "message": "Timetable entry deleted successfully" }
Status: 200 OK
Access: Homeroom teacher of the class ONLY
Error: 403 FORBIDDEN if not homeroom teacher
```

---

## 🔐 Authorization Rules

### Student Access
- ✅ **Can VIEW** their class timetable
- ✅ **Can VIEW** timetable by day
- ✅ **Can VIEW** timetable by session
- ❌ **Cannot EDIT, CREATE, or DELETE**

**Logic**: 
```sql
SELECT * FROM timetables 
WHERE class_id = (SELECT class_id FROM students WHERE user_id = :currentUserId)
  AND academic_year = '2024-2025'
```

### Homeroom Teacher Access
- ✅ **Can VIEW** their class timetable
- ✅ **Can CREATE** new timetable entries
- ✅ **Can EDIT** existing timetable entries
- ✅ **Can DELETE** timetable entries

**Logic**:
```java
if (schoolClass.getClassTeacher().getUser().getId().equals(currentUser.getId())) {
    // Allow all operations (READ, CREATE, UPDATE, DELETE)
}
```

### Other Teachers Access
- ✅ **Can VIEW** timetables of classes
- ❌ **Cannot EDIT** (unless they are homeroom teacher)

### Admin Access
- ✅ **Can VIEW** all timetables
- ✅ **Can EDIT** all timetables
- ✅ **Can CREATE/DELETE** all timetables

---

## 📝 Query Examples

### Get Class 10A Full Timetable
```bash
curl -X GET "http://localhost:8080/api/v1/timetables/class/1?academicYear=2024-2025"
```

### Get Monday Schedule for Class 10A
```bash
curl -X GET "http://localhost:8080/api/v1/timetables/class/1/day/MONDAY?academicYear=2024-2025"
```

### Get Morning Session for Class 10A on Monday
```bash
curl -X GET "http://localhost:8080/api/v1/timetables/class/1/day/MONDAY/session/MORNING?academicYear=2024-2025"
```

### Create New Timetable Entry (Homeroom Teacher Only)
```bash
curl -X POST "http://localhost:8080/api/v1/timetables/class/1" \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "dayOfWeek": "TUESDAY",
    "sessionType": "MORNING",
    "timeSlot": 2,
    "startTime": "08:00:00",
    "endTime": "08:45:00",
    "subject": "English",
    "classroom": "A",
    "academicYear": "2024-2025"
  }'
```

### Update Timetable Entry (Homeroom Teacher Only)
```bash
curl -X PUT "http://localhost:8080/api/v1/timetables/1" \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Physics",
    "startTime": "07:00:00",
    "endTime": "07:45:00",
    "classroom": "A",
    "status": "ACTIVE"
  }'
```

### Delete Timetable Entry (Homeroom Teacher Only)
```bash
curl -X DELETE "http://localhost:8080/api/v1/timetables/1" \
  -H "Authorization: Bearer <jwt-token>"
```

---

## ✅ Implementation Checklist

- [x] Create Timetable entity
- [x] Create TimetableRepository
- [x] Create TimetableDTO
- [x] Create TimetableService with authorization
- [x] Create TimetableController with API endpoints
- [x] Create database schema (TIMETABLE_SETUP.sql)
- [x] Insert test data (60 timetable entries)
- [x] Authorization rules implemented
- [x] REST API endpoints working
- [x] No hardcoded data - all from database

---

## 🧪 Testing Instructions

### 1. Run SQL Script
```sql
-- Execute TIMETABLE_SETUP.sql to create table and insert test data
-- Connection: PostgreSQL (Aiven Cloud)
```

### 2. Test as Student (View Only)
```bash
# Login as student1
POST /api/v1/auth/login
{ "username": "student1", "password": "..." }

# Get bearer token, then:
GET /api/v1/timetables/class/1?academicYear=2024-2025
# Returns: Full timetable for Class 10A
```

### 3. Test as Homeroom Teacher (Edit Rights)
```bash
# Login as teacher1 (homeroom teacher of Class 10A)
POST /api/v1/auth/login
{ "username": "teacher1", "password": "..." }

# Create new timetable entry
POST /api/v1/timetables/class/1
{ ... timetable data ... }

# Update existing entry
PUT /api/v1/timetables/{id}
{ ... updated data ... }

# Delete entry
DELETE /api/v1/timetables/{id}
```

### 4. Verify Authorization
```bash
# Login as student (not homeroom teacher)
# Try to create timetable entry:
POST /api/v1/timetables/class/1
# Expected: 403 FORBIDDEN - "Only the homeroom teacher can create timetable"
```

---

## 📊 Data Flow

```
STUDENT REQUEST
    ↓
GET /api/v1/timetables/class/{classId}
    ↓
TimetableController.getTimetableByClass()
    ↓
TimetableService.getTimetableByClass()
    ↓
TimetableRepository.findBySchoolClassAndAcademicYear()
    ↓
Database Query:
SELECT * FROM timetables 
WHERE class_id = ? AND academic_year = ?
    ↓
TimetableDTO[] (mapped from entities)
    ↓
JSON Response to Student
```

```
HOMEROOM TEACHER REQUEST (CREATE)
    ↓
POST /api/v1/timetables/class/{classId}
    ↓
TimetableController.createTimetable()
    ↓
Verify: user.staff.classTeacher.id == schoolClass.classTeacher.id
    ├─ If YES: Continue
    └─ If NO: Throw UnauthorizedException
    ↓
TimetableService.createTimetable()
    ↓
Check: Slot already exists?
    ├─ If YES: Throw IllegalArgumentException
    └─ If NO: Continue
    ↓
Create new Timetable entity
    ↓
TimetableRepository.save()
    ↓
INSERT INTO timetables ...
    ↓
TimetableDTO (mapped from saved entity)
    ↓
201 CREATED Response with new timetable
```

---

## 🚀 Features

✅ **Database driven** - No hardcoded data
✅ **Authorization** - Homeroom teacher edit rights only
✅ **Multiple queries** - By class, day, session
✅ **Academic year filtering** - Support multiple years
✅ **Full CRUD** - Create, Read, Update, Delete
✅ **REST API** - Complete endpoints
✅ **Error handling** - Proper HTTP status codes
✅ **Test data** - 60 entries across 2 classes

---

## 🔄 Future Enhancements

1. **Batch Operations**:
   - Import timetable from CSV/Excel
   - Export timetable to PDF

2. **Advanced Filtering**:
   - Filter by teacher
   - Filter by subject
   - Filter by room

3. **Notifications**:
   - Notify students of schedule changes
   - Send weekly timetable reminder

4. **Calendar Integration**:
   - Export to Google Calendar
   - iCal format support

5. **Mobile App**:
   - Push notifications
   - Offline access

---

**Status**: ✅ COMPLETE - Production Ready!

All timetable data is now loaded from database with proper authorization!

