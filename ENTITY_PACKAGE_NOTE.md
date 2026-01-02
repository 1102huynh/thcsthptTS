# ⚠️ IMPORTANT NOTE - ENTITY PACKAGE LOCATION

## 📁 **CORRECT PACKAGE NAME**

The project uses `com.schoolmanagement.entity` NOT `com.schoolmanagement.model`

## 🔄 **FILES NEED TO BE MOVED:**

### **Created in WRONG location** (`model` package):
1. ❌ `model/GradeLevel.java`
2. ❌ `model/SchoolClass.java`
3. ❌ `model/Subject.java`
4. ❌ `model/ClassSubjectAssignment.java`
5. ❌ `model/TeacherSpecialization.java`

### **Should be in CORRECT location** (`entity` package):
1. ✅ `entity/GradeLevel.java`
2. ✅ `entity/SchoolClass.java`
3. ✅ `entity/Subject.java`
4. ✅ `entity/ClassSubjectAssignment.java`
5. ✅ `entity/TeacherSpecialization.java`

## 🛠️ **TO FIX:**

### **Option 1: Manual Move**
- Delete `backend/src/main/java/com/schoolmanagement/model/` folder
- Recreate all files in `backend/src/main/java/com/schoolmanagement/entity/`
- Change package name from `package com.schoolmanagement.model;` to `package com.schoolmanagement.entity;`

### **Option 2: Let me recreate**
- I will recreate all 5 files in correct location
- With correct package name

## 📝 **ENTITIES TO CREATE/UPDATE:**

### **New Entities (in entity package):**
1. GradeLevel.java
2. SchoolClass.java
3. Subject.java
4. ClassSubjectAssignment.java
5. TeacherSpecialization.java
6. Timetable.java (later)

### **Update Existing:**
7. Student.java - Add gradeLevel, schoolClass, academicYear
8. Staff.java - Add relationships

---

**Action:** Should I recreate all files in correct `entity` package?
