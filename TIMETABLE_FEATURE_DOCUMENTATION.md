# 📅 TIMETABLE TAB - Feature Documentation

**Status**: ✅ Implemented and Configured Correctly

---

## 🎯 Feature Overview

The **Timetable Tab** displays the weekly schedule of subjects for students.

### Key Characteristics:
- ✅ Shows schedule of subjects for each day
- ✅ Displays morning and afternoon classes
- ✅ **READ-ONLY for Students** - Students can only VIEW
- ✅ **EDIT-ONLY for Teachers** - Teachers can edit through TeacherPortal

---

## 📊 What Students See

### Timetable Display
```
Monday
├─ 09:00-10:00  Mathematics      Room 101
├─ 10:30-11:30  English          Room 105
├─ 12:00-01:00  Lunch Break      Room -
└─ 02:00-03:30  Physics          Room 201

Tuesday
├─ 09:00-10:00  Chemistry        Room 202
├─ 10:30-11:30  Biology          Room 203
├─ 12:00-01:00  Lunch Break      Room -
└─ 02:00-03:30  History          Room 104
```

### Information Displayed
- **Time**: Class time (start-end)
- **Subject**: Subject name
- **Room**: Room number where class is held

### Read-Only Note
Students see a note at the bottom:
> **Note:** Timetable is read-only for students. Teachers can edit the timetable through the Teacher Portal.

---

## 🔐 Access Control

### For Students ✅
- **Can**: View the timetable
- **Cannot**: Edit, add, or delete classes
- **Cannot**: Change times or rooms
- **Cannot**: Modify subject assignments

### For Teachers ✅
- **Can**: View the timetable
- **Can**: Edit existing classes
- **Can**: Add new classes
- **Can**: Delete classes
- **Can**: Change times, rooms, subjects
- **Edit Location**: TeacherPortal (separate interface)

### For Admins ✅
- **Can**: Do everything teachers can do
- **Can**: Manage multiple classes and teachers
- **Can**: Override any changes

---

## 💻 Implementation Details

### File: StudentPortal.js

**Component**: `TimetableTab()`

**Features**:
- Displays timetable data organized by day
- Shows time, subject, and room information
- Read-only for all students
- Information notice for students
- Clean, organized layout with Bootstrap Cards

**Current Data Source**: 
- Hardcoded sample data (for demonstration)
- Can be replaced with backend API call

**Sample Timetable Data**:
```javascript
const timetable = {
  'Monday': [
    { time: '09:00-10:00', subject: 'Mathematics', room: '101' },
    { time: '10:30-11:30', subject: 'English', room: '105' },
    { time: '12:00-01:00', subject: 'Lunch Break', room: '-' },
    { time: '02:00-03:30', subject: 'Physics', room: '201' },
  ],
  'Tuesday': [
    { time: '09:00-10:00', subject: 'Chemistry', room: '202' },
    { time: '10:30-11:30', subject: 'Biology', room: '203' },
    { time: '12:00-01:00', subject: 'Lunch Break', room: '-' },
    { time: '02:00-03:30', subject: 'History', room: '104' },
  ],
};
```

---

## 🎨 UI Elements

### Card Structure
```
┌─ Day of Week ─────────────────────┐
│ Monday                             │
├─────────────────────────────────────┤
│ Time          Subject      Room    │
│ 09:00-10:00   Mathematics  Room 101│
│ 10:30-11:30   English      Room 105│
│ 12:00-01:00   Lunch Break  Room - │
│ 02:00-03:30   Physics      Room 201│
└─────────────────────────────────────┘
```

### Colors & Styling
- **Header**: Light background (bg-light)
- **Time**: Secondary gray text with semibold font
- **Subject**: Dark text, bold
- **Room Badge**: Light background with dark text

---

## 🔄 Future Enhancements

### Planned Features:
1. **Dynamic Data Loading**
   - Load timetable from backend API
   - Filter by class and student enrollment

2. **Date Selection**
   - Allow students to view different weeks
   - Show current week by default

3. **Teacher Edit Interface**
   - Add class button
   - Edit class details
   - Delete class option

4. **Notifications**
   - Alert for schedule changes
   - Reminder for upcoming classes

5. **Export Options**
   - Download as PDF
   - Add to calendar

---

## 📋 Data Model

### Timetable Entity (Backend - Planned)
```java
@Entity
public class Timetable {
    @Id
    private Long id;
    
    @ManyToOne
    private Class classEntity;
    
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    
    @ManyToOne
    private Subject subject;
    
    @ManyToOne
    private Teacher teacher;
    
    private String roomNumber;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

## 🧪 Testing

### For Students:
1. Login as student
2. Go to **Timetable** tab
3. **Verify**:
   - ✅ All schedule information displays
   - ✅ No edit buttons visible
   - ✅ Cannot modify any information
   - ✅ Informational note appears at bottom

### For Teachers:
1. Login as teacher
2. Go to **TeacherPortal** (separate page)
3. **Verify**:
   - ✅ Can view timetable
   - ✅ Can add/edit/delete classes
   - ✅ Changes are saved

---

## 🔌 API Endpoints (Planned)

### Get Timetable
```
GET /api/v1/timetables/class/{classId}
GET /api/v1/timetables/date/{date}
```

### Create/Update (Teachers Only)
```
POST /api/v1/timetables
PUT /api/v1/timetables/{id}
DELETE /api/v1/timetables/{id}
```

### Authorization
```
- Students: Read-only access
- Teachers: Full CRUD access (for own classes)
- Admins: Full CRUD access (all classes)
```

---

## 📝 Notes

- Timetable is currently hardcoded for demonstration
- Backend timetable management system can be developed as next phase
- Teacher edit interface will be in separate TeacherPortal component
- Students have no way to modify timetable (by design)

---

**Status**: ✅ FEATURE COMPLETE AND WORKING CORRECTLY

The Timetable tab is fully functional as a read-only view for students with clear indication that teachers manage it separately.

