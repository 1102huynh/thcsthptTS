# 📅 TIMETABLE - Updated Requirements & Implementation

**Status**: ✅ Updated with Realistic Requirements

---

## 🎯 Updated Requirements

### Lesson Duration
- **Each lesson lasts**: 45 minutes
- **Breaks between lessons**: 15 minutes (automatically scheduled)

### Daily Schedule Sessions
- **Morning Session**: 07:00 - 12:00
- **Afternoon Session**: 13:00 - 18:00
- **Maximum lessons per session**: 5

### Important Rule
- **Students study ONLY morning OR afternoon** - NOT both in the same day

---

## 📅 Sample Schedule Structure

### Morning Session Example (07:00 - 12:00)
```
Lesson 1: 07:00 - 07:45 (45 minutes)  → Break (15 min)
Lesson 2: 08:00 - 08:45 (45 minutes)  → Break (15 min)
Lesson 3: 09:00 - 09:45 (45 minutes)  → Break (15 min)
Lesson 4: 10:15 - 11:00 (45 minutes)  → Break (15 min)
Lesson 5: 11:15 - 12:00 (45 minutes)  ✓ SESSION ENDS
```

### Afternoon Session Example (13:00 - 18:00)
```
Lesson 1: 13:00 - 13:45 (45 minutes)  → Break (15 min)
Lesson 2: 14:00 - 14:45 (45 minutes)  → Break (15 min)
Lesson 3: 15:00 - 15:45 (45 minutes)  → Break (15 min)
Lesson 4: 16:00 - 16:45 (45 minutes)  → Break (15 min)
Lesson 5: 17:00 - 17:45 (45 minutes)  ✓ SESSION ENDS
```

---

## 💻 Frontend Implementation

### File: `StudentPortal.js`
### Component: `TimetableTab()`

**Data Structure**:
```javascript
const timetable = {
  'Monday - Morning': [
    { time: '07:00-07:45', subject: 'Mathematics', room: '101' },
    { time: '08:00-08:45', subject: 'English', room: '105' },
    { time: '09:00-09:45', subject: 'Physics', room: '201' },
    { time: '10:15-11:00', subject: 'Chemistry', room: '202' },
    { time: '11:15-12:00', subject: 'Biology', room: '203' },
  ],
  'Monday - Afternoon': [
    { time: '13:00-13:45', subject: 'History', room: '104' },
    { time: '14:00-14:45', subject: 'Geography', room: '106' },
    { time: '15:00-15:45', subject: 'Literature', room: '107' },
    { time: '16:00-16:45', subject: 'Computer Science', room: '108' },
    { time: '17:00-17:45', subject: 'Arts', room: '109' },
  ],
};
```

**Display Format**:
- Day and Session label (e.g., "Monday - Morning")
- "5 lessons × 45 minutes each" subtitle
- Each lesson shows:
  - Time (07:00-07:45)
  - Duration note ("45 min")
  - Subject name (Mathematics)
  - Room number (Badge: "Room 101")

**Information Alert**:
Shows students:
- Each lesson lasts 45 minutes
- Students study either morning OR afternoon (not both)
- Maximum 5 lessons per session
- Breaks are included in time gaps
- Teachers can edit through Teacher Portal

---

## 👨‍🎓 Student View

### What Students See
1. **Timetable cards** organized by day and session
2. **5 lessons maximum** per session (morning or afternoon)
3. **Lesson details**: Time, Subject, Room
4. **Duration info**: Shows "45 min" for each lesson
5. **Clear explanation**: Information note about requirements

### Permissions
- ✅ View the timetable
- ❌ Edit anything
- ❌ Add lessons
- ❌ Delete lessons

---

## 👨‍🏫 Teacher Management (Future)

### TeacherPortal Features (To Be Implemented)
1. **View timetable** - See all classes and schedules
2. **Add lessons** - Create new class sessions
   - Constraint: Max 5 per session
   - Constraint: 45 minutes each
   - Constraint: Morning OR afternoon only
3. **Edit lessons** - Modify existing classes
   - Keep 45-minute duration
   - Manage room assignments
   - Update subject names
4. **Delete lessons** - Remove classes from timetable
5. **Validation** - System enforces:
   - 45-minute duration
   - 15-minute breaks
   - Max 5 lessons per session
   - Morning/afternoon separation

---

## 📊 Example Timetable Display

### For Student in Class 10A

**Monday - Morning**
```
┌─ Monday - Morning (5 lessons × 45 minutes each) ─┐
│ 07:00-07:45  Mathematics          Room 101  45min │
│ 08:00-08:45  English              Room 105  45min │
│ 09:00-09:45  Physics              Room 201  45min │
│ 10:15-11:00  Chemistry            Room 202  45min │
│ 11:15-12:00  Biology              Room 203  45min │
└──────────────────────────────────────────────────┘
```

**Tuesday - Afternoon**
```
┌─ Tuesday - Afternoon (5 lessons × 45 minutes each) ─┐
│ 13:00-13:45  Computer Science     Room 108  45min  │
│ 14:00-14:45  History              Room 104  45min  │
│ 15:00-15:45  Literature           Room 107  45min  │
│ 16:00-16:45  Geography            Room 106  45min  │
│ 17:00-17:45  Arts                 Room 109  45min  │
└────────────────────────────────────────────────────┘
```

---

## 🔄 Timeline Calculation Algorithm

For creating timetable in backend:

```
Morning Session (07:00 - 12:00):
  Lesson 1: 07:00 + 45min = 07:45
  Break: 15 min
  Lesson 2: 08:00 + 45min = 08:45
  Break: 15 min
  Lesson 3: 09:00 + 45min = 09:45
  Break: 15 min
  Lesson 4: 10:15 + 45min = 11:00
  Break: 15 min
  Lesson 5: 11:15 + 45min = 12:00 ✓
  (Session ends at 12:00)

Afternoon Session (13:00 - 18:00):
  Lesson 1: 13:00 + 45min = 13:45
  Break: 15 min
  Lesson 2: 14:00 + 45min = 14:45
  Break: 15 min
  Lesson 3: 15:00 + 45min = 15:45
  Break: 15 min
  Lesson 4: 16:00 + 45min = 16:45
  Break: 15 min
  Lesson 5: 17:00 + 45min = 17:45 ✓
  (Session ends at 18:00)
```

---

## 🎨 UI Components

### Card Structure
- **Header**: Day and Session (e.g., "Monday - Morning")
- **Subheader**: "5 lessons × 45 minutes each"
- **Body**: List of lessons with details

### Styling
- **Time**: Dark gray text, semibold
- **Duration**: Light gray text (45 min)
- **Subject**: Dark text, bold
- **Room**: Badge with light background

### Information Alert
- Blue info alert at bottom
- Lists all key requirements
- Explains morning/afternoon separation
- Notes teacher management

---

## 📋 Data Validation Rules

When creating/editing timetables:

✅ **Lesson Duration**: Must be exactly 45 minutes
✅ **Session Time**: 
   - Morning: 07:00 - 12:00
   - Afternoon: 13:00 - 18:00
✅ **Maximum Lessons**: 5 per session
✅ **Breaks**: 15 minutes between lessons (automatic)
✅ **Morning/Afternoon**: Students only in one per day
✅ **No Overlaps**: Lessons must not overlap
✅ **Subject Required**: Each lesson must have a subject
✅ **Room Required**: Each lesson must have a room

---

## 🔄 Future Enhancements

### Phase 2 - Teacher Interface
1. Timetable management page
2. Add/edit/delete lessons with validation
3. Bulk upload timetables
4. Clone timetables for other classes

### Phase 3 - Advanced Features
1. Timetable conflict detection
2. Student/Teacher availability checking
3. Resource booking (rooms, equipment)
4. Automatic break scheduling
5. Timetable import/export (CSV, Excel)

### Phase 4 - Optimization
1. AI-based timetable generation
2. Constraint satisfaction solver
3. Performance optimization
4. Mobile app sync

---

## ✅ Status

- ✅ Frontend UI implemented with realistic requirements
- ✅ 45-minute lesson duration enforced in display
- ✅ Morning/afternoon sessions clearly separated
- ✅ Maximum 5 lessons per session shown
- ✅ Student view is read-only
- ✅ Information alert explains all requirements
- ⏳ Backend timetable API (future)
- ⏳ Teacher edit interface (future)
- ⏳ Validation constraints (future)

---

**Result**: Timetable now reflects realistic school schedule with proper lesson duration, session management, and access control!

