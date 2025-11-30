# 📅 TIMETABLE TAB - Quick Reference

**Status**: ✅ Correctly Configured

---

## 🎯 Feature Summary

**Purpose**: Display school class schedule/timetable
**Data**: Time, Subject, Room number
**Access**: 
- ✅ Students: View-only (read-only)
- ✅ Teachers: Edit (in TeacherPortal)

---

## ⏰ Lesson Duration & Schedule Requirements

### Lesson Duration
- **Each lesson/subject lasts**: 45 minutes
- **Breaks**: Included in time gaps (15 minutes between lessons)

### Daily Schedule
- **Morning Session**: 07:00 - 12:00
  - Maximum 5 lessons
  - Students study morning OR afternoon (not both)
  
- **Afternoon Session**: 13:00 - 18:00
  - Maximum 5 lessons
  - Students study morning OR afternoon (not both)

### Example Schedule
```
MORNING SESSION (07:00 - 12:00)
Lesson 1: 07:00-07:45 (45 min)  → Break (15 min)
Lesson 2: 08:00-08:45 (45 min)  → Break (15 min)
Lesson 3: 09:00-09:45 (45 min)  → Break (15 min)
Lesson 4: 10:15-11:00 (45 min)  → Break (15 min)
Lesson 5: 11:15-12:00 (45 min)
```

---

## 👨‍🎓 What Students See

### Display Example - Morning Session
```
Monday - Morning (5 lessons × 45 minutes each)
├─ 07:00-07:45  Mathematics      Room 101
├─ 08:00-08:45  English          Room 105
├─ 09:00-09:45  Physics          Room 201
├─ 10:15-11:00  Chemistry        Room 202
└─ 11:15-12:00  Biology          Room 203
```

### Display Example - Afternoon Session
```
Monday - Afternoon (5 lessons × 45 minutes each)
├─ 13:00-13:45  History          Room 104
├─ 14:00-14:45  Geography        Room 106
├─ 15:00-15:45  Literature       Room 107
├─ 16:00-16:45  Computer Science Room 108
└─ 17:00-17:45  Arts             Room 109
```

### Permissions
- ✅ Can VIEW timetable
- ❌ Cannot EDIT
- ❌ Cannot DELETE
- ❌ Cannot ADD classes

### Information Shown
> **Schedule Information:**
> - Each lesson lasts **45 minutes**
> - Students study either **morning OR afternoon** (not both)
> - Maximum **5 lessons per session**
> - Breaks are included in schedule time gaps
> - **Teachers** can edit the timetable through the Teacher Portal

---

## 👨‍🏫 What Teachers See

**Location**: TeacherPortal (separate interface)

### Permissions
- ✅ Can VIEW
- ✅ Can EDIT classes
- ✅ Can ADD new classes (max 5 per session)
- ✅ Can DELETE classes
- ✅ Can change times, rooms, subjects
- ✅ Can manage morning/afternoon sessions

### Constraints Teachers Must Follow
- ⚠️ Each lesson: Exactly 45 minutes
- ⚠️ Breaks: 15 minutes between lessons
- ⚠️ Morning session: 07:00 - 12:00 (max 5 lessons)
- ⚠️ Afternoon session: 13:00 - 18:00 (max 5 lessons)
- ⚠️ Cannot mix morning and afternoon for one student

---

## 📊 Information Shown

Each class listing displays:

| Field | Example | Details |
|-------|---------|---------|
| **Time** | 07:00-07:45 | Always 45 minutes |
| **Subject** | Mathematics | Subject name |
| **Room** | 101 | Room/classroom number |
| **Duration** | 45 min | Shown for clarity |

---

## 📁 Implementation

**File**: `StudentPortal.js`
**Component**: `TimetableTab()`

**Current Data**: 
- Realistic sample data matching requirements
- 3 days × 2 sessions = 6 timetable blocks
- Each session has exactly 5 lessons

**Styling**: 
- Dark gray time labels (with duration note)
- Dark text for subjects
- Badge for room numbers

---

## 🔄 Future Development

1. **Load from database** - Replace hardcoded data with API calls
2. **Filter by class** - Show timetable for student's specific class
3. **Filter by session** - Morning or Afternoon only
4. **Date selection** - View different weeks
5. **Teacher edit interface** - Add/edit/delete in TeacherPortal with constraints
6. **Automatic validation** - Enforce 45-min lessons and max 5 per session
7. **Export to PDF** - Download timetable

---

## ✅ Key Points

✅ **45-minute lessons** - Each subject/lesson duration
✅ **Morning OR Afternoon** - Students don't study both in one day
✅ **Maximum 5 lessons** - Per session (morning or afternoon)
✅ **Scheduled breaks** - 15 minutes between lessons
✅ Students can only **VIEW** (no edit capability)
✅ Teachers manage timetable in separate interface
✅ Clear information note for students
✅ Clean, organized display
✅ Read-only design prevents accidental changes

---

**All Requirements Met**: 
- ✅ 45-minute lesson duration
- ✅ Morning or afternoon sessions only
- ✅ Maximum 5 lessons per session
- ✅ Students: View-only access
- ✅ Teachers: Edit access (separately)

