# Quick Start Guide - Profile Save Feature

## For Developers

### What Was Fixed
Students can now edit their profile and have changes saved to the database.

**Before**: Profile edit showed a success message but didn't save anything.
**After**: Profile edit saves data to the database and persists across page refreshes.

### How It Works

```
User edits profile in browser
           ↓
Clicks "Save" button
           ↓
Frontend calls: PUT /api/v1/students/{studentId}
           ↓
Backend updates Student record in database
           ↓
Returns updated StudentDTO
           ↓
Frontend shows success alert
           ↓
Data persists in database
```

### Key Components

#### Backend API Endpoint
```
GET /api/v1/students/user/{userId}
- Fetches student profile by user ID
- Returns: StudentDTO with all student information

PUT /api/v1/students/{id}
- Updates student profile
- Returns: Updated StudentDTO
```

#### Frontend Service
```javascript
studentService.getByUserId(userId)  // Fetch profile
studentService.update(id, data)     // Save profile
```

#### Component
```javascript
StudentPortal > ProfileTab
```

### What Gets Saved

✅ These fields are saved to database:
- Date of Birth
- Gender
- Address
- Father's Name & Phone
- Mother's Name & Phone

❌ These fields are NOT saved (read-only):
- First Name / Last Name
- Email
- Student ID / Roll Number
- Class / Section

### To Test Locally

1. **Start Backend**
   ```bash
   # In backend folder
   mvn spring-boot:run
   # Or use IntelliJ: Ctrl+F10 (while file is open)
   ```

2. **Start Frontend**
   ```bash
   # In frontend folder
   npm start
   ```

3. **Test Feature**
   - Navigate to http://localhost:3000
   - Login as student
   - Go to Profile tab
   - Click "Edit Profile"
   - Change some fields
   - Click "Save"
   - See success message
   - Refresh page (Ctrl+F5)
   - Verify changes persist

### Quick Debugging

#### Profile doesn't load
- Check browser console (F12 → Console)
- Check if backend is running
- Check network tab (F12 → Network)
- Look for 404 or 500 errors

#### Save fails
- Check backend console for errors
- Look at network response (F12 → Network)
- Check if you're logged in as STUDENT role
- Verify student record exists in database

#### Changes don't persist after refresh
- Check MySQL database
- Run: `SELECT * FROM students WHERE id = ?`
- Verify `updated_at` timestamp is recent
- Check backend logs for errors

### Code Locations

Quick reference for where things are:

**Backend**
- Repository: `/backend/src/main/java/com/schoolmanagement/repository/StudentRepository.java`
  - New method: `findByUserId(Long userId)`

- Service: `/backend/src/main/java/com/schoolmanagement/service/StudentService.java`
  - New method: `getStudentByUserId(Long userId)`

- Controller: `/backend/src/main/java/com/schoolmanagement/controller/StudentController.java`
  - New endpoint: `GET /v1/students/user/{userId}`

**Frontend**
- Service: `/frontend/src/services/dataService.js`
  - New method: `studentService.getByUserId(userId)`
  - Fixed: All API paths to include `/v1` prefix

- Component: `/frontend/src/pages/StudentPortal.js`
  - ProfileTab component (lines 135-400)
  - useEffect hook to load data
  - handleSave function with API call

### Database Schema

Student profile data stored in `students` table:
```sql
students
├── id (PRIMARY KEY)
├── user_id (FOREIGN KEY to users)
├── roll_number
├── admission_number
├── date_of_birth ✓ (editable)
├── gender ✓ (editable)
├── blood_group
├── class_name
├── section
├── date_of_admission
├── status
├── father_name ✓ (editable)
├── father_phone ✓ (editable)
├── father_occupation
├── mother_name ✓ (editable)
├── mother_phone ✓ (editable)
├── mother_occupation
├── address ✓ (editable)
├── city
├── state
├── postal_code
├── emergency_contact_name
├── emergency_contact_phone
├── emergency_contact_relation
├── created_at
└── updated_at (AUTO UPDATE)
```

### Common Tasks

#### Add a new editable field

1. **Frontend** - Add to ProfileTab form:
   ```javascript
   <Form.Control
     type="text"
     value={profileData.newField}
     onChange={(e) => handleInputChange('newField', e.target.value)}
   />
   ```

2. **Frontend** - Add to updateData in handleSave:
   ```javascript
   const updateData = {
     // ... existing fields
     newField: profileData.newField
   };
   ```

3. **Frontend** - Add to initial state:
   ```javascript
   const [profileData, setProfileData] = useState({
     // ... existing fields
     newField: student.newField || 'default value'
   });
   ```

4. **Backend** - Field already exists in Student entity
   (if it's a new database column, add it to the entity and migrate database)

#### Check recent profile updates

```sql
SELECT id, user_id, roll_number, updated_at 
FROM students 
ORDER BY updated_at DESC 
LIMIT 10;
```

#### Clear test data

```sql
-- Reset a student's profile to default
UPDATE students 
SET address = 'Default Address',
    father_name = 'Default Father',
    mother_name = 'Default Mother',
    updated_at = NOW()
WHERE id = 1;
```

### API Response Examples

#### GET /api/v1/students/user/5
```json
{
  "id": 1,
  "rollNumber": "STU001",
  "user": {
    "id": 5,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@school.com"
  },
  "dateOfBirth": "2009-01-15",
  "gender": "Male",
  "address": "123 Main Street",
  "fatherName": "Mr. Doe",
  "fatherPhone": "+84 900 000 001",
  "motherName": "Mrs. Doe",
  "motherPhone": "+84 900 000 002",
  "className": "10A",
  "section": "A",
  "updatedAt": "2024-11-22T16:00:00"
}
```

#### PUT /api/v1/students/1
**Request**:
```json
{
  "dateOfBirth": "2009-01-15",
  "gender": "Male",
  "address": "123 Updated Street",
  "fatherName": "Mr. Updated Doe",
  "fatherPhone": "+84 900 111 111",
  "motherName": "Mrs. Updated Doe",
  "motherPhone": "+84 900 222 222"
}
```

**Response**: Same as GET, with updated fields

### Documentation Files

For more detailed information:
- `PROFILE_SAVE_FIX_SUMMARY.md` - Feature summary
- `COMPLETE_IMPLEMENTATION_SUMMARY.md` - Technical details
- `TESTING_PROFILE_SAVE.md` - Testing procedures
- `API_DOCUMENTATION_PROFILE.md` - API reference
- `IMPLEMENTATION_VERIFICATION_CHECKLIST.md` - Verification checklist

### Support

If you need to debug or extend this feature:

1. **Check logs**
   - Backend: Application console output
   - Frontend: Browser DevTools Console (F12)
   - Database: MySQL error log

2. **Use breakpoints**
   - Backend: Set breakpoints in StudentService.getStudentByUserId()
   - Frontend: Set breakpoints in ProfileTab useEffect

3. **Test with Postman**
   - Import backend endpoints
   - Test with Bearer token
   - Verify request/response format

4. **Check network requests**
   - F12 → Network tab
   - Look for GET /api/v1/students/user/{userId}
   - Look for PUT /api/v1/students/{id}
   - Check status codes and responses

---

**Last Updated**: November 22, 2025
**Status**: Production Ready

