# Testing Profile Save Feature

## Prerequisites
- Backend application is running (Spring Boot on port 8080)
- Frontend application is running (React on port 3000)
- Database is properly configured with MySQL

## Steps to Test

### 1. Login as Student
- Navigate to http://localhost:3000/login
- Enter student credentials
- Click "Login"
- You should be redirected to the Student Portal

### 2. Navigate to Profile Tab
- In the Student Portal, look for the "Profile" tab
- Click on it to view the profile information

### 3. Edit Profile
- Click the "Edit Profile" button
- Modify the following editable fields:
  - Date of Birth
  - Gender
  - Address
  - Father's Name
  - Father's Phone
  - Mother's Name
  - Mother's Phone

### 4. Save Changes
- Click the "Save" button
- You should see a green success alert: "Profile updated successfully!"

### 5. Verify Changes in Database
- Open your MySQL client
- Query the students table:
  ```sql
  SELECT * FROM students WHERE id = <student_id> LIMIT 1;
  ```
- Verify that the changed fields are updated in the database

### 6. Test Error Handling (Optional)
- Stop the backend server
- Try to edit and save profile
- You should see a red error alert with a message like "Failed to save profile"
- Start the backend server again

### 7. Test Page Refresh
- Edit and save profile
- Refresh the page (Ctrl+F5)
- The profile data should still show the saved values
- This confirms the data was persisted in the database

## Expected Behavior

### Success Flow
1. Profile loads with current student data
2. User can edit available fields
3. Click Save
4. API call is made to backend
5. Database is updated
6. Success alert appears
7. Profile remains in edit mode (user must click Cancel or refresh to see read-only view)

### Error Flow
1. Network error occurs during save
2. Error alert displays with error message
3. Profile data is not changed
4. User can try again

## Database Schema

The student profile is stored in the `students` table:
```sql
CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    admission_number VARCHAR(50) NOT NULL UNIQUE,
    date_of_birth DATE,
    gender VARCHAR(20),
    blood_group VARCHAR(10),
    class_name VARCHAR(50),
    section VARCHAR(10),
    date_of_admission DATE,
    status VARCHAR(20),
    father_name VARCHAR(100),
    father_phone VARCHAR(20),
    father_occupation VARCHAR(100),
    mother_name VARCHAR(100),
    mother_phone VARCHAR(20),
    mother_occupation VARCHAR(100),
    address TEXT,
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(10),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relation VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Troubleshooting

### Profile doesn't load
- Check browser console for errors (F12)
- Verify backend is running on http://localhost:8080
- Check network tab in DevTools to see if API call was made
- Verify user has userId in localStorage (check Application tab)

### Save fails with error
- Check backend logs for exceptions
- Verify student record exists in database
- Check if user has STUDENT role in authentication token

### Data doesn't persist after refresh
- Verify MySQL database connection is active
- Check if backend is actually receiving the update request
- Look at backend logs for any errors during update

## Related Files
- Frontend: `/frontend/src/pages/StudentPortal.js` (ProfileTab component)
- Frontend: `/frontend/src/services/dataService.js` (studentService)
- Backend: `/backend/src/main/java/com/schoolmanagement/controller/StudentController.java`
- Backend: `/backend/src/main/java/com/schoolmanagement/service/StudentService.java`
- Backend: `/backend/src/main/java/com/schoolmanagement/repository/StudentRepository.java`

