# 🚀 TIMETABLE - QUICK START GUIDE

**Status**: ✅ Complete Implementation

---

## 📦 What You Get

### Database
- 1 SQL file with complete schema
- 60 test data entries
- 3 indexes for performance
- Ready to run

### Backend Code
- 5 Java files (Entity, DTO, Repository, Service, Controller)
- Full CRUD operations
- Authorization built-in
- REST API ready

### Documentation
- Complete API reference
- Authorization rules
- Test data explanation
- Usage examples

---

## ⚡ Quick Setup (5 minutes)

### 1. Create Database Table
```bash
# Execute TIMETABLE_SETUP.sql in PostgreSQL
psql -U user -d database -f TIMETABLE_SETUP.sql
```

### 2. Copy Java Files
```bash
# Copy 5 files to appropriate directories in backend/
Timetable.java → src/main/java/com/schoolmanagement/entity/
TimetableDTO.java → src/main/java/com/schoolmanagement/dto/
TimetableRepository.java → src/main/java/com/schoolmanagement/repository/
TimetableService.java → src/main/java/com/schoolmanagement/service/
TimetableController.java → src/main/java/com/schoolmanagement/controller/
```

### 3. Compile Backend
```bash
mvn clean install
```

### 4. Start Backend
```bash
mvn spring-boot:run
```

### 5. Test API
```bash
curl http://localhost:8080/api/v1/timetables/class/1?academicYear=2024-2025
```

---

## 📊 Test Data

### Class 10A (Morning)
- **ID**: 1
- **Students**: student1, student2, student3
- **Teacher**: teacher1
- **Time**: 07:00-12:00
- **Records**: 30

### Class 10B (Afternoon)
- **ID**: 2
- **Students**: student4, student5, student6
- **Teacher**: teacher2
- **Time**: 13:00-18:00
- **Records**: 30

---

## 🔌 API Endpoints

### View Timetable
```bash
# Get full class timetable
GET /api/v1/timetables/class/1?academicYear=2024-2025

# Get Monday schedule
GET /api/v1/timetables/class/1/day/MONDAY

# Get morning session
GET /api/v1/timetables/class/1/day/MONDAY/session/MORNING
```

### Edit Timetable (Homeroom Teacher Only)
```bash
# Create new entry
POST /api/v1/timetables/class/1

# Update entry
PUT /api/v1/timetables/1

# Delete entry
DELETE /api/v1/timetables/1
```

---

## 🔐 Authorization

| User Type | View | Edit |
|-----------|------|------|
| Student | ✅ Own class only | ❌ |
| Homeroom Teacher | ✅ Own class | ✅ Own class |
| Other Teacher | ✅ All classes | ❌ |
| Admin | ✅ All classes | ✅ All classes |

---

## 📁 Files Created

| File | Type | Purpose |
|------|------|---------|
| TIMETABLE_SETUP.sql | SQL | Database schema & test data |
| Timetable.java | Entity | JPA mapping |
| TimetableDTO.java | DTO | API response object |
| TimetableRepository.java | Repository | Database queries |
| TimetableService.java | Service | Business logic & auth |
| TimetableController.java | Controller | REST endpoints |
| TIMETABLE_DATABASE_API.md | Doc | Complete reference |
| TIMETABLE_IMPLEMENTATION_GUIDE.md | Doc | Integration guide |
| TIMETABLE_QUICK_START.md | Doc | This file |

---

## ✅ Features

✅ **No Hardcoded Data** - Everything from database
✅ **Authorization** - Only homeroom teacher can edit
✅ **Multiple Queries** - By class, day, session
✅ **Full CRUD** - Create, Read, Update, Delete
✅ **REST API** - Complete endpoints
✅ **60 Test Entries** - Ready to use
✅ **Indexed Queries** - Fast performance
✅ **Error Handling** - Proper HTTP codes

---

## 🧪 Quick Test

```bash
# As Student (view only)
curl -H "Authorization: Bearer <student-token>" \
  http://localhost:8080/api/v1/timetables/class/1

# As Homeroom Teacher (can edit)
curl -X POST \
  -H "Authorization: Bearer <teacher-token>" \
  -H "Content-Type: application/json" \
  -d '{"dayOfWeek":"TUESDAY","sessionType":"MORNING","timeSlot":1,"startTime":"07:00:00","endTime":"07:45:00","subject":"Math","classroom":"A","academicYear":"2024-2025"}' \
  http://localhost:8080/api/v1/timetables/class/1

# As Non-Homeroom Teacher (403 Forbidden)
curl -X POST \
  -H "Authorization: Bearer <other-teacher-token>" \
  ... # Same data
  http://localhost:8080/api/v1/timetables/class/1
```

---

## 🎯 Next Steps

1. ✅ Run SQL script
2. ✅ Copy Java files
3. ✅ Compile backend
4. ✅ Test API endpoints
5. ✅ Update frontend to load from API

---

## 📚 Full Documentation

- `TIMETABLE_DATABASE_API.md` - Complete reference
- `TIMETABLE_IMPLEMENTATION_GUIDE.md` - Step-by-step guide
- `TIMETABLE_SETUP.sql` - Database script with comments

---

**Status**: ✅ READY TO USE

All files created and ready for integration!

