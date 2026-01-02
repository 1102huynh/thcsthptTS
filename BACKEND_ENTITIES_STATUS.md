# ✅ BACKEND ENTITIES - COMPLETION STATUS

## 📁 **CORRECT PACKAGE:** `com.schoolmanagement.entity`

---

## ✅ **CREATED ENTITIES (5/10)** - 50% Complete

### **1. GradeLevel.java** ✅
- **Path:** `backend/src/main/java/com/schoolmanagement/entity/GradeLevel.java`
- **Maps to:** grade_levels table
- **Features:**
  - Khối 6-12 management
  - SchoolType enum (THCS/THPT)
  - Relationships: classes[], students[]
  - Helper methods: isMiddleSchool(), isHighSchool()

### **2. SchoolClass.java** ✅
- **Path:** `backend/src/main/java/com/schoolmanagement/entity/SchoolClass.java`
- **Maps to:** classes table
- **Features:**
  - Lớp học management (6A, 10A1, etc.)
  - Homeroom teacher (GVCN) assignment
  - Student capacity tracking
  - Auto-calculate occupancy rate
  - Relationships: students[], subjectAssignments[]
  - Helper methods: isFull(), getAvailableSlots(), getOccupancyRate()

### **3. Subject.java** ✅
- **Path:** `backend/src/main/java/com/schoolmanagement/entity/Subject.java`
- **Maps to:** subjects table
- **Features:**
  - Môn học management
  - Coefficient (hệ số) support
  - SchoolType (THCS/THPT/BOTH)
  - Category classification
  - Relationships: classAssignments[], teacherSpecializations[]
  - Helper methods: getCoefficientValue(), isForMiddleSchool()

### **4. ClassSubjectAssignment.java** ✅
- **Path:** `backend/src/main/java/com/schoolmanagement/entity/ClassSubjectAssignment.java`
- **Maps to:** class_subject_assignments table
- **Features:**
  - Teacher-subject-class assignment
  - Semester support (HK1/HK2)
  - Periods per week tracking
  - Academic year tracking
  - Helper methods: getSemesterDisplay(), getTotalPeriodsInSemester()

### **5. TeacherSpecialization.java** ✅
- **Path:** `backend/src/main/java/com/schoolmanagement/entity/TeacherSpecialization.java`
- **Maps to:** teacher_specializations table
- **Features:**
  - Teacher subject expertise
  - Certification level (Giỏi/Khá/Trung bình)
  - Years of experience
  - Primary specialization flag
  - Helper methods: getExperienceLevel(), isPrimarySpecialization()

---

## ⏳ **TO CREATE (5/10)** - Next Steps

### **6. Update Student.java** 🔴 PRIORITY!
- **Action:** Add new fields
- **New Fields:**
  ```java
  @ManyToOne
  @JoinColumn(name = "grade_level_id")
  private GradeLevel gradeLevel;
  
  @ManyToOne
  @JoinColumn(name = "class_id")
  private SchoolClass schoolClass;
  
  @Column(name = "academic_year")
  private String academicYear;
  ```
- **Deprecate:** className, section (keep for migration)

### **7. Update Staff.java** 🔴 PRIORITY!
- **Action:** Add relationships
- **New Fields:**
  ```java
  @OneToMany(mappedBy = "homeroomTeacher")
  private List<SchoolClass> homeroomClasses;
  
  @OneToMany(mappedBy = "teacher")
  private List<ClassSubjectAssignment> subjectAssignments;
  
  @OneToMany(mappedBy = "teacher")
  private List<TeacherSpecialization> specializations;
  ```

### **8. Grade.java** 🟠 NEW
- **Purpose:** Store student grades
- **Fields:** student, subject, semester, score, grade_letter, academic_year

### **9. Attendance.java** 🟠 NEW
- **Purpose:** Store attendance records
- **Fields:** student, date, status (Present/Absent/Late), remarks

### **10. Timetable.java** 🟡 LATER
- **Purpose:** Store class timetables
- **Already has table:** timetables
- **Fields:** class, subject, teacher, day_of_week, period_number, room

---

## 📊 **ENTITY RELATIONSHIPS**

```
GradeLevel (Khối)
├── classes[] → SchoolClass
└── students[] → Student

SchoolClass (Lớp)
├── gradeLevel → GradeLevel
├── homeroomTeacher → Staff (GVCN)
├── students[] → Student
└── subjectAssignments[] → ClassSubjectAssignment

Subject (Môn học)
├── classAssignments[] → ClassSubjectAssignment
└── teacherSpecializations[] → TeacherSpecialization

ClassSubjectAssignment (Phân công)
├── schoolClass → SchoolClass
├── subject → Subject
└── teacher → Staff

Staff (Giáo viên)
├── homeroomClasses[] → SchoolClass (GVCN)
├── subjectAssignments[] → ClassSubjectAssignment
└── specializations[] → TeacherSpecialization

Student (Học sinh)
├── gradeLevel → GradeLevel
└── schoolClass → SchoolClass
```

---

## 🎯 **NEXT IMMEDIATE STEPS**

### **Step 1: Update Student.java** (15 minutes)
```bash
# Add 3 new fields + relationships
```

### **Step 2: Update Staff.java** (10 minutes)
```bash
# Add 3 new relationships
```

### **Step 3: Create Repositories** (30 minutes)
```bash
# Create JPA repositories for all 5 entities
```

### **Step 4: Create Services** (1 hour)
```bash
# Business logic layer
```

### **Step 5: Create Controllers** (1 hour)
```bash
# REST APIs
```

---

## ⚠️ **IMPORTANT NOTES**

### **1. Delete Old Package**
The `com.schoolmanagement.model` package can be deleted:
```
backend/src/main/java/com/schoolmanagement/model/
```

### **2. Package Structure**
All entities MUST be in:
```
backend/src/main/java/com/schoolmanagement/entity/
```

### **3. Enum Location**
Enums are defined INSIDE entity classes:
- `GradeLevel.SchoolType`
- `Subject.SchoolType`
- `Subject.SubjectCategory`
- `TeacherSpecialization.CertificationLevel`

---

## 📝 **COMPLETION CHECKLIST**

- [x] GradeLevel.java
- [x] SchoolClass.java
- [x] Subject.java
- [x] ClassSubjectAssignment.java
- [x] TeacherSpecialization.java
- [ ] Update Student.java
- [ ] Update Staff.java
- [ ] Grade.java (later)
- [ ] Attendance.java (later)
- [ ] Timetable.java (later)

**Progress:** 50% Complete (5/10)

---

**Last Updated:** 2025-12-30 20:35  
**Current Status:** ✅ 5 core entities created  
**Next Action:** Update Student & Staff entities  
**ETA for Phase 2:** 2 more hours
