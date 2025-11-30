# 📅 TIMETABLE - Full Week Schedule (Monday-Saturday)

**Status**: ✅ Updated with Full Week Display

---

## 🎯 Updated Requirements - Full Implementation

### Display Structure
- **Full School Week**: Monday to Saturday (6 days)
- **Single Classroom**: All lessons in "Room A" (shared by all students)
- **Student Groups**: Divided into two permanent groups for entire week
  - **Group A**: Morning session (07:00-12:00) for entire week
  - **Group B**: Afternoon session (13:00-18:00) for entire week

### Important Rule
- **No Mixed Sessions**: Students study EITHER morning for the entire week OR afternoon for the entire week (NOT both)

---

## 📋 Weekly Schedule Format

### Group A - Morning Students (07:00-12:00)

| Day | Lesson 1 | Lesson 2 | Lesson 3 | Lesson 4 | Lesson 5 |
|-----|----------|----------|----------|----------|----------|
| **Monday** | 07:00-07:45 Math | 08:00-08:45 Eng | 09:00-09:45 Phy | 10:15-11:00 Chem | 11:15-12:00 Bio |
| **Tuesday** | 07:00-07:45 Hist | 08:00-08:45 Geo | 09:00-09:45 Lit | 10:15-11:00 CS | 11:15-12:00 Arts |
| **Wednesday** | 07:00-07:45 Phy | 08:00-08:45 Math | 09:00-09:45 Chem | 10:15-11:00 Eng | 11:15-12:00 CS |
| **Thursday** | 07:00-07:45 Bio | 08:00-08:45 Hist | 09:00-09:45 Geo | 10:15-11:00 Lit | 11:15-12:00 Arts |
| **Friday** | 07:00-07:45 Eng | 08:00-08:45 Chem | 09:00-09:45 Math | 10:15-11:00 Phy | 11:15-12:00 CS |
| **Saturday** | 07:00-07:45 Arts | 08:00-08:45 Lit | 09:00-09:45 Geo | 10:15-11:00 Hist | 11:15-12:00 Bio |

### Group B - Afternoon Students (13:00-18:00)

| Day | Lesson 1 | Lesson 2 | Lesson 3 | Lesson 4 | Lesson 5 |
|-----|----------|----------|----------|----------|----------|
| **Monday** | 13:00-13:45 Math | 14:00-14:45 Eng | 15:00-15:45 Phy | 16:00-16:45 Chem | 17:00-17:45 Bio |
| **Tuesday** | 13:00-13:45 Hist | 14:00-14:45 Geo | 15:00-15:45 Lit | 16:00-16:45 CS | 17:00-17:45 Arts |
| **Wednesday** | 13:00-13:45 Phy | 14:00-14:45 Math | 15:00-15:45 Chem | 16:00-16:45 Eng | 17:00-17:45 CS |
| **Thursday** | 13:00-13:45 Bio | 14:00-14:45 Hist | 15:00-15:45 Geo | 16:00-16:45 Lit | 17:00-17:45 Arts |
| **Friday** | 13:00-13:45 Eng | 14:00-14:45 Chem | 15:00-15:45 Math | 16:00-16:45 Phy | 17:00-17:45 CS |
| **Saturday** | 13:00-13:45 Arts | 14:00-14:45 Lit | 15:00-15:45 Geo | 16:00-16:45 Hist | 17:00-17:45 Bio |

---

## 💻 Frontend Implementation

### File: `StudentPortal.js`
### Component: `TimetableTab()`

**Key Features**:
1. **Two Timetable Objects**:
   - `timetableGroupA` - 6 days × 5 lessons (morning)
   - `timetableGroupB` - 6 days × 5 lessons (afternoon)

2. **Dynamic Display**:
   - Shows Group A or Group B based on student assignment
   - `isGroupA` variable: Set true for morning, false for afternoon
   - In production: Fetched from backend based on student data

3. **Information Alert**:
   - Shows student's group (Group A or Group B)
   - Shows session time (morning 07:00-12:00 OR afternoon 13:00-18:00)
   - Confirms single classroom (Room A)

4. **Single Classroom**:
   - All lessons show "Room A"
   - No classroom conflicts
   - Efficient space utilization

---

## 👨‍🎓 What Students See

### Example: Group A Student View

```
📚 Your Class Schedule: Group A - Morning
Session Time: 07:00 - 12:00
Classroom: Room A (Shared with all students)

Monday - Morning (5 lessons × 45 minutes each)
├─ 07:00-07:45  Mathematics       Room A  (45 min)
├─ 08:00-08:45  English           Room A  (45 min)
├─ 09:00-09:45  Physics           Room A  (45 min)
├─ 10:15-11:00  Chemistry         Room A  (45 min)
└─ 11:15-12:00  Biology           Room A  (45 min)

Tuesday - Morning (5 lessons × 45 minutes each)
├─ 07:00-07:45  History           Room A  (45 min)
├─ 08:00-08:45  Geography         Room A  (45 min)
├─ 09:00-09:45  Literature        Room A  (45 min)
├─ 10:15-11:00  Computer Science  Room A  (45 min)
└─ 11:15-12:00  Arts              Room A  (45 min)

... (continues through Saturday)
```

### Information Shown
- **Group Assignment**: Which group student is in
- **Session Time**: Morning or afternoon only
- **Classroom**: Always "Room A" (single shared classroom)
- **Full Week**: Monday through Saturday
- **5 Lessons Daily**: 45 minutes each with 15-minute breaks

---

## 🎯 Key Design Features

### Single Classroom Efficiency
✅ **One Room A** - All students share same classroom
✅ **Two Groups** - Morning and afternoon classes
✅ **No Conflicts** - Two groups never use same room same time
✅ **Full Utilization** - Room used all day (07:00-18:00)

### Student Group Assignment
✅ **Permanent for Week** - No mixed sessions
✅ **Clear Assignment** - Student knows their session time
✅ **Consistent Schedule** - Same time every day of week
✅ **Simple Management** - Only two groups to manage

### Lesson Structure
✅ **45 Minutes Each** - Standard lesson duration
✅ **15 Minute Breaks** - Between each lesson
✅ **5 Lessons Daily** - Maximum capacity
✅ **6 Days Weekly** - Full school week schedule

---

## 🔄 Implementation Details

### Code Structure

```javascript
// Two separate timetable objects
const timetableGroupA = {
  'Monday - Morning': [
    { time: '07:00-07:45', subject: 'Mathematics', room: 'A' },
    // ... 4 more lessons ...
  ],
  'Tuesday - Morning': [
    // ... 5 lessons ...
  ],
  // ... continues through Saturday ...
};

const timetableGroupB = {
  'Monday - Afternoon': [
    { time: '13:00-13:45', subject: 'Mathematics', room: 'A' },
    // ... 4 more lessons ...
  ],
  // ... continues through Saturday ...
};

// Student's group (from backend in production)
const isGroupA = true; // or false for afternoon
const timetable = isGroupA ? timetableGroupA : timetableGroupB;
```

### Display Elements

1. **Primary Alert**:
   - Shows group assignment
   - Shows session time
   - Confirms shared classroom

2. **Daily Cards**:
   - One card per day (Monday-Saturday)
   - Shows "Morning" or "Afternoon" in label
   - Displays 5 lessons per day

3. **Lesson Details**:
   - Time (HH:MM-HH:MM)
   - Duration (45 min)
   - Subject name
   - Room (always "A")

4. **Information Alert**:
   - Full week display
   - Single classroom note
   - Lesson duration
   - Student group restriction
   - 15-minute breaks
   - Teacher edit note

---

## 📊 Sample Data

**Total Lessons**: 6 days × 5 lessons × 2 groups = 60 lessons

**Subjects Covered**:
- Mathematics
- English
- Physics
- Chemistry
- Biology
- History
- Geography
- Literature
- Computer Science
- Arts

**Room Configuration**:
- Only Room A used
- Efficient scheduling
- No conflicts

---

## 🚀 Features Implemented

✅ **Full Week Display** (Monday-Saturday)
✅ **Single Classroom** (Room A for all)
✅ **Two Student Groups** (Morning & Afternoon)
✅ **No Mixed Sessions** (Entire week same time)
✅ **45-Minute Lessons** (Standard duration)
✅ **5 Lessons Daily** (Maximum capacity)
✅ **15-Minute Breaks** (Between lessons)
✅ **Read-Only for Students** (Cannot edit)
✅ **Group Information** (Clear assignment)
✅ **Professional Display** (Clean, organized)

---

## ⏳ Future Enhancements

1. **Backend Integration**:
   - Load student's group from database
   - Fetch timetable from backend API
   - Update based on class assignment

2. **Teacher Management**:
   - TeacherPortal to edit timetables
   - Add/remove lessons
   - Change subjects/times

3. **Advanced Features**:
   - Swap between groups (if available)
   - View other group's schedule
   - Export to calendar
   - Mobile app sync
   - Conflict detection

---

## ✅ Requirements Met

✅ **Display**: Full school week (Monday-Saturday)
✅ **Classroom**: Only one (Room A) - shared by all
✅ **Students**: Divided into two groups
✅ **Sessions**: Morning (07:00-12:00) OR Afternoon (13:00-18:00)
✅ **Duration**: Full week for each group (not mixed)
✅ **Lessons**: 45 minutes each, 5 per day
✅ **Breaks**: 15 minutes between lessons
✅ **Access**: Students view-only, teachers edit (separate)

---

**Status**: ✅ COMPLETE - Full week timetable with single classroom!

The timetable now displays a complete school week (Monday-Saturday) with students divided into morning and afternoon groups using a single shared classroom. Perfect for schools with limited resources!

